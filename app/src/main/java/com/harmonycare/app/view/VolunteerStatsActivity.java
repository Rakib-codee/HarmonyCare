package com.harmonycare.app.view;

import android.os.Bundle;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.harmonycare.app.R;
import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.repository.EmergencyRepository;
import com.harmonycare.app.util.AnalyticsHelper;
import com.harmonycare.app.viewmodel.AuthViewModel;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Volunteer Statistics Activity
 */
public class VolunteerStatsActivity extends BaseActivity {
    private TextView tvTotalHelped;
    private TextView tvSuccessRate;
    private TextView tvAvgResponseTime;
    private TextView tvTotalAccepted;
    
    private AuthViewModel authViewModel;
    private EmergencyRepository emergencyRepository;
    private AnalyticsHelper analyticsHelper;
    private int currentUserId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_stats);
        
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        emergencyRepository = new EmergencyRepository(this);
        analyticsHelper = new AnalyticsHelper(this);
        
        currentUserId = authViewModel.getCurrentUserId();
        if (currentUserId == -1) {
            finish();
            return;
        }
        
        initViews();
        loadStatistics();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload statistics when returning to this screen
        // This ensures stats are updated after completing emergencies
        if (currentUserId > 0) {
            loadStatistics();
        }
    }
    
    private void initViews() {
        tvTotalHelped = findViewById(R.id.tvTotalHelped);
        tvSuccessRate = findViewById(R.id.tvSuccessRate);
        tvAvgResponseTime = findViewById(R.id.tvAvgResponseTime);
        tvTotalAccepted = findViewById(R.id.tvTotalAccepted);
    }
    
    private void loadStatistics() {
        showProgressDialog("Loading statistics...");
        
        android.util.Log.d("VolunteerStats", "Loading stats for volunteer ID: " + currentUserId);
        
        emergencyRepository.getEmergenciesForStats(currentUserId, new EmergencyRepository.RepositoryCallback<List<Emergency>>() {
            @Override
            public void onSuccess(List<Emergency> emergencies) {
                hideProgressDialog();
                android.util.Log.d("VolunteerStats", "Loaded " + (emergencies != null ? emergencies.size() : 0) + " emergencies");
                if (emergencies != null && !emergencies.isEmpty()) {
                    for (Emergency e : emergencies) {
                        android.util.Log.d("VolunteerStats", "Emergency ID: " + e.getId() + 
                            ", Status: " + e.getStatus() + 
                            ", VolunteerId: " + e.getVolunteerId());
                    }
                }
                calculateAndDisplayStats(emergencies);
            }
            
            @Override
            public void onError(Exception error) {
                hideProgressDialog();
                android.util.Log.e("VolunteerStats", "Error loading statistics", error);
                showToast("Error loading statistics: " + error.getMessage());
            }
        });
    }
    
    private void calculateAndDisplayStats(List<Emergency> emergencies) {
        if (emergencies == null || emergencies.isEmpty()) {
            tvTotalHelped.setText("0");
            tvTotalAccepted.setText("0");
            tvSuccessRate.setText("0%");
            tvAvgResponseTime.setText("N/A");
            return;
        }
        
        int completedCount = 0;
        int acceptedCount = 0;
        long totalResponseTime = 0;
        int responseTimeCount = 0;
        
        for (Emergency emergency : emergencies) {
            String status = emergency.getStatus();
            
            if ("accepted".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
                acceptedCount++;
                
                // Calculate response time (time between creation and acceptance)
                // For now, we'll use timestamp difference as approximation
                // In a real app, you'd track acceptance timestamp separately
                if (emergency.getVolunteerId() != null && emergency.getVolunteerId() == currentUserId) {
                    // Approximate response time (this is simplified)
                    responseTimeCount++;
                }
            }
            
            if ("completed".equalsIgnoreCase(status)) {
                completedCount++;
            }
        }
        
        // Calculate success rate
        double successRate = 0;
        if (acceptedCount > 0) {
            successRate = (double) completedCount / acceptedCount * 100;
        }
        
        // Display statistics
        tvTotalHelped.setText(String.valueOf(completedCount));
        tvTotalAccepted.setText(String.valueOf(acceptedCount));
        
        DecimalFormat df = new DecimalFormat("#.#");
        tvSuccessRate.setText(df.format(successRate) + "%");
        
        // Average response time using AnalyticsHelper
        double avgResponseTime = analyticsHelper.getAverageResponseTime(currentUserId, emergencies);
        if (avgResponseTime > 0) {
            tvAvgResponseTime.setText(df.format(avgResponseTime) + " min");
        } else {
            tvAvgResponseTime.setText("N/A");
        }
        
        // Additional analytics (can be displayed in expanded stats view)
        String mostActivePeriod = analyticsHelper.getMostActivePeriod(emergencies);
        android.util.Log.d("VolunteerStats", "Most active period: " + mostActivePeriod);
    }
}

