package com.harmonycare.app.util;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DeviceApiHelper {
    private static final String TAG = "DeviceApiHelper";

    private static final int CONNECTION_TIMEOUT = 7000;
    private static final int READ_TIMEOUT = 7000;

    private final Context context;
    private final NetworkHelper networkHelper;

    public DeviceApiHelper(Context context) {
        this.context = context;
        this.networkHelper = new NetworkHelper(context);
    }

    public boolean isApiAvailable() {
        return networkHelper != null && networkHelper.isConnected();
    }

    public void registerDevice(int userId, String role, String fcmToken, Boolean isAvailable,
                               Double latitude, Double longitude, EmergencyApiHelper.ApiCallback<Void> callback) {
        if (!isApiAvailable()) {
            if (callback != null) callback.onError(new Exception("Device is offline"));
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(Constants.API_BASE_URL + "/devices/register");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                JSONObject json = new JSONObject();
                json.put("user_id", userId);
                json.put("role", role);
                json.put("jpush_id", fcmToken);
                if (isAvailable != null) json.put("is_available", isAvailable);
                if (latitude != null) json.put("latitude", latitude);
                if (longitude != null) json.put("longitude", longitude);

                OutputStream os = connection.getOutputStream();
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int code = connection.getResponseCode();
                connection.disconnect();

                if (code >= 200 && code < 300) {
                    if (callback != null) callback.onSuccess(null);
                } else {
                    if (callback != null) callback.onError(new Exception("Server returned error code: " + code));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error registering device", e);
                if (callback != null) callback.onError(e);
            }
        }).start();
    }

    public void updateVolunteerAvailability(int volunteerId, boolean isAvailable, String fcmToken,
                                           Double latitude, Double longitude, EmergencyApiHelper.ApiCallback<Void> callback) {
        if (!isApiAvailable()) {
            if (callback != null) callback.onError(new Exception("Device is offline"));
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(Constants.API_BASE_URL + "/volunteers/availability");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                JSONObject json = new JSONObject();
                json.put("volunteer_id", volunteerId);
                json.put("is_available", isAvailable);
                if (fcmToken != null && !fcmToken.isEmpty()) {
                    json.put("jpush_id", fcmToken);
                }
                if (latitude != null) json.put("latitude", latitude);
                if (longitude != null) json.put("longitude", longitude);

                OutputStream os = connection.getOutputStream();
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int code = connection.getResponseCode();
                connection.disconnect();

                if (code >= 200 && code < 300) {
                    if (callback != null) callback.onSuccess(null);
                } else {
                    if (callback != null) callback.onError(new Exception("Server returned error code: " + code));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating volunteer availability", e);
                if (callback != null) callback.onError(e);
            }
        }).start();
    }
}
