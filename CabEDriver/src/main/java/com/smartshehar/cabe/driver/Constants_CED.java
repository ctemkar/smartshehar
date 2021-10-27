package com.smartshehar.cabe.driver;

/**
 * Created by jijo_soumen on 08/03/2016.
 * Constants for Cab Driver app
 */
public class Constants_CED {
    public static final String APP_CODE = "CED";
    public static final String TRIP_TYPE_SELF_DRIVER = "S";
    public static final String TRIP_TYPE_SHARE_DRIVER = "P";

    public static final String VERSION = "v1";
    public static final String APPNAME = "cabeapp";
    public static final String ALPHA = "/alpha";
    //public static final String ALPHA = "/beta";
    //public static final String ALPHA = "/prod";
    private static final String AUTHORITY = "www.cabebooking.com";
    private static final String SITE = "https://" + AUTHORITY;
    private static final String PHP_DIRECTORY = ALPHA + "/" + APPNAME + "/" + VERSION + "/svr/php/";
    private static final String PHP_PATH = SITE + PHP_DIRECTORY;

    public static final String USER_ACCESS_URL = PHP_PATH + "driver/user_access_driver.php"; //US
    public static final String SELECT_VEHICLE_URL = PHP_PATH + "driver/select_vehicle.php"; //SV
    public static final String DRIVER_END_SHIFT_URL = PHP_PATH + "driver/driver_end_shift.php";  //DES
    public static final String DRIVER_START_SHIFT_URL = PHP_PATH + "driver/driver_start_shift.php";  //DSS
    public static final String SET_FOR_HIRE_URL = PHP_PATH + "set_for_hire.php"; //SFH
    public static final String CHECK_IF_BOOKED_URL = PHP_PATH + "driver/check_if_booked.php"; //CIB
    public static final String CANCEL_TRIP_DRIVER_URL = PHP_PATH + "driver/cancel_trip_driver.php"; //CTD
    public static final String GET_CAB_TRIP_STATUS_URL = PHP_PATH + "driver/get_cab_tripstatus.php"; //GCTS
    public static final String TRIP_CREATE_DRIVER_URL = PHP_PATH + "driver/create_trip_driver.php"; //TCD
    public static final String MISSING_LOCATION_URL = PHP_PATH + "driver/missing_lat_lng.php"; //ML
    public static final String UPDATE_POSITON_DRIVER_URL = PHP_PATH + "driver/update_position_driver.php"; //UPD
    public static final String TRIP_ACTION_DRIVER_URL = PHP_PATH + "driver/trip_action_driver.php"; //TAD
    public static final String UPDATE_TRIP_COST_URL = PHP_PATH + "driver/update_trip_cost.php"; //UTC
    public static final String DRIVER_HISTORY_URL = PHP_PATH + "driver/driver_history.php"; //DH
    public static final String WEEKLY_HISTORY_URL = PHP_PATH + "driver/weekly_history.php"; //WH
    public static final String GET_DRIVER_RATING_URL = PHP_PATH + "driver/get_driver_rating.php"; //GDR
    public static final String GET_DRIVER_PROFILE_URL = PHP_PATH + "driver/get_driver_profile.php"; //GDP
    public static final String GET_DRIVER_PROFILE_IMAGE_URL = SITE + ALPHA + "/" + APPNAME;
    public static final String GET_CANCEL_REASONS_URL = PHP_PATH + "get_cancel_reasons.php"; //GCR
    public static final String CHECK_IF_LOGIN_REQUESTED_URL = PHP_PATH + "driver/check_if_login_requested.php"; //CILR
    public static final String GET_TRIP_PATH_URL = PHP_PATH + "driver/get_trip_path.php"; //GTP
    public static final String GET_FIXED_ADDRESS_URL = PHP_PATH + "shared/get_fixed_address.php"; //GFX
    public static final String CREATE_TRIP_PATH_URL = PHP_PATH + "driver/create_trip_path.php"; //CTP
    public static final String JUMP_IN_URL = PHP_PATH + "driver/jump_in.php"; //JI
    public static final String GET_SHARED_INFO_AFTER_TRIP_URL = PHP_PATH + "shared/get_shared_info_after_trip.php"; //GSIAT

