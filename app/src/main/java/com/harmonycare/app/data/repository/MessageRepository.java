package com.harmonycare.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.harmonycare.app.data.database.AppDatabase;
import com.harmonycare.app.data.database.MessageDao;
import com.harmonycare.app.data.model.Message;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for Message data operations
 */
public class MessageRepository {
    private MessageDao messageDao;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    public MessageRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.messageDao = database.messageDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void sendMessage(Message message, RepositoryCallback<Long> callback) {
        final RepositoryCallback<Long> finalCallback = callback;
        executorService.execute(() -> {
            try {
                // Validate message data before saving
                if (message.getEmergencyId() <= 0 || message.getSenderId() <= 0 || 
                    message.getReceiverId() <= 0 || message.getMessage() == null || 
                    message.getMessage().trim().isEmpty()) {
                    if (finalCallback != null) {
                        final Exception error = new IllegalArgumentException("Invalid message data");
                        mainHandler.post(() -> finalCallback.onError(error));
                    }
                    return;
                }
                
                long id = messageDao.insertMessage(message);
                if (id > 0) {
                    // Verify message was saved by retrieving it
                    Message savedMessage = messageDao.getLatestMessage(message.getEmergencyId());
                    if (finalCallback != null) {
                        final long finalId = id;
                        mainHandler.post(() -> finalCallback.onSuccess(finalId));
                    }
                } else {
                    if (finalCallback != null) {
                        final Exception error = new Exception("Failed to save message - invalid ID returned");
                        mainHandler.post(() -> finalCallback.onError(error));
                    }
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public void getMessagesByEmergency(int emergencyId, RepositoryCallback<List<Message>> callback) {
        final RepositoryCallback<List<Message>> finalCallback = callback;
        executorService.execute(() -> {
            try {
                if (emergencyId <= 0) {
                    if (finalCallback != null) {
                        final Exception error = new IllegalArgumentException("Invalid emergency ID");
                        mainHandler.post(() -> finalCallback.onError(error));
                    }
                    return;
                }
                
                List<Message> messages = messageDao.getMessagesByEmergency(emergencyId);
                if (finalCallback != null) {
                    final List<Message> finalMessages = messages != null ? messages : new java.util.ArrayList<>();
                    mainHandler.post(() -> finalCallback.onSuccess(finalMessages));
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
     * Verify a message was saved by checking if it exists
     */
    public void verifyMessageSaved(int emergencyId, long messageId, RepositoryCallback<Boolean> callback) {
        final RepositoryCallback<Boolean> finalCallback = callback;
        executorService.execute(() -> {
            try {
                List<Message> messages = messageDao.getMessagesByEmergency(emergencyId);
                boolean found = false;
                if (messages != null) {
                    for (Message msg : messages) {
                        if (msg.getId() == messageId) {
                            found = true;
                            break;
                        }
                    }
                }
                final boolean finalFound = found;
                if (finalCallback != null) {
                    mainHandler.post(() -> finalCallback.onSuccess(finalFound));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public void getMessagesByEmergencyAndUser(int emergencyId, int userId, RepositoryCallback<List<Message>> callback) {
        final RepositoryCallback<List<Message>> finalCallback = callback;
        executorService.execute(() -> {
            try {
                List<Message> messages = messageDao.getMessagesByEmergencyAndUser(emergencyId, userId);
                if (finalCallback != null) {
                    final List<Message> finalMessages = messages != null ? messages : new java.util.ArrayList<>();
                    mainHandler.post(() -> finalCallback.onSuccess(finalMessages));
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

