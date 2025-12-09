package com.harmonycare.app.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.harmonycare.app.R;
import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.model.User;
import com.harmonycare.app.data.repository.UserRepository;
import com.harmonycare.app.viewmodel.AuthViewModel;
import com.harmonycare.app.viewmodel.EmergencyViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Emergency History Activity
 */
public class EmergencyHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmptyState;
    private EmergencyAdapter adapter;
    private EmergencyViewModel emergencyViewModel;
    private AuthViewModel authViewModel;
    private UserRepository userRepository;
    private List<Emergency> emergencyList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_history);
        
        emergencyViewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userRepository = new UserRepository(this);
        
        initViews();
        loadHistory();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload history when returning to this screen
        // This ensures history is updated after accepting/completing emergencies
        loadHistory();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmergencyAdapter();
        recyclerView.setAdapter(adapter);
        
        // Setup pull-to-refresh
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadHistory();
                // Stop refreshing after a delay
                recyclerView.postDelayed(() -> {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            });
        }
    }
    
    private void loadHistory() {
        String role = authViewModel.getCurrentUserRole();
        int userId = authViewModel.getCurrentUserId();
        
        if ("elderly".equals(role)) {
            emergencyViewModel.loadEmergenciesByElderly(userId);
        } else {
            emergencyViewModel.loadEmergenciesByVolunteer(userId);
        }
        
        emergencyViewModel.getActiveEmergencies().observe(this, emergencies -> {
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
                // Stop refreshing if active
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
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
    
    private class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_emergency_history, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // Use getAdapterPosition() to get current position
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }
            
            Emergency emergency = emergencyList.get(adapterPosition);
            User user = userRepository.getUserFromCache(emergency.getElderlyId());
            
            if (user != null) {
                holder.tvName.setText(user.getName());
                holder.tvContact.setText(user.getContact());
            } else {
                holder.tvName.setText("Loading...");
                holder.tvContact.setText("");
                // Load user asynchronously
                final int finalPosition = adapterPosition;
                userRepository.getUserById(emergency.getElderlyId(), new UserRepository.RepositoryCallback<User>() {
                    @Override
                    public void onSuccess(User loadedUser) {
                        if (loadedUser != null && holder.getAdapterPosition() == finalPosition) {
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
            
            holder.tvStatus.setText(emergency.getStatus().toUpperCase());
            holder.tvLocation.setText(String.format(Locale.getDefault(), "%.6f, %.6f",
                    emergency.getLatitude(), emergency.getLongitude()));
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(emergency.getTimestamp())));
        }
        
        @Override
        public int getItemCount() {
            return emergencyList.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvContact, tvStatus, tvLocation, tvTime;
            
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvContact = itemView.findViewById(R.id.tvContact);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                tvTime = itemView.findViewById(R.id.tvTime);
            }
        }
    }
}

