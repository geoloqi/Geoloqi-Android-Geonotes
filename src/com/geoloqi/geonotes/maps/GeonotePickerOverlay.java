package com.geoloqi.geonotes.maps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;

import com.geoloqi.geonotes.R;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * Stub...
 * 
 * @author Brian Ledger: Wrote the original code for the first client app.
 * @author Tristan Waddington: Ported the code from the original client
 *         application to the new Geonotes app.
 */
public class GeonotePickerOverlay extends Overlay {
    private static final String TAG = "GeonotePickerOverlay";
    
    /** Stub */
    private static final double PROJECTION_ANGLE = 45.0 * (Math.PI / 180.0);
    
    /** Stub */
    private static final double SIN_PROJECTION_ANGLE = Math.sin(PROJECTION_ANGLE);
    
    /** Stub */
    private static final double COS_PROJECTION_ANGLE = Math.cos(PROJECTION_ANGLE);
    
    /** Stub */
    private static final long LINGER_TIME = 500L;
    
    /** Stub */
    private static final long LIFT_TIME = 150L;
    
    /** Stub */
    private static final long DROP_TIME = 500L;

    /** The default state of the marker pin. */
    private static final int STATIC = 0;
    
    /** The marker pin is animating up. */
    private static final int LIFTING = 1;
    
    /** The marker pin has finished animating up and is holding. */
    private static final int HOLDING = 2;
    
    /** The marker pin is lingering before animating down. */
    private static final int LINGERING = 3;
    
    /** The marker pin is animating down. */
    private static final int DROPPING = 4;
    
    /** Stub */
    private int mMarkerState = STATIC;
    
    /** Stub */
    private int mMarkerDisplacement = 0;
    
    /** Stub */
    private int mTargetAlpha = 0;
    
    /** Stub */
    private long mLastStage = 0;
    
    /** Stub */
    private long mAnimStart = 0;
    
