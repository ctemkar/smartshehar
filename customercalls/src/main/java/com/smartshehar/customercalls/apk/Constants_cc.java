package com.smartshehar.customercalls.apk;
/**
 * Created by ctemkar on 07/03/2016.
 * Constants for CustomerCP Calls
 */
public class Constants_cc {
    public static final String PREF_PHONENO = "PREF_PHONENO";
    //public static final String PREF_CALL_LOG = "PREF_CALL_LOG";


    public static final String VERSION = "v1";
    public static final String APPNAME = "/customercallsapp";
    public static final String ALPHA = "/alpha";
    public static final String APP_CODE = "SCC";
    //     public static final String ALPHA = "/dev/beta";
//    public static final String ALPHA = "/prod";
    public static final String AUTHORITY = "www.smartshehar.com";
    public static final String SITE = "http://" + AUTHORITY;
    // public static final String SITE = "http://hp-ferrari/sites";
    public static final String SITE_DIRECTORY = SITE + ALPHA  + APPNAME;
    public static final String PHP_DIRECTORY = ALPHA  + APPNAME + "/" +
            VERSION + "/svr" + "/php/";
    public static final String PHP_PATH = SITE + PHP_DIRECTORY;
    public static final String ACRA_URL = PHP_PATH
            + "acra/acra.php";

    public static final String FIND_CUSTOMER_URL = PHP_PATH + "cc/find_customer.php";
    public static final String ADD_CUSTOMER_URL = PHP_PATH + "cc/add_customer_data.php";
    public static final String USER_ACCESS_URL = PHP_PATH + "cc/user_access.php";
    public static final String ADD_ORDER_HEADER_URL = PHP_PATH + "cc/add_order_header.php";
    public static int CHECKE_CONNECTION_INTERVAL = 1000 * 15/*60 * 3*/;

}
