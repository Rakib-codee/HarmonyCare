package com.harmonycare.app.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.harmonycare.app.R;
import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.model.Message;
import com.harmonycare.app.data.model.User;
import com.harmonycare.app.data.repository.MessageRepository;
import com.harmonycare.app.data.repository.UserRepository;
import com.harmonycare.app.util.NotificationHelper;
import com.harmonycare.app.viewmodel.AuthViewModel;
import com.harmonycare.app.viewmodel.EmergencyViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Chat Activity for volunteer-elderly communication during emergency
 */
public class ChatActivity extends BaseActivity {
    private static final int POLL_INTERVAL = 3000; // 3 seconds
    
    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvElderlyName;
    private TextView tvConnectionStatus;
    
    private MessageRepository messageRepository;
    private UserRepository userRepository;
    private AuthViewModel authViewModel;
    private EmergencyViewModel emergencyViewModel;
    private NotificationHelper notificationHelper;
    
    private int emergencyId;
    private int currentUserId;
    private int otherUserId;
    private String currentEmergencyStatus = "";
    private boolean isChatEnabled = true;
    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter adapter;
    private Handler pollHandler;
    private Runnable pollRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        emergencyId = getIntent().getIntExtra("emergency_id", -1);
        if (emergencyId == -1) {
            showToast("Invalid emergency");
            finish();
            return;
        }
        
        messageRepository = new MessageRepository(this);
        userRepository = new UserRepository(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        emergencyViewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);
        notificationHelper = new NotificationHelper(this);
        
        currentUserId = authViewModel.getCurrentUserId();
        if (currentUserId == -1) {
            finish();
            return;
        }
        
