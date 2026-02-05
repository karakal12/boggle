package com.amibar.boggle.ui;


import android.annotation.SuppressLint;
import android.os.Handler;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Locale;

public class Timer implements Runnable {
    private final LinearProgressIndicator indicator;
    private final TextView timerText;
    private final long millisTime;
    private final long millisTimeBegan;
    @SuppressWarnings("Deprecated")
    private final Handler handler = new Handler();
    private Runnable onTimerEnd;

    public Timer(TextView timerText, LinearProgressIndicator indicator, long timeInMillis,
                 Runnable onTimerEnd) {
        this.indicator = indicator;
        this.millisTimeBegan = System.currentTimeMillis();
        this.millisTime = timeInMillis;
        this.timerText = timerText;
        this.onTimerEnd = onTimerEnd;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void run() {
        long elapsedTime = System.currentTimeMillis() - millisTimeBegan;
        float progress = (float) elapsedTime / millisTime;
        indicator.setProgress((int) (progress * indicator.getMax()));
        timerText.setText(formatTime(elapsedTime));
        if (elapsedTime >= millisTime) {
            timerText.setText("00:00");
            indicator.setProgress(indicator.getMax());
            onTimerEnd.run();
            return;
        }
        handler.postDelayed(this, 1000L - (elapsedTime % 1000L));
    }

    private String formatTime(long elapsedTime) {
        long remainingTime = millisTime - elapsedTime;
        long minutes = remainingTime / 60000;
        long seconds = (remainingTime % 60000) / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    public void start(){
        handler.post(this);
    }

}
