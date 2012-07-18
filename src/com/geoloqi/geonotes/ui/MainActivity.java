package com.geoloqi.geonotes.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.geoloqi.android.sdk.LQSharedPreferences;
import com.geoloqi.android.sdk.service.LQService;
import com.geoloqi.android.sdk.service.LQService.LQBinder;
import com.geoloqi.geonotes.R;
import com.geoloqi.geonotes.app.MainFragmentPagerAdapter;
import com.geoloqi.geonotes.app.SimpleAlertDialogFragment;
import com.viewpagerindicator.TabPageIndicator;

/**
 * The main activity for the Geoloqi client application.
 * 
 * @author Tristan Waddington
 */
public class MainActivity extends SherlockFragmentActivity implements OnClickListener {
    public static final String EXTRA_CURRENT_ITEM =
            "com.geoloqi.geonotes.extra.CURRENT_ITEM";
    
    private static final String TAG = "MainActivity";
    
    private ViewPager mPager;
    private MainFragmentPagerAdapter mAdapter;
    
    private LQService mService;
    private boolean mBound;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Define our activity layout
        setContentView(R.layout.main);
        
        // Configure our navigation
        mAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager());
        mAdapter.addItem(getString(R.string.activity_list_title),
                Fragment.instantiate(this, ActivityListFragment.class.getName()));
        mAdapter.addItem(getString(R.string.layer_list_title),
                Fragment.instantiate(this, LayerListFragment.class.getName()));
        mAdapter.addItem(getString(R.string.geonote_list_title),
                Fragment.instantiate(this, GeonoteListFragment.class.getName()));
        
        mPager = (ViewPager) findViewById(R.id.main_pager);
        mPager.setAdapter(mAdapter);
        
        // Configure our navigation titles
        TabPageIndicator indicator =
                (TabPageIndicator) findViewById(R.id.main_pager_indicator);
        indicator.setViewPager(mPager);
        
        // Set the active tab
        Intent intent = getIntent();
        mPager.setCurrentItem(intent.getIntExtra(EXTRA_CURRENT_ITEM, 0));
        
        // Wire up our onclick handlers
        Button signUpButton = (Button) findViewById(R.id.sign_up_button);
        if (signUpButton != null) {
            signUpButton.setOnClickListener(this);
        }
    }
    
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        
        // Set the active tab
        mPager.setCurrentItem(intent.getIntExtra(EXTRA_CURRENT_ITEM, 0));
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
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        
        // Prompt anonymous users to register
        View authNotice = findViewById(R.id.auth_notice);
        if (LQSharedPreferences.getSessionIsAnonymous(this)) {
            authNotice.setVisibility(View.VISIBLE);
        } else {
            authNotice.setVisibility(View.GONE);
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
            startActivity(new Intent(this, EditGeonoteActivity.class));
            return true;
        case R.id.menu_refresh:
            refreshListFragments();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.sign_up_button:
            startActivity(new Intent(this, SignUpActivity.class));
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
        
        /**
         * This callback will be run when the {@link MainActivity} receives
         * a refresh request from the user. Implementors should attempt
         * to refresh their content when this method is invoked.
         */
        public void onRefreshRequested(LQService service);
    }

    /** Notify the list fragments that they should refresh. */
    public void refreshListFragments() {
        // TODO: Refactor this logic once we implement a background
        //       sync task.
        for (Fragment f : mAdapter.getAllItems()) {
            ((LQServiceConnection) f).onRefreshRequested(mService);
        }
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
                refreshListFragments();
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
