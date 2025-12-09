package com.harmonycare.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

/**
 * Helper class for managing app language
 */
public class LanguageHelper {
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_BENGALI = "bn";
    
    /**
     * Apply saved language preference
     * @param context Context
     */
    public static void applyLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        String language = prefs.getString("app_language", LANGUAGE_ENGLISH);
        setLocale(context, language);
    }
    
    /**
     * Set app locale
     * @param context Context
     * @param languageCode Language code (e.g., "en", "bn")
     */
    public static void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Configuration config = new Configuration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            Resources resources = context.getResources();
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }
    
    /**
     * Save language preference
     * @param context Context
     * @param languageCode Language code to save
     */
    public static void saveLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("app_language", languageCode).apply();
        setLocale(context, languageCode);
    }
    
    /**
     * Get current language
     * @param context Context
     * @return Current language code
     */
    public static String getCurrentLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("app_language", LANGUAGE_ENGLISH);
    }
}

