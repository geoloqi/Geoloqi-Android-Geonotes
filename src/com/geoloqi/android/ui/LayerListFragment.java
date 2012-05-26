package com.geoloqi.android.ui;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.geoloqi.android.R;
import com.geoloqi.android.sdk.LQException;
import com.geoloqi.android.sdk.LQSession;
import com.geoloqi.android.sdk.LQSession.OnRunApiRequestListener;
import com.geoloqi.android.sdk.service.LQService;
import com.geoloqi.android.ui.MainActivity.LQServiceConnection;
import com.geoloqi.android.widget.LayerListAdapter;

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
        
        LQService service = ((MainActivity) getActivity()).getService();
        if (service != null) {
            onServiceConnected(service);
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
        session.runGetRequest("layer/list", new OnRunApiRequestListener() {
            @Override
            public void onSuccess(LQSession session, HttpResponse response) {
                // Create our list adapter
                mAdapter = new LayerListAdapter(getActivity());
                
                JSONObject obj;
                try {
                    obj = new JSONObject(EntityUtils.toString(
                                    response.getEntity(), HTTP.UTF_8));
                    
                    JSONArray array = obj.getJSONArray("layers");
                    
                    for (int i = 0; i < array.length(); i++) {
                        mAdapter.add(array.optJSONObject(i));
                    }
                    setListAdapter(mAdapter);
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    // The Fragment was probably detached while the
                    // request was in-progress. We should cancel
                    // the request when this happens.
                }
            }
            
            @Override
            public void onFailure(LQSession session, LQException e) {
                Log.d(TAG, "onFailure");
            }
            
            @Override
            public void onComplete(LQSession session, HttpResponse response, StatusLine status) {
                Log.d(TAG, "onComplete");
            }
        });
    }
}
