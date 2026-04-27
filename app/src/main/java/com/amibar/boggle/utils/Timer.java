package com.amibar.boggle.utils;


import android.os.Handler;
import android.os.Looper;

/**
 * A utility class to manage a countdown timer for the Boggle game.
 * It periodically notifies listeners of the elapsed time and executes a callback when the time is up.
 * Implements {@link Runnable} to run on the main thread via a {@link Handler}.
 */
public class Timer implements Runnable {
    /** The total duration of the timer in milliseconds. */
    private final long millisTime;
    /** The system time when the timer was started or resumed. */
    private final long millisTimeBegan;
    /** Handler used to schedule the next periodic update on the main UI thread. */
    private final Handler handler = new Handler(Looper.getMainLooper());
    /** Listener to be notified when the timer reaches its duration. */
    private final OnTimerEndListener onTimerEnd;
    /** Listener to be notified on every tick (increment). */
    private final OnTickListener onTick;
    /** Flag to track if the timer has been stopped or paused. */
    private boolean isStopped = false;

    /**
     * Interface definition for a callback to be invoked when the timer expires.
     */
    public interface OnTimerEndListener{
        /** Called when the elapsed time meets or exceeds the set duration. */
        void onTimerEnd();
    }

    /**
     * Interface definition for a callback to be invoked on every timer tick.
     */
    public interface OnTickListener {
        /**
         * Called periodically to report progress.
         * @param elapsedTime The total time in milliseconds since the timer started.
         */
        void onTick(long elapsedTime);
    }

    /**
     * Initializes a new Timer with the specified duration and callbacks.
     * @param timeInMillis The total duration in milliseconds.
     * @param onTick       Callback for periodic updates.
     * @param onTimerEnd   Callback for completion.
     */
    public Timer(long timeInMillis, OnTickListener onTick, OnTimerEndListener onTimerEnd) {
        this.millisTimeBegan = System.currentTimeMillis();
        this.millisTime = timeInMillis;
        this.onTick = onTick;
        this.onTimerEnd = onTimerEnd;
    }

    /**
     * The main execution loop of the timer.
     * Calculates elapsed time, notifies listeners, and schedules the next tick.
     */
    @Override
    public void run() {
        if (isStopped) return;

        long elapsedTime = System.currentTimeMillis() - millisTimeBegan;

        // Notify listener of the current progress
        if (onTick != null) {
            onTick.onTick(elapsedTime);
        }
        
        // Check if the timer has reached its final duration
        if (elapsedTime >= millisTime) {
            if (onTimerEnd != null) {
                onTimerEnd.onTimerEnd();
            }
            return;
        }
        
        // Schedule the next update.
        // The delay is calculated to align the next tick precisely with the next whole second boundary.
        handler.postDelayed(this, 1000L - (elapsedTime % 1000L));
    }

    /**
     * Starts or resumes the timer.
     */
    public void start(){
        isStopped = false;
        handler.post(this);
    }

    /**
     * Stops the timer and cancels any pending scheduled updates.
     */
    public void stop() {
        isStopped = true;
        handler.removeCallbacks(this);
    }

}
