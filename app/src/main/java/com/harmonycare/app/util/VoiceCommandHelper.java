package com.harmonycare.app.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Helper class for voice command recognition
 */
public class VoiceCommandHelper {
    private static final String TAG = "VoiceCommandHelper";
    private SpeechRecognizer speechRecognizer;
    private Context context;
    private VoiceCommandListener listener;
    private boolean isListening = false;
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 3;
    
    // Keywords to detect
    private static final String[] SOS_KEYWORDS = {"sos", "help", "emergency", "বিপদ", "সাহায্য", "জরুরি"};
    
    public interface VoiceCommandListener {
        void onSOSDetected();
        void onError(String error);
    }
    
    public VoiceCommandHelper(Context context) {
        this.context = context;
    }
    
    public void setListener(VoiceCommandListener listener) {
        this.listener = listener;
    }
    
    public boolean isAvailable() {
        boolean available = SpeechRecognizer.isRecognitionAvailable(context);
        if (!available) {
            Log.w(TAG, "Speech recognition not available on this device");
        }
        return available;
    }
    
    public void startListening() {
        if (!isAvailable()) {
            if (listener != null) {
                listener.onError("Speech recognition not available");
            }
            return;
        }
        
        // Clean up any existing recognizer first
        if (speechRecognizer != null) {
            try {
                speechRecognizer.cancel();
                speechRecognizer.destroy();
            } catch (Exception e) {
                Log.w(TAG, "Error cleaning up existing recognizer", e);
            }
            speechRecognizer = null;
        }
        
        isListening = false;
        retryCount = 0; // Reset retry count for new listening session
        
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            if (speechRecognizer == null) {
                Log.e(TAG, "Failed to create SpeechRecognizer");
                if (listener != null) {
                    listener.onError("Failed to initialize speech recognition");
                }
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception creating SpeechRecognizer", e);
            if (listener != null) {
                listener.onError("Failed to create speech recognizer: " + e.getMessage());
            }
            return;
        }
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                isListening = true;
            }
            
            @Override
            public void onBeginningOfSpeech() {
                // Speech started
            }
            
            @Override
            public void onRmsChanged(float rmsdB) {
                // Volume level changed
            }
            
            @Override
            public void onBufferReceived(byte[] buffer) {
                // Audio buffer received
            }
            
            @Override
            public void onEndOfSpeech() {
                isListening = false;
            }
            
            @Override
            public void onError(int error) {
                isListening = false;
                String errorMessage = getErrorText(error);
                Log.e(TAG, "Speech recognition error code: " + error + " - " + errorMessage);
                
                // Clean up current recognizer
                if (speechRecognizer != null) {
                    try {
                        speechRecognizer.cancel();
                    } catch (Exception e) {
                        Log.w(TAG, "Error canceling speech recognizer", e);
                    }
                    try {
                        speechRecognizer.destroy();
                    } catch (Exception e) {
                        Log.w(TAG, "Error destroying speech recognizer", e);
                    }
                    speechRecognizer = null;
                }
                
                // Don't retry on unknown errors (like error code 11) or server errors
                // Only retry on recoverable errors
                boolean shouldRetry = (error == SpeechRecognizer.ERROR_NO_MATCH || 
                                      error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
                                      error == SpeechRecognizer.ERROR_NETWORK ||
                                      error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT) 
                                      && retryCount < MAX_RETRY_COUNT;
                
                // Don't retry on ERROR_CLIENT or unknown errors (like 11)
                if (error == SpeechRecognizer.ERROR_CLIENT || error == 11 || error > 10) {
                    shouldRetry = false;
                    Log.w(TAG, "Critical error detected, stopping voice recognition");
                }
                
                if (shouldRetry) {
                    retryCount++;
                    Log.d(TAG, "Retrying speech recognition (attempt " + retryCount + "/" + MAX_RETRY_COUNT + ")");
                    
                    // Restart after a delay
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        if (context != null && retryCount < MAX_RETRY_COUNT) {
                            try {
                                startListening();
                            } catch (Exception e) {
                                Log.e(TAG, "Error restarting speech recognition", e);
                                retryCount = MAX_RETRY_COUNT; // Stop retrying
                            }
                        }
                    }, 3000); // Longer delay
                } else {
                    // Stop retrying - either max retries reached or critical error
                    retryCount = 0; // Reset for next manual start
                    Log.w(TAG, "Stopping voice recognition after error code: " + error);
                    
                    if (listener != null) {
                        // Report error to listener
                        listener.onError(errorMessage + " (Error code: " + error + ")");
                    }
                }
            }
            
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0).toLowerCase();
                    Log.d(TAG, "Recognized: " + spokenText);
                    
                    // Check for SOS keywords
                    for (String keyword : SOS_KEYWORDS) {
                        if (spokenText.contains(keyword.toLowerCase())) {
                            if (listener != null) {
                                listener.onSOSDetected();
                            }
                            return;
                        }
                    }
                }
                
                // Restart listening
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (speechRecognizer != null) {
                        startListening();
                    }
                }, 1000);
            }
            
            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0).toLowerCase();
                    
                    // Check for SOS keywords in partial results
                    for (String keyword : SOS_KEYWORDS) {
                        if (spokenText.contains(keyword.toLowerCase())) {
                            if (listener != null) {
                                listener.onSOSDetected();
                            }
                            return;
                        }
                    }
                }
            }
            
            @Override
            public void onEvent(int eventType, Bundle params) {
                // Event occurred
            }
        });
        
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        
        // Prefer offline recognition if available
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        }
        
        try {
            speechRecognizer.startListening(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting speech recognition", e);
            if (listener != null) {
                listener.onError("Failed to start voice recognition: " + e.getMessage());
            }
        }
    }
    
    public void stopListening() {
        if (speechRecognizer != null) {
            try {
                speechRecognizer.stopListening();
            } catch (Exception e) {
                Log.w(TAG, "Error stopping listening", e);
            }
            try {
                speechRecognizer.cancel();
            } catch (Exception e) {
                Log.w(TAG, "Error canceling recognizer", e);
            }
            try {
                speechRecognizer.destroy();
            } catch (Exception e) {
                Log.w(TAG, "Error destroying recognizer", e);
            }
            speechRecognizer = null;
        }
        isListening = false;
        retryCount = 0;
    }
    
    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No match found";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "RecognitionService busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Unknown error (code: " + errorCode + ")";
        }
    }
    
    public void destroy() {
        stopListening();
    }
}

