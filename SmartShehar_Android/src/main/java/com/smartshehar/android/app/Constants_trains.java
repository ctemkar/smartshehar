package com.smartshehar.android.app;


import lib.app.util.Constants_lib_ss;

public class Constants_trains {
    public static final String PREFS_VERSION_PERSISTENT = "PREFS_VERSION_PERSISTENT";
    public static final String VERSION = "v15";
    public static final String APPNAME = "smartsheharapp";
    public static final String PREF_REG_ID = "PREF_REG_ID";
    public static final String PREF_PROPERTY_APP_VERSION_NAME = "PREF_PROPERTY_APP_VERSION_NAME";
    public static final String PREF_USER_TYPE =  "PREF_USER_TYPE";
//    public static final String ALPHA = "alpha";
    public static final String ALPHA = Constants_lib_ss.ALPHA;

    public static final String SITE = "http://www.smartshehar.com";
    public static final String PHP_DIRECTORY = "/"+ALPHA + "/" + APPNAME +"/"+
            VERSION + "/svr" + "/php/";
    public static String MEGABLOCK_PHP = SITE+PHP_DIRECTORY+"train/t_megablock.php?";
    public static final String USER_ACCESS_URL =  SITE+PHP_DIRECTORY + "offence/da_user_access.php";
    public static final String ADD_CALL_HOME_URL =  SITE+PHP_DIRECTORY + "add_callhome.php";
    public static final String PREF_REGISTERED = "PREF_REGISTERED";
    public static final String PREF_CURRENT_COUNTRY = "PREF_CURRENT_COUNTRY";
    public static final String MEGABLOCK="megablock";
    public static  final String LAST_LAT="LAST_LAT";
    public static  final String LAST_LNG="LAST_LNG";
    public static final String PREF_RECEIVE_NOTIFICATIONS = "PREF_RECEIVE_NOTIFICATIONS";
    public static String PREF_NOTIFICATION_CLEAR_FLAG = "PREF_NOTIFICATION_CLEAR_FLAG";
    public static final int SERVER_NOTIFICATION_ID = 9;
    public static final String PREF_TOKEN_SAVED = "PREF_TOKEN_SAVED";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
}
