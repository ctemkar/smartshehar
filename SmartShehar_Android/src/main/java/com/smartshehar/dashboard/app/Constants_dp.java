package com.smartshehar.dashboard.app;


import lib.app.util.Constants_lib_ss;

public class Constants_dp {

    public static final double MINCITYLAT = 18.9750;
    public static final double MINCITYLON = 72.8258;
    public static final String VERSION = "v17";
    public static final String APPNAME = "/smartsheharapp";
    public static final String ALPHA = Constants_lib_ss.ALPHA;
    //          public static final String ALPHA = "/beta";
//   public static final String ALPHA = "/prod";
    public static final String AUTHORITY = "www.smartshehar.com";
    public static final String SITE = "http://" + AUTHORITY;
    public static final String SITE_DIRECTORY = SITE + ALPHA + APPNAME +"/" + VERSION;

    public static final String PHP_DIRECTORY = ALPHA + APPNAME + "/" + VERSION
            + "/svr/php/";
    public static final String PHP_PATH = SITE + PHP_DIRECTORY;

    public static final String PREF_APP_PREFERENCES = "PREF_APP_PREFERENCES";
    public static final String PREF_USER_REGISTRATION = "PREF_USER_REGISTRATION";
    public static final String PREF_USER_NAME = "PREF_USER_NAME";
    public static final String PREF_USER_FULL_NAME = "PREF_USER_FULL_NAME";
    public static final String PREF_USER_AGE = "PREF_USER_AGE";
    public static final String PREF_USER_GENDER = "PREF_USER_GENDER";
    public static final String PREF_SHOW_NOTIFICATION = "PREF_SHOW_NOTIFICATION";
    public static final String PREF_REG_ID = "PREF_REG_ID";
    public static final String PREF_USER_TYPE = "PREF_USER_TYPE";

