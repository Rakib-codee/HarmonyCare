package com.harmonycare.app.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.harmonycare.app.R;

/**
 * Utility class for handling and displaying errors
 */
public class ErrorHandler {
    
    /**
     * Show error dialog
     * @param context Context
     * @param title Dialog title
     * @param message Error message
     */
    public static void showErrorDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.ok), null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    
    /**
     * Show error dialog with retry option
     * @param context Context
     * @param title Dialog title
     * @param message Error message
     * @param retryListener Retry button listener
     */
    public static void showErrorDialogWithRetry(Context context, String title, String message,
                                                DialogInterface.OnClickListener retryListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Retry", retryListener)
                .setNegativeButton(context.getString(R.string.cancel), null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    
    /**
     * Show confirmation dialog
     * @param context Context
     * @param title Dialog title
     * @param message Confirmation message
     * @param positiveListener Positive button listener
     */
    public static void showConfirmationDialog(Context context, String title, String message,
                                              DialogInterface.OnClickListener positiveListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.ok), positiveListener)
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show();
    }
    
    /**
     * Get user-friendly error message from exception
     * @param e Exception
     * @return User-friendly error message
     */
    public static String getErrorMessage(Exception e) {
        if (e == null) {
            return "An unknown error occurred";
        }
        
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            return "An error occurred: " + e.getClass().getSimpleName();
        }
        
        // Simplify common error messages
        if (message.contains("SQLite")) {
            return "Database error. Please try again.";
        }
        if (message.contains("Network")) {
            return "Network error. Please check your connection.";
        }
        if (message.contains("Location")) {
            return "Location error. Please enable location services.";
        }
        
        return message;
    }
}

