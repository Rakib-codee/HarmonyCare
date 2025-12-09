package com.harmonycare.app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Emergency Contact entity for SQLite database
 */
@Entity(tableName = "emergency_contacts")
public class EmergencyContact {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "user_id")
    private int userId; // Elderly user ID
    private String name;
    @ColumnInfo(name = "phone_number")
    private String phoneNumber;
    private String relationship; // e.g., "Son", "Daughter", "Friend"
    @ColumnInfo(name = "is_primary")
    private boolean isPrimary; // Primary contact gets called first
    
    @ColumnInfo(name = "notification_enabled")
    private boolean notificationEnabled = true; // Whether to notify this contact
    
    @ColumnInfo(name = "notification_method")
    private String notificationMethod = "sms"; // "sms" or "email"
    
    public EmergencyContact() {
    }
    
    public EmergencyContact(int userId, String name, String phoneNumber, String relationship) {
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
        this.isPrimary = false;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getRelationship() {
        return relationship;
    }
    
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
    
    public boolean isPrimary() {
        return isPrimary;
    }
    
    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }
    
    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }
    
    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }
    
    public String getNotificationMethod() {
        return notificationMethod;
    }
    
    public void setNotificationMethod(String notificationMethod) {
        this.notificationMethod = notificationMethod;
    }
}

