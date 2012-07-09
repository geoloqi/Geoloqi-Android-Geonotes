package com.geoloqi.geonotes.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.geoloqi.geonotes.R;

/**
 * This Activity is responsible for launching the Geoloqi client
 * and performing any set up before the application is started. A
 * splash screen with corporate branding is displayed to the user.
 * 
 * @author Tristan Waddington
 */
public class LauncherActivity extends Activity {
    /** The minimum amount of time that the splash screen will be displayed. */
    private static final int SPLASH_TIMEOUT = 2000;
    
    /** A runnable that will launch the main activity. */
    private Runnable mStartMainActivity = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(LauncherActivity.this, MainActivity.class));
        }
    };
    
    private final Handler mHandler = new Handler();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        // Start up the tracking service
        if (SettingsActivity.isTrackerEnabled(this)) {
            SettingsActivity.startTracker(this);
        }
        
        // Show the splash screen then start the main activity
        mHandler.postDelayed(mStartMainActivity, SPLASH_TIMEOUT);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        // If the user has stopped the launcher activity, don't
        // continue launching the main activity.
        mHandler.removeCallbacks(mStartMainActivity);
    }
}
