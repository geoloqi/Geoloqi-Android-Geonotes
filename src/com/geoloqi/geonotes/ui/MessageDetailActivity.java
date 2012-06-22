package com.geoloqi.geonotes.ui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.geoloqi.geonotes.R;
import com.geoloqi.geonotes.utils.FileUtils;

/**
 * Display detailed information about an activity item.
 * 
 * @author Tristan Waddington
 */
public class MessageDetailActivity extends SherlockActivity {
    public static final String EXTRA_JSON = "com.geoloqi.geonotes.ui.extra.JSON";

    private static final String TAG = "MessageDetailActivity";
    private static final String MARKER_IMAGE_URL =
            "http://geoloqi.s3.amazonaws.com/markers/single/marker-images/image.png";

    private static final int STATIC_MAP_WIDTH = 800;
    private static final int STATIC_MAP_HEIGHT = 250;
    private static final int STATIC_MAP_SCALE = 1;

    private JSONObject mMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_detail);
        
        Intent intent = getIntent();
        try {
            // Get the activity object
            mMessage = new JSONObject(intent.getStringExtra(EXTRA_JSON));
            
            JSONObject actor = mMessage.getJSONObject("actor");
            JSONObject location = mMessage.getJSONObject("location");
            JSONObject object = mMessage.getJSONObject("object");
            
            // Display our location name
            TextView nameView = (TextView) findViewById(R.id.actor_name);
            if (nameView != null) {
                nameView.setText(actor.optString("displayName"));
            }
            
            // Display our summary text
            TextView summaryView = (TextView) findViewById(R.id.summary_text);
            if (summaryView != null) {
                summaryView.setText(object.optString("summary"));
            }
            
            // Display our date and actor info
            TextView dateView = (TextView) findViewById(R.id.actor_text);
            if (dateView != null) {
                dateView.setText(String.format("%s | %s",
                        mMessage.optString("displayDate"), actor.optString("displayName")));
            }
            
            // Display our static map
            ImageButton mapView = (ImageButton) findViewById(R.id.static_map);
            if (mapView != null) {
                new LoadStaticMapTask(this, mapView, location.getString("latitude"),
                        location.getString("longitude")).execute();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse message data!");
        }
    }

    /**
     * Build a URL to a Google static map image using the
     * provider lat/long coordinates.
     * 
     * @param lat
     * @param lng
     */
    private static String getStaticMapUrl(String lat, String lng) {
        // Really wish there was a decent URL formatter
        // in the Android standard library.
        String url = "https://maps.google.com/maps/api/staticmap?" +
                "sensor=true&maptype=roadmap" +
                String.format("&size=%sx%s", STATIC_MAP_WIDTH, STATIC_MAP_HEIGHT) +
                String.format("&scale=%s", STATIC_MAP_SCALE) +
                String.format("&visible=%s,%s", lat, lng) +
                String.format("&markers=icon:%s|%s,%s", Uri.encode(MARKER_IMAGE_URL), lat, lng);
        return url;
    }

    /**
     * Return the filename for a cached map image from the given map URL.
     * @param url
     */
    private static String getMapFilename(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(url.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            return number.toString(16);
        } catch (NoSuchAlgorithmException e) {
            // Pass
        }
        return null;
    }

    /**
     * Retrieve a Google static map image from the web or a
     * local image cache.
     * 
     * @author Tristan Waddington
     */
    private static class LoadStaticMapTask extends AsyncTask<Object, Object, Bitmap> {
        private final Context mContext;
        private final ImageView mView;
        private final String mUrl;

        public LoadStaticMapTask(Context context, ImageView view, String lat, String lng) {
            mContext = context;
            mView = view;
            mUrl = getStaticMapUrl(lat, lng);
        }

        @Override
        protected Bitmap doInBackground(Object...o) {
            File imageFile = new File(mContext.getCacheDir(), getMapFilename(mUrl));
            if (!imageFile.exists()) {
                // Download map from the web
                try {
                    URL url = new URL(mUrl);
                    
                    // Open our URL connection
                    HttpURLConnection urlConnection =
                            (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(10000);
                    try {
                        InputStream in = new BufferedInputStream(
                              urlConnection.getInputStream());

                        // Write the file to disk
                        FileUtils.writeFileToDisk(imageFile, in);
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (ClientProtocolException e) {
                    Log.e(TAG, "Failed to load static map from the web!", e);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to load static map from the web!", e);
                }
            }
            return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                mView.setImageBitmap(bitmap);
            }
        }
    }
}
