package com.harmonycare.app.data.repository;

import android.content.Context;

import com.harmonycare.app.data.database.AppDatabase;
import com.harmonycare.app.data.database.EmergencyContactDao;
import com.harmonycare.app.data.model.EmergencyContact;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for EmergencyContact data operations
 */
public class EmergencyContactRepository {
    private EmergencyContactDao contactDao;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    public EmergencyContactRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.contactDao = database.emergencyContactDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void insertContact(EmergencyContact contact, RepositoryCallback<Long> callback) {
        final RepositoryCallback<Long> finalCallback = callback;
        executorService.execute(() -> {
            try {
                long id = contactDao.insertEmergencyContact(contact);
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
    
    public void getContactsByUser(int userId, RepositoryCallback<List<EmergencyContact>> callback) {
        final RepositoryCallback<List<EmergencyContact>> finalCallback = callback;
        executorService.execute(() -> {
            try {
                List<EmergencyContact> contacts = contactDao.getContactsByUser(userId);
                if (finalCallback != null) {
                    final List<EmergencyContact> finalContacts = contacts;
                    mainHandler.post(() -> finalCallback.onSuccess(finalContacts));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public void getPrimaryContact(int userId, RepositoryCallback<EmergencyContact> callback) {
        final RepositoryCallback<EmergencyContact> finalCallback = callback;
        executorService.execute(() -> {
            try {
                EmergencyContact contact = contactDao.getPrimaryContact(userId);
                final EmergencyContact finalContact = contact;
                if (finalCallback != null) {
                    mainHandler.post(() -> finalCallback.onSuccess(finalContact));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }
    
    public void updateContact(EmergencyContact contact, RepositoryCallback<Void> callback) {
        final RepositoryCallback<Void> finalCallback = callback;
        executorService.execute(() -> {
            try {
                contactDao.updateEmergencyContact(contact);
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
    
    public void deleteContact(EmergencyContact contact, RepositoryCallback<Void> callback) {
        final RepositoryCallback<Void> finalCallback = callback;
        executorService.execute(() -> {
            try {
                contactDao.deleteEmergencyContact(contact);
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
    
    public void setPrimaryContact(int userId, int contactId, RepositoryCallback<Void> callback) {
        final RepositoryCallback<Void> finalCallback = callback;
        executorService.execute(() -> {
            try {
                contactDao.clearPrimaryContacts(userId);
                EmergencyContact contact = contactDao.getContactById(contactId);
                if (contact != null) {
                    contact.setPrimary(true);
                    contactDao.updateEmergencyContact(contact);
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

