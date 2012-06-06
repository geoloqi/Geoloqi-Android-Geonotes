package com.geoloqi.geonotes.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A collection of static utility methods for working
 * with {@link File} objects and input/output streams.
 * 
 * @author Tristan Waddington
 */
public class FileUtils {
    /** The size of one kilobyte in bytes. */
    public static final int ONE_KB = 1024;
    
    /** The size of one megabyte in bytes. */
    public static final int ONE_MB = ONE_KB * 1024;
    
    /** The size of one gigabyte in bytes. */
    public static final int ONE_GB = ONE_MB * 1024;
    
    /** The buffer size to use when reading or writing files to disk. */
    public static final int BUFFER_SIZE = (32 * 1024);
    
    /**
     * Calculates the size of the given {@link File} in bytes. If the
     * given File is a directory, the method will recurse over all
     * children and return the total size of the directory.
     * 
     * @param path
     * @return The total size in bytes.
     */
    public static long sizeOf(File path) {
        long size = 0;
        
        if (path != null) {
            size += path.length();
            
            if (path.isDirectory()) {
                String[] children = path.list();
                for (String child : children) {
                    File file = new File(path, child);
                    
                    if (file.isDirectory()) {
                        // Recurse
                        size += sizeOf(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        }
        return size;
    }
    
    /**
     * Takes a file size in bytes and returns a human-readable
     * String indicating the size in one of KB, MB or GB.
     * 
     * @param size
     * @return A human readable String indicating size.
     */
    public static String formatFileSize(long size) {
        NumberFormat nf = DecimalFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        
        if (size > ONE_GB)
            return nf.format((float) size / ONE_GB) + "GB";
        else if (size > ONE_MB)
            return nf.format((float) size / ONE_MB) + "MB";
        else
            return nf.format((float) size / ONE_KB) + "KB";
    }
    
    /**
     * Empty a directory, recursively deleting all children.
     * 
     * @param dir
     * @return true if the directory was emptied; false if otherwise.
     */
    public static boolean emptyDirectory(File dir) {
        if (dir.isDirectory()) {
            for (String child : dir.list()) {
                File file = new File(dir, child);
                if (file.isDirectory()) {
                    // Delete the directory
                    deleteDirectory(file);
                } else {
                    // Delete file
                    file.delete();
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Delete a directory and all of it's contents.
     * 
     * @param dir
     * @return true if the directory was removed; false if otherwise;
     */
    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            if (emptyDirectory(dir)) {
                // Delete the directory
                return dir.delete();
            }
        }
        return false;
    }
    
    /**
     * <p>Takes an {@link InputStream}, wraps it with a
     * {@link BufferedInputStream} and writes the bytes to disk.</p>
     * 
     * <p>Note that this method will close the given InputStream when
     * finished.</p>
     * 
     * @param file
     * @param inputStream
     * @return the File that was written to.
     */
    public static File writeFileToDisk(File file, InputStream inputStream) throws
            FileNotFoundException, IOException {
        // Create our buffered streams
        BufferedInputStream bufferedInputStream = new BufferedInputStream(
                inputStream, BUFFER_SIZE);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                new FileOutputStream(file), BUFFER_SIZE);
        
        try {
            // Write the file to disk
            int lengthRead = 0;
            byte [] buffer = new byte[BUFFER_SIZE];
            while ((lengthRead = bufferedInputStream.read(buffer)) != -1) {
                bufferedOutputStream.write(buffer, 0, lengthRead);
            }
        } finally {
            bufferedInputStream.close();
            bufferedOutputStream.close();
        }
        return file;
    }
}
