package com.harmonycare.app.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.harmonycare.app.data.model.EmergencyContact;

import java.util.List;

/**
 * Data Access Object for EmergencyContact operations
 */
@Dao
public interface EmergencyContactDao {
    @Insert
    long insertEmergencyContact(EmergencyContact contact);
    
    @Query("SELECT * FROM emergency_contacts WHERE user_id = :userId ORDER BY is_primary DESC, name ASC")
    List<EmergencyContact> getContactsByUser(int userId);
    
    @Query("SELECT * FROM emergency_contacts WHERE user_id = :userId AND is_primary = 1 LIMIT 1")
    EmergencyContact getPrimaryContact(int userId);
    
    @Query("SELECT * FROM emergency_contacts WHERE id = :id")
    EmergencyContact getContactById(int id);
    
    @Update
    void updateEmergencyContact(EmergencyContact contact);
    
    @Delete
    void deleteEmergencyContact(EmergencyContact contact);
    
    @Query("UPDATE emergency_contacts SET is_primary = 0 WHERE user_id = :userId")
    void clearPrimaryContacts(int userId);
}

