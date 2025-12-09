package com.harmonycare.app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Rating entity for volunteer feedback
 */
@Entity(tableName = "ratings")
public class Rating {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "emergency_id")
    private int emergencyId;
    
    @ColumnInfo(name = "volunteer_id")
    private int volunteerId;
    
    @ColumnInfo(name = "elderly_id")
    private int elderlyId;
    
    private int rating; // 1-5 stars
    
    private String feedback; // Optional feedback text
    
    private long timestamp;
    
    public Rating() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Rating(int emergencyId, int volunteerId, int elderlyId, int rating, String feedback) {
        this.emergencyId = emergencyId;
        this.volunteerId = volunteerId;
        this.elderlyId = elderlyId;
        this.rating = rating;
        this.feedback = feedback;
        this.timestamp = System.currentTimeMillis();
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getEmergencyId() {
        return emergencyId;
    }
    
    public void setEmergencyId(int emergencyId) {
        this.emergencyId = emergencyId;
    }
    
    public int getVolunteerId() {
        return volunteerId;
    }
    
    public void setVolunteerId(int volunteerId) {
        this.volunteerId = volunteerId;
    }
    
    public int getElderlyId() {
        return elderlyId;
    }
    
    public void setElderlyId(int elderlyId) {
        this.elderlyId = elderlyId;
    }
    
    public int getRating() {
        return rating;
    }
    
    public void setRating(int rating) {
        this.rating = rating;
    }
    
    public String getFeedback() {
        return feedback;
    }
    
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

