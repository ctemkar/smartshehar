package com.jumpinjumpout.apk.user;

public class Constants_user {
    public static final String COMMERCIAL = "CO";
    public static final String NORMAL_USER = "NU";
    public static final String VEHICLE_CATEGORY_LONG_DISTANCE_CAR = "L";

    public static final double DEFAULT_WALKING_DISTANCE = 5; // in km
    public static final double JUMP_IN_DISTANCE = 150; // 150 meters
    public static final String COL_IN_PATH = "inpath";

    public final static int MAXADDRESS = 15;

    public static final double CHECKNOTIFICATIONDISTANCE = .001;
    public static final String TRIP_TYPE_USER = "U";
    public static final String TRIP_TYPE_COMMERCIAL = "CD";

    public static final String TRIP_VEHICLE_CATEGORY_BUS = "B";
    public static final String TRIP_VEHICLE_CATEGORY_CAB = "C";

    // Preferences
    public static final String PREF_PROPERTY_APP_VERSION_NAME = "PREF_PROPERTY_APP_VERSION_NAME";
    public static final String PREF_REG_ID = "PREF_REG_ID";
    public static final String PREF_APPUSERID = "PREF_APPUSERID";
    public static final String PREFS_VERSION_PERSISTENT = "PREFS_VERSION_PERSISTENT";
    public static final String PREF_TRIP_ACTION = "PREF_TRIP_ACTION";
    public static final String PREF_REGISTERED = "PREF_REGISTERED";
    public static final String PREF_NOTIFICATION_STRANGER = "PREF_NOTIFICATION_STRANGER";
    public static final String PREF_NOTIFY_FRIENDS = "PREF_NOTIFY_FRIENDS";
    public static final String PREF_NOTIFY_FRIENDS_LIST = "PREF_NOTIFY_FRIENDS_LIST";
    public static final String PREF_JOINED_TRIP = "PREF_JOINED_TRIP";
    public static final String PREF_TRIP_ID = "PREF_TRIP_ID";
    public static final String PREF_TRIP_PASSOUT = "PREF_TRIP_PASSOUT";
    public static final String PREF_IN_TRIP = "IN_TRIP";
    public static final String PREF_TRIP_TYPE = "PREF_TRIP_TYPE";
    public static final String PREF_IS_ADMIN = "PREF_IS_ADMIN";
    public static final String PREF_DISPLAY_TRIP_TYPE = "PREF_DISPLAY_TRIP_TYPE";
    public static final String PREF_MY_LOCATION = "PREF_MY_LOCATION";
    public static final String PREF_PHONENO = "PREF_PHONENO";
    public static final String PREF_COUNTRY_CODE = "PREF_COUNTRY_CODE";
    public static final String PREF_TRIP_ID_INT = "PREF_TRIP_ID_INT";
    public static final String PREF_USER_TYPE = "PREF_USER_TYPE";
    public static final String PREF_CURRENT_COUNTRY = "PREF_CURRENT_COUNTRY";
    public static final String PREF_INVITE_USER_CURRENT_TO_APP = "PREF_INVITE_USER_CURRENT_TO_APP";
    public static final String PREF_TRIP_MEMBER_ID = "PREF_TRIP_MEMBER_ID";
    public static final String PREF_GOTIT_ACTIVE_TRIPS = "PREF_GOTIT_ACTIVE_TRIPS";
    public static final String PREF_NOTIFICATION_LIST_SAVED = "PREF_NOTIFICATION_LIST_SAVED";
    public static final String PREF_NOTIFICATION_CLEAR_FLAG = "PREF_NOTIFICATION_CLEAR_FLAG";
    public static final String PREF_SCHEDULED_TRIPS = "PREF_SCHEDULED_TRIPS";
    public static final String PREF_SCHEDULED_TRIPS_I = "PREF_SCHEDULED_TRIPS_I";
    public static final String PREF_ENTER_SEAT_BOOK = "PREF_ENTER_SEAT_BOOK";
    public static final String PREF_LONG_DISTANCE_CTRIP = "PREF_LONG_DISTANCE_CTRIP";
    public static final String KEY_PREF_MY_NAME = "pref_myname";
    public static final String KEY_PREF_MY_NUMBER = "pref_mynumber";

