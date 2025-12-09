package com.harmonycare.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.harmonycare.app.data.database.AppDatabase;
import com.harmonycare.app.data.database.RatingDao;
import com.harmonycare.app.data.model.Rating;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for Rating data operations
 */
public class RatingRepository {
    private RatingDao ratingDao;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    public RatingRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.ratingDao = database.ratingDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void insertRating(Rating rating, RepositoryCallback<Long> callback) {
        final RepositoryCallback<Long> finalCallback = callback;
        executorService.execute(() -> {
            try {
                long id = ratingDao.insertRating(rating);
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
    
    public void getRatingByEmergency(int emergencyId, RepositoryCallback<Rating> callback) {
        final RepositoryCallback<Rating> finalCallback = callback;
        executorService.execute(() -> {
            try {
                Rating rating = ratingDao.getRatingByEmergency(emergencyId);
                if (finalCallback != null) {
                    final Rating finalRating = rating;
                    mainHandler.post(() -> finalCallback.onSuccess(finalRating));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public void getAverageRatingByVolunteer(int volunteerId, RepositoryCallback<Double> callback) {
        final RepositoryCallback<Double> finalCallback = callback;
        executorService.execute(() -> {
            try {
                Double average = ratingDao.getAverageRatingByVolunteer(volunteerId);
                if (finalCallback != null) {
                    final Double finalAverage = average != null ? average : 0.0;
                    mainHandler.post(() -> finalCallback.onSuccess(finalAverage));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }
}

