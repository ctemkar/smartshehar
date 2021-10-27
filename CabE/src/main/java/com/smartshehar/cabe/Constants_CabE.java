package com.smartshehar.cabe;

/**
 * Created by jijo_soumen on 08/03/2016.
 * Constants for CabE app
 */
public class Constants_CabE {

    public static final String APP_CODE = "CEP";

    public static final String VERSION = "v1";
    public static final String APPNAME = "cabeapp";
    public static final String ALPHA = "/alpha";
    //public static final String ALPHA = "/beta";
    //public static final String ALPHA = "/prod";
    public static final String AUTHORITY = "www.cabebooking.com";
    public static final String SITE = "https://" + AUTHORITY;
    public static final String PHP_DIRECTORY = ALPHA + "/" + APPNAME + "/" + VERSION + "/svr/php/";
    public static final String PHP_PATH = SITE + PHP_DIRECTORY;

    public static final String USER_ACCESS_URL = PHP_PATH + "passenger/user_access.php";
    public static final String GET_FOR_HIRE_CAB_URL = PHP_PATH + "passenger/get_for_hire_cab.php";
    public static final String REQUEST_CAB_URL = PHP_PATH + "passenger/request_cab.php";
    public static final String GET_USER_PROFILE_IMAGE_FILE_NAME_URL = SITE + ALPHA + "/" + APPNAME;
    public static final String CANCEL_CAB_TRIP_URL = PHP_PATH + "passenger/cancel_trip_user.php";
    public static final String GET_TRIP_STATUS = PHP_PATH + "passenger/get_trip_status.php";
    public static final String GET_DRIVER_POSITION_URL = PHP_PATH + "passenger/get_driver_position.php";
    public static final String UPDATE_TRIP_RATING_URL = PHP_PATH + "passenger/update_trip_rating.php";
    public static final String TRIP_HISTORY_URL = PHP_PATH + "passenger/trip_history.php";
    public static final String WALKWITHME_UPDATEPOSITION_URL = PHP_PATH + "passenger/ws/walkwithme_updateposition.php";
    public static final String ADD_USER_DATA_URL = PHP_PATH + "passenger/add_user_data.php";
    public static final String ADD_CABE_USER_DATA_URL = PHP_PATH + "passenger/add_cabe_user_data.php";
    public static final String GOT_CAB_URL = PHP_PATH + "passenger/got_cab.php";
    public static final String GET_USER_PROFILE_URL = PHP_PATH + "passenger/get_user_profile.php";
    public static final String TRIP_PATH_HISTORY_URL = PHP_PATH + "passenger/trip_path_history.php";
    public static final String GET_CANCEL_REASONS_URL = PHP_PATH + "get_cancel_reasons.php";
    public static final String NO_CAB_FOUND_URL = PHP_PATH + "passenger/no_cab_found.php";
    public static final String GET_DRIVER_POSITION_SHORT_URL = PHP_PATH + "passenger/get_driver_position_short.php";
    public static final String TRIP_CREATE_TRIP_PATH_PASSENGER_URL = PHP_PATH + "passenger/create_trip_path.php";
    public static final String GET_FIXED_ADDRESS_URL = PHP_PATH + "shared/get_fixed_address.php";
    public static final String REQUEST_SHARED_CAB_URL = PHP_PATH + "shared/request_shared_cab.php";
    public static final String ADD_SHARED_PASSENGER_URL = PHP_PATH + "shared/add_shared_passenger.php";

    // Preferences
    public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String PREFS_VERSION_PERSISTENT = "PREFS_VERSION_PERSISTENT";
    public static final String PREF_REGISTERED = "PREF_REGISTERED";
    public static final String PREF_PHONENO = "PREF_PHONENO";
    public static final String PREF_REG_ID = "PREF_REG_ID";
    public static final String PREF_APPUSERID = "PREF_APPUSERID";
    public static final String PREF_COUNTRY_CODE = "PREF_COUNTRY_CODE";
    public static final String PREF_MY_LOCATION = "PREF_MY_LOCATION";
    public static final String PREF_CURRENT_COUNTRY = "PREF_CURRENT_COUNTRY";
    public static final String PREF_ISLOGIN_MY_APP = "PREF_ISLOGIN_MY_APP";
    public static final String WHERE_AM_I = "WHERE_AM_I";
    public static final String CABE_REQUEST_RESPONSE = "CABE_REQUEST_RESPONSE";
    public static final String PREF_CABE_NUMBER_VERIFY_DONE = "PREF_CABE_NUMBER_VERIFY_DONE";
    public static final String PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE = "PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE";
    public static final String CURRENT_TRACKED_TRIP = "CURRENT_TRACKED_TRIP";
    public static final String KEY_PREF_EMERGENCY_CC_PHONE = "KEY_PREF_EMERGENCY_CC_PHONE";
    public static final String KEY_PREF_EMERGENCY_EMAIL = "KEY_PREF_EMERGENCY_EMAIL";
    public static final String KEY_PREF_EMERGENCY_NAME = "KEY_PREF_EMERGENCY_NAME";
    public static final String KEY_PREF_MY_NAME = "pref_myname";
    public static final String KEY_PREF_MY_NUMBER = "pref_mynumber";
    public static final String PERF_HOME_ADDRESS = "PERF_HOME_ADDRESS";
    public static final String PERF_WORK_ADDRESS = "PERF_WORK_ADDRESS";
    public static final String PREF_RECEIVE_NOTIFICATIONS = "PREF_SEND_NOTIFICATIONS";
    public static final String PREF_CAB_ALERTS = "PREF_CAB_NOTIFICATIONS";
    public static final String PREF_NOTIFICATION_LIST_SAVED = "PREF_NOTIFICATION_LIST_SAVED";
    public static final String PREF_NOTIFICATION_CLEAR_FLAG = "PREF_NOTIFICATION_CLEAR_FLAG";
    public static final String PREF_VERSION_CODE = "PREF_VERSION_CODE";
    public static final String PREF_TRIP_ID_PASSENGER = "PREF_TRIP_ID_PASSENGER";
    public static final String DRIVER_CAB_LAT = "DRIVER_CAB_LAT";
    public static final String DRIVER_CAB_LNG = "DRIVER_CAB_LNG";
    public static final String SAVE_RATING_NOT_SEND = "SAVE_RATING_NOT_SEND";
    public static final String SAVE_RATING_COUNT = "SAVE_RATING_COUNT";
    public static final String PREF_CANCEL_REASON_CODE = "PREF_CANCEL_REASON_CODE";
    public static final String PREF_UPDATE_DRIVER_RESPONSE = "PREF_UPDATE_DRIVER_RESPONSE";
    public static final String PREF_DRIVER_STATUS = "PREF_DRIVER_STATUS";
    public static final String STATUS_DRIVER_CAB_LAT = "STATUS_DRIVER_CAB_LAT";
    public static final String STATUS_DRIVER_CAB_LNG = "STATUS_DRIVER_CAB_LNG";
    public static final String PREF_UPDATE_DRIVER_RESPONSE_STATUS = "PREF_UPDATE_DRIVER_RESPONSE_STATUS";
    public static final String GMAIL_FACEBOOK_IMAGE_URL = "GMAIL_FACEBOOK_IMAGE_URL";
    public static final String PREF_SERVICE_CODE_SAVE = "PREF_SERVICE_CODE_SAVE";

