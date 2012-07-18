package com.geoloqi.geonotes.ui;

import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.geoloqi.android.sdk.LQException;
import com.geoloqi.android.sdk.LQSession;
import com.geoloqi.android.sdk.LQSession.OnRunApiRequestListener;
import com.geoloqi.android.sdk.service.LQService;
import com.geoloqi.geonotes.R;
import com.geoloqi.geonotes.ui.MainActivity.LQServiceConnection;
import com.geoloqi.geonotes.widget.ActivityListAdapter;

/**
 * An implementation of {@link ListFragment} for displaying
 * the currently authenticated user's activity stream.
 * 
 * @author Tristan Waddington
 */
public class ActivityListFragment extends SherlockListFragment implements
        OnItemClickListener, LQServiceConnection {
    private static final String TAG = "ActivityListFragment";
    
    private ActivityListAdapter mAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // Configure our ListView
        ListView lv = getListView();
        lv.setFastScrollEnabled(false);
        lv.setOnItemClickListener(this);
        
        // Set the default text
        setEmptyText(getString(R.string.empty_activity_list));
        
        LQService service = ((MainActivity) getActivity()).getService();
        if (service != null) {
            onServiceConnected(service);
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        JSONObject json = mAdapter.getItem(position);
        
        // Start our message detail activity
        Intent intent = new Intent(getActivity(), MessageDetailActivity.class);
        intent.putExtra(MessageDetailActivity.EXTRA_JSON, json.toString());
        startActivity(intent);
    }
    
    @Override
    public void onServiceConnected(LQService service) {
        if (getListAdapter() != null) {
            // Bail out if our list adapter has already
            // been populated!
            return;
        }
        onRefreshRequested(service);
    }

    @Override
    public void onRefreshRequested(LQService service) {
        LQSession session = service.getSession();
        
        if (session == null) {
            // Bail!
            // TODO: This is a huge hack. We should always return a valid
            //       session from LQService.
            return;
        }
        
        session.runGetRequest("timeline/messages", new OnRunApiRequestListener() {
            @Override
            public void onSuccess(LQSession session, JSONObject json,
                    Header[] headers) {
                Activity activity = getActivity();
                if (activity != null) {
                    // Create our list adapter
                    mAdapter = new ActivityListAdapter(activity);
                    
                    try {
                        JSONArray array = json.getJSONArray("items");
                        
                        for (int i = 0; i < array.length(); i++) {
                            mAdapter.add(array.optJSONObject(i));
                        }
                        setListAdapter(mAdapter);
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse the list of messages!", e);
                    } catch (IllegalStateException e) {
                        // The Fragment was probably detached while the
                        // request was in-progress. We should cancel
                        // the request when this happens.
                    }
                }
            }
            @Override
            public void onFailure(LQSession session, LQException e) {
                Log.e(TAG, "Failed to load the activity list!", e);
                
                // Set an empty adapter on the list
                setListAdapter(new ActivityListAdapter(getActivity()));
            }
            @Override
            public void onComplete(LQSession session, JSONObject json,
                    Header[] headers, StatusLine status) {
                Log.d(TAG, status.toString());
                Log.e(TAG, "Failed to load the activity list!");
                
                // Set an empty adapter on the list
                setListAdapter(new ActivityListAdapter(getActivity()));
            }
        });
    }
}
