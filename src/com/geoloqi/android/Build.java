package com.geoloqi.android;

/**
 * This class contains the current build information for the
 * application. The static tokens will be replaced with
 * real values at build time.
 * 
 * @author Tristan Waddington
 */
public class Build {
    /** The human-readable version of the application. */
    public static final String APP_VERSION = "@version-token@";
    
    /** The build number for this version of the application. */
    public static final String APP_BUILD = "@build-token@";
}
