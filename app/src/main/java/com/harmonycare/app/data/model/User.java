package com.harmonycare.app.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * User entity for SQLite database
 */
@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String name;
    private String contact;
    private String role; // "elderly" or "volunteer"
    private String password;
    
    @ColumnInfo(name = "photo_path")
    private String photoPath;
    
    @ColumnInfo(name = "address")
    private String address;
    
    @ColumnInfo(name = "medical_info")
    private String medicalInfo;
    
    public User() {
    }
    
    public User(String name, String contact, String role, String password) {
        this.name = name;
        this.contact = contact;
        this.role = role;
        this.password = password;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPhotoPath() {
        return photoPath;
    }
    
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getMedicalInfo() {
        return medicalInfo;
    }
    
    public void setMedicalInfo(String medicalInfo) {
        this.medicalInfo = medicalInfo;
    }
}

