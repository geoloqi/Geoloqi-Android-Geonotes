package com.geoloqi.geonotes.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.geoloqi.geonotes.ui.SettingsActivity;

/**
 * A basic broadcast receiver implementation designed to receive
 * notifications when the device has booted.
 * 
 * @author Tristan Waddington
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            if (SettingsActivity.isStartOnBootEnabled(context)) {
                SettingsActivity.startTracker(context);
            }
        }
    }
}
