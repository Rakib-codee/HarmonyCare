package com.harmonycare.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.harmonycare.app.data.model.Emergency;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Helper class for analytics and statistics
 */
public class AnalyticsHelper {
    private static final String PREFS_NAME = "HarmonyCareAnalytics";
    private Context context;
    private SharedPreferences analyticsPrefs;
    
    public AnalyticsHelper(Context context) {
        this.context = context;
        this.analyticsPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Track emergency creation
     */
    public void trackEmergencyCreated(int emergencyId, int elderlyId) {
        String key = "emergency_created_" + emergencyId;
        analyticsPrefs.edit()
            .putLong(key, System.currentTimeMillis())
            .putInt("emergency_elderly_" + emergencyId, elderlyId)
            .apply();
    }
    
    /**
     * Track emergency acceptance
     */
    public void trackEmergencyAccepted(int emergencyId, int volunteerId, long responseTimeMs) {
        String key = "emergency_accepted_" + emergencyId;
        analyticsPrefs.edit()
            .putLong(key, System.currentTimeMillis())
            .putInt("emergency_volunteer_" + emergencyId, volunteerId)
            .putLong("emergency_response_time_" + emergencyId, responseTimeMs)
            .apply();
    }
    
    /**
     * Track emergency completion
     */
    public void trackEmergencyCompleted(int emergencyId, long completionTimeMs) {
        String key = "emergency_completed_" + emergencyId;
        analyticsPrefs.edit()
            .putLong(key, System.currentTimeMillis())
            .putLong("emergency_completion_time_" + emergencyId, completionTimeMs)
            .apply();
    }
    
    /**
     * Get average response time for a volunteer
     * @param volunteerId Volunteer ID
     * @param emergencies List of emergencies
     * @return Average response time in minutes
     */
    public double getAverageResponseTime(int volunteerId, List<Emergency> emergencies) {
        if (emergencies == null || emergencies.isEmpty()) {
            return 0;
        }
        
        long totalResponseTime = 0;
        int count = 0;
        
        for (Emergency emergency : emergencies) {
            if (emergency.getVolunteerId() != null && emergency.getVolunteerId() == volunteerId) {
                long responseTime = analyticsPrefs.getLong(
                    "emergency_response_time_" + emergency.getId(), 0);
                if (responseTime > 0) {
                    totalResponseTime += responseTime;
                    count++;
                }
            }
        }
        
        if (count == 0) {
            return 0;
        }
        
        return (totalResponseTime / (double) count) / 60000.0; // Convert to minutes
    }
    
    /**
     * Get emergency trends by time of day
     * @param emergencies List of emergencies
     * @return Map of hour (0-23) to count
     */
    public Map<Integer, Integer> getEmergenciesByHour(List<Emergency> emergencies) {
        Map<Integer, Integer> hourCounts = new HashMap<>();
        
        for (int i = 0; i < 24; i++) {
            hourCounts.put(i, 0);
        }
        
        if (emergencies != null) {
            for (Emergency emergency : emergencies) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(emergency.getTimestamp());
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                hourCounts.put(hour, hourCounts.get(hour) + 1);
            }
        }
        
        return hourCounts;
    }
    
    /**
     * Get emergency trends by day of week
     * @param emergencies List of emergencies
     * @return Map of day (1-7, Sunday=1) to count
     */
    public Map<Integer, Integer> getEmergenciesByDayOfWeek(List<Emergency> emergencies) {
        Map<Integer, Integer> dayCounts = new HashMap<>();
        
        for (int i = 1; i <= 7; i++) {
            dayCounts.put(i, 0);
        }
        
        if (emergencies != null) {
            for (Emergency emergency : emergencies) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(emergency.getTimestamp());
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                dayCounts.put(dayOfWeek, dayCounts.get(dayOfWeek) + 1);
            }
        }
        
        return dayCounts;
    }
    
    /**
     * Get completion rate for a volunteer
     * @param volunteerId Volunteer ID
     * @param emergencies List of emergencies
     * @return Completion rate as percentage (0-100)
     */
    public double getCompletionRate(int volunteerId, List<Emergency> emergencies) {
        if (emergencies == null || emergencies.isEmpty()) {
            return 0;
        }
        
        int accepted = 0;
        int completed = 0;
        
        for (Emergency emergency : emergencies) {
            if (emergency.getVolunteerId() != null && emergency.getVolunteerId() == volunteerId) {
                String status = emergency.getStatus();
                if ("accepted".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
                    accepted++;
                    if ("completed".equalsIgnoreCase(status)) {
                        completed++;
                    }
                }
            }
        }
        
        if (accepted == 0) {
            return 0;
        }
        
        return (completed / (double) accepted) * 100.0;
    }
    
    /**
     * Get most active time period
     * @param emergencies List of emergencies
     * @return String describing most active period
     */
    public String getMostActivePeriod(List<Emergency> emergencies) {
        Map<Integer, Integer> hourCounts = getEmergenciesByHour(emergencies);
        
        int maxCount = 0;
        int maxHour = 0;
        
        for (Map.Entry<Integer, Integer> entry : hourCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxHour = entry.getKey();
            }
        }
        
        if (maxCount == 0) {
            return "No data";
        }
        
        String period;
        if (maxHour >= 6 && maxHour < 12) {
            period = "Morning";
        } else if (maxHour >= 12 && maxHour < 18) {
            period = "Afternoon";
        } else if (maxHour >= 18 && maxHour < 22) {
            period = "Evening";
        } else {
            period = "Night";
        }
        
        return period + " (" + maxHour + ":00)";
    }
}

