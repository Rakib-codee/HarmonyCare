package com.harmonycare.app.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Helper class for Text-to-Speech functionality
 */
public class TTSHelper {
    private TextToSpeech textToSpeech;
    private boolean isInitialized = false;
    
    public TTSHelper(Context context) {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.getDefault());
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Language not supported, use default
                } else {
                    isInitialized = true;
                }
            }
        });
    }
    
    public void speak(String text) {
        if (isInitialized && text != null && !text.isEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
    
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}

