package com.harmonycare.app.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.model.User;
import com.harmonycare.app.data.repository.EmergencyRepository;
import com.harmonycare.app.data.repository.UserRepository;
import com.harmonycare.app.util.ErrorHandler;
import com.harmonycare.app.util.FamilyNotificationHelper;
import com.harmonycare.app.util.NotificationHelper;
import com.harmonycare.app.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for Emergency operations
 */
public class EmergencyViewModel extends AndroidViewModel {
    private EmergencyRepository emergencyRepository;
    private MutableLiveData<List<Emergency>> activeEmergencies = new MutableLiveData<>();
    private MutableLiveData<Boolean> emergencyCreated = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public EmergencyViewModel(Application application) {
        super(application);
        emergencyRepository = new EmergencyRepository(application);
    }
    
    public void createEmergency(int elderlyId, double latitude, double longitude) {
        Emergency emergency = new Emergency(elderlyId, latitude, longitude, "active");
        emergencyRepository.createEmergency(emergency, new EmergencyRepository.RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long emergencyId) {
                if (emergencyId != null && emergencyId > 0) {
                    emergencyCreated.postValue(true);
                    
                    // Notify available volunteers about the new emergency
                    notifyVolunteersAboutEmergency(elderlyId);
                    
                    // Notify emergency contacts (family)
                    FamilyNotificationHelper familyNotificationHelper = new FamilyNotificationHelper(getApplication());
                    familyNotificationHelper.notifyEmergencyContacts(elderlyId, emergency);
                } else {
                    errorMessage.postValue("Failed to create emergency");
                    emergencyCreated.postValue(false);
                }
            }
            
            @Override
            public void onError(Exception error) {
                Log.e("EmergencyViewModel", "Error creating emergency", error);
                errorMessage.postValue(ErrorHandler.getErrorMessage(error));
                emergencyCreated.postValue(false);
            }
        });
    }
    
    /**
     * Notify all available volunteers about a new emergency
     */
    private void notifyVolunteersAboutEmergency(int elderlyId) {
        UserRepository userRepository = new UserRepository(getApplication());
        NotificationHelper notificationHelper = new NotificationHelper(getApplication());
        
        // Get elderly user details
        userRepository.getUserById(elderlyId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User elderlyUser) {
                if (elderlyUser != null) {
                    String elderlyName = elderlyUser.getName();
                    // For now, don't calculate distance (can be enhanced later)
                    notificationHelper.notifyAvailableVolunteers(elderlyName, null);
                }
            }
            
            @Override
            public void onError(Exception error) {
                // If we can't get user details, still try to notify with generic message
                notificationHelper.notifyAvailableVolunteers("An elderly person", null);
            }
        });
    }
    
    public void loadActiveEmergencies() {
        emergencyRepository.getActiveEmergencies(new EmergencyRepository.RepositoryCallback<List<Emergency>>() {
            @Override
            public void onSuccess(List<Emergency> emergencies) {
                activeEmergencies.postValue(emergencies != null ? emergencies : new ArrayList<>());
            }
            
            @Override
            public void onError(Exception error) {
                Log.e("EmergencyViewModel", "Error loading active emergencies", error);
                errorMessage.postValue(ErrorHandler.getErrorMessage(error));
                activeEmergencies.postValue(new ArrayList<>());
            }
        });
    }
    
    /**
     * Load active and accepted emergencies for volunteers
     * This shows both new emergencies (active) and emergencies they accepted (accepted)
     */
    public void loadActiveAndAcceptedEmergencies() {
        emergencyRepository.getActiveAndAcceptedEmergencies(new EmergencyRepository.RepositoryCallback<List<Emergency>>() {
            @Override
            public void onSuccess(List<Emergency> emergencies) {
                activeEmergencies.postValue(emergencies != null ? emergencies : new ArrayList<>());
            }
            
            @Override
            public void onError(Exception error) {
                Log.e("EmergencyViewModel", "Error loading active and accepted emergencies", error);
                errorMessage.postValue(ErrorHandler.getErrorMessage(error));
                activeEmergencies.postValue(new ArrayList<>());
            }
        });
    }
    
    public void loadEmergenciesByElderly(int elderlyId) {
        emergencyRepository.getEmergenciesByElderly(elderlyId, new EmergencyRepository.RepositoryCallback<List<Emergency>>() {
            @Override
            public void onSuccess(List<Emergency> emergencies) {
                activeEmergencies.postValue(emergencies != null ? emergencies : new ArrayList<>());
            }
            
            @Override
            public void onError(Exception error) {
                Log.e("EmergencyViewModel", "Error loading emergencies by elderly", error);
                errorMessage.postValue(ErrorHandler.getErrorMessage(error));
                activeEmergencies.postValue(new ArrayList<>());
            }
        });
    }
    
    public void loadEmergenciesByVolunteer(int volunteerId) {
        emergencyRepository.getEmergenciesByVolunteer(volunteerId, new EmergencyRepository.RepositoryCallback<List<Emergency>>() {
            @Override
            public void onSuccess(List<Emergency> emergencies) {
                activeEmergencies.postValue(emergencies != null ? emergencies : new ArrayList<>());
            }
            
            @Override
            public void onError(Exception error) {
                Log.e("EmergencyViewModel", "Error loading emergencies by volunteer", error);
                errorMessage.postValue(ErrorHandler.getErrorMessage(error));
                activeEmergencies.postValue(new ArrayList<>());
            }
        });
    }
    
    public void acceptEmergency(int emergencyId, int volunteerId) {
        emergencyRepository.getEmergencyById(emergencyId, new EmergencyRepository.RepositoryCallback<Emergency>() {
            @Override
            public void onSuccess(Emergency emergency) {
                if (emergency != null) {
                    emergency.setStatus(Constants.STATUS_ACCEPTED);
                    emergency.setVolunteerId(volunteerId);
                    emergencyRepository.updateEmergency(emergency, new EmergencyRepository.RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            // Reload active emergencies for the list
                            loadActiveEmergencies();
                            
                            // Also reload volunteer's emergencies for history/stats
                            loadEmergenciesByVolunteer(volunteerId);
                            
                            // Notify elderly that volunteer accepted
                            notifyElderlyEmergencyAccepted(emergency, volunteerId);
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            Log.e("EmergencyViewModel", "Error updating emergency", error);
                            errorMessage.postValue(ErrorHandler.getErrorMessage(error));
                        }
                    });
                } else {
                    errorMessage.postValue("Emergency not found");
                }
            }
            
            @Override
            public void onError(Exception error) {
                Log.e("EmergencyViewModel", "Error accepting emergency", error);
                errorMessage.postValue(ErrorHandler.getErrorMessage(error));
            }
        });
    }
    
    /**
     * Notify elderly when volunteer accepts their emergency
     */
    private void notifyElderlyEmergencyAccepted(Emergency emergency, int volunteerId) {
        UserRepository userRepository = new UserRepository(getApplication());
        NotificationHelper notificationHelper = new NotificationHelper(getApplication());
        
        // Get volunteer name
        userRepository.getUserById(volunteerId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User volunteer) {
                if (volunteer != null) {
                    notificationHelper.notifyElderlyEmergencyAccepted(
                            emergency.getId(),
                            volunteer.getName()
                    );
                }
            }
            
            @Override
            public void onError(Exception error) {
                // Silently fail
            }
        });
    }
    
    public void completeEmergency(int emergencyId) {
        emergencyRepository.getEmergencyById(emergencyId, new EmergencyRepository.RepositoryCallback<Emergency>() {
            @Override
            public void onSuccess(Emergency emergency) {
                if (emergency != null) {
                    // Ensure volunteerId is set (should already be set from accept, but ensure it)
                    if (emergency.getVolunteerId() == null) {
                        Log.w("EmergencyViewModel", "Emergency completed but volunteerId is null");
                        errorMessage.postValue("Cannot complete: Volunteer not assigned");
                        return;
                    }
                    
                    emergency.setStatus("completed");
                    // Preserve volunteerId when completing
                    emergencyRepository.updateEmergency(emergency, new EmergencyRepository.RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            // Reload active emergencies for the list
                            loadActiveEmergencies();
                            
                            // Also reload volunteer's emergencies for history/stats
                            if (emergency.getVolunteerId() != null) {
                                loadEmergenciesByVolunteer(emergency.getVolunteerId());
                            }
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            Log.e("EmergencyViewModel", "Error updating emergency", error);
                            errorMessage.postValue(ErrorHandler.getErrorMessage(error));
                        }
                    });
                } else {
                    errorMessage.postValue("Emergency not found");
                }
            }
            
            @Override
            public void onError(Exception error) {
                Log.e("EmergencyViewModel", "Error completing emergency", error);
                errorMessage.postValue(ErrorHandler.getErrorMessage(error));
            }
        });
    }
    
    public void cancelEmergency(int emergencyId) {
        emergencyRepository.getEmergencyById(emergencyId, new EmergencyRepository.RepositoryCallback<Emergency>() {
            @Override
            public void onSuccess(Emergency emergency) {
                if (emergency != null) {
                    emergency.setStatus("cancelled");
                    emergencyRepository.updateEmergency(emergency, new EmergencyRepository.RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            loadActiveEmergencies();
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            Log.e("EmergencyViewModel", "Error updating emergency", error);
                            errorMessage.postValue(ErrorHandler.getErrorMessage(error));
                        }
                    });
                } else {
                    errorMessage.postValue("Emergency not found");
                }
            }
            
            @Override
            public void onError(Exception error) {
                Log.e("EmergencyViewModel", "Error cancelling emergency", error);
                errorMessage.postValue(ErrorHandler.getErrorMessage(error));
            }
        });
    }
    
    public void getEmergencyById(int id, EmergencyCallback callback) {
        emergencyRepository.getEmergencyById(id, new EmergencyRepository.RepositoryCallback<Emergency>() {
            @Override
            public void onSuccess(Emergency emergency) {
                if (callback != null) {
                    callback.onSuccess(emergency);
                }
            }
            
            @Override
            public void onError(Exception error) {
                Log.e("EmergencyViewModel", "Error getting emergency by id", error);
                errorMessage.postValue(ErrorHandler.getErrorMessage(error));
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }
        });
    }
    
    public void getActiveEmergencyByElderly(int elderlyId, EmergencyCallback callback) {
        emergencyRepository.getActiveEmergencyByElderly(elderlyId, new EmergencyRepository.RepositoryCallback<Emergency>() {
            @Override
            public void onSuccess(Emergency emergency) {
                if (callback != null) {
                    callback.onSuccess(emergency);
                }
            }
            
            @Override
            public void onError(Exception error) {
                Log.e("EmergencyViewModel", "Error getting active emergency", error);
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }
        });
    }
    
    public LiveData<List<Emergency>> getActiveEmergencies() {
        return activeEmergencies;
    }
    
    public LiveData<Boolean> getEmergencyCreated() {
        return emergencyCreated;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Callback for emergency operations
     */
    public interface EmergencyCallback {
        void onSuccess(Emergency emergency);
    }
}
