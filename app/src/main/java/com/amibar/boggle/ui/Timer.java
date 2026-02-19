package com.amibar.boggle.ui;


import android.annotation.SuppressLint;
import android.os.Handler;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Locale;

/**
 * A utility class to manage a countdown timer synchronized with UI elements.
 * Updates a {@link LinearProgressIndicator} and a {@link TextView} every second.
 */
public class Timer implements Runnable {
    private final LinearProgressIndicator indicator;
    private final TextView timerText;
    /** The total duration of the timer in milliseconds. */
    private final long millisTime;
    /** The system time when the timer was started. */
    private final long millisTimeBegan;
    /** Handler to schedule the next UI update. */
    @SuppressWarnings("deprecation")
    private final Handler handler = new Handler();
    private final OnTimerEndListener onTimerEnd;

    /**
     * Interface to receive a notification when the timer expires.
     */
    public interface OnTimerEndListener{
        void onTimerEnd();
    }

    /**
     * Initializes a new Timer.
     * @param timerText The TextView to display the remaining time (MM:SS).
     * @param indicator The progress indicator to update.
     * @param timeInMillis The total countdown time in milliseconds.
     * @param onTimerEnd Callback executed when the timer reaches zero.
     */
    public Timer(TextView timerText, LinearProgressIndicator indicator, long timeInMillis,
                 OnTimerEndListener onTimerEnd) {
        this.indicator = indicator;
        this.millisTimeBegan = System.currentTimeMillis();
        this.millisTime = timeInMillis;
        this.timerText = timerText;
        this.onTimerEnd = onTimerEnd;
    }

    /**
     * Updates the UI state and schedules the next execution.
     * Calculates progress as a ratio of elapsed time to total time.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void run() {
        long elapsedTime = System.currentTimeMillis() - millisTimeBegan;
        
        // Update progress bar
        float progress = (float) elapsedTime / millisTime;
        indicator.setProgress((int) (progress * indicator.getMax()));
        
        // Update text display
        timerText.setText(formatTime(elapsedTime));
        
        // Check if timer finished
        if (elapsedTime >= millisTime) {
            timerText.setText("00:00");
            indicator.setProgress(indicator.getMax());
            onTimerEnd.onTimerEnd();
            return;
        }
        
        // Schedule next update precisely at the turn of the next second
        handler.postDelayed(this, 1000L - (elapsedTime % 1000L));
    }

    /**
     * Formats the remaining time into a MM:SS string.
     * @param elapsedTime Time elapsed since start in ms.
     * @return Formatted string.
     */
    private String formatTime(long elapsedTime) {
        long remainingTime = millisTime - elapsedTime;
        long minutes = remainingTime / 60000;
        long seconds = (remainingTime % 60000) / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    /**
     * Starts the timer execution.
     */
    public void start(){
        handler.post(this);
    }

}