    // // Preferences
    public static final int TWO_MINUTES = 1000 * 60 * 2;
    // Map variables
    public static final int MAP_PADDING = 50;
    // // Map variables
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
            + "/svr/apk/" + VERSION + "/php/user/";
    public static final String PHP_PATH = SITE + PHP_DIRECTORY;

    // php URLs
    public static final String REGISTER_SAR_USER_URL = null;
    public static final String USER_ACCESS_URL = PHP_PATH
            + "user_access_sar_apk.php";
    public static final String CREATE_TRIP_USER_URL = PHP_PATH
            + "create_trip_user.php";
    public static final String CREATE_TRIP_PATH_USER_URL = PHP_PATH
            + "create_trip_path_user.php";
    public static final String TRIP_ACTION_USER_URL = PHP_PATH
            + "trip_action_user.php";
    public static final String ACTIVE_USERS_URL = PHP_PATH + "active_users.php";
    public static final String PASSENGER_PROSPECTS_URL = PHP_PATH
            + "passenger_prospects.php";
    public static final String MISSING_LOCATION_URL = PHP_PATH
            + "missing_location.php";
    public static final String GET_TRIP_SAR_APK_URL = PHP_PATH + "get_trip_sar_apk.php";
    public static final String GET_TRIP_STATUS_URL = PHP_PATH
            + "get_trip_status.php";
    public static final String GET_MY_GROUPS_URL = PHP_PATH
            + "get_my_groups.php";
    public static final String UPDATE_POSITION_URL = PHP_PATH + "update_position_sar_apk.php";
    // .... Group Details...//
    public static final String CREATE_COMMUNITY_URL = PHP_PATH
            + "create_community.php";
    public static final String UPDATE_APPUSER_DETAIL_URL = PHP_PATH
            + "update_appuser_detail.php";
    public static final String ADD_COMMUNITY_MEMBER_URL = PHP_PATH
            + "add_community_member.php";
    public static final String GET_COMMUNITY_MEMBERS_DETAIL_URL = PHP_PATH
            + "get_community_members_detail.php";
    public static final String UPDATE_COMMUNITY_URL = PHP_PATH
            + "update_community.php";
    public static final String CHECK_USER_NAME_URL = PHP_PATH
            + "check_username.php";
    public static final String PREF_GROUP_ID = "PREF_GROUP_ID";
    public static final String VALUE_TRUE = "1";
    public static final String VALUE_FLASE = "0";
    public static final String GET_USER_PROFILE_URL = PHP_PATH
            + "get_user_profile.php";
    public static final String DELETE_COMMUNITY_MEMBER_URL = PHP_PATH
            + "delete_community_member.php";
    public static final String DELETE_COMMUNITY_URL = PHP_PATH
            + "delete_community.php";
    public static final String GET_MY_COMMUNITIES_URL = PHP_PATH
            + "get_my_communities.php";
    public static final String COMMUNITY_DONE_URL = PHP_PATH
            + "community_done.php";
    public static final String COMMUNITY_MEMBER_ACTIVE_URL = PHP_PATH
            + "community_member_active.php";
    public static final String DELETE_MEMBER_WITH_APPUSERID_URL = PHP_PATH
            + "delete_member_with_appuserid.php";
    public static final String ADD_TRIP_MEMBER_URL = PHP_PATH
            + "add_trip_member.php";
    public static final String ADD_TRIP_COMMUNITY_URL = PHP_PATH
            + "add_trip_community.php";
    public static final String INVITE_TRIP_MEMBER_URL = PHP_PATH
            + "invite_trip_members.php";
    public static final String DO_NOT_HAVE_APP_URL = PHP_PATH
            + "do_not_have_app.php";
    public static final String COL_COMMUNITY_ID = "community_id";
    public static final String PARAMETER_COMMUNITY_ID = "communityid";
    public static final String DELETE_TRIP_MEMBER_URL = PHP_PATH
            + "delete_trip_member.php";
    public static final String PREF_VERSION_CODE = "PREF_VERSION_CODE";
    // .... Group Details...//
    public static final String ADD_HOME_WORK_URL = PHP_PATH
            + "add_home_work.php";
    public static final String GET_TRIP_MEMBERS_DRIVING_URL = PHP_PATH
            + "get_trip_members_driving.php";
    public static final String OPEN_TRIP_URL = PHP_PATH
            + "open_trip.php";
    public static final String LAST_INVITED_MEMBER_TRIP_URL = PHP_PATH
            + "last_invited_member_trip.php";
    public static final String GET_USER_PROFILE_IMAGE_FILE_NAME_URL = PHP_PATH;
    public static final String GET_GET_FOR_HIRE_CAB_URL = PHP_PATH + "get_for_hire_cab.php";
    public static final String SET_TRIP_FLAG_URL = PHP_PATH + "../set_user_preference_flag.php";
    public static final String UPGRADE_APP_URL = PHP_PATH + "../upgrade_app.php";
    // Track driver
    public static final String JOIN_TRIP_URL = PHP_PATH
            + "join_trip_sar_apk.php";
    public static final String JUMP_IN_URL = PHP_PATH + "jump_in.php";
    public static final String REQUEST_CAB_URL = PHP_PATH + "request_cab.php";
    public static final String HAS_DRIVER_ACCEPTED_URL = PHP_PATH + "has_driver_accepted.php";
    public static final String CANCEL_CAB_TRIP_URL = PHP_PATH + "cancel_cab_trip_user.php";
    public static final String UPDATE_RATING_COMMENT_URL = PHP_PATH + "update_rating_comment.php";
    public static final String COMMON_HOME_WORK_URL = PHP_PATH + "common_home_work.php";
    public static final String INSERT_FRIEND_DATA_URL = PHP_PATH + "insert_friend_data.php";
    public static final String GET_FRIEND_PENDING_REQUEST_URL = PHP_PATH + "get_friend_pending_request.php";
    public static final String UPDATE_HOME_WORK_PENDING_REQUEST_URL = PHP_PATH + "update_home_work_pending_request.php";
    public static final String CHECK_ISINTRIP_URL = PHP_PATH + "check_isintrip.php";
    public static final String TRIP_HISTORY_URL = PHP_PATH + "trip_history.php";
    public static final String REQUEST_DRIVER_URL = PHP_PATH + "request_driver.php";
    public static final String WALKWITHME_UPDATEPOSITION_URL = PHP_PATH + "ws/walkwithme_updateposition.php";
    public static final String LONG_DISTANCE_ACTIVE_USERS_URL = PHP_PATH + "long_distance_active_users.php";
    public static final String ADD_TRANSACTION_SEATS_URL = PHP_PATH + "add_transaction_seats.php";
    public static final String HAS_DRIVER_STARTED_URL = PHP_PATH + "has_driver_started.php";

