package com.securevault.service;

import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Clipboard service with automatic clearing for security.
 * 
 * Copies sensitive data to clipboard and automatically
 * clears it after a configurable timeout.
 */
public class ClipboardService {

    private Timer clearTimer;
    private int clearDelaySeconds = 30;

    public ClipboardService() {
        // Default constructor
    }

    /**
     * Copies text to the clipboard and schedules automatic clearing.
     * 
     * @param text The text to copy
     */
    public void copyWithAutoClear(String text) {
        // Cancel any existing timer
        if (clearTimer != null) {
            clearTimer.cancel();
        }

        // Copy to clipboard
        Platform.runLater(() -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            Clipboard.getSystemClipboard().setContent(content);
        });

        // Schedule clipboard clear
        clearTimer = new Timer(true);
        clearTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                clearClipboard();
            }
        }, clearDelaySeconds * 1000L);
    }

    /**
     * Copies text to clipboard without auto-clear.
     * 
     * @param text The text to copy
     */
    public void copy(String text) {
        Platform.runLater(() -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            Clipboard.getSystemClipboard().setContent(content);
        });
    }

    /**
     * Clears the clipboard contents.
     */
    public void clearClipboard() {
        Platform.runLater(() -> {
            ClipboardContent content = new ClipboardContent();
            content.putString("");
            Clipboard.getSystemClipboard().setContent(content);
        });
    }

    /**
     * Sets the auto-clear delay in seconds.
     * 
     * @param seconds The delay before clipboard is cleared
     */
    public void setClearDelaySeconds(int seconds) {
        this.clearDelaySeconds = seconds;
    }

    /**
     * Gets the current auto-clear delay.
     * 
     * @return The delay in seconds
     */
    public int getClearDelaySeconds() {
        return clearDelaySeconds;
    }

    /**
     * Cancels any pending clipboard clear.
     */
    public void cancelClear() {
        if (clearTimer != null) {
            clearTimer.cancel();
            clearTimer = null;
        }
    }
}
