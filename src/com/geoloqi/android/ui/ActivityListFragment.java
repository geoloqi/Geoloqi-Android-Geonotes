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

import com.geoloqi.android.sdk.LQException;
import com.geoloqi.android.sdk.LQSession;
import com.geoloqi.android.sdk.LQSession.OnRunApiRequestListener;
import com.geoloqi.android.sdk.service.LQService;
import com.geoloqi.android.ui.MainActivity.LQServiceConnection;
import com.geoloqi.android.widget.ActivityListAdapter;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;

/**
 * An implementation of {@link ListFragment} for displaying
 * the currently authenticated user's activity stream.
 * 
 * @author Tristan Waddington
 */
public class ActivityListFragment extends ListFragment implements LQServiceConnection {
    private static final String TAG = "ActivityListFragment";
    
    private ActivityListAdapter mAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // Set the default text
        setEmptyText("You have no Activity!");
        
        // Set the list adapter
        mAdapter = new ActivityListAdapter(getActivity());
        setListAdapter(mAdapter);
        
        // Hide the list and display a loading indicator
        setListShown(false);
        
        LQService service = ((MainActivity) getActivity()).getService();
        if (service != null) {
            onServiceConnected(service);
        }
    }
    
    @Override
    public void onServiceConnected(LQService service) {
        Log.d(TAG, "onServiceConnected");
        
        LQSession session = service.getSession();
        session.runGetRequest("timeline/messages", new OnRunApiRequestListener() {
            @Override
            public void onSuccess(LQSession session, HttpResponse response) {
                Log.d(TAG, "onSuccess");
                
                JSONObject obj;
                try {
                    obj = new JSONObject(EntityUtils.toString(
                                    response.getEntity(), HTTP.UTF_8));
                    Log.d(TAG, obj.toString());
                    
                    // items, paging {next_offset, limit, total}
                    
                    JSONArray array = obj.getJSONArray("items");
                    
                    for (int i = 0; i < array.length(); i++) {
                        mAdapter.add(array.optJSONObject(i));
                    }
                    setListShown(true);
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
