package com.harmonycare.app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * PendingOperation entity for queuing operations when offline
 */
@Entity(tableName = "pending_operations")
public class PendingOperation {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "operation_type")
    private String operationType; // "create_emergency", "update_emergency", etc.
    
    @ColumnInfo(name = "data_json")
    private String dataJson; // JSON string containing operation data
    
    private long timestamp;
    
    @ColumnInfo(name = "retry_count")
    private int retryCount = 0;
    
    public PendingOperation() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public PendingOperation(String operationType, String dataJson) {
        this.operationType = operationType;
        this.dataJson = dataJson;
        this.timestamp = System.currentTimeMillis();
        this.retryCount = 0;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
    
    public String getDataJson() {
        return dataJson;
    }
    
    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}

