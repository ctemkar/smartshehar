package com.smartshehar.busdriver_apk;

/**
 * Created by user pc on 24-07-2015.
 */
public class Constants_bd {
    public static final String PREFS_BUS_START_ARRAY = "PREFS_BUS_START_ARRAY";
    public static final String PREF_APPUSERID = "PREF_APPUSERID";
    public static final String APP_TYPE = "BD";
    public static final String TRIP_TYPE = "BD";
    public static final String VERSION = "v15";
    public static final String APPNAME = "smartsheharapp";
    public static final String ALPHA = "/alpha";
    //      public static final String ALPHA = "/beta";
    //public static final String ALPHA = "/prod";
    public static final String AUTHORITY = "www.smartshehar.com";
    public static final String SITE = "http://" + AUTHORITY;
    // public static final String SITE = "http://hp-ferrari/sites";
    public static final String SITE_DIRECTORY = SITE + ALPHA + "/" + APPNAME;
    public static final String SVR_DIRECTORY = SITE_DIRECTORY + "/svr/apk/"
            + VERSION + "/";
    public static final String PHP_DIRECTORY = ALPHA + "/" + APPNAME + "/" + VERSION
            + "/svr/php/";
    public static final String PHP_PATH = SITE + PHP_DIRECTORY;
    public static String INSERT_BUSTRIP_URL = PHP_PATH + "bus/insert_bustrip.php";
    public static String UPDATE_BUS_LOCATION_URL = PHP_PATH + "bus/update_bus_location.php";
    public static String DA_USER_ACCESS_URL = PHP_PATH + "da_user_access.php";
    public static String UPDATE_POSITION_BUS_URL = PHP_PATH + "bus/update_position_bus.php";
    public static String UPDATE_TRIP_STATUS_URL = PHP_PATH + "bus/update_trip_status.php";
    public static final String TRIP_CREATE_DRIVER_URL = PHP_PATH + "bus/create_trip_bus.php";
    public static final String TRIP_CREATE_TRIP_PATH_DRIVER_URL = PHP_PATH + "bus/create_trip_path_bus.php";
    public static String TRIP_CREATE= "C";
    public static String TRIP_BEGIN = "B";
    public static String TRIP_END = "E";
    public static final float NEAR_DESTINATION_DISTANCE = 150;
    public static final int TWO_MINUTES = 1000 * 60 * 2;
    public static final String PREF_MY_LOCATION = "PREF_MY_LOCATION";
    public static final String PREF_BUS_DRIVER_TRIP_ID = "PREF_BUS_DRIVER_TRIP_ID";
    public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final long DRIVER_UPDATE_INTERVAL = 10 * 1000;
    public static final String PREF_BUS_LABEL = "PREF_BUS_LABEL";
    public static final String PREF_BUS_START_LAT = "PREF_BUS_START_LAT";
    public static final String PREF_BUS_START_LON = "PREF_BUS_START_LON";
    public static final String PREF_BUS_DESTINATION_LAT = "PREF_BUS_DESTINATION_LAT";
    public static final String PREF_BUS_DESTINATION_LON = "PREF_BUS_DESTINATION_LON";
    public static final String PREFS_APP = "SAR_PREFS";
    public static final String PREFS_VERSION_PERSISTENT = "PREFS_VERSION_PERSISTENT";
    public static final String PREF_IN_TRIP = "IN_TRIP";
    public static final String PREF_TRIP_ACTION = "PREF_TRIP_ACTION";
    public static final String COL_IN_PATH = "inpath";
    public static final String PREF_CURRENT_PASSENGER_PHONENO = "PREF_CURRENT_PASSENGER_PHONENO";
    public static final String PREF_CURRENT_COUNTRY = "PREF_CURRENT_COUNTRY";
    public static final float NEAR_BE_READY_DISTANCE = 2000;
}
