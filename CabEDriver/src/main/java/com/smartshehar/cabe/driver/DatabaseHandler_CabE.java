package com.smartshehar.cabe.driver;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;

public class DatabaseHandler_CabE extends SQLiteOpenHelper {
    private Context mcontext;
    private static final String TAG = "DatabhaseHandler: ";
    private static final int DATABASE_VERSION = 19;
    private static final String DATABASE_NAME = "jijo_local_database";
    private static final String TABLE_MISSING_LOCATION = "trip_location";
    private static final String KEY_ID = "id";
    private static final String TRIP_CURRENT_DATETIME = "current_datetime";
    private static final String TRIP_LAT = "trip_lat";
    private static final String TRIP_LNG = "trip_lng";
    private static final String TRIP_ACCURACY = "trip_accuracy";
    private static final String TRIP_ALTITUDE = "trip_altitude";
    private static final String TRIP_BEARING = "trip_bearing";
    private static final String TRIP_SPEED = "trip_speed";
    private static final String TRIP_LOCTIME = "trip_loctime";
    private static final String TRIP_PROVIDER = "trip_provider";
    private static final String SENT_TO_SERVER = "sent_to_server";

    private static final String TABLE_LOCAL_DATA = "local_data";
    private static final String ID = "a_id";
    private static final String APPUSER_ID = "appuser_id";
    private static final String EMAIL_ID = "email_id";
    private static final String TRIP_ID = "trip_id";
    private static final String TRIP_TYPE = "trip_type";
    private static final String TRIP_ACTION = "trip_action";
    private static final String CURRENT_DATETIME = "current_datetime";
    private static final String PROVIDER = "provider";
    private static final String LAT = "lat";
    private static final String LNG = "lng";
    private static final String ACCURACY = "accuracy";
    private static final String ALTITUDE = "altitude";
    private static final String BEARING = "bearing";
    private static final String SPEED = "speed";
    private static final String LOCTIME = "loctime";
    private static final String IS_FOR_HIRE = "is_for_hire";
    private static final String SAVE_DATE_TIME = "save_date_time";
    private static final String IMEI = "imei";
    private static final String SHIF_ID = "shift_id";
    private static final String VEHICLE_ID = "vehicle_id";
    private static final String CANCEL_REASON_CODE = "cancel_reason_code";
    private static final String FROM_ADDRESS = "from_address";
    private static final String FROM_SUBLOCALITY = "from_sublocality";
    private static final String FROM_LAT = "from_lat";
    private static final String FROM_LNG = "from_lng";
    private static final String TO_ADDRESS = "to_address";
    private static final String TO_SUBLOCALITY = "to_sublocality";
    private static final String TO_LAT = "to_lat";
    private static final String TO_LNG = "to_lng";
    private static final String PLANNED_START_DATETIME = "planned_start_datetime";
    private static final String TRIP_COST = "trip_cost";
    private static final String TRIP_DISTANCE = "trip_distance";
    private static final String TRIP_PATH = "trip_path";
    private static final String PASSENGER_APPUSER_ID = "passenger_appuser_id";
    private static final String JUMP_IN_OUT = "jump_in_out";
    private static final String PHP_NAME_CODE = "php_name_code";

    private static final String TABLE_FIXED_ADDRESS = "fixed_address";
    private static final String FIXED_ID = "fixed_id";
    private static final String FIXED_ADDRESS_ID = "fixed_address_id";
    private static final String AREA = "area";
    private static final String LANDMARK = "landmark";
    private static final String PICK_DROP_POINT = "pick_drop_point";
    private static final String FORMATTED_ADDRESS = "formatted_address";
    private static final String LOCALITY = "locality";
    private static final String SUBLOCALITY = "sublocality";
    private static final String POSTAL_CODE = "postal_code";
    private static final String ROUTE = "route";
    private static final String NEIGHBORHOOD = "neighborhood";
    private static final String ADMINISTRATIVE_AREA_LEVEL_2 = "administrative_area_level_2";
    private static final String ADMINISTRATIVE_AREA_LEVEL_1 = "administrative_area_level_1";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";

