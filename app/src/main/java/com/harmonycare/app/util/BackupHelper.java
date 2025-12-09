package com.harmonycare.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.harmonycare.app.data.database.AppDatabase;
import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.model.EmergencyContact;
import com.harmonycare.app.data.model.User;
import com.harmonycare.app.data.repository.EmergencyRepository;
import com.harmonycare.app.data.repository.EmergencyContactRepository;
import com.harmonycare.app.data.repository.UserRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for backup and restore functionality
 */
public class BackupHelper {
    private static final String TAG = "BackupHelper";
    private Context context;
    private UserRepository userRepository;
    private EmergencyRepository emergencyRepository;
    private EmergencyContactRepository emergencyContactRepository;
    
    public BackupHelper(Context context) {
        this.context = context;
        this.userRepository = new UserRepository(context);
        this.emergencyRepository = new EmergencyRepository(context);
        this.emergencyContactRepository = new EmergencyContactRepository(context);
    }
    
    /**
     * Export user data to JSON file
     * @param userId User ID to export
     * @param callback Callback for result
     */
    public void exportData(int userId, BackupCallback callback) {
        try {
            JSONObject backupData = new JSONObject();
            backupData.put("export_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            backupData.put("app_version", "1.0");
            
            // Export user data
            userRepository.getUserById(userId, new UserRepository.RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    try {
                        JSONObject userJson = new JSONObject();
                        userJson.put("id", user.getId());
                        userJson.put("name", user.getName());
                        userJson.put("contact", user.getContact());
                        userJson.put("role", user.getRole());
                        // Don't export password for security
                        backupData.put("user", userJson);
                        
                        // Export emergencies
                        exportEmergencies(userId, user.getRole(), backupData, callback);
                    } catch (Exception e) {
                        if (callback != null) {
                            callback.onError("Error exporting user data: " + e.getMessage());
                        }
                    }
                }
                
                @Override
                public void onError(Exception error) {
                    if (callback != null) {
                        callback.onError("Error getting user data: " + error.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            if (callback != null) {
                callback.onError("Error creating backup: " + e.getMessage());
            }
        }
    }
    
    /**
     * Export emergencies based on user role
     */
    private void exportEmergencies(int userId, String role, JSONObject backupData, BackupCallback callback) {
        if ("elderly".equals(role)) {
            emergencyRepository.getEmergenciesByElderly(userId, new EmergencyRepository.RepositoryCallback<List<Emergency>>() {
                @Override
                public void onSuccess(List<Emergency> emergencies) {
                    exportEmergencyContacts(userId, emergencies, backupData, callback);
                }
                
                @Override
                public void onError(Exception error) {
                    exportEmergencyContacts(userId, new java.util.ArrayList<>(), backupData, callback);
                }
            });
        } else {
            emergencyRepository.getEmergenciesByVolunteer(userId, new EmergencyRepository.RepositoryCallback<List<Emergency>>() {
                @Override
                public void onSuccess(List<Emergency> emergencies) {
                    exportEmergencyContacts(userId, emergencies, backupData, callback);
                }
                
                @Override
                public void onError(Exception error) {
                    exportEmergencyContacts(userId, new java.util.ArrayList<>(), backupData, callback);
                }
            });
        }
    }
    
    /**
     * Export emergency contacts
     */
    private void exportEmergencyContacts(int userId, List<Emergency> emergencies, JSONObject backupData, BackupCallback callback) {
        try {
            // Export emergencies
            JSONArray emergenciesJson = new JSONArray();
            for (Emergency emergency : emergencies) {
                JSONObject emergencyJson = new JSONObject();
                emergencyJson.put("id", emergency.getId());
                emergencyJson.put("elderly_id", emergency.getElderlyId());
                emergencyJson.put("volunteer_id", emergency.getVolunteerId());
                emergencyJson.put("latitude", emergency.getLatitude());
                emergencyJson.put("longitude", emergency.getLongitude());
                emergencyJson.put("status", emergency.getStatus());
                emergencyJson.put("timestamp", emergency.getTimestamp());
                emergenciesJson.put(emergencyJson);
            }
            backupData.put("emergencies", emergenciesJson);
            
            // Export emergency contacts
            emergencyContactRepository.getContactsByUser(userId, 
                new EmergencyContactRepository.RepositoryCallback<List<EmergencyContact>>() {
                    @Override
                    public void onSuccess(List<EmergencyContact> contacts) {
                        try {
                            JSONArray contactsJson = new JSONArray();
                            for (EmergencyContact contact : contacts) {
                                JSONObject contactJson = new JSONObject();
                                contactJson.put("id", contact.getId());
                                contactJson.put("elderly_id", contact.getUserId());
                                contactJson.put("name", contact.getName());
                                contactJson.put("phone", contact.getPhoneNumber());
                                contactJson.put("relationship", contact.getRelationship());
                                contactsJson.put(contactJson);
                            }
                            backupData.put("emergency_contacts", contactsJson);
                            
                            // Save to file
                            saveBackupToFile(backupData, callback);
                        } catch (Exception e) {
                            if (callback != null) {
                                callback.onError("Error exporting contacts: " + e.getMessage());
                            }
                        }
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        try {
                            backupData.put("emergency_contacts", new JSONArray());
                            saveBackupToFile(backupData, callback);
                        } catch (Exception e) {
                            if (callback != null) {
                                callback.onError("Error creating backup: " + e.getMessage());
                            }
                        }
                    }
                });
        } catch (Exception e) {
            if (callback != null) {
                callback.onError("Error exporting emergencies: " + e.getMessage());
            }
        }
    }
    
    /**
     * Save backup data to file
     */
    private void saveBackupToFile(JSONObject backupData, BackupCallback callback) {
        try {
            // Create backup directory if it doesn't exist
            File backupDir = new File(context.getExternalFilesDir(null), "backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            // Create backup file with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File backupFile = new File(backupDir, "harmonycare_backup_" + timestamp + ".json");
            
            // Write JSON to file
            FileWriter writer = new FileWriter(backupFile);
            writer.write(backupData.toString(2)); // Pretty print with 2-space indent
            writer.close();
            
            if (callback != null) {
                callback.onSuccess(backupFile.getAbsolutePath());
            }
            Log.d(TAG, "Backup saved to: " + backupFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error saving backup file", e);
            if (callback != null) {
                callback.onError("Error saving backup file: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating backup file", e);
            if (callback != null) {
                callback.onError("Error creating backup: " + e.getMessage());
            }
        }
    }
    
    /**
     * Import data from JSON file
     * @param filePath Path to backup file
     * @param callback Callback for result
     */
    public void importData(String filePath, BackupCallback callback) {
        // Note: Import functionality would require reading JSON and restoring data
        // This is a basic implementation - full import would need more work
        if (callback != null) {
            callback.onError("Import functionality not yet implemented");
        }
    }
    
    /**
     * Callback interface for backup operations
     */
    public interface BackupCallback {
        void onSuccess(String filePath);
        void onError(String error);
    }
}

