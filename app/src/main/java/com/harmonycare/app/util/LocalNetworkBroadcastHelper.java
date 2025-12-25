package com.harmonycare.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.model.Message;
import com.harmonycare.app.data.repository.EmergencyRepository;
import com.harmonycare.app.data.repository.MessageRepository;

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
    private static final String BROADCAST_EMERGENCY_PREFIX = "HARMONYCARE_EMERGENCY:";
    private static final String BROADCAST_MESSAGE_PREFIX = "HARMONYCARE_MESSAGE:";
    
    private Context context;
    private NetworkHelper networkHelper;
    private DatagramSocket broadcastSocket;
    private boolean isListening = false;
    private Thread listenThread;
    private EmergencyListener listener;
    private MessageListener messageListener;
    
    public interface EmergencyListener {
        void onEmergencyReceived(Emergency emergency);
    }

    public interface MessageListener {
        void onMessageReceived(Message message);
    }
    
    public LocalNetworkBroadcastHelper(Context context) {
        this.context = context;
        this.networkHelper = new NetworkHelper(context);
    }
    
    public void setListener(EmergencyListener listener) {
        this.listener = listener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
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
                if (emergency.getElderlyName() != null) {
                    emergencyJson.put("elderly_name", emergency.getElderlyName());
                }
                if (emergency.getElderlyContact() != null) {
                    emergencyJson.put("elderly_contact", emergency.getElderlyContact());
                }
                emergencyJson.put("latitude", emergency.getLatitude());
                emergencyJson.put("longitude", emergency.getLongitude());
                emergencyJson.put("timestamp", emergency.getTimestamp());
                emergencyJson.put("status", emergency.getStatus());
                if (emergency.getId() > 0) {
                    emergencyJson.put("emergency_id", emergency.getId());
                }
                if (emergency.getVolunteerId() != null) {
                    emergencyJson.put("volunteer_id", emergency.getVolunteerId());
                }
                if (emergency.getVolunteerName() != null) {
                    emergencyJson.put("volunteer_name", emergency.getVolunteerName());
                }
                if (emergency.getVolunteerContact() != null) {
                    emergencyJson.put("volunteer_contact", emergency.getVolunteerContact());
                }
                
                String emergencyData = emergencyJson.toString();
                String message = BROADCAST_EMERGENCY_PREFIX + emergencyData;
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
                    
                    if (receivedMessage.startsWith(BROADCAST_EMERGENCY_PREFIX)) {
                        String emergencyData = receivedMessage.substring(BROADCAST_EMERGENCY_PREFIX.length());
                        handleReceivedEmergency(emergencyData);
                    } else if (receivedMessage.startsWith(BROADCAST_MESSAGE_PREFIX)) {
                        String messageData = receivedMessage.substring(BROADCAST_MESSAGE_PREFIX.length());
                        handleReceivedChatMessage(messageData);
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
            if (emergencyJson.has("elderly_name") && !emergencyJson.isNull("elderly_name")) {
                emergency.setElderlyName(emergencyJson.getString("elderly_name"));
            }
            if (emergencyJson.has("elderly_contact") && !emergencyJson.isNull("elderly_contact")) {
                emergency.setElderlyContact(emergencyJson.getString("elderly_contact"));
            }
            emergency.setLatitude(emergencyJson.getDouble("latitude"));
            emergency.setLongitude(emergencyJson.getDouble("longitude"));
            emergency.setTimestamp(emergencyJson.getLong("timestamp"));
            emergency.setStatus(emergencyJson.getString("status"));
            if (emergencyJson.has("emergency_id")) {
                emergency.setId(emergencyJson.getInt("emergency_id"));
            }
            if (emergencyJson.has("volunteer_id") && !emergencyJson.isNull("volunteer_id")) {
                emergency.setVolunteerId(emergencyJson.getInt("volunteer_id"));
            }
            if (emergencyJson.has("volunteer_name") && !emergencyJson.isNull("volunteer_name")) {
                emergency.setVolunteerName(emergencyJson.getString("volunteer_name"));
            }
            if (emergencyJson.has("volunteer_contact") && !emergencyJson.isNull("volunteer_contact")) {
                emergency.setVolunteerContact(emergencyJson.getString("volunteer_contact"));
            }
            
            Log.d(TAG, "Received emergency from local network: " + emergencyData);
            
            // Save to local database
            EmergencyRepository emergencyRepository = new EmergencyRepository(context);
            final int broadcastEmergencyId = emergency.getId();
            emergencyRepository.createEmergency(emergency, new EmergencyRepository.RepositoryCallback<Long>() {
                @Override
                public void onSuccess(Long emergencyId) {
                    Log.d(TAG, "Emergency received and saved, ID: " + emergencyId);
                    if (broadcastEmergencyId <= 0 && emergencyId != null) {
                        emergency.setId(emergencyId.intValue());
                    } else {
                        emergency.setId(broadcastEmergencyId);
                    }
                    
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

    public void broadcastMessage(Message message) {
        if (message == null) return;
        if (!networkHelper.isWifiConnected()) {
            Log.d(TAG, "Not on WiFi network, skipping message broadcast");
            return;
        }

        new Thread(() -> {
            try {
                JSONObject messageJson = new JSONObject();
                messageJson.put("emergency_id", message.getEmergencyId());
                messageJson.put("sender_id", message.getSenderId());
                if (message.getSenderContact() != null) {
                    messageJson.put("sender_contact", message.getSenderContact());
                }
                messageJson.put("receiver_id", message.getReceiverId());
                if (message.getReceiverContact() != null) {
                    messageJson.put("receiver_contact", message.getReceiverContact());
                }
                messageJson.put("message", message.getMessage());
                messageJson.put("timestamp", message.getTimestamp());

                String payload = messageJson.toString();
                String wire = BROADCAST_MESSAGE_PREFIX + payload;
                byte[] messageBytes = wire.getBytes(StandardCharsets.UTF_8);

                InetAddress broadcastAddress = getBroadcastAddress();
                if (broadcastAddress == null) {
                    Log.w(TAG, "Could not determine broadcast address for message");
                    return;
                }

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

                Log.d(TAG, "Chat message broadcast sent to local network");
            } catch (Exception e) {
                Log.e(TAG, "Error broadcasting chat message", e);
            }
        }).start();
    }

    private void handleReceivedChatMessage(String messageData) {
        try {
            JSONObject json = new JSONObject(messageData);

            Message message = new Message();
            message.setEmergencyId(json.getInt("emergency_id"));
            message.setSenderId(json.getInt("sender_id"));
            if (json.has("sender_contact") && !json.isNull("sender_contact")) {
                message.setSenderContact(json.getString("sender_contact"));
            }
            message.setReceiverId(json.getInt("receiver_id"));
            if (json.has("receiver_contact") && !json.isNull("receiver_contact")) {
                message.setReceiverContact(json.getString("receiver_contact"));
            }
            message.setMessage(json.getString("message"));
            message.setTimestamp(json.getLong("timestamp"));

            MessageRepository messageRepository = new MessageRepository(context);
            messageRepository.sendMessage(message, new MessageRepository.RepositoryCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    if (messageListener != null) {
                        messageListener.onMessageReceived(message);
                    }

                    SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
                    int currentUserId = prefs.getInt(Constants.KEY_USER_ID, -1);
                    String currentUserContact = prefs.getString(Constants.KEY_USER_CONTACT, null);
                    boolean isReceiverById = currentUserId > 0 && currentUserId == message.getReceiverId();
                    boolean isReceiverByContact = currentUserContact != null && message.getReceiverContact() != null
                            && currentUserContact.equals(message.getReceiverContact());
                    if (isReceiverById || isReceiverByContact) {
                        NotificationHelper notificationHelper = new NotificationHelper(context);
                        notificationHelper.showEmergencyNotification(
                                "New Message",
                                message.getMessage()
                        );
                    }
                }

                @Override
                public void onError(Exception error) {
                    Log.e(TAG, "Error saving received chat message", error);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error parsing received chat message", e);
        }
    }
    
    public void cleanup() {
        stopListening();
    }
    
    public boolean isListening() {
        return isListening;
    }
}
