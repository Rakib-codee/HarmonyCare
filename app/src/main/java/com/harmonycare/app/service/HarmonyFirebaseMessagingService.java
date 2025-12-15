package com.harmonycare.app.service;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.harmonycare.app.util.NotificationHelper;

import java.util.Map;

public class HarmonyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "HarmonyFCM";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        try {
            Map<String, String> data = message.getData();
            if (data == null || data.isEmpty()) {
                return;
            }

            String type = data.get("type");
            NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());

            if ("emergency_new".equals(type)) {
                String emergencyId = data.get("emergency_id");
                notificationHelper.showEmergencyNotification(
                        "New Emergency Request",
                        "Tap to view emergency" + (emergencyId != null ? (" (#" + emergencyId + ")") : "")
                );
            } else if ("emergency_accepted".equals(type)) {
                notificationHelper.showEmergencyNotification(
                        "Emergency Accepted",
                        "A volunteer accepted your emergency. Open app to chat."
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling FCM message", e);
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token received");
        // Token is registered when user logs in (see FcmTokenRegistrar)
    }
}
