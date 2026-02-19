package com.amibar.boggle.utils;


import android.os.Handler;

/**
 * A utility class to manage a countdown timer.
 * Notifies a listener of progress every second.
 */
public class Timer implements Runnable {
    /** The total duration of the timer in milliseconds. */
    private final long millisTime;
    /** The system time when the timer was started. */
    private final long millisTimeBegan;
    /** Handler to schedule the next update. */
    @SuppressWarnings("deprecation")
    private final Handler handler = new Handler();
    private final OnTimerEndListener onTimerEnd;
    private final OnTickListener onTick;

    /**
     * Interface to receive a notification when the timer expires.
     */
    public interface OnTimerEndListener{
        void onTimerEnd();
    }

    /**
     * Interface to receive a notification on every tick.
     */
    public interface OnTickListener {
        void onTick(long elapsedTime);
    }

    /**
     * Initializes a new Timer.
     * @param timeInMillis The total countdown time in milliseconds.
     * @param onTick Callback executed on every tick.
     * @param onTimerEnd Callback executed when the timer reaches zero.
     */
    public Timer(long timeInMillis, OnTickListener onTick, OnTimerEndListener onTimerEnd) {
        this.millisTimeBegan = System.currentTimeMillis();
        this.millisTime = timeInMillis;
        this.onTick = onTick;
        this.onTimerEnd = onTimerEnd;
    }

    /**
     * Updates the state and schedules the next execution.
     */
    @Override
    public void run() {
        long elapsedTime = System.currentTimeMillis() - millisTimeBegan;

        // Notify listener of progress
        onTick.onTick(elapsedTime);
        
        // Check if timer finished
        if (elapsedTime >= millisTime) {
            onTimerEnd.onTimerEnd();
            return;
        }
        
        // Schedule next update precisely at the turn of the next second
        handler.postDelayed(this, 1000L - (elapsedTime % 1000L));
    }

    /**
     * Starts the timer execution.
     */
    public void start(){
        handler.post(this);
    }

}
