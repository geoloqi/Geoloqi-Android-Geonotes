package com.geoloqi.geonotes.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.geoloqi.android.sdk.LQBuild;
import com.geoloqi.android.sdk.LQSharedPreferences;
import com.geoloqi.android.sdk.LQTracker.LQTrackerProfile;
import com.geoloqi.android.sdk.service.LQService;
import com.geoloqi.android.sdk.service.LQService.LQBinder;
import com.geoloqi.geonotes.Build;
import com.geoloqi.geonotes.R;

/**
 * <p>This activity class is used to expose location tracking
 * preferences to a user.</p>
 * 
 * @author Tristan Waddington
 */
public class SettingsActivity extends SherlockPreferenceActivity implements
        OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String TAG = "SettingsActivity";
    private static final String PREF_USER_EMAIL = "com.geoloqi.geonotes.preference.EMAIL";
    private static final String URL_PRIVACY_POLICY =
            "https://geoloqi.com/privacy?utm_source=preferences&utm_medium=app&utm_campaign=android";
    
    /** A cached reference of the application version from the manifest. */
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
        Preference preference = findPreference(
                getString(R.string.pref_key_tracker_status));
        if (preference != null) {
            preference.setOnPreferenceChangeListener(this);
        }
        
        preference = findPreference(
                getString(R.string.pref_key_vibrate));
        if (preference != null) {
            preference.setOnPreferenceChangeListener(this);
        }
        
        preference = findPreference(
                getString(R.string.pref_key_account_username));
        if (preference != null) {
            preference.setOnPreferenceClickListener(this);
        }
        
        preference = findPreference(
                getString(R.string.pref_key_privacy_policy));
        if (preference != null) {
            preference.setOnPreferenceClickListener(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        
        if (mPreferences != null) {
            Preference preference = null;
            CheckBoxPreference checkboxPreference =
                    (CheckBoxPreference) findPreference(getString(R.string.pref_key_vibrate));
            
            // Display the vibrate preference
            if (checkboxPreference != null) {
                checkboxPreference.setChecked(LQSharedPreferences.shouldVibrate(this));
            }
            
            // Display the account
            preference = findPreference(getString(R.string.pref_key_account_username));
            if (preference != null) {
                if (!LQSharedPreferences.getSessionIsAnonymous(this)) {
                    preference.setTitle(getUserEmail(this));
                    preference.setSummary(R.string.user_account_summary);
                }
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
        if (key.equals(getString(R.string.pref_key_tracker_status))) {
            boolean enableLocation = newValue.equals(true);
            
            CheckBoxPreference startOnBoot =
                    (CheckBoxPreference) findPreference(getString(R.string.pref_key_start_on_boot));
            
            if (enableLocation) {
                // Start the service
                startTracker(this);
                
                // Enable the start on boot option
                startOnBoot.setEnabled(true);
            } else {
                // Stop the service
                stopService(new Intent(this, LQService.class));
                
                // Disable the start on boot option
                startOnBoot.setEnabled(false);
                startOnBoot.setChecked(false);
            }
        } else if (key.equals(getString(R.string.pref_key_vibrate))) {
            boolean shouldVibrate = newValue.equals(true);
            
            if (shouldVibrate) {
                LQSharedPreferences.enableVibration(this);
            } else {
                LQSharedPreferences.disableVibration(this);
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (key.equals(getString(R.string.pref_key_account_username))) {
            if (LQSharedPreferences.getSessionIsAnonymous(this)) {
                startActivity(new Intent(this, SignInActivity.class));
            }
            return true;
        } else if (key.equals(getString(R.string.pref_key_privacy_policy))) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(URL_PRIVACY_POLICY));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }
        return false;
    }

    /**
     * Store the active user's email address in shared preferences.
     * 
     * @param context
     * @param email
     * @return true if the value was successfully written.
     */
    public static boolean setUserEmail(Context context, String email) {
        if (!TextUtils.isEmpty(email)) {
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(context);
            Editor editor = preferences.edit();
            editor.putString(PREF_USER_EMAIL, email);
            return editor.commit();
        }
        return false;
    }

    /**
     * Get the active user's email address as a string.
     * 
     * @param context
     * @return the email address; "Anonymous" if not set.
     */
    public static String getUserEmail(Context context) {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_USER_EMAIL, "Anonymous");
    }

    /** Determine if the user has disabled the tracker. */
    public static boolean isTrackerEnabled(Context context) {
        SharedPreferences preferences = (SharedPreferences)
                PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(
                context.getString(R.string.pref_key_tracker_status), true);
    }

    /** Determine if the user has asked the tracker to be started on boot. */
    public static boolean isStartOnBootEnabled(Context context) {
        SharedPreferences preferences = (SharedPreferences)
                PreferenceManager.getDefaultSharedPreferences(context);
        return isTrackerEnabled(context) && preferences.getBoolean(
                context.getString(R.string.pref_key_start_on_boot), true);
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
    
    /** Start the background location service. */
    public static void startTracker(Context c) {
        Intent intent = new Intent(c, LQService.class);
        c.startService(intent);
        
        // Ensure the tracker is always in the correct profile
        Intent profileIntent = new Intent(c, LQService.class);
        profileIntent.setAction(LQService.ACTION_SET_TRACKER_PROFILE);
        profileIntent.putExtra(LQService.EXTRA_PROFILE, LQTrackerProfile.ADAPTIVE);
        c.startService(profileIntent);
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
