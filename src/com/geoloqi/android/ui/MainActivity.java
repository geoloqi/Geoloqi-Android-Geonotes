package com.geoloqi.android.ui;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.geoloqi.android.R;
import com.geoloqi.android.sdk.service.LQService;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

/** Stub */
public class MainActivity extends SherlockActivity implements TabListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Configure the ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        // Configure our navigation
        ActionBar.Tab tab = actionBar.newTab();
        tab.setText("Activity");
        tab.setTabListener(this);
        actionBar.addTab(tab);
        
        tab = actionBar.newTab();
        tab.setText("Layers");
        tab.setTabListener(this);
        actionBar.addTab(tab);
        
        tab = actionBar.newTab();
        tab.setText("Privacy");
        tab.setTabListener(this);
        actionBar.addTab(tab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        
        /*
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "blah!");
        ShareActionProvider p = (ShareActionProvider) menu.findItem(R.id.menu_share).getActionProvider();
        p.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        p.setShareIntent(intent);
        */
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        /*
        case: R.id.toggle_tracker:
            Intent intent = new Intent(this, LQService.class);
            if (!stopService(intent)) {
                // Service was not running, start the service!
                // ...
            }
            return true;
            */
        }
        return false;
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        
    }
}
