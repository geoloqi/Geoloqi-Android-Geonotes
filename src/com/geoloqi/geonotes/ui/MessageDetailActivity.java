package com.geoloqi.geonotes.ui;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.geoloqi.geonotes.Constants;
import com.geoloqi.geonotes.R;
import com.geoloqi.geonotes.maps.DoubleTapMapView;
import com.geoloqi.geonotes.maps.GeonoteItemizedOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * Display detailed information about an activity item.
 * 
 * @author Tristan Waddington
 */
public class MessageDetailActivity extends SherlockMapActivity {
    public static final String EXTRA_JSON = "com.geoloqi.geonotes.ui.extra.JSON";

    private static final String TAG = "MessageDetailActivity";

    private static final int DEFAULT_MAP_ZOOM = 17;

    private JSONObject mMessage;
    private JSONObject mMessageActor;
    private JSONObject mMessageObject;
    
    private MapView mMapView;
    private GeoPoint mMapCenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_detail);
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        Intent intent = getIntent();
        try {
            // Get the activity object
            mMessage = new JSONObject(intent.getStringExtra(EXTRA_JSON));
            
            mMessageActor = mMessage.getJSONObject("actor");
            mMessageObject = mMessage.getJSONObject("object");
            JSONObject location = mMessage.optJSONObject("location");
            
            if (location != null) {
                // Display our location name
                TextView nameView = (TextView) findViewById(R.id.location_name);
                if (nameView != null) {
                    nameView.setText(location.optString("displayName"));
                }
            } else {
                mMapView.setVisibility(View.GONE);
            }
            
            // Display our summary text
            TextView summaryView = (TextView) findViewById(R.id.summary_text);
            if (summaryView != null) {
                summaryView.setText(mMessageObject.optString("summary"));
            }
            
            // Display our date and actor info
            TextView dateView = (TextView) findViewById(R.id.actor_text);
            if (dateView != null) {
                dateView.setText(String.format("%s | %s",
                        mMessage.optString("displayDate"), mMessageActor.optString("displayName")));
            }
            
            // Display our map
            mMapView = new DoubleTapMapView(this, Constants.GOOGLE_MAPS_KEY);
            mMapView.setClickable(false);
            mMapView.setBuiltInZoomControls(false);
            mMapView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.message_detail_map_height)));
            
            // Add the MapView to the layout
            ((ViewGroup) findViewById(R.id.map_container)).addView(mMapView, 0);
            
            // Build a GeoPoint representing where the geonote was
            // picked up.
            mMapCenter = new GeoPoint((int) (location.optDouble("latitude") * 1e6),
                    (int) (location.optDouble("longitude") * 1e6));
            
            // Build our geonote overlay
            GeonoteItemizedOverlay geonoteOverlay = new GeonoteItemizedOverlay(
                    getResources().getDrawable(R.drawable.marker));
            
            OverlayItem geonote = new OverlayItem(mMapCenter,
                    location.optString("displayName"), mMessageObject.optString("summary"));
            geonoteOverlay.addOverlay(geonote);
            
            // Add the geonote to our MapView as an overlay
            List<Overlay> mapOverlays = mMapView.getOverlays();
            mapOverlays.add(geonoteOverlay);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse message data!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Center the map
        MapController mapController = mMapView.getController();
        mapController.setCenter(mMapCenter);
        mapController.setZoom(DEFAULT_MAP_ZOOM);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.message_detail_menu, menu);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            startActivity(new Intent(this, MainActivity.class));
            return true;
        case R.id.menu_share:
            String message = mMessageObject.optString("summary");
            String url = mMessage.optString("url");
            String text = String.format("%s: %s", message, url);
            
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(share,
                    getString(R.string.menu_share_title)));
            
            return true;
        case R.id.menu_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return false;
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
