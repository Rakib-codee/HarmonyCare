package com.harmonycare.app.util;

import android.content.Context;
import android.util.Log;

import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.util.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for HTTP API communication for emergency sync
 * Allows unlimited range when devices are online
 * Falls back to local network broadcast when offline
 */
public class EmergencyApiHelper {
    private static final String TAG = "EmergencyApiHelper";
    
    // API Configuration - Uses Constants.API_BASE_URL
    // Update Constants.API_BASE_URL with your server URL
    private static final String API_CREATE_EMERGENCY = "/emergencies";
    private static final String API_GET_ACTIVE_EMERGENCIES = "/emergencies/active";
    private static final int CONNECTION_TIMEOUT = 7000;
    private static final int READ_TIMEOUT = 7000;
    
    private Context context;
    private NetworkHelper networkHelper;
    
    public EmergencyApiHelper(Context context) {
        this.context = context;
        this.networkHelper = new NetworkHelper(context);
    }
    
    /**
     * Check if API is available (device is online)
     */
    public boolean isApiAvailable() {
        return networkHelper != null && networkHelper.isConnected();
    }
    
    /**
     * Create emergency on server
     */
    public void createEmergency(Emergency emergency, ApiCallback<Long> callback) {
        if (!isApiAvailable()) {
            if (callback != null) {
                callback.onError(new Exception("Device is offline"));
            }
            return;
        }
        
        new Thread(() -> {
            try {
                String baseUrl = Constants.API_BASE_URL;
                URL url = new URL(baseUrl + API_CREATE_EMERGENCY);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setDoOutput(true);
                connection.setInstanceFollowRedirects(true);
                connection.setUseCaches(false);
                
                // Create JSON payload
                JSONObject emergencyJson = new JSONObject();
                emergencyJson.put("elderly_id", emergency.getElderlyId());
                emergencyJson.put("latitude", emergency.getLatitude());
                emergencyJson.put("longitude", emergency.getLongitude());
                emergencyJson.put("timestamp", emergency.getTimestamp());
                emergencyJson.put("status", emergency.getStatus());
                
                // Send request
                OutputStream os = connection.getOutputStream();
                os.write(emergencyJson.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();
                
                // Get response
                int responseCode = connection.getResponseCode();
                
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    // Parse response
                    JSONObject responseJson = new JSONObject(response.toString());
                    Long emergencyId = responseJson.has("id") ? responseJson.getLong("id") : null;
                    
                    Log.d(TAG, "Emergency created on server, ID: " + emergencyId);
                    
                    if (callback != null) {
                        callback.onSuccess(emergencyId);
                    }
                } else {
                    String errorMessage = "Server returned error code: " + responseCode;
                    Log.e(TAG, errorMessage);
                    if (callback != null) {
                        callback.onError(new Exception(errorMessage));
                    }
                }
                
                connection.disconnect();
                
            } catch (java.net.SocketTimeoutException e) {
                Log.w(TAG, "Connection timeout to server (Vercel cold start may be slow). Falling back to local sync.", e);
                if (callback != null) {
                    callback.onError(new Exception("Server connection timeout. Using local sync instead."));
                }
            } catch (java.net.UnknownHostException e) {
                Log.w(TAG, "Cannot resolve server host. Check internet connection.", e);
                if (callback != null) {
                    callback.onError(new Exception("Cannot reach server. Check internet connection."));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating emergency on server", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }).start();
    }
    
    /**
     * Get active emergencies from server
     */
    public void getActiveEmergencies(ApiCallback<List<Emergency>> callback) {
        getActiveEmergencies(null, callback);
    }

    /**
     * Get active emergencies from server (optionally include volunteer's accepted emergencies)
     */
    public void getActiveEmergencies(Integer volunteerId, ApiCallback<List<Emergency>> callback) {
        if (!isApiAvailable()) {
            if (callback != null) {
                callback.onError(new Exception("Device is offline"));
            }
            return;
        }
        
        new Thread(() -> {
            try {
                String baseUrl = Constants.API_BASE_URL;
                String urlStr = baseUrl + API_GET_ACTIVE_EMERGENCIES;
                if (volunteerId != null && volunteerId > 0) {
                    urlStr = urlStr + "?volunteer_id=" + volunteerId;
                }
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setInstanceFollowRedirects(true);
                connection.setUseCaches(false);
                
                // Get response
                int responseCode = connection.getResponseCode();
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    // Parse response
                    JSONArray emergenciesArray = new JSONArray(response.toString());
                    List<Emergency> emergencies = new ArrayList<>();
                    
                    for (int i = 0; i < emergenciesArray.length(); i++) {
                        JSONObject emergencyJson = emergenciesArray.getJSONObject(i);
                        Emergency emergency = parseEmergencyFromJson(emergencyJson);
                        emergencies.add(emergency);
                    }
                    
                    Log.d(TAG, "Fetched " + emergencies.size() + " active emergencies from server");
                    
                    if (callback != null) {
                        callback.onSuccess(emergencies);
                    }
                } else {
                    String errorMessage = "Server returned error code: " + responseCode;
                    Log.e(TAG, errorMessage);
                    if (callback != null) {
                        callback.onError(new Exception(errorMessage));
                    }
                }
                
                connection.disconnect();
                
            } catch (java.net.SocketTimeoutException e) {
                Log.w(TAG, "Connection timeout fetching emergencies. Server may be slow (Vercel cold start).", e);
                if (callback != null) {
                    callback.onError(new Exception("Server connection timeout. Using local database."));
                }
            } catch (java.net.UnknownHostException e) {
                Log.w(TAG, "Cannot resolve server host. Check internet connection.", e);
                if (callback != null) {
                    callback.onError(new Exception("Cannot reach server. Check internet connection."));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching emergencies from server", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }).start();
    }
    
    /**
     * Update emergency status on server
     */
    public void updateEmergencyStatus(int emergencyId, String status, Integer volunteerId, ApiCallback<Void> callback) {
        if (!isApiAvailable()) {
            if (callback != null) {
                callback.onError(new Exception("Device is offline"));
            }
            return;
        }
        
        new Thread(() -> {
            try {
                String baseUrl = Constants.API_BASE_URL;
                URL url = new URL(baseUrl + API_CREATE_EMERGENCY + "/" + emergencyId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setDoOutput(true);
                connection.setInstanceFollowRedirects(true);
                connection.setUseCaches(false);
                
                // Create JSON payload
                JSONObject updateJson = new JSONObject();
                updateJson.put("status", status);
                if (volunteerId != null) {
                    updateJson.put("volunteer_id", volunteerId);
                }
                
                // Send request
                OutputStream os = connection.getOutputStream();
                os.write(updateJson.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();
                
                // Get response
                int responseCode = connection.getResponseCode();
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Emergency status updated on server");
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                } else if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
                    String errorMessage = "Emergency already accepted";
                    Log.w(TAG, errorMessage);
                    if (callback != null) {
                        callback.onError(new Exception(errorMessage));
                    }
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    String errorMessage = "Emergency not found";
                    Log.w(TAG, errorMessage);
                    if (callback != null) {
                        callback.onError(new Exception(errorMessage));
                    }
                } else {
                    String errorMessage = "Server returned error code: " + responseCode;
                    Log.e(TAG, errorMessage);
                    if (callback != null) {
                        callback.onError(new Exception(errorMessage));
                    }
                }
                
                connection.disconnect();
                
            } catch (java.net.SocketTimeoutException e) {
                Log.w(TAG, "Connection timeout updating emergency. Server may be slow.", e);
                if (callback != null) {
                    callback.onError(new Exception("Server connection timeout."));
                }
            } catch (java.net.UnknownHostException e) {
                Log.w(TAG, "Cannot resolve server host. Check internet connection.", e);
                if (callback != null) {
                    callback.onError(new Exception("Cannot reach server. Check internet connection."));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating emergency on server", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }).start();
    }
    
    /**
     * Parse Emergency from JSON
     */
    private Emergency parseEmergencyFromJson(JSONObject json) throws Exception {
        Emergency emergency = new Emergency();
        emergency.setId(json.getInt("id"));
        emergency.setElderlyId(json.getInt("elderly_id"));
        emergency.setLatitude(json.getDouble("latitude"));
        emergency.setLongitude(json.getDouble("longitude"));
        emergency.setTimestamp(json.getLong("timestamp"));
        emergency.setStatus(json.getString("status"));
        if (json.has("volunteer_id") && !json.isNull("volunteer_id")) {
            emergency.setVolunteerId(json.getInt("volunteer_id"));
        }
        return emergency;
    }
    
    /**
     * Callback interface for API operations
     */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }
    
    /**
     * Get API base URL (from Constants)
     */
    public static String getApiBaseUrl() {
        return Constants.API_BASE_URL;
    }
}
