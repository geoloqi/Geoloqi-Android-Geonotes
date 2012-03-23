package com.geoloqi.android.test;

import com.geoloqi.android.ui.LauncherActivity;
import com.geoloqi.android.R;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ImageView;

public class LauncherActivityTest extends
                ActivityInstrumentationTestCase2<LauncherActivity> {
    
    /** The currently active Activity. */
    private Activity mActivity;
    
    public LauncherActivityTest() {
        super(LauncherActivity.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        // This method runs prior to each test!
        mActivity = getActivity();
    }
    
    public void testPreconditions() {
        // This method runs only once and can be used to verify
        // conditions before starting the test run.
    }
    
    /** Stub */
    public void testSplash() {
        ImageView splash = (ImageView) mActivity.findViewById(R.id.splash);
        assertEquals(splash.getVisibility(), View.VISIBLE);
    }
}
