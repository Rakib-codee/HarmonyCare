package com.harmonycare.app.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import com.harmonycare.app.data.model.PendingOperation;

import java.util.List;

/**
 * Data Access Object for PendingOperation
 */
@Dao
public interface PendingOperationDao {
    @Insert
    long insertPendingOperation(PendingOperation operation);
    
    @Query("SELECT * FROM pending_operations ORDER BY timestamp ASC")
    List<PendingOperation> getAllPendingOperations();
    
    @Query("SELECT * FROM pending_operations WHERE operation_type = :operationType ORDER BY timestamp ASC")
    List<PendingOperation> getPendingOperationsByType(String operationType);
    
    @Delete
    void deletePendingOperation(PendingOperation operation);
    
    @Query("DELETE FROM pending_operations")
    void clearAllPendingOperations();
}

