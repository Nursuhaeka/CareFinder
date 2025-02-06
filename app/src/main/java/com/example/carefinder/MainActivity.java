package com.example.carefinder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView textViewGreeting;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean cameraMoved = false;  // Flag to track if the camera was manually moved

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Toolbar with Back Button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // Enable back button
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);  // Set back icon
        }

        // Handle Back Button Click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize Firebase Authentication & Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Find UI components
        textViewGreeting = findViewById(R.id.textViewGreeting);

        // Fetch the user's name from Firebase
        fetchUserName();

        // Check permissions and GPS
        checkLocationPermission();
        checkGPSEnabled();

        // Initialize Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Start real-time location updates
        startLocationUpdates();
    }

    private void fetchUserName() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            mDatabase.child("users").child(userId).child("name").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    String userName = task.getResult().getValue(String.class);
                    textViewGreeting.setText("Hello, " + userName + "!");
                } else {
                    textViewGreeting.setText("Hello, User!");
                }
            });
        } else {
            textViewGreeting.setText("Hello, Guest!");
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void checkGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable GPS.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(200000); // Update every 5 seconds
        locationRequest.setFastestInterval(100000); // Fastest update at 2 seconds

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null || mMap == null) {
                    return;
                }

                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                LatLng userLocation = new LatLng(latitude, longitude);

                // Check if the user has manually moved the camera
                if (!cameraMoved) {
                    // Update map with user location
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                    // Send location to Firebase
                    sendLocationToFirebase(latitude, longitude);

                    // Add hospital markers
                    addHospitalMarkers();
                }
            }
        };

        // Start location updates only if permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void sendLocationToFirebase(double latitude, double longitude) {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);
        locationData.put("timestamp", timestamp);

        mDatabase.child("user_locations").child(userId).setValue(locationData);
    }

    private void addHospitalMarkers() {
        // Define hospital/clinic locations with names
        HashMap<LatLng, String> hospitals = new HashMap<>();
        hospitals.put(new LatLng(5.764522330649819, 102.22621733973995), "Hospital Machang");
        hospitals.put(new LatLng(5.766195754893868, 102.24296387317892), "Klinik Pergigian Faiza (Principal)");
        hospitals.put(new LatLng(5.764756377888941, 102.22052984493779), "Klinik Wan Fuad");
        hospitals.put(new LatLng(5.765106303504682, 102.21852089215007), "Klinik Perdana Machang");
        hospitals.put(new LatLng(5.777925539711349, 102.2205083839044), "Klinik Bima Sakti");
        hospitals.put(new LatLng(5.77696260423765, 102.21379042513743), "Klinik Hidayah Machang");
        hospitals.put(new LatLng(5.779448744565333, 102.20406073715097), "Klinik Primer Machang");
        hospitals.put(new LatLng(5.768017256336569, 102.21611086163561), "Machang Health Clinic");
        hospitals.put(new LatLng(5.764174158610726, 102.21972918270498), "Klinik Gigi Zuraina");
        hospitals.put(new LatLng(5.743008902962131, 102.24398367548655), "Bukit Bakar Health Clinic");

        for (Map.Entry<LatLng, String> entry : hospitals.entrySet()) {
            mMap.addMarker(new MarkerOptions().position(entry.getKey()).title(entry.getValue()));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Add listeners for manual map movements
        mMap.setOnCameraMoveListener(this::onCameraMove);
        mMap.setOnCameraIdleListener(this::onCameraIdle);
    }

    // This method will be called when the user manually moves the camera
    private void onCameraMove() {
        cameraMoved = true;
    }

    // Reset camera moved flag when the user stops dragging
    private void onCameraIdle() {
        cameraMoved = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}