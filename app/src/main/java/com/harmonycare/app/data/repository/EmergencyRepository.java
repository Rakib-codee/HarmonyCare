package com.harmonycare.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.harmonycare.app.data.database.AppDatabase;
import com.harmonycare.app.data.database.EmergencyDao;
import com.harmonycare.app.data.model.Emergency;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for Emergency data operations
 */
public class EmergencyRepository {
    private EmergencyDao emergencyDao;
    private ExecutorService executorService;
    private Handler mainHandler;

    public EmergencyRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.emergencyDao = database.emergencyDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void createEmergency(Emergency emergency, RepositoryCallback<Long> callback) {
        final RepositoryCallback<Long> finalCallback = callback;
        executorService.execute(() -> {
            try {
                long id = emergencyDao.insertEmergency(emergency);
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

    public void getActiveEmergencies(RepositoryCallback<List<Emergency>> callback) {
        final RepositoryCallback<List<Emergency>> finalCallback = callback;
        executorService.execute(() -> {
            try {
                List<Emergency> emergencies = emergencyDao.getEmergenciesByStatus("active");
                if (finalCallback != null) {
                    final List<Emergency> finalEmergencies = emergencies;
                    mainHandler.post(() -> finalCallback.onSuccess(finalEmergencies));
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
     * Get active and accepted emergencies for volunteers
     * This includes both "active" (new) and "accepted" (in progress) emergencies
     */
    public void getActiveAndAcceptedEmergencies(RepositoryCallback<List<Emergency>> callback) {
        final RepositoryCallback<List<Emergency>> finalCallback = callback;
        executorService.execute(() -> {
            try {
                List<Emergency> emergencies = emergencyDao.getActiveAndAcceptedEmergencies();
                if (finalCallback != null) {
                    final List<Emergency> finalEmergencies = emergencies != null ? emergencies
                            : new java.util.ArrayList<>();
                    mainHandler.post(() -> finalCallback.onSuccess(finalEmergencies));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }

    public void getEmergenciesByElderly(int elderlyId, RepositoryCallback<List<Emergency>> callback) {
        final RepositoryCallback<List<Emergency>> finalCallback = callback;
        executorService.execute(() -> {
            try {
                List<Emergency> emergencies = emergencyDao.getEmergenciesByElderly(elderlyId);
                if (finalCallback != null) {
                    final List<Emergency> finalEmergencies = emergencies;
                    mainHandler.post(() -> finalCallback.onSuccess(finalEmergencies));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }

    public void getEmergenciesByVolunteer(int volunteerId, RepositoryCallback<List<Emergency>> callback) {
        final RepositoryCallback<List<Emergency>> finalCallback = callback;
        executorService.execute(() -> {
            try {
                // Get all emergencies where volunteer_id matches (including accepted and
                // completed)
                List<Emergency> emergencies = emergencyDao.getEmergenciesByVolunteer(volunteerId);
                if (finalCallback != null) {
                    final List<Emergency> finalEmergencies = emergencies != null ? emergencies
                            : new java.util.ArrayList<>();
                    mainHandler.post(() -> finalCallback.onSuccess(finalEmergencies));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }

    public void getEmergencyById(int id, RepositoryCallback<Emergency> callback) {
        final RepositoryCallback<Emergency> finalCallback = callback;
        executorService.execute(() -> {
            try {
                Emergency emergency = emergencyDao.getEmergencyById(id);
                if (finalCallback != null) {
                    final Emergency finalEmergency = emergency;
                    mainHandler.post(() -> finalCallback.onSuccess(finalEmergency));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }

    public void updateEmergency(Emergency emergency, RepositoryCallback<Void> callback) {
        final RepositoryCallback<Void> finalCallback = callback;
        executorService.execute(() -> {
            try {
                // Log for debugging
                android.util.Log.d("EmergencyRepository", "Updating emergency ID: " + emergency.getId() +
                        ", Status: " + emergency.getStatus() +
                        ", VolunteerId: " + emergency.getVolunteerId());

                emergencyDao.updateEmergency(emergency);

                // Verify the update by reading back
                Emergency updated = emergencyDao.getEmergencyById(emergency.getId());
                if (updated != null) {
                    android.util.Log.d("EmergencyRepository", "Emergency updated successfully. " +
                            "ID: " + updated.getId() +
                            ", Status: " + updated.getStatus() +
                            ", VolunteerId: " + updated.getVolunteerId());
                }

                if (finalCallback != null) {
                    mainHandler.post(() -> finalCallback.onSuccess(null));
                }
            } catch (Exception e) {
                android.util.Log.e("EmergencyRepository", "Error updating emergency", e);
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }

    public void getAllEmergencies(RepositoryCallback<List<Emergency>> callback) {
        final RepositoryCallback<List<Emergency>> finalCallback = callback;
        executorService.execute(() -> {
            try {
                List<Emergency> emergencies = emergencyDao.getAllEmergencies();
                if (finalCallback != null) {
                    final List<Emergency> finalEmergencies = emergencies;
                    mainHandler.post(() -> finalCallback.onSuccess(finalEmergencies));
                }
            } catch (Exception e) {
                if (finalCallback != null) {
                    final Exception finalError = e;
                    mainHandler.post(() -> finalCallback.onError(finalError));
                }
            }
        });
    }

    public void getActiveEmergencyByElderly(int elderlyId, RepositoryCallback<Emergency> callback) {
        final RepositoryCallback<Emergency> finalCallback = callback;
        executorService.execute(() -> {
            try {
                Emergency emergency = emergencyDao.getActiveEmergencyByElderly(elderlyId, "active");
                if (finalCallback != null) {
                    final Emergency finalEmergency = emergency;
                    mainHandler.post(() -> finalCallback.onSuccess(finalEmergency));
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
     * Get total number of completed emergencies by a volunteer
     */
    public void getCompletedCountByVolunteer(int volunteerId, RepositoryCallback<Integer> callback) {
        final RepositoryCallback<Integer> finalCallback = callback;
        executorService.execute(() -> {
            try {
                List<Emergency> emergencies = emergencyDao.getEmergenciesByVolunteer(volunteerId);
                int count = 0;
                if (emergencies != null) {
                    for (Emergency emergency : emergencies) {
                        if ("completed".equalsIgnoreCase(emergency.getStatus())) {
                            count++;
                        }
                    }
                }
                final int finalCount = count;
                if (finalCallback != null) {
                    mainHandler.post(() -> finalCallback.onSuccess(finalCount));
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
     * Get total number of accepted emergencies by a volunteer
     */
    public void getAcceptedCountByVolunteer(int volunteerId, RepositoryCallback<Integer> callback) {
        final RepositoryCallback<Integer> finalCallback = callback;
        executorService.execute(() -> {
            try {
                List<Emergency> emergencies = emergencyDao.getEmergenciesByVolunteer(volunteerId);
                int count = 0;
                if (emergencies != null) {
                    for (Emergency emergency : emergencies) {
                        String status = emergency.getStatus();
                        if ("accepted".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
                            count++;
                        }
                    }
                }
                final int finalCount = count;
                if (finalCallback != null) {
                    mainHandler.post(() -> finalCallback.onSuccess(finalCount));
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
     * Get all emergencies by volunteer for statistics
     * This includes both accepted and completed emergencies
     */
    public void getEmergenciesForStats(int volunteerId, RepositoryCallback<List<Emergency>> callback) {
        final RepositoryCallback<List<Emergency>> finalCallback = callback;
        executorService.execute(() -> {
            try {
                android.util.Log.d("EmergencyRepository", "Getting stats for volunteerId: " + volunteerId);

                // Try the specific query first
                List<Emergency> emergencies = emergencyDao.getAcceptedAndCompletedEmergenciesByVolunteer(volunteerId);

                android.util.Log.d("EmergencyRepository",
                        "Query returned " + (emergencies != null ? emergencies.size() : 0) + " emergencies");

                // If no results, try the general query as fallback
                if (emergencies == null || emergencies.isEmpty()) {
                    android.util.Log.d("EmergencyRepository", "Trying fallback query...");
                    emergencies = emergencyDao.getEmergenciesByVolunteer(volunteerId);
                    if (emergencies != null) {
                        // Filter to only accepted and completed
                        java.util.List<Emergency> filtered = new java.util.ArrayList<>();
                        for (Emergency e : emergencies) {
                            String status = e.getStatus();
                            if (("accepted".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status))
                                    && e.getVolunteerId() != null && e.getVolunteerId() == volunteerId) {
                                filtered.add(e);
                            }
                        }
                        emergencies = filtered;
                        android.util.Log.d("EmergencyRepository",
                                "Fallback query returned " + emergencies.size() + " emergencies");
                    }
                }

                if (finalCallback != null) {
                    final List<Emergency> finalEmergencies = emergencies != null ? emergencies
                            : new java.util.ArrayList<>();
                    mainHandler.post(() -> finalCallback.onSuccess(finalEmergencies));
                }
            } catch (Exception e) {
                android.util.Log.e("EmergencyRepository", "Error getting emergencies for stats", e);
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
