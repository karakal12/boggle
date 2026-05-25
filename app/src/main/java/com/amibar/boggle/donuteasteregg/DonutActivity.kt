package com.amibar.boggle.donuteasteregg

import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.amibar.boggle.engine.DonutRenderer

/**
 * An Activity that displays a 3D rotating donut (torus).
 * This activity handles touch events for rotating the donut and scale gestures for zooming.
 */
class DonutActivity : AppCompatActivity() {
    /** The renderer responsible for drawing the 3D donut on the surface.  */
    private var renderer: DonutRenderer? = null

    /** The SurfaceView where the donut is drawn.  */
    private var surfaceView: SurfaceView? = null

    /** Detector for pinch-to-zoom gestures.  */
    private var scaleDetector: ScaleGestureDetector? = null

    /** ID of the pointer currently being tracked for rotation.  */
    private var activePointerId = MotionEvent.INVALID_POINTER_ID

    /** Last recorded X coordinate of the touch event.  */
    private var lastTouchX = 0f

    /** Last recorded Y coordinate of the touch event.  */
    private var lastTouchY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        surfaceView = SurfaceView(this)
        setContentView(surfaceView)


        // Initialize the scale gesture detector with a custom listener
        scaleDetector = ScaleGestureDetector(this, OnScaleListener())

        // Add a callback to the SurfaceHolder to manage the renderer lifecycle
        surfaceView!!.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                renderer = DonutRenderer(surfaceView!!)
                renderer!!.startRender()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                if (renderer != null) {
                    renderer!!.surfaceChanged(holder, format, width, height)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                if (renderer != null) {
                    renderer!!.stopRender()
                    renderer = null
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure rendering is stopped when the activity is destroyed
        if (renderer != null) {
            renderer!!.stopRender()
        }
    }


    /**
     * Handles touch events for rotation and delegates scale gestures.
     * Implements multi-touch handling to ensure smooth rotation when multiple fingers are present.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Pass the event to the scale detector first
        scaleDetector!!.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val pointerIndex = event.actionIndex

                lastTouchX = event.getX(pointerIndex)
                lastTouchY = event.getY(pointerIndex)

                activePointerId = event.getPointerId(pointerIndex)
            }

            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(activePointerId)
                if (pointerIndex < 0) return false

                val touchX = event.getX(pointerIndex)
                val touchY = event.getY(pointerIndex)

                // Calculate the movement delta since the last event
                val dx = touchX - lastTouchX
                val dy = touchY - lastTouchY

                // Update rotation angles in the renderer
                if (renderer != null) {
                    renderer!!.addA(dx * DonutRenderer.A_RATE)
                    renderer!!.addB(dy * DonutRenderer.B_RATE)
                }

                surfaceView!!.invalidate()

                lastTouchX = touchX
                lastTouchY = touchY
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }

            MotionEvent.ACTION_POINTER_UP -> {
                // Handle another finger being lifted while the primary finger might still be down
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)

                if (pointerId == activePointerId) {
                    // This is the active pointer going up. Choose a new active pointer.
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    lastTouchX = event.getX(newPointerIndex)
                    lastTouchY = event.getY(newPointerIndex)
                    activePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }

        return true
    }

    /**
     * Listener class for handling pinch-to-zoom gestures.
     */
    private inner class OnScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        /**
         * Called when a scale gesture is detected.
         * Adjusts the distance of the donut from the viewer in the renderer.
         */
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (renderer != null) {
                renderer!!.scaleDonutDistance((1 / detector.getScaleFactor()).toDouble())
                surfaceView!!.invalidate()
            }
            return true
        }
    }
}