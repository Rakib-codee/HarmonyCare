package com.harmonycare.app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Emergency entity for SQLite database
 */
@Entity(tableName = "emergencies")
public class Emergency {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "elderly_id")
    private int elderlyId;
    private double latitude;
    private double longitude;
    private String status; // "active", "accepted", "completed"
    @ColumnInfo(name = "volunteer_id")
    private Integer volunteerId; // null if not accepted
    private long timestamp;
    
    public Emergency() {
    }
    
    public Emergency(int elderlyId, double latitude, double longitude, String status) {
        this.elderlyId = elderlyId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
        this.volunteerId = null;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getElderlyId() {
        return elderlyId;
    }
    
    public void setElderlyId(int elderlyId) {
        this.elderlyId = elderlyId;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getVolunteerId() {
        return volunteerId;
    }
    
    public void setVolunteerId(Integer volunteerId) {
        this.volunteerId = volunteerId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

