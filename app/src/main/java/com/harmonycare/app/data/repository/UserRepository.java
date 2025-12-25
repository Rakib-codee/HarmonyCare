package com.harmonycare.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.harmonycare.app.data.database.AppDatabase;
import com.harmonycare.app.data.database.UserDao;
import com.harmonycare.app.data.model.User;
import com.harmonycare.app.util.PasswordHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for User data operations
 */
public class UserRepository {
    private UserDao userDao;
    private ExecutorService executorService;
    private Handler mainHandler;
    private Map<Integer, User> userCache; // Cache for synchronous access in adapters
    
    public UserRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.userDao = database.userDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.userCache = new HashMap<>();
    }
    
    /**
     * Get user from cache (synchronous, for use in adapters)
     * Note: User must be loaded first using getUserById with callback
     */
    public User getUserFromCache(int id) {
        return userCache.get(id);
    }
    
    /**
     * Preload users into cache
     */
    public void preloadUsers(List<Integer> userIds, RepositoryCallback<Void> callback) {
        final RepositoryCallback<Void> finalCallback = callback;
        executorService.execute(() -> {
            try {
                for (Integer userId : userIds) {
                    User user = userDao.getUserById(userId);
                    if (user != null) {
                        userCache.put(userId, user);
                    }
                }
                if (finalCallback != null) {
                    mainHandler.post(() -> finalCallback.onSuccess(null));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public void registerUser(User user, RepositoryCallback<Long> callback) {
        final RepositoryCallback<Long> finalCallback = callback;
        executorService.execute(() -> {
            try {
                // Hash password before storing
                if (user.getPassword() != null && !user.getPassword().contains(":")) {
                    String hashedPassword = PasswordHelper.hashPassword(user.getPassword());
                    user.setPassword(hashedPassword);
                }
                long id = userDao.insertUser(user);
                if (finalCallback != null) {
                    final long finalId = id;
                    mainHandler.post(() -> finalCallback.onSuccess(finalId));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public void login(String contact, String password, RepositoryCallback<User> callback) {
        final RepositoryCallback<User> finalCallback = callback;
        final String finalContact = contact;
        final String finalPassword = password;
        executorService.execute(() -> {
            try {
                User user = userDao.getUserByContact(finalContact);
                final User finalUser;
                if (user != null && PasswordHelper.verifyPassword(finalPassword, user.getPassword())) {
                    finalUser = user;
                } else {
                    finalUser = null;
                }
                if (finalCallback != null) {
                    mainHandler.post(() -> finalCallback.onSuccess(finalUser));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public void getUserById(int id, RepositoryCallback<User> callback) {
        final RepositoryCallback<User> finalCallback = callback;
        executorService.execute(() -> {
            try {
                User user = userDao.getUserById(id);
                if (user != null) {
                    // Update cache
                    userCache.put(id, user);
                }
                final User finalUser = user;
                if (finalCallback != null) {
                    mainHandler.post(() -> finalCallback.onSuccess(finalUser));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public void getUserByContact(String contact, RepositoryCallback<User> callback) {
        final RepositoryCallback<User> finalCallback = callback;
        executorService.execute(() -> {
            try {
                User user = userDao.getUserByContact(contact);
                final User finalUser = user;
                if (finalCallback != null) {
                    mainHandler.post(() -> finalCallback.onSuccess(finalUser));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public void getUsersByRole(String role, RepositoryCallback<List<User>> callback) {
        final RepositoryCallback<List<User>> finalCallback = callback;
        executorService.execute(() -> {
            try {
                List<User> users = userDao.getUsersByRole(role);
                final List<User> finalUsers = users;
                if (finalCallback != null) {
                    mainHandler.post(() -> finalCallback.onSuccess(finalUsers));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public void updateUser(User user, RepositoryCallback<Void> callback) {
        final RepositoryCallback<Void> finalCallback = callback;
        executorService.execute(() -> {
            try {
                userDao.updateUser(user);
                if (user != null) {
                    userCache.put(user.getId(), user);
                }
                if (finalCallback != null) {
                    mainHandler.post(() -> finalCallback.onSuccess(null));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    /**
     * Callback interface for repository operations
     */
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }
}

