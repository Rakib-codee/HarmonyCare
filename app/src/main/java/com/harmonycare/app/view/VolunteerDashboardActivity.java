package com.harmonycare.app.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.harmonycare.app.R;
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
}

