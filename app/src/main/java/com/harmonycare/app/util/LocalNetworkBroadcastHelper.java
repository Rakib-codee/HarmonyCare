package com.harmonycare.app.util;

import android.content.Context;
import android.util.Log;

import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.repository.EmergencyRepository;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * Helper class for broadcasting emergencies on local network (same WiFi)
 * Uses UDP broadcast to send emergency data to nearby devices
 * Works offline - only requires same WiFi network
 */
public class LocalNetworkBroadcastHelper {
    private static final String TAG = "LocalNetworkBroadcast";
    private static final int BROADCAST_PORT = 8888;
    private static final String BROADCAST_MESSAGE_PREFIX = "HARMONYCARE_EMERGENCY:";
    
    private Context context;
    private NetworkHelper networkHelper;
    private DatagramSocket broadcastSocket;
    private boolean isListening = false;
    private Thread listenThread;
    private EmergencyListener listener;
    
    public interface EmergencyListener {
        void onEmergencyReceived(Emergency emergency);
    }
    
    public LocalNetworkBroadcastHelper(Context context) {
        this.context = context;
        this.networkHelper = new NetworkHelper(context);
    }
    
    public void setListener(EmergencyListener listener) {
        this.listener = listener;
    }
    
    /**
     * Broadcast emergency to local network
     */
    public void broadcastEmergency(Emergency emergency) {
        if (!networkHelper.isWifiConnected()) {
            Log.d(TAG, "Not on WiFi network, skipping broadcast");
            return;
        }
        
        new Thread(() -> {
            try {
                JSONObject emergencyJson = new JSONObject();
                emergencyJson.put("elderly_id", emergency.getElderlyId());
                emergencyJson.put("latitude", emergency.getLatitude());
                emergencyJson.put("longitude", emergency.getLongitude());
                emergencyJson.put("timestamp", emergency.getTimestamp());
                emergencyJson.put("status", emergency.getStatus());
                if (emergency.getId() > 0) {
                    emergencyJson.put("emergency_id", emergency.getId());
                }
                
                String emergencyData = emergencyJson.toString();
                String message = BROADCAST_MESSAGE_PREFIX + emergencyData;
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                
                // Get broadcast address
                InetAddress broadcastAddress = getBroadcastAddress();
                if (broadcastAddress == null) {
                    Log.w(TAG, "Could not determine broadcast address");
                    return;
                }
                
                // Create socket and send broadcast
                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);
                
                DatagramPacket packet = new DatagramPacket(
                        messageBytes,
                        messageBytes.length,
                        broadcastAddress,
                        BROADCAST_PORT
                );
                
                socket.send(packet);
                socket.close();
                
                Log.d(TAG, "Emergency broadcast sent to local network");
                
            } catch (Exception e) {
                Log.e(TAG, "Error broadcasting emergency", e);
            }
        }).start();
    }
    
    /**
     * Start listening for emergency broadcasts on local network
     */
    public void startListening() {
        if (isListening) {
            return;
        }
        
        if (!networkHelper.isWifiConnected()) {
            Log.d(TAG, "Not on WiFi network, cannot listen");
            return;
        }
        
        isListening = true;
        listenThread = new Thread(() -> {
            try {
                broadcastSocket = new DatagramSocket(BROADCAST_PORT);
                broadcastSocket.setBroadcast(true);
                
                byte[] buffer = new byte[4096];
                
                Log.d(TAG, "Started listening for emergency broadcasts on port " + BROADCAST_PORT);
                
                while (isListening) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    broadcastSocket.receive(packet);
                    
                    String receivedMessage = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    
                    if (receivedMessage.startsWith(BROADCAST_MESSAGE_PREFIX)) {
                        String emergencyData = receivedMessage.substring(BROADCAST_MESSAGE_PREFIX.length());
                        handleReceivedEmergency(emergencyData);
                    }
                }
            } catch (SocketException e) {
                if (isListening) {
                    Log.e(TAG, "Socket error while listening", e);
                }
            } catch (IOException e) {
                if (isListening) {
                    Log.e(TAG, "IO error while listening", e);
                }
            } finally {
                if (broadcastSocket != null && !broadcastSocket.isClosed()) {
                    broadcastSocket.close();
                }
            }
        });
        
        listenThread.start();
    }
    
    /**
     * Stop listening for broadcasts
     */
    public void stopListening() {
        isListening = false;
        if (broadcastSocket != null && !broadcastSocket.isClosed()) {
            broadcastSocket.close();
        }
        if (listenThread != null) {
            listenThread.interrupt();
        }
        Log.d(TAG, "Stopped listening for broadcasts");
    }
    
    /**
     * Get broadcast address for current WiFi network
     */
    private InetAddress getBroadcastAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                // Skip loopback and inactive interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    
                    // Check if it's a valid IPv4 address
                    if (address.getAddress().length == 4) {
                        byte[] ip = address.getAddress();
                        byte[] broadcast = new byte[4];
                        
                        // Calculate broadcast address (last address in subnet)
                        // For simplicity, assume /24 subnet (255.255.255.0)
                        broadcast[0] = ip[0];
                        broadcast[1] = ip[1];
                        broadcast[2] = ip[2];
                        broadcast[3] = (byte) 255;
                        
                        return InetAddress.getByAddress(broadcast);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting broadcast address", e);
        }
        
        // Fallback: try common broadcast address
        try {
            return InetAddress.getByName("255.255.255.255");
        } catch (Exception e) {
            Log.e(TAG, "Error getting fallback broadcast address", e);
            return null;
        }
    }
    
    /**
     * Handle received emergency data
     */
    private void handleReceivedEmergency(String emergencyData) {
        try {
            JSONObject emergencyJson = new JSONObject(emergencyData);
            
            Emergency emergency = new Emergency();
            emergency.setElderlyId(emergencyJson.getInt("elderly_id"));
            emergency.setLatitude(emergencyJson.getDouble("latitude"));
            emergency.setLongitude(emergencyJson.getDouble("longitude"));
            emergency.setTimestamp(emergencyJson.getLong("timestamp"));
            emergency.setStatus(emergencyJson.getString("status"));
            if (emergencyJson.has("emergency_id")) {
                emergency.setId(emergencyJson.getInt("emergency_id"));
            }
            
            Log.d(TAG, "Received emergency from local network: " + emergencyData);
            
            // Save to local database
            EmergencyRepository emergencyRepository = new EmergencyRepository(context);
            emergencyRepository.createEmergency(emergency, new EmergencyRepository.RepositoryCallback<Long>() {
                @Override
                public void onSuccess(Long emergencyId) {
                    Log.d(TAG, "Emergency received and saved, ID: " + emergencyId);
                    emergency.setId(emergencyId.intValue());
                    
                    // Notify listener
                    if (listener != null) {
                        listener.onEmergencyReceived(emergency);
                    }
                    
                    // Show notification
                    NotificationHelper notificationHelper = new NotificationHelper(context);
                    notificationHelper.showEmergencyNotification(
                            "New Emergency on Local Network",
                            "Emergency detected from nearby device"
                    );
                }
                
                @Override
                public void onError(Exception error) {
                    Log.e(TAG, "Error saving received emergency", error);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing received emergency data", e);
        }
    }
    
    public void cleanup() {
        stopListening();
    }
    
    public boolean isListening() {
        return isListening;
    }
}
