package com.amibar.boggle.utils

import android.os.Handler
import android.os.Looper


/**
 * A utility class to manage a countdown timer for the Boggle game.
 * It periodically notifies listeners of the elapsed time and executes a callback when the time is up.
 * Implements [Runnable] to run on the main thread via a [Handler].
 */
class Timer(
    /** The total duration of the timer in milliseconds.  */
    private val millisTime: Long,
    /** Listener to be notified on every tick (increment).  */
    private val onTick: (millis: Long) -> Unit,
    /** Listener to be notified when the timer reaches its duration.  */
    private val onTimerEnd: () -> Unit
) : Runnable {
    /** The system time when the current session started. */
    private var millisTimeBegan: Long = 0

    /** Total time elapsed in previous sessions before the current one started. */
    private var accumulatedTime: Long = 0

    /** Handler used to schedule the next periodic update on the main UI thread.  */
    private val handler = Handler(Looper.getMainLooper())

    /** Flag to track if the timer has been stopped or paused.  */
    private var isStopped = true


    /**
     * The main execution loop of the timer.
     * Calculates elapsed time, notifies listeners, and schedules the next tick.
     */
    override fun run() {
        if (isStopped) return

        val sessionElapsedTime = System.currentTimeMillis() - millisTimeBegan
        val totalElapsedTime = accumulatedTime + sessionElapsedTime

        // Notify listener of the current progress
        onTick(totalElapsedTime)


        // Check if the timer has reached its final duration
        if (totalElapsedTime >= millisTime) {
            onTimerEnd()
            isStopped = true
            return
        }


        // Schedule the next update.
        // The delay is calculated to align the next tick precisely with the next whole second boundary.
        handler.postDelayed(this, 1000L - (totalElapsedTime % 1000L))
    }

    /**
     * Starts or resumes the timer.
     */
    fun start() {
        if (!isStopped) return
        isStopped = false
        millisTimeBegan = System.currentTimeMillis()
        handler.post(this)
    }

    /**
     * Stops the timer and cancels any pending scheduled updates.
     */
    fun stop() {
        if (isStopped) return
        accumulatedTime += System.currentTimeMillis() - millisTimeBegan
        isStopped = true
        handler.removeCallbacks(this)
    }
}
