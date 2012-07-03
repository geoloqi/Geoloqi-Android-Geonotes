package com.geoloqi.geonotes.ui;

import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.geoloqi.android.sdk.LQException;
import com.geoloqi.android.sdk.LQSession;
import com.geoloqi.android.sdk.LQSession.OnRunApiRequestListener;
import com.geoloqi.android.sdk.LQTracker;
import com.geoloqi.android.sdk.service.LQService;
import com.geoloqi.android.sdk.service.LQService.LQBinder;
import com.geoloqi.geonotes.R;

/**
 * This Activity class handles all the logic required to authenticate
 * an existing Geoloqi user.
 * 
 * @author Tristan Waddington
 */
public class SignInActivity extends SherlockActivity implements OnClickListener {
    public static final String TAG = "SignInActivity";
    
    /** The email address given by the user. */
    private String mEmail;
    
    private ProgressDialog mProgressDialog;
    private Handler mHandler = new Handler();
    private LQService mService;
    private boolean mBound;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Define our layout
        setContentView(R.layout.auth);
        
        // Set our layout content
        ((TextView) findViewById(R.id.title_text)).setText(R.string.sign_in_title);
        ((TextView) findViewById(R.id.auth_mode_text)).setText(R.string.new_user_message);
        ((Button) findViewById(R.id.auth_mode_button)).setText(R.string.sign_up_button);
        
        // Bind our on click listeners
        ((Button) findViewById(R.id.submit_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.auth_mode_button)).setOnClickListener(this);
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
        
        // Hide the progress bar
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        
        // Unbind from LQService
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.submit_button:
            String email = ((TextView) findViewById(R.id.email)).getText().toString();
            String password = ((TextView) findViewById(R.id.password)).getText().toString();
            
            // Need to persist the email so we can use it later
            mEmail = email;
            
            // Validate our user input
            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
                Toast.makeText(this, R.string.invalid_form_error,
                        Toast.LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, R.string.invalid_email_error,
                        Toast.LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, R.string.invalid_password_error,
                        Toast.LENGTH_LONG).show();
            } else {
                // Show a loading dialog
                mProgressDialog = ProgressDialog.show(this, null, 
                        getString(R.string.authenticating_message), true);
                
                // Perform the authentication
                LQSession.requestSession(email, password,
                        new OnSignInListener(), mHandler, this);
            }
            break;
        case R.id.auth_mode_button:
            startActivity(new Intent(this, SignUpActivity.class));
            break;
        }
    }

    /**
     * Handle the server response to the auth request.
     * 
     * @author Tristan Waddington
     */
    private class OnSignInListener implements OnRunApiRequestListener {
        @Override
        public void onSuccess(LQSession session, JSONObject json,
                Header[] headers) {
            // Swap out the tracker session with our fresh one
            LQTracker tracker = mService.getTracker();
            if (tracker != null) {
                // Store the user's email
                SettingsActivity.setUserEmail(SignInActivity.this, mEmail);
                
                // Update the saved session
                mService.setSavedSession(session);
                
                // Update the tracker with the new session
                tracker.setSession(session);
                
                // Finish the activity
                finish();
            }
        }
        @Override
        public void onFailure(LQSession session, LQException e) {
            // Hide the progress bar
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            
            // Log the error
            Log.e(TAG, e.getMessage(), e.getWrappedException());
            
            // Notify the user
            Toast.makeText(SignInActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        @Override
        public void onComplete(LQSession session, JSONObject json,
                Header[] headers, StatusLine status) {
            // Hide the progress bar
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            
            // Log the error
            Log.e(TAG, status.toString());
            
            // Notify the user
            Toast.makeText(SignInActivity.this, json.optString("error_description"),
                    Toast.LENGTH_LONG).show();
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
