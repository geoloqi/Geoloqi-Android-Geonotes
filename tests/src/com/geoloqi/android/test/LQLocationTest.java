package com.geoloqi.android.test;

import org.json.JSONException;
import org.json.JSONObject;

import com.geoloqi.android.sdk.LQSession;
import com.geoloqi.android.sdk.data.LQLocation;
import com.geoloqi.android.sdk.provider.LQContract;
import com.geoloqi.android.sdk.provider.LQDatabaseHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.os.Build;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This class tests the various contortions of {@link #LQLocation}.
 * 
 * @author Tristan Waddington
 */
public class LQLocationTest extends TestCase {
    private Location mLocation;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        // This method runs prior to each test!
        
        // Create a Location fixture
        mLocation = new Location("test");
        mLocation.setAccuracy(14);
        mLocation.setAltitude(5.900000095367432);
        mLocation.setLatitude(45.5267281);
        mLocation.setLongitude(-122.68007024);
        mLocation.setSpeed(0);
        mLocation.setTime(System.currentTimeMillis());
    }
    
    /**
     * Ensure that a new {@link #LQLocation} object can be created
     * from a native {@link #Location} with no loss in data
     * precision. 
     */
    public void testFromLocation() {
        LQLocation lqLocation = new LQLocation(mLocation);
        
        // Verify the new lqLocation fields match
        assertEquals(lqLocation.getAccuracy(),mLocation.getAccuracy());
        assertEquals(lqLocation.getAltitude(), mLocation.getAltitude());
        assertEquals(lqLocation.getBearing(), mLocation.getBearing());
        assertEquals(lqLocation.getLatitude(), mLocation.getLatitude());
        assertEquals(lqLocation.getLongitude(), mLocation.getLongitude());
        assertEquals(lqLocation.getProvider(), mLocation.getProvider());
        assertEquals(lqLocation.getSpeed(), mLocation.getSpeed());
        assertEquals(lqLocation.getTime(), mLocation.getTime());
        assertEquals(lqLocation.getExtras(), mLocation.getExtras());
        
        // Verify battery
        assertEquals(lqLocation.getBattery(), 0);
        lqLocation.setBattery(73);
        assertEquals(lqLocation.getBattery(), 73);
    }
    
    /**
     * Ensure that a new {@link #LQLocation} created from a database
     * cursor will have the expected values with no loss in precision.
     */
    public void testFromCursor() {
        // TODO: This is an important method to test, but will require some
        //       work to get testing properly. Coming back to this later.
    }
    
    /**
     * Ensure that the {@link LQLocation#toJson} method accurately
     * converts the location data to the expected JSON representation.
     */
    public void testToJson() {
        LQLocation lqLocation = new LQLocation(mLocation);
        
        // Convert the location to a JSONObject
        JSONObject json = new JSONObject();
        try {
            json = lqLocation.toJson();
        } catch (JSONException e) {
            Assert.fail(e.getMessage());
        }
        
        // Ensure the date is included and formatted correctly
        String date = json.optString("date");
        assertNotNull(date);
        assertEquals(date, LQSession.formatTimestamp(mLocation.getTime()));
        
        // Ensure the raw object is included
        JSONObject raw = json.optJSONObject("raw");
        assertNotNull(raw);
        assertEquals(raw.optInt("battery"), lqLocation.getBattery());
        
        // Ensure the location object is included
        JSONObject location = json.optJSONObject("location");
        assertNotNull(location);
        assertEquals(location.optString("type"), "point");
        assertEquals(location.optString("source"), mLocation.getProvider());
        
        // Ensure the position values are included and correct
        JSONObject position = location.optJSONObject("position");
        assertNotNull(position);
        assertEquals(position.optString("horizontal_accuracy"),
                        String.valueOf(mLocation.getAccuracy()));
        assertEquals(position.optString("altitude"),
                        String.valueOf(mLocation.getAltitude()));
        assertEquals(position.optString("latitude"),
                        String.valueOf(mLocation.getLatitude()));
        assertEquals(position.optString("longitude"),
                        String.valueOf(mLocation.getLongitude()));
        assertEquals(position.optString("speed"),
                        String.valueOf(mLocation.getSpeed()));
    }
    
    /**
     * Ensure that the {@link LQLocation#toContentValues} method
     * accurately converts the location to a {@link ContentValues}
     * representation of the data.
     */
    public void testToContentValues() {
        LQLocation lqLocation = new LQLocation(mLocation);
        
        // Verify the expected size of the content values
        ContentValues cv = lqLocation.toContentValues();
        assertEquals(cv.size(), 8);
        
        // Compare the values to those of the original location object
        assertEquals(cv.getAsString(LQContract.Fixes.PROVIDER),
                        lqLocation.getProvider());
        assertEquals(cv.getAsString(LQContract.Fixes.LATITUDE),
                        String.valueOf(lqLocation.getLatitude()));
        assertEquals(cv.getAsString(LQContract.Fixes.LONGITUDE),
                        String.valueOf(lqLocation.getLongitude()));
        assertEquals(cv.getAsString(LQContract.Fixes.SPEED),
                        String.valueOf(lqLocation.getSpeed()));
        assertEquals(cv.getAsString(LQContract.Fixes.ALTITUDE),
                        String.valueOf(lqLocation.getAltitude()));
        assertEquals(cv.getAsString(LQContract.Fixes.HORIZONTAL_ACCURACY),
                        String.valueOf(lqLocation.getAccuracy()));
        assertEquals(cv.getAsString(LQContract.Fixes.BATTERY),
                        String.valueOf(lqLocation.getBattery()));
        assertEquals(cv.getAsString(LQContract.Fixes.FIX_CREATED_ON),
                        String.valueOf(lqLocation.getTime()));
    }
}
