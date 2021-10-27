package com.jumpinjumpout;


import lib.app.util.Constants_lib_ss;

public class Constants_dp {
    public static final String PREF_PHONENO = "PREF_PHONENO";
    public static final String PREF_COUNTRY_CODE = "PREF_COUNTRY_CODE";
    public static final String PREF_APPUSERID = "PREF_APPUSERID";
    public static final String PREF_REG_ID = "PREF_REG_ID";
    public static final String PREF_PROPERTY_APP_VERSION_NAME = "PREF_PROPERTY_APP_VERSION_NAME";
    public static final String PREF_CURRENT_COUNTRY = "PREF_CURRENT_COUNTRY";
    public static final String PREF_USER_TYPE = "PREF_USER_TYPE";
    public static final String PREF_REGISTERED = "PREF_REGISTERED";
    public static final String PREF_MY_LOCATION = "PREF_MY_LOCATION";
    public static final int TWO_MINUTES = 1000 * 60 * 2;

    public static final String VERSION = "v1";
    public static final String APPNAME = "cabeapp";
//    public static final String ALPHA = "/alpha";
   public static final String ALPHA = Constants_lib_ss.ALPHA;
//     public static final String ALPHA = "/beta";
  //public static final String ALPHA = "/prod";
    public static final String AUTHORITY = "www.cabebooking.com";
    public static final String SITE = "https://" + AUTHORITY;


    public static final String PHP_DIRECTORY = ALPHA + "/" + APPNAME
            + "/" + VERSION + "/svr/php/";
    public static final String PHP_PATH = SITE + PHP_DIRECTORY;

    public static final String USER_ACCESS_URL = PHP_PATH + "driver/user_access_driver.php";
    public static final String PREFS_VERSION_PERSISTENT = "PREFS_VERSION_PERSISTENT";
    public static final String PREFS_LAST_SYNC_DTAE = "PREFS_LAST_SYNC_DTAE";
    public static String INSERT_DRIVER_PROFILE_DATA_URL = PHP_PATH + "driver_registration/insert_driver_profile_data.php";
    public static String INSERT_OTHER_COMPANY_CODE_URL =  PHP_PATH +"driver_registration/insert_other_company_code.php";
    public static String DRIVER_IMAGE_URL = PHP_PATH + "driver_registration/driver_image.php";
    public static String GET_DRIVER_PROFILE_DATA = PHP_PATH + "driver_registration/get_driver_profile_data.php";

    public static String GET_DRIVER_IMAGE_URL = SITE+ALPHA + "/" + APPNAME;
}
