package com.jumpinjumpout.apk.driver;

public class Constants_driver {

    public static final String VEHICLE_CATEGORY_LONG_DISTANCE_CAR = "L";

    public static final String NORMAL_USER = "NU";
    public static final double DEFAULT_WALKING_DISTANCE = 2; // in km
    public static final String COL_IN_PATH = "inpath";
    // Preferences
    public static final String PREFS_APP = "SAR_PREFS";
    public static final String PREF_PROPERTY_APP_VERSION_NAME = "PREF_PROPERTY_APP_VERSION_NAME";
    public static final String PREF_CURRENT_COUNTRY = "PREF_CURRENT_COUNTRY";

    public static final String PREF_REG_ID = "PREF_REG_ID";
    public static final String PREF_APPUSERID = "PREF_APPUSERID";
    public static final String PREFS_VERSION_PERSISTENT = "PREFS_VERSION_PERSISTENT";
    public static final String PREF_TRIP_ACTION = "PREF_TRIP_ACTION";
    public static final String PREF_REGISTERED = "PREF_REGISTERED";
    public static final String PREF_SCHEDULED_TRIPS = "PREF_SCHEDULED_TRIPS";
    public static final String PREF_SCHEDULED_TRIPS_I = "PREF_SCHEDULED_TRIPS_I";
    public static final String PREF_NOTIFY_FRIENDS = "PREF_NOTIFY_FRIENDS";
    public static final String PREF_NOTIFY_FRIENDS_LIST = "PREF_NOTIFY_FRIENDS_LIST";
    public static final String PREF_IN_TRIP = "IN_TRIP";
    public static final String PREF_TRIP_TYPE = "PREF_TRIP_TYPE";
    public static final String PREF_IS_ADMIN = "PREF_IS_ADMIN";
    public static final String PREF_VEHICLE_CATEGORY = "PREF_VEHICLE_CATEGORY";
    public static final String PREF_PHONENO = "PREF_PHONENO";
    public static final String PREF_COUNTRY_CODE = "PREF_COUNTRY_CODE";
    public static final String PREF_USER_TYPE = "PREF_USER_TYPE";
    public static final String PREF_SET_SWITCH_FLAG = "PREF_SET_SWITCH_FLAG";
    public static final String PREF_SAVE_RESPONSE_PASSENGER = "PREF_SAVE_RESPONSE_PASSENGER";
    public static final String PREF_CURRENT_PASSENGER_PHONENO = "PREF_CURRENT_PASSENGER_PHONENO";
    public static final String PREF_CURRENT_PASSENGER_IMAGEURL = "PREF_CURRENT_PASSENGER_IMAGEURL";
    //// Preferences
    public static final int TWO_MINUTES = 1000 * 60 * 2;
    public static final int MOVE_MARKER = 1000 * 10;
    // Server stuff
    public static final String VERSION = "v45";
    public static final String APPNAME = "jumpinjumpoutapp";
    public static final String ALPHA = "/dev/alpha";
    //	public static final String ALPHA = "/dev/beta";
    //public static final String ALPHA = "/prod";
    public static final String AUTHORITY = "www.jumpinjumpout.com";
    public static final String SITE = "http://" + AUTHORITY;
    //	public static final String SITE = "http://hp-ferrari/sites";
    public static final String SITE_DIRECTORY = SITE + ALPHA + "/" + APPNAME;
    public static final String SVR_DIRECTORY = SITE_DIRECTORY + "/svr/apk/" + VERSION + "/";
    public static final String PHP_DIRECTORY = ALPHA + "/" + APPNAME + "/svr/apk/" + VERSION + "/php/";
    public static final String PHP_PATH = SITE + PHP_DIRECTORY;
    // php URLs
    public static final String USER_ACCESS_DRIVER_URL = PHP_PATH + "driver/user_access_driver.php";
    public static final String TRIP_ACTION_DRIVER_URL = PHP_PATH + "driver/trip_action_driver.php";
    public static final String TRIP_CREATE_DRIVER_URL = PHP_PATH + "driver/create_trip_driver.php";
    public static final String TRIP_CREATE_TRIP_PATH_DRIVER_URL = PHP_PATH + "driver/create_trip_path_driver.php";


    public static final String UPDATE_POSITON_DRIVER_URL = PHP_PATH + "driver/update_position_driver.php";
    public static final String PASSENGER_PROSPECTS_DRIVER_URL = PHP_PATH
            + "driver/passenger_prospects_driver.php";

    public static final String MISSING_LOCATION_URL = PHP_PATH
            + "user/missing_location.php";

    public static final String DRIVER_TRIP_SUMMARY_URL = PHP_PATH
            + "driver/trip_cab_summary.php";

