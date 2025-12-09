package com.harmonycare.app.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.harmonycare.app.data.model.User;

import java.util.List;

/**
 * Data Access Object for User operations
 */
@Dao
public interface UserDao {
    @Insert
    long insertUser(User user);
    
    // Login is now handled in repository with password verification
    // This method is kept for backward compatibility but not used
    @Deprecated
    @Query("SELECT * FROM users WHERE contact = :contact AND password = :password")
    User login(String contact, String password);
    
    @Query("SELECT * FROM users WHERE id = :id")
    User getUserById(int id);
    
    @Query("SELECT * FROM users WHERE contact = :contact")
    User getUserByContact(String contact);
    
    @Query("SELECT * FROM users WHERE role = :role")
    List<User> getUsersByRole(String role);
    
    @Update
    void updateUser(User user);
    
    @Query("SELECT * FROM users")
    List<User> getAllUsers();
}

