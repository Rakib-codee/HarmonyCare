package com.harmonycare.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.harmonycare.app.data.database.AppDatabase;
import com.harmonycare.app.data.database.ReminderDao;
import com.harmonycare.app.data.model.Reminder;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for Reminder data operations
 */
public class ReminderRepository {
    private ReminderDao reminderDao;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    public ReminderRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.reminderDao = database.reminderDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void insertReminder(Reminder reminder, RepositoryCallback<Long> callback) {
        final RepositoryCallback<Long> finalCallback = callback;
        executorService.execute(() -> {
            try {
                long id = reminderDao.insertReminder(reminder);
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
    
    public void getActiveRemindersByUser(int userId, RepositoryCallback<List<Reminder>> callback) {
        final RepositoryCallback<List<Reminder>> finalCallback = callback;
        executorService.execute(() -> {
            try {
                List<Reminder> reminders = reminderDao.getActiveRemindersByUser(userId);
                if (finalCallback != null) {
                    final List<Reminder> finalReminders = reminders != null ? reminders : new java.util.ArrayList<>();
                    mainHandler.post(() -> finalCallback.onSuccess(finalReminders));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public void updateReminder(Reminder reminder, RepositoryCallback<Void> callback) {
        final RepositoryCallback<Void> finalCallback = callback;
        executorService.execute(() -> {
            try {
                reminderDao.updateReminder(reminder);
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
    
    public void deleteReminder(Reminder reminder, RepositoryCallback<Void> callback) {
        final RepositoryCallback<Void> finalCallback = callback;
        executorService.execute(() -> {
            try {
                reminderDao.deleteReminder(reminder);
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
    
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }
}

