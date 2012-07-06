package com.geoloqi.geonotes.maps;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

/**
 * This is an implementation of {@link MapView} that adds a
 * {@link SimpleOnGestureListener} to support double-tap to zoom
 * functionality.
 * 
 * @author Tristan Waddington
 */
public class DoubleTapMapView extends MapView {
    private GestureDetector mGestureDetector;
    
    public DoubleTapMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, mDoubleTapListener);
    }
    
    public DoubleTapMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mGestureDetector = new GestureDetector(context, mDoubleTapListener);
    }
    
    public DoubleTapMapView(Context context, String apiKey) {
        super(context, apiKey);
        mGestureDetector = new GestureDetector(context, mDoubleTapListener);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }
    
    private SimpleOnGestureListener mDoubleTapListener = new SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            MapController mc = getController();
            if (mc != null) {
                return mc.zoomInFixing((int) e.getX(), (int) e.getY());
            }
            return false;
        }
    };

}
