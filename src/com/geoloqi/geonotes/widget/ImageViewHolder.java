package com.geoloqi.geonotes.widget;

import android.widget.ImageView;
import android.widget.ListView;

/**
 * A basic container class used to cache row
 * values for a {@link ListView} item that includes
 * an ImageView and a remote image url.
 * 
 * @author Tristan Waddington
 */
public class ImageViewHolder extends BaseViewHolder {
    public String imageUrl;
    public ImageView image;
}
