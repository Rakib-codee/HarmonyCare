package com.harmonycare.app.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.harmonycare.app.data.model.Reminder;

import java.util.List;

/**
 * Data Access Object for Reminder operations
 */
@Dao
public interface ReminderDao {
    @Insert
    long insertReminder(Reminder reminder);
    
    @Query("SELECT * FROM reminders WHERE user_id = :userId AND is_active = 1 ORDER BY reminder_time ASC")
    List<Reminder> getActiveRemindersByUser(int userId);
    
    @Query("SELECT * FROM reminders WHERE user_id = :userId ORDER BY reminder_time ASC")
    List<Reminder> getAllRemindersByUser(int userId);
    
    @Query("SELECT * FROM reminders WHERE id = :id")
    Reminder getReminderById(int id);
    
    @Update
    void updateReminder(Reminder reminder);
    
    @Delete
    void deleteReminder(Reminder reminder);
    
    @Query("SELECT * FROM reminders WHERE is_active = 1 AND reminder_time <= :currentTime ORDER BY reminder_time ASC")
    List<Reminder> getDueReminders(long currentTime);
}