    public static final String PREF_REGISTERED = "PREF_REGISTERED";
    public static final String PREF_CURRENT_COUNTRY = "PREF_CURRENT_COUNTRY";
    public static final String PREF_VIOLATION_ALL_VALUE = "PREF_VIOLATION_ALL_VALUE";
    public static final String PREF_GET_LAST_ADDRESS = "PREF_GET_LAST_ADDRESS";
    public static final String PREF_GET_LAST_POSTAL_CODE = "PREF_GET_LAST_POSTAL_CODE";
    public static final String PREF_GET_LAST_CITY_NAME = "PREF_GET_LAST_CITY_NAME";
    public static final String PREF_GET_LAST_SUBLOCALITY = "PREF_GET_LAST_SUBLOCALITY";
    public static final String PREF_GET_LAST_ROUTE = "PREF_GET_LAST_ROUTE";
    public static final String PREF_GET_LAST_NEIGHBOURHOOD = "PREF_GET_LAST_NEIGHBOURHOOD";
    public static final String PREF_GET_LAST_ADMINISTRATIVE_1 = "PREF_GET_LAST_ADMINISTRATIVE_1";
    public static final String PREF_GET_LAST_ADMINISTRATIVE_2 = "PREF_GET_LAST_ADMINISTRATIVE_2";
    public static final String PREF_GET_LAST_LAT = "PREF_GET_LAST_LAT";
    public static final String PREF_GET_LAST_LNG = "PREF_GET_LAST_LNG";
    public static final String PREF_WARD = "PREF_WARD";
    public static final String PREF_RECENT_CAT = "PREF_RECENT_CAT";
    public static String UPDATE_APPUSER_DETAILS_URL = PHP_PATH + "update_appuser_detail.php";
    public static String GET_APPUSER_DETAILS_URL = PHP_PATH + "get_appuser_detail.php";
    public static String GET_WARD_LIST_URL = PHP_PATH + "issue/get_ward_list.php";
    public static String ADD_ISSUE_URL = PHP_PATH + "issue/add_issue.php";
    public static String GET_WARD_FROM_LAT_LNG_URL = PHP_PATH + "issue/get_ward_from_lat_lng.php";
    public static String ADD_ISSUE_IMAGES_URL = PHP_PATH + "issue/issue_image.php";
    public static String DELETE_CANCELLED_IMAGE_URL = PHP_PATH + "issue/delete_cancelled_image.php";
    public static final String USER_ACCESS_URL = PHP_PATH + "issue/da_user_access.php";
    public static String ISSUE_EMAIL_URL = PHP_PATH + "issue/issue_email.php";
    public static String PREF_GOTIT_ACTIVE_TRIPS = "PREF_GOTIT_ACTIVE_TRIPS";
    public static final String PREF_MY_LOCATION = "PREF_MY_LOCATION";
    public static final int TWO_MINUTES = 1000 * 60 * 2;
    public static final String PREF_PHONENO = "PREF_PHONENO";
    public static final String PREF_COUNTRY_CODE = "PREF_COUNTRY_CODE";
    public static String GET_ISSUE_URL = PHP_PATH + "issue/get_issue.php";
    public static String ADD_LETTERHEAD_DETAILS_URL = PHP_PATH + "issue/add_letterhead_details.php";
    public static String DELETE_LETTERHEAD_INFO_URL = PHP_PATH + "issue/delete_letterhead_info.php";
    public static String UPDATE_RESOLVED_UNRESOLVED_URL = PHP_PATH + "issue/update_resolved_unresolved.php";
    public static String ADD_INTERMEDIATE_PROGRESS_URL = PHP_PATH + "issue/add_intermediate_progress.php";
    public static String ADD_ISSUE_RATING_URL = PHP_PATH + "issue/add_issue_rating.php";
    public static String GET__INTERMEDIATE_PROGRESS_URL = PHP_PATH + "issue/get_intermediate_progress.php";
    public static String GET__WARD_ADDRESS_URL = PHP_PATH + "issue/get_ward_address.php";
    public static String UPDATE_CLOSED_OPENED_URL = PHP_PATH + "issue/update_closed_opened.php";
    public static String RESOLVED_ISSUE_EMAIL_URL = PHP_PATH + "issue/resolved_issue_email.php";
    public static String ISSUE_CLOSED_EMAIL_URL = PHP_PATH + "issue/issue_closed_email.php";
    public static String UPDATE_LETTER_DETAILS_URL = PHP_PATH + "issue/update_letter_details.php";
    public static String UPDATE_NOTIFICATON_FLAG_URL = PHP_PATH + "issue/update_notification_flag.php";
    public static String DASHBOARD_REPROT_URL = SITE_DIRECTORY + "/"+ "dbadmin/pages/index.html";
    public static String TRAFFIC_REPROT_URL = SITE_DIRECTORY + "/"+ "dbadmin/pages/department.html?departmentcode=TR&groupby=issue_sub_type_code";
    public static String MUNICIPAL_REPROT_URL = SITE_DIRECTORY + "/"+ "dbadmin/pages/department.html?departmentcode=MU&groupby=issue_sub_type_code";
    public static String SMARTSHEHAR_SUMMARY_URL = PHP_PATH + "dbadmin/smartshehar_summary.php";
    public static String GET_USER_PROFILE_IMAGE_FILE_NAME_URL = PHP_PATH;
    public static long GET_LAST_UNIQUE_KEY = 0;
    public static int CHECKE_CONNECTION_INTERVAL = 1000 * 20;
    public static DataBaseHandler db;
    public static String ISSUE_REPORT_URL = PHP_PATH + "reports/issue_report.php";
    public static String ADD_ISSUE_LIKE_COUNT_URL= PHP_PATH + "issue/add_issue_like_count.php";
    public static final String PREF_RECEIVE_NOTIFICATIONS = "PREF_RECEIVE_NOTIFICATIONS";
    public static final String PREF_NOTIFICATION_LIST_SAVED = "PREF_NOTIFICATION_LIST_SAVED";
    public static final int MAX_NOTIFICATIONS = 25;
    public static final int SERVER_NOTIFICATION_ID = 9;


    //issue reports//
    public static String REPORT_FILTER_URL = SITE + ALPHA + APPNAME + "/" + VERSION + "/" + "issue_report.html?";
    public static String PREF_NOTIFICATION_CLEAR_FLAG = "PREF_NOTIFICATION_CLEAR_FLAG";
    public static String PREF_FILTER_FROM_DATE = "PREF_FILTER_FROM_DATE";
    public static String PREF_FILTER_TO_DATE = "PREF_FILTER_TO_DATE";
    public static String PREF_FILTER_POSTAL_CODE = "PREF_FILTER_POSTAL_CODE";
    public static String PREF_FILTER_MY_COMPLAINTS = "PREF_FILTER_MY_COMPLAINTS";
    public static String PREF_FILTER_ALL = "PREF_FILTER_ALL";


