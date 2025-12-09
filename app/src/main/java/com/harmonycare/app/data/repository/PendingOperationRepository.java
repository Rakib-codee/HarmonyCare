package com.harmonycare.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.harmonycare.app.data.database.AppDatabase;
import com.harmonycare.app.data.database.PendingOperationDao;
import com.harmonycare.app.data.model.PendingOperation;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for PendingOperation data operations
 */
public class PendingOperationRepository {
    private PendingOperationDao pendingOperationDao;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    public PendingOperationRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.pendingOperationDao = database.pendingOperationDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void insertPendingOperation(PendingOperation operation, RepositoryCallback<Long> callback) {
        final RepositoryCallback<Long> finalCallback = callback;
        executorService.execute(() -> {
            try {
                long id = pendingOperationDao.insertPendingOperation(operation);
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
    
    public void getAllPendingOperations(RepositoryCallback<List<PendingOperation>> callback) {
        final RepositoryCallback<List<PendingOperation>> finalCallback = callback;
        executorService.execute(() -> {
            try {
                List<PendingOperation> operations = pendingOperationDao.getAllPendingOperations();
                if (finalCallback != null) {
                    final List<PendingOperation> finalOperations = operations != null ? operations : new java.util.ArrayList<>();
                    mainHandler.post(() -> finalCallback.onSuccess(finalOperations));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public void deletePendingOperation(PendingOperation operation, RepositoryCallback<Void> callback) {
        final RepositoryCallback<Void> finalCallback = callback;
        executorService.execute(() -> {
            try {
                pendingOperationDao.deletePendingOperation(operation);
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

