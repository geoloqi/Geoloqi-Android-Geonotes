package com.geoloqi.android.receiver;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.geoloqi.android.R;
import com.geoloqi.android.sdk.LQTracker.LQTrackerProfile;
import com.geoloqi.android.sdk.receiver.LQBroadcastReceiver;
import com.geoloqi.android.sdk.service.LQService;

/**
 * An implementation of the abstract receiver
 * {@link LQBroadcastReceiver}. Handle broadcast intents
 * from the Geoloqi SDK.
 * 
 * @author Tristan Waddington
 */
public class LocationBroadcastReceiver extends LQBroadcastReceiver {
    private static final String TAG = "LocationBroadcastReceiver";

    @Override
    public void onLocationChanged(Context context, Location location) {
        // Pass
    }

    @Override
    public void onLocationUploaded(Context context, int count) {
        // Pass
    }

    @Override
    public void onPushMessageReceived(Context context, Bundle data) {
        // Pass
    }

    @Override
    public void onTrackerProfileChanged(Context context,
            LQTrackerProfile oldProfile, LQTrackerProfile newProfile) {
        // Pass
    }
}