    // // php URLs

    // Flags
    public static final String PREF_SHOW_ME = "PREF_SHOW_ME";
    public static final String PREF_RECEIVE_NOTIFICATIONS = "PREF_SEND_NOTIFICATIONS";
    public static final String PREF_CAB_ALERTS = "PREF_CAB_NOTIFICATIONS";
    public static final String PERF_GROUP_NOTIFICATION = "PERF_GROUP_NOTIFICATION";
    public static final String PREF_LOGIN_LOGOUT_FLAG = "PREF_LOGIN_LOGOUT_FLAG";
    public static final String PREF_MARATHI_LONG_DISTANCE_VEHICLE_CATEGORY = "PREF_MARATHI_LONG_DISTANCE_VEHICLE_CATEGORY";


    // / flags

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
    // passive

    // Connectivity
    // / Connectivity
    public static final double INVALIDLAT = -999;
    public static final double INVALIDLNG = -999;

    public static final int REQUEST_CODE = 11;

    // Notifications
    public static final int SERVER_NOTIFICATION_ID = 9;
    // / Notifications
    public static final String CURRENT_TRACKED_TRIP = "CURRENT_TRACKED_TRIP";
    public static final String TRIP_STATUS_TRACKED_TRIP = "TRIP_STATUS_TRACKED_TRIP";
    public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final float NEAR_DESTINATION_DISTANCE = 150;
    public static final float NEAR_BE_READY_DISTANCE = 2000;

