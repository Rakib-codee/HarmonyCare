package com.harmonycare.app.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

/**
 * Helper class for haptic feedback and vibration
 */
public class HapticHelper {
    private static final long VIBRATION_SHORT = 50; // 50ms
    private static final long VIBRATION_MEDIUM = 100; // 100ms
    private static final long VIBRATION_LONG = 200; // 200ms
    private static final long VIBRATION_SOS_PRESS = 50; // Short vibration on press
    private static final long VIBRATION_SOS_TICK = 30; // Tick vibration during countdown
    private static final long VIBRATION_SOS_COMPLETE = 300; // Long vibration on completion
    
    /**
     * Vibrate with short duration
     * @param context Context
     */
    public static void vibrateShort(Context context) {
        vibrate(context, VIBRATION_SHORT);
    }
    
    /**
     * Vibrate with medium duration
     * @param context Context
     */
    public static void vibrateMedium(Context context) {
        vibrate(context, VIBRATION_MEDIUM);
    }
    
    /**
     * Vibrate with long duration
     * @param context Context
     */
    public static void vibrateLong(Context context) {
        vibrate(context, VIBRATION_LONG);
    }
    
    /**
     * Vibrate for SOS button press
     * @param context Context
     */
    public static void vibrateSOSPress(Context context) {
        vibrate(context, VIBRATION_SOS_PRESS);
    }
    
    /**
     * Vibrate for SOS countdown tick
     * @param context Context
     */
    public static void vibrateSOSTick(Context context) {
        vibrate(context, VIBRATION_SOS_TICK);
    }
    
    /**
     * Vibrate for SOS completion
     * @param context Context
     */
    public static void vibrateSOSComplete(Context context) {
        vibrate(context, VIBRATION_SOS_COMPLETE);
    }
    
    /**
     * Vibrate with custom pattern
     * @param context Context
     * @param pattern Vibration pattern (timings in milliseconds)
     * @param repeat Repeat index (-1 for no repeat)
     */
    public static void vibratePattern(Context context, long[] pattern, int repeat) {
        Vibrator vibrator = getVibrator(context);
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createWaveform(pattern, repeat);
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(pattern, repeat);
            }
        } catch (Exception e) {
            android.util.Log.e("HapticHelper", "Error vibrating", e);
        }
    }
    
    /**
     * Vibrate with duration
     * @param context Context
     * @param duration Duration in milliseconds
     */
    private static void vibrate(Context context, long duration) {
        Vibrator vibrator = getVibrator(context);
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(duration);
            }
        } catch (Exception e) {
            android.util.Log.e("HapticHelper", "Error vibrating", e);
        }
    }
    
    /**
     * Get vibrator instance
     * @param context Context
     * @return Vibrator instance or null
     */
    private static Vibrator getVibrator(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                return vibratorManager != null ? vibratorManager.getDefaultVibrator() : null;
            } else {
                return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }
        } catch (Exception e) {
            android.util.Log.e("HapticHelper", "Error getting vibrator", e);
            return null;
        }
    }
}

