package com.harmonycare.app;

import android.app.Application;
import android.content.Context;

import com.harmonycare.app.data.database.AppDatabase;
import com.harmonycare.app.util.LocalNetworkBroadcastHelper;
import com.harmonycare.app.util.NetworkHelper;

import org.osmdroid.config.Configuration;

import java.io.File;

/**
 * Application class for HarmonyCare
 */
public class HarmonyCareApplication extends Application {
    private AppDatabase database;
    private LocalNetworkBroadcastHelper localNetworkBroadcastHelper;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize AMap privacy compliance FIRST (before any AMap SDK calls)
        initializeAMapPrivacy();
        
        database = AppDatabase.getInstance(this);
        
        // Apply language before any UI is created
        com.harmonycare.app.util.LanguageHelper.applyLanguage(this);
        
        // Apply theme before any UI is created
        com.harmonycare.app.util.ThemeHelper.applyTheme(this);
        
        // Initialize osmdroid configuration
        initializeOsmdroid();

        // Start local network listener for offline two-device Wi-Fi demo
        ensureLocalNetworkListenerStarted();
    }

    public void ensureLocalNetworkListenerStarted() {
        if (localNetworkBroadcastHelper != null && localNetworkBroadcastHelper.isListening()) {
            return;
        }
        try {
            NetworkHelper networkHelper = new NetworkHelper(this);
            if (networkHelper.isWifiConnected()) {
                localNetworkBroadcastHelper = new LocalNetworkBroadcastHelper(getApplicationContext());
                localNetworkBroadcastHelper.startListening();
            }
        } catch (Exception e) {
            android.util.Log.e("HarmonyCareApplication", "Failed to start local network listener", e);
        }
    }
    
    /**
     * Initialize AMap privacy compliance settings
     * Must be called before any AMap SDK initialization
     */
    private void initializeAMapPrivacy() {
        try {
            // Method 1: Try AMapLocationClient static methods (most common)
            try {
                Class<?> locationClientClass = Class.forName("com.amap.api.location.AMapLocationClient");
                java.lang.reflect.Method updatePrivacyShow = locationClientClass.getMethod("updatePrivacyShow", Context.class, boolean.class, boolean.class);
                updatePrivacyShow.invoke(null, this, true, true);
                
                java.lang.reflect.Method updatePrivacyAgree = locationClientClass.getMethod("updatePrivacyAgree", Context.class, boolean.class);
                updatePrivacyAgree.invoke(null, this, true);
                
                android.util.Log.d("HarmonyCareApplication", "AMap Location SDK privacy compliance initialized");
            } catch (Exception e1) {
                android.util.Log.d("HarmonyCareApplication", "AMapLocationClient method not found, trying alternatives", e1);
            }
            
            // Method 2: Try PrivacyUtils class
            try {
                Class<?> privacyUtilsClass = Class.forName("com.amap.api.location.PrivacyUtils");
                java.lang.reflect.Method updatePrivacyShow = privacyUtilsClass.getMethod("updatePrivacyShow", Context.class, boolean.class, boolean.class);
                updatePrivacyShow.invoke(null, this, true, true);
                
                java.lang.reflect.Method updatePrivacyAgree = privacyUtilsClass.getMethod("updatePrivacyAgree", Context.class, boolean.class);
                updatePrivacyAgree.invoke(null, this, true);
                
                android.util.Log.d("HarmonyCareApplication", "AMap privacy compliance initialized via PrivacyUtils");
            } catch (Exception e2) {
                android.util.Log.d("HarmonyCareApplication", "PrivacyUtils method not found, trying alternatives", e2);
            }
            
            // Method 3: Try MapsInitializer (for 3D map SDK)
            try {
                Class<?> mapsInitClass = Class.forName("com.amap.api.maps.MapsInitializer");
                java.lang.reflect.Method updatePrivacyShow = mapsInitClass.getMethod("updatePrivacyShow", Context.class, boolean.class, boolean.class);
                updatePrivacyShow.invoke(null, this, true, true);
                
                java.lang.reflect.Method updatePrivacyAgree = mapsInitClass.getMethod("updatePrivacyAgree", Context.class, boolean.class);
                updatePrivacyAgree.invoke(null, this, true);
                
                android.util.Log.d("HarmonyCareApplication", "AMap Maps SDK privacy compliance initialized");
            } catch (Exception e3) {
                android.util.Log.d("HarmonyCareApplication", "MapsInitializer method not found", e3);
            }
            
            // Method 4: Try AMapNavi (for Navigation SDK)
            try {
                Class<?> naviClass = Class.forName("com.amap.api.navi.AMapNavi");
                java.lang.reflect.Method updatePrivacyShow = naviClass.getMethod("updatePrivacyShow", Context.class, boolean.class, boolean.class);
                updatePrivacyShow.invoke(null, this, true, true);
                
                java.lang.reflect.Method updatePrivacyAgree = naviClass.getMethod("updatePrivacyAgree", Context.class, boolean.class);
                updatePrivacyAgree.invoke(null, this, true);
                
                android.util.Log.d("HarmonyCareApplication", "AMap Navigation SDK privacy compliance initialized");
            } catch (Exception e4) {
                android.util.Log.d("HarmonyCareApplication", "AMapNavi privacy method not found", e4);
            }
            
        } catch (Exception e) {
            android.util.Log.e("HarmonyCareApplication", "All AMap privacy initialization methods failed", e);
            // Even if initialization fails, app should continue (privacy might be set elsewhere)
        }
    }
    
    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Reapply language when configuration changes
        com.harmonycare.app.util.LanguageHelper.applyLanguage(this);
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

