package com.harmonycare.app.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;

import com.harmonycare.app.R;
import com.harmonycare.app.data.model.Emergency;
import com.harmonycare.app.data.model.User;
import com.harmonycare.app.data.repository.UserRepository;
import com.harmonycare.app.util.DistanceCalculator;
import com.harmonycare.app.util.LocationHelper;
import com.harmonycare.app.viewmodel.AuthViewModel;
import com.harmonycare.app.viewmodel.EmergencyViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Map Activity for Volunteers to view emergency locations using AMap (高德地图)
 */
public class VolunteerMapActivity extends BaseActivity implements LocationSource.OnLocationChangedListener, AMapNaviListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;
    
    private MapView mapView;
    private AMap aMap;
    private LocationSource.OnLocationChangedListener locationSourceListener;
    private LocationHelper locationHelper;
    private EmergencyViewModel emergencyViewModel;
    private AuthViewModel authViewModel;
    private UserRepository userRepository;
    private Location currentLocation;
    private Map<Marker, Emergency> markerEmergencyMap = new HashMap<>();
    
    // Navigation components
    private AMapNavi aMapNavi;
    private Thread.UncaughtExceptionHandler defaultExceptionHandler;
    private Button btnStartNavigation;
    private Button btnStopNavigation;
    private Emergency selectedEmergency;
    private boolean isNavigating = false;
    private RouteOverLay routeOverLay;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Store default exception handler
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        
        // Set up uncaught exception handler for OpenGL/EGL errors
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            if (ex instanceof RuntimeException) {
                String message = ex.getMessage();
                StackTraceElement[] stack = ex.getStackTrace();
                boolean isEGLError = false;
                
                // Check if it's an EGL/OpenGL error
                if (message != null && (message.contains("EGL") || 
                        message.contains("OpenGL") || 
                        message.contains("createContext") ||
                        message.contains("GLThread"))) {
                    isEGLError = true;
                } else if (stack != null) {
                    // Also check stack trace
                    for (StackTraceElement element : stack) {
                        String className = element.getClassName();
                        if (className != null && (className.contains("GLSurfaceView") || 
                                className.contains("EGL") || 
                                className.contains("GLThread"))) {
                            isEGLError = true;
                            break;
                        }
                    }
                }
                
                if (isEGLError) {
                    android.util.Log.e("VolunteerMapActivity", "Caught OpenGL/EGL exception: " + message, ex);
                    // Show user-friendly message on UI thread
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        Toast.makeText(VolunteerMapActivity.this, 
                            "Map rendering error. Please use a physical device or emulator with GPU acceleration enabled.", 
                            Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(VolunteerMapActivity.this, 
                                "Map rendering error. Please use a physical device or emulator with GPU acceleration enabled.", 
                                Toast.LENGTH_LONG).show();
                            finish();
                        });
                    }
                    return;
                }
            }
            // For other exceptions, use default handler
            if (defaultExceptionHandler != null) {
                defaultExceptionHandler.uncaughtException(thread, ex);
            }
        });
        
        setContentView(R.layout.activity_volunteer_map);
        
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        emergencyViewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);
        userRepository = new UserRepository(this);
        locationHelper = new LocationHelper(this);
        
        // Initialize map FIRST (this is the main feature)
        try {
            mapView = findViewById(R.id.map);
            if (mapView != null) {
                // Wrap onCreate in try-catch for EGL errors
                try {
                    mapView.onCreate(savedInstanceState);
                    initializeMap();
                } catch (RuntimeException e) {
                    // Handle OpenGL/EGL errors gracefully
                    if (e.getMessage() != null && (e.getMessage().contains("EGL") || 
                            e.getMessage().contains("OpenGL") || 
                            e.getMessage().contains("createContext"))) {
                        android.util.Log.e("VolunteerMapActivity", "OpenGL/EGL error (common in emulators): " + e.getMessage());
                        Toast.makeText(this, "Map rendering may not work on this emulator. Please use a physical device or emulator with GPU acceleration enabled.", Toast.LENGTH_LONG).show();
                        // Don't finish - map might still partially work
                    } else {
                        throw e; // Re-throw if it's a different error
                    }
                }
            } else {
                android.util.Log.e("VolunteerMapActivity", "MapView not found in layout");
                Toast.makeText(this, "Error loading map. Please try again.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } catch (Exception e) {
            android.util.Log.e("VolunteerMapActivity", "Error initializing map", e);
            Toast.makeText(this, "Error loading map: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Don't finish immediately - let user see the error
        }
        
        // Initialize navigation buttons
        btnStartNavigation = findViewById(R.id.btnStartNavigation);
        btnStopNavigation = findViewById(R.id.btnStopNavigation);
        
        // Initialize AMap Navigation AFTER map (optional - map will work without it)
        // Navigation requires native libraries which may not be available on all devices/emulators
        // Initialize on main thread but catch all errors so map still works
        initializeNavigation();
        
        // Set button click listeners
        if (btnStartNavigation != null) {
            btnStartNavigation.setOnClickListener(v -> startNavigationToSelectedEmergency());
        }
        if (btnStopNavigation != null) {
            btnStopNavigation.setOnClickListener(v -> stopNavigation());
        }
        
        checkLocationPermission();
        loadEmergencies();
        
        // Check if opened from EmergencyDetailsActivity with specific emergency to focus
        handleFocusEmergency();
    }
    
    /**
     * Handle focusing on a specific emergency when opened from EmergencyDetailsActivity
     */
    private void handleFocusEmergency() {
        boolean focusEmergency = getIntent().getBooleanExtra("focus_emergency", false);
        int focusEmergencyId = getIntent().getIntExtra("emergency_id", -1);
        
        if (focusEmergency && focusEmergencyId > 0) {
            // Wait for map to initialize, then focus on this emergency
            if (mapView != null) {
                mapView.post(() -> {
                    emergencyViewModel.getEmergencyById(focusEmergencyId, emergency -> {
                        if (emergency != null && aMap != null) {
                            // Center map on this emergency location
                            LatLng location = new LatLng(emergency.getLatitude(), emergency.getLongitude());
                            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16f));
                            
                            // Select this emergency for navigation
                            selectedEmergency = emergency;
                            
                            // Show navigation button if available
                            if (btnStartNavigation != null && aMapNavi != null && currentLocation != null) {
                                btnStartNavigation.setVisibility(View.VISIBLE);
                                User user = userRepository.getUserFromCache(emergency.getElderlyId());
                                btnStartNavigation.setText("Navigate to " + (user != null ? user.getName() : "Emergency"));
                            }
                            
                            android.util.Log.d("VolunteerMapActivity", "Focused on emergency ID: " + focusEmergencyId);
                        }
                    });
                });
            }
        }
    }
    
    /**
     * Initialize AMap Navigation (optional feature)
     * If native libraries are missing, navigation will be disabled but map will still work
     */
    private void initializeNavigation() {
        try {
            aMapNavi = AMapNavi.getInstance(this);
            if (aMapNavi != null) {
                try {
                    aMapNavi.addAMapNaviListener(this);
                    // Try to set voice guidance (may fail if native libs missing)
                    try {
                        aMapNavi.setUseInnerVoice(true);
                    } catch (UnsatisfiedLinkError | NoSuchMethodError e) {
                        android.util.Log.w("VolunteerMapActivity", "Voice guidance not available (native libs missing)", e);
                        // Continue without voice guidance
                    }
                    android.util.Log.d("VolunteerMapActivity", "Navigation initialized successfully");
                } catch (Exception e) {
                    android.util.Log.w("VolunteerMapActivity", "Navigation partially initialized", e);
                    aMapNavi = null; // Mark as unavailable
                }
            }
        } catch (UnsatisfiedLinkError e) {
            // Native libraries not found - navigation unavailable
            android.util.Log.w("VolunteerMapActivity", "Navigation unavailable: Native libraries not found. Map will work without navigation.", e);
            aMapNavi = null;
            // Hide navigation buttons since navigation is not available
            if (btnStartNavigation != null) {
                btnStartNavigation.setVisibility(View.GONE);
            }
            if (btnStopNavigation != null) {
                btnStopNavigation.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            android.util.Log.w("VolunteerMapActivity", "Navigation initialization failed. Map will work without navigation.", e);
            aMapNavi = null;
            // Hide navigation buttons
            if (btnStartNavigation != null) {
                btnStartNavigation.setVisibility(View.GONE);
            }
            if (btnStopNavigation != null) {
                btnStopNavigation.setVisibility(View.GONE);
            }
        }
    }
    
    private void initializeMap() {
        try {
            if (aMap == null && mapView != null) {
                aMap = mapView.getMap();
            }
            
            if (aMap == null) {
                android.util.Log.e("VolunteerMapActivity", "Failed to get AMap instance");
                Toast.makeText(this, "Failed to initialize map. Please try again.", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Set map type (normal, satellite, night)
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        
        // Enable zoom controls
        aMap.getUiSettings().setZoomControlsEnabled(true);
        aMap.getUiSettings().setCompassEnabled(true);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.getUiSettings().setScaleControlsEnabled(true);
        
        // Enable my location
        setupMyLocationStyle();
        
        // Set location source
        aMap.setLocationSource(new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener listener) {
                locationSourceListener = listener;
                if (ContextCompat.checkSelfPermission(VolunteerMapActivity.this, 
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                }
            }
            
            @Override
            public void deactivate() {
                locationSourceListener = null;
            }
        });
        
        aMap.setMyLocationEnabled(true);
        } catch (Exception e) {
            android.util.Log.e("VolunteerMapActivity", "Error in initializeMap", e);
            Toast.makeText(this, "Error initializing map features: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Map might still be partially functional, so don't finish activity
        }
    }
    
    private void setupMyLocationStyle() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // Set my location icon style
        myLocationStyle.myLocationIcon(com.amap.api.maps.model.BitmapDescriptorFactory
                .defaultMarker(com.amap.api.maps.model.BitmapDescriptorFactory.HUE_BLUE));
        // Set stroke color
        myLocationStyle.strokeColor(android.graphics.Color.argb(0, 0, 0, 0));
        // Set fill color
        myLocationStyle.radiusFillColor(android.graphics.Color.argb(0, 0, 0, 0));
        // Set stroke width
        myLocationStyle.strokeWidth(0);
        // Set location update interval
        myLocationStyle.interval(2000);
        aMap.setMyLocationStyle(myLocationStyle);
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
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required for map view", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    public void onLocationChanged(android.location.Location location) {
        if (location != null && aMap != null) {
            currentLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (locationSourceListener != null) {
                locationSourceListener.onLocationChanged(location);
            }
            // Center map on current location (only first time)
            if (markerEmergencyMap.isEmpty()) {
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
            }
        }
    }
    
    private void getCurrentLocation() {
        if (locationHelper.hasLocationPermission()) {
            currentLocation = locationHelper.getLastKnownLocation();
            if (currentLocation == null) {
                locationHelper.requestLocationUpdates(location -> {
                    currentLocation = location;
                    if (aMap != null && currentLocation != null) {
                        LatLng latLng = new LatLng(
                                currentLocation.getLatitude(),
                                currentLocation.getLongitude());
                        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                        locationHelper.stopLocationUpdates();
                    }
                });
            } else if (aMap != null) {
                LatLng latLng = new LatLng(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude());
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
            }
        }
    }
    
    private void loadEmergencies() {
        emergencyViewModel.loadActiveAndAcceptedEmergencies();
        emergencyViewModel.getActiveEmergencies().observe(this, emergencies -> {
            if (emergencies != null && aMap != null) {
                updateMapMarkers(emergencies);
            }
        });
    }
    
    private void updateMapMarkers(List<Emergency> emergencies) {
        // Clear existing markers
        aMap.clear();
        markerEmergencyMap.clear();
        
        if (emergencies == null || emergencies.isEmpty()) {
            return;
        }
        
        // Add markers for each emergency
        for (Emergency emergency : emergencies) {
            try {
                if (emergency == null) continue;
                
                LatLng location = new LatLng(emergency.getLatitude(), emergency.getLongitude());
                
                // Get user name for marker title
                User user = userRepository.getUserFromCache(emergency.getElderlyId());
                String title = user != null ? user.getName() : "Emergency";
                
                // Calculate distance and ETA if current location available
                String snippet = "Status: " + (emergency.getStatus() != null ? emergency.getStatus().toUpperCase() : "UNKNOWN");
                if (currentLocation != null) {
                    double distance = DistanceCalculator.calculateDistance(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude(),
                        emergency.getLatitude(),
                        emergency.getLongitude()
                    );
                    String distanceStr = DistanceCalculator.formatDistance(distance);
                    // Estimate ETA (assuming average speed of 30 km/h in city)
                    double estimatedTimeHours = distance / 30.0;
                    int estimatedMinutes = (int) (estimatedTimeHours * 60);
                    String eta = estimatedMinutes > 0 ? estimatedMinutes + " min" : "< 1 min";
                    snippet += "\nDistance: " + distanceStr + " | ETA: ~" + eta;
                }
                
                // Create marker options
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(location);
                markerOptions.title(title);
                markerOptions.snippet(snippet);
                
                // Set marker icon based on status
                if ("active".equalsIgnoreCase(emergency.getStatus())) {
                    markerOptions.icon(com.amap.api.maps.model.BitmapDescriptorFactory
                            .defaultMarker(com.amap.api.maps.model.BitmapDescriptorFactory.HUE_RED));
                } else if ("accepted".equalsIgnoreCase(emergency.getStatus())) {
                    markerOptions.icon(com.amap.api.maps.model.BitmapDescriptorFactory
                            .defaultMarker(com.amap.api.maps.model.BitmapDescriptorFactory.HUE_ORANGE));
                } else {
                    markerOptions.icon(com.amap.api.maps.model.BitmapDescriptorFactory
                            .defaultMarker(com.amap.api.maps.model.BitmapDescriptorFactory.HUE_BLUE));
                }
                
                // Add marker to map
                Marker marker = aMap.addMarker(markerOptions);
                markerEmergencyMap.put(marker, emergency);
            } catch (Exception e) {
                android.util.Log.e("VolunteerMapActivity", "Error adding marker", e);
            }
        }
        
        // Set marker click listener
        aMap.setOnMarkerClickListener(marker -> {
            Emergency emergency = markerEmergencyMap.get(marker);
            if (emergency != null) {
                selectedEmergency = emergency;
                // Show navigation button when emergency is selected and location is available
                // Only show if navigation is actually available
                if (btnStartNavigation != null && aMapNavi != null) {
                    if (currentLocation != null) {
                        btnStartNavigation.setVisibility(View.VISIBLE);
                        // Get user name for button text
                        User user = userRepository.getUserFromCache(emergency.getElderlyId());
                        btnStartNavigation.setText("Navigate to " + (user != null ? user.getName() : "Emergency"));
                    } else {
                        btnStartNavigation.setVisibility(View.GONE);
                    }
                } else if (btnStartNavigation != null) {
                    // Navigation not available - keep button hidden
                    btnStartNavigation.setVisibility(View.GONE);
                }
                // Show emergency details dialog or navigate
                android.content.Intent intent = new android.content.Intent(
                        VolunteerMapActivity.this, EmergencyDetailsActivity.class);
                intent.putExtra("emergency_id", emergency.getId());
                startActivity(intent);
                return true;
            }
            return false;
        });
        
        // Center map on emergencies if current location not available
        if (currentLocation == null && !emergencies.isEmpty()) {
            Emergency firstEmergency = emergencies.get(0);
            LatLng center = new LatLng(firstEmergency.getLatitude(), firstEmergency.getLongitude());
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 12f));
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            try {
                mapView.onResume();
            } catch (RuntimeException e) {
                // Handle OpenGL/EGL errors in onResume
                if (e.getMessage() != null && (e.getMessage().contains("EGL") || 
                        e.getMessage().contains("OpenGL") || 
                        e.getMessage().contains("createContext"))) {
                    android.util.Log.e("VolunteerMapActivity", "OpenGL/EGL error in onResume: " + e.getMessage());
                    // Don't crash - just log the error
                } else {
                    throw e;
                }
            }
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            try {
                mapView.onPause();
            } catch (RuntimeException e) {
                android.util.Log.e("VolunteerMapActivity", "Error in onPause", e);
            }
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }
    
    private void startNavigationToSelectedEmergency() {
        if (aMapNavi == null) {
            Toast.makeText(this, "Navigation is not available on this device. Map viewing is still available.", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (selectedEmergency == null || currentLocation == null) {
            Toast.makeText(this, "Please select an emergency and ensure location is available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Get start and end points
            NaviLatLng startPoint = new NaviLatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            NaviLatLng endPoint = new NaviLatLng(selectedEmergency.getLatitude(), selectedEmergency.getLongitude());
            
            // Calculate route - use default driving strategy (0)
            // AMapNavi.DrivingDefault = 0, AMapNavi.DrivingSaveMoney = 1, etc.
            aMapNavi.calculateDriveRoute(
                java.util.Arrays.asList(startPoint),
                java.util.Arrays.asList(endPoint),
                null,
                0  // Default driving strategy
            );
            
            Toast.makeText(this, "Calculating route...", Toast.LENGTH_SHORT).show();
        } catch (UnsatisfiedLinkError e) {
            android.util.Log.e("VolunteerMapActivity", "Navigation native libraries not available", e);
            Toast.makeText(this, "Navigation is not available on this device. Native libraries missing.", Toast.LENGTH_LONG).show();
            // Disable navigation
            aMapNavi = null;
            if (btnStartNavigation != null) {
                btnStartNavigation.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            android.util.Log.e("VolunteerMapActivity", "Error starting navigation", e);
            Toast.makeText(this, "Error starting navigation: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void stopNavigation() {
        if (aMapNavi != null) {
            try {
                aMapNavi.stopNavi();
                aMapNavi.destroy();
                isNavigating = false;
                
                // Hide navigation buttons
                if (btnStartNavigation != null) {
                    btnStartNavigation.setVisibility(View.GONE);
                }
                if (btnStopNavigation != null) {
                    btnStopNavigation.setVisibility(View.GONE);
                }
                
                // Clear route overlay
                if (routeOverLay != null && aMap != null) {
                    routeOverLay.removeFromMap();
                }
                
                Toast.makeText(this, "Navigation stopped", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                android.util.Log.e("VolunteerMapActivity", "Error stopping navigation", e);
            }
        }
    }
    
    // AMapNaviListener implementation
    @Override
    public void onInitNaviFailure() {
        Toast.makeText(this, "Navigation initialization failed", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onInitNaviSuccess() {
        // Navigation initialized successfully
    }
    
    @Override
    public void onStartNavi(int type) {
        isNavigating = true;
        if (btnStartNavigation != null) {
            btnStartNavigation.setVisibility(View.GONE);
        }
        if (btnStopNavigation != null) {
            btnStopNavigation.setVisibility(View.VISIBLE);
        }
        // Center map on route
        if (aMapNavi != null && aMap != null) {
            AMapNaviPath naviPath = aMapNavi.getNaviPath();
            if (naviPath != null && currentLocation != null) {
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));
            }
        }
        Toast.makeText(this, "Navigation started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    // Overloaded method for different SDK versions
    @Override
    public void onCalculateRouteSuccess(int[] routeIds) {
        if (aMapNavi != null && routeIds != null && routeIds.length > 0) {
            startNavigationWithRoute();
        }
    }
    
    @Override
    public void onCalculateRouteSuccess(com.amap.api.navi.model.AMapCalcRouteResult result) {
        if (aMapNavi != null && result != null && result.getRouteid() != null && result.getRouteid().length > 0) {
            startNavigationWithRoute();
        }
    }
    
    private void startNavigationWithRoute() {
        try {
            // Get the calculated route
            AMapNaviPath naviPath = aMapNavi.getNaviPath();
            if (naviPath != null) {
                // Draw route on map
                if (routeOverLay == null) {
                    routeOverLay = new RouteOverLay(aMap, naviPath, this);
                    routeOverLay.addToMap();
                } else {
                    // Remove old route and add new one
                    routeOverLay.removeFromMap();
                    routeOverLay = new RouteOverLay(aMap, naviPath, this);
                    routeOverLay.addToMap();
                }
                
                // Start navigation - use GPS mode (0) or Emulator mode (1)
                // AMapNavi.GPSNaviMode = 0, AMapNavi.EmulatorNaviMode = 1
                try {
                    // Try GPS mode first (0)
                    aMapNavi.startNavi(0);
                } catch (Exception e) {
                    android.util.Log.e("VolunteerMapActivity", "Error starting navigation", e);
                    Toast.makeText(this, "Error starting navigation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            android.util.Log.e("VolunteerMapActivity", "Error processing route", e);
            Toast.makeText(this, "Error processing route", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onCalculateRouteFailure(com.amap.api.navi.model.AMapCalcRouteResult result) {
        String errorMsg = "Route calculation failed";
        if (result != null) {
            errorMsg += ". Error code: " + result.getErrorCode();
        }
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
    }
    
    // Note: onStopSpeaking may not be in all SDK versions
    // If compilation fails, remove @Override
    public void onStopSpeaking() {
        // Navigation voice stopped
    }
    
    @Override
    public void onReCalculateRouteForYaw() {
        // Recalculating route due to deviation
    }
    
    @Override
    public void onReCalculateRouteForTrafficJam() {
        // Recalculating route due to traffic jam
    }
    
    @Override
    public void onArriveDestination() {
        Toast.makeText(this, "Arrived at destination", Toast.LENGTH_LONG).show();
        stopNavigation();
    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onArrivedWayPoint(int wayID) {
        // Arrived at waypoint
    }
    
    @Override
    public void onGpsOpenStatus(boolean enabled) {
        // GPS status changed
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void onGpsSignalWeak(boolean isWeak) {
        // GPS signal weak status
    }
    
    @Override
    public void onNaviRouteNotify(com.amap.api.navi.model.AMapNaviRouteNotifyData notifyData) {
        // Navigation route notification
    }
    
    @Override
    public void onPlayRing(int type) {
        // Play navigation ring
    }
    
    @Override
    public void updateAimlessModeCongestionInfo(com.amap.api.navi.model.AimLessModeCongestionInfo congestionInfo) {
        // Aimless mode congestion info
    }
    
    @Override
    public void updateAimlessModeStatistics(com.amap.api.navi.model.AimLessModeStat stat) {
        // Aimless mode statistics
    }
    
    @Override
    public void OnUpdateTrafficFacility(com.amap.api.navi.model.AMapNaviTrafficFacilityInfo trafficFacilityInfo) {
        // Traffic facility updated
    }
    
    @Override
    public void OnUpdateTrafficFacility(com.amap.api.navi.model.AMapNaviTrafficFacilityInfo[] trafficFacilityInfos) {
        // Traffic facility updated (array version)
    }
    
    @Override
    public void notifyParallelRoad(int roadStatus) {
        // Parallel road status
    }
    
    @Override
    public void hideLaneInfo() {
        // Hide lane info
    }
    
    @Override
    public void showLaneInfo(com.amap.api.navi.model.AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {
        // Show lane info
    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideCross() {
        // Hide cross info
    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    // Note: showCross(AMapModelCross) may not be in all SDK versions
    // If compilation fails, remove @Override
    public void showCross(com.amap.api.navi.model.AMapModelCross modelCross) {
        // Show cross info
    }
    
    @Override
    public void onServiceAreaUpdate(com.amap.api.navi.model.AMapServiceAreaInfo[] serviceAreaInfos) {
        // Service area updated
    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void updateCameraInfo(com.amap.api.navi.model.AMapNaviCameraInfo[] cameraInfos) {
        // Camera info updated
    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    // Optional navigation listener methods - commented out if classes not available in SDK version
    // Uncomment if your SDK version supports these:
    /*
    @Override
    public void onNaviInfoUpdate(com.amap.api.navi.model.AMapNaviInfo naviInfo) {
        // Navigation info updated
    }
    
    @Override
    public void onNaviInfoUpdated(com.amap.api.navi.model.AMapNaviInfo naviInfo) {
        // Navigation info updated (alternative method)
    }
    
    @Override
    public void updateCameraInfo(com.amap.api.navi.model.AMapNaviCameraInfo[] cameraInfos) {
        // Camera info updated
    }
    
    @Override
    public void onServiceAreaUpdate(com.amap.api.navi.model.AMapServiceAreaInfo[] serviceAreaInfos) {
        // Service area updated
    }
    
    @Override
    public void showCross(com.amap.api.navi.model.AMapModelCross modelCross) {
        // Show cross info
    }
    
    @Override
    public void hideCross() {
        // Hide cross info
    }
    
    @Override
    public void showLaneInfo(com.amap.api.navi.model.AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {
        // Show lane info
    }
    
    @Override
    public void hideLaneInfo() {
        // Hide lane info
    }
    
    // Optional methods - may not be available in all SDK versions
    // Uncomment if your SDK version supports:
    /*
    @Override
    public void onCalculateMultipleRoutesSuccess(int[] routeIds) {
        // Multiple routes calculated
        if (routeIds != null && routeIds.length > 0) {
            onCalculateRouteSuccess(routeIds);
        }
    }
    
    @Override
    public void notifyParallelRoad(int roadStatus) {
        // Parallel road status
    }
    
    @Override
    public void OnUpdateTrafficFacility(com.amap.api.navi.model.AMapNaviTrafficFacilityInfo[] trafficFacilityInfos) {
        // Traffic facility updated
    }
    
    @Override
    public void OnUpdateTrafficFacility(com.amap.api.navi.model.AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {
        // Traffic facility updated (alternative)
    }
    
    @Override
    public void updateAimlessModeStatistics(com.amap.api.navi.model.AimlessModeStat aimlessModeStat) {
        // Aimless mode statistics
    }
    
    @Override
    public void updateAimlessModeCongestionInfo(com.amap.api.navi.model.AimlessModeCongestionInfo aimlessModeCongestionInfo) {
        // Aimless mode congestion info
    }
    
    @Override
    public void onPlayRing(int type) {
        // Play navigation ring
    }
    */
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Restore default exception handler
        if (defaultExceptionHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(defaultExceptionHandler);
        }
        
        if (locationHelper != null) {
            locationHelper.stopLocationUpdates();
        }
        if (aMapNavi != null) {
            try {
                try {
                    aMapNavi.stopNavi();
                } catch (UnsatisfiedLinkError | NoSuchMethodError e) {
                    android.util.Log.w("VolunteerMapActivity", "Error stopping navigation (native libs)", e);
                } catch (Exception e) {
                    android.util.Log.w("VolunteerMapActivity", "Error stopping navigation", e);
                }
                try {
                    aMapNavi.destroy();
                } catch (UnsatisfiedLinkError | NoSuchMethodError e) {
                    android.util.Log.w("VolunteerMapActivity", "Error destroying navigation (native libs)", e);
                } catch (Exception e) {
                    android.util.Log.w("VolunteerMapActivity", "Error destroying navigation", e);
                }
            } catch (Exception e) {
                android.util.Log.w("VolunteerMapActivity", "Error in navigation cleanup", e);
            }
        }
        if (mapView != null) {
            try {
                mapView.onDestroy();
            } catch (RuntimeException e) {
                android.util.Log.e("VolunteerMapActivity", "Error destroying map view", e);
            }
        }
    }
}
