package com.amibar.boggle.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.Choreographer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.amibar.boggle.utils.PointAndDepth;
import com.amibar.boggle.utils.Quad;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * A real-time 3D renderer for a rotating torus (donut) shape.
 * This class handles the mathematical projections, lighting, and rendering
 * of a torus onto a {@link SurfaceView} using Android's {@link Canvas} API.
 * inspired by <a href="https://www.a1k0n.net/2011/07/20/donut-math.html">donut.c</a>
 */
public class DonutRenderer implements Choreographer.FrameCallback, SurfaceHolder.Callback {
    /** Default rotation rate for angle A */
    public static final double A_RATE = 0.005;
    /** Default rotation rate for angle B */
    public static final double B_RATE = 0.007;

    /** Paint used for scaling operations (filtering disabled for performance/look) */
    public static final Paint scalingPaint = new Paint();
    /** Paint used for drawing the donut segments */
    public static final Paint shapePaint = new Paint();

    private Choreographer choreographer;
    private final SurfaceView surfaceView;
    private Bitmap bitmap;
    private int screenWidth;
    private int screenHeight;

    /** The vector representing the light source direction in 3D space */
    private static final double[] lightVector = normalize(new double[]{0, 1, -1});
    /** Minimum light level for lighting calculations */
    public static final float MIN_LIGHT = 0.2f;

    /**
     * Normalizes a 3D vector to have a magnitude of 1.
     * @param vector The 3D vector to normalize.
     * @return The normalized vector.
     */
    private static double[] normalize(double[] vector) {
        double factor = 1 / Math.sqrt(Arrays.stream(vector).map(x -> x*x).sum());
        for (int i = 0; i < vector.length; i++) {
            vector[i] *= factor;
        }
        return vector;
    }

    // Geometry resolution settings
    private static final double thetaSpacing = 0.1;
    private static final double phiSpacing = 0.07;
    private static final int thetaSteps = (int) (2 * Math.PI / thetaSpacing + 1);
    private static final int phiSteps = (int) (2 * Math.PI / phiSpacing + 1);

    // Torus dimensions
    private static final double R1 = 1; // Radius of the tube
    private static final double R2 = 2; // Radius from center to tube center
    
    /** Distance from the viewer to the object center */
    private static double K2 = 10;
    /** Projection constant based on screen size */
    private double K1;

    /** Rotation angles around two axes */
    private double A = 0, B = 0;
    
    /** Flag indicating if the surface is ready for drawing */
    private boolean isSurfaceReady = false;


    /**
     * Constructs a new DonutRenderer.
     * @param surfaceView The SurfaceView where the donut will be rendered.
     */
    public DonutRenderer(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        scalingPaint.setFilterBitmap(false);
        shapePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        shapePaint.setStrokeWidth(5);
        surfaceView.getHolder().addCallback(this);
        
        // If surface is already valid, initialize immediately
        if (surfaceView.getHolder().getSurface().isValid()) {
            isSurfaceReady = true;
            initResources(surfaceView.getWidth(), surfaceView.getHeight());
        }
    }

    /**
     * Initializes or updates resources when the screen dimensions change.
     * @param width New width of the surface.
     * @param height New height of the surface.
     */
    private void initResources(int width, int height) {
        if (width <= 0 || height <= 0) return;
        screenWidth = width;
        screenHeight = height;

        // Manage bitmap lifecycle
        if (bitmap != null) {
            if (bitmap.getWidth() == width && bitmap.getHeight() == height) return;
            bitmap.recycle();
        }

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        
        // Calculate projection constant K1 based on screen width to maintain aspect ratio
        K1 = (width * K2 * 3 / (8f * (R1 + R2)));
    }

