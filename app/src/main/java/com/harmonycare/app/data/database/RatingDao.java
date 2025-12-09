package com.harmonycare.app.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.harmonycare.app.data.model.Rating;

import java.util.List;

/**
 * Data Access Object for Rating operations
 */
@Dao
public interface RatingDao {
    @Insert
    long insertRating(Rating rating);
    
    @Query("SELECT * FROM ratings WHERE emergency_id = :emergencyId LIMIT 1")
    Rating getRatingByEmergency(int emergencyId);
    
    @Query("SELECT * FROM ratings WHERE volunteer_id = :volunteerId")
    List<Rating> getRatingsByVolunteer(int volunteerId);
    
    @Query("SELECT AVG(rating) FROM ratings WHERE volunteer_id = :volunteerId")
    Double getAverageRatingByVolunteer(int volunteerId);
    
    @Query("SELECT COUNT(*) FROM ratings WHERE volunteer_id = :volunteerId")
    int getRatingCountByVolunteer(int volunteerId);
}

