package com.harmonycare.app.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.model.EmergencyContact;
import com.harmonycare.app.data.repository.EmergencyContactRepository;
import com.harmonycare.app.data.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for notifying family/emergency contacts
 */
public class FamilyNotificationHelper {
    private static final String TAG = "FamilyNotificationHelper";
    private Context context;
    private EmergencyContactRepository contactRepository;
    private UserRepository userRepository;
    
    public FamilyNotificationHelper(Context context) {
        this.context = context;
        this.contactRepository = new EmergencyContactRepository(context);
        this.userRepository = new UserRepository(context);
    }
    
    /**
     * Notify emergency contacts when an emergency is created
     */
    public void notifyEmergencyContacts(int elderlyId, Emergency emergency) {
        contactRepository.getContactsByUser(elderlyId, new EmergencyContactRepository.RepositoryCallback<List<EmergencyContact>>() {
            @Override
            public void onSuccess(List<EmergencyContact> contacts) {
                if (contacts == null || contacts.isEmpty()) {
                    return;
                }
                
                // Get elderly user name
                userRepository.getUserById(elderlyId, new UserRepository.RepositoryCallback<com.harmonycare.app.data.model.User>() {
                    @Override
                    public void onSuccess(com.harmonycare.app.data.model.User elderly) {
                        String elderlyName = elderly != null ? elderly.getName() : "An elderly person";
                        
                        // Notify primary contact first
                        for (EmergencyContact contact : contacts) {
                            if (contact.isPrimary() && contact.isNotificationEnabled()) {
                                sendNotification(contact, elderlyName, emergency);
                                break;
                            }
                        }
                        
                        // Notify other enabled contacts
                        for (EmergencyContact contact : contacts) {
                            if (!contact.isPrimary() && contact.isNotificationEnabled()) {
                                sendNotification(contact, elderlyName, emergency);
                            }
                        }
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        // Use generic name
                        String elderlyName = "An elderly person";
                        for (EmergencyContact contact : contacts) {
                            if (contact.isNotificationEnabled()) {
                                sendNotification(contact, elderlyName, emergency);
                            }
                        }
                    }
                });
            }
            
            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading emergency contacts", error);
            }
        });
    }
    
    private void sendNotification(EmergencyContact contact, String elderlyName, Emergency emergency) {
        String method = contact.getNotificationMethod();
        if (method == null) {
            method = "sms"; // Default to SMS
        }
        
        String message = buildNotificationMessage(elderlyName, emergency);
        
        if ("sms".equalsIgnoreCase(method)) {
            sendSMS(contact.getPhoneNumber(), message);
        } else if ("email".equalsIgnoreCase(method)) {
            sendEmail(contact.getPhoneNumber(), message); // Using phone number as email for now
        } else if ("call".equalsIgnoreCase(method)) {
            // Could initiate a call, but SMS is more reliable
            sendSMS(contact.getPhoneNumber(), message);
        }
    }
    
    private String buildNotificationMessage(String elderlyName, Emergency emergency) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String time = sdf.format(new Date(emergency.getTimestamp()));
        
        return String.format(Locale.getDefault(),
                "EMERGENCY ALERT: %s has sent an emergency request at %s. " +
                "Location: %.6f, %.6f. Please check HarmonyCare app for details.",
                elderlyName, time, emergency.getLatitude(), emergency.getLongitude());
    }
    
    private void sendSMS(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.w(TAG, "Phone number is empty, cannot send SMS");
            return;
        }
        
        // Check if SEND_SMS permission is granted (required for Android 6.0+)
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) 
                == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, send SMS directly
            try {
                SmsManager smsManager = SmsManager.getDefault();
                if (smsManager != null) {
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                    Log.d(TAG, "SMS sent to " + phoneNumber);
                    return;
                }
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException: SEND_SMS permission not granted", e);
                // Fall through to intent method
            } catch (Exception e) {
                Log.e(TAG, "Error sending SMS", e);
                // Fall through to intent method
            }
        } else {
            Log.d(TAG, "SEND_SMS permission not granted, using intent method");
        }
        
        // Fallback: Use intent to open SMS app (doesn't require SEND_SMS permission)
        // This opens the default SMS app with the message pre-filled
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:" + phoneNumber));
            intent.putExtra("sms_body", message);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                Log.d(TAG, "Opened SMS app for " + phoneNumber);
            } else {
                Log.e(TAG, "No SMS app available");
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error opening SMS app", ex);
        }
    }
    
    private void sendEmail(String email, String message) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Emergency Alert - HarmonyCare");
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (Exception e) {
            Log.e(TAG, "Error sending email", e);
        }
    }
}

