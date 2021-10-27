package com.jumpinjumpout.apk.lib;

public class Constants_lib {
    public static final String DRIVER = "D";
    public static final String TRIP_TYPE_USER = "U";
    public static final String TRIP_TYPE_COMMERCIAL = "CD";
    public static final String VEHICLE_CATEGORY_LONG_DISTANCE_CAR = "L";

    public static final String CITY_OR_TOWN_CITY = "C";
    public static final String CITY_OR_TOWN_TOWN = "T";

    public static final String PREF_SAVE_TRIP_CREATE_RESPONSE = "PREF_SAVE_TRIP_CREATE_RESPONSE";
    public static final String PREF_MYLOCATION_LAT = "PREF_MYLOCATION_LAT";
    public static final String PREF_MYLOCATION_LON = "PREF_MYLOCATION_LON";

    public static final String TRIP_PATH = "TRIP_PATH";
    // Trip
    public static final String TRIP_FROM_ADDRESS = "TRIP_FROM_ADDRESS";
    public static final String TRIP_TO_ADDRESS = "TRIP_TO_ADDRESS";
    public static final String TRIP_JSON_OBJECT = "TRIP_JSON_OBJECT";
    public static final String NOTIFICATION_TYPE_BEGIN = "TA_B";
    // Trip
    public static final String SENDER_ID = "784677467274";
    public static final String COL_LAT = "lat";
    public static final String COL_LNG = "lng";
    public static final double DEFAULT_WALKING_DISTANCE = 2; // in km
    public static final double JUMP_IN_DISTANCE = 150; // 150 meters
    public static final String COL_IN_PATH = "inpath";
    public static final String COL_PHONE_NO = "phoneno";
    public static final String COL_IS_TRACKING = "istracking";
    public static final String COL_HAS_JOINED = "has_joined";
    public static final String COL_HAS_JUMPED_IN = "has_jumped_in";
    public static final String COL_HAS_JUMPED_OUT = "has_jumped_out";
    public static final String COL_CANCEL_JOIN = "cancel_join";
    public final static int MAXADDRESS = 15;
    public final static int MAXTRIPS = 10;
    public static final String PREF_APPUSERID = "PREF_APPUSERID";
    public static final String PREFS_VERSION_PERSISTENT = "PREFS_VERSION_PERSISTENT";
    public static final String PREF_TRIP_ACTION = "PREF_TRIP_ACTION";
    public static final String PREF_REGISTERED = "PREF_REGISTERED";
    public static final String PREF_VERIFICATION_CODE = "PREF_VERIFICATION_CODE";
    public static final String PREF_IN_TRIP = "IN_TRIP";
    public static final String PREF_TRIP_TYPE = "PREF_TRIP_TYPE";
    public static final String PREF_PHONENO = "PREF_PHONENO";
    public static final String PREF_COUNTRY_CODE = "PREF_COUNTRY_CODE";
    public static final String PREF_RECENT_ADDRESSES = "PREF_RECENT_ADDRESSES";
    public static final String PREF_FROM_ADDRESS = "PREF_FROM_ADDRESS";
    public static final String PREF_TO_ADDRESS = "PREF_TO_ADDRESS";
    public static final String PREF_SCHEDULE_ID = "PREF_SCHEDULE_ID";
    public static final String PREF_SCHEDULE_TRIP_CREATE = "PREF_SCHEDULE_TRIP_CREATE";

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PREF_TOKEN_SAVED = "PREF_TOKEN_SAVED";
    public static final long DRIVER_UPDATE_INTERVAL_USER = 30 * 1000;

    public static final int VOLLEY_NETYWORK_ERROR = -9;

    public static final long DRIVER_CHECK_NOTIFICATION_USER = 2 * 1000;

    // // Preferences
    public static final int TWO_MINUTES = 1000 * 60 * 2;
    // Map variables
    public static final int MAP_PADDING = 50;
    // Server stuff
    public static final String VERSION = "v45";
    public static final String APPNAME = "jumpinjumpoutapp";
    public static final String ALPHA = "/dev/alpha";
    // public static final String ALPHA = "/dev/beta";
    //public static final String ALPHA = "/prod";
    public static final String AUTHORITY = "www.jumpinjumpout.com";
    public static final String SITE = "https://" + AUTHORITY;
    // public static final String SITE = "http://hp-ferrari/sites";
    public static final String SITE_DIRECTORY = SITE + ALPHA + "/" + APPNAME;
    public static final String SVR_DIRECTORY = SITE_DIRECTORY + "/svr/apk/"
            + VERSION + "/";
    public static final String PHP_DIRECTORY = ALPHA + "/" + APPNAME
            + "/svr/apk/" + VERSION + "/php/";
    public static final String PHP_PATH = SITE + PHP_DIRECTORY;