    // Preferences
    public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String PREFS_VERSION_PERSISTENT = "PREFS_VERSION_PERSISTENT";
    public static final String PREF_REGISTERED = "PREF_REGISTERED";
    public static final String PREF_PHONENO = "PREF_PHONENO";
    public static final String PREF_COUNTRY_CODE = "PREF_COUNTRY_CODE";
    public static final String PREF_REG_ID = "PREF_REG_ID";
    public static final String PREF_CURRENT_COUNTRY = "PREF_CURRENT_COUNTRY";
    public static final String PREF_ISLOGIN_MY_APP = "PREF_ISLOGIN_MY_APP";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PREF_TOKEN_SAVED = "PREF_TOKEN_SAVED";
    public static final String PERF_VEHICLE_DETAILS = "PERF_VEHICLE_DETAILS";
    public static final String PERF_VALUE_SAVED = "PERF_VALUE_SAVED";
    public static final String PREF_VEHICLE_ID = "PREF_VEHICLE_ID";
    public static final String PREF_SHIFT_ID = "PREF_SHIFT_ID";
    public static final String PREF_LOGIN_LOGOUT_FLAG = "PREF_LOGIN_LOGOUT_FLAG";
    public static final String INTENT_SHIFT_ID = "INTENT_SHIFT_ID";
    public static final String INTENT_VEHICLE_ID = "INTENT_VEHICLE_ID";
    public static final String PREF_SET_SWITCH_FLAG = "PREF_SET_SWITCH_FLAG";
    public static final String PREF_SAVE_RESPONSE_PASSENGER = "PREF_SAVE_RESPONSE_PASSENGER";
    public static final String PREF_TRIP_ACTION = "PREF_TRIP_ACTION";
    public static final String PREF_IN_TRIP = "IN_TRIP";
    public static final String PREF_TRIP_ID_INT = "PREF_TRIP_ID_INT";
    public static final String PREF_TRIP_DISTANCE_TRAVELED = "PREF_TRIP_DISTANCE_TRAVELED";
    public static final String PASSENGER_EVENT = "passenger_joined";
    public static final String PREF_MYLOCATION_LAT = "PREF_MYLOCATION_LAT";
    public static final String PREF_MYLOCATION_LON = "PREF_MYLOCATION_LON";
    public static final String LOCATION_SERVICE_FOR_HIRE = "location_service_forhire";
    public static final String LOCATION_SERVICE_DRIVER_MAIN = "location_service_drivermain";
    public static final String PREF_CABE_NUMBER_VERIFY_DONE = "PREF_CABE_NUMBER_VERIFY_DONE";
    public static final String PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE = "PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE";
    public static final String PREF_CABE_DRIVER_HISTORY_DAY = "PREF_CABE_DRIVER_HISTORY_DAY";
    public static final String PREF_SERVER_VOLLEY_CALL_FAILED = "PREF_SERVER_VOLLEY_CALL_FAILED";
    public static final String PREF_FOR_HIRE_DATE_TIME = "PREF_FOR_HIRE_DATE_TIME";
    public static final String PREF_VOLLEY_CALL_FAILED_CANCEL_TRIP = "PREF_VOLLEY_CALL_FAILED_CANCEL_TRIP";
    public static final String PREF_VERIFIED_FLAG = "PREF_VERIFIED_FLAG";
    public static final String PREF_MANUALLY_VERIFIED_FLAG = "PREF_MANUALLY_VERIFIED_FLAG";
    public static final String CAB_TRIP_USER_TYPE_DRIVER = "CAB_TRIP_USER_TYPE_DRIVER";
    public static final String CAB_TRIP_ID_DRIVER = "CAB_TRIP_ID_DRIVER";
    public static final String PERF_SERVICE_CODE_BOOKING_DRIVER = "PERF_SERVICE_CODE_BOOKING_DRIVER";
    public static final String PERF_SET_BUSY_CREATE_TRIP_DRIVER = "PERF_SET_BUSY_CREATE_TRIP_DRIVER";
    public static final String PREF_CABE_DRIVER_RATING_HISTORY_DAY = "PREF_CABE_DRIVER_RATING_HISTORY_DAY";
    public static final String PREF_CABE_DRIVER_ACCOUNT_HISTORY_DAY = "PREF_CABE_DRIVER_ACCOUNT_HISTORY_DAY";
    public static final String PREF_CHECK_BOOK_RESPONSE = "PREF_CHECK_BOOK_RESPONSE";
    public static final String PREF_IS_CHECK_BOOK = "PREF_IS_CHECK_BOOK";
    public static final String PREF_CANCEL_REASON_CODE = "PREF_CANCEL_REASON_CODE";
    public static final String PREF_TOTAL_TRIP_COST = "PREF_TOTAL_TRIP_COST";
    public static final String PREF_TOTAL_TRIP_DISTANCE = "PREF_TOTAL_TRIP_DISTANCE";
    public static final String PREF_TRIP_COST_DISTANCE_SUBMIT = "PREF_TRIP_COST_DISTANCE_SUBMIT";
    public static final String PREF_TRIP_ACTION_WITH_INTERNET = "PREF_TRIP_ACTION_WITH_INTERNET";
    public static final String PREF_ACTION_BEGIN_PARAMS = "PREF_ACTION_BEGIN_PARAMS";
    public static final String PREF_ACTION_END_PARAMS = "PREF_ACTION_END_PARAMS";
    public static final String PREF_UPDATE_TRIP_COST_DISTANCE_DIALOG = "PREF_UPDATE_TRIP_COST_DISTANCE_DIALOG";
    public static final String PREF_CHECK_LOGIN_REQUEST_RESPONSE = "PREF_CHECK_LOGIN_REQUEST_RESPONSE";
    public static final String PREF_CAB_E_TRIP_PATH_POLYLINE = "PREF_CAB_E_TRIP_PATH_POLYLINE";
    public static final String PREF_CAB_E_DRIVER_END_SHIFT = "PREF_CAB_E_DRIVER_END_SHIFT";
    public static final String PREF_CAB_E_DRIVER_END_SHIFT_BOOLEAN = "PREF_CAB_E_DRIVER_END_SHIFT_BOOLEAN";
    public static final String PREF_DRIVER_FIXED_ADDRESS_FROM = "PREF_DRIVER_FIXED_ADDRESS_FROM";
    public static final String PREF_DRIVER_FIXED_ADDRESS_TO = "PREF_DRIVER_FIXED_ADDRESS_TO";
    public static final String PREF_DRIVER_FIXED_SHARE_DATE_TIME = "PREF_DRIVER_FIXED_SHARE_DATE_TIME";
    public static final String ERVICE_DRIVER_ALL_PHP = "service_driver_all_php";
    public static final String PREF_ISIN_TRIP_MAIN = "PREF_ISIN_TRIP_MAIN";
    public static final String PREF_NOTIFICATION_CLEAR_FLAG = "PREF_NOTIFICATION_CLEAR_FLAG";

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
    public static final String TRIP_ACTION_NONE = "N"; // Accessed driver -

