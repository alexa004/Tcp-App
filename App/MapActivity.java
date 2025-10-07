package com.example.weathertcpapp;

import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;


public class MapActivity extends AppCompatActivity {

    private MapView mapView;

    private Handler handler = new Handler();
    private int currentIndex = 0;
    private boolean isCycling = true;

    private List<LocationTemp> locationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Load osmdroid configuration
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // ✅ Center map on India initially
        mapView.getController().setZoom(5.5);
        mapView.getController().setCenter(new GeoPoint(23.0, 80.0));

        // ✅ Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            isCycling = false; // Stop loop
            finish();
        });

        // ✅ Get data from Intent
        String data = getIntent().getStringExtra("temperature_data");
        if (data != null) {
            prepareLocations(data);   // ✅ Parse into list
            addMarkers();             // ✅ Add all markers
            startCycling();           // ✅ Start auto cycling
        }
    }

    private void prepareLocations(String data) {
        // Example data: Jaipur:32\nDelhi:30\nMumbai:28
        String[] lines = data.split("\n");
        for (String line : lines) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String city = parts[0].trim();
                String temp = parts[1].trim();

                GeoPoint point = getCityCoordinates(city);
                if (point != null) {
                    locationList.add(new LocationTemp(city, temp, point));
                }
            }
        }
    }

    private void addMarkers() {
        for (LocationTemp loc : locationList) {
            Marker marker = new Marker(mapView);
            marker.setPosition(loc.geoPoint);
            marker.setTitle(loc.city + " - " + loc.temp + "°C");
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    private void startCycling() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isCycling || locationList.isEmpty()) return;

                LocationTemp loc = locationList.get(currentIndex);

                // ✅ Move map to next location
                mapView.getController().animateTo(loc.geoPoint);
                mapView.getController().setZoom(7.0);

                // ✅ Show a floating info window automatically
                Marker highlightMarker = new Marker(mapView);
                highlightMarker.setPosition(loc.geoPoint);
                highlightMarker.setTitle("📍 " + loc.city + " - " + loc.temp + "°C");
                highlightMarker.showInfoWindow();  // Auto-open popup
                mapView.getOverlays().add(highlightMarker);

                // ✅ Clear after 3 seconds to avoid clutter
                handler.postDelayed(() -> {
                    mapView.getOverlays().remove(highlightMarker);
                    mapView.invalidate();
                }, 2000);

                // ✅ Move to next city (loop back if needed)
                currentIndex = (currentIndex + 1) % locationList.size();

                // ✅ Repeat after 3 seconds
                handler.postDelayed(this, 3000);
            }
        }, 1000);
    }

    private GeoPoint getCityCoordinates(String city) {
        switch (city) {
            case "Jaipur": return new GeoPoint(26.9124, 75.7873);
            case "Delhi": return new GeoPoint(28.6139, 77.2090);
            case "Mumbai": return new GeoPoint(19.0760, 72.8777);
            case "Kolkata": return new GeoPoint(22.5726, 88.3639);
            case "Chennai": return new GeoPoint(13.0827, 80.2707);
            default: return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCycling = false; // Stop when leaving map
    }

    // ✅ Helper class
    static class LocationTemp {
        String city;
        String temp;
        GeoPoint geoPoint;
        LocationTemp(String c, String t, GeoPoint g) {
            city = c;
            temp = t;
            geoPoint = g;
        }
    }
}
