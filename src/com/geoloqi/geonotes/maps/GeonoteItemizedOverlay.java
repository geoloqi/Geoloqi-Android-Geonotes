package com.geoloqi.geonotes.maps;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 * A basic implementation of {@link ItemizedOverlay} to display
 * a set of Geonote overlay items on a map view.
 * 
 * @author Tristan Waddington
 */
public class GeonoteItemizedOverlay extends ItemizedOverlay<OverlayItem> {
    private Context mContext;
    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

    public GeonoteItemizedOverlay(Drawable defaultMarker) {
        super(boundCenterBottom(defaultMarker));
    }

    public GeonoteItemizedOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker));
        mContext = context;
    }

    @Override
    protected OverlayItem createItem(int index) {
        return mOverlays.get(index);
    }

    @Override
    public int size() {
        return mOverlays.size();
    }
    
    @Override
    protected boolean onTap(int index) {
        OverlayItem item = mOverlays.get(index);
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(item.getTitle());
        dialog.setMessage(item.getSnippet());
        dialog.show();
        return false;
    }
    
    /** Stub */
    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }
}
