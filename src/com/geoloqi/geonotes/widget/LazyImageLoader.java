package com.geoloqi.geonotes.widget;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import com.geoloqi.geonotes.utils.FileUtils;

/**
 * A lazy image loader class for downloading images on a background
 * thread, caching them to disk and finally updating the
 * corresponding {@link ImageView} in a {@link ListView} row.
 * 
 * @author Tristan Waddington
 */
public class LazyImageLoader {
    private static final String TAG = "LazyImageLoader";
    
    private static final int HTTP_CONNECT_TIMEOUT = 2500;
    private static final int HTTP_SOCKET_TIMEOUT = 5000;
    
    /** The bitmap options used when loading images from disk. */
    private static BitmapFactory.Options sBitmapOptions;
    
    /** The singleton instance of the object. */
    private static LazyImageLoader sInstance;
    
    /** The Activity context. */
    private final Context mContext;
    
    /** Our DefaultHttpClient instance. */
    private final DefaultHttpClient mClient;
    
    /** An instance of {@link ExecutorService} for downloading remote images. */
    private final ExecutorService mDownloadExecutor;
    
    private LazyImageLoader(Context context) {
        mContext = context;
        mClient = new DefaultHttpClient();
        mDownloadExecutor = Executors.newSingleThreadExecutor();
        
        // Set default client parameters
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params,
                HTTP_CONNECT_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params,
                HTTP_SOCKET_TIMEOUT);
        mClient.setParams(params);
    }
    
    /**
     * Get a singleton instance of {@link LazyImageLoader}.
     */
    public static LazyImageLoader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LazyImageLoader(context);
        }
        return sInstance;
    }
    
    /**
     * <p>Given an {@link ImageViewHolder} instance, the lazy image
     * loader will attempt to retrieve the image specified by
     * {@link ImageViewHolder#imageUrl} from cache. If the image
     * does not exist in a local cache, the loader will spawn a
     * download task on a background thread.</p>
     * 
     * <p>When the loader has found an image, it will post a
     * runnable to the main thread to update the image view.</p>
     * 
     * @param holder
     */
    public void loadImage(final ImageViewHolder holder) {
        mDownloadExecutor.execute(new ImageDownload(holder));
    }
    
    /**
     * Returns a reference to the image {@link File} on disk.
     */
    private File getImageFile(String url) {
        return new File(mContext.getCacheDir(), getFilename(url));
    }
    
    /**
     * Get the on-disk filename for an image url; returns null if an error
     * occurred.
     */
    private String getFilename(String url) {
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
     * A {@link Runnable} that will download a remote image, cache
     * it to disk and, when finished, update an ImageView.
     * 
     * @author Tristan Waddington
     */
    private class ImageDownload implements Runnable {
        private final ImageViewHolder mHolder;
        private final String mUrl;
        
        public ImageDownload(ImageViewHolder holder) {
            mHolder = holder;
            mUrl = holder.imageUrl;
        }
        
        @Override
        public void run() {
            try {
                // Get the path to the image on disk
                File imageFile = getImageFile(mUrl);
                
                if (!imageFile.exists()) {
                    URI uri = URI.create(mUrl);
                    Log.d(TAG, String.format("Downloading image from '%s'", uri));
                    
                    // Build our request
                    HttpGet request = new HttpGet();
                    request.setURI(uri);
                    
                    // Execute the request
                    HttpResponse response = mClient.execute(request);
                    StatusLine status = response.getStatusLine();
                    
                    if (status.getStatusCode() == HttpStatus.SC_OK) {
                        HttpEntity entity = response.getEntity();
                        
                        // Write the bitmap to disk
                        FileUtils.writeFileToDisk(imageFile, entity.getContent());
                    } else {
                        Log.w(TAG, String.format(
                                "Image download failed with status: %s!", status));
                    }
                }
                
                // Load the bitmap into memory
                final Bitmap bitmap = BitmapFactory.decodeFile(
                        imageFile.getAbsolutePath(), sBitmapOptions);
                
                // Set the bitmap on the main thread
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mUrl.equals(mHolder.imageUrl)) {
                            mHolder.image.setImageBitmap(bitmap);
                        }
                    }
                });
            } catch (IllegalStateException e) {
                Log.w(TAG, "Failed to download image!");
            } catch (ClientProtocolException e) {
                Log.w(TAG, "Failed to download image!");
            } catch (IOException e) {
                Log.w(TAG, "Failed to download image!");
            }
        }
    }
}
