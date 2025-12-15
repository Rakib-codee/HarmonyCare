package com.harmonycare.app.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.harmonycare.app.data.model.Emergency;

import java.util.List;

/**
 * Data Access Object for Emergency operations
 */
@Dao
public interface EmergencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertEmergency(Emergency emergency);
    
    @Query("SELECT * FROM emergencies WHERE status = :status ORDER BY timestamp DESC")
    List<Emergency> getEmergenciesByStatus(String status);
    
    @Query("SELECT * FROM emergencies WHERE status IN ('active', 'accepted') ORDER BY timestamp DESC")
    List<Emergency> getActiveAndAcceptedEmergencies();
    
    @Query("SELECT * FROM emergencies WHERE elderly_id = :elderlyId ORDER BY timestamp DESC")
    List<Emergency> getEmergenciesByElderly(int elderlyId);
    
    @Query("SELECT * FROM emergencies WHERE elderly_id = :elderlyId AND status = :status ORDER BY timestamp DESC LIMIT 1")
    Emergency getActiveEmergencyByElderly(int elderlyId, String status);
    
    @Query("SELECT * FROM emergencies WHERE volunteer_id = :volunteerId ORDER BY timestamp DESC")
    List<Emergency> getEmergenciesByVolunteer(int volunteerId);
    
    @Query("SELECT * FROM emergencies WHERE volunteer_id = :volunteerId AND volunteer_id IS NOT NULL ORDER BY timestamp DESC")
    List<Emergency> getEmergenciesByVolunteerNotNull(int volunteerId);
    
    @Query("SELECT * FROM emergencies WHERE volunteer_id = :volunteerId AND status IN ('accepted', 'completed') ORDER BY timestamp DESC")
    List<Emergency> getAcceptedAndCompletedEmergenciesByVolunteer(int volunteerId);
    
    @Query("SELECT * FROM emergencies WHERE id = :id")
    Emergency getEmergencyById(int id);
    
    @Update
    void updateEmergency(Emergency emergency);
    
    @Query("SELECT * FROM emergencies ORDER BY timestamp DESC")
    List<Emergency> getAllEmergencies();
}

