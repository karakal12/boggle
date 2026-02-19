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

public class DonutActivity extends AppCompatActivity {
    private DonutRenderer renderer;
    private SurfaceView surfaceView;

    private ScaleGestureDetector scaleDetector;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surfaceView = new SurfaceView(this);
        setContentView(surfaceView);
        scaleDetector = new ScaleGestureDetector(this, new OnScaleListener());

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                renderer = new DonutRenderer(surfaceView);
                renderer.startRender();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                renderer.surfaceChanged(holder, format, width, height);
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
        if (renderer != null) {
            renderer.stopRender();
        }
    }

    private int activePointerId = INVALID_POINTER_ID;
    private float lastTouchX = 0;
    private float lastTouchY = 0;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
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

                float dx = touchX - lastTouchX;
                float dy = touchY - lastTouchY;

                renderer.addA(dx * DonutRenderer.A_RATE);
                renderer.addB(dy * DonutRenderer.B_RATE);

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

                final int pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId(pointerIndex);

                if (pointerId == activePointerId) {
                    // This is the active pointer going up. Choose a new
                    // active pointer and adjust it accordingly.
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

    private class OnScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            renderer.scaleDonutDistance(1 / detector.getScaleFactor());
            surfaceView.invalidate();
            return true;
        }
    }

}