    /**
     * Main animation frame callback called by Choreographer.
     * @param frameTimeNanos The time in nanoseconds when the frame started.
     */
    @Override
    public void doFrame(long frameTimeNanos) {
        if (choreographer == null) return;

        if (isSurfaceReady) {
            int width = surfaceView.getWidth();
            int height = surfaceView.getHeight();
            if (width > 0 && height > 0) {
                initResources(width, height);

                if (bitmap != null) {
                    Canvas surfaceCanvas = surfaceView.getHolder().lockCanvas();
                    if (surfaceCanvas != null) {
                        drawDonut();
                        // Draw the back-buffer bitmap to the surface
                        surfaceCanvas.drawBitmap(bitmap, 0, 0, null);
                        surfaceView.getHolder().unlockCanvasAndPost(surfaceCanvas);
                    }
                }
            }
        }

        // Request next frame
        choreographer.postFrameCallback(this);
    }

    /**
     * Performs the math and rendering of the donut onto the internal bitmap.
     * This method handles the full 3D to 2D transformation pipeline, lighting calculations,
     * and depth sorting (Painter's algorithm) before drawing to the back-buffer.
     */
    private void drawDonut() {
        if (bitmap == null) return;

        // Create a canvas to draw on the back-buffer BITMAP
        Canvas bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.drawColor(Color.DKGRAY);

        // Precompute trigonometric values for current rotation angles to optimize performance
        double cosA = Math.cos(A), sinA = Math.sin(A);
        double cosB = Math.cos(B), sinB = Math.sin(B);

        // Generate the 3D grid of points projected into 2D screen space
        PointAndDepth[][] grid = getToroidalMap(cosA, sinA, cosB, sinB);

        // Thread-safe list to store quadrilateral faces for depth sorting
        java.util.List<Quad> quadsToDraw = Collections.synchronizedList(new java.util.ArrayList<>());

        // Use a parallel stream to distribute heavy mathematical computations across CPU cores
        IntStream.range(0, thetaSteps).parallel().forEach(thetaIndex -> {
            int nextTheta = (thetaIndex + 1) % thetaSteps;
            double theta = thetaIndex * thetaSpacing;
            double cosTheta = Math.cos(theta), sinTheta = Math.sin(theta);

            for (int phiIndex = 0; phiIndex < phiSteps; phiIndex++) {
                int nextPhi = (phiIndex + 1) % phiSteps;
                double phi = phiIndex * phiSpacing;
                double cosPhi = Math.cos(phi), sinPhi = Math.sin(phi);

                // --- Lighting Logic ---
                // 1. Define the surface normal in local coordinates
                double nx = cosTheta * cosPhi;
                @SuppressWarnings("UnnecessaryLocalVariable")
                double ny = sinTheta;
                
                // 2. Rotate the normal to match the current orientation of the torus (A and B angles)
                double rotNx = nx * (cosB * cosPhi + sinA * sinB * sinPhi) - ny * cosA * sinB;
                double rotNy = nx * (sinB * cosPhi - sinA * cosB * sinPhi) + ny * cosA * cosB;
                double rotNz = nx * cosA * sinPhi + ny * sinA;
                
                // 3. Calculate luminosity via dot product with the light source vector
                float L = (float) (lightVector[0] * rotNx + lightVector[1] * rotNy + lightVector[2] * rotNz);

                // --- Geometry Mapping ---
                // Retrieve the four projected corners of the current quad face
                PointAndDepth p1 = grid[thetaIndex][phiIndex];
                PointAndDepth p2 = grid[nextTheta][phiIndex];
                PointAndDepth p3 = grid[thetaIndex][nextPhi];
                PointAndDepth p4 = grid[nextTheta][nextPhi];
                
                // Calculate average inverse depth (1/z) for sorting (larger ooz means closer to viewer)
                double avgOoz = (p1.ooz() + p2.ooz() + p3.ooz() + p4.ooz()) * 0.25;

                // --- Styling ---
                // Calculate final brightness, hue (based on phi), and saturation (based on theta)
                float luminosity = getLuminosityWithMinLight(L);
                float hue = (phiIndex * 360f / phiSteps) % 360;
                float saturation = Math.abs(thetaIndex - thetaSteps * 0.5f) / (thetaSteps * 0.5f);
                int color = Color.HSVToColor(new float[]{hue, saturation, luminosity});

                // Define the 2D path for the quad face
                Path path = new Path();
                path.moveTo((float)p1.screenX(), (float)p1.screenY());
                path.lineTo((float)p2.screenX(), (float)p2.screenY());
                path.lineTo((float)p4.screenX(), (float)p4.screenY());
                path.lineTo((float)p3.screenX(), (float)p3.screenY());
                path.close();

                quadsToDraw.add(new Quad(path, color, avgOoz));
            }
        });
        
        // Painter's Algorithm: Sort quads by depth (back-to-front) to ensure correct occlusion
        quadsToDraw.sort(Comparator.comparingDouble(Quad::avgOoz));
        
        // Render the sorted quads to the bitmap canvas
        for (Quad quad : quadsToDraw) {
            shapePaint.setColor(quad.color());
            bitmapCanvas.drawPath(quad.path(), shapePaint);
        }
    }

