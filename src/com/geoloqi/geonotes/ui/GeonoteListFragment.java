package com.geoloqi.geonotes.ui;

import org.apache.http.Header;
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
import android.widget.ListView;

import com.geoloqi.android.sdk.LQException;
import com.geoloqi.android.sdk.LQSession;
import com.geoloqi.android.sdk.LQSession.OnRunApiRequestListener;
import com.geoloqi.android.sdk.service.LQService;
import com.geoloqi.geonotes.R;
import com.geoloqi.geonotes.ui.MainActivity.LQServiceConnection;
import com.geoloqi.geonotes.widget.GeonoteListAdapter;

/**
 * An implementation of {@link ListFragment} for displaying
 * the currently authenticated user's list of pending geonotes.
 * 
 * @author Tristan Waddington
 */
public class GeonoteListFragment extends ListFragment implements
        OnItemClickListener, LQServiceConnection {
    private static final String TAG = "GeonoteListFragment";
    
    private GeonoteListAdapter mAdapter;
    
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
        lv.setOnItemClickListener(this);
        
        // Set the default text
        setEmptyText(getString(R.string.empty_geonote_list));
        
        LQService service = ((MainActivity) getActivity()).getService();
        if (service != null) {
            onServiceConnected(service);
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Pass
    }
    
    @Override
    public void onServiceConnected(LQService service) {
        Log.d(TAG, "onServiceConnected");
        
        if (getListAdapter() != null) {
            // Bail out if our list adapter has already
            // been populated!
            return;
        }
        
        LQSession session = service.getSession();
        session.runGetRequest("geonote/list_set", new OnRunApiRequestListener() {
            @Override
            public void onSuccess(LQSession session, JSONObject json,
                    Header[] headers) {
                Activity activity = getActivity();
                if (activity != null) {
                    // Create our list adapter
                    mAdapter = new GeonoteListAdapter(activity);
                    
                    try {
                        JSONArray array = json.getJSONArray("geonotes");
                        
                        for (int i = 0; i < array.length(); i++) {
                            mAdapter.add(array.optJSONObject(i));
                        }
                        setListAdapter(mAdapter);
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse the list of geonotes!", e);
                    } catch (IllegalStateException e) {
                        // The Fragment was probably detached while the
                        // request was in-progress. We should cancel
                        // the request when this happens.
                    }
                }
            }
            @Override
            public void onFailure(LQSession session, LQException e) {
                Log.d(TAG, "onFailure");
            }
            @Override
            public void onComplete(LQSession session, JSONObject json,
                    Header[] headers, StatusLine status) {
                Log.d(TAG, "onComplete");
            }
        });
    }
}
