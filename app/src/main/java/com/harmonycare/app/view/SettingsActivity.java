package com.harmonycare.app.view;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.harmonycare.app.service.FallDetectionService;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.harmonycare.app.R;
import com.harmonycare.app.util.BackupHelper;
import com.harmonycare.app.util.Constants;
import com.harmonycare.app.util.SnackbarHelper;
import com.harmonycare.app.util.ThemeHelper;
import com.harmonycare.app.viewmodel.AuthViewModel;

/**
 * Settings Activity
 */
public class SettingsActivity extends BaseActivity {
    private static final String PREFS_NAME = "HarmonyCarePrefs";
    private static final String KEY_LARGE_TEXT = "large_text_mode";
    private static final int ACTIVITY_RECOGNITION_PERMISSION_REQUEST = 2001;
    
    private Spinner spinnerTheme;
    private Switch switchLargeText;
    private Switch switchVoiceCommands;
    private Switch switchFallDetection;
    private Switch switchNotificationSound;
    private Switch switchNotificationVibration;
    private Button btnLogout;
    private Button btnProfile;
    private Button btnBackup;
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
        spinnerTheme = findViewById(R.id.spinnerTheme);
        switchLargeText = findViewById(R.id.switchLargeText);
        switchVoiceCommands = findViewById(R.id.switchVoiceCommands);
        switchFallDetection = findViewById(R.id.switchFallDetection);
        switchNotificationSound = findViewById(R.id.switchNotificationSound);
        switchNotificationVibration = findViewById(R.id.switchNotificationVibration);
        btnLogout = findViewById(R.id.btnLogout);
        btnProfile = findViewById(R.id.btnProfile);
        btnBackup = findViewById(R.id.btnBackup);
        tvTitle = findViewById(R.id.tvTitle);
        
        setupThemeSpinner();
        
        if (switchNotificationSound != null) {
            switchNotificationSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveNotificationSoundEnabled(isChecked);
            });
        }
        
        if (switchNotificationVibration != null) {
            switchNotificationVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveNotificationVibrationEnabled(isChecked);
            });
        }
        
        if (btnBackup != null) {
            btnBackup.setOnClickListener(v -> performBackup());
        }
        
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
                    // Temporarily remove listener to avoid recursive calls
                    switchFallDetection.setOnCheckedChangeListener(null);
                    switchFallDetection.setChecked(false);
                    // Re-add listener after state change
                    switchFallDetection.post(() -> {
                        switchFallDetection.setOnCheckedChangeListener((b, checked) -> {
                            if (checked) {
                                if (checkActivityRecognitionPermission()) {
                                    saveFallDetectionEnabled(true);
                                    toggleFallDetectionService(true);
                                } else {
                                    switchFallDetection.setOnCheckedChangeListener(null);
                                    switchFallDetection.setChecked(false);
                                    switchFallDetection.post(() -> switchFallDetection.setOnCheckedChangeListener((btn, chk) -> {
                                        if (chk) {
                                            if (checkActivityRecognitionPermission()) {
                                                saveFallDetectionEnabled(true);
                                                toggleFallDetectionService(true);
                                            } else {
                                                switchFallDetection.setOnCheckedChangeListener(null);
                                                switchFallDetection.setChecked(false);
                                                switchFallDetection.post(() -> initFallDetectionListener());
                                            }
                                        } else {
                                            saveFallDetectionEnabled(false);
                                            toggleFallDetectionService(false);
                                        }
                                    }));
                                }
                            } else {
                                saveFallDetectionEnabled(false);
                                toggleFallDetectionService(false);
                            }
                        });
                    });
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
    
    private void setupThemeSpinner() {
        String[] themeOptions = {
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, themeOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(adapter);
        
        // Set current theme selection
        String currentTheme = ThemeHelper.getThemeMode(this);
        int position = 0;
        if (ThemeHelper.THEME_DARK.equals(currentTheme)) {
            position = 1;
        } else if (ThemeHelper.THEME_SYSTEM.equals(currentTheme)) {
            position = 2;
        }
        spinnerTheme.setSelection(position, false); // false to prevent triggering listener on initial set
        
        // Store initial position to prevent unnecessary recreates
        final int[] previousPosition = {position};
        
        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Only process if selection actually changed
                if (position != previousPosition[0]) {
                    previousPosition[0] = position;
                    String selectedTheme;
                    switch (position) {
                        case 0:
                            selectedTheme = ThemeHelper.THEME_LIGHT;
                            break;
                        case 1:
                            selectedTheme = ThemeHelper.THEME_DARK;
                            break;
                        case 2:
                        default:
                            selectedTheme = ThemeHelper.THEME_SYSTEM;
                            break;
                    }
                    String currentThemeMode = ThemeHelper.getThemeMode(SettingsActivity.this);
                    if (!selectedTheme.equals(currentThemeMode)) {
                        ThemeHelper.saveThemeMode(SettingsActivity.this, selectedTheme);
                        // Restart activity to apply theme
                        recreate();
                    }
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
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
        
        boolean notificationSoundEnabled = sharedPreferences.getBoolean("notification_sound_enabled", true);
        if (switchNotificationSound != null) {
            switchNotificationSound.setChecked(notificationSoundEnabled);
        }
        
        boolean notificationVibrationEnabled = sharedPreferences.getBoolean("notification_vibration_enabled", true);
        if (switchNotificationVibration != null) {
            switchNotificationVibration.setChecked(notificationVibrationEnabled);
        }
    }
    
    private void saveNotificationSoundEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notification_sound_enabled", enabled);
        editor.apply();
    }
    
    private void saveNotificationVibrationEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notification_vibration_enabled", enabled);
        editor.apply();
    }
    
    private void performBackup() {
        int userId = authViewModel.getCurrentUserId();
        if (userId == -1) {
            SnackbarHelper.showError(findViewById(android.R.id.content), "User not logged in");
            return;
        }
        
        showProgressDialog("Creating backup...");
        BackupHelper backupHelper = new BackupHelper(this);
        backupHelper.exportData(userId, new BackupHelper.BackupCallback() {
            @Override
            public void onSuccess(String filePath) {
                hideProgressDialog();
                SnackbarHelper.showSuccess(findViewById(android.R.id.content), 
                    "Backup created successfully: " + new java.io.File(filePath).getName());
            }
            
            @Override
            public void onError(String error) {
                hideProgressDialog();
                SnackbarHelper.showError(findViewById(android.R.id.content), "Backup failed: " + error);
            }
        });
    }
    
    private void initFallDetectionListener() {
        switchFallDetection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Check for required permissions before enabling
                if (checkActivityRecognitionPermission()) {
                    saveFallDetectionEnabled(true);
                    toggleFallDetectionService(true);
                } else {
                    // Permission not granted, uncheck the switch
                    // Temporarily remove listener to avoid recursive calls
                    switchFallDetection.setOnCheckedChangeListener(null);
                    switchFallDetection.setChecked(false);
                    // Re-add listener after state change
                    switchFallDetection.post(() -> initFallDetectionListener());
                }
            } else {
                saveFallDetectionEnabled(false);
                toggleFallDetectionService(false);
            }
        });
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

