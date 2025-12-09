package com.harmonycare.app.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.harmonycare.app.data.model.VolunteerStatus;
import com.harmonycare.app.data.repository.VolunteerStatusRepository;
import com.harmonycare.app.util.ErrorHandler;

/**
 * ViewModel for Volunteer operations
 */
public class VolunteerViewModel extends AndroidViewModel {
    private VolunteerStatusRepository volunteerStatusRepository;
    private MutableLiveData<Boolean> availabilityStatus = new MutableLiveData<>();
    
    public VolunteerViewModel(Application application) {
        super(application);
        volunteerStatusRepository = new VolunteerStatusRepository(application);
    }
    
    private MutableLiveData<Boolean> currentAvailability = new MutableLiveData<>();
    
    public void setAvailability(int volunteerId, boolean isAvailable) {
        volunteerStatusRepository.setVolunteerAvailability(volunteerId, isAvailable, 
                new VolunteerStatusRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                availabilityStatus.postValue(isAvailable);
                currentAvailability.postValue(isAvailable);
            }
            
            @Override
            public void onError(Exception error) {
                Log.e("VolunteerViewModel", "Error setting availability", error);
            }
        });
    }
    
    public void getAvailability(int volunteerId, AvailabilityCallback callback) {
        volunteerStatusRepository.getVolunteerStatus(volunteerId, 
                new VolunteerStatusRepository.RepositoryCallback<VolunteerStatus>() {
            @Override
            public void onSuccess(VolunteerStatus status) {
                boolean available = status != null && status.isAvailable();
                currentAvailability.postValue(available);
                if (callback != null) {
                    callback.onSuccess(available);
                }
            }
            
            @Override
            public void onError(Exception error) {
                Log.e("VolunteerViewModel", "Error getting availability", error);
                if (callback != null) {
                    callback.onSuccess(false);
                }
            }
        });
    }
    
    // Legacy method - use getAvailability with callback instead
    @Deprecated
    public boolean getAvailability(int volunteerId) {
        Boolean value = currentAvailability.getValue();
        return value != null && value;
    }
    
    /**
     * Callback for availability operations
     */
    public interface AvailabilityCallback {
        void onSuccess(boolean isAvailable);
    }
    
    public LiveData<Boolean> getAvailabilityStatus() {
        return availabilityStatus;
    }
}

