package com.amibar.boggle.ui;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amibar.boggle.engine.DonutRenderer;

/**
 * An Activity that displays a 3D rotating donut (torus).
 * This activity handles touch events for rotating the donut and scale gestures for zooming.
 */
public class DonutActivity extends AppCompatActivity {
    /** The renderer responsible for drawing the 3D donut on the surface. */
    private DonutRenderer renderer;
    /** The SurfaceView where the donut is drawn. */
    private SurfaceView surfaceView;
    /** Detector for pinch-to-zoom gestures. */
    private ScaleGestureDetector scaleDetector;

    /** ID of the pointer currently being tracked for rotation. */
    private int activePointerId = INVALID_POINTER_ID;
    /** Last recorded X coordinate of the touch event. */
    private float lastTouchX = 0;
    /** Last recorded Y coordinate of the touch event. */
    private float lastTouchY = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surfaceView = new SurfaceView(this);
        setContentView(surfaceView);
        
        // Initialize the scale gesture detector with a custom listener
        scaleDetector = new ScaleGestureDetector(this, new OnScaleListener());

        // Add a callback to the SurfaceHolder to manage the renderer lifecycle
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                renderer = new DonutRenderer(surfaceView);
                renderer.startRender();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                if (renderer != null) {
                    renderer.surfaceChanged(holder, format, width, height);
                }
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                if (renderer != null) {
                    renderer.stopRender();
                    renderer = null;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ensure rendering is stopped when the activity is destroyed
        if (renderer != null) {
            renderer.stopRender();
        }
    }


    /**
     * Handles touch events for rotation and delegates scale gestures.
     * Implements multi-touch handling to ensure smooth rotation when multiple fingers are present.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Pass the event to the scale detector first
        scaleDetector.onTouchEvent(event);

        switch (event.getActionMasked()){
            case ACTION_DOWN: {
                int pointerIndex = event.getActionIndex();

                lastTouchX = event.getX(pointerIndex);
                lastTouchY = event.getY(pointerIndex);

                activePointerId = event.getPointerId(pointerIndex);
                break;
            }
            case ACTION_MOVE:{
                int pointerIndex = event.findPointerIndex(activePointerId);
                if (pointerIndex < 0) return false;

                float touchX = event.getX(pointerIndex);
                float touchY = event.getY(pointerIndex);

                // Calculate the movement delta since the last event
                float dx = touchX - lastTouchX;
                float dy = touchY - lastTouchY;

                // Update rotation angles in the renderer
                if (renderer != null) {
                    renderer.addA(dx * DonutRenderer.A_RATE);
                    renderer.addB(dy * DonutRenderer.B_RATE);
                }

                surfaceView.invalidate();

                lastTouchX = touchX;
                lastTouchY = touchY;
                break;
            }
            case ACTION_UP:
            case ACTION_CANCEL: {
                activePointerId = INVALID_POINTER_ID;
                break;
                }
            case ACTION_POINTER_UP:{
                // Handle another finger being lifted while the primary finger might still be down
                final int pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId(pointerIndex);

                if (pointerId == activePointerId) {
                    // This is the active pointer going up. Choose a new active pointer.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    lastTouchX = event.getX(newPointerIndex);
                    lastTouchY = event.getY(newPointerIndex);
                    activePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
    }

    /**
     * Listener class for handling pinch-to-zoom gestures.
     */
    private class OnScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        /**
         * Called when a scale gesture is detected.
         * Adjusts the distance of the donut from the viewer in the renderer.
         */
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            if (renderer != null) {
                renderer.scaleDonutDistance(1 / detector.getScaleFactor());
                surfaceView.invalidate();
            }
            return true;
        }
    }

}