    public static String PREF_FILTER_PV = "PREF_FILTER_ PV";
    public static String PREF_FILTER_MV = "PREF_FILTER_ MV";
    public static String PREF_FILTER_RT = "PREF_FILTER_ RT";
    public static String PREF_FILTER_AUT = "PREF_FILTER_ AUT";
    public static String PREF_FILTER_RD = "PREF_FILTER_ RD";
    public static String PREF_FILTER_CL = "PREF_FILTER_ CL";
    public static String PREF_FILTER_EN = "PREF_FILTER_ EN";
    public static String PREF_FILTER_SF = "PREF_FILTER_ SF";
    public static String PREF_FILTER_WE = "PREF_FILTER_ WE";
    public static String PREF_FILTER_OG = "PREF_FILTER_ OG";
    public static String PREF_FILTER_RS = "PREF_FILTER_ RS";


    public static String PREF_FILTER_CLOSED = "PREF_FILTER_CLOSED";
    public static String PREF_FILTER_RESOLVED = "PREF_FILTER_RESOLVED";
    //issue reports//
    ////saftey shield//
    public static String PREFS_NAME = "SSS";
    public static final String PREFTRAVELDIST = "traveldistance";
    public static final String NEARBY = "NEARBY";
    public static final String POLICE = "police";
    public static final String HOSPITAL = "hospital";
    public static final String SAFTEY_SHIELD_PHP_PATH = PHP_PATH + "ws/";
    public static final String KEY_PREF_MY_NAME = "pref_myname";
    public static final String KEY_PREF_MY_NUMBER = "pref_mynumber";
    public static final String KEY_PREF_MY_EMAIL = "pref_myemail";
    public static final String KEY_PREF_CC1 = "pref_cc1";
    public static final String KEY_PREF_CC1_PHONE = "pref_cc1_phone";
    public static final String KEY_PREF_CC1_MSG = "pref_cc1_msg";
    public static final String KEY_PREF_CC2 = "pref_cc2";
    public static final String KEY_PREF_CC2_PHONE = "pref_cc2_phone";
    public static final String KEY_PREF_CC2_MSG = "pref_cc2_msg";
    public static final String KEY_PREF_CC3 = "pref_cc3";
    public static final String KEY_PREF_CC3_PHONE = "pref_cc3_phone";
    public static final String KEY_PREF_CC3_MSG = "pref_cc3_msg";
    ////saftey shield//
    public static final String PREF_TOKEN_SAVED = "PREF_TOKEN_SAVED";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PREF_LAST_TRAIN_STATION_ONE = "PREF_LAST_TRAIN_STATION_ONE";
    public static final String PREF_LAST_TRAIN_STATION_TWO = "PREF_LAST_TRAIN_STATION_TWO";
    public static final String PREF_LAST_TRAIN_STATION_THREE = "PREF_LAST_TRAIN_STATION_THREE";

    public static final String PREF_LAST__BUS_STATION_ONE = "PREF_LAST__BUS_STATION";
    public static final String PREF_LAST__BUS_STATION_TWO = "PREF_LAST__BUS_STATION_TWO";
    public static final String PREF_LAST__BUS_STATION_THREE = "PREF_LAST__BUS_STATION_THREE";

    // Twitter
    public static final String KEY_CONSUMER_KEY = "AGf5kHPYLdWzq8haa1jAqGPUj";
    public static final String KEY_CONSUMER_SECRET = "LmJv9qfq7ROS2dAxjBZRJpE9rmPSiOkH46eDITSXez84dXdbof";
    public static final String KEY_CALLBACK_URL = "http://www.smartshehar.com";
    public static final String PREF_NAME = "userData";
    public static final String KEY_OAUTH_TOKEN = "oauth_token";
    public static final String KEY_OAUTH_SECRET = "oauth_token_secret";
    public static final String KEY_TWITTER_LOGIN = "is_twitter_loggedin";
    public static final String KEY_USER_NAME = "twitter_user_name";
    ///


    // issue counts//
    public static final String PREF_LAST_TOTAL_TRAFFIC_ISSUES = "PREF_LAST_TOTAL_TRAFFIC_ISSUES";
    public static final String PREF_LAST_NEAREST_TRAFFIC_ISSUES = "PREF_LAST_NEAREST_TRAFFIC_ISSUES";
    public static final String PREF_LAST_TOTAL_MUNICIPAL_ISSUES = "PREF_LAST_TOTAL_MUNICIPAL_ISSUES";
    public static final String PREF_LAST_NEAREST_MUNICIPAL_ISSUES = "PREF_LAST_NEAREST_MUNICIPAL_ISSUES";
    public static final String PREF_LAST_TOTAL_ISSUES = "PREF_LAST_TOTAL_ISSUES";
    public static final String PREF_LAST_TOTAL_NEAREST_ISSUES = "PREF_LAST_TOTAL_NEAREST_ISSUES";
    public static final String PREF_LAST_MY_ISSUES = "PREF_LAST_MY_ISSUES";

    //issue counts//
}
