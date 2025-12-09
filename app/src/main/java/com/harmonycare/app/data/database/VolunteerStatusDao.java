package com.harmonycare.app.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;

import com.harmonycare.app.data.model.VolunteerStatus;

import java.util.List;

/**
 * Data Access Object for VolunteerStatus operations
 */
@Dao
public interface VolunteerStatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateVolunteerStatus(VolunteerStatus status);
    
    @Query("SELECT * FROM volunteer_status WHERE volunteer_id = :volunteerId")
    VolunteerStatus getVolunteerStatus(int volunteerId);
    
    @Update
    void updateVolunteerStatus(VolunteerStatus status);
    
    @Query("SELECT * FROM volunteer_status WHERE is_available = 1")
    List<VolunteerStatus> getAvailableVolunteers();
}

