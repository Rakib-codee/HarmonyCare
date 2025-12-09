package com.harmonycare.app.util;

/**
 * Constants class for the application
 */
public class Constants {
    // SharedPreferences keys
    public static final String PREFS_NAME = "HarmonyCarePrefs";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_LARGE_TEXT = "large_text_mode";
    public static final String KEY_DARK_MODE = "dark_mode";
    public static final String KEY_THEME_MODE = "theme_mode"; // "light", "dark", "system"
    
    // Emergency statuses
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_ACCEPTED = "accepted";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";
    
    // User roles
    public static final String ROLE_ELDERLY = "elderly";
    public static final String ROLE_VOLUNTEER = "volunteer";
    
    // Permission request codes
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 1003;
    
    // Database
    public static final String DATABASE_NAME = "harmonycare_database";
    public static final int DATABASE_VERSION = 1;
    
    // Location update intervals (milliseconds)
    public static final long LOCATION_UPDATE_INTERVAL = 30000; // 30 seconds
    public static final float LOCATION_UPDATE_DISTANCE = 10.0f; // 10 meters
    
    // Notification
    public static final String NOTIFICATION_CHANNEL_ID = "harmonycare_emergency_channel";
    public static final String NOTIFICATION_CHANNEL_NAME = "Emergency Notifications";
    
    // Intent extras
    public static final String EXTRA_EMERGENCY_ID = "emergency_id";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_ELDERLY_ID = "elderly_id";
    
    // SOS button
    public static final int SOS_COUNTDOWN_SECONDS = 3;
    public static final long SOS_CONFIRMATION_DELAY = 2000; // 2 seconds
    
    // Validation
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MIN_NAME_LENGTH = 2;
}

