package com.harmonycare.app.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.harmonycare.app.data.model.Message;

import java.util.List;

/**
 * DAO for Message entity
 */
@Dao
public interface MessageDao {
    @Insert
    long insertMessage(Message message);
    
    @Query("SELECT * FROM messages WHERE emergency_id = :emergencyId ORDER BY timestamp ASC")
    List<Message> getMessagesByEmergency(int emergencyId);
    
    @Query("SELECT * FROM messages WHERE emergency_id = :emergencyId AND (sender_id = :userId OR receiver_id = :userId) ORDER BY timestamp ASC")
    List<Message> getMessagesByEmergencyAndUser(int emergencyId, int userId);
    
    @Query("SELECT * FROM messages WHERE emergency_id = :emergencyId ORDER BY timestamp DESC LIMIT 1")
    Message getLatestMessage(int emergencyId);
}

