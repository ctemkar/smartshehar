package com.jumpinjumpout.apk.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TAG = "DatabhaseHandler: ";
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "jijo_local_database";
    private static final String TABLE_SCHEDULED_TRIPS = "trip_location";
    private static final String KEY_ID = "id";
    private static final String TRIP_LAT = "trip_lat";
    private static final String TRIP_LNG = "trip_lng";
    private static final String TRIP_ACCURACY = "trip_accuracy";
    private static final String TRIP_ALTITUDE = "trip_altitude";
    private static final String TRIP_BEARING = "trip_bearing";
    private static final String TRIP_SPEED = "trip_speed";
    private static final String TRIP_LOCTIME = "trip_loctime";
    private static final String TRIP_PROVIDER = "trip_provider";
    private static final String SENT_TO_SERVER = "sent_to_server";
    // Trip Schdhule table
    private static final String TABLE_TRIP_SCHDHULE = "trip_schedule";
    private static final String START_ADDRESS = "start_address";
    private static final String START_LAT = "start_lat";
    private static final String START_LNG = "start_lng";
    private static final String DESTINATION_ADDRESS = "destination_address";
    private static final String DESTINATION_LAT = "destination_lat";
    private static final String DESTINATION_LNG = "destination_lng";
    private static final String SCHDHULE_TIME = "schdhule_time";
    private static final String SC_SUN = "sc_sun";
    private static final String SC_MON = "sc_mon";
    private static final String SC_TWE = "sc_twe";
    private static final String SC_WES = "sc_wes";
    private static final String SC_THU = "sc_thu";
    private static final String SC_FRI = "sc_fri";
    private static final String SC_SAT = "sc_sat";
    private static final String CURRENT_TIME_IN_SEC = "cuttenttime_insec";
    private static final String CURRENT_DATE = "current_date";

    String CREATE_SCHDHULE_TABLE = "CREATE TABLE " + TABLE_TRIP_SCHDHULE + "("
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + START_LAT + " TEXT, "
            + START_LNG + " TEXT, "
            + START_ADDRESS + " TEXT, "
            + DESTINATION_ADDRESS + " TEXT, "
            + DESTINATION_LAT + " TEXT, "
            + DESTINATION_LNG + " TEXT, "
            + SCHDHULE_TIME + " TEXT, "
            + SC_SUN + " INTEGER DEFAULT 0, "
            + SC_MON + " INTEGER DEFAULT 0, "
            + SC_TWE + " INTEGER DEFAULT 0, "
            + SC_WES + " INTEGER DEFAULT 0, "
            + SC_THU + " INTEGER DEFAULT 0, "
            + SC_FRI + " INTEGER DEFAULT 0, "
            + SC_SAT + " INTEGER DEFAULT 0, "
            + CURRENT_TIME_IN_SEC + " INTEGER DEFAULT 0, "
            + CURRENT_DATE + " TEXT"
            + ")";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_SCHEDULED_TRIPS + "("
                    + KEY_ID + " INTEGER PRIMARY KEY, "
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
            db.execSQL(CREATE_SCHDHULE_TABLE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < DATABASE_VERSION) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULED_TRIPS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP_SCHDHULE);
                onCreate(db);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMissingLocation(MissingLocation missingLocation) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String strSQL = "insert into trip_location " +
                    "(trip_lat,trip_lng,trip_accuracy,trip_altitude,trip_bearing,trip_speed,trip_loctime,trip_provider,sent_to_server)" +
                    "values('" + missingLocation.getTrip_lat() + "','" + missingLocation.getTrip_lng() + "','" + missingLocation.getAccuracy() + "','"
                    + missingLocation.getAltitude() + "','" + missingLocation.getBearing() + "','" + missingLocation.getSpeed() + "','" +
                    missingLocation.getLoctime() + "','" + missingLocation.getSProvider() + "','" + missingLocation.getSent_to_server() + "') ;";
            db.execSQL(strSQL);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateSentToServer(int senttoserver) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String strSQL = "UPDATE " + TABLE_SCHEDULED_TRIPS + " SET sent_to_server = " + senttoserver;
            db.execSQL(strSQL);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLastRunDate(Date lastRunDate) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String strSQL = "UPDATE " + TABLE_SCHEDULED_TRIPS + " SET lastrundate = " + lastRunDate.toString();
            db.execSQL(strSQL);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<MissingLocation> getSentToServer(int tripID) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<MissingLocation> missingLocationList = new ArrayList<MissingLocation>();
        try {
            String selectQuery = "SELECT trip_lat, trip_lng, trip_accuracy, trip_altitude, trip_bearing, trip_speed, trip_loctime, trip_provider FROM " +
                    TABLE_SCHEDULED_TRIPS + " WHERE sent_to_server=0 ORDER BY " + KEY_ID;
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    MissingLocation missingLocation = new MissingLocation();
                    double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex("trip_lat")));
                    double lng = Double.parseDouble(cursor.getString(cursor.getColumnIndex("trip_lng")));
                    float acc = Float.parseFloat(cursor.getString(cursor.getColumnIndex("trip_accuracy")));
                    double alt = Double.parseDouble(cursor.getString(cursor.getColumnIndex("trip_altitude")));
                    float bear = Float.parseFloat(cursor.getString(cursor.getColumnIndex("trip_bearing")));
                    float speed = Float.parseFloat(cursor.getString(cursor.getColumnIndex("trip_speed")));
                    String loctime = cursor.getString(cursor.getColumnIndex("trip_loctime"));
                    String prov = cursor.getString(cursor.getColumnIndex("trip_provider"));
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
            e.printStackTrace();
        }
        return missingLocationList;

    }
