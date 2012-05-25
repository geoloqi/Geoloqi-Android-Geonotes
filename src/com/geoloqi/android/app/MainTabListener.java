package com.geoloqi.android.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * This class handles swapping Fragments in and out of the
 * main activity layout.
 * 
 * @author Tristan Waddington
 */
public class MainTabListener<T extends Fragment> implements TabListener {
    private Fragment mFragment;
    private final SherlockFragmentActivity mActivity;
    private final FragmentManager mFragmentManager;
    private final String mTag;
    private final Class<T> mClass;

    public MainTabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
        mActivity = activity;
        mFragmentManager = mActivity.getSupportFragmentManager();
        mTag = tag;
        mClass = clz;
        
        // Detach tab if it was previously attached because the initial state should be hidden.
        mFragment = mFragmentManager.findFragmentByTag(mTag);
        if (mFragment != null && !mFragment.isDetached()) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.detach(mFragment);
            ft.commit();
        }
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction unused) {
        // Since we're using ActionBarSherlock we need to use
        // the SupportFragmentManager here to get a FragmentTransaction.
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        
        if (mFragment == null) {
            mFragment = Fragment.instantiate(mActivity, mClass.getName());
            ft.add(android.R.id.content, mFragment, mTag);
        } else {
            ft.attach(mFragment);
        }
        ft.commit();
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction unused) {
        // Since we're using ActionBarSherlock we need to use
        // the SupportFragmentManager here to get a FragmentTransaction.
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        
        if (mFragment != null) {
            // Detach the fragment, because another one is being attached
            ft.detach(mFragment);
            ft.commit();
        }
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction unused) {
        // Pass
    }
}
