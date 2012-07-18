package com.geoloqi.geonotes.ui;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.geoloqi.android.sdk.LQException;
import com.geoloqi.android.sdk.LQSession;
import com.geoloqi.android.sdk.LQSession.OnRunApiRequestListener;
import com.geoloqi.android.sdk.service.LQService;
import com.geoloqi.geonotes.R;
import com.geoloqi.geonotes.ui.MainActivity.LQServiceConnection;
import com.geoloqi.geonotes.widget.LayerListAdapter;

/**
 * An implementation of {@link ListFragment} for displaying
 * and managing the currently authenticated user's layer list.
 * 
 * @author Tristan Waddington
 */
public class LayerListFragment extends SherlockListFragment implements
        OnItemClickListener, LQServiceConnection {
    private static final String TAG = "LayerListFragment";
    
    private LayerListAdapter mAdapter;
    
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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
        
        // Toggle the subscribed state of the layer
        checkbox.toggle();
        
        JSONObject layer = mAdapter.getItem(position);
        if (checkbox.isChecked()) {
            subscribeLayer(layer.optString("layer_id"));
        } else {
            unsubscribeLayer(layer.optString("layer_id"));
        }
    }

    /**
     * Subscribe the active user to the indicated layer.
     * @param id
     */
    private void subscribeLayer(String id) {
        LQService service = ((MainActivity) getActivity()).getService();
        if (service != null) {
            LQSession session = service.getSession();
            if (session != null) {
                session.runPostRequest(String.format("layer/subscribe/%s", id),
                        new JSONObject(), new LayerSubscriptionListener());
            }
        }
    }
    
    /**
     * Unsubscribe the active user from the indicated layer.
     * @param id
     */
    private void unsubscribeLayer(String id) {
        LQService service = ((MainActivity) getActivity()).getService();
        if (service != null) {
            LQSession session = service.getSession();
            if (session != null) {
                session.runPostRequest(String.format("layer/unsubscribe/%s", id),
                        new JSONObject(), new LayerSubscriptionListener());
            }
        }
    }
    
    /** Handle the response to a layer subscription request. */
    private class LayerSubscriptionListener implements OnRunApiRequestListener {
        @Override
        public void onComplete(LQSession session, JSONObject json,
                Header[] headers, StatusLine status) {
            if (status.getStatusCode() == HttpStatus.SC_CREATED) {
                // Success! This API endpoint returns a 201 when
                // subscribing succeeds.
                try {
                    Log.d(TAG, json.toString(2));
                } catch (JSONException e) {
                    // Pass
                }
            } else {
                // Error!
                Toast.makeText(getActivity(), json.optString("error_description"),
                        Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public void onFailure(LQSession session, LQException e) {
            Toast.makeText(getActivity(), e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        @Override
        public void onSuccess(LQSession session, JSONObject json,
                Header[] headers) {
            try {
                Log.d(TAG, json.toString(2));
            } catch (JSONException e) {
                // Pass
            }
        }
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
        
        session.runGetRequest("layer/app_list", new OnRunApiRequestListener() {
            @Override
            public void onSuccess(LQSession session, JSONObject json,
                    Header[] headers) {
                Activity activity = getActivity();
                if (activity != null) {
                    // Create our list adapter
                    mAdapter = new LayerListAdapter(activity);
                    
                    try {
                        TreeMap<String, JSONObject> layers =
                                new TreeMap<String, JSONObject>();
                        
                        Iterator<?> keys = json.keys();
                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            
                            JSONArray items = json.getJSONArray(key);
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject layer = items.optJSONObject(i);
                                layers.put(layer.optString("name"), layer);
                            }
                        }
                        
                        // Add the sorted layers to the adapter
                        for (Map.Entry<String, JSONObject> cursor : layers.entrySet()) {
                            mAdapter.add(cursor.getValue());
                        }
                        
                        setListAdapter(mAdapter);
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse the layer list!");
                    } catch (IllegalStateException e) {
                        // The Fragment was probably detached while the
                        // request was in-progress. We should cancel
                        // the request when this happens.
                    }
                }
            }
            @Override
            public void onFailure(LQSession session, LQException e) {
                Log.e(TAG, "Failed to load the layer list!", e);
                
                // Set an empty adapter on the list
                setListAdapter(new LayerListAdapter(getActivity()));
            }
            @Override
            public void onComplete(LQSession session, JSONObject json,
                    Header[] headers, StatusLine status) {
                Log.d(TAG, status.toString());
                Log.e(TAG, "Failed to load the layer list!");
                
                // Set an empty adapter on the list
                setListAdapter(new LayerListAdapter(getActivity()));
            }
        });
    }
}
