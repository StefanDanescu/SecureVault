package com.securevault.util;

import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Timer for automatic vault locking after inactivity.
 * 
 * Monitors user activity and triggers a lock callback
 * when the timeout is reached.
 */
public class AutoLockTimer {

    private final Runnable lockCallback;
    private Timer timer;
    private int timeoutMinutes = Constants.DEFAULT_AUTO_LOCK_MINUTES;
    private long lastActivityTime;

    public AutoLockTimer(Runnable lockCallback) {
        this.lockCallback = lockCallback;
    }

    /**
     * Starts the auto-lock timer.
     */
    public void start() {
        stop(); // Stop any existing timer
        
        lastActivityTime = System.currentTimeMillis();
        timer = new Timer(true);
        
        // Check every 30 seconds
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkInactivity();
            }
        }, 30000, 30000);
    }

    /**
     * Stops the auto-lock timer.
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Resets the inactivity timer. Call this on user activity.
     */
    public void resetTimer() {
        lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Sets the timeout in minutes.
     * 
     * @param minutes Timeout duration
     */
    public void setTimeoutMinutes(int minutes) {
        this.timeoutMinutes = minutes;
    }

    /**
     * Gets the current timeout setting.
     * 
     * @return Timeout in minutes
     */
    public int getTimeoutMinutes() {
        return timeoutMinutes;
    }

    private void checkInactivity() {
        long elapsed = System.currentTimeMillis() - lastActivityTime;
        long timeoutMs = timeoutMinutes * 60 * 1000L;

        if (elapsed >= timeoutMs) {
            Platform.runLater(() -> {
                stop();
                lockCallback.run();
            });
        }
    }
}
