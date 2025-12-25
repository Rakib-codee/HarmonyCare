package com.harmonycare.app.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.harmonycare.app.view.SplashActivity;
import com.harmonycare.app.util.NotificationHelper;

import org.json.JSONObject;

import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class HarmonyFirebaseMessagingService extends JPushMessageReceiver {
    private static final String TAG = "HarmonyJPush";

    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage message) {
        super.onNotifyMessageArrived(context, message);
        try {
            if (context == null || message == null) return;

            String type = null;
            String emergencyId = null;

            if (message.notificationExtras != null && !message.notificationExtras.isEmpty()) {
                JSONObject extras = new JSONObject(message.notificationExtras);
                type = extras.optString("type", null);
                emergencyId = extras.optString("emergency_id", null);
            }

            NotificationHelper notificationHelper = new NotificationHelper(context.getApplicationContext());

            if ("emergency_new".equals(type)) {
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
            Log.e(TAG, "Error handling JPush notification", e);
        }
    }

    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage message) {
        super.onNotifyMessageOpened(context, message);
        try {
            if (context == null) return;
            Intent i = new Intent(context.getApplicationContext(), SplashActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.getApplicationContext().startActivity(i);
        } catch (Exception e) {
            Log.e(TAG, "Error opening from JPush notification", e);
        }
    }

    @Override
    public void onMessage(Context context, CustomMessage customMessage) {
        super.onMessage(context, customMessage);
    }
}
