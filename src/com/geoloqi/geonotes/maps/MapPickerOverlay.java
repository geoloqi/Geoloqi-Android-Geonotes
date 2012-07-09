package com.geoloqi.geonotes.maps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

import com.geoloqi.geonotes.R;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * This implementation of an {@link Overlay} displays an animated map pin
 * with a highlighted region to aid a user in creating objects on
 * a {@link MapView}.
 * 
 * @author Brian Ledger: Wrote the original code for the first client app.
 * @author Tristan Waddington: Ported the code from the original client
 *         application to the new Geonotes app.
 */
public class MapPickerOverlay extends Overlay {
    /** The fractional value used to determine the size of our selection. */
    public static final float FRACTIONAL_GRADIENT_VALUE = 3.0f / 8.0f;
    
    private static final String TAG = "MapPickerOverlay";
    
    /** The length of the marker pin animation. */
    private static final long ANIM_DURATION = 150L;
    
    /** The default state of the marker pin. */
    private static final int STATIC = 0;
    
    /** The marker pin is animating up. */
    private static final int LIFTING = 1;
    
    /** The marker pin has finished animating up and is holding. */
    private static final int HOLDING = 2;
    
    /** The marker pin is animating down. */
    private static final int DROPPING = 3;
    
    /** The target for our map marker. */
    private final Paint mMarkerTarget;
    
    /** The radial gradient that denotes the boundary of the selection. */
    private final Paint mMarkerRegion;
    
    /** The current state of the marker animation. */
    private int mMarkerState = STATIC;
    
    /** When the animation was started. */
    private long mAnimStart = 0;
    
    /** The map marker. */
    private BitmapDrawable mMarker;
    
    public MapPickerOverlay(Context context) {
        Resources res = context.getResources();
        
        mMarker = new BitmapDrawable(res, BitmapFactory.decodeResource(res,
                R.drawable.marker));
        
        // Paint the target for our map marker
        mMarkerTarget = new Paint();
        mMarkerTarget.setAlpha(0);
        mMarkerTarget.setAntiAlias(true);
        mMarkerTarget.setColor(Color.GRAY);
        mMarkerTarget.setDither(true);
        mMarkerTarget.setStyle(Paint.Style.STROKE);
        mMarkerTarget.setStrokeWidth(2);
        
        // Paint the region for our map marker
        mMarkerRegion = new Paint();
        mMarkerRegion.setAlpha(0);
        mMarkerRegion.setAntiAlias(true);
        mMarkerRegion.setDither(true);
        mMarkerRegion.setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView view) {
        // TODO: Don't trigger the pin up animation when the user
        //       is performing some other animation, like pinch-zoom
        //       or double-tap.
        
        if (event.getPointerCount() == 1) {
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (mMarkerState) {
                case STATIC:
                    // Start animating the marker up.
                    mMarkerState = LIFTING;
                    mAnimStart = System.currentTimeMillis();
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
                    // Start animating the marker down.
                    mMarkerState = DROPPING;
                    mAnimStart = System.currentTimeMillis();
                }
                break;
            }
        }
        return false;
    }
    
    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
        // Get the bounds of our canvas
        Rect clipBounds = canvas.getClipBounds();
        
        // Find the center of the canvas
        int centerX = (clipBounds.left + clipBounds.width()) / 2;
        int centerY = (clipBounds.top + clipBounds.height()) / 2;
        
        // Get the dimensions of our map marker
        int markerWidth = mMarker.getIntrinsicWidth();
        int markerHeight = mMarker.getIntrinsicHeight();
        
        // Get our animation details
        float animDuration = ANIM_DURATION;
        float animElapsed = System.currentTimeMillis() - mAnimStart;
        float animProgress = animElapsed / animDuration;
        
        // Determine if our animation has finished.
        if (animProgress > 1) {
            animProgress = 1;
        }
        
        // Calculate the delta for the next step in our animation
        float animDelta = (float) Math.pow(animProgress, 3); // Quadratic delta
        
        if (shadow) {
            int alpha = 0;
            int maxAlpha = 255;
            
            switch(mMarkerState) {
            case LIFTING:
                // Lift map marker, fade in target
                alpha = Math.round(Math.min(maxAlpha, maxAlpha * animDelta));
                break;
            case HOLDING:
                // Hold map marker, hold target
                alpha = maxAlpha;
                break;
            case DROPPING:
                // Drop map marker, fade out target
                alpha = maxAlpha - Math.round(Math.min(maxAlpha, maxAlpha * animDelta));
                break;
            case STATIC:
                // Hide the target
                alpha = 0;
                break;
            }
            
            // Update the alpha of our marker target
            mMarkerTarget.setAlpha(alpha);
            
            // Draw the marker target
            int lineLength = 12;
            canvas.drawLine(centerX - lineLength, (centerY + 5) - lineLength,
                    centerX + lineLength, (centerY - 5) + lineLength, mMarkerTarget);
            canvas.drawLine(centerX + lineLength, (centerY + 5) - lineLength,
                    centerX - lineLength, (centerY - 5) + lineLength, mMarkerTarget);
            
            // Determine the radius of 
            int radius = Math.round(Math.min(canvas.getWidth(),
                    canvas.getHeight()) * FRACTIONAL_GRADIENT_VALUE);
            
            RadialGradient gradient = new RadialGradient(centerX, centerY, radius,
                    new int[] { 0x8827ADEC, 0x4427ADEC, 0x0027ADEC },
                    new float[] { 0f, .8f, 1f }, TileMode.CLAMP);
            
            mMarkerRegion.setAlpha(Math.abs(alpha - maxAlpha));
            mMarkerRegion.setShader(gradient);
            
            canvas.drawCircle(centerX, centerY, radius, mMarkerRegion);
        } else {
            int displacement = 0;
            int maxDisplacement = 120;
            
            switch(mMarkerState) {
            case LIFTING:
                // Lift map marker, fade in target
                displacement = Math.round(Math.min(maxDisplacement, maxDisplacement * animDelta));
                break;
            case HOLDING:
                // Hold map marker, hold target
                displacement = maxDisplacement;
                break;
            case DROPPING:
                // Drop map marker, fade out target
                displacement = maxDisplacement - Math.round(Math.min(maxDisplacement, maxDisplacement * animDelta));
                break;
            case STATIC:
                // Hide the target
                displacement = 0;
                break;
            }
            
            // Draw our map marker
            mMarker.setBounds(centerX - (markerWidth / 2),
                    centerY - markerHeight - (displacement / 2),
                    centerX + (markerWidth / 2), centerY - (displacement / 2));
            mMarker.draw(canvas);
        }
        
        // Are we still animating?
        if (animProgress == 1) {
            switch (mMarkerState) {
            case LIFTING:
                mMarkerState = HOLDING;
                break;
            case DROPPING:
                mMarkerState = STATIC;
                break;
            }
            
            // Animation finished!
            return false;
        }
        
        // Still animating!
        return true;
    }
}