    public static final String SELECT_COMMERCIAL_VEHICLE_URL = PHP_PATH
            + "driver/select_commercial_vehicle.php";

    public static final String COMMERCIAL_DRIVER_START_SHIFT_URL = PHP_PATH
            + "driver/commercial_driver_start_shift.php";

    public static final String COMMERCIAL_DRIVER_END_SHIFT_URL = PHP_PATH
            + "driver/commercial_driver_end_shift.php";

    public static final String GET_USER_PROFILE_IMAGE_FILE_NAME_URL = PHP_PATH + "user/";

    public static final String SET_APPUSER_FLAG_URL = PHP_PATH + "set_appuser_flag.php";

    public static final String CHECKED_IF_BOOK_URL = PHP_PATH + "driver/checked_if_booked.php";

    public static final String ACCEPT_REJECT_CAB_TRIP_URL = PHP_PATH + "driver/accept_reject_cab_trip.php";

    public static final String CANCEL_CAB_TRIP_URL = PHP_PATH + "driver/cancel_cab_trip_driver.php";

    public static final String JUMP_IN_URL = PHP_PATH + "user/jump_in.php";

    public static final String MANAGER_TRACK_TRIPS_URL = PHP_PATH + "driver/manager_track_trips.php";

    public static final String UPDATE_APPUSER_DETAIL_DRIVER_URL = PHP_PATH + "driver/update_appuser_detail_driver.php";

    public static final String UPDATE_COMMERCIAL_VEHICLE_URL = PHP_PATH
            + "driver/update_commercial_vehicle.php";

    public static final String GET_USER_PROFILE_URL = PHP_PATH
            + "user/get_user_profile.php";

    public static final String GET_CAB_TRIP_STATUS_URL = PHP_PATH
            + "driver/get_cab_tripstatus.php";

    //// php URLs  accept_reject_cab_trip.php get_cab_tripstatus.php
    // Flags
    public static final String PREF_RECEIVE_NOTIFICATIONS = "PREF_SEND_NOTIFICATIONS";
    public static final String PREF_CAB_ALERTS = "PREF_CAB_NOTIFICATIONS";
    public static final String IS_FOR_HIRE = "is_for_hire";
    /// flags
    public static final String TRIP_TYPE = "CD";    // Planned - dormant
    // Trip action
    public static final String TRIP_ACTION_BEGIN = "B";    // In trip begin - active
    public static final String TRIP_ACTION_END = "E";        // trip end - passive
    /// Connectivity
    public static final double INVALIDLAT = -999;
    public static final double INVALIDLNG = -999;
    // Notifications
    public static final int SERVER_NOTIFICATION_ID = 9;
    public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final float NEAR_DESTINATION_DISTANCE = 150;
    // Nofication categories
    public static final String NOTIFICATON_CATEGORY_DRIVER_TRIP = "DT";
    public static final String NOTIFICATON_TYPE_UPDATE_POSITION = "UP";
    public static final String NOTIFICATON_TYPE_DRIVER_MOVED = "DM";
    public static final String NOTIFICATON_CATEGORY_UPGRADE_APP = "UA";
    // Distance from user in meter
    public static final float NEAR_BE_READY_DISTANCE = 2000;
    // Delays for polling
    public static final long DRIVER_UPDATE_INTERVAL = 60 * 1000;
    public static final long DRIVER_UPDATE_INTERVAL_GETACAB = 10 * 1000;
    public static final long DRIVER_UPDATE_INTERVAL_CANCELTRIP = 30 * 1000;
    // Trip information for commercial cabs
    public static final String PREF_CAR_INFO = "PREF_CAR_INFO";
    public static final String PREF_MY_LOCATION = "PREF_MY_LOCATION";
    public static final String PREF_INVITE_USER_TO_APP = "PREF_INVITE_USER_TO_APP";
    public static final String PREF_VERSION_CODE = "PREF_VERSION_CODE";
    public static final String PREF_LOGIN_LOGOUT_FLAG = "PREF_LOGIN_LOGOUT_FLAG";
    public static final String INTENT_SHIFT_ID = "INTENT_SHIFT_ID";
    public static final String INTENT_VEHICLE_ID = "INTENT_VEHICLE_ID";
    public static final String PREF_SHIFT_ID = "PREF_SHIFT_ID";
    public static final String PREF_VEHICLE_ID = "PREF_VEHICLE_ID";
    public static final String PREF_UPDTE_SEATS_AND_FARE = "PREF_UPDTE_SEATS_AND_FARE";
    public static final String RECENT_TRIP = "RECENT_TRIP";


}
