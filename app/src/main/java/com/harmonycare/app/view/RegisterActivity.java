package com.harmonycare.app.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.harmonycare.app.R;
import com.harmonycare.app.util.ErrorHandler;
import com.harmonycare.app.util.PasswordHelper;
import com.harmonycare.app.util.ValidationHelper;
import com.harmonycare.app.viewmodel.AuthViewModel;

/**
 * Register Activity
 */
public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etContact, etPassword;
    private RadioGroup rgRole;
    private RadioButton rbElderly, rbVolunteer;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        initViews();
        setupObservers();
    }
    
    private void initViews() {
        etName = findViewById(R.id.etName);
        etContact = findViewById(R.id.etContact);
        etPassword = findViewById(R.id.etPassword);
        rgRole = findViewById(R.id.rgRole);
        rbElderly = findViewById(R.id.rbElderly);
        rbVolunteer = findViewById(R.id.rbVolunteer);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
        
        btnRegister.setOnClickListener(v -> performRegister());
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    private void setupObservers() {
        authViewModel.getRegisterResult().observe(this, success -> {
            hideLoading();
            if (success) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                navigateToDashboard();
            }
        });
        
        authViewModel.getErrorMessage().observe(this, error -> {
            hideLoading();
            if (error != null && !error.isEmpty()) {
                ErrorHandler.showErrorDialog(this, "Registration Error", error);
            }
        });
    }
    
    private void performRegister() {
        String name = etName.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        int selectedRoleId = rgRole.getCheckedRadioButtonId();
        
        // Validate name
        if (!ValidationHelper.isValidName(name)) {
            ErrorHandler.showErrorDialog(this, "Invalid Name", 
                    "Name must be 2-50 characters and contain only letters and spaces");
            return;
        }
        
        // Validate contact
        ValidationHelper.ValidationResult contactResult = ValidationHelper.validateContact(contact);
        if (!contactResult.isValid()) {
            ErrorHandler.showErrorDialog(this, "Invalid Contact", contactResult.getMessage());
            return;
        }
        
        // Validate password
        ValidationHelper.ValidationResult passwordResult = ValidationHelper.validatePassword(password);
        if (!passwordResult.isValid()) {
            ErrorHandler.showErrorDialog(this, "Invalid Password", passwordResult.getMessage());
            return;
        }
        
        // Show password strength
        int strength = PasswordHelper.getPasswordStrength(password);
        String strengthText = PasswordHelper.getPasswordStrengthText(password);
        if (strength < 1) {
            ErrorHandler.showConfirmationDialog(this, "Weak Password", 
                    "Your password is weak. Consider using a stronger password. Continue anyway?",
                    (dialog, which) -> {
                        String role = selectedRoleId == rbElderly.getId() ? "elderly" : "volunteer";
                        authViewModel.register(name, contact, role, password);
                    });
            return;
        }
        
        if (selectedRoleId == -1) {
            ErrorHandler.showErrorDialog(this, "Role Required", "Please select a role");
            return;
        }
        
        String role = selectedRoleId == rbElderly.getId() ? "elderly" : "volunteer";
        showLoading();
        authViewModel.register(name, contact, role, password);
    }
    
    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (btnRegister != null) {
            btnRegister.setEnabled(false);
        }
    }
    
    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (btnRegister != null) {
            btnRegister.setEnabled(true);
        }
    }
    
    private void navigateToDashboard() {
        String role = authViewModel.getCurrentUserRole();
        Intent intent;
        
        if ("elderly".equals(role)) {
            intent = new Intent(RegisterActivity.this, ElderlyDashboardActivity.class);
        } else {
            intent = new Intent(RegisterActivity.this, VolunteerDashboardActivity.class);
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

