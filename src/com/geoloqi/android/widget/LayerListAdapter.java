package com.geoloqi.android.widget;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.geoloqi.android.R;

/**
 * This class is a simple implementation of ArrayAdapter and
 * should be used for displaying layer details in a list.
 * 
 * @author Tristan Waddington
 */
public class LayerListAdapter extends ArrayAdapter<JSONObject> {
    private LazyImageLoader mLazyLoader;
    private LayoutInflater mInflater;
    
    public LayerListAdapter(Context context) {
        super(context, R.layout.simple_icon_list_item);
        
        // Get our lazy loader
        mLazyLoader = LazyImageLoader.getInstance(context);
        
        // Get our layout inflater
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageViewHolder holder;
        
        if (convertView == null) {
            // Inflate our row layout
            convertView = mInflater.inflate(
                    R.layout.simple_icon_list_item, parent, false);
            
            // Cache the row elements for efficient retrieval
            holder = new ImageViewHolder();
            holder.text1 = (TextView) convertView.findViewById(R.id.text1);
            holder.text2 = (TextView) convertView.findViewById(R.id.text2);
            holder.image = (ImageView) convertView.findViewById(R.id.icon);
            
            // Store the holder object on the row
            convertView.setTag(holder);
        } else {
            holder = (ImageViewHolder) convertView.getTag();
        }
        
        // Reset our row values
        holder.text1.setText("");
        holder.text2.setText("");
        holder.image.setImageDrawable(null);
        
        // Populate our data
        JSONObject layer = getItem(position);
        holder.imageUrl = layer.optString("icon");
        holder.text1.setText(layer.optString("name"));
        holder.text2.setText(layer.optString("description"));
        
        // Load the icon on a background thread
        mLazyLoader.loadImage(holder);
        
        // Hide the description TextView if it is empty so
        // the name field will be centered.
        if (TextUtils.isEmpty(holder.text2.getText())) {
            holder.text2.setVisibility(View.GONE);
        } else {
            holder.text2.setVisibility(View.VISIBLE);
        }
        
        return convertView;
    }
}