    public DatabaseHandler_CabE(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mcontext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_MISSING_LOCATION + "("
                    + KEY_ID + " INTEGER PRIMARY KEY, "
                    + TRIP_CURRENT_DATETIME + " TEXT, "
                    + TRIP_LAT + " REAL, "
                    + TRIP_LNG + " REAL, "
                    + TRIP_ACCURACY + " REAL, "
                    + TRIP_ALTITUDE + " REAL, "
                    + TRIP_BEARING + " REAL, "
                    + TRIP_SPEED + " REAL, "
                    + TRIP_LOCTIME + " TEXT, "
                    + TRIP_PROVIDER + " TEXT, "
                    + SENT_TO_SERVER + " INTEGER DEFAULT 0"
                    + ")";
            db.execSQL(CREATE_CONTACTS_TABLE);

            String CREATE_TABLE_LOCAL_DATA = "CREATE TABLE " + TABLE_LOCAL_DATA + "("
                    + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + APPUSER_ID + " INTEGER DEFAULT 0, "
                    + EMAIL_ID + " TEXT, "
                    + TRIP_ID + " INTEGER DEFAULT 0, "
                    + TRIP_TYPE + " TEXT, "
                    + TRIP_ACTION + " TEXT, "
                    + CURRENT_DATETIME + " TEXT, "
                    + PROVIDER + " TEXT, "
                    + LAT + " TEXT, "
                    + LNG + " TEXT, "
                    + ACCURACY + " TEXT, "
                    + ALTITUDE + " TEXT, "
                    + BEARING + " TEXT, "
                    + SPEED + " TEXT, "
                    + LOCTIME + " TEXT, "
                    + IS_FOR_HIRE + " TEXT, "
                    + SAVE_DATE_TIME + " TEXT, "
                    + IMEI + " TEXT, "
                    + SHIF_ID + " TEXT, "
                    + VEHICLE_ID + " TEXT, "
                    + CANCEL_REASON_CODE + " TEXT, "
                    + FROM_ADDRESS + " TEXT, "
                    + FROM_SUBLOCALITY + " TEXT, "
                    + FROM_LAT + " TEXT, "
                    + FROM_LNG + " TEXT, "
                    + TO_ADDRESS + " TEXT, "
                    + TO_SUBLOCALITY + " TEXT, "
                    + TO_LAT + " TEXT, "
                    + TO_LNG + " TEXT, "
                    + PLANNED_START_DATETIME + " TEXT, "
                    + TRIP_COST + " TEXT, "
                    + TRIP_DISTANCE + " TEXT, "
                    + TRIP_PATH + " TEXT, "
                    + PASSENGER_APPUSER_ID + " INTEGER DEFAULT 0, "
                    + JUMP_IN_OUT + " TEXT, "
                    + PHP_NAME_CODE + " TEXT"
                    + ")";
            db.execSQL(CREATE_TABLE_LOCAL_DATA);

            String CREATE_FIXED_ADDRESS = "CREATE TABLE " + TABLE_FIXED_ADDRESS + "("
                    + FIXED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + FIXED_ADDRESS_ID + " TEXT, "
                    + AREA + " TEXT, "
                    + LANDMARK + " TEXT, "
                    + PICK_DROP_POINT + " TEXT, "
                    + FORMATTED_ADDRESS + " TEXT, "
                    + LOCALITY + " TEXT, "
                    + SUBLOCALITY + " TEXT, "
                    + POSTAL_CODE + " TEXT, "
                    + ROUTE + " TEXT, "
                    + NEIGHBORHOOD + " TEXT, "
                    + ADMINISTRATIVE_AREA_LEVEL_2 + " TEXT, "
                    + ADMINISTRATIVE_AREA_LEVEL_1 + " TEXT, "
                    + LATITUDE + " REAL, "
                    + LONGITUDE + " REAL"
                    + ")";
            db.execSQL(CREATE_FIXED_ADDRESS);
        } catch (Exception e) {
            SSLog_SS.e(TAG, " onCreate:- ", e, mcontext);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < DATABASE_VERSION) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MISSING_LOCATION);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCAL_DATA);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FIXED_ADDRESS);
                onCreate(db);
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, " onUpgrade:- ", e, mcontext);
        }
    }

    public void addMissingLocation(MissingLocation_CabE missingLocation) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String strSQL = "insert into trip_location " +
                    "(current_datetime,trip_lat,trip_lng,trip_accuracy,trip_altitude,trip_bearing,trip_speed,trip_loctime,trip_provider,sent_to_server)" +
                    "values('" + missingLocation.getCurrent_Datetime() + "','" + missingLocation.getTrip_lat() + "','" + missingLocation.getTrip_lng() + "','" + missingLocation.getAccuracy() + "','"
                    + missingLocation.getAltitude() + "','" + missingLocation.getBearing() + "','" + missingLocation.getSpeed() + "','" +
                    missingLocation.getLoctime() + "','" + missingLocation.getSProvider() + "','" + missingLocation.getSent_to_server() + "') ;";
            db.execSQL(strSQL);
            db.close();
        } catch (Exception e) {
            SSLog_SS.e(TAG, " addMissingLocation:- ", e, mcontext);
        }
    }

    public void deleteMissingLocation(String current_datetime) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("DELETE from " + TABLE_MISSING_LOCATION + " WHERE  " + TRIP_CURRENT_DATETIME + " = '" + current_datetime + "'");
        } catch (Exception e) {
            SSLog_SS.e(TAG, " deleteMissingLocation:- ", e, mcontext);
        }
    }

    public ArrayList<MissingLocation_CabE> getSentToServer() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<MissingLocation_CabE> missingLocationList = new ArrayList<>();
        try {
            /*String selectQuery = "SELECT id, current_datetime, trip_lat, trip_lng, trip_accuracy, trip_altitude, trip_bearing, trip_speed, trip_loctime, trip_provider FROM " +
                    TABLE_MISSING_LOCATION + " GROUP BY " + TRIP_CURRENT_DATETIME + "";*/
            /*String selectQuery = "SELECT id, current_datetime, trip_lat, trip_lng, trip_accuracy, trip_altitude, trip_bearing, trip_speed, trip_loctime, trip_provider FROM " +
                    TABLE_MISSING_LOCATION + " GROUP BY " + TRIP_LAT + "," + TRIP_LNG + "," + TRIP_CURRENT_DATETIME +
                    " ORDER BY "+ TRIP_CURRENT_DATETIME + " DESC LIMIT 1";*/

            String selectQuery = "SELECT id, current_datetime, trip_lat, trip_lng, trip_accuracy, trip_altitude, trip_bearing, trip_speed, trip_loctime, trip_provider FROM "
                    + TABLE_MISSING_LOCATION + " GROUP BY " + TRIP_LAT
                    + "," + TRIP_LNG + "," + TRIP_CURRENT_DATETIME + " LIMIT 1";
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    MissingLocation_CabE missingLocation = new MissingLocation_CabE();
                    int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));
                    String current_datetime = cursor.getString(cursor.getColumnIndex("current_datetime"));
                    double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex("trip_lat")));
                    double lng = Double.parseDouble(cursor.getString(cursor.getColumnIndex("trip_lng")));
                    float acc = Float.parseFloat(cursor.getString(cursor.getColumnIndex("trip_accuracy")));
                    double alt = Double.parseDouble(cursor.getString(cursor.getColumnIndex("trip_altitude")));
                    float bear = Float.parseFloat(cursor.getString(cursor.getColumnIndex("trip_bearing")));
                    float speed = Float.parseFloat(cursor.getString(cursor.getColumnIndex("trip_speed")));
                    String loctime = cursor.getString(cursor.getColumnIndex("trip_loctime"));
                    String prov = cursor.getString(cursor.getColumnIndex("trip_provider"));
                    missingLocation.setID(id);
                    missingLocation.setCurrent_Datetime(current_datetime);
                    missingLocation.setTrip_lat(lat);
                    missingLocation.setTrip_lng(lng);
                    missingLocation.setAccuracy(acc);
                    missingLocation.setAltitude(alt);
                    missingLocation.setBearing(bear);
                    missingLocation.setSpeed(speed);
                    missingLocation.setLoctime(loctime);
                    missingLocation.setSProvider(prov);
                    // Adding contact to list
                    missingLocationList.add(missingLocation);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            db.close();
            SSLog_SS.e(TAG, " getSentToServer:- ", e, mcontext);
        }
        return missingLocationList;
    }

    //Trip Action insert
    public void addTripAction(Location location, int tripid, int appuserid, String triptype, String tripaction,
                              String emailid, String currentdatetime) {
        try {

            SimpleDateFormat df = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT,
                    Locale.getDefault());
            Date locationDateTime = new Date(location.getTime());
            SQLiteDatabase db = this.getWritableDatabase();
            String strSQL = "insert into " + TABLE_LOCAL_DATA +
                    " (" + APPUSER_ID + "," + EMAIL_ID + "," + TRIP_ID + "," + TRIP_TYPE
                    + "," + TRIP_ACTION + "," + CURRENT_DATETIME + "," + PROVIDER + ","
                    + LAT + "," + LNG + "," + ACCURACY + "," + ALTITUDE
                    + "," + BEARING + "," + SPEED + "," + LOCTIME + "," + PHP_NAME_CODE + ")" +
                    "values('" + appuserid + "','" + emailid + "','"
                    + tripid + "','" + triptype + "','"
                    + tripaction + "','" + currentdatetime + "','"
                    + location.getProvider() + "','" + location.getLatitude() + "','"
                    + location.getLongitude() + "','" + location.getAccuracy() + "','"
                    + location.getAltitude() + "','" + location.getBearing() + "','"
                    + location.getSpeed() + "','" + df.format(locationDateTime)
                    + "','" + "TAD" + "') ;";
            db.execSQL(strSQL);
            db.close();
        } catch (Exception e) {
            SSLog_SS.e(TAG, " addMissingLocation:- ", e, mcontext);
        }
    }

    //Set Flag insert
    public void addFlagSetDriver(String setforhire, String saveDateTime, int appuserid, String emailid, String imei) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String strSQL = "insert into " + TABLE_LOCAL_DATA +
                    " (" + APPUSER_ID + "," + EMAIL_ID + "," + IS_FOR_HIRE + "," + SAVE_DATE_TIME + ","
                    + IMEI + "," + PHP_NAME_CODE + ")" +
                    "values('" + appuserid + "','" + emailid + "','" + setforhire + "','"
                    + saveDateTime + "','" + imei + "','" + "SFH" + "') ;";
            db.execSQL(strSQL);
            db.close();
        } catch (Exception e) {
            SSLog_SS.e(TAG, " addMissingLocation:- ", e, mcontext);
        }
    }

    //Driver End Shif insert
    public void addDriverShifEnd(int appuserid, String emailid, String currentdatetime, String imei, String shifid, String vehicleid) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String strSQL = "insert into " + TABLE_LOCAL_DATA +
                    " (" + APPUSER_ID + "," + EMAIL_ID + "," + CURRENT_DATETIME + "," + IMEI + ","
                    + SHIF_ID + "," + VEHICLE_ID + "," + PHP_NAME_CODE + ")" +
                    "values('" + appuserid + "','" + emailid + "','" + currentdatetime + "','"
                    + imei + "','" + shifid + "','" + vehicleid + "','" + "DES" + "') ;";
            db.execSQL(strSQL);
            db.close();
        } catch (Exception e) {
            SSLog_SS.e(TAG, " addMissingLocation:- ", e, mcontext);
        }
    }

    //Driver Cancel Trip insert
    public void addCancelDriver(int appuserid, String emailid, int tripid, String triptype, String currentdatetime,
                                String imei, String cancelreasoncode) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String strSQL = "insert into " + TABLE_LOCAL_DATA +
                    " (" + APPUSER_ID + "," + EMAIL_ID + "," + TRIP_ID + "," + TRIP_TYPE + ","
                    + CURRENT_DATETIME + "," + IMEI + "," + CANCEL_REASON_CODE + "," + PHP_NAME_CODE + ")" +
                    "values('" + appuserid + "','" + emailid + "','" + tripid + "','"
                    + triptype + "','" + currentdatetime + "','" + imei + "','" + cancelreasoncode + "','" + "CTD" + "') ;";
            db.execSQL(strSQL);
            db.close();
        } catch (Exception e) {
            SSLog_SS.e(TAG, " addMissingLocation:- ", e, mcontext);
        }
    }

    // Create trip Driver insert
    public void addCreateTrip(Location location, int appuserid, String triptype, String tripaction,
                              String emailid, String currentdatetime, String fromaddress, String fromsublocality, String fromlat,
                              String fromlng, String toaddress, String tosublocality, String tolat,
                              String tolng, String plannedstartdatetime) {
        try {

            SimpleDateFormat df = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT,
                    Locale.getDefault());
            Date locationDateTime = new Date(location.getTime());
            SQLiteDatabase db = this.getWritableDatabase();
            String strSQL = "insert into " + TABLE_LOCAL_DATA +
                    " (" + APPUSER_ID + "," + EMAIL_ID + "," + TRIP_TYPE
                    + "," + TRIP_ACTION + "," + CURRENT_DATETIME + "," + PROVIDER + ","
                    + LAT + "," + LNG + "," + ACCURACY + "," + ALTITUDE
                    + "," + BEARING + "," + SPEED + "," + LOCTIME + "," + FROM_ADDRESS + ","
                    + FROM_SUBLOCALITY + "," + FROM_LAT + "," + FROM_LNG + "," + TO_ADDRESS + "," + TO_SUBLOCALITY + ","
                    + TO_LAT + "," + TO_LNG + "," + PLANNED_START_DATETIME + "," + PHP_NAME_CODE + ")" +
                    "values('" + appuserid + "','" + emailid + "','" + triptype + "','"
                    + tripaction + "','" + currentdatetime + "','"
                    + location.getProvider() + "','" + location.getLatitude() + "','"
                    + location.getLongitude() + "','" + location.getAccuracy() + "','"
                    + location.getAltitude() + "','" + location.getBearing() + "','"
                    + location.getSpeed() + "','" + df.format(locationDateTime) + "','"
                    + fromaddress + "','" + fromsublocality + "','" + fromlat + "','" + fromlng + "','"
                    + toaddress + "','" + tosublocality + "','" + tolat + "','" + tolng + "','"
                    + plannedstartdatetime + "','" + "TCD" + "') ;";
            db.execSQL(strSQL);
            db.close();
        } catch (Exception e) {
            SSLog_SS.e(TAG, " addMissingLocation:- ", e, mcontext);
        }
    }

    // Trip Cost & Ditance insert
    public void addTripCostDistance(int appuserid, int tripid, String triptype, String emailid, String currentdatetime, String imei,
                                    String tripcost, String tripdistance) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String strSQL = "insert into " + TABLE_LOCAL_DATA +
                    " (" + APPUSER_ID + "," + EMAIL_ID + "," + TRIP_ID + "," + TRIP_TYPE + ","
                    + CURRENT_DATETIME + "," + IMEI + "," + TRIP_COST + "," + TRIP_DISTANCE + "," + PHP_NAME_CODE + ")" +
                    "values('" + appuserid + "','" + emailid + "','" + tripid + "','"
                    + triptype + "','" + currentdatetime + "','" + imei + "','" + tripcost + "','" +
                    tripdistance + "','" + "UTC" + "') ;";
            db.execSQL(strSQL);
            db.close();
        } catch (Exception e) {
            SSLog_SS.e(TAG, " addMissingLocation:- ", e, mcontext);
        }
    }

    // Get Trip Path insert
    public void addGetTripPath(int appuserid, int tripid, String emailid, String currentdatetime, String imei) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String strSQL = "insert into " + TABLE_LOCAL_DATA +
                    " (" + APPUSER_ID + "," + EMAIL_ID + "," + TRIP_ID + "," +
                    CURRENT_DATETIME + "," + IMEI + "," + PHP_NAME_CODE + ")" +
                    "values('" + appuserid + "','" + emailid + "','" + tripid + "','"
                    + currentdatetime + "','" + imei + "','" + "GTP" + "') ;";
            db.execSQL(strSQL);
            db.close();
        } catch (Exception e) {
            SSLog_SS.e(TAG, " addMissingLocation:- ", e, mcontext);
        }
    }

    // Create Trip Path insert
    public void addCreateTripPath(int appuserid, int tripid, String emailid, String currentdatetime, String imei, String path) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String strSQL = "insert into " + TABLE_LOCAL_DATA +
                    " (" + APPUSER_ID + "," + EMAIL_ID + "," + TRIP_ID + "," +
                    CURRENT_DATETIME + "," + IMEI + "," + TRIP_PATH + "," + PHP_NAME_CODE + ")" +
                    "values('" + appuserid + "','" + emailid + "','" + tripid + "','"
                    + currentdatetime + "','" + imei + "','" + path + "','" + "CTP" + "') ;";
            db.execSQL(strSQL);
            db.close();
        } catch (Exception e) {
            SSLog_SS.e(TAG, " addMissingLocation:- ", e, mcontext);
        }
    }

    // Jump In Out insert
    public void addJumpInOutTrip(Location location, int appuserid, int tripid, int passengerappuserid,
                                 String emailid, String currentdatetime, String jumpinout) {
        try {

            SimpleDateFormat df = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT,
                    Locale.getDefault());
            Date locationDateTime = new Date(location.getTime());
            SQLiteDatabase db = this.getWritableDatabase();
            String strSQL = "insert into " + TABLE_LOCAL_DATA +
                    " (" + APPUSER_ID + "," + EMAIL_ID + "," + TRIP_ID + "," + CURRENT_DATETIME + "," + PROVIDER + ","
                    + LAT + "," + LNG + "," + ACCURACY + "," + ALTITUDE
                    + "," + BEARING + "," + SPEED + "," + LOCTIME + "," + PASSENGER_APPUSER_ID + ","
                    + JUMP_IN_OUT + "," + PHP_NAME_CODE + ")" +
                    "values('" + appuserid + "','" + emailid + "','" + tripid + "','" + currentdatetime + "','"
                    + location.getProvider() + "','" + location.getLatitude() + "','"
                    + location.getLongitude() + "','" + location.getAccuracy() + "','"
                    + location.getAltitude() + "','" + location.getBearing() + "','"
                    + location.getSpeed() + "','" + df.format(locationDateTime) + "','"
                    + passengerappuserid + "','" + jumpinout + "','" + "JI" + "') ;";
            db.execSQL(strSQL);
            db.close();
        } catch (Exception e) {
            SSLog_SS.e(TAG, " addMissingLocation:- ", e, mcontext);
        }
    }
