package com.harmonycare.app.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.model.PendingOperation;
import com.harmonycare.app.data.repository.EmergencyRepository;
import com.harmonycare.app.data.repository.PendingOperationRepository;

import org.json.JSONObject;

import java.util.List;

/**
 * Helper class for offline sync functionality
 */
public class OfflineSyncHelper {
    private Context context;
    private EmergencyRepository emergencyRepository;
    private PendingOperationRepository pendingOperationRepository;
    private NetworkHelper networkHelper;
    private Handler mainHandler;
    
    public OfflineSyncHelper(Context context) {
        this.context = context;
        this.emergencyRepository = new EmergencyRepository(context);
        this.pendingOperationRepository = new PendingOperationRepository(context);
        this.networkHelper = new NetworkHelper(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Check if device is online
     * @return true if online, false otherwise
     */
    public boolean isOnline() {
        return networkHelper != null && networkHelper.isConnected();
    }
    
    /**
     * Queue emergency for sync when offline
     * @param emergency Emergency to queue
     * @param callback Callback for result
     */
    public void queueEmergencyForSync(Emergency emergency, SyncCallback callback) {
        if (isOnline()) {
            // If online, try to sync immediately
            syncEmergency(emergency, callback);
        } else {
            // If offline, queue it
            try {
                JSONObject json = new JSONObject();
                json.put("elderly_id", emergency.getElderlyId());
                json.put("latitude", emergency.getLatitude());
                json.put("longitude", emergency.getLongitude());
                json.put("timestamp", emergency.getTimestamp());
                
                PendingOperation operation = new PendingOperation(
                    "create_emergency",
                    json.toString()
                );
                
                pendingOperationRepository.insertPendingOperation(operation, 
                    new PendingOperationRepository.RepositoryCallback<Long>() {
                        @Override
                        public void onSuccess(Long result) {
                            if (callback != null) {
                                mainHandler.post(() -> callback.onQueued(result));
                            }
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            if (callback != null) {
                                mainHandler.post(() -> callback.onError(error));
                            }
                        }
                    });
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }
    }
    
    /**
     * Sync emergency (create it)
     * @param emergency Emergency to sync
     * @param callback Callback for result
     */
    private void syncEmergency(Emergency emergency, SyncCallback callback) {
        emergencyRepository.createEmergency(emergency, 
            new EmergencyRepository.RepositoryCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onSynced(result));
                    }
                }
                
                @Override
                public void onError(Exception error) {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onError(error));
                    }
                }
            });
    }
    
    /**
     * Sync all pending operations
     * @param callback Callback for result
     */
    public void syncAllPendingOperations(SyncAllCallback callback) {
        if (!isOnline()) {
            if (callback != null) {
                callback.onComplete(0, 0, "Device is offline");
            }
            return;
        }
        
        pendingOperationRepository.getAllPendingOperations(
            new PendingOperationRepository.RepositoryCallback<List<PendingOperation>>() {
                @Override
                public void onSuccess(List<PendingOperation> operations) {
                    if (operations == null || operations.isEmpty()) {
                        if (callback != null) {
                            callback.onComplete(0, 0, "No pending operations");
                        }
                        return;
                    }
                    
                    syncOperations(operations, 0, 0, callback);
                }
                
                @Override
                public void onError(Exception error) {
                    if (callback != null) {
                        callback.onComplete(0, 0, "Error: " + error.getMessage());
                    }
                }
            });
    }
    
    /**
     * Sync operations recursively
     */
    private void syncOperations(List<PendingOperation> operations, int index, 
                               int successCount, SyncAllCallback callback) {
        if (index >= operations.size()) {
            if (callback != null) {
                callback.onComplete(successCount, operations.size() - successCount, null);
            }
            return;
        }
        
        PendingOperation operation = operations.get(index);
        if (!isOnline()) {
            if (callback != null) {
                callback.onComplete(successCount, operations.size() - successCount, "Device went offline");
            }
            return;
        }
        
        try {
            JSONObject json = new JSONObject(operation.getDataJson());
            
            if ("create_emergency".equals(operation.getOperationType())) {
                Emergency emergency = new Emergency();
                emergency.setElderlyId(json.getInt("elderly_id"));
                emergency.setLatitude(json.getDouble("latitude"));
                emergency.setLongitude(json.getDouble("longitude"));
                emergency.setTimestamp(json.getLong("timestamp"));
                emergency.setStatus("active");
                
                syncEmergency(emergency, new SyncCallback() {
                    @Override
                    public void onSynced(Long result) {
                        // Delete pending operation on success
                        pendingOperationRepository.deletePendingOperation(operation, null);
                        syncOperations(operations, index + 1, successCount + 1, callback);
                    }
                    
                    @Override
                    public void onQueued(Long result) {
                        // Should not happen
                        syncOperations(operations, index + 1, successCount, callback);
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        // Keep operation for retry
                        syncOperations(operations, index + 1, successCount, callback);
                    }
                });
            } else {
                // Unknown operation type, skip
                syncOperations(operations, index + 1, successCount, callback);
            }
        } catch (Exception e) {
            // Error parsing, skip
            syncOperations(operations, index + 1, successCount, callback);
        }
    }
    
    /**
     * Callback interface for sync operations
     */
    public interface SyncCallback {
        void onSynced(Long emergencyId);
        void onQueued(Long operationId);
        void onError(Exception error);
    }
    
    /**
     * Callback interface for sync all operations
     */
    public interface SyncAllCallback {
        void onComplete(int successCount, int failureCount, String message);
    }
}

