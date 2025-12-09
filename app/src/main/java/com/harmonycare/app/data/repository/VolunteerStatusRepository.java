package com.harmonycare.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.harmonycare.app.data.database.AppDatabase;
import com.harmonycare.app.data.database.VolunteerStatusDao;
import com.harmonycare.app.data.model.VolunteerStatus;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for VolunteerStatus data operations
 */
public class VolunteerStatusRepository {
    private VolunteerStatusDao volunteerStatusDao;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    public VolunteerStatusRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.volunteerStatusDao = database.volunteerStatusDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void setVolunteerAvailability(int volunteerId, boolean isAvailable, RepositoryCallback<Void> callback) {
        final RepositoryCallback<Void> finalCallback = callback;
        executorService.execute(() -> {
            try {
                VolunteerStatus status = new VolunteerStatus(volunteerId, isAvailable);
                volunteerStatusDao.insertOrUpdateVolunteerStatus(status);
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
    
    public void getVolunteerStatus(int volunteerId, RepositoryCallback<VolunteerStatus> callback) {
        executorService.execute(() -> {
            try {
                VolunteerStatus status = volunteerStatusDao.getVolunteerStatus(volunteerId);
                final VolunteerStatus finalStatus;
                if (status == null) {
                    // Default to unavailable if not set
                    finalStatus = new VolunteerStatus(volunteerId, false);
                } else {
                    finalStatus = status;
                }
                if (callback != null) {
                    final RepositoryCallback<VolunteerStatus> finalCallback = callback;
                    mainHandler.post(() -> finalCallback.onSuccess(finalStatus));
                }
            } catch (Exception e) {
                if (callback != null) {
                    final RepositoryCallback<VolunteerStatus> finalCallback = callback;
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public void getAvailableVolunteers(RepositoryCallback<List<VolunteerStatus>> callback) {
        final RepositoryCallback<List<VolunteerStatus>> finalCallback = callback;
        executorService.execute(() -> {
            try {
                List<VolunteerStatus> volunteers = volunteerStatusDao.getAvailableVolunteers();
                final List<VolunteerStatus> finalVolunteers = volunteers;
                if (finalCallback != null) {
                    mainHandler.post(() -> finalCallback.onSuccess(finalVolunteers));
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

