package com.harmonycare.app.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.harmonycare.app.R;
import com.harmonycare.app.data.model.User;
import com.harmonycare.app.data.repository.UserRepository;
import com.harmonycare.app.util.Constants;
import com.harmonycare.app.util.ErrorHandler;
import com.harmonycare.app.util.ValidationHelper;
import com.harmonycare.app.viewmodel.AuthViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Profile Activity for managing user profile
 */
public class ProfileActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    
    private EditText etName, etContact, etAddress, etMedicalInfo;
    private Button btnSave, btnChangePassword, btnChangePhoto;
    private TextView tvRole;
    private ImageView ivProfilePhoto;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;
    private UserRepository userRepository;
    private User currentUser;
    private String currentPhotoPath;
    private Uri photoUri;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userRepository = new UserRepository(this);
        
        initViews();
        loadUserProfile();
    }
    
    private void initViews() {
        etName = findViewById(R.id.etName);
        etContact = findViewById(R.id.etContact);
        etAddress = findViewById(R.id.etAddress);
        etMedicalInfo = findViewById(R.id.etMedicalInfo);
        tvRole = findViewById(R.id.tvRole);
        btnSave = findViewById(R.id.btnSave);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        progressBar = findViewById(R.id.progressBar);
        
        btnSave.setOnClickListener(v -> saveProfile());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnChangePhoto.setOnClickListener(v -> showPhotoOptionsDialog());
    }
    
    private void loadUserProfile() {
        int userId = authViewModel.getCurrentUserId();
        if (userId == -1) {
            finish();
            return;
        }
        
        showLoading();
        userRepository.getUserById(userId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                if (currentUser != null) {
                    etName.setText(currentUser.getName());
                    etContact.setText(currentUser.getContact());
                    etAddress.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "");
                    etMedicalInfo.setText(currentUser.getMedicalInfo() != null ? currentUser.getMedicalInfo() : "");
                    tvRole.setText("Role: " + currentUser.getRole().substring(0, 1).toUpperCase() + 
                            currentUser.getRole().substring(1));
                    
                    // Load profile photo if exists
                    if (currentUser.getPhotoPath() != null && !currentUser.getPhotoPath().isEmpty()) {
                        loadProfilePhoto(currentUser.getPhotoPath());
                    }
                }
                hideLoading();
            }
            
            @Override
            public void onError(Exception error) {
                hideLoading();
                ErrorHandler.showErrorDialog(ProfileActivity.this, "Error", "Failed to load profile");
            }
        });
    }
    
    private void loadProfilePhoto(String photoPath) {
        try {
            File photoFile = new File(photoPath);
            if (photoFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
                if (bitmap != null) {
                    ivProfilePhoto.setImageBitmap(bitmap);
                }
            }
        } catch (Exception e) {
            // Photo not found or error loading
        }
    }
    
    private void showPhotoOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Photo");
        builder.setItems(new String[]{"Camera", "Gallery"}, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else {
                openGallery();
            }
        });
        builder.show();
    }
    
    private void openCamera() {
        if (checkCameraPermission()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePictureIntent, REQUEST_CAMERA);
                }
            }
        }
    }
    
    private void openGallery() {
        if (checkStoragePermission()) {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, REQUEST_GALLERY);
        }
    }
    
    private File createImageFile() {
        String imageFileName = "profile_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            currentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException e) {
            return null;
        }
    }
    
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }
    
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1004);
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1005);
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1004 || requestCode == 1005) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                if (currentPhotoPath != null) {
                    loadProfilePhoto(currentPhotoPath);
                }
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null) {
                            // Save bitmap to file
                            File photoFile = createImageFile();
                            if (photoFile != null) {
                                FileOutputStream fos = new FileOutputStream(photoFile);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                                fos.close();
                                currentPhotoPath = photoFile.getAbsolutePath();
                                loadProfilePhoto(currentPhotoPath);
                            }
                        }
                    } catch (IOException e) {
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    
    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String medicalInfo = etMedicalInfo.getText().toString().trim();
        
        if (!ValidationHelper.isValidName(name)) {
            ErrorHandler.showErrorDialog(this, "Invalid Name", 
                    "Name must be 2-50 characters and contain only letters and spaces");
            return;
        }
        
        ValidationHelper.ValidationResult contactResult = ValidationHelper.validateContact(contact);
        if (!contactResult.isValid()) {
            ErrorHandler.showErrorDialog(this, "Invalid Contact", contactResult.getMessage());
            return;
        }
        
        if (currentUser != null) {
            currentUser.setName(name);
            currentUser.setContact(contact);
            currentUser.setAddress(address);
            currentUser.setMedicalInfo(medicalInfo);
            
            // Save photo path if photo was selected
            if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
                currentUser.setPhotoPath(currentPhotoPath);
            }
            
            showLoading();
            userRepository.updateUser(currentUser, new UserRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Update SharedPreferences name only (keep existing session)
                    android.content.SharedPreferences prefs = getSharedPreferences("HarmonyCarePrefs", MODE_PRIVATE);
                    prefs.edit().putString("user_name", name).apply();
                    hideLoading();
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
                
                @Override
                public void onError(Exception error) {
                    hideLoading();
                    ErrorHandler.showErrorDialog(ProfileActivity.this, "Error", "Failed to update profile");
                }
            });
        }
    }
    
    private void showChangePasswordDialog() {
        // Simple implementation - in production, use a proper dialog
        ErrorHandler.showErrorDialog(this, "Change Password", 
                "Password change feature will be available in the next update");
    }
    
    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (btnSave != null) {
            btnSave.setEnabled(false);
        }
    }
    
    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (btnSave != null) {
            btnSave.setEnabled(true);
        }
    }
}
