package com.harmonycare.app;

import android.app.Application;
import android.content.Context;

import com.harmonycare.app.data.database.AppDatabase;

import org.osmdroid.config.Configuration;

import java.io.File;

/**
 * Application class for HarmonyCare
 */
public class HarmonyCareApplication extends Application {
    private AppDatabase database;
    
    @Override
    public void onCreate() {
        super.onCreate();
        database = AppDatabase.getInstance(this);
        
        // Initialize osmdroid configuration
        initializeOsmdroid();
    }
    
    private void initializeOsmdroid() {
        // Set user agent (required by OSM tile servers)
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue("HarmonyCare/1.0");
        
        // Set cache directory for map tiles
        File cacheDir = new File(getCacheDir(), "osmdroid");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        Configuration.getInstance().setOsmdroidBasePath(cacheDir);
        Configuration.getInstance().setOsmdroidTileCache(cacheDir);
    }
    
    public AppDatabase getDatabase() {
        return database;
    }
}

