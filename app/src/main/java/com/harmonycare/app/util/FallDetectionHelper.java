package com.harmonycare.app.util;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Helper class for fall detection using accelerometer and gyroscope
 */
public class FallDetectionHelper implements SensorEventListener {
    private static final String TAG = "FallDetectionHelper";
    private static final float FALL_THRESHOLD = 15.0f; // m/s^2 - threshold for fall detection
    private static final long FALL_WINDOW_MS = 500; // Time window for fall detection
    
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private FallDetectionListener listener;
    private boolean isMonitoring = false;
    
    private float[] lastAcceleration = new float[3];
    private long lastFallTime = 0;
    
    public interface FallDetectionListener {
        void onFallDetected();
    }
    
    public FallDetectionHelper(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }
    
    public void setListener(FallDetectionListener listener) {
        this.listener = listener;
    }
    
    public void startMonitoring() {
        if (isMonitoring) {
            return;
        }
        
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            isMonitoring = true;
            Log.d(TAG, "Fall detection monitoring started");
        }
        
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI);
        }
    }
    
    public void stopMonitoring() {
        if (!isMonitoring) {
            return;
        }
        
        sensorManager.unregisterListener(this);
        isMonitoring = false;
        Log.d(TAG, "Fall detection monitoring stopped");
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            
            // Calculate total acceleration magnitude
            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
            
            // Check for sudden change (fall detection)
            if (lastAcceleration[0] != 0) {
                float deltaAcceleration = Math.abs(acceleration - lastAcceleration[0]);
                
                // Detect fall: sudden large change in acceleration
                if (deltaAcceleration > FALL_THRESHOLD) {
                    long currentTime = System.currentTimeMillis();
                    // Prevent multiple detections in short time
                    if (currentTime - lastFallTime > FALL_WINDOW_MS) {
                        lastFallTime = currentTime;
                        Log.d(TAG, "Fall detected! Delta: " + deltaAcceleration);
                        
                        if (listener != null) {
                            listener.onFallDetected();
                        }
                    }
                }
            }
            
            lastAcceleration[0] = acceleration;
            lastAcceleration[1] = x;
            lastAcceleration[2] = y;
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Sensor accuracy changed
    }
    
    public boolean isAvailable() {
        return accelerometer != null;
    }
}