//  SCHDHULE TRIP

    public int getDatabaseRow(int sCurrentTimeinSec, String sCurrentDate) {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String SCount = "select * from " + TABLE_TRIP_SCHDHULE + " where (" + sCurrentTimeinSec + " - cuttenttime_insec)/1000 > - 10*60 AND ("
                    + sCurrentTimeinSec + " - cuttenttime_insec)/1000 <= 15*60 AND current_date = '" + sCurrentDate + "'";
            Cursor cursor = db.rawQuery(SCount, null);
            Log.v("Cursor Object", DatabaseUtils.dumpCursorToString(cursor));
            if (cursor.moveToFirst()) {
                do {
                    count = cursor.getCount();
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            db.close();
            e.printStackTrace();
        }
        return count;
    }

    public void schdhuleTripInsert(String start, String startLAT, String startLNG, String drstination,
                                   String desLAT, String desLNG, String time, int sun, int mon, int twe,
                                   int wes, int thu, int fri, int sat, int sCurrentTimeinSec, String sCurrentDate) {
        try {
            int iCount = getDatabaseRow(sCurrentTimeinSec, sCurrentDate);
            if (iCount == 0) {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(START_ADDRESS, start);
                values.put(START_LAT, startLAT);
                values.put(START_LNG, startLNG);
                values.put(DESTINATION_ADDRESS, drstination);
                values.put(DESTINATION_LAT, desLAT);
                values.put(DESTINATION_LNG, desLNG);
                values.put(SCHDHULE_TIME, time);
                values.put(SC_SUN, sun);
                values.put(SC_MON, mon);
                values.put(SC_TWE, twe);
                values.put(SC_WES, wes);
                values.put(SC_THU, thu);
                values.put(SC_FRI, fri);
                values.put(SC_SAT, sat);
                values.put(CURRENT_TIME_IN_SEC, sCurrentTimeinSec);
                values.put(CURRENT_DATE, sCurrentDate);
                db.insertOrThrow(TABLE_TRIP_SCHDHULE, null, values);
                db.close();
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<CTrip> getSchdhuleList() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<CTrip> scheduleResultArrayList = new ArrayList<CTrip>();
        try {
            String selectQuery = "SELECT " + KEY_ID + ", start_address, destination_address, schdhule_time, sc_sun, sc_mon," +
                    " sc_twe, sc_wes, sc_thu, sc_fri, sc_sat, start_lat, start_lng, destination_lat, destination_lng FROM " +
                    TABLE_TRIP_SCHDHULE;
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    CTrip scheduleResult = new CTrip();
                    int keyid = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                    String start = cursor.getString(cursor.getColumnIndex("start_address"));
                    String slat = cursor.getString(cursor.getColumnIndex("start_lat"));
                    String slng = cursor.getString(cursor.getColumnIndex("start_lng"));
                    String destination = cursor.getString(cursor.getColumnIndex("destination_address"));
                    String dlat = cursor.getString(cursor.getColumnIndex("destination_lat"));
                    String dlng = cursor.getString(cursor.getColumnIndex("destination_lng"));
                    String time = cursor.getString(cursor.getColumnIndex("schdhule_time"));
                    int sun = Integer.parseInt(cursor.getString(cursor.getColumnIndex("sc_sun")));
                    int mon = Integer.parseInt(cursor.getString(cursor.getColumnIndex("sc_mon")));
                    int twe = Integer.parseInt(cursor.getString(cursor.getColumnIndex("sc_twe")));
                    int wes = Integer.parseInt(cursor.getString(cursor.getColumnIndex("sc_wes")));
                    int thu = Integer.parseInt(cursor.getString(cursor.getColumnIndex("sc_thu")));
                    int fri = Integer.parseInt(cursor.getString(cursor.getColumnIndex("sc_fri")));
                    int sat = Integer.parseInt(cursor.getString(cursor.getColumnIndex("sc_sat")));

                    scheduleResult.setId(keyid);
                    scheduleResult.setstart_address(start);
                    scheduleResult.setstart_Lat(slat);
                    scheduleResult.setstart_Lng(slng);
                    scheduleResult.setdestination_address(destination);
                    scheduleResult.setdestination_Lat(dlat);
                    scheduleResult.setdestination_Lng(dlng);
                    scheduleResult.setStime(time);
                    scheduleResult.setsun(sun);
                    scheduleResult.setmon(mon);
                    scheduleResult.settwe(twe);
                    scheduleResult.setwes(wes);
                    scheduleResult.setthu(thu);
                    scheduleResult.setfri(fri);
                    scheduleResult.setsat(sat);
                    // Adding contact to list
                    scheduleResultArrayList.add(scheduleResult);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            db.close();
            e.printStackTrace();
        }
        return scheduleResultArrayList;

    }

    public void deleteSchedule(int rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + TABLE_TRIP_SCHDHULE + " WHERE " + KEY_ID + " = " + Integer.toString(rowId));
        } catch (Exception e) {
            SSLog.e(TAG, "deleteSchedule", e);
        }
    }

    /*private static final String TABLE_MARATHI_ADDRESS = "city_marathi_address";
    private static final String START_ADDRESS = "start_address";
    private static final String START_LAT = "start_lat";
    private static final String START_LNG = "start_lng";
    private static final String DESTINATION_ADDRESS = "destination_address";
    private static final String DESTINATION_LAT = "destination_lat";
    private static final String DESTINATION_LNG = "destination_lng";
    private static final String SCHDHULE_TIME = "schdhule_time";
    private static final String SC_SUN = "sc_sun";
    private static final String SC_MON = "sc_mon";
    private static final String SC_TWE = "sc_twe";
    private static final String SC_WES = "sc_wes";
    private static final String SC_THU = "sc_thu";
    private static final String SC_FRI = "sc_fri";
    private static final String SC_SAT = "sc_sat";
    private static final String CURRENT_TIME_IN_SEC = "cuttenttime_insec";
    private static final String CURRENT_DATE = "current_date";*/

}
