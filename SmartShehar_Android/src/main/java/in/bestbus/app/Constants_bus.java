package in.bestbus.app;

import lib.app.util.Constants_lib_ss;

/**
 * Created by jijo_soumen on 19/01/2016.
 */
public class Constants_bus {
    public static final String VERSION = "v17";
    public static final String APPNAME = "smartsheharapp";
    public static final String ALPHA = Constants_lib_ss.ALPHA;
    //public static final String ALPHA = "/beta";
   // public static final String ALPHA = "/prod";
    public static final String AUTHORITY = "www.smartshehar.com";
    public static final String SITE = "http://" + AUTHORITY;
    // public static final String SITE = "http://hp-ferrari/sites";
    public static final String SITE_DIRECTORY = SITE + ALPHA + "/" + APPNAME;
    public static final String SVR_DIRECTORY = SITE_DIRECTORY + "/svr/apk/"
            + VERSION + "/";
    public static final String PHP_DIRECTORY = ALPHA + "/" + APPNAME + "/" + VERSION
            + "/svr/php/";
    public static final String PHP_PATH = SITE + PHP_DIRECTORY;
    public static final long DRIVER_UPDATE_INTERVAL = 60 * 1000;
    public static final String PREFS_VERSION_PERSISTENT = "PREFS_VERSION_PERSISTENT";
    public static final String PREF_ETA_BUTTON_CLICK = "PREF_ETA_BUTTON_CLICK";
    public static final String PREF_ETA_BUTTON_CLICK_POSITION_VALUE = "PREF_ETA_BUTTON_CLICK_POSITION_VALUE";
    public static String GET_BUS_LOCATION_URL = PHP_PATH + "bus/get_bus_location.php";
    public static String SHARE_BUS_LOCATION_URL = PHP_PATH + "bus/share_bus_location.php";
    public static String DA_USER_ACCESS_URL = PHP_PATH + "da_user_access.php";
    public static final String APP_TYPE = "SSB";
    public static final int DIRECT_BUSES_WALKING_DISTANCE = 500;
    public static final String DIRECT_STATIONS_TO_BUSES_STNAME = "DIRECT_STATIONS_TO_BUSES_STNAME";
    public static final String BUS_STOP = "BUS_STOP";
    public static final String BUS_MAP = "BUS_MAP";
}
