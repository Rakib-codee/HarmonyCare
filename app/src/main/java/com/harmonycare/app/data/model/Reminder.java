package com.harmonycare.app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Reminder entity for scheduled reminders
 */
@Entity(tableName = "reminders")
public class Reminder {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "user_id")
    private int userId;
    
    private String title;
    private String description;
    
    @ColumnInfo(name = "reminder_time")
    private long reminderTime; // Timestamp for when reminder should fire
    
    @ColumnInfo(name = "repeat_type")
    private String repeatType; // "none", "daily", "weekly"
    
    @ColumnInfo(name = "is_active")
    private boolean isActive = true;
    
    public Reminder() {
    }
    
    public Reminder(int userId, String title, String description, long reminderTime, String repeatType) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.reminderTime = reminderTime;
        this.repeatType = repeatType;
        this.isActive = true;
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
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public long getReminderTime() {
        return reminderTime;
    }
    
    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }
    
    public String getRepeatType() {
        return repeatType;
    }
    
    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
}

