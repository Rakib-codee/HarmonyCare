package com.harmonycare.app.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.harmonycare.app.R;
import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.model.User;
import com.harmonycare.app.data.repository.UserRepository;
import com.harmonycare.app.util.DistanceCalculator;
import com.harmonycare.app.util.LocationHelper;
import com.harmonycare.app.util.LocalNetworkBroadcastHelper;
import com.harmonycare.app.util.NetworkHelper;
import com.harmonycare.app.util.Constants;
import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.viewmodel.AuthViewModel;
import com.harmonycare.app.viewmodel.EmergencyViewModel;

import android.os.Handler;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Volunteer Emergency List Activity
 */
public class VolunteerEmergencyListActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmptyState;
    private EmergencyAdapter adapter;
    private EmergencyViewModel emergencyViewModel;
    private AuthViewModel authViewModel;
    private UserRepository userRepository;
    private LocationHelper locationHelper;
    private Location currentLocation;
    private List<Emergency> emergencyList = new ArrayList<>();
    private LocalNetworkBroadcastHelper networkBroadcastHelper;
    private NetworkHelper networkHelper;
    private Handler pollHandler;
    private Runnable pollRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_emergency_list);
        
        emergencyViewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userRepository = new UserRepository(this);
        locationHelper = new LocationHelper(this);
        
        initViews();
        checkLocationPermission();
        loadEmergencies();
        setupNetworkBroadcast();
        startPollingForEmergencies();
    }
    
    private void setupNetworkBroadcast() {
        networkHelper = new NetworkHelper(this);
        networkBroadcastHelper = new LocalNetworkBroadcastHelper(this);
        
        networkBroadcastHelper.setListener(new LocalNetworkBroadcastHelper.EmergencyListener() {
            @Override
            public void onEmergencyReceived(Emergency emergency) {
                // Reload emergencies when new one received
                loadEmergencies();
            }
        });
        
        if (networkHelper.isWifiConnected()) {
            networkBroadcastHelper.startListening();
        }
    }
    
    /**
     * Start polling for new emergencies from API (if enabled)
     */
    private void startPollingForEmergencies() {
        if (!Constants.API_ENABLED) {
            return; // API disabled, no polling needed
        }
        
        pollHandler = new Handler(Looper.getMainLooper());
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                // Only poll if online
                if (networkHelper != null && networkHelper.isConnected()) {
                    loadEmergencies(); // This will use API if available
                }
                
                // Schedule next poll
                if (pollHandler != null && pollRunnable != null) {
                    pollHandler.postDelayed(this, Constants.API_POLL_INTERVAL);
                }
            }
        };
        
        // Start polling after initial delay
        pollHandler.postDelayed(pollRunnable, Constants.API_POLL_INTERVAL);
    }
    
    /**
     * Stop polling for emergencies
     */
    private void stopPollingForEmergencies() {
        if (pollHandler != null && pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload emergencies when returning from chat or details screen
        // This ensures the list is updated (e.g., if emergency was completed)
        loadEmergencies();
        
        // Restart listening if on WiFi
        if (networkBroadcastHelper != null && networkHelper != null && networkHelper.isWifiConnected()) {
            if (!networkBroadcastHelper.isListening()) {
                networkBroadcastHelper.startListening();
            }
        }
        
        // Restart polling when activity resumes
        startPollingForEmergencies();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmergencyAdapter();
        recyclerView.setAdapter(adapter);
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadEmergencies();
            getCurrentLocation();
        });
    }
    
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }
    
    private void getCurrentLocation() {
        if (locationHelper.hasLocationPermission()) {
            currentLocation = locationHelper.getLastKnownLocation();
            if (currentLocation == null) {
                locationHelper.requestLocationUpdates(location -> {
                    currentLocation = location;
                    adapter.notifyDataSetChanged();
                    locationHelper.stopLocationUpdates();
                });
            }
        }
    }
    
    private void loadEmergencies() {
        // Load both active and accepted emergencies for volunteers
        // This allows volunteers to see new emergencies and complete their accepted ones
        emergencyViewModel.loadActiveAndAcceptedEmergencies();
        emergencyViewModel.getActiveEmergencies().observe(this, emergencies -> {
            swipeRefreshLayout.setRefreshing(false);
            if (emergencies != null) {
                emergencyList = emergencies;
                // Preload users for the adapter
                List<Integer> userIds = new java.util.ArrayList<>();
                for (Emergency emergency : emergencies) {
                    userIds.add(emergency.getElderlyId());
                }
                if (!userIds.isEmpty()) {
                    userRepository.preloadUsers(userIds, null);
                }
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }
        });
    }
    
    private void updateEmptyState() {
        if (emergencyList.isEmpty()) {
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.VISIBLE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
            }
        } else {
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.GONE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void acceptEmergency(int emergencyId) {
        int volunteerId = authViewModel.getCurrentUserId();
        emergencyViewModel.acceptEmergency(emergencyId, volunteerId);
        Toast.makeText(this, "Emergency accepted", Toast.LENGTH_SHORT).show();
        loadEmergencies();
    }
    
    private class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_volunteer_emergency, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Emergency emergency = emergencyList.get(position);
            User user = userRepository.getUserFromCache(emergency.getElderlyId());
            
            if (user != null) {
                holder.tvName.setText(user.getName());
                holder.tvContact.setText(user.getContact());
            } else {
                holder.tvName.setText("Loading...");
                holder.tvContact.setText("");
                // Load user asynchronously
                userRepository.getUserById(emergency.getElderlyId(), new UserRepository.RepositoryCallback<User>() {
                    @Override
                    public void onSuccess(User loadedUser) {
                        if (loadedUser != null && holder.getAdapterPosition() == position) {
                            holder.tvName.setText(loadedUser.getName());
                            holder.tvContact.setText(loadedUser.getContact());
                        }
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        // Ignore error, keep "Loading..." text
                    }
                });
            }
            
            // Calculate distance
            if (currentLocation != null) {
                double distance = DistanceCalculator.calculateDistance(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude(),
                        emergency.getLatitude(),
                        emergency.getLongitude()
                );
                holder.tvDistance.setText(DistanceCalculator.formatDistance(distance));
            } else {
                holder.tvDistance.setText("Calculating...");
            }
            
            // Set status badge
            String status = emergency.getStatus().toUpperCase();
            holder.tvStatusBadge.setText(status);
            if ("active".equalsIgnoreCase(emergency.getStatus())) {
                holder.tvStatusBadge.setBackgroundResource(R.drawable.status_badge_active);
                // For active emergencies, show "Accept" button
                holder.btnAccept.setText("Accept");
                holder.btnAccept.setEnabled(true);
                holder.btnAccept.setOnClickListener(v -> {
                    acceptEmergency(emergency.getId());
                    // Navigate to details after accepting
                    Intent intent = new Intent(VolunteerEmergencyListActivity.this, EmergencyDetailsActivity.class);
                    intent.putExtra("emergency_id", emergency.getId());
                    startActivity(intent);
                });
            } else if ("accepted".equalsIgnoreCase(emergency.getStatus())) {
                holder.tvStatusBadge.setBackgroundResource(R.drawable.status_badge_accepted);
                // For accepted emergencies, show "View Details" button to complete
                holder.btnAccept.setText("View Details");
                holder.btnAccept.setEnabled(true);
                holder.btnAccept.setOnClickListener(v -> {
                    // Navigate directly to details (already accepted)
                    Intent intent = new Intent(VolunteerEmergencyListActivity.this, EmergencyDetailsActivity.class);
                    intent.putExtra("emergency_id", emergency.getId());
                    startActivity(intent);
                });
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(emergency.getTimestamp())));
        }
        
        @Override
        public int getItemCount() {
            return emergencyList.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvContact, tvDistance, tvTime, tvStatusBadge;
            Button btnAccept;
            
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvContact = itemView.findViewById(R.id.tvContact);
                tvDistance = itemView.findViewById(R.id.tvDistance);
                tvTime = itemView.findViewById(R.id.tvTime);
                tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
                btnAccept = itemView.findViewById(R.id.btnAccept);
            }
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Stop polling when activity is paused to save battery
        stopPollingForEmergencies();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPollingForEmergencies();
        if (locationHelper != null) {
            locationHelper.stopLocationUpdates();
        }
        if (networkBroadcastHelper != null) {
            networkBroadcastHelper.cleanup();
        }
    }
}

