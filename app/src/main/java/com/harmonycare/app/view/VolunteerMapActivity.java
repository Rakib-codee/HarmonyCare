package com.harmonycare.app.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import com.harmonycare.app.R;
import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.model.User;
import com.harmonycare.app.data.repository.UserRepository;
import com.harmonycare.app.util.LocationHelper;
import com.harmonycare.app.viewmodel.AuthViewModel;
import com.harmonycare.app.viewmodel.EmergencyViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Map Activity for Volunteers to view emergency locations using OpenStreetMap
 */
public class VolunteerMapActivity extends BaseActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;
    
    private MapView mapView;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private LocationHelper locationHelper;
    private EmergencyViewModel emergencyViewModel;
    private AuthViewModel authViewModel;
    private UserRepository userRepository;
    private Location currentLocation;
    private Map<Marker, Emergency> markerEmergencyMap = new HashMap<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_map);
        
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        emergencyViewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);
        userRepository = new UserRepository(this);
        locationHelper = new LocationHelper(this);
        
        try {
            mapView = findViewById(R.id.map);
            if (mapView != null) {
                initializeMap();
            } else {
                android.util.Log.e("VolunteerMapActivity", "MapView not found in layout");
                Toast.makeText(this, "Error loading map. Please try again.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } catch (Exception e) {
            android.util.Log.e("VolunteerMapActivity", "Error initializing map", e);
            Toast.makeText(this, "Error loading map. Please try again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        checkLocationPermission();
        loadEmergencies();
    }
    
    private void initializeMap() {
        // Set tile source (OpenStreetMap)
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        
        // Enable multi-touch controls
        mapView.setMultiTouchControls(true);
        
        // Get map controller
        mapController = mapView.getController();
        mapController.setZoom(12.0);
        
        // Enable zoom controls
        mapView.setBuiltInZoomControls(true);
        mapView.setZoomRounding(true);
        
        // Enable my location overlay
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            setupMyLocationOverlay();
            getCurrentLocation();
        }
    }
    
    private void setupMyLocationOverlay() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);
    }
    
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mapView != null && myLocationOverlay == null) {
                    setupMyLocationOverlay();
                }
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required for map view", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void getCurrentLocation() {
        if (locationHelper.hasLocationPermission()) {
            currentLocation = locationHelper.getLastKnownLocation();
            if (currentLocation == null) {
                locationHelper.requestLocationUpdates(location -> {
                    currentLocation = location;
                    if (mapController != null && currentLocation != null) {
                        GeoPoint myLocation = new GeoPoint(
                                currentLocation.getLatitude(),
                                currentLocation.getLongitude());
                        mapController.setCenter(myLocation);
                        mapController.setZoom(12.0);
                        locationHelper.stopLocationUpdates();
                    }
                });
            } else if (mapController != null) {
                GeoPoint myLocation = new GeoPoint(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude());
                mapController.setCenter(myLocation);
                mapController.setZoom(12.0);
            }
        }
    }
    
    private void loadEmergencies() {
        emergencyViewModel.loadActiveAndAcceptedEmergencies();
        emergencyViewModel.getActiveEmergencies().observe(this, emergencies -> {
            if (emergencies != null && mapView != null) {
                updateMapMarkers(emergencies);
            }
        });
    }
    
    private void updateMapMarkers(List<Emergency> emergencies) {
        // Clear existing markers
        mapView.getOverlays().clear();
        markerEmergencyMap.clear();
        
        // Re-add my location overlay if it exists
        if (myLocationOverlay != null) {
            mapView.getOverlays().add(myLocationOverlay);
        }
        
        if (emergencies == null || emergencies.isEmpty()) {
            return;
        }
        
        // Add markers for each emergency
        for (Emergency emergency : emergencies) {
            try {
                if (emergency == null) continue;
                
                GeoPoint location = new GeoPoint(emergency.getLatitude(), emergency.getLongitude());
                
                // Get user name for marker title
                User user = userRepository.getUserFromCache(emergency.getElderlyId());
                String title = user != null ? user.getName() : "Emergency";
                
                // Create marker
                Marker marker = new Marker(mapView);
                marker.setPosition(location);
                marker.setTitle(title);
                marker.setSnippet("Status: " + (emergency.getStatus() != null ? emergency.getStatus().toUpperCase() : "UNKNOWN"));
                
                // Set marker icon based on status
                if ("active".equalsIgnoreCase(emergency.getStatus())) {
                    marker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
                } else if ("accepted".equalsIgnoreCase(emergency.getStatus())) {
                    marker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_directions));
                } else {
                    marker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_compass));
                }
                
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                
                // Add marker to map
                mapView.getOverlays().add(marker);
                markerEmergencyMap.put(marker, emergency);
                
                // Set click listener for this marker
                marker.setOnMarkerClickListener((markerItem, mapView) -> {
                    Emergency emergencyItem = markerEmergencyMap.get(markerItem);
                    if (emergencyItem != null) {
                        android.content.Intent intent = new android.content.Intent(
                                VolunteerMapActivity.this, EmergencyDetailsActivity.class);
                        intent.putExtra("emergency_id", emergencyItem.getId());
                        startActivity(intent);
                        return true;
                    }
                    return false;
                });
            } catch (Exception e) {
                android.util.Log.e("VolunteerMapActivity", "Error adding marker", e);
            }
        }
        
        // Center map on emergencies if current location not available
        if (currentLocation == null && !emergencies.isEmpty()) {
            Emergency firstEmergency = emergencies.get(0);
            GeoPoint center = new GeoPoint(firstEmergency.getLatitude(), firstEmergency.getLongitude());
            mapController.setCenter(center);
            mapController.setZoom(12.0);
        }
        
        // Refresh map
        mapView.invalidate();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationHelper != null) {
            locationHelper.stopLocationUpdates();
        }
    }
}