        initViews();
        // Load emergency details first, then messages
        loadEmergencyDetails();
        // Delay message loading slightly to ensure emergency is loaded
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadMessages();
            startPolling();
        }, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload messages when returning to chat screen
        if (emergencyId > 0) {
            loadMessages();
        }
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvElderlyName = findViewById(R.id.tvElderlyName);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter();
        recyclerView.setAdapter(adapter);
        
        btnSend.setOnClickListener(v -> sendMessage());
        
        // Initially hide connection status
        if (tvConnectionStatus != null) {
            tvConnectionStatus.setVisibility(View.GONE);
        }
    }
    
    private void loadEmergencyDetails() {
        emergencyViewModel.getEmergencyById(emergencyId, emergency -> {
            if (emergency != null) {
                // Update current emergency status
                currentEmergencyStatus = emergency.getStatus();
                
                // Check if emergency is completed - disable chat
                if ("completed".equalsIgnoreCase(currentEmergencyStatus)) {
                    disableChat("Emergency completed - Chat connection closed");
                    return;
                }
                
                // Check if emergency is accepted - enable chat
                if ("accepted".equalsIgnoreCase(currentEmergencyStatus)) {
                    enableChat();
                }
                
                // Determine other user (elderly or volunteer)
                if (currentUserId == emergency.getElderlyId()) {
                    // Current user is elderly, other is volunteer
                    otherUserId = emergency.getVolunteerId() != null ? emergency.getVolunteerId() : -1;
                    
                    // If emergency is accepted but volunteer_id is not set yet, wait a bit
                    if ("accepted".equalsIgnoreCase(emergency.getStatus()) && otherUserId <= 0) {
                        // Retry after a delay to get updated volunteer_id
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            loadEmergencyDetails();
                        }, 2000);
                        return;
                    }
                } else {
                    // Current user is volunteer, other is elderly
                    otherUserId = emergency.getElderlyId();
                }
                
                // Load other user's name
                if (otherUserId > 0) {
                    userRepository.getUserById(otherUserId, new UserRepository.RepositoryCallback<User>() {
                        @Override
                        public void onSuccess(User user) {
                            if (user != null && tvElderlyName != null) {
                                tvElderlyName.setText("Chat with " + user.getName());
                            } else if (tvElderlyName != null) {
                                tvElderlyName.setText("Chat");
                            }
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            if (tvElderlyName != null) {
                                tvElderlyName.setText("Chat");
                            }
                        }
                    });
                } else {
                    if (tvElderlyName != null) {
                        tvElderlyName.setText("Chat - Waiting for connection...");
                    }
                }
                
                // Always reload messages after emergency details are loaded
                // This ensures we have the latest messages even if emergency status changed
                loadMessages();
            } else {
                showToast("Emergency not found");
                // Don't finish - allow user to see existing messages
            }
        });
    }
    
    private void loadMessages() {
        messageRepository.getMessagesByEmergency(emergencyId, new MessageRepository.RepositoryCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (messages != null && !messages.isEmpty()) {
                    int previousSize = messageList.size();
                    messageList = messages;
                    
                    // Always update adapter and scroll if new messages
                    adapter.notifyDataSetChanged();
                    if (messages.size() > previousSize || previousSize == 0) {
                        scrollToBottom();
                    }
                } else {
                    // Even if empty, update the list
                    messageList = new ArrayList<>();
                    adapter.notifyDataSetChanged();
                }
            }
            
            @Override
            public void onError(Exception error) {
                // Don't show error toast on every poll, only log silently
                // Messages might not exist yet, which is okay
            }
        });
    }
    
    private void sendMessage() {
        // Check if chat is enabled
        if (!isChatEnabled) {
            showToast("Chat connection is closed. Emergency has been completed.");
            return;
        }
        
        String messageText = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            showToast("Please enter a message");
            return;
        }
        
        // Always reload emergency details before sending to ensure we have latest status
        emergencyViewModel.getEmergencyById(emergencyId, emergency -> {
            // Check if emergency is completed
            if (emergency != null && "completed".equalsIgnoreCase(emergency.getStatus())) {
                disableChat("Emergency completed - Chat connection closed");
                showToast("Cannot send message. Emergency has been completed.");
                return;
            }
            if (emergency == null) {
                showToast("Emergency not found");
                return;
            }
            
            // Re-determine other user ID from latest emergency data
            int finalOtherUserId;
            if (currentUserId == emergency.getElderlyId()) {
                // Current user is elderly, other is volunteer
                finalOtherUserId = emergency.getVolunteerId() != null ? emergency.getVolunteerId() : -1;
            } else {
                // Current user is volunteer, other is elderly
                finalOtherUserId = emergency.getElderlyId();
            }
            
            // Update otherUserId
            otherUserId = finalOtherUserId;
            
            // Check if other user is available
            if (otherUserId <= 0) {
                showToast("Waiting for connection... Please try again");
                // Reload emergency details
                loadEmergencyDetails();
                return;
            }
            
            // Create and send message with current emergency_id
            Message message = new Message(emergencyId, currentUserId, otherUserId, messageText);
            
            // Double-check emergency_id is valid
            if (message.getEmergencyId() <= 0) {
                showToast("Invalid emergency. Please try again.");
                loadEmergencyDetails();
                return;
            }
            
            messageRepository.sendMessage(message, new MessageRepository.RepositoryCallback<Long>() {
                @Override
                public void onSuccess(Long id) {
                    if (id != null && id > 0) {
                        etMessage.setText("");
                        // Wait a moment then reload to ensure message is persisted
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            loadMessages();
                        }, 300);
                        
                        // Notify the receiver (other user) about the new message
                        notifyReceiverAboutMessage(messageText);
                    } else {
                        showToast("Failed to save message. Please try again.");
                    }
                }
                
                @Override
                public void onError(Exception error) {
                    showToast("Error sending message: " + error.getMessage());
                    // Try to reload emergency and messages
                    loadEmergencyDetails();
                }
            });
        });
    }
    
    /**
     * Notify the receiver about the new message
     */
    private void notifyReceiverAboutMessage(String messageText) {
        if (otherUserId > 0) {
            // Get current user's name for notification
            userRepository.getUserById(currentUserId, new UserRepository.RepositoryCallback<User>() {
                @Override
                public void onSuccess(User sender) {
                    if (sender != null && notificationHelper != null) {
                        // Check if receiver is elderly (otherUserId == elderlyId)
                        emergencyViewModel.getEmergencyById(emergencyId, emergency -> {
                            if (emergency != null && otherUserId == emergency.getElderlyId()) {
                                // Receiver is elderly, send notification
                                notificationHelper.notifyElderlyNewMessage(
                                        emergencyId,
                                        sender.getName(),
                                        messageText
                                );
                            }
                            // If receiver is volunteer, they will see it when they open chat
                        });
                    }
                }
                
                @Override
                public void onError(Exception error) {
                    // Silently fail - notification is not critical
                }
            });
        }
    }
    
    private void startPolling() {
        if (pollHandler == null) {
            pollHandler = new Handler(Looper.getMainLooper());
        }
        if (pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                if (emergencyId > 0 && isChatEnabled) {
                    // Always check emergency status to detect when it becomes completed
                    loadEmergencyDetails();
                    loadMessages();
                } else if (!isChatEnabled) {
                    // Stop polling if chat is disabled
                    return;
                }
                if (pollHandler != null && pollRunnable != null && isChatEnabled) {
                    pollHandler.postDelayed(this, POLL_INTERVAL);
                }
            }
        };
        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL);
    }
    
    /**
     * Disable chat when emergency is completed
     */
    private void disableChat(String message) {
        isChatEnabled = false;
        
        // Disable send button and input
        if (btnSend != null) {
            btnSend.setEnabled(false);
            btnSend.setAlpha(0.5f);
        }
        if (etMessage != null) {
            etMessage.setEnabled(false);
            etMessage.setHint("Chat connection closed");
            etMessage.setAlpha(0.5f);
        }
        
        // Show connection status message
        if (tvConnectionStatus != null) {
            tvConnectionStatus.setText(message);
            tvConnectionStatus.setVisibility(View.VISIBLE);
            tvConnectionStatus.setTextColor(getResources().getColor(R.color.sos_red));
        }
        
        // Stop polling
        if (pollHandler != null && pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }
    }
    
    /**
     * Enable chat when emergency is accepted
     */
    private void enableChat() {
        isChatEnabled = true;
        
        // Enable send button and input
        if (btnSend != null) {
            btnSend.setEnabled(true);
            btnSend.setAlpha(1.0f);
        }
        if (etMessage != null) {
            etMessage.setEnabled(true);
            etMessage.setHint("Type a message...");
            etMessage.setAlpha(1.0f);
        }
        
        // Hide connection status message
        if (tvConnectionStatus != null) {
            tvConnectionStatus.setVisibility(View.GONE);
        }
    }
    
    private void scrollToBottom() {
        if (recyclerView != null && adapter != null && adapter.getItemCount() > 0) {
            recyclerView.post(() -> recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1));
        }
    }
    
    private class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Message message = messageList.get(position);
            boolean isSent = message.getSenderId() == currentUserId;
            
            holder.tvMessage.setText(message.getMessage());
            
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(message.getTimestamp())));
            
            // Set alignment and background based on sender
            ViewGroup.LayoutParams params = holder.messageContainer.getLayoutParams();
            if (params instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) params;
                if (isSent) {
                    layoutParams.gravity = android.view.Gravity.END;
                    holder.messageContainer.setBackgroundResource(R.drawable.message_sent_background);
                    holder.tvMessage.setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    layoutParams.gravity = android.view.Gravity.START;
                    holder.messageContainer.setBackgroundResource(R.drawable.message_received_background);
                    holder.tvMessage.setTextColor(getResources().getColor(R.color.text_primary));
                }
                holder.messageContainer.setLayoutParams(layoutParams);
            }
        }
        
        @Override
        public int getItemCount() {
            return messageList.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            View messageContainer;
            TextView tvMessage;
            TextView tvTime;
            
            ViewHolder(View itemView) {
                super(itemView);
                messageContainer = itemView.findViewById(R.id.messageContainer);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvTime = itemView.findViewById(R.id.tvTime);
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pollHandler != null && pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }
    }
}

