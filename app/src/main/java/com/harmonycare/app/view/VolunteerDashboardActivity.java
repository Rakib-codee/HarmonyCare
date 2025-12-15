package com.harmonycare.app.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.harmonycare.app.R;
import com.harmonycare.app.util.FcmTokenRegistrar;
import com.harmonycare.app.util.LocalNetworkBroadcastHelper;
import com.harmonycare.app.util.OfflineSyncHelper;
import com.harmonycare.app.util.NetworkHelper;
import com.harmonycare.app.util.LocationHelper;
import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.viewmodel.AuthViewModel;
import com.harmonycare.app.viewmodel.VolunteerViewModel;

/**
 * Volunteer Dashboard Activity
 */
public class VolunteerDashboardActivity extends BaseActivity {
    private TextView tvWelcome;
    private TextView tvAvailabilityStatus;
    private View viewStatusIndicator;
    private Switch switchAvailability;
    private CardView cardViewEmergencies;
    private CardView cardMapView;
    private CardView cardStats;
    private CardView cardHistory;
    private CardView cardSettings;
    
    private AuthViewModel authViewModel;
    private VolunteerViewModel volunteerViewModel;
    private int currentUserId;
    private LocalNetworkBroadcastHelper networkBroadcastHelper;
    private OfflineSyncHelper offlineSyncHelper;
    private NetworkHelper networkHelper;
    private LocationHelper locationHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_dashboard);
        
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        volunteerViewModel = new ViewModelProvider(this).get(VolunteerViewModel.class);
        
        currentUserId = authViewModel.getCurrentUserId();
        if (currentUserId == -1) {
            finish();
            return;
        }
        
        initViews();
        loadAvailabilityStatus();
        setupObservers();
        setupSyncAndBroadcast();
    }
    
    private void setupSyncAndBroadcast() {
        // Initialize helpers
        networkHelper = new NetworkHelper(this);
        offlineSyncHelper = new OfflineSyncHelper(this);
        networkBroadcastHelper = new LocalNetworkBroadcastHelper(this);
        locationHelper = new LocationHelper(this);
        
        // Start listening for local network broadcasts
        networkBroadcastHelper.setListener(new LocalNetworkBroadcastHelper.EmergencyListener() {
            @Override
            public void onEmergencyReceived(Emergency emergency) {
                // Refresh emergency list when new emergency received
                showToast("New emergency received from local network!");
            }
        });
        
        if (networkHelper.isWifiConnected()) {
            networkBroadcastHelper.startListening();
        }
        
        // Auto-sync pending operations when online
        if (networkHelper.isConnected()) {
            syncPendingOperations();
        }
    }
    
    private void syncPendingOperations() {
        offlineSyncHelper.syncAllPendingOperations((successCount, failureCount, message) -> {
            if (successCount > 0) {
                Log.d("VolunteerDashboard", "Synced " + successCount + " pending operations");
                showToast("Synced " + successCount + " pending emergencies");
            }
        });
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvAvailabilityStatus = findViewById(R.id.tvAvailabilityStatus);
        viewStatusIndicator = findViewById(R.id.viewStatusIndicator);
        switchAvailability = findViewById(R.id.switchAvailability);
        cardViewEmergencies = findViewById(R.id.cardViewEmergencies);
        cardMapView = findViewById(R.id.cardMapView);
        cardStats = findViewById(R.id.cardStats);
        cardHistory = findViewById(R.id.cardHistory);
        cardSettings = findViewById(R.id.cardSettings);
        
        String userName = authViewModel.getCurrentUserName();
        tvWelcome.setText("Welcome, " + userName);
        
        switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            volunteerViewModel.setAvailability(currentUserId, isChecked);
            updateAvailabilityStatus(isChecked);
            android.location.Location loc = locationHelper != null ? locationHelper.getLastKnownLocation() : null;
            FcmTokenRegistrar.updateVolunteerAvailability(getApplicationContext(), currentUserId, isChecked, loc);
            if (isChecked) {
                showToast("You are now available");
            } else {
                showToast("You are now unavailable");
            }
        });
        
        cardViewEmergencies.setOnClickListener(v -> {
            Intent intent = new Intent(VolunteerDashboardActivity.this, VolunteerEmergencyListActivity.class);
            startActivity(intent);
        });
        
        cardMapView.setOnClickListener(v -> {
            Intent intent = new Intent(VolunteerDashboardActivity.this, VolunteerMapActivity.class);
            startActivity(intent);
        });
        
        // Statistics
        cardStats.setOnClickListener(v -> {
            Intent intent = new Intent(VolunteerDashboardActivity.this, VolunteerStatsActivity.class);
            startActivity(intent);
        });
        
        cardHistory.setOnClickListener(v -> {
            Intent intent = new Intent(VolunteerDashboardActivity.this, EmergencyHistoryActivity.class);
            startActivity(intent);
        });
        
        cardSettings.setOnClickListener(v -> {
            Intent intent = new Intent(VolunteerDashboardActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
    
    private void updateAvailabilityStatus(boolean isAvailable) {
        if (tvAvailabilityStatus != null) {
            tvAvailabilityStatus.setText(isAvailable ? "Available" : "Unavailable");
        }
        if (viewStatusIndicator != null) {
            if (isAvailable) {
                viewStatusIndicator.setBackground(ContextCompat.getDrawable(this, R.drawable.status_indicator_available));
            } else {
                viewStatusIndicator.setBackground(ContextCompat.getDrawable(this, R.drawable.status_indicator_unavailable));
            }
        }
    }
    
    private void loadAvailabilityStatus() {
        volunteerViewModel.getAvailability(currentUserId, isAvailable -> {
            switchAvailability.setChecked(isAvailable);
            updateAvailabilityStatus(isAvailable);
        });
    }
    
    private void setupObservers() {
        volunteerViewModel.getAvailabilityStatus().observe(this, isAvailable -> {
            switchAvailability.setChecked(isAvailable);
            updateAvailabilityStatus(isAvailable);
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Restart listening when activity resumes
        if (networkBroadcastHelper != null && networkHelper != null && networkHelper.isWifiConnected()) {
            if (!networkBroadcastHelper.isListening()) {
                networkBroadcastHelper.startListening();
            }
        }
        
        // Sync pending operations when coming back online
        if (networkHelper != null && networkHelper.isConnected()) {
            syncPendingOperations();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Keep listening in background (optional - can stop if needed)
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup
        if (networkBroadcastHelper != null) {
            networkBroadcastHelper.cleanup();
        }
    }
}

