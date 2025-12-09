package com.harmonycare.app.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.harmonycare.app.data.model.User;
import com.harmonycare.app.data.repository.UserRepository;
import com.harmonycare.app.util.ErrorHandler;
import com.harmonycare.app.util.Constants;

/**
 * ViewModel for Authentication operations
 */
public class AuthViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private SharedPreferences sharedPreferences;
    
    private MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> registerResult = new MutableLiveData<>();
    
    public AuthViewModel(Application application) {
        super(application);
        try {
            userRepository = new UserRepository(application);
            sharedPreferences = application.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        } catch (Exception e) {
            Log.e("AuthViewModel", "Error initializing", e);
        }
    }
    
    public void login(String contact, String password) {
        userRepository.login(contact, password, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    saveLoginSession(user);
                    loginResult.postValue(true);
                } else {
                    errorMessage.postValue("Invalid contact or password");
                    loginResult.postValue(false);
                }
            }
            
            @Override
            public void onError(Exception error) {
                Log.e("AuthViewModel", "Error during login", error);
                errorMessage.postValue(ErrorHandler.getErrorMessage(error));
                loginResult.postValue(false);
            }
        });
    }
    
    public void register(String name, String contact, String role, String password) {
        // Check if user already exists
        userRepository.getUserByContact(contact, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User existingUser) {
                if (existingUser != null) {
                    errorMessage.postValue("Contact number already registered");
                    registerResult.postValue(false);
                } else {
                    // User doesn't exist, proceed with registration
                    User newUser = new User(name, contact, role, password);
                    userRepository.registerUser(newUser, new UserRepository.RepositoryCallback<Long>() {
                        @Override
                        public void onSuccess(Long userId) {
                            if (userId != null && userId > 0) {
                                newUser.setId(userId.intValue());
                                saveLoginSession(newUser);
                                registerResult.postValue(true);
                            } else {
                                errorMessage.postValue("Registration failed");
                                registerResult.postValue(false);
                            }
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            Log.e("AuthViewModel", "Error during registration", error);
                            errorMessage.postValue(ErrorHandler.getErrorMessage(error));
                            registerResult.postValue(false);
                        }
                    });
                }
            }
            
            @Override
            public void onError(Exception error) {
                Log.e("AuthViewModel", "Error checking existing user", error);
                errorMessage.postValue(ErrorHandler.getErrorMessage(error));
                registerResult.postValue(false);
            }
        });
    }
    
    private void saveLoginSession(User user) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(Constants.KEY_USER_ID, user.getId());
            editor.putString(Constants.KEY_USER_NAME, user.getName());
            editor.putString(Constants.KEY_USER_ROLE, user.getRole());
            editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
            editor.apply();
        } catch (Exception e) {
            Log.e("AuthViewModel", "Error saving session", e);
        }
    }
    
    public void logout() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        } catch (Exception e) {
            Log.e("AuthViewModel", "Error during logout", e);
        }
    }
    
    public boolean isLoggedIn() {
        try {
            return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
        } catch (Exception e) {
            Log.e("AuthViewModel", "Error checking login status", e);
            return false;
        }
    }
    
    public int getCurrentUserId() {
        try {
            return sharedPreferences.getInt(Constants.KEY_USER_ID, -1);
        } catch (Exception e) {
            Log.e("AuthViewModel", "Error getting user id", e);
            return -1;
        }
    }
    
    public String getCurrentUserName() {
        try {
            return sharedPreferences.getString(Constants.KEY_USER_NAME, "");
        } catch (Exception e) {
            Log.e("AuthViewModel", "Error getting user name", e);
            return "";
        }
    }
    
    public String getCurrentUserRole() {
        try {
            return sharedPreferences.getString(Constants.KEY_USER_ROLE, "");
        } catch (Exception e) {
            Log.e("AuthViewModel", "Error getting user role", e);
            return "";
        }
    }
    
    public LiveData<Boolean> getLoginResult() {
        return loginResult;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> getRegisterResult() {
        return registerResult;
    }
}