    // Trip preferences
    public static final String PREF_FROM_ADDRESS = "PREF_FROM_ADDRESS";
    public static final String PREF_TO_ADDRESS = "PREF_TO_ADDRESS";
    public static final String CURRENT_CAB_TRIP = "CURRENT_CAB_TRIP";

    public static final String PREF_FROM_ADDRESS_FIXED = "PREF_FROM_ADDRESS_FIXED";
    public static final String PREF_TO_ADDRESS_FIXED = "PREF_TO_ADDRESS_FIXED";

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PREF_TOKEN_SAVED = "PREF_TOKEN_SAVED";
    public static final String NOTIFICATION_TYPE_BEGIN = "TA_B";

    // Nofication categories
    public static final String COMMERCIAL = "CO";
    public static final String NOTIFICATON_CATEGORY_DRIVER_TRIP = "DT";
    public static final String NOTIFICATON_CATEGORY_UPGRADE_APP = "UA";
    public static final String NOTIFICATON_CATEGORY_COMMERCIAL_DRIVER_TRIP = "CDT";
    public static final String NOTIFICATON_TYPE_UPDATE_POSITION = "UP";
    public static final String NOTIFICATON_TYPE_DRIVER_MOVED = "DM";
    public static final String NOTIFICATON_TYPE_JUMP_IN = "PH";

    // Connectivity
    public static final double INVALIDLAT = -999;
    public static final double INVALIDLNG = -999;

    // Trip action
    public static final String TRIP_ACTION_CREATE = "C"; // Planned - dormant
    public static final String TRIP_ACTION_BEGIN = "B"; // In trip begin -
    // delayed start
    public static final String TRIP_ACTION_PAUSE = "P"; // In trip paused -
    // active
    public static final String TRIP_ACTION_ABORT = "A"; // In trip abort -
    // active
    public static final String TRIP_ACTION_RESUME = "R"; // In trip resumed -
    // active
    public static final String TRIP_ACTION_END = "E"; // trip end - passive
    //App Booking Type
    public static final String USER_TYPE = "A";

    public static final String COL_HAS_JOINED = "has_joined";
    public static final String COL_HAS_JUMPED_IN = "has_jumped_in";
    public static final String COL_HAS_JUMPED_OUT = "has_jumped_out";
    public static final String COL_CANCEL_JOIN = "cancel_join";

    //handler  Time set
    public static final int TWO_MINUTES = 1000 * 60 * 2;
    public static final long DRIVER_UPDATE_INTERVAL_USER = 15 * 1000;
    public static final long DRIVER_UPDATE_LOCATION_INTERVAL_USER = 5 * 1000;
    public static final long DRIVER_GET_TRIP_STATUS_INTERVAL = 10 * 1000;
    public static final long DELAY_SHOW_FOR_HIRE_CABS = 10 * 1000;
    public static final int VOLLEY_NETYWORK_ERROR = -9;
    public static final int MAX_NOTIFICATIONS = 25;
    public static final double DEFAULT_WALKING_DISTANCE = 5; // in km
    //public static final double CHECKNOTIFICATIONDISTANCE = .001;
    public static final int SERVER_NOTIFICATION_ID = 9;

    // Service codes
    public static final String SERVICE_CODE_BY = "BY"; // Blank & Yellow
    public static final String SERVICE_CODE_CC = "CC"; //
    public static final String SERVICE_CODE_FT = "FT";
    public static final String SERVICE_CODE_TTP = "TTP";
    public static final String SERVICE_CODE_TNP = "TNP";
    public static final String SERVICE_CODE_SG = "SG";
    public static final String SERVICE_CODE_AR = "AR";
    public static final String SERVICE_CODE_ST = "ST";

}
