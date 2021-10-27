package lib.app.util;


public class Constants_lib_ss {

    public static final int SUCCESS_RESULT = 0;
    public static int Request_Code = 101;
    public static final int FAILURE_RESULT = 1;
    public static String PREF_CAMERA_PHOTO_PATH = "PREF_CAMERA_PHOTO_PATH";
    public static String PREF_IMAGE_NAME = "PREF_IMAGE_NAME";
    public static String PREF_MY_LOCATION = "PREF_MY_LOCATION";
    public static final String PACKAGE_NAME = "com.smartshehar.citizen.android.app";
    public static final String PREF_APPUSERID = "PREF_APPUSERID";
    public static final String PREF_APPUSAGEID = "PREF_APPUSAGEID";
    public static final String RECEIVER = PACKAGE_NAME + ".ui.RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME
            + ".ui.RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME
            + ".LOCATION_DATA_EXTRA";
    public static final long INVALIDLAT = -999;
    public static final long INVALIDLNG = -999;
    public static final String PREF_RECENT_ADDRESSES = "PREF_RECENT_ADDRESSES";
    public static final int MAXADDRESS = 15;
    public static final int FINDADDRESS_FROM = 5;
    public static final int FINDADDRESS_TO = 6;
    public static final int TYPELIST_SUBTYPE = 4;
    public static final String PREFS_VERSION_PERSISTENT = "PREFS_VERSION_PERSISTENT";

    public static final String VERSION = "v1";
    public static final String APPNAME = "cabeapp";
    public static final String ALPHA = "/alpha";
    //public static final String ALPHA = "/beta";
    //public static final String ALPHA = "/prod";
    public static final String AUTHORITY = "www.cabebooking.com";
    public static final String SITE = "https://" + AUTHORITY;
    public static final String PHP_DIRECTORY = ALPHA + "/" + APPNAME + "/" + VERSION + "/svr/php/";
    public static final String PHP_PATH = SITE + PHP_DIRECTORY;

    public static final String PREF_VERIFICATION_CODE = "PREF_VERIFICATION_CODE";
    public static final String PREF_PHONENO = "PREF_PHONENO";
    public static final String PREF_REGISTERED = "PREF_REGISTERED";
    public static final String PREF_SKIPPED = "PREF_SKIPPED";
    public static final String PREF_COUNTRY_CODE = "PREF_COUNTRY_CODE";
    public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String SEND_LOG_TO_SERVER_URL = PHP_PATH
            + "log_to_server_sar_apk.php";
    /// train app
    public static final String ADD_CALL_HOME_URL = PHP_DIRECTORY + "add_callhome.php";
    public static final String UPGRADE_APP_URL = PHP_PATH + "upgrade_app.php";
    /// issue image directory
    public static String OFFENCE_IMAGES_URL = SITE + ALPHA + APPNAME;
    public static final long INTERNET_CONNECTION_INTERVAL = 2 * 1000;
    public static final String TRIP_JSON_OBJECT = "TRIP_JSON_OBJECT";
    public static final String TRIP_FROM_ADDRESS = "TRIP_FROM_ADDRESS";
    public static final String TRIP_TO_ADDRESS = "TRIP_TO_ADDRESS";
    public static final String TRIP_PATH = "TRIP_PATH";
    public static final int DISTANCE_SAME_FROM = 500;
    public static final int MAP_PADDING = 50;
    public static final long DRIVER_UPDATE_INTERVAL = 30 * 1000;
    public static final String PREF_TOKEN_SAVED = "PREF_TOKEN_SAVED";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String KEY_PREF_EMAIL = "KEY_PREF_EMAIL";
}
