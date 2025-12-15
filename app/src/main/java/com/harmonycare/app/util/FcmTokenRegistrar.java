package com.harmonycare.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

public class FcmTokenRegistrar {
    private static final String TAG = "FcmTokenRegistrar";

    private FcmTokenRegistrar() {}

    public static void registerCurrentDevice(Context context) {
        if (context == null) return;

        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(Constants.KEY_USER_ID, -1);
        String role = prefs.getString(Constants.KEY_USER_ROLE, "");

        if (userId <= 0 || role == null || role.isEmpty()) {
            return;
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (token == null || token.isEmpty()) {
                        return;
                    }

                    DeviceApiHelper api = new DeviceApiHelper(context.getApplicationContext());
                    if (!api.isApiAvailable() || !Constants.API_ENABLED) {
                        return;
                    }

                    api.registerDevice(
                            userId,
                            role,
                            token,
                            null,
                            null,
                            null,
                            new EmergencyApiHelper.ApiCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    Log.d(TAG, "Device registered for push");
                                }

                                @Override
                                public void onError(Exception error) {
                                    Log.w(TAG, "Device register failed", error);
                                }
                            }
                    );
                })
                .addOnFailureListener(e -> Log.w(TAG, "FCM token fetch failed", e));
    }

    public static void updateVolunteerAvailability(Context context, int volunteerId, boolean isAvailable, Location location) {
        if (context == null) return;
        if (!Constants.API_ENABLED) return;

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    DeviceApiHelper api = new DeviceApiHelper(context.getApplicationContext());
                    if (!api.isApiAvailable()) {
                        return;
                    }

                    Double lat = null;
                    Double lon = null;
                    if (location != null) {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                    }

                    api.updateVolunteerAvailability(
                            volunteerId,
                            isAvailable,
                            token,
                            lat,
                            lon,
                            new EmergencyApiHelper.ApiCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    Log.d(TAG, "Volunteer availability synced");
                                }

                                @Override
                                public void onError(Exception error) {
                                    Log.w(TAG, "Volunteer availability sync failed", error);
                                }
                            }
                    );
                })
                .addOnFailureListener(e -> Log.w(TAG, "FCM token fetch failed", e));
    }
}
