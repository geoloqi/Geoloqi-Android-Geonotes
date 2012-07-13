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
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

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
public class LayerListFragment extends SherlockListFragment implements LQServiceConnection {
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
        lv.setFastScrollEnabled(true);
        
        // Set the default text
        setEmptyText(getString(R.string.empty_activity_list));
        
        // Register our context menu
        registerForContextMenu(lv);
        
        LQService service = ((MainActivity) getActivity()).getService();
        if (service != null) {
            onServiceConnected(service);
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        // Inflate our context menu
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.layer_context_menu, menu);
        
        // Configure our menu items
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        JSONObject layer = (JSONObject) mAdapter.getItem(info.position);
        
        if (layer.optBoolean("subscribed")) {
            MenuItem unsubscribe = menu.findItem(R.id.unsubscribe);
            unsubscribe.setEnabled(true);
        } else {
            MenuItem subscribe = menu.findItem(R.id.subscribe);
            subscribe.setEnabled(true);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        JSONObject layer = (JSONObject) mAdapter.getItem(info.position);
        
        switch (item.getItemId()) {
        case R.id.subscribe:
            subscribeLayer(layer.optString("layer_id"));
            return true;
        case R.id.unsubscribe:
            unsubscribeLayer(layer.optString("layer_id"));
            return true;
        }
        return false;
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
        LQSession session = service.getSession();
        
        if (getListAdapter() != null) {
            // Bail out if our list adapter has already
            // been populated!
            return;
        }
        
        // TODO: Get the app layer_list!
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
                                
                                try{
                                    // TODO: Card layout for layer items with
                                    //       active/inactive status clearly marked.
                                    // TODO: Longpress to subscribe/unsubscribe
                                    //       from a layer.
                                    // TODO: List item text is being clipped "g".
                                    Log.d(TAG, layer.toString(2));
                                } catch (JSONException e) {
                                    // Pass
                                }
                                
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
