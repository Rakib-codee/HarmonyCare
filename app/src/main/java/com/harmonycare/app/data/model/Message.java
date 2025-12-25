package com.harmonycare.app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Message entity for chat functionality
 */
@Entity(tableName = "messages")
public class Message {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "emergency_id")
    private int emergencyId;
    
    @ColumnInfo(name = "sender_id")
    private int senderId;

    @ColumnInfo(name = "sender_contact")
    private String senderContact;
    
    @ColumnInfo(name = "receiver_id")
    private int receiverId;

    @ColumnInfo(name = "receiver_contact")
    private String receiverContact;
    
    private String message;
    private long timestamp;
    
    public Message() {
    }
    
    public Message(int emergencyId, int senderId, int receiverId, String message) {
        this.emergencyId = emergencyId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getEmergencyId() {
        return emergencyId;
    }
    
    public void setEmergencyId(int emergencyId) {
        this.emergencyId = emergencyId;
    }
    
    public int getSenderId() {
        return senderId;
    }
    
    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getSenderContact() {
        return senderContact;
    }

    public void setSenderContact(String senderContact) {
        this.senderContact = senderContact;
    }
    
    public int getReceiverId() {
        return receiverId;
    }
    
    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverContact() {
        return receiverContact;
    }

    public void setReceiverContact(String receiverContact) {
        this.receiverContact = receiverContact;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