    /**
     * Maps luminosity value to a specific range.
     *
     * @param L Calculated dot product luminosity.
     * @return Clamped luminosity value.
     */
    private static float getLuminosityWithMinLight(float L) {
        return Math.clamp((L * (1 - MIN_LIGHT)) + MIN_LIGHT, 0, 1);
    }

    /**
     * Converts a 3D Y coordinate to a 2D screen coordinate.
     */
    private int toScreenY(double height, double y, double ooz) {
        return (int) (height / 2 - K1 * y * ooz);
    }

    /**
     * Converts a 3D X coordinate to a 2D screen coordinate.
     */
    private int toScreenX(double width, double x, double ooz) {
        return (int) (width / 2 + K1 * x * ooz);
    }

    /**
     * Generates a grid of 3D points rotated and projected onto a 2D plane.
     * @return A 2D array of PointAndDepth objects.
     */
    public PointAndDepth[][] getToroidalMap(double cosA, double sinA, double cosB, double sinB) {
        PointAndDepth[][] grid = new PointAndDepth[thetaSteps][phiSteps];

        IntStream.range(0, thetaSteps).parallel().forEach(i -> {
            double theta = i * thetaSpacing;
            double cosTheta = Math.cos(theta), sinTheta = Math.sin(theta);
            
            // 2D Circle in the XY plane (cross-section of the torus)
            double circleX = R2 + R1 * cosTheta;
            double circleY = R1 * sinTheta;

            for (int j = 0; j < phiSteps; j++) {
                double phi = j * phiSpacing;
                double cosPhi = Math.cos(phi), sinPhi = Math.sin(phi);

                // 3D Rotation and projection math
                // Final X position after rotations
                double x = circleX * (cosB * cosPhi + sinA * sinB * sinPhi) - circleY * cosA * sinB;
                // Final Y position after rotations
                double y = circleX * (sinB * cosPhi - sinA * cosB * sinPhi) + circleY * cosA * cosB;
                // Final Z position (depth)
                double z = K2 + cosA * circleX * sinPhi + circleY * sinA;
                
                // One over Z (inverse depth)
                double ooz = 1 / z;

                // Project to screen coordinates
                int screenX = toScreenX(screenWidth, x, ooz);
                int screenY = toScreenY(screenHeight, y, ooz);

                grid[i][j] = new PointAndDepth(screenX, screenY, ooz);
            }
        });
        return grid;
    }


    /**
     * Starts the rendering loop.
     */
    public void startRender() {
        if (choreographer != null) return;
        choreographer = Choreographer.getInstance();
        choreographer.postFrameCallback(this);
    }

    /**
     * Stops the rendering loop and releases resources.
     */
    public void stopRender() {
        if (choreographer != null) {
            choreographer.removeFrameCallback(this);
            choreographer = null;
        }
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        surfaceView.getHolder().removeCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        isSurfaceReady = true;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        initResources(width, height);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isSurfaceReady = false;
    }

    /** Adds to rotation angle A */
    public void addA(double v) {
        A += v;
    }

    /** Adds to rotation angle B */
    public void addB(double v) {
        B += v;
    }

    /**
     * Scales the distance (K2) of the donut from the viewer.
     * @param v Scaling factor.
     */
    public void scaleDonutDistance(double v){
        K2 *= v;
    }
}
