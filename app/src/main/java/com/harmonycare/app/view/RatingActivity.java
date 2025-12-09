package com.harmonycare.app.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.harmonycare.app.R;
import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.model.Rating;
import com.harmonycare.app.data.repository.RatingRepository;
import com.harmonycare.app.util.ErrorHandler;
import com.harmonycare.app.viewmodel.AuthViewModel;
import com.harmonycare.app.viewmodel.EmergencyViewModel;

/**
 * Rating Activity for elderly to rate volunteers
 */
public class RatingActivity extends BaseActivity {
    private RatingBar ratingBar;
    private EditText etFeedback;
    private Button btnSubmit;
    private TextView tvVolunteerName;
    
    private int emergencyId;
    private int volunteerId;
    private int elderlyId;
    private EmergencyViewModel emergencyViewModel;
    private AuthViewModel authViewModel;
    private RatingRepository ratingRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        
        emergencyId = getIntent().getIntExtra("emergency_id", -1);
        if (emergencyId == -1) {
            showToast("Invalid emergency");
            finish();
            return;
        }
        
        emergencyViewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        ratingRepository = new RatingRepository(this);
        
        elderlyId = authViewModel.getCurrentUserId();
        if (elderlyId == -1) {
            finish();
            return;
        }
        
        initViews();
        loadEmergencyDetails();
        checkExistingRating();
    }
    
    private void initViews() {
        ratingBar = findViewById(R.id.ratingBar);
        etFeedback = findViewById(R.id.etFeedback);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvVolunteerName = findViewById(R.id.tvVolunteerName);
        
        btnSubmit.setOnClickListener(v -> submitRating());
    }
    
    private void loadEmergencyDetails() {
        emergencyViewModel.getEmergencyById(emergencyId, emergency -> {
            if (emergency != null) {
                volunteerId = emergency.getVolunteerId() != null ? emergency.getVolunteerId() : -1;
                if (volunteerId > 0) {
                    // Load volunteer name
                    com.harmonycare.app.data.repository.UserRepository userRepository = 
                            new com.harmonycare.app.data.repository.UserRepository(this);
                    userRepository.getUserById(volunteerId, new com.harmonycare.app.data.repository.UserRepository.RepositoryCallback<com.harmonycare.app.data.model.User>() {
                        @Override
                        public void onSuccess(com.harmonycare.app.data.model.User user) {
                            if (user != null && tvVolunteerName != null) {
                                tvVolunteerName.setText("Rate " + user.getName());
                            }
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            if (tvVolunteerName != null) {
                                tvVolunteerName.setText("Rate Volunteer");
                            }
                        }
                    });
                } else {
                    showToast("Volunteer not assigned");
                    finish();
                }
            } else {
                showToast("Emergency not found");
                finish();
            }
        });
    }
    
    private void checkExistingRating() {
        ratingRepository.getRatingByEmergency(emergencyId, new RatingRepository.RepositoryCallback<Rating>() {
            @Override
            public void onSuccess(Rating rating) {
                if (rating != null) {
                    // Already rated
                    ratingBar.setRating(rating.getRating());
                    ratingBar.setEnabled(false);
                    if (rating.getFeedback() != null) {
                        etFeedback.setText(rating.getFeedback());
                    }
                    etFeedback.setEnabled(false);
                    btnSubmit.setEnabled(false);
                    btnSubmit.setText("Already Rated");
                }
            }
            
            @Override
            public void onError(Exception error) {
                // No existing rating, allow new rating
            }
        });
    }
    
    private void submitRating() {
        int rating = (int) ratingBar.getRating();
        String feedback = etFeedback.getText().toString().trim();
        
        if (rating == 0) {
            ErrorHandler.showErrorDialog(this, "Invalid Rating", "Please select a rating");
            return;
        }
        
        if (volunteerId <= 0) {
            ErrorHandler.showErrorDialog(this, "Error", "Volunteer not found");
            return;
        }
        
        Rating newRating = new Rating(emergencyId, volunteerId, elderlyId, rating, feedback);
        ratingRepository.insertRating(newRating, new RatingRepository.RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long id) {
                showToast("Thank you for your feedback!");
                finish();
            }
            
            @Override
            public void onError(Exception error) {
                ErrorHandler.showErrorDialog(RatingActivity.this, "Error", "Failed to submit rating");
            }
        });
    }
}

