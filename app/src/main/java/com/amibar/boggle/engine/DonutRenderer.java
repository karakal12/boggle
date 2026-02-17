package com.amibar.boggle.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Choreographer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.amibar.boggle.utils.MathUtilsKt;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class DonutRenderer implements Choreographer.FrameCallback, SurfaceHolder.Callback {
    public static final int scale = 10;
    private Choreographer choreographer;

    private final SurfaceView surfaceView;
    private Canvas bitmapCanvas;
    private Bitmap bitmap;
    private @ColorInt int[] pixelBuffer;
    private Vector3D[] rayDirections;
    private Rect scaledSize;
    private Rect bitmapSize;


    private static final double R1 = 1; // minor radius
    private static final double R2 = 2; // major radius
    private static final double K2 = 100;
    public static final Vector3D ORIGIN = new Vector3D(0, 0, -K2);
    private double K1;
    private Quaternion torusRotation;
    private static final Vector3D lightDirection = new Vector3D(0, 1, -1).normalize();


    private boolean isSurfaceReady = false;

    public DonutRenderer(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        surfaceView.getHolder().addCallback(this);
        if (surfaceView.getHolder().getSurface().isValid()) {
            isSurfaceReady = true;
            initResources(surfaceView.getWidth() / scale, surfaceView.getHeight() / scale);
        }
    }

    private void initResources(int width, int height) {
        if (width <= 0 || height <= 0) return;

        if (bitmap != null) {
            if (bitmap.getWidth() == width && bitmap.getHeight() == height) return;
            bitmap.recycle();
        }

        if (pixelBuffer == null || pixelBuffer.length != width * height) {
            pixelBuffer = new int[width * height];
        }

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.drawColor(0xff000000);

        // K1 calculation: scales the object based on width
        K1 = (width * K2 * 3.0 / (8.0 * (R1 + R2)));

        scaledSize = new Rect(0, 0, surfaceView.getWidth(), surfaceView.getHeight());
        bitmapSize = new Rect(0, 0, width, height);
        if (torusRotation == null) {
            torusRotation = new Quaternion(1, 0, 0, 0);
        }

        rayDirections = new Vector3D[width * height];
        for (int i = 0; i < width * height; i++) {
            int x = i % width - width / 2;
            int y = i / width - height / 2;


            rayDirections[i] = new Vector3D(x, y, K1).normalize();
        }
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (choreographer == null) return;

        if (isSurfaceReady) {
            int width = surfaceView.getWidth() / scale;
            int height = surfaceView.getHeight() / scale;
            if (width > 0 && height > 0) {
                initResources(width, height);

                if (bitmap != null) {
                    drawDonut();
                    Canvas surfaceCanvas = surfaceView.getHolder().lockCanvas();
                    if (surfaceCanvas != null) {
                        surfaceCanvas.drawBitmap(bitmap, bitmapSize, scaledSize, null);
                        surfaceView.getHolder().unlockCanvasAndPost(surfaceCanvas);
                    }
                }
            }
        }

        choreographer.postFrameCallback(this);
        // Spin the donut
        torusRotation = torusRotation.multiply(new Quaternion(0.999, 0.01, 0.02, 0.03)).normalize();
    }

    private void drawDonut() {
        if (bitmapCanvas == null || bitmap == null) return;



        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        for (int i = 0; i < pixelBuffer.length; i++) {

            // R2 is major radius, R1 is minor radius
            double t = MathUtilsKt.rayIntersectTorus(ORIGIN, rayDirections[i], torusRotation, R2, R1);

            if (t == Double.POSITIVE_INFINITY || Double.isNaN(t)) {
                pixelBuffer[i] = 0xff000000;
                continue;
            }

            Vector3D intersection = ORIGIN.add(rayDirections[i].scalarMultiply(t));
            Vector3D normal = MathUtilsKt.getTorusNormal(intersection, torusRotation, R2);

            double lightIntensity = Math.max(0, lightDirection.dotProduct(normal));

            // Correct color bitmasking
            int luminance = (int) (lightIntensity * 200) + 55;
            @ColorInt int color = 0xff000000 | (luminance << 16) | (luminance << 8) | luminance;

            pixelBuffer[i] = color;
        }
        bitmap.setPixels(pixelBuffer, 0, width, 0, 0, width, height);
    }

    public void startRender() {
        if (choreographer != null) return;
        choreographer = Choreographer.getInstance();
        choreographer.postFrameCallback(this);
    }

    public void stopRender() {
        if (choreographer != null) {
            choreographer.removeFrameCallback(this);
            choreographer = null;
        }
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
            bitmapCanvas = null;
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        isSurfaceReady = true;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        initResources(width / scale, height / scale);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isSurfaceReady = false;
    }
}
