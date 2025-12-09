package com.harmonycare.app.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.model.EmergencyContact;
import com.harmonycare.app.data.model.Message;
import com.harmonycare.app.data.model.PendingOperation;
import com.harmonycare.app.data.model.Rating;
import com.harmonycare.app.data.model.Reminder;
import com.harmonycare.app.data.model.User;
import com.harmonycare.app.data.model.VolunteerStatus;

/**
 * Room Database for HarmonyCare app
 */
@Database(entities = {User.class, Emergency.class, VolunteerStatus.class, EmergencyContact.class, Message.class, Rating.class, Reminder.class, PendingOperation.class}, version = 9, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    
    public abstract UserDao userDao();
    public abstract EmergencyDao emergencyDao();
    public abstract VolunteerStatusDao volunteerStatusDao();
    public abstract EmergencyContactDao emergencyContactDao();
    public abstract MessageDao messageDao();
    public abstract RatingDao ratingDao();
    public abstract ReminderDao reminderDao();
    public abstract PendingOperationDao pendingOperationDao();
    
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "harmonycare_database"
            )
            .fallbackToDestructiveMigration()
            // Removed allowMainThreadQueries - use background threads
            .build();
        }
        return instance;
    }
}

