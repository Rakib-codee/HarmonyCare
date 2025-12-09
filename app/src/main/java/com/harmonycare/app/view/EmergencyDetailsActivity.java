package com.harmonycare.app.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.harmonycare.app.R;
import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.model.User;
import com.harmonycare.app.data.repository.UserRepository;
import com.harmonycare.app.viewmodel.AuthViewModel;
import com.harmonycare.app.viewmodel.EmergencyViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Emergency Details Activity
 */
public class EmergencyDetailsActivity extends AppCompatActivity {
    private TextView tvElderlyName, tvContact, tvLocation, tvStatus, tvTime;
    private Button btnChat, btnNavigate, btnMarkCompleted, btnRate;
    private EmergencyViewModel emergencyViewModel;
    private AuthViewModel authViewModel;
    private UserRepository userRepository;
    private Emergency emergency;
    private int emergencyId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_details);
        
        emergencyViewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userRepository = new UserRepository(this);
        
        emergencyId = getIntent().getIntExtra("emergency_id", -1);
        if (emergencyId == -1) {
            Toast.makeText(this, "Invalid emergency", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        loadEmergencyDetails();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload emergency details when returning from chat or other activities
        // This ensures we have the latest status (especially if it was completed)
        if (emergencyId > 0) {
            loadEmergencyDetails();
        }
    }
    
    private void initViews() {
        tvElderlyName = findViewById(R.id.tvElderlyName);
        tvContact = findViewById(R.id.tvContact);
        tvLocation = findViewById(R.id.tvLocation);
        tvStatus = findViewById(R.id.tvStatus);
        tvTime = findViewById(R.id.tvTime);
        btnChat = findViewById(R.id.btnChat);
        btnNavigate = findViewById(R.id.btnNavigate);
        btnMarkCompleted = findViewById(R.id.btnMarkCompleted);
        btnRate = findViewById(R.id.btnRate);
        
        btnChat.setOnClickListener(v -> openChat());
        btnNavigate.setOnClickListener(v -> openNavigation());
        btnMarkCompleted.setOnClickListener(v -> markAsCompleted());
        if (btnRate != null) {
            btnRate.setOnClickListener(v -> openRating());
        }
    }
    
    private void openChat() {
        Intent intent = new Intent(EmergencyDetailsActivity.this, ChatActivity.class);
        intent.putExtra("emergency_id", emergencyId);
        startActivity(intent);
    }
    
    private void loadEmergencyDetails() {
        emergencyViewModel.getEmergencyById(emergencyId, loadedEmergency -> {
            emergency = loadedEmergency;
            if (emergency == null) {
                Toast.makeText(EmergencyDetailsActivity.this, "Emergency not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            // Check if emergency is completed - if so, show message and disable actions
            if ("completed".equalsIgnoreCase(emergency.getStatus())) {
                btnMarkCompleted.setEnabled(false);
                btnMarkCompleted.setText("Completed");
                btnChat.setVisibility(View.GONE);
                Toast.makeText(EmergencyDetailsActivity.this, "Emergency has been completed", Toast.LENGTH_SHORT).show();
            }
            updateUI();
        });
    }
    
    private void updateUI() {
        if (emergency == null) return;
        
            tvLocation.setText(String.format(Locale.getDefault(), "%.6f, %.6f",
                    emergency.getLatitude(), emergency.getLongitude()));
            tvStatus.setText(emergency.getStatus().toUpperCase());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvTime.setText(sdf.format(new Date(emergency.getTimestamp())));
            
            // Show chat button only if emergency is accepted
            if ("accepted".equalsIgnoreCase(emergency.getStatus())) {
                btnChat.setVisibility(View.VISIBLE);
            } else {
                btnChat.setVisibility(View.GONE);
            }
            
            // Hide mark completed button if already completed
            if ("completed".equals(emergency.getStatus())) {
                btnMarkCompleted.setEnabled(false);
                btnMarkCompleted.setText("Completed");
                
                // Show rating button for elderly users
                int currentUserId = authViewModel.getCurrentUserId();
                if (currentUserId == emergency.getElderlyId() && btnRate != null) {
                    btnRate.setVisibility(View.VISIBLE);
                }
            } else if (btnRate != null) {
                btnRate.setVisibility(View.GONE);
            }
            
            // Load user asynchronously
            User cachedUser = userRepository.getUserFromCache(emergency.getElderlyId());
            if (cachedUser != null) {
                tvElderlyName.setText(cachedUser.getName());
                tvContact.setText(cachedUser.getContact());
            } else {
                userRepository.getUserById(emergency.getElderlyId(), new UserRepository.RepositoryCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user != null) {
                            tvElderlyName.setText(user.getName());
                            tvContact.setText(user.getContact());
                        }
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        tvElderlyName.setText("Unknown");
                        tvContact.setText("N/A");
                    }
                });
            }
        }
    
    private void openNavigation() {
        if (emergency == null) {
            Toast.makeText(this, "Emergency information not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Open VolunteerMapActivity which has AMap integration
        // This will show the map with the emergency location and allow navigation
        try {
            Intent intent = new Intent(EmergencyDetailsActivity.this, VolunteerMapActivity.class);
            // Pass emergency ID so map can focus on this emergency
            intent.putExtra("emergency_id", emergencyId);
            intent.putExtra("focus_emergency", true);
            startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("EmergencyDetailsActivity", "Error opening map", e);
            // Fallback to external map app if VolunteerMapActivity fails
            openExternalMap();
        }
    }
    
    /**
     * Fallback method to open external map application
     */
    private void openExternalMap() {
        if (emergency == null) return;
        
        // Try multiple map app options
        // Option 1: Generic geo URI
        String geoUri = String.format(Locale.getDefault(), "geo:%f,%f?q=%f,%f",
                emergency.getLatitude(), emergency.getLongitude(),
                emergency.getLatitude(), emergency.getLongitude());
        Intent geoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
        
        if (geoIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(geoIntent);
            return;
        }
        
        // Option 2: Google Maps web (if available)
        String googleMapsUri = String.format(Locale.getDefault(),
                "https://www.google.com/maps/search/?api=1&query=%f,%f",
                emergency.getLatitude(), emergency.getLongitude());
        Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsUri));
        if (googleIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(googleIntent);
            return;
        }
        
        // Option 3: OpenStreetMap web (works everywhere)
        String osmUri = String.format(Locale.getDefault(),
                "https://www.openstreetmap.org/?mlat=%f&mlon=%f&zoom=15",
                emergency.getLatitude(), emergency.getLongitude());
        Intent osmIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(osmUri));
        if (osmIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(osmIntent);
            return;
        }
        
        // Last resort: Show coordinates
        Toast.makeText(this, 
            String.format(Locale.getDefault(), "Location: %.6f, %.6f", 
                emergency.getLatitude(), emergency.getLongitude()), 
            Toast.LENGTH_LONG).show();
    }
    
    private void markAsCompleted() {
        emergencyViewModel.completeEmergency(emergencyId);
        Toast.makeText(this, "Emergency marked as completed", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private void openRating() {
        Intent intent = new Intent(EmergencyDetailsActivity.this, RatingActivity.class);
        intent.putExtra("emergency_id", emergencyId);
        startActivity(intent);
    }
}

