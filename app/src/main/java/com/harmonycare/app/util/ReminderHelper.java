package com.harmonycare.app.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.harmonycare.app.data.model.Reminder;

import java.util.Calendar;
import java.util.List;

/**
 * Helper class for scheduling reminders using AlarmManager
 */
public class ReminderHelper {
    private static final String TAG = "ReminderHelper";
    private Context context;
    private AlarmManager alarmManager;
    
    public ReminderHelper(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
    
    /**
     * Schedule a reminder
     */
    public void scheduleReminder(Reminder reminder) {
        if (!reminder.isActive()) {
            cancelReminder(reminder);
            return;
        }
        
        long reminderTime = reminder.getReminderTime();
        long currentTime = System.currentTimeMillis();
        
        // If reminder time is in the past and not repeating, don't schedule
        if (reminderTime < currentTime && "none".equals(reminder.getRepeatType())) {
            return;
        }
        
        // Adjust time for repeating reminders
        if ("daily".equals(reminder.getRepeatType()) && reminderTime < currentTime) {
            // Add 24 hours
            reminderTime += 24 * 60 * 60 * 1000;
        } else if ("weekly".equals(reminder.getRepeatType()) && reminderTime < currentTime) {
            // Add 7 days
            reminderTime += 7 * 24 * 60 * 60 * 1000;
        }
        
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("reminder_id", reminder.getId());
        intent.putExtra("reminder_title", reminder.getTitle());
        intent.putExtra("reminder_description", reminder.getDescription());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent);
            Log.d(TAG, "Reminder scheduled: " + reminder.getTitle() + " at " + reminderTime);
        }
    }
    
    /**
     * Cancel a reminder
     */
    public void cancelReminder(Reminder reminder) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Reminder cancelled: " + reminder.getTitle());
        }
    }
    
    /**
     * Schedule all active reminders for a user
     */
    public void scheduleAllReminders(List<Reminder> reminders) {
        if (reminders == null) {
            return;
        }
        
        for (Reminder reminder : reminders) {
            if (reminder.isActive()) {
                scheduleReminder(reminder);
            }
        }
    }
}

