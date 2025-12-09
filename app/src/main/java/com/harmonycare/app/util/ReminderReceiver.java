package com.harmonycare.app.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver for handling reminder alarms
 */
public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderId = intent.getIntExtra("reminder_id", -1);
        String title = intent.getStringExtra("reminder_title");
        String description = intent.getStringExtra("reminder_description");
        
        if (reminderId > 0 && title != null) {
            // Show notification
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showReminderNotification(reminderId, title, description);
        }
    }
}

