package com.harmonycare.app.view;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.harmonycare.app.service.FallDetectionService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.harmonycare.app.R;
import com.harmonycare.app.viewmodel.AuthViewModel;

/**
 * Settings Activity
 */
public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "HarmonyCarePrefs";
    private static final String KEY_LARGE_TEXT = "large_text_mode";
    private static final int ACTIVITY_RECOGNITION_PERMISSION_REQUEST = 2001;
    
    private Switch switchLargeText;
    private Switch switchVoiceCommands;
    private Switch switchFallDetection;
    private Button btnLogout;
    private Button btnProfile;
    private TextView tvTitle;
    private AuthViewModel authViewModel;
    private SharedPreferences sharedPreferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        initViews();
        loadSettings();
    }
    
    private void initViews() {
        switchLargeText = findViewById(R.id.switchLargeText);
        switchVoiceCommands = findViewById(R.id.switchVoiceCommands);
        switchFallDetection = findViewById(R.id.switchFallDetection);
        btnLogout = findViewById(R.id.btnLogout);
        btnProfile = findViewById(R.id.btnProfile);
        tvTitle = findViewById(R.id.tvTitle);
        
        switchLargeText.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveLargeTextMode(isChecked);
            applyLargeTextMode(isChecked);
        });
        
        switchVoiceCommands.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveVoiceCommandsEnabled(isChecked);
        });
        
        switchFallDetection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Check for required permissions before enabling
                if (checkActivityRecognitionPermission()) {
                    saveFallDetectionEnabled(true);
                    toggleFallDetectionService(true);
                } else {
                    // Permission not granted, uncheck the switch
                    switchFallDetection.setChecked(false);
                }
            } else {
                saveFallDetectionEnabled(false);
                toggleFallDetectionService(false);
            }
        });
        
        btnLogout.setOnClickListener(v -> performLogout());
        
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }
    
    private void loadSettings() {
        boolean largeTextMode = sharedPreferences.getBoolean(KEY_LARGE_TEXT, false);
        switchLargeText.setChecked(largeTextMode);
        applyLargeTextMode(largeTextMode);
        
        boolean voiceCommandsEnabled = sharedPreferences.getBoolean("voice_commands_enabled", false);
        if (switchVoiceCommands != null) {
            switchVoiceCommands.setChecked(voiceCommandsEnabled);
        }
        
        boolean fallDetectionEnabled = sharedPreferences.getBoolean("fall_detection_enabled", false);
        if (switchFallDetection != null) {
            switchFallDetection.setChecked(fallDetectionEnabled);
        }
    }
    
    private void saveFallDetectionEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("fall_detection_enabled", enabled);
        editor.apply();
    }
    
    private boolean checkActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        ACTIVITY_RECOGNITION_PERMISSION_REQUEST);
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTIVITY_RECOGNITION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable fall detection
                switchFallDetection.setChecked(true);
                saveFallDetectionEnabled(true);
                toggleFallDetectionService(true);
                Toast.makeText(this, "Fall detection enabled", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Activity recognition permission is required for fall detection", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void toggleFallDetectionService(boolean enabled) {
        try {
            Intent serviceIntent = new Intent(this, FallDetectionService.class);
            if (enabled) {
                // Double-check permission before starting
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                            != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission required for fall detection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                serviceIntent.setAction("START_MONITORING");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            } else {
                serviceIntent.setAction("STOP_MONITORING");
                stopService(serviceIntent);
            }
        } catch (SecurityException e) {
            android.util.Log.e("SettingsActivity", "Error starting fall detection service", e);
            Toast.makeText(this, "Error starting fall detection service. Please check permissions.", Toast.LENGTH_LONG).show();
            switchFallDetection.setChecked(false);
            saveFallDetectionEnabled(false);
        } catch (Exception e) {
            android.util.Log.e("SettingsActivity", "Unexpected error", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveVoiceCommandsEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("voice_commands_enabled", enabled);
        editor.apply();
    }
    
    private void saveLargeTextMode(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LARGE_TEXT, enabled);
        editor.apply();
    }
    
    private void applyLargeTextMode(boolean enabled) {
        // Apply large text mode to views
        if (enabled) {
            tvTitle.setTextSize(24);
        } else {
            tvTitle.setTextSize(20);
        }
        // Note: In a full implementation, you would apply this to all text views
    }
    
    private void performLogout() {
        authViewModel.logout();
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

