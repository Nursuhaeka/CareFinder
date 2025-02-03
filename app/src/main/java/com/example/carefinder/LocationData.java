package com.example.carefinder;

public class LocationData {
    private double latitude;
    private double longitude;

    public LocationData() {
        // Default constructor required for Firebase
    }

    public LocationData(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}