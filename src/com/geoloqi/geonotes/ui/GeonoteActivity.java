package com.geoloqi.geonotes.ui;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.geoloqi.geonotes.Constants;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class GeonoteActivity extends MapActivity {
    private static final String TAG = "GeonoteActivity";
    
    /** The default zoom level to display. */
    private static final int DEFAULT_ZOOM_LEVEL = 15;
    
    /** The default center point to display. */
    private static final GeoPoint DEFAULT_MAP_CENTER =
            new GeoPoint(45516290, -122675943);
    
    private MapView mMapView;
    private MapController mMapController;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure our MapView.
        mMapView = new MapView(this, Constants.GOOGLE_MAPS_KEY);
        mMapView.setClickable(true);
        mMapView.setBuiltInZoomControls(true);
        
        // Get our MapController.
        mMapController = mMapView.getController();
        
        // Insert the MapView into the layout.
        setContentView(mMapView);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Set the map defaults
        
        int zoom = DEFAULT_ZOOM_LEVEL;
        GeoPoint center = DEFAULT_MAP_CENTER;
        
        Location location = getLastKnownLocation();
        if (location != null) {
            center = new GeoPoint((int) (location.getLatitude() * 1e6),
                    (int) (location.getLongitude() * 1e6));
        }
        
        // Center the MapView on the last known location
        mMapController.setCenter(center);
        mMapController.setZoom(zoom);
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    /**
     * Get the last known {@link Location} from the GPS provider. If the
     * GPS provider is disabled, query the network provider.
     * 
     * @return the last known location; otherwise null.
     */
    private Location getLastKnownLocation() {
        Location location;
        LocationManager locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);
        
        // Attempt to get the last known GPS location
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER);
        } else {
            location = locationManager.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER);
        }
        return location;
    }
}
