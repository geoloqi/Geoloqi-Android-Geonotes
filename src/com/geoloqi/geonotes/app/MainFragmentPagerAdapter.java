package com.geoloqi.geonotes.app;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * This is a basic implementation of a {@link FragmentPagerAdapter}
 * for displaying a group of fragments in a {@link ViewPager}.
 * 
 * @author Tristan Waddington
 */
public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<String> mTitles = new ArrayList<String>(3);
    private ArrayList<Fragment> mItems = new ArrayList<Fragment>(3);

    public MainFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

    /**
     * Add a {@link Fragment} to the ViewPager. This must be called before
     * binding the adapter to the pager.
     * 
     * @param title
     * @param fragment
     */
    public void addItem(String title, Fragment fragment) {
        mTitles.add(title);
        mItems.add(fragment);
    }
}
