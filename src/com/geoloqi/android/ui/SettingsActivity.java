package com.geoloqi.android.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.geoloqi.android.Build;
import com.geoloqi.android.R;
import com.geoloqi.android.sdk.LQBuild;
import com.geoloqi.android.sdk.LQSession;
import com.geoloqi.android.sdk.LQSharedPreferences;
import com.geoloqi.android.sdk.LQTracker;
import com.geoloqi.android.sdk.LQTracker.LQTrackerProfile;
import com.geoloqi.android.sdk.service.LQService;
import com.geoloqi.android.sdk.service.LQService.LQBinder;

/**
 * <p>This activity class is used to expose location tracking
 * preferences to a user.</p>
 * 
 * @author Tristan Waddington
 */
public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener,
        OnPreferenceClickListener {
    private static final String TAG = "SettingsActivity";
    private static final String URL_PRIVACY_POLICY = "https://geoloqi.com/privacy?utm_source=preferences&utm_medium=app&utm_campaign=android";
    
    private static String sAppVersion;
    
    /** An instance of the default SharedPreferences. */
    private SharedPreferences mPreferences;
    private LQService mService;
    private boolean mBound;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        
        // Get a shared preferences instance
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Set any preference listeners
        Preference preference = findPreference(getString(R.string.pref_key_tracker_profile));
        if (preference != null) {
            preference.setOnPreferenceChangeListener(this);
        }
        
        preference = findPreference(getString(R.string.pref_key_account_username));
        if (preference != null) {
            preference.setOnPreferenceClickListener(this);
        }
        
        preference = findPreference(getString(R.string.pref_key_privacy_policy));
        if (preference != null) {
            preference.setOnPreferenceClickListener(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        
        if (mPreferences != null) {
            Preference preference = null;
            
            // Display the account username
            preference = findPreference(getString(R.string.pref_key_account_username));
            if (preference != null) {
                preference.setSummary(LQSharedPreferences.getSessionUsername(this));
            }
            
            // Display the app version
            preference = findPreference(getString(R.string.pref_key_app_version));
            if (preference != null) {
                preference.setSummary(getAppVersion(this));
            }
            
            // Display the app build
            preference = findPreference(getString(R.string.pref_key_app_build));
            if (preference != null) {
                preference.setSummary(Build.APP_BUILD);
            }
            
            // Display the SDK version
            preference = findPreference(getString(R.string.pref_key_sdk_version));
            if (preference != null) {
                preference.setSummary(LQBuild.LQ_SDK_VERSION);
            }
            
            // Display the SDK build
            preference = findPreference(getString(R.string.pref_key_sdk_build));
            if (preference != null) {
                preference.setSummary(LQBuild.LQ_SDK_BUILD);
            }
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
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (key != null) {
            if (key.equals(getString(R.string.pref_key_tracker_profile))) {
                // Check to see if the tracker can switch to the requested profile
                if (mBound && mService != null) {
                    final LQTracker tracker = mService.getTracker();
                    if (tracker != null) {
                        int ordinal = Integer.parseInt((String) newValue);
                        final LQTrackerProfile newProfile = LQTrackerProfile.values()[ordinal];
                        if (!tracker.setProfile(newProfile)) {
                            // Cannot switch to the indicated profile, don't update preferences!
                            Toast.makeText(this, String.format("Unable to switch to profile %s.",
                                            newProfile), Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        boolean consumed = false;
        String key = preference.getKey();
        if (key.equals(getString(R.string.pref_key_account_username))) {
            LQSession session = mService.getSession();
            if (session != null) {
                if (session.isAnonymous()) {
                    // Start log-in Activity
                    startActivity(new Intent(this, AuthActivity.class));
                } else {
                    // TODO: Sign-out!
                }
            }
            consumed = true;
        } else if (key.equals(getString(R.string.pref_key_privacy_policy))) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(URL_PRIVACY_POLICY));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            consumed = true;
        }
        return consumed;
    }

    /** Get the human-readable application version. */
    public static String getAppVersion(Context context) {
        if (TextUtils.isEmpty(sAppVersion)) {
            PackageManager pm = context.getPackageManager();
            try {
                sAppVersion = pm.getPackageInfo(
                        context.getPackageName(), 0).versionName;
            } catch (NameNotFoundException e) {
                // Pass
            }
        }
        return sAppVersion;
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
