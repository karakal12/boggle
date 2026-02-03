package com.amibar.boggle;


import android.os.Handler;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class Timer implements Runnable {
    private final LinearProgressIndicator indicator;
    private final TextView timerText;
    private final long millisTime;
    private final long millisTimeBegan;
    private final Handler handler = new Handler();

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss");

    public Timer(TextView timerText, LinearProgressIndicator indicator, long millisTime) {
        this.indicator = indicator;
        this.millisTimeBegan = System.currentTimeMillis();
        this.millisTime = millisTime;
        this.timerText = timerText;
    }

    @Override
    public void run() {
        long elapsedTime = System.currentTimeMillis() - millisTimeBegan;
        float progress = (float) elapsedTime / millisTime;
        indicator.setProgress((int) (progress * indicator.getMax()));
        timerText.setText(formatter.format(Instant.ofEpochMilli(elapsedTime)));
        handler.postDelayed(this, 1000L);
    }

    public void start(){
        handler.post(this);
    }

}
