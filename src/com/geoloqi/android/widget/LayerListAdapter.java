package com.geoloqi.android.widget;

import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * This class is a simple implementation of ArrayAdapter and
 * should be used for displaying layer details in a list.
 * 
 * @author Tristan Waddington
 */
public class LayerListAdapter extends ArrayAdapter<JSONObject> {
    private LayoutInflater mInflater;
    
    /** A simple class to cache references to view resources. */
    private static class ViewHolder {
        public TextView name;
        public TextView description;
    }
    
    public LayerListAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_2);
        
        // Get our layout inflater
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            // Inflate our row layout
            convertView = mInflater.inflate(android.R.layout.simple_list_item_2, parent, false);

            // Cache the row elements for efficient retrieval
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(android.R.id.text1);
            holder.description = (TextView) convertView.findViewById(android.R.id.text2);

            // Store the holder object on the row
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        // Populate our data
        JSONObject layer = getItem(position);
        holder.name.setText(layer.optString("name"));
        holder.description.setText(layer.optString("description"));
        
        return convertView;
    }
}