    public static final String COL_HAS_JOINED = "has_joined";
    public static final String COL_HAS_JUMPED_IN = "has_jumped_in";
    public static final String COL_HAS_JUMPED_OUT = "has_jumped_out";
    public static final String COL_CANCEL_JOIN = "cancel_join";
    public static final String IS_FOR_HIRE = "is_for_hire";
    public static final String PREF_CURRENT_TRIP = "PREF_CURRENT_TRIP"; // Current trip details in string format. Have to convert to CTrip

    //Interval time
    public static final long DRIVER_UPDATE_INTERVAL = 5 * 1000; //ten sec
    public static final long PASSENGER_UPDATE_INTERVAL_ACTIVE = 5 * 1000; //five sec
    public static final long PASSENGER_UPDATE_INTERVAL_PASSIVE = 30 * 1000; // half
    public static final long PASSIVE_INTERVAL = 1000 * 60 * 2; // minutes
    public static final long DRIVER_ACTION_SEND = 6 * 1000; //ten sec

    public static final long LOGIN_REQUEST = 60 * 1000; //ten sec
    public static final int SERVER_NOTIFICATION_ID = 9;

    public static final String SERVICE_FLAG = "SERVICE_FLAG";

    public static final String SERVICE_CODE_BY = "BY";
    public static final String SERVICE_CODE_CC = "CC";
    public static final String SERVICE_CODE_FT = "FT";
    public static final String SERVICE_CODE_TTP = "TTP";
    public static final String SERVICE_CODE_TNP = "TNP";
    public static final String SERVICE_CODE_SG = "SG";
    public static final String SERVICE_CODE_AR = "AR";
    public static final String SERVICE_CODE_ST = "ST";

}
