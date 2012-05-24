package com.geoloqi.android.ui;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import com.geoloqi.android.R;
import com.geoloqi.android.sdk.LQSharedPreferences;
import com.geoloqi.android.sdk.LQTracker.LQTrackerProfile;
import com.geoloqi.android.sdk.service.LQService;

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
    
    private final Handler mHandler = new Handler();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        // Start up the tracking service
        startTracker();
        
        // Show the splash screen then start the main activity
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LauncherActivity.this, MainActivity.class));
            }
        }, SPLASH_TIMEOUT);
    }
    
    /** Start the background location service. */
    private void startTracker() {
        Intent intent = new Intent(this, LQService.class);
        intent.setAction(LQService.ACTION_FOREGROUND);
        intent.putExtra(LQService.EXTRA_NOTIFICATION, getNotification());
        startService(intent);
    }
    
    /** Get the {@link PendingIntent} used by the service Notification. */
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(Intent.ACTION_DEFAULT);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }
    
    /** Get the {@link Notification} used by the foreground service. */
    private Notification getNotification() {
        String contentText;
        LQTrackerProfile currentProfile = LQSharedPreferences.getTrackerProfile(this);
        if (currentProfile.equals(LQTrackerProfile.OFF)) {
            contentText = getString(R.string.notify_tracker_off);
        } else {
            contentText = getString(R.string.notify_tracker_on);
        }
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setOnlyAlertOnce(true);
        builder.setSmallIcon(R.drawable.ic_stat_notify);
        builder.setTicker(contentText);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(contentText);
        builder.setContentIntent(getPendingIntent());
        return builder.getNotification();
    }
}
