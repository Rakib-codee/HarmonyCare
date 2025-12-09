package com.harmonycare.app.util;

import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

/**
 * Helper class for showing Snackbar messages
 */
public class SnackbarHelper {
    
    /**
     * Show success snackbar
     * @param view Root view
     * @param message Message to display
     */
    public static void showSuccess(View view, String message) {
        if (view == null) return;
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(view.getContext().getResources().getColor(
            com.harmonycare.app.R.color.success_green, null));
        setTextColor(snackbar, android.graphics.Color.WHITE);
        snackbar.show();
    }
    
    /**
     * Show error snackbar
     * @param view Root view
     * @param message Message to display
     */
    public static void showError(View view, String message) {
        if (view == null) return;
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(view.getContext().getResources().getColor(
            com.harmonycare.app.R.color.error, null));
        setTextColor(snackbar, android.graphics.Color.WHITE);
        snackbar.show();
    }
    
    /**
     * Show info snackbar
     * @param view Root view
     * @param message Message to display
     */
    public static void showInfo(View view, String message) {
        if (view == null) return;
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(view.getContext().getResources().getColor(
            com.harmonycare.app.R.color.info_blue, null));
        setTextColor(snackbar, android.graphics.Color.WHITE);
        snackbar.show();
    }
    
    /**
     * Show warning snackbar
     * @param view Root view
     * @param message Message to display
     */
    public static void showWarning(View view, String message) {
        if (view == null) return;
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(view.getContext().getResources().getColor(
            com.harmonycare.app.R.color.warning_orange, null));
        setTextColor(snackbar, android.graphics.Color.WHITE);
        snackbar.show();
    }
    
    /**
     * Show snackbar with action
     * @param view Root view
     * @param message Message to display
     * @param actionText Action button text
     * @param actionListener Action click listener
     */
    public static void showWithAction(View view, String message, String actionText, 
                                     View.OnClickListener actionListener) {
        if (view == null) return;
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(actionText, actionListener);
        snackbar.setActionTextColor(android.graphics.Color.WHITE);
        snackbar.show();
    }
    
    /**
     * Set text color for snackbar
     * @param snackbar Snackbar instance
     * @param color Color to set
     */
    private static void setTextColor(Snackbar snackbar, int color) {
        View snackbarView = snackbar.getView();
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setTextColor(color);
        }
    }
}

