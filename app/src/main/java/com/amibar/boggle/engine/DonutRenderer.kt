@file:Suppress("PrivatePropertyName")

package com.amibar.boggle.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.amibar.boggle.utils.PointAndDepth
import com.amibar.boggle.utils.Quad
import java.util.Arrays
import java.util.Collections
import java.util.stream.IntStream
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A real-time 3D renderer for a rotating torus (donut) shape.
 * This class handles the mathematical projections, lighting, and rendering
 * of a torus onto a [SurfaceView] using Android's [Canvas] API.
 * inspired by [donut.c](https://www.a1k0n.net/2011/07/20/donut-math.html)
 */
class DonutRenderer(private val surfaceView: SurfaceView) : FrameCallback, SurfaceHolder.Callback {
    private var choreographer: Choreographer? = null
    private var bitmap: Bitmap? = null
    private var screenWidth = 0
    private var screenHeight = 0

    /** Projection constant based on screen size  */
    private var K1 = 0.0

    /** Rotation angles around two axes  */
    private var A = 0.0
    private var B = 0.0

    /** Flag indicating if the surface is ready for drawing  */
    private var isSurfaceReady = false


    /**
     * Constructs a new DonutRenderer.
     * @param surfaceView The SurfaceView where the donut will be rendered.
     */
    init {
        scalingPaint.isFilterBitmap = false
        shapePaint.style = Paint.Style.FILL_AND_STROKE
        shapePaint.strokeWidth = 5f
        surfaceView.holder.addCallback(this)


        // If surface is already valid, initialize immediately
        if (surfaceView.holder.surface.isValid) {
            isSurfaceReady = true
            initResources(surfaceView.width, surfaceView.height)
        }
    }

    /**
     * Initializes or updates resources when the screen dimensions change.
     * @param width New width of the surface.
     * @param height New height of the surface.
     */
    private fun initResources(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        screenWidth = width
        screenHeight = height

        // Manage bitmap lifecycle
        if (bitmap != null) {
            if (bitmap!!.getWidth() == width && bitmap!!.getHeight() == height) return
            bitmap!!.recycle()
        }

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)


        // Calculate projection constant K1 based on screen width to maintain aspect ratio
        K1 = (width * K2 * 3 / (8f * (R1 + R2)))
    }

    /**
     * Main animation frame callback called by Choreographer.
     * @param frameTimeNanos The time in nanoseconds when the frame started.
     */
    override fun doFrame(frameTimeNanos: Long) {
        if (choreographer == null) return

        if (isSurfaceReady) {
            val width = surfaceView.width
            val height = surfaceView.height
            if (width > 0 && height > 0) {
                initResources(width, height)

                if (bitmap != null) {
                    val surfaceCanvas = surfaceView.holder.lockCanvas()
                    if (surfaceCanvas != null) {
                        drawDonut()
                        // Draw the back-buffer bitmap to the surface
                        surfaceCanvas.drawBitmap(bitmap!!, 0f, 0f, null)
                        surfaceView.holder.unlockCanvasAndPost(surfaceCanvas)
                    }
                }
            }
        }

        // Request next frame
        choreographer!!.postFrameCallback(this)
    }

    /**
     * Performs the math and rendering of the donut onto the internal bitmap.
     * This method handles the full 3D to 2D transformation pipeline, lighting calculations,
     * and depth sorting (Painter's algorithm) before drawing to the back-buffer.
     */
    private fun drawDonut() {
        if (bitmap == null) return

        // Create a canvas to draw on the back-buffer BITMAP
        val bitmapCanvas = Canvas(bitmap!!)
        bitmapCanvas.drawColor(Color.DKGRAY)

        // Precompute trigonometric values for current rotation angles to optimize performance
        val cosA = cos(A)
        val sinA = sin(A)
        val cosB = cos(B)
        val sinB = sin(B)

        // Generate the 3D grid of points projected into 2D screen space
        val grid = getToroidalMap(cosA, sinA, cosB, sinB)

        // Thread-safe list to store quadrilateral faces for depth sorting
        val quadsToDraw = Collections.synchronizedList(ArrayList<Quad>())

        // Use a parallel stream to distribute heavy mathematical computations across CPU cores
        IntStream.range(0, THETA_STEPS).parallel().forEach { thetaIndex: Int ->
            val nextTheta: Int = (thetaIndex + 1) % THETA_STEPS
            val thetaCos = thetaCosTable[thetaIndex]
            val thetaSin = thetaSinTable[thetaIndex]
            
            for (phiIndex in 0..<PHI_STEPS) {
                val nextPhi: Int = (phiIndex + 1) % PHI_STEPS
                val cosPhi = phiCosTable[phiIndex]
                val sinPhi = phiSinTable[phiIndex]

                // --- Lighting Logic ---
                // 1. Define the surface normal in local coordinates
                val nx = thetaCos * cosPhi


                // 2. Rotate the normal to match the current orientation of the torus (A and B angles)
                val rotNx = nx * (cosB * cosPhi + sinA * sinB * sinPhi) - thetaSin * cosA * sinB
                val rotNy = nx * (sinB * cosPhi - sinA * cosB * sinPhi) + thetaSin * cosA * cosB
                val rotNz = nx * cosA * sinPhi + thetaSin * sinA


                // 3. Calculate luminosity via dot product with the light source vector
                val normDotLight =
                    (lightVector[0] * rotNx + lightVector[1] * rotNy + lightVector[2] * rotNz).toFloat()

                // --- Geometry Mapping ---
                // Retrieve the four projected corners of the current quad face
                val p1 = grid[thetaIndex][phiIndex]
                val p2 = grid[nextTheta][phiIndex]
                val p3 = grid[thetaIndex][nextPhi]
                val p4 = grid[nextTheta][nextPhi]


                // Calculate average inverse depth (1/z) for sorting (larger ooz means closer to viewer)
                val avgOoz = (p1.ooz + p2.ooz + p3.ooz + p4.ooz) * 0.25

                // --- Styling ---
                // Calculate final brightness, hue (based on phi), and saturation (based on theta)
                val luminosity: Float = getLuminosityWithMinLight(normDotLight)
                val hue: Float = (phiIndex * 360f / PHI_STEPS) % 360
                val saturation: Float = abs(thetaIndex - THETA_STEPS * 0.5f) / (THETA_STEPS * 0.5f)
                val color = Color.HSVToColor(floatArrayOf(hue, saturation, luminosity))

                // Define the 2D path for the quad face
                val path = Path()
                path.moveTo(p1.screenX.toFloat(), p1.screenY.toFloat())
                path.lineTo(p2.screenX.toFloat(), p2.screenY.toFloat())
                path.lineTo(p4.screenX.toFloat(), p4.screenY.toFloat())
                path.lineTo(p3.screenX.toFloat(), p3.screenY.toFloat())
                path.close()

                quadsToDraw.add(Quad(path, color, avgOoz))
            }
        }


        // Painter's Algorithm: Sort quads by depth (back-to-front) to ensure correct occlusion
        quadsToDraw.sortWith(Comparator.comparingDouble(Quad::avgOoz))


        // Render the sorted quads to the bitmap canvas
        quadsToDraw.forEach { quad ->
            shapePaint.setColor(quad.color)
            bitmapCanvas.drawPath(quad.path, shapePaint)
        }
    }

    /**
     * Converts a 3D Y coordinate to a 2D screen coordinate.
     */
    private fun toScreenY(height: Double, y: Double, ooz: Double): Int {
        return (height / 2 - K1 * y * ooz).toInt()
    }

    /**
     * Converts a 3D X coordinate to a 2D screen coordinate.
     */
    private fun toScreenX(width: Double, x: Double, ooz: Double): Int {
        return (width / 2 + K1 * x * ooz).toInt()
    }

    /**
     * Generates a grid of 3D points rotated and projected onto a 2D plane.
     * @return A 2D array of PointAndDepth objects.
     */
    fun getToroidalMap(
        cosA: Double,
        sinA: Double,
        cosB: Double,
        sinB: Double
    ): Array<Array<PointAndDepth>> {
        val sinAsinB = sinA * sinB
        val sinAcosB = sinA * cosB
        val cosAcosB = cosA * cosB
        val cosAsinB = cosA * sinB

        return Array(THETA_STEPS) { i ->
            val circleX = circleXTable[i]
            val circleY = circleYTable[i]

            val yTermOffset = circleY * cosAcosB
            val xTermOffset = circleY * cosAsinB
            val zTermOffset = circleY * sinA

            Array(PHI_STEPS) { j ->
                val cosPhi = phiCosTable[j]
                val sinPhi = phiSinTable[j]

                val x = circleX * (cosB * cosPhi + sinAsinB * sinPhi) - xTermOffset
                val y = circleX * (sinB * cosPhi - sinAcosB * sinPhi) + yTermOffset
                val z: Double = K2 + cosA * circleX * sinPhi + zTermOffset

                val ooz = 1 / z

                val screenX = toScreenX(screenWidth.toDouble(), x, ooz)
                val screenY = toScreenY(screenHeight.toDouble(), y, ooz)

                PointAndDepth(screenX, screenY, ooz)
            }
        }
    }


    /**
     * Starts the rendering loop.
     */
    fun startRender() {
        if (choreographer != null) return
        choreographer = Choreographer.getInstance()
        choreographer!!.postFrameCallback(this)
    }

    /**
     * Stops the rendering loop and releases resources.
     */
    fun stopRender() {
        if (choreographer != null) {
            choreographer!!.removeFrameCallback(this)
            choreographer = null
        }
        if (bitmap != null) {
            bitmap!!.recycle()
            bitmap = null
        }
        surfaceView.holder.removeCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isSurfaceReady = true
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        initResources(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isSurfaceReady = false
    }

    /** Adds to rotation angle A  */
    fun addA(v: Double) {
        A += v
    }

    /** Adds to rotation angle B  */
    fun addB(v: Double) {
        B += v
    }

    /**
     * Scales the distance (K2) of the donut from the viewer.
     * @param v Scaling factor.
     */
    fun scaleDonutDistance(v: Double) {
        K2 *= v
    }

    companion object {
        /** Default rotation rate for angle A  */
        const val A_RATE: Double = 0.005

        /** Default rotation rate for angle B  */
        const val B_RATE: Double = 0.007

        /** Paint used for scaling operations (filtering disabled for performance/look)  */
        val scalingPaint: Paint = Paint()

        /** Paint used for drawing the donut segments  */
        val shapePaint: Paint = Paint()

        /** The vector representing the light source direction in 3D space  */
        private val lightVector: DoubleArray = normalize(doubleArrayOf(0.0, 1.0, -1.0))

        /** Minimum light level for lighting calculations  */
        const val MIN_LIGHT: Float = 0.2f

        /**
         * Normalizes a 3D vector to have a magnitude of 1.
         * @param vector The 3D vector to normalize.
         * @return The normalized vector.
         */
        private fun normalize(vector: DoubleArray): DoubleArray {
            val factor =
                1 / sqrt(
                    Arrays.stream(vector).map { x: Double -> x * x }
                        .sum()
                )
            for (i in vector.indices) {
                vector[i] *= factor
            }
            return vector
        }

        // Geometry resolution settings
        private const val THETA_SPACING = 0.1
        private const val PHI_SPACING = 0.07
        private const val THETA_STEPS = (2 * Math.PI / THETA_SPACING + 1).toInt()
        private const val PHI_STEPS = (2 * Math.PI / PHI_SPACING + 1).toInt()

        // Precomputed lookup tables for trigonometric values
        private val thetaCosTable = DoubleArray(THETA_STEPS) { cos(it * THETA_SPACING) }
        private val thetaSinTable = DoubleArray(THETA_STEPS) { sin(it * THETA_SPACING) }
        private val phiCosTable = DoubleArray(PHI_STEPS) { cos(it * PHI_SPACING) }
        private val phiSinTable = DoubleArray(PHI_STEPS) { sin(it * PHI_SPACING) }

        // Torus dimensions
        private const val R1 = 1.0 // Radius of the tube
        private const val R2 = 2.0 // Radius from center to tube center

        // Precomputed circle values
        private val circleXTable = DoubleArray(THETA_STEPS) { R2 + R1 * thetaCosTable[it] }
        private val circleYTable = DoubleArray(THETA_STEPS) { R1 * thetaSinTable[it] }

        /** Distance from the viewer to the object center  */
        private var K2 = 10.0

        /**
         * Maps luminosity value to a specific range.
         * 
         * @param light Calculated dot product luminosity.
         * @return Clamped luminosity value.
         */
        private fun getLuminosityWithMinLight(light: Float): Float {
            return Math.clamp((light * (1 - MIN_LIGHT)) + MIN_LIGHT, 0f, 1f)
        }
    }
}
