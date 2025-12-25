package com.harmonycare.app.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.harmonycare.app.R;
import com.harmonycare.app.data.model.User;
import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.repository.UserRepository;
import com.harmonycare.app.util.AnimationHelper;
import com.harmonycare.app.util.Constants;
import com.harmonycare.app.util.ErrorHandler;
import com.harmonycare.app.util.HapticHelper;
import com.harmonycare.app.util.LocationHelper;
import com.harmonycare.app.util.NetworkHelper;
import com.harmonycare.app.util.NotificationHelper;
import com.harmonycare.app.util.TTSHelper;
import com.harmonycare.app.util.VoiceCommandHelper;
import com.harmonycare.app.viewmodel.AuthViewModel;
import com.harmonycare.app.viewmodel.EmergencyViewModel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

/**
 * Elderly Dashboard Activity with large SOS button
 */
public class ElderlyDashboardActivity extends BaseActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private Button btnSOS;
    private CardView cardHistory;
    private CardView cardSettings;
    private CardView cardCancelButton;
    private CardView cardChatButton;
    private CardView cardEmergencyContacts;
    private Button btnCancelEmergency;
    private TextView tvWelcome;
    private ImageView ivProfilePhoto;
    private TextView tvCountdown;
    private TextView tvEmergencyStatus;
    private TextView tvUnreadCount;
    private CardView cardEmergencyStatus;
    private Emergency activeEmergency;
    private CountDownTimer countDownTimer;
    private boolean isSOSPressed = false;
    private LocationHelper locationHelper;
    private EmergencyViewModel emergencyViewModel;
    private AuthViewModel authViewModel;
    private NotificationHelper notificationHelper;
    private TTSHelper ttsHelper;
    private VoiceCommandHelper voiceCommandHelper;
    private NetworkHelper networkHelper;
    private TextView tvOfflineStatus;
    private int currentUserId;
    private UserRepository userRepository;
    private Handler statusCheckHandler;
    private Runnable statusCheckRunnable;
    private boolean voiceCommandsEnabled = false;
    private BroadcastReceiver fallDetectionReceiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elderly_dashboard);
        
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        emergencyViewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);
        locationHelper = new LocationHelper(this);
        notificationHelper = new NotificationHelper(this);
        ttsHelper = new TTSHelper(this);
        voiceCommandHelper = new VoiceCommandHelper(this);
        networkHelper = new NetworkHelper(this);
        userRepository = new UserRepository(this);
        
        // Load voice commands setting
        android.content.SharedPreferences prefs = getSharedPreferences("HarmonyCarePrefs", MODE_PRIVATE);
        voiceCommandsEnabled = prefs.getBoolean("voice_commands_enabled", false);
        
        currentUserId = authViewModel.getCurrentUserId();
        if (currentUserId == -1) {
            finish();
            return;
        }
        
        try {
            initViews();
            checkLocationPermission();
            setupObservers();
            checkActiveEmergency();
            setupVoiceCommands();
            checkNetworkStatus();
            setupFallDetectionReceiver();
            loadHeaderProfilePhoto();
        } catch (Exception e) {
            android.util.Log.e("ElderlyDashboard", "Error in onCreate", e);
            Toast.makeText(this, "Error initializing app. Please restart.", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void setupFallDetectionReceiver() {
        try {
            fallDetectionReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if ("com.harmonycare.app.FALL_DETECTED".equals(intent.getAction())) {
                        // Fall detected - show confirmation dialog
                        runOnUiThread(() -> {
                            new androidx.appcompat.app.AlertDialog.Builder(ElderlyDashboardActivity.this)
                                    .setTitle("Fall Detected!")
                                    .setMessage("A fall has been detected. Do you want to send an emergency request?")
                                    .setPositiveButton("Yes, Send Emergency", (dialog, which) -> {
                                        // Trigger emergency
                                        if (btnSOS != null && btnSOS.isEnabled()) {
                                            startSOSCountdown();
                                        }
                                    })
                                    .setNegativeButton("No, I'm OK", null)
                                    .setCancelable(false)
                                    .show();
                        });
                    }
                }
            };
            
            IntentFilter filter = new IntentFilter("com.harmonycare.app.FALL_DETECTED");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ requires explicit flag
                registerReceiver(fallDetectionReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(fallDetectionReceiver, filter);
            }
        } catch (Exception e) {
            // Silently fail - receiver registration is not critical
            android.util.Log.e("ElderlyDashboard", "Error setting up fall detection receiver", e);
        }
    }
    
    private void checkNetworkStatus() {
        // Since this is a local-only app, we'll just show offline indicator
        // In a real app with backend, this would queue operations
        boolean isOnline = networkHelper.isConnected();
        if (tvOfflineStatus != null) {
            if (!isOnline) {
                tvOfflineStatus.setVisibility(View.VISIBLE);
                tvOfflineStatus.setText("Offline Mode - Data saved locally");
            } else {
                tvOfflineStatus.setVisibility(View.GONE);
            }
        }
    }
    
    private void setupVoiceCommands() {
        try {
            if (voiceCommandHelper != null && voiceCommandHelper.isAvailable() && voiceCommandsEnabled) {
                voiceCommandHelper.setListener(new VoiceCommandHelper.VoiceCommandListener() {
                    @Override
                    public void onSOSDetected() {
                        // Trigger SOS when voice command detected
                        if (!isSOSPressed && btnSOS != null && btnSOS.isEnabled()) {
                            runOnUiThread(() -> {
                                try {
                                    if (ttsHelper != null) {
                                        ttsHelper.speak("Emergency detected from voice command");
                                    }
                                    startSOSCountdown();
                                } catch (Exception e) {
                                    android.util.Log.e("ElderlyDashboard", "Error in voice command SOS", e);
                                }
                            });
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        // Log error
                        android.util.Log.w("ElderlyDashboard", "Voice command error: " + error);
                        
                        // If it's a critical error (like error code 11), disable voice commands
                        if (error != null && (error.contains("code: 11") || error.contains("Unknown error"))) {
                            android.util.Log.w("ElderlyDashboard", "Disabling voice commands due to critical error");
                            // Disable voice commands in settings
                            android.content.SharedPreferences prefs = getSharedPreferences("HarmonyCarePrefs", MODE_PRIVATE);
                            prefs.edit().putBoolean("voice_commands_enabled", false).apply();
                            voiceCommandsEnabled = false;
                            
                            // Stop listening
                            if (voiceCommandHelper != null) {
                                voiceCommandHelper.stopListening();
                            }
                            
                            // Show user-friendly message
                            runOnUiThread(() -> {
                                showToast("Voice commands disabled due to error. Please enable again from Settings.");
                            });
                        }
                    }
                });
                
                // Check microphone permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                        == PackageManager.PERMISSION_GRANTED) {
                    voiceCommandHelper.startListening();
                } else {
                    ActivityCompat.requestPermissions(this, 
                            new String[]{Manifest.permission.RECORD_AUDIO}, 
                            Constants.CAMERA_PERMISSION_REQUEST_CODE + 1);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("ElderlyDashboard", "Error setting up voice commands", e);
        }
    }
    
    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvOfflineStatus = findViewById(R.id.tvOfflineStatus);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        btnSOS = findViewById(R.id.btnSOS);
        cardHistory = findViewById(R.id.cardHistory);
        cardSettings = findViewById(R.id.cardSettings);
        cardCancelButton = findViewById(R.id.cardCancelButton);
        cardChatButton = findViewById(R.id.cardChatButton);
        cardEmergencyContacts = findViewById(R.id.cardEmergencyContacts);
        btnCancelEmergency = findViewById(R.id.btnCancelEmergency);
        tvCountdown = findViewById(R.id.tvCountdown);
        tvEmergencyStatus = findViewById(R.id.tvEmergencyStatus);
        tvUnreadCount = findViewById(R.id.tvUnreadCount);
        cardEmergencyStatus = findViewById(R.id.cardEmergencyStatus);
        
        String userName = authViewModel.getCurrentUserName();
        if (tvWelcome != null) {
            tvWelcome.setText("Welcome, " + userName);
        }
        
        // Add pulse animation to SOS button and text shadow for visibility
        if (btnSOS != null) {
            // Add text shadow for better visibility on red background
            btnSOS.setShadowLayer(8f, 0f, 4f, 0xFF000000);
            
            Animation pulseAnimation = new AlphaAnimation(1.0f, 0.5f);
            pulseAnimation.setDuration(1000);
            pulseAnimation.setRepeatCount(Animation.INFINITE);
            pulseAnimation.setRepeatMode(Animation.REVERSE);
            btnSOS.startAnimation(pulseAnimation);
            
            btnSOS.setOnLongClickListener(v -> {
                if (!isSOSPressed && btnSOS.isEnabled()) {
                    // Vibration feedback on press
                    HapticHelper.vibrateSOSPress(this);
                    startSOSCountdown();
                    return true;
                }
                return false;
            });
            
            // Add touch feedback for better UX
            btnSOS.setOnTouchListener((v, event) -> {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN && !isSOSPressed) {
                    // Scale animation on press
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    HapticHelper.vibrateShort(this);
                } else if (event.getAction() == android.view.MotionEvent.ACTION_UP || 
                          event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                }
                return false; // Let long click listener handle it
            });
            
            btnSOS.setOnClickListener(v -> {
                // Show instruction for long press
                if (!isSOSPressed && btnSOS.isEnabled()) {
                    showToast("Hold the button for 3 seconds to send emergency");
                }
            });
        }
        
        if (cardHistory != null) {
            // Animate card on appear
            AnimationHelper.cardSlideIn(cardHistory, 100);
            cardHistory.setOnClickListener(v -> {
                AnimationHelper.buttonPress(cardHistory);
                AnimationHelper.buttonRelease(cardHistory);
                Intent intent = new Intent(ElderlyDashboardActivity.this, EmergencyHistoryActivity.class);
                startActivity(intent);
            });
        }
        
        if (cardSettings != null) {
            // Animate card on appear
            AnimationHelper.cardSlideIn(cardSettings, 200);
            cardSettings.setOnClickListener(v -> {
                AnimationHelper.buttonPress(cardSettings);
                AnimationHelper.buttonRelease(cardSettings);
                Intent intent = new Intent(ElderlyDashboardActivity.this, SettingsActivity.class);
                startActivity(intent);
            });
        }
        
        if (cardEmergencyContacts != null) {
            // Animate card on appear
            AnimationHelper.cardSlideIn(cardEmergencyContacts, 150);
            cardEmergencyContacts.setOnClickListener(v -> {
                AnimationHelper.buttonPress(cardEmergencyContacts);
                AnimationHelper.buttonRelease(cardEmergencyContacts);
                Intent intent = new Intent(ElderlyDashboardActivity.this, EmergencyContactsActivity.class);
                startActivity(intent);
            });
        }
        
        if (btnCancelEmergency != null) {
            btnCancelEmergency.setOnClickListener(v -> cancelActiveEmergency());
        }
        
        // Make entire chat card clickable
        if (cardChatButton != null) {
            cardChatButton.setOnClickListener(v -> openChat());
        }
    }

    private void loadHeaderProfilePhoto() {
        if (ivProfilePhoto == null || userRepository == null || currentUserId <= 0) {
            return;
        }

        userRepository.getUserById(currentUserId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;
                String photoPath = user.getPhotoPath();
                if (photoPath == null || photoPath.isEmpty()) return;

                try {
                    File photoFile = new File(photoPath);
                    if (!photoFile.exists()) return;
                    Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
                    if (bitmap == null) return;
                    runOnUiThread(() -> {
                        if (ivProfilePhoto != null) {
                            ivProfilePhoto.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onError(Exception error) {
            }
        });
    }
    
    private void openChat() {
        if (activeEmergency != null) {
            String status = activeEmergency.getStatus();
            // Chat is only available when emergency is accepted (not completed)
            if ("accepted".equalsIgnoreCase(status)) {
                Intent intent = new Intent(ElderlyDashboardActivity.this, ChatActivity.class);
                intent.putExtra("emergency_id", activeEmergency.getId());
                startActivity(intent);
            } else if ("completed".equalsIgnoreCase(status)) {
                showToast("Emergency completed - Chat connection closed");
            } else if ("active".equalsIgnoreCase(status)) {
                showToast("Waiting for volunteer to accept...");
            } else {
                showToast("Chat is only available when emergency is accepted");
            }
        } else {
            showToast("No active emergency found");
        }
    }
    
    private void checkActiveEmergency() {
        try {
            if (emergencyViewModel == null || currentUserId <= 0) {
                return;
            }
            emergencyViewModel.getActiveEmergencyByElderly(currentUserId, emergency -> {
                try {
                    activeEmergency = emergency;
                    if (activeEmergency != null) {
                        String status = activeEmergency.getStatus();
                        if (status == null) {
                            status = "unknown";
                        }
                        
                        // Show emergency status card
                        if (cardEmergencyStatus != null) {
                            cardEmergencyStatus.setVisibility(View.VISIBLE);
                        }
                        
                        // Update status text
                        if (tvEmergencyStatus != null) {
                            if ("active".equalsIgnoreCase(status)) {
                                tvEmergencyStatus.setText("Emergency Request Sent\nWaiting for volunteer...");
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    tvEmergencyStatus.setTextColor(getResources().getColor(R.color.sos_red, getTheme()));
                                } else {
                                    tvEmergencyStatus.setTextColor(getResources().getColor(R.color.sos_red));
                                }
                            } else if ("accepted".equalsIgnoreCase(status)) {
                                tvEmergencyStatus.setText("Emergency Accepted\nA volunteer is on the way!");
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    tvEmergencyStatus.setTextColor(getResources().getColor(R.color.primary, getTheme()));
                                } else {
                                    tvEmergencyStatus.setTextColor(getResources().getColor(R.color.primary));
                                }
                            } else if ("completed".equalsIgnoreCase(status)) {
                                tvEmergencyStatus.setText("Emergency Completed\nYou are safe now!");
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    tvEmergencyStatus.setTextColor(getResources().getColor(R.color.success_green, getTheme()));
                                } else {
                                    tvEmergencyStatus.setTextColor(getResources().getColor(R.color.success_green));
                                }
                            }
                        }
                        
                        // Show/hide buttons based on status
                        if ("active".equalsIgnoreCase(status)) {
                            if (cardCancelButton != null) {
                                cardCancelButton.setVisibility(View.VISIBLE);
                            }
                            // Show chat button even when active (volunteer might message)
                            if (cardChatButton != null) {
                                cardChatButton.setVisibility(View.VISIBLE);
                            }
                            if (btnSOS != null) {
                                btnSOS.setEnabled(false);
                                btnSOS.setText("Emergency Active");
                            }
                        } else if ("accepted".equalsIgnoreCase(status)) {
                            // Emergency accepted - show chat button prominently
                            if (cardCancelButton != null) {
                                cardCancelButton.setVisibility(View.GONE);
                            }
                            if (cardChatButton != null) {
                                cardChatButton.setVisibility(View.VISIBLE);
                            }
                            if (btnSOS != null) {
                                btnSOS.setEnabled(false);
                                btnSOS.setText("Volunteer Coming");
                            }
                            // Show notification that volunteer accepted
                            showToast("Volunteer accepted your emergency! You can now chat.");
                        } else if ("completed".equalsIgnoreCase(status)) {
                            // Emergency completed
                            if (cardCancelButton != null) {
                                cardCancelButton.setVisibility(View.GONE);
                            }
                            if (cardChatButton != null) {
                                cardChatButton.setVisibility(View.VISIBLE); // Still show chat for history
                            }
                            if (btnSOS != null) {
                                btnSOS.setEnabled(true);
                                btnSOS.setText(getString(R.string.sos_button));
                            }
                        } else {
                            if (cardCancelButton != null) {
                                cardCancelButton.setVisibility(View.GONE);
                            }
                            if (cardChatButton != null) {
                                cardChatButton.setVisibility(View.GONE);
                            }
                            if (btnSOS != null) {
                                btnSOS.setEnabled(true);
                                btnSOS.setText(getString(R.string.sos_button));
                            }
                        }
                    } else {
                        // No active emergency
                        if (cardEmergencyStatus != null) {
                            cardEmergencyStatus.setVisibility(View.GONE);
                        }
                        if (cardCancelButton != null) {
                            cardCancelButton.setVisibility(View.GONE);
                        }
                        if (cardChatButton != null) {
                            cardChatButton.setVisibility(View.GONE);
                        }
                        if (btnSOS != null) {
                            btnSOS.setEnabled(true);
                            btnSOS.setText(getString(R.string.sos_button));
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("ElderlyDashboard", "Error in checkActiveEmergency callback", e);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("ElderlyDashboard", "Error checking active emergency", e);
        }
    }
    
    private void cancelActiveEmergency() {
        if (activeEmergency != null) {
            ErrorHandler.showConfirmationDialog(this, "Cancel Emergency", 
                    "Are you sure you want to cancel this emergency request?",
                    (dialog, which) -> {
                        emergencyViewModel.cancelEmergency(activeEmergency.getId());
                        checkActiveEmergency();
                        Toast.makeText(this, "Emergency cancelled", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    
    private void setupObservers() {
        try {
            emergencyViewModel.getEmergencyCreated().observe(this, success -> {
                try {
                    if (success) {
                        if (ttsHelper != null) {
                            ttsHelper.speak("Emergency request sent successfully");
                        }
                        if (notificationHelper != null) {
                            notificationHelper.showSOSNotification("Your emergency request has been sent to nearby volunteers");
                        }
                        showToast("Emergency request sent!");
                        checkActiveEmergency();
                    } else {
                        ErrorHandler.showErrorDialog(this, "Error", "Failed to send emergency request");
                    }
                } catch (Exception e) {
                    android.util.Log.e("ElderlyDashboard", "Error in emergency created observer", e);
                }
            });
            
            emergencyViewModel.getErrorMessage().observe(this, error -> {
                try {
                    if (error != null && !error.isEmpty()) {
                        ErrorHandler.showErrorDialog(this, "Error", error);
                    }
                } catch (Exception e) {
                    android.util.Log.e("ElderlyDashboard", "Error in error message observer", e);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("ElderlyDashboard", "Error setting up observers", e);
        }
        
        // Poll for emergency status changes (to detect when volunteer accepts)
        statusCheckHandler = new Handler(Looper.getMainLooper());
        statusCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (activeEmergency != null && "active".equalsIgnoreCase(activeEmergency.getStatus())) {
                    // Check if status changed to accepted
                    checkActiveEmergency();
                }
                if (statusCheckHandler != null) {
                    statusCheckHandler.postDelayed(this, 5000); // Check every 5 seconds
                }
            }
        };
        statusCheckHandler.postDelayed(statusCheckRunnable, 5000);
    }
    
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission is required for emergency services", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == Constants.CAMERA_PERMISSION_REQUEST_CODE + 1) {
            // Microphone permission for voice commands
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (voiceCommandHelper != null && voiceCommandsEnabled) {
                    voiceCommandHelper.startListening();
                }
            }
        }
    }

    private void startSOSCountdown() {
        try {
            if (isSOSPressed) return;
            
            if (btnSOS == null) {
                return;
            }
            
            isSOSPressed = true;
            btnSOS.setEnabled(false);
            if (tvCountdown != null) {
                tvCountdown.setVisibility(View.VISIBLE);
                // Add text shadow for better visibility
                tvCountdown.setShadowLayer(8f, 0f, 4f, 0xFF000000);
            }
            
            countDownTimer = new CountDownTimer(3000, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    try {
                        int seconds = (int) (millisUntilFinished / 1000);
                        int milliseconds = (int) (millisUntilFinished % 1000);
                        
                        // Update countdown display
                        if (tvCountdown != null) {
                            tvCountdown.setText(String.valueOf(seconds + 1));
                            // Pulse animation for countdown
                            float scale = 1.0f + (milliseconds / 1000.0f) * 0.1f;
                            tvCountdown.setScaleX(scale);
                            tvCountdown.setScaleY(scale);
                        }
                        if (btnSOS != null) {
                            btnSOS.setText(String.valueOf(seconds + 1));
                            btnSOS.setShadowLayer(8f, 0f, 4f, 0xFF000000);
                        }
                        
                        // Vibration and animation on each second
                        if (milliseconds < 100) {
                            HapticHelper.vibrateSOSTick(ElderlyDashboardActivity.this);
                            // Scale animation
                            if (btnSOS != null) {
                                btnSOS.animate()
                                    .scaleX(1.1f)
                                    .scaleY(1.1f)
                                    .setDuration(100)
                                    .withEndAction(() -> {
                                        if (btnSOS != null) {
                                            btnSOS.animate()
                                                .scaleX(1.0f)
                                                .scaleY(1.0f)
                                                .setDuration(200)
                                                .start();
                                        }
                                    })
                                    .start();
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ElderlyDashboard", "Error in countdown tick", e);
                    }
                }
                
                @Override
                public void onFinish() {
                    try {
                        // Final vibration and animation
                        HapticHelper.vibrateSOSComplete(ElderlyDashboardActivity.this);
                        
                        if (btnSOS != null) {
                            btnSOS.animate()
                                .scaleX(1.2f)
                                .scaleY(1.2f)
                                .setDuration(200)
                                .withEndAction(() -> {
                                    if (btnSOS != null) {
                                        btnSOS.animate()
                                            .scaleX(1.0f)
                                            .scaleY(1.0f)
                                            .setDuration(200)
                                            .start();
                                    }
                                })
                                .start();
                        }
                        
                        if (tvCountdown != null) {
                            tvCountdown.setVisibility(View.GONE);
                            tvCountdown.setScaleX(1.0f);
                            tvCountdown.setScaleY(1.0f);
                        }
                        sendSOS();
                        resetSOSButton();
                    } catch (Exception e) {
                        android.util.Log.e("ElderlyDashboard", "Error in countdown finish", e);
                        resetSOSButton();
                    }
                }
            };
            countDownTimer.start();
        } catch (Exception e) {
            android.util.Log.e("ElderlyDashboard", "Error starting SOS countdown", e);
            resetSOSButton();
        }
    }
    
    private void resetSOSButton() {
        try {
            isSOSPressed = false;
            if (btnSOS != null) {
                btnSOS.setEnabled(true);
                btnSOS.setText(getString(R.string.sos_button));
            }
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
        } catch (Exception e) {
            android.util.Log.e("ElderlyDashboard", "Error resetting SOS button", e);
        }
    }
    
    private void sendSOS() {
        try {
            if (locationHelper == null) {
                ErrorHandler.showErrorDialog(this, "Error", "Location service not available");
                resetSOSButton();
                return;
            }
            
            if (!locationHelper.hasLocationPermission()) {
                ErrorHandler.showErrorDialog(this, "Permission Required", 
                        "Location permission is required for emergency services");
                checkLocationPermission();
                resetSOSButton();
                return;
            }
            
            Location location = locationHelper.getLastKnownLocation();
            if (location == null) {
                showToast("Getting location... Please wait");
                // Try to get location updates
                locationHelper.requestLocationUpdates(location1 -> {
                    if (location1 != null) {
                        createEmergency(location1.getLatitude(), location1.getLongitude());
                        locationHelper.stopLocationUpdates();
                    } else {
                        resetSOSButton();
                    }
                });
                return;
            }
            
            createEmergency(location.getLatitude(), location.getLongitude());
        } catch (Exception e) {
            android.util.Log.e("ElderlyDashboard", "Error sending SOS", e);
            ErrorHandler.showErrorDialog(this, "Error", "Failed to send emergency. Please try again.");
            resetSOSButton();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadHeaderProfilePhoto();
        // Reload voice commands setting
        android.content.SharedPreferences prefs = getSharedPreferences("HarmonyCarePrefs", MODE_PRIVATE);
        boolean newVoiceCommandsEnabled = prefs.getBoolean("voice_commands_enabled", false);
        if (newVoiceCommandsEnabled != voiceCommandsEnabled) {
            voiceCommandsEnabled = newVoiceCommandsEnabled;
            if (voiceCommandsEnabled) {
                setupVoiceCommands();
            } else {
                if (voiceCommandHelper != null) {
                    voiceCommandHelper.stopListening();
                }
            }
        } else if (voiceCommandsEnabled && voiceCommandHelper != null) {
            setupVoiceCommands();
        }
        
        // Check network status
        checkNetworkStatus();
        // Refresh emergency status when returning to screen
        checkActiveEmergency();
    }
    
    private void createEmergency(double latitude, double longitude) {
        try {
            if (emergencyViewModel != null && currentUserId > 0) {
                emergencyViewModel.createEmergency(currentUserId, latitude, longitude);
            } else {
                showToast("Unable to create emergency. Please try again.");
            }
        } catch (Exception e) {
            android.util.Log.e("ElderlyDashboard", "Error creating emergency", e);
            showToast("Error creating emergency. Please try again.");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceCommandHelper != null) {
            voiceCommandHelper.destroy();
        }
        try {
            if (fallDetectionReceiver != null) {
                unregisterReceiver(fallDetectionReceiver);
            }
        } catch (Exception e) {
            // Ignore - receiver might not be registered
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (statusCheckHandler != null && statusCheckRunnable != null) {
            statusCheckHandler.removeCallbacks(statusCheckRunnable);
        }
        if (locationHelper != null) {
            locationHelper.stopLocationUpdates();
        }
        if (ttsHelper != null) {
            ttsHelper.shutdown();
        }
    }
}

