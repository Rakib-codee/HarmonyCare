package com.harmonycare.app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.harmonycare.app.R;
import com.harmonycare.app.util.FallDetectionHelper;
import com.harmonycare.app.util.NotificationHelper;
import com.harmonycare.app.view.ElderlyDashboardActivity;

/**
 * Background service for fall detection monitoring
 */
public class FallDetectionService extends Service {
    private static final String TAG = "FallDetectionService";
    private static final String CHANNEL_ID = "fall_detection_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private FallDetectionHelper fallDetectionHelper;
    private SensorManager sensorManager;
    private boolean isMonitoring = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            if (sensorManager == null) {
                Log.e(TAG, "SensorManager not available");
                stopSelf();
                return;
            }
            
            fallDetectionHelper = new FallDetectionHelper(sensorManager);
            
            fallDetectionHelper.setListener(() -> {
                // Fall detected - trigger emergency
                Log.d(TAG, "Fall detected in service");
                handleFallDetected();
            });
            
            createNotificationChannel();
            
            // Check for required permissions before starting foreground service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(this, 
                        android.Manifest.permission.ACTIVITY_RECOGNITION) 
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "ACTIVITY_RECOGNITION permission not granted");
                    stopSelf();
                    return;
                }
            }
            
            startForeground(NOTIFICATION_ID, createNotification());
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException starting foreground service", e);
            stopSelf();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            stopSelf();
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("START_MONITORING".equals(action)) {
                startMonitoring();
            } else if ("STOP_MONITORING".equals(action)) {
                stopMonitoring();
                stopSelf();
            }
        }
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMonitoring();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void startMonitoring() {
        if (!isMonitoring && fallDetectionHelper.isAvailable()) {
            fallDetectionHelper.startMonitoring();
            isMonitoring = true;
            Log.d(TAG, "Fall detection started");
        }
    }
    
    private void stopMonitoring() {
        if (isMonitoring) {
            fallDetectionHelper.stopMonitoring();
            isMonitoring = false;
            Log.d(TAG, "Fall detection stopped");
        }
    }
    
    private void handleFallDetected() {
        // Show notification and trigger emergency
        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.showEmergencyNotification(
                "Fall Detected!",
                "A fall has been detected. Emergency will be triggered automatically."
        );
        
        // Broadcast to activity to trigger emergency
        Intent broadcastIntent = new Intent("com.harmonycare.app.FALL_DETECTED");
        sendBroadcast(broadcastIntent);
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Fall Detection",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Fall detection monitoring service");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, ElderlyDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Fall Detection Active")
                .setContentText("Monitoring for falls")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }
}

