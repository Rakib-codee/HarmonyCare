package com.harmonycare.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

public class FcmTokenRegistrar {
    private static final String TAG = "FcmTokenRegistrar";

    private FcmTokenRegistrar() {}

    public static void registerCurrentDevice(Context context) {
        if (context == null) return;
        if (!Constants.API_ENABLED) return;

        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(Constants.KEY_USER_ID, -1);
        String role = prefs.getString(Constants.KEY_USER_ROLE, "");

        if (userId <= 0 || role == null || role.isEmpty()) {
            return;
        }

        // JPush removed for offline/local-only demo. No external push token registration.
        Log.d(TAG, "Push token registration disabled (JPush removed)");
    }

    public static void updateVolunteerAvailability(Context context, int volunteerId, boolean isAvailable, Location location) {
        if (context == null) return;
        if (!Constants.API_ENABLED) return;

        // JPush removed for offline/local-only demo. Skip external availability updates.
        Log.d(TAG, "Volunteer availability update skipped (JPush removed)");
    }
}
