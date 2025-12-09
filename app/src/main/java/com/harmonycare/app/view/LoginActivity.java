package com.harmonycare.app.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.harmonycare.app.R;
import com.harmonycare.app.util.ErrorHandler;
import com.harmonycare.app.util.ValidationHelper;
import com.harmonycare.app.viewmodel.AuthViewModel;

/**
 * Login Activity
 */
public class LoginActivity extends AppCompatActivity {
    private EditText etContact, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Check if already logged in
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        if (authViewModel.isLoggedIn()) {
            navigateToDashboard();
            return;
        }
        
        initViews();
        setupObservers();
    }
    
    private void initViews() {
        etContact = findViewById(R.id.etContact);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
        
        btnLogin.setOnClickListener(v -> performLogin());
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
    
    private void setupObservers() {
        authViewModel.getLoginResult().observe(this, success -> {
            hideLoading();
            if (success) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                navigateToDashboard();
            }
        });
        
        authViewModel.getErrorMessage().observe(this, error -> {
            hideLoading();
            if (error != null && !error.isEmpty()) {
                ErrorHandler.showErrorDialog(this, "Login Error", error);
            }
        });
    }
    
    private void performLogin() {
        String contact = etContact.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (contact.isEmpty() || password.isEmpty()) {
            ErrorHandler.showErrorDialog(this, "Validation Error", "Please fill all fields");
            return;
        }
        
        // Validate contact format
        ValidationHelper.ValidationResult contactResult = ValidationHelper.validateContact(contact);
        if (!contactResult.isValid()) {
            ErrorHandler.showErrorDialog(this, "Invalid Contact", contactResult.getMessage());
            return;
        }
        
        showLoading();
        authViewModel.login(contact, password);
    }
    
    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(false);
        }
    }
    
    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(true);
        }
    }
    
    private void navigateToDashboard() {
        String role = authViewModel.getCurrentUserRole();
        Intent intent;
        
        if ("elderly".equals(role)) {
            intent = new Intent(LoginActivity.this, ElderlyDashboardActivity.class);
        } else if ("volunteer".equals(role)) {
            intent = new Intent(LoginActivity.this, VolunteerDashboardActivity.class);
        } else {
            Toast.makeText(this, "Invalid role", Toast.LENGTH_SHORT).show();
            return;
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

