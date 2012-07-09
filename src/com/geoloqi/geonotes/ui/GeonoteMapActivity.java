package com.geoloqi.geonotes.ui;

import java.util.List;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.geoloqi.geonotes.Constants;
import com.geoloqi.geonotes.R;
import com.geoloqi.geonotes.maps.DoubleTapMapView;
import com.geoloqi.geonotes.maps.GeonotePickerOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class GeonoteMapActivity extends MapActivity {
    private static final String TAG = "GeonoteMapActivity";
    private static final String EXTRA_LAT = "com.geoloqi.geonotes.extra.LAT";
    private static final String EXTRA_LNG = "com.geoloqi.geonotes.extra.LNG";
    private static final String EXTRA_ZOOM = "com.geoloqi.geonotes.extra.ZOOM";
    
    /** The default zoom level to display. */
    private static final int DEFAULT_ZOOM_LEVEL = 15;
    
    /** The default center point to display. */
    private static final GeoPoint DEFAULT_MAP_CENTER =
            new GeoPoint(45516290, -122675943);
    
    /** The initial map zoom. */
    private int mMapZoom = DEFAULT_ZOOM_LEVEL;
    
    /** The initial map center. */
    private GeoPoint mMapCenter = DEFAULT_MAP_CENTER;
    
    private MapView mMapView;
    private MapController mMapController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set our content view
        setContentView(R.layout.geonote_picker);
        
        // Configure our MapView.
        mMapView = new DoubleTapMapView(this, Constants.GOOGLE_MAPS_KEY);
        mMapView.setClickable(true);
        mMapView.setBuiltInZoomControls(true);
        
        // Get our list overlays
        List<Overlay> overlayList = mMapView.getOverlays();
        
        // Add our overlay to the map
        overlayList.add(new GeonotePickerOverlay(this, mMapView));
        
        // Get our MapController.
        mMapController = mMapView.getController();
        
        // Insert the MapView into the layout.
        setContentView(mMapView);
        //((ViewGroup) findViewById(R.id.frame)).addView(mMapView, 0);
        
        // Restore our saved map state
        if (savedInstanceState != null) {
            int lat = savedInstanceState.getInt(EXTRA_LAT, 0);
            int lng = savedInstanceState.getInt(EXTRA_LNG, 0);
            
            if ((lat + lng) != 0) {
                mMapCenter = new GeoPoint(lat, lng);
            }
            mMapZoom = savedInstanceState.getInt(EXTRA_ZOOM, DEFAULT_ZOOM_LEVEL);
        } else {
            Location location = getLastKnownLocation();
            if (location != null) {
                // Set the map center to the device's last known location
                mMapCenter = new GeoPoint((int) (location.getLatitude() * 1e6),
                        (int) (location.getLongitude() * 1e6));
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Set our map center and zoom level
        mMapController.setCenter(mMapCenter);
        mMapController.setZoom(mMapZoom);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Persist our map position
        outState.putInt(EXTRA_LAT,
                mMapView.getMapCenter().getLatitudeE6());
        outState.putInt(EXTRA_LNG,
                mMapView.getMapCenter().getLongitudeE6());
        outState.putInt(EXTRA_ZOOM,
                mMapView.getZoomLevel());
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
