package com.harmonycare.app.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.harmonycare.app.R;
import com.harmonycare.app.util.Constants;
import com.harmonycare.app.viewmodel.AuthViewModel;

/**
 * Splash Screen Activity - Shows app logo and navigates to appropriate screen
 */
public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2500; // 2.5 seconds
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToNextScreen();
        }, SPLASH_DELAY);
    }
    
    private void navigateToNextScreen() {
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        // Check if user is already logged in
        if (authViewModel.isLoggedIn()) {
            // User is logged in, navigate to appropriate dashboard
            String userRole = authViewModel.getCurrentUserRole();
            Intent intent;
            
            if (Constants.ROLE_ELDERLY.equals(userRole)) {
                intent = new Intent(SplashActivity.this, ElderlyDashboardActivity.class);
            } else if (Constants.ROLE_VOLUNTEER.equals(userRole)) {
                intent = new Intent(SplashActivity.this, VolunteerDashboardActivity.class);
            } else {
                // Invalid role, go to login
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            
            startActivity(intent);
        } else {
            // User not logged in, go to login screen
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        
        finish(); // Close splash screen
    }
}

