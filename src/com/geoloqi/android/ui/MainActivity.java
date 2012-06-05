package com.geoloqi.android.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.geoloqi.android.R;
import com.geoloqi.android.app.MainTabListener;
import com.geoloqi.android.sdk.service.LQService;
import com.geoloqi.android.sdk.service.LQService.LQBinder;

/**
 * The main activity for the Geoloqi client application.
 * 
 * @author Tristan Waddington
 */
public class MainActivity extends SherlockFragmentActivity {
    private static final String PARAM_TAB_INDEX = "tab_index";
    
    private LQService mService;
    private boolean mBound;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Note that setContentView is omitted because we use the root
        // android.R.id.content as the container for each fragment.
        
        // Start up the tracking service
        // TODO: Notify the user if GPS and/or WiFi is disabled!
        SettingsActivity.startTracker(this);
        
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
        tab.setText("Privacy");
        tab.setTabListener(new MainTabListener<Fragment>(this,
                "privacy", Fragment.class));
        actionBar.addTab(tab);
        
        if (savedInstanceState != null) {
            actionBar.setSelectedNavigationItem(
                    savedInstanceState.getInt(PARAM_TAB_INDEX, 0));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Bind to the tracking service so we can call public methods on it
        Intent intent = new Intent(this, LQService.class);
        bindService(intent, mConnection, 0);
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
            startActivity(new Intent(this, GeonoteActivity.class));
            return true;
        }
        return false;
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
