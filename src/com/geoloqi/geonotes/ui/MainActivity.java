package com.geoloqi.geonotes.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.geoloqi.android.sdk.LQSharedPreferences;
import com.geoloqi.android.sdk.service.LQService;
import com.geoloqi.android.sdk.service.LQService.LQBinder;
import com.geoloqi.geonotes.R;
import com.geoloqi.geonotes.app.MainTabListener;
import com.geoloqi.geonotes.app.SimpleAlertDialogFragment;

/**
 * The main activity for the Geoloqi client application.
 * 
 * @author Tristan Waddington
 */
public class MainActivity extends SherlockFragmentActivity implements OnClickListener {
    private static final String TAG = "MainActivity";
    private static final String PARAM_TAB_INDEX = "tab_index";
    
    private LQService mService;
    private boolean mBound;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Define our activity layout
        setContentView(R.layout.main);
        
        // Start up the tracking service
        if (SettingsActivity.isTrackerEnabled(this)) {
            SettingsActivity.startTracker(this);
        }
        
        // Configure the ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        // Configure our navigation
        ActionBar.Tab tab = actionBar.newTab();
        tab.setText("Activity");
        tab.setTabListener(new MainTabListener<ActivityListFragment>(this,
                "activity", ActivityListFragment.class));
        actionBar.addTab(tab);
        
        tab = actionBar.newTab();
        tab.setText("Layers");
        tab.setTabListener(new MainTabListener<LayerListFragment>(this,
                "layers", LayerListFragment.class));
        actionBar.addTab(tab);
        
        tab = actionBar.newTab();
        tab.setText("Geonotes");
        tab.setTabListener(new MainTabListener<GeonoteListFragment>(this,
                "geonotes", GeonoteListFragment.class));
        actionBar.addTab(tab);
        
        if (savedInstanceState != null) {
            actionBar.setSelectedNavigationItem(
                    savedInstanceState.getInt(PARAM_TAB_INDEX, 0));
        }
        
        // Wire up our onclick handlers
        Button signUpButton = (Button) findViewById(R.id.sign_up_button);
        if (signUpButton != null) {
            signUpButton.setOnClickListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Get our location manager
        LocationManager locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);
        
        // Notify the user if GPS is disabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            DialogFragment dialog = SimpleAlertDialogFragment.newInstance(
                    R.string.dialog_gps_title, R.string.dialog_gps_message);
            dialog.show(getSupportFragmentManager(), "gpsdialog");
        }
        
        // Bind to the tracking service so we can call public methods on it
        Intent intent = new Intent(this, LQService.class);
        bindService(intent, mConnection, 0);
        
        // Prompt anonymous users to register
        if (LQSharedPreferences.getSessionIsAnonymous(this)) {
            View authNotice = findViewById(R.id.auth_notice);
            if (authNotice != null) {
                authNotice.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        
        // Unbind from LQService
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        ActionBar actionBar = getSupportActionBar();
        
        // Save the current tab state
        outState.putInt(PARAM_TAB_INDEX,
                actionBar.getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        case R.id.menu_create_geonote:
            startActivity(new Intent(this, GeonoteMapActivity.class));
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.sign_up_button:
            // TODO: Implement the register activity!
            Log.d(TAG, "Sign Up!");
            break;
        }
    }

    /** Get the bound instance of {@link LQService}. */
    public LQService getService() {
        return mService;
    }

    /** A basic callback interface for children of this Activity. */
    public interface LQServiceConnection {
        /**
         * This callback will be run when the {@link MainActivity} has
         * successfully bound to the {@link LQService}.
         */
        public void onServiceConnected(LQService service);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                // We've bound to LocalService, cast the IBinder and get LocalService instance.
                LQBinder binder = (LQBinder) service;
                mService = binder.getService();
                mBound = true;
                
                // Notify the Fragments that the service has
                // finished binding.
                // TODO: Remove this logic once we implement a background
                //       sync task.
                FragmentManager fm = getSupportFragmentManager();
                try {
                    ((LQServiceConnection) fm.findFragmentByTag("activity")).onServiceConnected(mService);
                } catch (NullPointerException e) {
                    // Pass
                }
                try {
                    ((LQServiceConnection) fm.findFragmentByTag("layers")).onServiceConnected(mService);
                } catch (NullPointerException e) {
                    // Pass
                }
            } catch (ClassCastException e) {
                // Pass
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };
}
