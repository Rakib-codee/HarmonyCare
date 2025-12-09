package com.harmonycare.app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * VolunteerStatus entity for SQLite database
 */
@Entity(tableName = "volunteer_status")
public class VolunteerStatus {
    @PrimaryKey
    @ColumnInfo(name = "volunteer_id")
    private int volunteerId;
    
    @ColumnInfo(name = "is_available")
    private boolean isAvailable;
    
    public VolunteerStatus() {
    }
    
    public VolunteerStatus(int volunteerId, boolean isAvailable) {
        this.volunteerId = volunteerId;
        this.isAvailable = isAvailable;
    }
    
    public int getVolunteerId() {
        return volunteerId;
    }
    
    public void setVolunteerId(int volunteerId) {
        this.volunteerId = volunteerId;
    }
    
    public boolean isAvailable() {
        return isAvailable;
    }
    
    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}