    public static final String ADD_TRIP_SCHEDULE_URL = PHP_PATH + "add_trip_schedule.php";
    public static final String DELETE_TRIP_SCHEDULE_URL = PHP_PATH + "delete_trip_schedule.php";
    public static final String SEND_LOG_TO_SERVER_URL = PHP_PATH
            + "log_to_server_sar_apk.php";
    public static final String GET_CITY_URL = PHP_PATH + "get_city.php";
    // flags
    public static final String PREF_TRIP_ID_INT = "PREF_TRIP_ID_INT";
    public static final String PREF_CURRENT_WAITING_COUNT = "PREF_CURRENT_WAITING_COUNT";
    public static final String PREF_CURRENT_JUMP_COUNT = "PREF_CURRENT_JUMP_COUNT";
    public static final String PREF_TRIP_DISTANCE_TRAVELED = "PREF_TRIP_DISTANCE_TRAVELED";
    public static final String PREF_PASSENGER_JUMPINOUT_DISTANCE = "PREF_PASSENGER_JUMPINOUT_DISTANCE";
    // Trip action
    public static final String TRIP_ACTION_CREATE = "C"; // Planned - dormant
    public static final String TRIP_ACTION_BEGIN = "B"; // In trip begin -
    // active
    public static final String TRIP_ACTION_SCHEDULE = "S"; // Scheduled -
    // delayed start
    public static final String TRIP_ACTION_PAUSE = "P"; // In trip paused -
    // active
    public static final String TRIP_ACTION_ABORT = "A"; // In trip abort -
    // active
    public static final String TRIP_ACTION_RESUME = "R"; // In trip resumed -
    // active
    public static final String TRIP_ACTION_END = "E"; // trip end - passive
    public static final String TRIP_ACTION_NONE = "N"; // Accessed driver -
    // passive
    public static final String TRIP_ACTION_LATER = "L";
    // Connectivity
    public static final double INVALIDLAT = -999;
    public static final double INVALIDLNG = -999;
    // Notifications
    public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final float NEAR_DESTINATION_DISTANCE = 150;
    // Distance from user in meter
    public static final int DISTANCE_FAR = 10000; // Relax
    public static final int DISTANCE_NEAR = 5000; // Ready
    public static final int DISTANCE_CLOSE = 2000; // Pants on fire
    public static final int DISTANCE_SAME_FROM = 500;
    // Delays for polling
    public static final long PASSIVE_INTERVAL = 1000 * 60 * 2; // minutes
    public static final long DRIVER_UPDATE_INTERVAL = 60 * 1000;
    public static final long INTERNET_CONNECTION_INTERVAL = 2 * 1000;
    public static final long ONE_SEC_INTERVAL = 1000;
    public static final long PASSENGER_UPDATE_INTERVAL_PASSIVE = 30 * 1000; // half
    // minute
    public static final long PASSENGER_UPDATE_INTERVAL_ACTIVE = 5000;
    // Trip information for commercial cabs
    public static final int SUCCESS_RESULT = 0;
    public static final String PREF_RECENT_TRIP = "PREF_RECENT_TRIP";
    // Service communication with activity
    public static final String PASSENGER_EVENT = "passenger_joined";

    public static final String CANCEL_DRIVER = "cancel_driver";

    public static final String HAS_MARATHI_ADDRESS = "HAS_MARATHI_ADDRESS";

    public class GData {
        // Milliseconds per second
        public static final int MILLISECONDS_PER_SECOND = 1000;
        // The update interval
        public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
        // A fast interval ceiling
        public static final int FAST_CEILING_IN_SECONDS = 1;
        // Update interval in milliseconds
        public static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
                * UPDATE_INTERVAL_IN_SECONDS;
        // A fast ceiling of update intervals, used when the app is visible
        public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
                * FAST_CEILING_IN_SECONDS;
    }
}
