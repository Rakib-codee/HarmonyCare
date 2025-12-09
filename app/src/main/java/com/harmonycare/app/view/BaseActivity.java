package com.harmonycare.app.view;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.harmonycare.app.R;
import com.harmonycare.app.util.Constants;

/**
 * Base Activity with common functionality
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Request notification permission for Android 13+
        requestNotificationPermissionIfNeeded();
    }
    
    /**
     * Request notification permission for Android 13+ (API 33+)
     */
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                android.util.Log.d("BaseActivity", "Notification permission granted");
            } else {
                // Permission denied
                android.util.Log.w("BaseActivity", "Notification permission denied");
                // Show a message that notifications won't work
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                    Toast.makeText(this, "Notification permission is required for emergency alerts", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    
    /**
     * Show progress dialog
     * @param message Message to display
     */
    protected void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }
    
    /**
     * Hide progress dialog
     */
    protected void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    
    /**
     * Show progress bar
     * @param progressBar ProgressBar view
     */
    protected void showProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Hide progress bar
     */
    protected void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show toast message
     * @param message Message to show
     */
    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Show long toast message
     * @param message Message to show
     */
    protected void showLongToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }
}