// delete data

    public void deleteRowLocalTable(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("DELETE from " + TABLE_LOCAL_DATA + " WHERE " + ID + "=" + id);
        } catch (Exception e) {
            SSLog_SS.e(TAG, " deleteMissingLocation:- ", e, mcontext);
        }
    }

    // Select All data send to server
    public ArrayList<CabEDriver_params> sentDataToServer() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<CabEDriver_params> action_cabEs = new ArrayList<>();
        try {
            String selectQuery = "SELECT * FROM " +
                    TABLE_LOCAL_DATA + " ORDER BY " + ID + " LIMIT 1";
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    CabEDriver_params tripAction_cabE = new CabEDriver_params();

                    int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ID)));
                    int appuserid = Integer.parseInt(cursor.getString(cursor.getColumnIndex(APPUSER_ID)));
                    String emailid = cursor.getString(cursor.getColumnIndex(EMAIL_ID));
                    int tripid = Integer.parseInt(cursor.getString(cursor.getColumnIndex(TRIP_ID)));
                    String triptype = cursor.getString(cursor.getColumnIndex(TRIP_TYPE));
                    String tripaction = cursor.getString(cursor.getColumnIndex(TRIP_ACTION));
                    String currentdatetime = cursor.getString(cursor.getColumnIndex(CURRENT_DATETIME));
                    String provider = cursor.getString(cursor.getColumnIndex(PROVIDER));
                    String lat = cursor.getString(cursor.getColumnIndex(LAT));
                    String lng = cursor.getString(cursor.getColumnIndex(LNG));
                    String accuracy = cursor.getString(cursor.getColumnIndex(ACCURACY));
                    String altitude = cursor.getString(cursor.getColumnIndex(ALTITUDE));
                    String bearing = cursor.getString(cursor.getColumnIndex(BEARING));
                    String speed = cursor.getString(cursor.getColumnIndex(SPEED));
                    String loctime = cursor.getString(cursor.getColumnIndex(LOCTIME));
                    String isforhire = cursor.getString(cursor.getColumnIndex(IS_FOR_HIRE));
                    String savedatetime = cursor.getString(cursor.getColumnIndex(SAVE_DATE_TIME));
                    String imei = cursor.getString(cursor.getColumnIndex(IMEI));
                    String shifid = cursor.getString(cursor.getColumnIndex(SHIF_ID));
                    String vehicleid = cursor.getString(cursor.getColumnIndex(VEHICLE_ID));
                    String cancelreasoncode = cursor.getString(cursor.getColumnIndex(CANCEL_REASON_CODE));
                    String fromaddress = cursor.getString(cursor.getColumnIndex(FROM_ADDRESS));
                    String fromsublocality = cursor.getString(cursor.getColumnIndex(FROM_SUBLOCALITY));
                    String fromlat = cursor.getString(cursor.getColumnIndex(FROM_LAT));
                    String fromlng = cursor.getString(cursor.getColumnIndex(FROM_LNG));
                    String toaddress = cursor.getString(cursor.getColumnIndex(TO_ADDRESS));
                    String tosublocality = cursor.getString(cursor.getColumnIndex(TO_SUBLOCALITY));
                    String tolat = cursor.getString(cursor.getColumnIndex(TO_LAT));
                    String tolng = cursor.getString(cursor.getColumnIndex(TO_LNG));
                    String plannedstartdatetime = cursor.getString(cursor.getColumnIndex(PLANNED_START_DATETIME));
                    String tripcost = cursor.getString(cursor.getColumnIndex(TRIP_COST));
                    String tripdistance = cursor.getString(cursor.getColumnIndex(TRIP_DISTANCE));
                    String trippath = cursor.getString(cursor.getColumnIndex(TRIP_PATH));
                    int passengerappuserid = Integer.parseInt(cursor.getString(cursor.getColumnIndex(PASSENGER_APPUSER_ID)));
                    String jumpinout = cursor.getString(cursor.getColumnIndex(JUMP_IN_OUT));
                    String phpnamecode = cursor.getString(cursor.getColumnIndex(PHP_NAME_CODE));

                    tripAction_cabE.setID(id);
                    tripAction_cabE.setAppuser_id(appuserid);
                    tripAction_cabE.setTrip_id(tripid);
                    tripAction_cabE.setPassenger_appuser_id(passengerappuserid);
                    tripAction_cabE.setEmail_id(emailid);
                    tripAction_cabE.setTrip_type(triptype);
                    tripAction_cabE.setTrip_action(tripaction);
                    tripAction_cabE.setCurrent_datetime(currentdatetime);
                    tripAction_cabE.setProvider(provider);
                    tripAction_cabE.setLat(lat);
                    tripAction_cabE.setLng(lng);
                    tripAction_cabE.setAccuracy(accuracy);
                    tripAction_cabE.setAltitude(altitude);
                    tripAction_cabE.setBearing(bearing);
                    tripAction_cabE.setSpeed(speed);
                    tripAction_cabE.setLoctime(loctime);
                    tripAction_cabE.setIs_for_hire(isforhire);
                    tripAction_cabE.setSave_datetime(savedatetime);
                    tripAction_cabE.setImei(imei);
                    tripAction_cabE.setShifid(shifid);
                    tripAction_cabE.setVehicle_id(vehicleid);
                    tripAction_cabE.setCancel_reason_code(cancelreasoncode);
                    tripAction_cabE.setFrom_address(fromaddress);
                    tripAction_cabE.setFrom_sublocality(fromsublocality);
                    tripAction_cabE.setFrom_lat(fromlat);
                    tripAction_cabE.setFrom_lng(fromlng);
                    tripAction_cabE.setTo_address(toaddress);
                    tripAction_cabE.setTo_sublocality(tosublocality);
                    tripAction_cabE.setTo_lat(tolat);
                    tripAction_cabE.setTo_lng(tolng);
                    tripAction_cabE.setPlanned_start_datetime(plannedstartdatetime);
                    tripAction_cabE.setTrip_cost(tripcost);
                    tripAction_cabE.setTrip_distance(tripdistance);
                    tripAction_cabE.setTrip_path(trippath);
                    tripAction_cabE.setJump_inout(jumpinout);
                    tripAction_cabE.setPhp_name_code(phpnamecode);
                    // Adding contact to list
                    action_cabEs.add(tripAction_cabE);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            db.close();
            SSLog_SS.e(TAG, " getSentToServer:- ", e, mcontext);
        }
        return action_cabEs;
    }

