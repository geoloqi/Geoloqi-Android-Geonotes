package com.geoloqi.android.ui;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.geoloqi.android.Constants;
import com.geoloqi.android.R;
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
    
    private Handler mHandler = new Handler();
    
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
    
    /** Stub */
    private void startTracker() {
        Intent intent = new Intent(this, LQService.class);
        intent.setAction(LQService.ACTION_FOREGROUND);
        intent.putExtra(LQService.EXTRA_SDK_ID, Constants.GEOLOQI_ID);
        intent.putExtra(LQService.EXTRA_SDK_SECRET, Constants.GEOLOQI_SECRET);
        intent.putExtra(LQService.EXTRA_C2DM_SENDER, "geoloqi@gmail.com");
        intent.putExtra(LQService.EXTRA_NOTIFICATION, getNotification());
        startService(intent);
    }
    
    /** Stub */
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(Intent.ACTION_DEFAULT);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }
    
    /** Stub */
    private Notification getNotification() {
        // TODO: Upgrade to use a Notification.Builder when more devices
        //       are on API level 11+.
        Notification notification = new Notification();
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        notification.icon = R.drawable.ic_stat_notify;
        notification.tickerText = "The tracker is running.";
        notification.when = System.currentTimeMillis();
        notification.setLatestEventInfo(this, "Geoloqi", "The tracker is running.",
                        getPendingIntent());
        return notification;
    }
}
