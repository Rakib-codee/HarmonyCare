package com.harmonycare.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Helper class for managing app theme (Light/Dark mode)
 */
public class ThemeHelper {
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";
    
    /**
     * Apply theme based on preference
     * @param context Context
     */
    public static void applyTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        String themeMode = prefs.getString(Constants.KEY_THEME_MODE, THEME_SYSTEM);
        applyTheme(themeMode);
    }
    
    /**
     * Apply specific theme mode
     * @param themeMode Theme mode: "light", "dark", or "system"
     */
    public static void applyTheme(String themeMode) {
        switch (themeMode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
    
    /**
     * Save theme preference
     * @param context Context
     * @param themeMode Theme mode to save
     */
    public static void saveThemeMode(Context context, String themeMode) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.KEY_THEME_MODE, themeMode).apply();
        applyTheme(themeMode);
    }
    
    /**
     * Get current theme mode
     * @param context Context
     * @return Current theme mode
     */
    public static String getThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(Constants.KEY_THEME_MODE, THEME_SYSTEM);
    }
    
    /**
     * Check if dark mode is currently active
     * @param context Context
     * @return true if dark mode is active
     */
    public static boolean isDarkMode(Context context) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
}

