package com.harmonycare.app.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

/**
 * ViewModel for Elderly-specific operations
 * Currently extends EmergencyViewModel functionality
 */
public class ElderlyViewModel extends AndroidViewModel {
    
    public ElderlyViewModel(Application application) {
        super(application);
    }
    
    // Elderly-specific operations can be added here
    // Currently, emergency operations are handled by EmergencyViewModel
}