// FIXED ADDRESS TABLE

    public void addFixedAddress(String fixed_address_id, String area, String landmark, String pick_drop_point,
                                String formatted_address, String locality, String sublocality, String postal_code, String route,
                                String neighborhood, String administrative_area_level_2, String administrative_area_level_1,
                                double latitude, double longitude) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String strSQL = "insert into " + TABLE_FIXED_ADDRESS +
                    " (" + FIXED_ADDRESS_ID + "," + AREA + "," + LANDMARK
                    + "," + PICK_DROP_POINT + "," + FORMATTED_ADDRESS + "," + LOCALITY + ","
                    + SUBLOCALITY + "," + POSTAL_CODE + "," + ROUTE + "," + NEIGHBORHOOD
                    + "," + ADMINISTRATIVE_AREA_LEVEL_2 + "," + ADMINISTRATIVE_AREA_LEVEL_1 + ","
                    + LATITUDE + "," + LONGITUDE + ")" +
                    "values('" + fixed_address_id + "','" + area + "','" + landmark + "','"
                    + pick_drop_point + "','" + formatted_address + "','"
                    + locality + "','" + sublocality + "','"
                    + postal_code + "','" + route + "','"
                    + neighborhood + "','" + administrative_area_level_2 + "','"
                    + administrative_area_level_1 + "','" + latitude + "','"
                    + longitude + "') ;";
            db.execSQL(strSQL);
            db.close();
        } catch (Exception e) {
            SSLog_SS.e(TAG, " addMissingLocation:- ", e, mcontext);
        }
    }

    // Select All data send to server
    public ArrayList<Fixed_Address> getFixedAddress() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Fixed_Address> action_cabEs = new ArrayList<>();
        try {
            String selectQuery = "SELECT * FROM " + TABLE_FIXED_ADDRESS;
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    Fixed_Address tripAction_cabE = new Fixed_Address();

                    int fixedid = Integer.parseInt(cursor.getString(cursor.getColumnIndex(FIXED_ID)));
                    String fixedaddressid = cursor.getString(cursor.getColumnIndex(FIXED_ADDRESS_ID));
                    String area = cursor.getString(cursor.getColumnIndex(AREA));
                    String landmark = cursor.getString(cursor.getColumnIndex(LANDMARK));
                    String pickdroppoint = cursor.getString(cursor.getColumnIndex(PICK_DROP_POINT));
                    String formattedaddress = cursor.getString(cursor.getColumnIndex(FORMATTED_ADDRESS));
                    String locality = cursor.getString(cursor.getColumnIndex(LOCALITY));
                    String sublocality = cursor.getString(cursor.getColumnIndex(SUBLOCALITY));
                    String postalcode = cursor.getString(cursor.getColumnIndex(POSTAL_CODE));
                    String route = cursor.getString(cursor.getColumnIndex(ROUTE));
                    String neighborhood = cursor.getString(cursor.getColumnIndex(NEIGHBORHOOD));
                    String administrativearealevel_2 = cursor.getString(cursor.getColumnIndex(ADMINISTRATIVE_AREA_LEVEL_2));
                    String administrativearealevel_1 = cursor.getString(cursor.getColumnIndex(ADMINISTRATIVE_AREA_LEVEL_1));
                    double latitudev = Double.parseDouble(cursor.getString(cursor.getColumnIndex(LATITUDE)));
                    double longitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex(LONGITUDE)));

                    tripAction_cabE.setFixed_id(fixedid);
                    tripAction_cabE.setFixed_address_id(fixedaddressid);
                    tripAction_cabE.setArea(area);
                    tripAction_cabE.setLandmark(landmark);
                    tripAction_cabE.setPick_Drop_Point(pickdroppoint);
                    tripAction_cabE.setFormatted_Address(formattedaddress);
                    tripAction_cabE.setLocality(locality);
                    tripAction_cabE.setSublocality(sublocality);
                    tripAction_cabE.setPostal_code(postalcode);
                    tripAction_cabE.setRoute(route);
                    tripAction_cabE.setNeighborhood(neighborhood);
                    tripAction_cabE.setAdministrative_area_level_2(administrativearealevel_2);
                    tripAction_cabE.setAdministrative_area_level_1(administrativearealevel_1);
                    tripAction_cabE.setLatitude(latitudev);
                    tripAction_cabE.setLongitude(longitude);
                    // Adding contact to list
                    action_cabEs.add(tripAction_cabE);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            db.close();
            SSLog_SS.e(TAG, " getSentToServer:- ", e, mcontext);
        }
        return action_cabEs;
    }

}