    // Nofication categories
    public static final String NOTIFICATON_CATEGORY_DRIVER_TRIP = "DT";
    public static final String NOTIFICATON_CATEGORY_COMMERCIAL_DRIVER_TRIP = "CDT";
    public static final String NOTIFICATON_TYPE_UPDATE_POSITION = "UP";
    public static final String NOTIFICATON_TYPE_DRIVER_MOVED = "DM";
    public static final String NOTIFICATON_TYPE_JUMP_IN = "PH";

    public static final String NOTIFICATON_CATEGORY_UPGRADE_APP = "UA";

    public static final int MAX_NOTIFICATIONS = 25;

    public static final String HAS_DESTINATION = "HAS_DESTINATION";
    public static final String GET_A_CAB_VALUE = "GET_A_CAB_VALUE";

    // Delays for polling
    public static final long PASSIVE_INTERVAL = 1000 * 5; // minutes
    public static final long DRIVER_UPDATE_INTERVAL = 30 * 1000;
    public static final long DRIVER_FOUND_CAB = 15 * 1000;
    public static final long DRIVER_FOUND_CAB_HAS_DRIVER_AC = 5 * 1000;
    public static final long DRIVER_CAB_ICONREQUEST = 60 * 1000;

    // Trip information for commercial cabs
    public static final String PREF_CAR_INFO = "PREF_CAR_INFO";
    public static final String PREF_JUMPIN = "PREF_JUMPIN";
    public static final String PREF_HAS_JUMPED_IN = "PREF_HAS_JUMPED_IN";
    public static final String PREF_HAS_JUMPED_OUT = "PREF_HAS_JUMPED_OUT";
    public static final String PREF_SET_SWITCH_OPEN_TRIPS = "PREF_SET_SWITCH_OPEN_TRIPS";
    public static final String PREF_SET_SWITCH_FLAG = "PREF_SET_SWITCH_FLAG";

    public static final String PERF_STRANGER_NOTIFICATION_ALLOW = "PERF_STRANGER_NOTIFICATION_ALLOW";
    public static final String PERF_SHOW_CAB_NOTIFICATION = "PERF_SHOW_CAB_NOTIFICATION";

    public static final String PREF_CURRENT_PASSENGER_PHONENO = "PREF_CURRENT_PASSENGER_PHONENO";
    public static final String PREF_CURRENT_PASSENGER_IMAGEURL = "PREF_CURRENT_PASSENGER_IMAGEURL";

    public static final String CHANGE_VALUE = "CHANGE_VALUE";
    public static final int SUCCESS_RESULT = 0;
    public static final String COL_CAB_ALERT = "show_cab_notifications";
    public static final String COL_OPEN_NOTIFICATION_ALERT = "allowstrangernotifications";
    // Twitter
    public static final String KEY_CONSUMER_KEY = "vS3A5ERchjV7odFkO91dejWbX";
    public static final String KEY_CONSUMER_SECRET = "dWts5Mk8GUGG056Fl3SAEuenPMTTIu88rSCG5wC40n3ygxVNWU";
    public static final String KEY_CALLBACK_URL = "http://www.jumpinjumpout.com";
    public static final String PREF_NAME = "userData";
    public static final String KEY_OAUTH_TOKEN = "oauth_token";
    public static final String KEY_OAUTH_SECRET = "oauth_token_secret";
    public static final String KEY_TWITTER_LOGIN = "is_twitter_loggedin";
    public static final String KEY_USER_NAME = "twitter_user_name";

}
