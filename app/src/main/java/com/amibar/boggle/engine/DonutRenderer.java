package com.amibar.boggle.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.Choreographer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class DonutRenderer implements Choreographer.FrameCallback, SurfaceHolder.Callback {
    public static final double scale = 4;

    public static final double OOSQRT2 = 0.70710678118654752440084436210484903928483593768847;
    public static final double SQRT2 = 1.41421356237309504880168;
    public static final Paint scalingPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private Choreographer choreographer;

    private final SurfaceView surfaceView;
    private Bitmap bitmap;

    private Rect scaledSize;
    private Rect bitmapSize;


    private double[] zBuffer;
    private int[] pixelBuffer;

    private static final double thetaSpacing = 0.07;
    private static final double phiSpacing = 0.02;

    private static final double R1 = 1;
    private static final double R2 = 2;
    private static final double K2 = 10;
    private double K1;

    private double A = 0, B = 0;
    private boolean isSurfaceReady = false;


    public DonutRenderer(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        surfaceView.getHolder().addCallback(this);
        if (surfaceView.getHolder().getSurface().isValid()) {
            isSurfaceReady = true;
            initResources((int) (surfaceView.getWidth() / scale), (int) (surfaceView.getHeight() / scale));
        }
    }

    private void initResources(int width, int height) {
        if (width <= 0 || height <= 0) return;

        if (bitmap != null) {
            if (bitmap.getWidth() == width && bitmap.getHeight() == height) return;
            bitmap.recycle();
        }

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        K1 = (width * K2 * 3 / (8f * (R1 + R2)));

        scaledSize = new Rect(0, 0, surfaceView.getWidth(), surfaceView.getHeight());
        bitmapSize = new Rect(0, 0, width, height);

        zBuffer = new double[width * height];
        pixelBuffer = new int[width * height];
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (choreographer == null) return;

        if (isSurfaceReady) {
            int width = (int) (surfaceView.getWidth() / scale);
            int height = (int) (surfaceView.getHeight() / scale);
            if (width > 0 && height > 0) {
                initResources(width, height);

                if (bitmap != null) {
                    drawDonut();
                    Canvas surfaceCanvas = surfaceView.getHolder().lockCanvas();
                    if (surfaceCanvas != null) {
                        surfaceCanvas.drawBitmap(bitmap, bitmapSize, scaledSize, scalingPaint);
                        surfaceView.getHolder().unlockCanvasAndPost(surfaceCanvas);
                    }
                }
            }
        }

        choreographer.postFrameCallback(this);
        A += 0.04;
        B += 0.02;
    }

    private void drawDonut() {
        if (bitmap == null || zBuffer == null || pixelBuffer == null) return;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Arrays.fill(zBuffer, 0.0);
        Arrays.fill(pixelBuffer, 0xff000000);

        double cosA = Math.cos(A), sinA = Math.sin(A);
        double cosB = Math.cos(B), sinB = Math.sin(B);

        DoubleStream stream = IntStream.range(0, (int) (2*Math.PI/thetaSpacing)).mapToDouble(i -> i * thetaSpacing);

        stream.forEach(theta -> {
            double cosTheta = Math.cos(theta), sinTheta = Math.sin(theta);
            double circleX = R2 + R1 * cosTheta;
            double circleY = R1 * sinTheta;

            for (double phi = 0; phi < 2 * Math.PI; phi += phiSpacing) {
                double cosPhi = Math.cos(phi), sinPhi = Math.sin(phi);

                double x = circleX * (cosB * cosPhi + sinA * sinB * sinPhi) - circleY * cosA * sinB;
                double y = circleX * (sinB * cosPhi - sinA * cosB * sinPhi) + circleY * cosA * cosB;
                double z = K2 + cosA * circleX * sinPhi + circleY * sinA;
                double ooz = 1 / z;

                int screenX = (int) ((double) width / 2 + K1 * x * ooz);
                int screenY = (int) ((double) height / 2 - K1 * y * ooz);

                if (screenX >= 0 && screenX < width && screenY >= 0 && screenY < height) {
                    double L = cosPhi * cosTheta * sinB - cosA * cosTheta * sinPhi -
                            sinA * sinTheta + cosB * (cosA * sinTheta - cosTheta * sinA * sinPhi);
                    L += SQRT2;

                    int index = screenX + screenY * width;
                    if (ooz > zBuffer[index]) {
                        zBuffer[index] = ooz;
                        int intensity = (int) (128 * L * OOSQRT2);
                        if (intensity > 255) intensity = 255;
                        int color = 0xff000000 | (intensity << 16) | (intensity << 8) | intensity;
                        pixelBuffer[index] = color;
                        }
                }
            }
        });

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
        }
        pixelBuffer = null;
        zBuffer = null;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        isSurfaceReady = true;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        initResources((int) (width / scale), (int) (height / scale));
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isSurfaceReady = false;
    }
}
