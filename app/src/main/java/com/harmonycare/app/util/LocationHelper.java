package com.harmonycare.app.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;

/**
 * Helper class for location services
 */
public class LocationHelper {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Context context;
    
    public LocationHelper(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }
    
    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    
    public Location getLastKnownLocation() {
        if (!hasLocationPermission()) {
            return null;
        }
        
        try {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            return lastKnownLocation;
        } catch (SecurityException e) {
            return null;
        }
    }
    
    public void requestLocationUpdates(LocationListener listener) {
        if (!hasLocationPermission()) {
            return;
        }
        
        this.locationListener = listener;
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000,
                        1,
                        listener
                );
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000,
                        1,
                        listener
                );
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    
    public void stopLocationUpdates() {
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}

