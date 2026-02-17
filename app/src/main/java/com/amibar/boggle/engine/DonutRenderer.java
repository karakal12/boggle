package com.amibar.boggle.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.Choreographer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class DonutRenderer implements Choreographer.FrameCallback, SurfaceHolder.Callback {
    public static final double OOSQRT2 = 0.70710678118654752440084436210484903928483593768847;
    private Choreographer choreographer;

    private final SurfaceView surfaceView;
    private Canvas bitmapCanvas;
    private Bitmap bitmap;

    private double[] zBuffer;

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
            initResources(surfaceView.getWidth(), surfaceView.getHeight());
        }
    }

    private void initResources(int width, int height) {
        if (width <= 0 || height <= 0) return;

        if (bitmap != null) {
            if (bitmap.getWidth() == width && bitmap.getHeight() == height) return;
            bitmap.recycle();
        }

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.drawColor(0xff000000);
        K1 = (width * K2 * 3 / (8f * (R1 + R2)));
        zBuffer = new double[width * height];
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (choreographer == null) return;

        if (isSurfaceReady) {
            int width = surfaceView.getWidth();
            int height = surfaceView.getHeight();
            if (width > 0 && height > 0) {
                initResources(width, height);

                if (bitmap != null) {
                    drawDonut();
                    Canvas surfaceCanvas = surfaceView.getHolder().lockCanvas();
                    if (surfaceCanvas != null) {
                        surfaceCanvas.drawBitmap(bitmap, 0, 0, null);
                        surfaceView.getHolder().unlockCanvasAndPost(surfaceCanvas);
                    }
                }
            }
        }

        choreographer.postFrameCallback(this);
        A += 0.01;
        B += 0.04;
    }

    private void drawDonut() {
        if (bitmapCanvas == null || bitmap == null) return;

        bitmapCanvas.drawColor(0xff000000);
        Arrays.fill(zBuffer, 0.0);

        double cosA = Math.cos(A), sinA = Math.sin(A);
        double cosB = Math.cos(B), sinB = Math.sin(B);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        for (double theta = 0; theta < 2 * Math.PI; theta += thetaSpacing) {
            double cosTheta = Math.cos(theta), sinTheta = Math.sin(theta);

            for (double phi = 0; phi < 2 * Math.PI; phi += phiSpacing) {
                double cosPhi = Math.cos(phi), sinPhi = Math.sin(phi);

                double circleX = R2 + R1 * cosTheta;
                double circleY = R1 * sinTheta;

                double x = circleX * (cosB * cosPhi + sinA * sinB * sinPhi) - circleY * cosA * sinB;
                double y = circleX * (sinB * cosPhi - sinA * cosB * sinPhi) + circleY * cosA * cosB;
                double z = K2 + cosA * circleX * sinPhi + circleY * sinA;
                double ooz = 1 / z;

                int screenX = (int) ((double) width / 2 + K1 * x * ooz);
                int screenY = (int) ((double) height / 2 - K1 * y * ooz);

                if (screenX >= 0 && screenX < width && screenY >= 0 && screenY < height) {
                    double L = cosPhi * cosTheta * sinB - cosA * cosTheta * sinPhi -
                            sinA * sinTheta + cosB * (cosA * sinTheta - cosTheta * sinA * sinPhi);

                    if (L > 0) {
                        int index = screenX + screenY * width;
                        if (ooz > zBuffer[index]) {
                            zBuffer[index] = ooz;
                            int intensity = (int) (255 * L * OOSQRT2);
                            int color = Color.argb(255, intensity, intensity, intensity);
                            bitmap.setPixel(screenX, screenY, color);
                        }
                    }
                }
            }
        }
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
        initResources(width, height);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isSurfaceReady = false;
    }
}
