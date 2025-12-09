package com.harmonycare.app.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.harmonycare.app.R;
import com.harmonycare.app.data.model.User;
import com.harmonycare.app.data.model.VolunteerStatus;
import com.harmonycare.app.data.repository.UserRepository;
import com.harmonycare.app.data.repository.VolunteerStatusRepository;
import com.harmonycare.app.view.ChatActivity;
import com.harmonycare.app.view.ElderlyDashboardActivity;
import com.harmonycare.app.view.VolunteerEmergencyListActivity;

import java.util.List;

/**
 * Helper class for local notifications
 */
public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "harmonycare_emergency_channel";
    private static final String CHANNEL_NAME = "Emergency Notifications";
    private Context context;
    private NotificationManager notificationManager;
    
    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    
    /**
     * Check if notification permission is granted
     */
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        // For Android 12 and below, notifications don't require runtime permission
        return true;
    }
    
    /**
     * Check if notifications are enabled
     */
    private boolean areNotificationsEnabled() {
        if (notificationManager == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return notificationManager.areNotificationsEnabled();
        }
        return true;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Emergency alerts and notifications");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 250, 500});
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    public void showEmergencyNotification(String title, String message) {
        if (!hasNotificationPermission()) {
            Log.w(TAG, "Notification permission not granted");
            return;
        }
        
        if (!areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are disabled");
            return;
        }
        
        try {
            Intent intent = new Intent(context, VolunteerEmergencyListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
            Log.d(TAG, "Emergency notification shown: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Error showing emergency notification", e);
        }
    }
    
    public void showSOSNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Emergency Request Sent")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);
        
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
    
    /**
     * Notify all available volunteers about a new emergency
     * @param elderlyName Name of the elderly person who created the emergency
     * @param distance Distance in kilometers (optional, can be null)
     */
    public void notifyAvailableVolunteers(String elderlyName, String distance) {
        if (!hasNotificationPermission()) {
            Log.w(TAG, "Cannot notify volunteers: Notification permission not granted");
            return;
        }
        
        if (!areNotificationsEnabled()) {
            Log.w(TAG, "Cannot notify volunteers: Notifications are disabled");
            return;
        }
        
        Log.d(TAG, "Notifying available volunteers about emergency from: " + elderlyName);
        
        VolunteerStatusRepository volunteerStatusRepository = new VolunteerStatusRepository(context);
        UserRepository userRepository = new UserRepository(context);
        
        volunteerStatusRepository.getAvailableVolunteers(new VolunteerStatusRepository.RepositoryCallback<List<VolunteerStatus>>() {
            @Override
            public void onSuccess(List<VolunteerStatus> availableVolunteers) {
                if (availableVolunteers == null || availableVolunteers.isEmpty()) {
                    Log.d(TAG, "No available volunteers to notify");
                    return; // No available volunteers
                }
                
                Log.d(TAG, "Found " + availableVolunteers.size() + " available volunteers");
                
                // Create notification for each available volunteer
                for (VolunteerStatus volunteerStatus : availableVolunteers) {
                    int volunteerId = volunteerStatus.getVolunteerId();
                    
                    // Get volunteer user details for personalized notification
                    userRepository.getUserById(volunteerId, new UserRepository.RepositoryCallback<User>() {
                        @Override
                        public void onSuccess(User volunteer) {
                            sendNotificationToVolunteer(volunteerId, elderlyName, distance);
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            // If we can't get user details, still send notification
                            sendNotificationToVolunteer(volunteerId, elderlyName, distance);
                        }
                    });
                }
            }
            
            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error getting available volunteers", error);
            }
        });
    }
    
    /**
     * Send notification to a specific volunteer
     */
    private void sendNotificationToVolunteer(int volunteerId, String elderlyName, String distance) {
        try {
            String title = "New Emergency Request";
            String message;
            if (distance != null && !distance.isEmpty()) {
                message = String.format("%s needs help (%s away)", elderlyName, distance);
            } else {
                message = String.format("%s needs help", elderlyName);
            }
            
            // Create unique notification ID for each volunteer
            int notificationId = volunteerId * 1000 + (int) (System.currentTimeMillis() % 1000);
            
            Intent intent = new Intent(context, VolunteerEmergencyListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
            
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification sent to volunteer ID: " + volunteerId);
        } catch (Exception e) {
            Log.e(TAG, "Error sending notification to volunteer " + volunteerId, e);
        }
    }
    
    /**
     * Notify elderly user when they receive a message from volunteer
     * @param emergencyId Emergency ID
     * @param senderName Name of the volunteer who sent the message
     * @param messageText The message text
     */
    public void notifyElderlyNewMessage(int emergencyId, String senderName, String messageText) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("emergency_id", emergencyId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                emergencyId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        String title = "New Message from " + senderName;
        String preview = messageText.length() > 50 ? messageText.substring(0, 50) + "..." : messageText;
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(preview)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        
        notificationManager.notify(emergencyId + 10000, builder.build());
    }
    
    /**
     * Notify elderly when volunteer accepts their emergency
     * @param emergencyId Emergency ID
     * @param volunteerName Name of the volunteer
     */
    public void notifyElderlyEmergencyAccepted(int emergencyId, String volunteerName) {
        Intent intent = new Intent(context, ElderlyDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                emergencyId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        String title = "Emergency Accepted!";
        String message = volunteerName + " is on the way. You can now chat with them.";
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        
        notificationManager.notify(emergencyId + 20000, builder.build());
    }
    
    /**
     * Show reminder notification
     * @param reminderId Reminder ID
     * @param title Reminder title
     * @param description Reminder description
     */
    public void showReminderNotification(int reminderId, String title, String description) {
        Intent intent = new Intent(context, com.harmonycare.app.view.ElderlyDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        
        notificationManager.notify(reminderId + 30000, builder.build());
    }
}