    private MapView mMapView;
    private BitmapDrawable mMarker;
    private BitmapDrawable mMarkerShadow;
    
    
    public GeonotePickerOverlay(Context context, MapView map) {
        Resources res = context.getResources();
        
        mMapView = map;
        mMarker = new BitmapDrawable(res, BitmapFactory.decodeResource(res,
                R.drawable.marker));
        mMarkerShadow = new BitmapDrawable(res, BitmapFactory.decodeResource(res,
                R.drawable.marker_shadow));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView view) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            switch (mMarkerState) {
            case STATIC:
                // Start animating the marker up.
                mMarkerState = LIFTING;
                mAnimStart = System.currentTimeMillis();
                
                Log.d(TAG, "ACTION_DOWN");
                
                break;
            case LINGERING:
                // Don't start animating the marker down. Instead hold
                // it in the up position.
                mMarkerState = HOLDING;
                break;
            }
            break;
        case MotionEvent.ACTION_UP:
            switch (mMarkerState) {
            case LIFTING:
                // Start animating the marker down.
                mMarkerState = DROPPING;
                break;
            case HOLDING:
                // Start the marker lingering before animating down.
                //mMarkerState = LINGERING;
                mMarkerState = DROPPING;
                mAnimStart = System.currentTimeMillis();
                Log.d(TAG, "ACTION_UP");
            }
            break;
        }
        return false;
    }
    
    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
        
        if (mMarkerState == STATIC || !shadow) {
            return false;
        }
        
        Rect clipBounds = canvas.getClipBounds();
        //Log.d(TAG, "clipBounds: "+clipBounds.toShortString());
        
        int density = canvas.getDensity();
        //Log.d(TAG, "density: "+density);
        
        // Find the center of the canvas
        int centerX = (clipBounds.left + clipBounds.width()) / 2;
        int centerY = (clipBounds.top + clipBounds.height()) / 2;
        
        // Get the dimensions of our map marker
        int markerWidth = mMarker.getIntrinsicWidth() * (density / 160);
        int markerHeight = mMarker.getIntrinsicHeight() * (density / 160);
        
        // Get the dimensions of our marker shadow
        int shadowWidth = mMarkerShadow.getIntrinsicWidth() * (density / 160);
        int shadowHeight = mMarkerShadow.getIntrinsicHeight() * (density / 160);
        
        // ...?
        double projH = COS_PROJECTION_ANGLE * (mMarkerDisplacement / 2);
        int dispX = (int) (SIN_PROJECTION_ANGLE * projH);
        int dispY = (int) (COS_PROJECTION_ANGLE * projH);
        
        // ...
        int pixelGradientRadius = 120;
        
        // ...
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.DKGRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        
        float duration = 250;
        
        int to = 255;
        
        // ...
        long timePassed = System.currentTimeMillis() - mAnimStart;
        
        // ...
        float progress = timePassed / duration;
        
        // ...?
        if (progress > 1) {
            progress = 1;
        }
        Log.d(TAG, String.format("%s / %s = %.5f", timePassed, duration, progress));
        
        // delta
        float delta = progress; //Linear
        
        // step
        if (mMarkerState == LIFTING) {
            mTargetAlpha = Math.round(Math.min(255, to * delta));
            Log.d(TAG, "if: "+mMarkerState);
        } else if (mMarkerState == DROPPING) {
            Log.d(TAG, "else: "+mMarkerState);
            mTargetAlpha = to - Math.round(Math.min(255, to * delta));
        }
        Log.d(TAG, "alpha: "+mTargetAlpha);
        
        paint.setAlpha(mTargetAlpha);

        int lineLength = 12;
        canvas.drawLine(centerX - lineLength, (centerY + 5) - lineLength,
                centerX + lineLength, (centerY - 5) + lineLength, paint);
        canvas.drawLine(centerX + lineLength, (centerY + 5) - lineLength,
                centerX - lineLength, (centerY - 5) + lineLength, paint);
        
        if (progress >= 1) {
            switch (mMarkerState) {
            case LIFTING:
                mMarkerState = HOLDING;
                break;
            case DROPPING:
                mMarkerState = STATIC;
                break;
            }
            return false;
        }
        // Still animating!
        return true;

        /*
        if (shadow) {
            Log.d(TAG, "Drawing shadows...");
            
            // Iteratively tween the map marker
            switch (mMarkerState) {
            case LIFTING:
                double tweenL = Math.min(1.0, ((double) (when - mLastStage) / LIFT_TIME));
                Log.d(TAG, "tweenL: "+tweenL);
                if (tweenL == 1.0) {
                    mMarkerDisplacement = pixelGradientRadius - 5;
                    mMarkerState = HOLDING;
                } else {
                    mMarkerDisplacement = (int) ((pixelGradientRadius - 10) * tweenL);
                }
                break;
            case LINGERING:
                if (when > mLastStage + LINGER_TIME) {
                    mLastStage = when;
                    mMarkerState = DROPPING;
                }
                break;
            case DROPPING:
                double tweenD = 1.0 - Math.min(1.0, ((double) (when - mLastStage) / DROP_TIME));
                if (tweenD == 0.0) {
                    mMarkerDisplacement = 0;
                    mMarkerState = STATIC;
                } else {
                    mMarkerDisplacement = (int) (Math.abs(Math.exp(-(1.2 - tweenD) * 2.0) * Math.cos(2. * Math.PI * tweenD)) * pixelGradientRadius);
                }
            }

            int lineLength = 12;
            canvas.drawLine(centerX - lineLength, (centerY + 5) - lineLength,
                    centerX + lineLength, (centerY - 5) + lineLength, paint);
            canvas.drawLine(centerX + lineLength, (centerY + 5) - lineLength,
                    centerX - lineLength, (centerY - 5) + lineLength, paint);

            // ...
            mMarkerShadow.setBounds(centerX - (shadowWidth / 2),
                    centerY - shadowHeight - (markerDisplacement / 2),
                    centerX + shadowWidth, centerY - (markerDisplacement / 2));
            //mMarkerShadow.setBounds(centerX + dispX - 2, centerY - shadowHeight - dispY + 2,
            //        centerX + shadowWidth + dispX - 2, centerY - dispY + 2);
            mMarkerShadow.draw(canvas);
        } else {
            Log.d(TAG, "Drawing markers...");
            
            
            // ...
            //mMarker.setBounds(centerX - (markerWidth / 2),
            //        centerY - markerHeight - (mMarkerDisplacement / 2),
            //        centerX + (markerWidth / 2), centerY - (mMarkerDisplacement / 2));
            mMarker.draw(canvas);
        }
        return mMarkerState != STATIC;
        */
    }
}
