package in.bestbus.app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import com.smartshehar.dashboard.app.CNearestStation;
import com.smartshehar.dashboard.app.SSLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import lib.app.util.SSLog_SS;


public class DBHelperBus extends SQLiteAssetHelper {
    public static final int SHORT_WALK = 700;    // 500 m - reasonable walking dist
    public static final int MAX_NEAR_BUSES = 1;
    public final static int CONNECTING_TRAIN_MIN_AFTER = 3; // 3 minutes to change and to allow for late trains
    public final static int CONNECTING_TRAIN_MIN_UPTO = 105; // look for trains up to 15 minutes after this train reaches
    public final static int MINUTES_IN_DAY = 24 * 60; // look for trains up to 15 minutes after this train reaches
    public final static int MAX_TRAINS = 15;
    private static final String TAG = "DBHelper: ";
    public static double CLOSE_LATLONG = .01;
    public static int BUS_SPEED = 60 / 20; // Min / Speed in km. per hour
    public static String _ID = "_id";
    public static String COL_AREA = "area";
    public static String COL_STOPNAME = "stopname";
    public static String COL_LASTSTOP = "laststop";
    public static String COL_BUSNO = "busno";
    public static String
            COLUMN_CALLHOME = "\n _id, date, clientms, email, imei, module, lat, lon, accuracy, locationtime, " +
            "\n provider, version, app, carrier, product, manufacturer";
    private static long mCallHomeLastRowId = -1;
    //The Android's default system path of your application database.
    //private static String DB_PATH; // = "/data/data/org.mesn.com.bestbus/databases/";
    private static String DB_NAME = "ssb.jet";
    private final Context mContext;
    public String msConnTime1;
    private boolean mCallHomeToBeCleared = false;
    private SQLiteQueryBuilder sbAllQueries;
    private SQLiteDatabase mBusDB;
    private Activity mActivity;
    private PackageInfo mVersionInfo;
    private CGlobals_BA mApp;
    private double MAX_STRAY = 0.01; // connect 1 km outside bounding rect

    public DBHelperBus(Context context) {
        super(context, DB_NAME, null, 6);
        this.mContext = context;
        setForcedUpgrade();
        //DB_PATH = context.getApplicationInfo().dataDir + "/";
        sbAllQueries = new SQLiteQueryBuilder();

    }

    /*private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        final String dbVersion = mVersionInfo.versionName + mVersionInfo.versionCode;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String currentDbVersion = prefs.getString("dbVersionInfo", "-1");
        if (!currentDbVersion.equals(dbVersion)) {
            return false;
        }
        try {
            String myPath = *//*DB_PATH +*//* DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        } catch (SQLiteException e) {
            Log.d(TAG + " checkDB - ", e.getMessage());
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }*/

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
   /* private void copyDataBase() throws IOException {
        //Open your local db as the input stream
        InputStream myInput = mContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = *//*DB_PATH +*//* DB_NAME;

        *//*File f = new File(DB_PATH);
        if (!f.exists()) {
            f.mkdir();
        }*//*
        //Open the empty db as the output stream
        OutputStream myOutput = null;
        try {
            myOutput = new FileOutputStream(outFileName);
        } catch (FileNotFoundException e) {
            SSLog_SS.e("DBHelper.copyDataBase): ", e.getMessage());
            throw (e);

        }
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
        // Update the db version on phone
    }*/
    public void openDataBase(PackageInfo versionInfo, Activity activity, CGlobals_BA app) throws SQLException {
        mActivity = activity;
        mVersionInfo = versionInfo;
        mApp = app;
        //boolean dbExist = checkDataBase();
        String myPath = /*DB_PATH +*/ DB_NAME;

    /*    if (dbExist) {
            //do nothing - database already exists
        } else {*/
        // if the database does not exist, copy it from the assets folder
        try {
            if (mBusDB == null)
                mBusDB = getReadableDatabase();
        } catch (Exception e) {
            throw new Error("Error copying database: " + e.getMessage());
        }
//			mApp.mCH.userPing(mActivity.getString(R.string.atDBCreate), "");
        // }
        //Open the database
        if (mBusDB == null)
            mBusDB = getReadableDatabase();

    }

    @Override
    public synchronized void close() {
        if (mBusDB != null)
            mBusDB.close();
        super.close();
    }

    /*@Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG + " OnCreate - ", "Database being created");
    }*/

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG + " onUpgrade - ", "Database being upgraded");
    }

    public String getBusesToStation(double dLat, double dLon,
                                    int iDow, double dTm) {
        String sBusesToStation = "";
        try {
            Station stn = getNearestStation(dLat, dLon);
            double dStationDistRad = 500;
            float[] res = new float[1];
            Location.distanceBetween(dLat, dLon,
                    stn.mdLat, stn.mdLon, res);
            double dStationDist = res[0];
            if (dStationDist <= dStationDistRad)
                return stn.msSearchStr + " station within walking distance (" + (int) dStationDist + " m)";


            double dStopDist = 1000;
            ArrayList<CStop> aStopsNearStation = getNearStops(stn.mdLat, stn.mdLon, dStationDistRad);
            ArrayList<CStop> aNearestStops = getNearStops(dLat, dLon, dStopDist);
/*			ArrayList<CBus> aBusesAtStation = getDirectBusesNear(dLat, dLon,
                    stn.mdLat, stn.mdLon,
					dow, tm, 25);*/

            StringBuilder sBuses = new StringBuilder("");
            StringBuilder sSearch = new StringBuilder("");
            String delim = "";
            int iNoBuses = 0;
            ArrayList<CBus> aBusesToStation;
            String sNearestStop = "";
            for (CStop ns : aNearestStops) {
                if (ns.mdDist > dStopDist)
                    break;
                for (CStop ss : aStopsNearStation) {
                    if (ss.mdDist > dStationDistRad)
                        break;
                    sNearestStop = ns.msStopNameDetail;
                    aBusesToStation = getDirectBuses(ns.miStopId, ss.miStopId, iDow, dTm);
                    for (CBus b : aBusesToStation) {
                        if (sSearch.indexOf(" " + b.msBusLabel + " ") == -1) {
                            iNoBuses++;
                            sSearch.append(" " + b.msBusLabel + " ");
                            sBuses.append(delim);
                            sBuses.append(b.msBusNo + " (" + b.msDirection + ")");
                            delim = ", ";
                        }
                    }

                }

                if (iNoBuses >= 1) {
                    if (mApp.moStartStop.miStopId != ns.miStopId)
                        sBuses.append(" (From: " + sNearestStop + ") ");
                    return "To: " + stn.msSearchStr + " - " + sBuses.toString();
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, e.getMessage());
        }
        return sBusesToStation;
    }

    public Station getNearestStation(Double dLat, Double dLon) {
        Station si;
        ArrayList<Station> asi = new ArrayList<Station>();
        asi = getNearStations(dLat, dLon);
        if (asi != null && asi.size() > 0) {
            si = asi.get(0);
            if (si != null)
                return si;
        }
        return null;
    }

    // Get the stops within MINWALKINGDIST from oStop
    ArrayList<Station> getNearStations(Double dLat, Double dLon) {
        if (dLat <= 0 || dLon <= 0)
            return null;
        ArrayList<Station> asi = new ArrayList<Station>();
        float[] res = new float[1];
        Cursor curs;
        Station si = null;
        String rsql = "SELECT s.station_id station_id, stnabbr, stationname, lat, lon, " +
                "\n stationname || ' (' || GROUP_CONCAT(sl.linecode) || ')'  searchstr " +
                "\n FROM ta_station s INNER JOIN ta_stationline sl " +
                "\n ON s.station_id = sl.station_id ";
        rsql = rsql + "\n WHERE ABS(lat -" + Double.toString(dLat)
                + " ) <= " + CGlobals_BA.MINLATLONDIFFSTATION + " AND ABS(lon - " + Double.toString(dLon)
                + " ) <= " + CGlobals_BA.MINLATLONDIFFSTATION;
        rsql = rsql + " \n GROUP BY s.station_id ORDER BY UPPER(stationname)";
        curs = doQuery(rsql, null);
        if (curs != null) {
            for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                si = setStationInfoFromCursor(curs);
                if (si != null) {
                    Location.distanceBetween(dLat, dLon,
                            si.mdLat, si.mdLon, res);
                    si.dDist = (int) res[0];
                    if (si.dDist <= CGlobals_BA.MINWALKINGDIST)
                        asi.add(si);
                }
            }
        }

        Collections.sort(asi, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                Station s1 = (Station) o1;
                Station s2 = (Station) o2;
                return (int) (s1.dDist - s2.dDist);
            }
        });
        closeCursor(curs, "getNearStations - ");
        return asi;
    } // getNearStations

    // For populating the start and destination stations
    public ArrayList<String> getStationNames() {
        Cursor curs = null;
        ArrayList<String> aStationNames = new ArrayList<String>();
        String sStationName;
        String rsql = "SELECT stationname || ' (' || GROUP_CONCAT(sl.linecode) || ')' AS searchstr " +
                " FROM ta_station s INNER JOIN ta_stationline sl " +
                " ON s.station_id = sl.station_id " +
                " GROUP BY s.station_id ORDER BY UPPER(stationname)";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    sStationName = curs.getString(curs.getColumnIndexOrThrow("searchstr"));
                    if (sStationName != null)
                        aStationNames.add(sStationName);
                }
            }
        } catch (Exception SQLException) {
            SSLog_SS.e(TAG + " getStationNames - ", SQLException.getMessage());
        }
        closeCursor(curs, " getStationNames - ");
        return aStationNames;
    } // getStationNames

    public ArrayList<CStation> getStations() {
        Cursor curs = null;
        ArrayList<CStation> aStations = new ArrayList<CStation>();
        String sStationName;
        String rsql = "SELECT s.station_id station_id, stationname || ' (' || GROUP_CONCAT(sl.linecode) || ')' AS searchstr " +
                " FROM ta_station s INNER JOIN ta_stationline sl " +
                " ON s.station_id = sl.station_id " +
                " GROUP BY s.station_id ORDER BY UPPER(stationname)";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    sStationName = curs.getString(curs.getColumnIndexOrThrow("searchstr"));
                    if (sStationName != null)
                        aStations.add(new CStation(curs.getInt(curs.getColumnIndexOrThrow("station_id")),
                                sStationName));

                }
            }
        } catch (Exception SQLException) {
            SSLog_SS.e(TAG + " getStations - ", SQLException.getMessage());

        }
        closeCursor(curs, " getStations - ");
        return aStations;
    } // getStations

    private void closeCursor(Cursor curs, String errmsg) {
        try {
            if (curs != null)
                curs.close();
            curs = null;
        } catch (Exception e) {
            SSLog_SS.e(TAG + errmsg, e.getMessage());
        }
    }

    Station setStationInfoFromCursor(Cursor curs) {
        int iId = curs.getInt(curs.getColumnIndexOrThrow("station_id"));
        String sStationName = curs.getString(curs.getColumnIndexOrThrow("stationname"));
        String sSearchStr = curs.getString(curs.getColumnIndexOrThrow("searchstr"));
        Double dLat = curs.getDouble(curs.getColumnIndexOrThrow("lat"));
        Double dLon = curs.getDouble(curs.getColumnIndexOrThrow("lon"));
        Station si = null;
        si = new Station(iId, sStationName, sSearchStr, dLat, dLon);
        si.msStationAbbr = curs.getString(curs.getColumnIndexOrThrow("stnabbr"));
        return si;
    }

    public ArrayList<CBusJourney> getBusJourney(String iRouteCode, String sBusDirection,
                                                int iDow, double dTm) {
        ArrayList<CBusJourney> aBusJourney = new ArrayList<CBusJourney>();
        Cursor curs;
        CBusJourney cb;
        String rsql = " SELECT stopserial, rd.stopcode stopcode, " +
                " stopdisplayname, " +
                "IFNULL((SELECT landmarklist FROM ba_stopunique su WHERE su.stopcode = s.stopcode), '') landmarklist," +
                "\n rd.routecode routecode,  " +
                " CASE WHEN rd.direction = 'U' THEN s.latu ELSE s.latd END lat, " +
                " CASE WHEN rd.direction = 'U' THEN s.lonu ELSE s.lond END lon" +
                "\n FROM ba_routedetail rd INNER JOIN ba_stopmaster s ON s.stopcode = rd.stopcode  " +
                "\n WHERE rd.routecode = '" + iRouteCode + "'" +
                "\n AND direction = '" + sBusDirection + "'" +
                "\n ORDER BY stopserial ";

        curs = doQuery(rsql, null);
        try {
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    cb = new CBusJourney(curs.getString(curs.getColumnIndexOrThrow("routecode")));
                    cb.mdStopSerial = curs.getDouble(curs.getColumnIndexOrThrow("stopserial"));
                    cb.msStopnameDetail = curs.getString(curs.getColumnIndexOrThrow("stopdisplayname"));
                    cb.msLandmarkList = curs.getString(curs.getColumnIndexOrThrow("landmarklist"));
                    cb.mdLat = curs.getDouble(curs.getColumnIndexOrThrow("lat"));
                    cb.mdLon = curs.getDouble(curs.getColumnIndexOrThrow("lon"));
                    cb.mistopCode = curs.getInt(curs.getColumnIndexOrThrow("stopcode"));
                    aBusJourney.add(cb);
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + " getBusJourney - ", e.getMessage());
        }
        closeCursor(curs, " getBusJourney - ");
        return aBusJourney;
    } // getBusJourney

    public void writeDBCallHome(String smodule, String slat, String slon,
                                String sacc, String sloctime, String sprovider) {
        Calendar cal = Calendar.getInstance();
        String sLastClientms = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        try {
            ContentValues rowValues = new ContentValues();
            rowValues.put("date", df.format(cal.getTime()));
            rowValues.put("clientms", Long.toString(cal.getTimeInMillis()));
            rowValues.put("email", CGlobals_BA.msGmail);
            rowValues.put("imei", CGlobals_BA.mIMEI);
            rowValues.put("module", smodule);
            rowValues.put("lat", slat);
            rowValues.put("lon", slon);
            rowValues.put("accuracy", sacc);
            rowValues.put("locationtime", sloctime);
            rowValues.put("provider", sprovider);
            rowValues.put("version", mApp.mPackageInfo.versionCode);
            rowValues.put("app", mApp.mAppNameCode);
            rowValues.put("carrier", mApp.msCarrier);
            rowValues.put("product", mApp.msProduct);
            rowValues.put("manufacturer", mApp.msManufacturer);
            try {
                String clientMs = Long.toString(cal.getTimeInMillis());
                if (!clientMs.equals(sLastClientms)) {
                    sLastClientms = Long.toString(cal.getTimeInMillis());
                    mCallHomeLastRowId = mBusDB.insert("callhome", null, rowValues);

                }
            } catch (SQLException e) {
                SSLog_SS.e("DBHelper: writeDBCallHome", e.getMessage());
            }
            SSLog_SS.i("Callhome: inserted", Long.toString(mCallHomeLastRowId));
//			Cursor cursor = getDBCallHome();
        } catch (Exception e) {
            SSLog_SS.e("DBHelper: writeDBCallHome", e.getMessage());
        }

    } // writeDBCallHome

    public Cursor getDBCallHome() {
        Cursor curs = null;
        String rsql = "SELECT " + COLUMN_CALLHOME + " FROM callhome ";
        if (mCallHomeToBeCleared)
            rsql += " WHERE _id > " + mCallHomeLastRowId;
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();
                return curs;
            } else {
                return null;
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + " getDBCallHome - ", e.getMessage());
            closeCursor(curs, " getDBCallHome - ");
            return null;
        }

    }    // writeDBCallHome

    public int DBClearCallHome() {
        try {
            mCallHomeToBeCleared = true;
            int nRowsDeleted = mBusDB.delete("callhome", "1", null);
            mCallHomeToBeCleared = false;
            SSLog_SS.i(TAG + " DBClearHome() - Deleted: ", Integer.toString(nRowsDeleted));
        } catch (Exception e) {
            SSLog_SS.e(TAG + " DBClearHome - ", e.getMessage());
            return 0;
        }
        return 1;
    }

    public Station getStationFromId(int iStationId) {
        Cursor curs = null;
        Station si = null;
        String rsql = "SELECT s.station_id station_id, stnabbr, stationname, lat, lon, \n stationname || ' (' || GROUP_CONCAT(sl.linecode) || ')'  searchstr " +
                "\n FROM ta_station s INNER JOIN ta_stationline sl " +
                "\n ON s.station_id = sl.station_id ";
        rsql = rsql + "\n WHERE s.station_id = '" + iStationId + "'";
        rsql = rsql + "\n GROUP BY s.station_id ORDER BY UPPER(stationname)";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();

                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    si = setStationInfoFromCursor(curs);
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + " getStationFromId - ", e.getMessage());
        }
        closeCursor(curs, " getStationFromId - ");
        return si;
    } // getStationFromId

    void createIndexes() {
//		mTrainDB.execSQL("DROP INDEX IF EXISTS ta_schedule_station_id;");
//		mTrainDB.execSQL("CREATE INDEX ta_schedule_station_id ON ta_schedule (station_id);");

    }

    public ArrayList<CStop> getAllStops() {
        Cursor curs = null;
        ArrayList<CStop> aoStop = new ArrayList<CStop>();
        CStop si;
        String rsql = "SELECT stop_id, stopnamedetail, stopcode, \nstopnamedetaillandmarklistid, landmarklist " +
                "\n FROM ba_stopunique s " +
                " ORDER BY noofbuses DESC";

        try {
            curs = doQuery(rsql, null);
            curs.moveToFirst();
            if (curs != null) {
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    si = new CStop();
                    si.msStopNameDetail = curs.getString(curs.getColumnIndexOrThrow("stopnamedetail"));
                    si.msLandmarkList = curs.getString(curs.getColumnIndexOrThrow("landmarklist"));
                    si.msStopNameDetailLandmarklistId = curs.getString(curs.getColumnIndexOrThrow("stopnamedetaillandmarklistid"));
                    si.miStopId = curs.getInt(curs.getColumnIndexOrThrow("stop_id"));
                    si.miStopCode = curs.getInt(curs.getColumnIndexOrThrow("stopcode"));
                    aoStop.add(si);
                }
            }
        } catch (Exception SQLException) {
            SSLog_SS.e(TAG + " getStopNames - ", SQLException.getMessage());
        }
        closeCursor(curs, " getStopNames - ");

        return aoStop;
    } // getAllStops

    public ArrayList<String> getStopNames() {
        Cursor curs = null;
        ArrayList<String> aStopNames = new ArrayList<String>();
        String sStopName;
        String rsql = "SELECT stopnamedetail " +
                " FROM ba_stopunique s " +
                " ORDER BY noofbuses DESC, stopnamedetail ";
        try {
            curs = doQuery(rsql, null);
            curs.moveToFirst();
            if (curs != null) {
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    sStopName = curs.getString(curs.getColumnIndexOrThrow("stopnamedetail"));
                    if (sStopName != null)
                        aStopNames.add(sStopName);

                }
            }
        } catch (Exception SQLException) {
            SSLog_SS.e(TAG + " getStopNames - ", SQLException.getMessage());
        }
        closeCursor(curs, " getStopNames - ");

        return aStopNames;
    } // getStopNames

    public Cursor getStopNamesCursor(String filter) {
        Cursor curs = null;
        filter = filter.trim();
        filter = filter.replaceAll("[^a-zA-Z0-9]+", "");
        String searchstr1 = " WHERE s LIKE  '" + filter + "%' ";
        String searchstr2 = " WHERE searchstr LIKE  '%" + filter + "%' AND searchstr NOT LIKE '" + filter + "%' ";
        String orderBy = " ORDER BY ord";
        String sql = "\n stop_id _id, \nstopnamedetail AS searchstr " +
                "\n FROM ba_stopunique s ";
        String rsql = "SELECT 1 as ord, " + sql + searchstr1 +
                " UNION SELECT 2 as ord, " + sql + searchstr2 + orderBy;
        if (TextUtils.isEmpty(filter)) {
            rsql = " SELECT " + sql + " ORDER BY noofbuses DESC, UPPER(stopnamedetail)";
        } else {
            rsql = " SELECT " + sql + " WHERE stopnamedetailid LIKE '%" + filter + "%'" +
                    " ORDER BY noofbuses DESC, UPPER(stopnamedetailid)";
        }
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();
            }
        } catch (Exception e) {
            SSLog_SS.e("DBHelper getStopNamesCursor: ", e.getMessage());
        }
        if (curs != null)
            return curs;
        else
            return new MatrixCursor(new String[]{"_id"}); // never return null
    } // getStopNamesCursor

    public ArrayList<CBus> getAllBuses() {
        Cursor curs = null;
        CBus si;
        ArrayList<CBus> aoBus = new ArrayList<CBus>();

        String sql = "SELECT busno, buslabel, bustype, routecode, fstopname, lstopname" +
                "\n FROM ba_route " +
                "\n WHERE direction = '" + CGlobals_BA.DIRECTIONCODEUP + "'" +
                "\n ORDER BY busno, bustype ";
        try {
            curs = doQuery(sql, null);
            if (curs != null) {
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    si = new CBus(curs.getInt(curs.getColumnIndexOrThrow("busno")),
                            curs.getInt(curs.getColumnIndexOrThrow("bustype")));
                    si.msFirstStop = curs.getString(curs.getColumnIndexOrThrow("fstopname"));
                    si.msLastStop = curs.getString(curs.getColumnIndexOrThrow("lstopname"));
                    si.msRouteCode = curs.getString(curs.getColumnIndexOrThrow("routecode"));
                    si.msBusLabel = curs.getString(curs.getColumnIndexOrThrow("buslabel"));
                    aoBus.add(si);


                }
            }
        } catch (Exception e) {
            SSLog_SS.e("DBHelper getBusNosCursor: ", e.getMessage());
        }
        return aoBus;
    }

    public Cursor getBusNosCursor(String filter) {
        Cursor curs = null;
        filter = filter.trim();
        String sql = "SELECT route_id _id, buslabel busno FROM ba_route " +
                " WHERE buslabel LIKE '%" + filter + "%'" +
                "\n GROUP BY busno, bustype " +
                "\n ORDER BY busno, bustype DESC ";
        try {
            curs = doQuery(sql, null);
            if (curs != null) {
                curs.moveToFirst();
            }
        } catch (Exception e) {
            SSLog_SS.e("DBHelper getBusNosCursor: ", e.getMessage());
        }
        if (curs != null)
            return curs;
        else
            return new MatrixCursor(new String[]{"_id"}); // never return null
    } // getStopNamesCursor

    public CStop getNearestStop(Double dLat, Double dLon) {
        CStop si;
        ArrayList<CStop> asi = new ArrayList<CStop>();
        asi = getNearStops(dLat, dLon, -1);
        if (asi != null && asi.size() > 0) {
            si = asi.get(0);
            if (si != null) {
                si = getStopFromId(si.miStopId);
                return si;
            }
        }
        return null;
    }

    // Get the stops within MINWALKINGDIST from oStop
    public ArrayList<CStop> getNearStops(Double dLat, Double dLon, double dRadialDist) {
        ArrayList<CStop> asi = new ArrayList<CStop>();
        if (dLat <= 0 || dLon <= 0)
            return asi;
        double dMinLatLonDiff = CGlobals_BA.MINLATLONDIFF;
        if (dRadialDist < 0)
            dRadialDist = CGlobals_BA.DEFAULT_RADIAL_DIST;

        dMinLatLonDiff = dRadialDist / 1000 / 111; // 1 deg = 111 km.
        float[] res = new float[1];

        Cursor curs;
        CStop si = null;
        String rsql = "SELECT _id, stop_id, stopcode, " +
                "\n (SELECT stopnamedetail FROM ba_stopunique su WHERE su.stop_id = s.stop_id) stopnamedetail, " +
                " latu lat, lonu lon  " +
                "\n FROM ba_stopmaster s" +
                "\n WHERE ABS(latu -" + Double.toString(dLat) +
                " ) <= " + dMinLatLonDiff + " AND ABS(lonu - " + Double.toString(dLon) +
                " ) <= " + dMinLatLonDiff +
                "\n UNION SELECT _id, stop_id, stopcode, " +
                "\n (SELECT stopnamedetail FROM ba_stopunique su WHERE su.stop_id = s.stop_id) stopnamedetail, " +
                " latd lat, lond lon  " +
                "\n FROM ba_stopmaster s" +
                "\n WHERE ABS(latd -" + Double.toString(dLat) +
                " ) <= " + dMinLatLonDiff + " AND ABS(lond - " + Double.toString(dLon) +
                " ) <= " + dMinLatLonDiff;

        curs = doQuery(rsql, null);
        if (curs != null) {
            for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {

                si = new CStop(curs.getInt(curs.getColumnIndexOrThrow("_id")),
                        curs.getInt(curs.getColumnIndexOrThrow("stopcode")),
                        curs.getString(curs.getColumnIndexOrThrow("stopnamedetail")),
                        curs.getDouble(curs.getColumnIndexOrThrow("lat")),
                        curs.getDouble(curs.getColumnIndexOrThrow("lon")));


                if (si != null) {
                    si.miStopId = curs.getInt(curs.getColumnIndexOrThrow("stop_id"));
                    Location.distanceBetween(dLat, dLon,
                            si.lat, si.lon, res);
                    si.mdDist = (int) res[0];
                    if (si.mdDist <= dRadialDist)
                        asi.add(si);

                }
            }
        }
        Collections.sort(asi, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                CStop s1 = (CStop) o1;
                CStop s2 = (CStop) o2;
                return (int) (s1.mdDist - s2.mdDist);
            }
        });
        closeCursor(curs, " getNearStops - ");
        return asi;
    } // getNearStations

    // Get the stops within MINWALKINGDIST from oStop
    public ArrayList<CStop> getNearStopsArray(Double dLat, Double dLon, double dRadialDist) {
        ArrayList<CStop> asi = new ArrayList<CStop>();
        if (dLat <= 0 || dLon <= 0)
            return asi;
        double dMinLatLonDiff = CGlobals_BA.MINLATLONDIFF;
        if (dRadialDist < 0)
            dRadialDist = CGlobals_BA.DEFAULT_RADIAL_DIST;

        dMinLatLonDiff = dRadialDist / 1000 / 111; // 1 deg = 111 km.
        float[] res = new float[1];

        Cursor curs;
        CStop si = null;
        String rsql = "SELECT _id, stop_id, stopcode, " +
                "\n (SELECT stopnamedetail FROM ba_stopunique su WHERE su.stop_id = s.stop_id) stopnamedetail, " +
                "\n (SELECT stopnamedetaillandmarklistid FROM ba_stopunique su WHERE su.stop_id = s.stop_id) stopnamedetaillandmarklistid, " +
                "IFNULL((SELECT landmarklist FROM ba_stopunique su WHERE su.stopcode = s.stopcode), '') landmarklist," +

                " latu lat, lonu lon  " +
                "\n FROM ba_stopmaster s" +
                "\n WHERE ABS(latu -" + Double.toString(dLat) +
                " ) <= " + dMinLatLonDiff + " AND ABS(lonu - " + Double.toString(dLon) +
                " ) <= " + dMinLatLonDiff +
                " GROUP BY stopnamedetail";
        curs = doQuery(rsql, null);
        if (curs != null) {
            for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {

                si = new CStop(curs.getInt(curs.getColumnIndexOrThrow("_id")),
                        curs.getInt(curs.getColumnIndexOrThrow("stopcode")),
                        curs.getString(curs.getColumnIndexOrThrow("stopnamedetail")),
                        curs.getDouble(curs.getColumnIndexOrThrow("lat")),
                        curs.getDouble(curs.getColumnIndexOrThrow("lon")));
                if (si != null) {
                    si.miStopColor = CStop.NEAR_STOP_COLOR;
                    si.miStopId = curs.getInt(curs.getColumnIndexOrThrow("stop_id"));
                    si.miStopCode = curs.getInt(curs.getColumnIndexOrThrow("stopcode"));
                    si.msLandmarkList = curs.getString(curs.getColumnIndexOrThrow("landmarklist"));
                    si.msStopNameDetailLandmarklistId = curs.getString(curs.getColumnIndexOrThrow("stopnamedetaillandmarklistid"));
                    Location.distanceBetween(dLat, dLon,
                            si.lat, si.lon, res);
                    si.mdDist = (int) res[0];
                    if (si.mdDist <= dRadialDist)
                        asi.add(si);

                }
            }
        }
        Collections.sort(asi, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                CStop s1 = (CStop) o1;
                CStop s2 = (CStop) o2;
                return (int) (s1.mdDist - s2.mdDist);
            }
        });
        closeCursor(curs, " getNearStops - ");
        return asi;
    } // getArrayNearStops

    //  getStopFromSearchStr
    public CStop getStopFromSearchStr(int iStop_Code) {
        Cursor curs = null;
        CStop si = null;
        String rsql = "SELECT _id, s.stop_id stop_id, s.stopcode stopcode, " +
                "IFNULL((SELECT landmarklist FROM ba_stopunique su WHERE su.stopcode = s.stopcode), '') landmarklist," +
                "su.stopnamedetail stopnamedetail, su.stopnamedetaillandmarklistid stopnamedetaillandmarklistid, s.lat, s.lon  " +
                "\n FROM ba_stopmaster s INNER JOIN ba_stopunique su ON su.stopcode = s.stopcode " +
                "\n AND s.stopcode = " + iStop_Code + "";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();
                while (curs.isAfterLast() == false) {
///				for(curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    si = new CStop(curs.getInt(curs.getColumnIndexOrThrow("_id")),
                            curs.getInt(curs.getColumnIndexOrThrow("stopcode")),
                            curs.getString(curs.getColumnIndexOrThrow("stopnamedetail")),
                            curs.getDouble(curs.getColumnIndexOrThrow("lat")),
                            curs.getDouble(curs.getColumnIndexOrThrow("lon")));
                    si.miStopId = curs.getInt(curs.getColumnIndexOrThrow("stop_id"));
                    si.msLandmarkList = curs.getString(curs.getColumnIndexOrThrow("landmarklist"));
                    si.msStopNameDetailLandmarklistId = curs.getString(curs.getColumnIndexOrThrow("stopnamedetaillandmarklistid"));
                    Location location = mApp.getMyLocation(mContext);
                    float[] res = new float[1];
                    if (location != null) {
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                si.lat, si.lon, res);
                        si.mdDist = res[0];
                    }

                    break;
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + "DBHelper - getStopFromSearchStr - ", e.getMessage());
        }
        closeCursor(curs, " getStopFromSearchStr - ");
        return si;
    }

    //  getStopFromSearchStr without stop id
    public CStop getStopFromSearchStr(String searchstr) {
        Cursor curs = null;
        CStop si = null;
        String rsql = "SELECT _id, s.stop_id stop_id, s.stopcode stopcode, " +
                "IFNULL((SELECT landmarklist FROM ba_stopunique su WHERE su.stopcode = s.stopcode), '') landmarklist," +
                "su.stopnamedetail stopnamedetail, su.stopnamedetaillandmarklistid stopnamedetaillandmarklistid, s.lat, s.lon  " +
                "\n FROM ba_stopmaster s INNER JOIN ba_stopunique su ON su.stopcode = s.stopcode " +
                "\n AND stopnamedetail LIKE '%" + searchstr.replace("'", "''") + "%' ";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();
                while (curs.isAfterLast() == false) {
///				for(curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    si = new CStop(curs.getInt(curs.getColumnIndexOrThrow("_id")),
                            curs.getInt(curs.getColumnIndexOrThrow("stopcode")),
                            curs.getString(curs.getColumnIndexOrThrow("stopnamedetail")),
                            curs.getDouble(curs.getColumnIndexOrThrow("lat")),
                            curs.getDouble(curs.getColumnIndexOrThrow("lon")));
                    si.miStopId = curs.getInt(curs.getColumnIndexOrThrow("stop_id"));
                    si.msLandmarkList = curs.getString(curs.getColumnIndexOrThrow("landmarklist"));
                    si.msStopNameDetailLandmarklistId = curs.getString(curs.getColumnIndexOrThrow("stopnamedetaillandmarklistid"));
                    Location location = mApp.getMyLocation(mContext);
                    float[] res = new float[1];
                    if (location != null) {
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                si.lat, si.lon, res);
                        si.mdDist = res[0];
                    }

                    break;
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + "DBHelper - getStopFromSearchStr - ", e.getMessage());
        }
        closeCursor(curs, " getStopFromSearchStr - ");
        return si;
    }

    //  getBusesAtStop
    public ArrayList<CBus> getBusesNearYou(int iStopId, int iDow, double tm) {
        Cursor curs = null;
        ArrayList<CBus> aoBus = new ArrayList<CBus>();
        CStop stop = getStopFromId(iStopId);
        CBus si = null;
        String rsql = "SELECT r.busno busno, r.buslabel buslabel, s.stopcode stopcode, bustype, latu, lonu, latd, lond, rd.direction, r.routecode routecode, stopserial, fstopname, lstopname, " +
                "\n(SELECT SUM(distance) FROM ba_routedetail rd2 " +
                "\n WHERE rd2.direction = 'U' AND rd2.routecode = rd.routecode AND rd2.stopserial < " +
                "\n (SELECT stopserial FROM ba_routedetail rdt " +
                "\n WHERE stopcode = s.stopcode AND rdt.direction='U'" +
                "\n AND rdt.routecode = rd.routecode)) distup," +
                "\n(SELECT SUM(distance) FROM ba_routedetail rd2 " +
                "\n WHERE rd2.direction = 'D' AND rd2.routecode = rd.routecode AND rd2.stopserial < " +
                "\n (SELECT stopserial FROM ba_routedetail rdt " +
                "\n WHERE stopcode = s.stopcode AND rdt.direction='D'" +
                "\n AND rdt.routecode = rd.routecode)) distdn," +
                "\n CASE WHEN r.direction='U' THEN 1 ELSE 2 END ord " +
                "\n FROM ba_stopmaster s" +
                "\n INNER JOIN ba_routedetail rd ON rd.stopcode = s.stopcode " +
                "\n INNER JOIN ba_route r ON r.routecode = rd.routecode AND r.direction = rd.direction" +
                "\n WHERE rd.direction = 'U' AND s.stop_id = " + iStopId +
                "\n GROUP BY busno";

        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    si = new CBus(curs.getInt(curs.getColumnIndexOrThrow("busno")),
                            curs.getInt(curs.getColumnIndexOrThrow("bustype")));
                    si.miStopSerial = curs.getInt(curs.getColumnIndexOrThrow("stopserial"));
                    si.msStopName = stop.msStopNameDetail;
                    si.stopLat = stop.lat;
                    si.stopLon = stop.lon;
                    si.mdLatu = curs.getDouble(curs.getColumnIndexOrThrow("latu"));
                    si.mdLatd = curs.getDouble(curs.getColumnIndexOrThrow("latd"));
                    si.mdLonu = curs.getDouble(curs.getColumnIndexOrThrow("lonu"));
                    si.mdLond = curs.getDouble(curs.getColumnIndexOrThrow("lond"));
                    si.mdDistUp = curs.getDouble(curs.getColumnIndexOrThrow("distup"));
                    si.mdDistDn = curs.getDouble(curs.getColumnIndexOrThrow("distdn"));
                    si.msFirstStop = curs.getString(curs.getColumnIndexOrThrow("fstopname"));
                    si.msLastStop = curs.getString(curs.getColumnIndexOrThrow("lstopname"));
//					si.msFrequency = curs.getString(curs.getColumnIndexOrThrow("frequency"));
                    si.msRouteCode = curs.getString(curs.getColumnIndexOrThrow("routecode"));
                    si.msBusLabel = curs.getString(curs.getColumnIndexOrThrow("buslabel"));
/*					si.moStop = new CStop(curs.getInt(curs.getColumnIndexOrThrow("stopcode")),
                            si.mdLatu, si.mdLonu, si.mdLatd, si.mdLond);
					);
*/

                    aoBus.add(si);
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + " getBusesAtStop: ", e.getMessage());
        }
        closeCursor(curs, " getBusesAtStop - ");
        return aoBus;
    } // getBusesAtStop

    public CBus getFrequency(CBus si, int iDow) {
        switch (si.miBusType) {
            case 7:
            case 8:
            case 9:
                si = getFrequencyAc(si, iDow);
                return si;
            default:
                break;
        }
        Cursor curs = null;
        int minsToStop;
        Calendar now = Calendar.getInstance(Locale.getDefault());
        Calendar tmp = Calendar.getInstance(Locale.getDefault());
        Calendar nxt = Calendar.getInstance(Locale.getDefault());

        minsToStop = (int) (si.mdDistUp * BUS_SPEED + 1);
        tmp.add(Calendar.MINUTE, (int) (-minsToStop + 1));
        Calendar calSTime = tmp;
        double dStartTime = calSTime.get(Calendar.HOUR_OF_DAY) + calSTime.get(Calendar.MINUTE) / 100.0;
        String rsql = "SELECT frequency_id, firstfromhh, firstfrommm,  startheadwayhh, startheadwaymm, " +
                "\n CASE WHEN " + Double.toString(dStartTime) +
                " BETWEEN startheadwayhh + startheadwaymm/100.0  AND endheadwayhh + endheadwaymm /100.0 THEN frequency ELSE -1 END frequency " +
                "\n FROM vw_frequency f " +
                "\n WHERE " + si.miStopSerial + " BETWEEN stopserialfrom AND stopserialto " +
                "\n AND " + iDow + " BETWEEN startdayweek AND enddayweek" +
//			"\n AND " + Double.toString(dStartTime) + " BETWEEN " +
//			"startheadwayhh + startheadwaymm / 100.0 AND endheadwayhh + endheadwaymm / 100.0 " +
                "\n AND routecode = '" + si.msRouteCode + "'";
        int min = 0;
        boolean isLast;
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    int startHeadwayHH = curs.getInt(curs.getColumnIndexOrThrow("startheadwayhh"));
                    int startHeadwayMM = curs.getInt(curs.getColumnIndexOrThrow("startheadwaymm"));
                    int firstFromHH = curs.getInt(curs.getColumnIndexOrThrow("firstfromhh"));
                    int firstFromMM = curs.getInt(curs.getColumnIndexOrThrow("firstfrommm"));
                    int frequency = curs.getInt(curs.getColumnIndexOrThrow("frequency"));
                    isLast = false;
                    if (frequency != -1) {
                        int startMin = (calSTime.get(Calendar.HOUR_OF_DAY) * 60) + calSTime.get(Calendar.MINUTE);
                        min = startHeadwayHH * 60 + startHeadwayMM + frequency;
                        int nxtBus = ((startMin - min) / frequency) + 1;
                        nxtBus = nxtBus * frequency;
                        min = min + nxtBus + minsToStop;

//						int minPrv = min - frequency;
//						int minNext = min + frequency;
//						double busMin = (int)(min / 60) + (min % 60) / 100.0;
                        si.msFrequencyUp = Integer.toString(frequency);
                        si.msEtaUp = String.format("%.2f",
                                (int) (min / 60) + (min % 60) / 100.0);

						/*						si.msEtaUp = String.format("%.2f, %.2f, %.2f",
                                (int)(minPrv / 60) + (minPrv % 60) / 100.0,
								(int)(min / 60) + (min % 60) / 100.0,
								(int)(minNext / 60) + (minNext % 60) / 100.0);
*/
                        break;
                    } else {
                        if (!isLast) {
                            nxt.set(Calendar.HOUR_OF_DAY, (int) (firstFromHH));
                            nxt.set(Calendar.MINUTE, firstFromMM);
                            nxt.add(Calendar.MINUTE, minsToStop);
/*							si.msEtaUp = "Next: " +
                            String.format("%.2f", tmp.get(Calendar.HOUR_OF_DAY) + calSTime.get(Calendar.MINUTE) / 100.0);
							si.msFrequencyUp = " - ";
*/
                            isLast = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + " getFrequency: ", e.getMessage());
        }
        closeCursor(curs, " getFrequency - ");
        tmp = (Calendar) now.clone();
        minsToStop = (int) (si.mdDistDn * BUS_SPEED + 1);
        tmp.add(Calendar.MINUTE, (int) (-minsToStop + 1));
        calSTime = tmp;
        dStartTime = calSTime.get(Calendar.HOUR_OF_DAY) + calSTime.get(Calendar.MINUTE) / 100.0;
        rsql = "SELECT frequency_id, firsttohh, firsttomm, startheadwayhh, startheadwaymm, " +
                "\n CASE WHEN " + Double.toString(dStartTime) +
                " BETWEEN startheadwayhh + startheadwaymm/100.0  AND endheadwayhh + endheadwaymm /100.0 THEN frequency ELSE -1 END frequency " +
                "\n FROM vw_frequency2 f " +
                "\n WHERE " + si.miStopSerial + " BETWEEN stopserialfrom AND stopserialto " +
                "\n AND " + iDow + " BETWEEN startdayweek AND enddayweek" +
//			"\n AND " + Double.toString(dStartTime) + " BETWEEN " +
//			"startheadwayhh + startheadwaymm / 100.0 AND endheadwayhh + endheadwaymm / 100.0 " +
                "\n AND routecode = '" + si.msRouteCode + "'";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    int startHeadwayHH = curs.getInt(curs.getColumnIndexOrThrow("startheadwayhh"));
                    int startHeadwayMM = curs.getInt(curs.getColumnIndexOrThrow("startheadwaymm"));
                    int firstToHH = curs.getInt(curs.getColumnIndexOrThrow("firsttohh"));
                    int firstToMM = curs.getInt(curs.getColumnIndexOrThrow("firsttomm"));
                    int frequency = curs.getInt(curs.getColumnIndexOrThrow("frequency"));
                    if (frequency != -1) {
                        int startMin = calSTime.get(Calendar.HOUR_OF_DAY) * 60 + calSTime.get(Calendar.MINUTE);
                        min = startHeadwayHH * 60 + startHeadwayMM;
                        int nxtBus = ((startMin - min) / frequency) + 1;
                        nxtBus = nxtBus * frequency;
                        min = min + nxtBus + minsToStop;
//						double busMin = (int)(min / 60) + (min % 60) / 100.0;
                        si.msFrequencyDn = Integer.toString(frequency);
//						int minPrv = min - frequency;
//						int minNext = min + frequency;
                        si.msEtaDn = String.format("%.2f",
                                (int) (min / 60) + (min % 60) / 100.0);

/*						si.msEtaDn = String.format("%.2f, %.2f, %.2f",
                                (int)(minPrv / 60) + (minPrv % 60) / 100.0,
								(int)(min / 60) + (min % 60) / 100.0,
								(int)(minNext / 60) + (minNext % 60) / 100.0);
*/
                    } else {
                        nxt.set(Calendar.HOUR_OF_DAY, (int) (firstToHH));
                        nxt.set(Calendar.MINUTE, firstToMM);
                        nxt.add(Calendar.MINUTE, minsToStop);
/*						si.msEtaDn = "Next: " +
                        String.format("%.2f", tmp.get(Calendar.HOUR_OF_DAY) + calSTime.get(Calendar.MINUTE) / 100.0);
						si.msFrequencyDn = " - ";
						break;
*/
                    }
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + " getFrequency: ", e.getMessage());
        }
        closeCursor(curs, " getFrequency - ");


        return si;
    } // getFrequency

    private CBus getFrequencyAc(CBus si, int iDow) {
        Cursor curs = null;
        int minsToStop;
        Calendar now = Calendar.getInstance();
        Calendar tmp = (Calendar) now.clone();
        minsToStop = (int) (si.mdDistUp * BUS_SPEED + 1);
        tmp.add(Calendar.MINUTE, (int) (-minsToStop + 1));
        Calendar calSTime = tmp;
        double dStartTime = calSTime.get(Calendar.HOUR_OF_DAY) + calSTime.get(Calendar.MINUTE) / 100.0;
        String rsql = "SELECT hh, mm " +
                "\n FROM ba_frequencyac f " +
                "\n WHERE direction = 'U' AND routecode = '" + si.msRouteCode + "'" +
                "\n AND " + Double.toString(dStartTime) + " - .1 < " + "hh + mm / 100.0 " +
//			"\n AND " + si.miStopSerial + " BETWEEN stopserialfrom AND stopserialto " +
                "\n AND " + iDow + " BETWEEN startdayweek AND enddayweek" +
                "\n ORDER BY hh + mm /100.00 LIMIT 1";
        try {
            curs = doQuery(rsql, null);
            StringBuilder sTimes = new StringBuilder("");
            String delim = "";
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    int hh = curs.getInt(curs.getColumnIndexOrThrow("hh"));
                    int mm = curs.getInt(curs.getColumnIndexOrThrow("mm"));
                    tmp.set(Calendar.HOUR_OF_DAY, (int) (hh));
                    tmp.set(Calendar.MINUTE, mm);
                    tmp.add(Calendar.MINUTE, minsToStop);
                    sTimes.append(delim + String.format("%.2f",
                            tmp.get(Calendar.HOUR_OF_DAY) +
                                    calSTime.get(Calendar.MINUTE) / 100.0));
                    delim = ", ";
                }
                si.msAcTimeUp = sTimes.toString();
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + " getFrequencyAc: ", e.getMessage());
        }
        closeCursor(curs, " getFrequencyAc - ");

        minsToStop = (int) (si.mdDistDn * BUS_SPEED + 1);
        tmp = (Calendar) now.clone();
        tmp.add(Calendar.MINUTE, (int) (-minsToStop + 1));
        calSTime = tmp;
        dStartTime = calSTime.get(Calendar.HOUR_OF_DAY) + calSTime.get(Calendar.MINUTE) / 100.0;
        rsql = "SELECT hh, mm " +
                "\n FROM ba_frequencyac f " +
                "\n WHERE direction = 'D' AND routecode = '" + si.msRouteCode + "'" +
                "\n AND " + Double.toString(dStartTime) + " - .1 < " + "hh + mm / 100.0 " +
//			"\n AND " + si.miStopSerial + " BETWEEN stopserialfrom AND stopserialto " +
                "\n AND " + iDow + " BETWEEN startdayweek AND enddayweek" +
                "\n ORDER BY hh + mm /100.00 LIMIT 1";
        try {
            curs = doQuery(rsql, null);
            StringBuilder sTimes = new StringBuilder("");
            String delim = "";
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    int hh = curs.getInt(curs.getColumnIndexOrThrow("hh"));
                    int mm = curs.getInt(curs.getColumnIndexOrThrow("mm"));
                    tmp.set(Calendar.HOUR_OF_DAY, (int) (hh));
                    tmp.set(Calendar.MINUTE, mm);
                    tmp.add(Calendar.MINUTE, minsToStop);
                    sTimes.append(delim + String.format("%.2f", tmp.get(Calendar.HOUR_OF_DAY) +
                            calSTime.get(Calendar.MINUTE) / 100.0));
                    delim = ", ";
                }
                si.msAcTimeDn = sTimes.toString();
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + " getFrequencyAc: ", e.getMessage());
        }
        closeCursor(curs, " getFrequencyAc - ");
        return si;
    }

    public ArrayList<CFrequency> getFrequencyForRoute(String sRoutecode) {
        ArrayList<CFrequency> aoFrequency = new ArrayList<CFrequency>();
/*		switch (si.miBusType) {
            case 7:
			case 8:
			case 9:
				si = getFrequencyAc(si, iDow);
				return si;
			default:
				break;
		}
*/
        Cursor curs = null;
        Calendar now = Calendar.getInstance();
        Calendar tmp = (Calendar) now.clone();
        String rsql = "SELECT startheadwayhh, startheadwaymm, endheadwayhh, endheadwaymm, stopcodefrom, stopcodeto," +
                "\n\t (SELECT stopdisplayname FROM ba_stopmaster WHERE stopcode = f.stopcodefrom) stopnamefrom," +
                "\n\t (SELECT stopdisplayname FROM ba_stopmaster WHERE stopcode = f.stopcodeto) stopnameto" +
                "\n FROM vw_frequency f" +
                "\n routecode = '" + sRoutecode + "'";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    curs.getInt(curs.getColumnIndexOrThrow("startheadwayhh"));
                    curs.getInt(curs.getColumnIndexOrThrow("startheadwaymm"));
                    int firstFromHH = curs.getInt(curs.getColumnIndexOrThrow("firstfromhh"));
                    int firstFromMM = curs.getInt(curs.getColumnIndexOrThrow("firstfrommm"));
                    int frequency = curs.getInt(curs.getColumnIndexOrThrow("frequency"));
                    if (frequency != -1) {
                    } else {
                        tmp.set(Calendar.HOUR_OF_DAY, (int) (firstFromHH));
                        tmp.set(Calendar.MINUTE, firstFromMM);
//						tmp.add(Calendar.MINUTE, minsToStop);
//						String.format("%.2f", tmp.get(Calendar.HOUR_OF_DAY) + calSTime.get(Calendar.MINUTE) / 100.0);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + " getFrequency: ", e.getMessage());
        }
        closeCursor(curs, " getFrequency - ");
        return aoFrequency;
    } // getFrequencyForRoute

    public Cursor getBusesCursor(String filter) {
        Cursor curs = null;
        filter = filter.trim();
        String rsql = " SELECT route_id as _id, \nbusno " +
                "\n FROM ba_route r ORDER BY busno ";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();
            }
        } catch (Exception e) {
            SSLog_SS.e("DBHelper getBusesCurser", e.getMessage());
        }
        if (curs != null)
            return curs;
        else
            return new MatrixCursor(new String[]{"_id"}); // never return null
    } // getBusesCursor

    public ArrayList<String> getBuses() {
        Cursor curs = null;
        ArrayList<String> aBusNos = new ArrayList<String>();
        String sBusNo;
//		String rsql = "SELECT " + COLUMN_SEARCHSTR + " AS searchstr FROM ta_station ORDER BY UPPER(stationname)";
        String rsql = "SELECT buslabel busno " +
                " FROM ba_route" +
                " ORDER BY busno";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    sBusNo = curs.getString(curs.getColumnIndexOrThrow("busno"));
                    if (sBusNo != null)
                        aBusNos.add(sBusNo);

                }
            }
        } catch (Exception SQLException) {
            SSLog_SS.e(TAG + " getBuses - ", SQLException.getMessage());

        }
        closeCursor(curs, " getBuses - ");
        return aBusNos;
    } // getStationNames

    public ArrayList<String> getBusRoute(String sRouteCode) {
        Cursor curs = null;
        ArrayList<String> aBusRoute = new ArrayList<String>();
/*		String rsql = "SELECT (SELECT stopnamedetail FROM ba_stopunique u " +
                "\n INNER JOIN ba_stopmaster ON s.stop_id = u.stopid AND s.stopcode = rd.stopcode) stopnamedetail " +
						"\n FROM ba_routedetail rd " +
						"\n INNER JOIN ba_route r ON r.routecode = rd.routecode AND r.routecode = '" + sRouteCode + "'" +
						"\n ORDER BY stopserial";
*/
        String rsql = " SELECT stopserial, rd.stopcode stopcode, " +
                " stopdisplayname, " +
                "\n rd.routecode routecode,  " +
                " CASE WHEN rd.direction = 'U' THEN s.latu ELSE s.latd END lat, " +
                " CASE WHEN rd.direction = 'U' THEN s.lonu ELSE s.lond END lon" +
                "\n FROM ba_routedetail rd INNER JOIN ba_stopmaster s ON s.stopcode = rd.stopcode  " +
                "\n WHERE rd.routecode = '" + sRouteCode + "'" +
//				"\n AND direction = '" + sBusDirection + "'" +
                "\n AND direction = 'U' " +
                "\n ORDER BY stopserial ";

        String sStopDetail = "";
        try {
            curs = doQuery(rsql, null);
            int i = 1;
            if (curs != null) {
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    sStopDetail = curs.getString(curs.getColumnIndexOrThrow("stopdisplayname"));
                    if (sStopDetail != null)
                        aBusRoute.add(String.format("%2s", Integer.toString(i++)) + "." + sStopDetail);
                }
            }
        } catch (Exception SQLException) {
            SSLog_SS.e(TAG + " getBusRoute - ", SQLException.getMessage());
        }
        closeCursor(curs, " getBusRoute - ");

        return aBusRoute;
    } // GetBusRoute

    public ArrayList<String> getBusSerial(String sRouteCode) {
        Cursor curs = null;
        ArrayList<String> aBusSerial = new ArrayList<String>();
        String rsql = "SELECT stopserial" +
                "\n FROM ba_routedetail rd WHERE rd.routecode = '" + sRouteCode + "'" +
                "\n AND direction = 'U' " +
                "\n ORDER BY stopserial";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    aBusSerial.add(curs.getString(curs.getColumnIndexOrThrow("stopserial")));
                }
            }
        } catch (Exception SQLException) {
            SSLog_SS.e(TAG + " getBusSerial - ", SQLException.getMessage());

        }
        closeCursor(curs, " getBusSerial - ");
        return aBusSerial;
    }

    public String getBusRouteCodeFromBusNo(String sBusNo) {
        Cursor curs = null;
        String rsql = "SELECT routecode " +
                "\n FROM ba_route r " +
                "\n WHERE buslabel = '" + sBusNo + "' LIMIT 1";
        String sRouteCode = "";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
//				for(curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                while (curs.moveToNext()) {
                    sRouteCode = curs.getString(curs.getColumnIndexOrThrow("routecode"));
                    break;
                }
            }
        } catch (Exception SQLException) {
            SSLog_SS.e(TAG + " getBusRouteCodeFromBusNo - ", SQLException.getMessage());
        }
        closeCursor(curs, " getBusRouteCodeFromBusNo - ");
        return sRouteCode;
    }

    public CStop getStopFromStopCode(int miStartStopcode) {
        Cursor curs = null;
        CStop stop = new CStop();
        String rsql = "SELECT stopcode, stop_id, lat, lon, stopdisplayname FROM ba_stopmaster " +
                "\n WHERE stopcode = '" + miStartStopcode + "' LIMIT 1 ";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    stop.msStopNameDetail = curs.getString(curs.getColumnIndexOrThrow("stopdisplayname"));
                    stop.lat = curs.getDouble((curs.getColumnIndexOrThrow("lat")));
                    stop.lon = curs.getDouble((curs.getColumnIndexOrThrow("lon")));
                    stop.miStopCode = curs.getInt((curs.getColumnIndexOrThrow("stopcode")));
                    stop.miStopId = curs.getInt((curs.getColumnIndexOrThrow("stop_id")));
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + " getStopFromStopCode - ", e.getMessage());
        }
        closeCursor(curs, " getStopFromStopCode - ");
        return stop;
    } // getStopFromStopCode

    public CStop getStopFromId(int iStopId) {
        Cursor curs = null;
        CStop stop = new CStop();
        String rsql = "SELECT stop_id, stopcode, lat, lon, stopnamedetail FROM ba_stopunique " +
                "\n WHERE stop_id = '" + iStopId + "' LIMIT 1 ";
        try {
            /*sbAllQueries.setTables("ba_stopunique");
            curs = sbAllQueries.query(mBusDB, null, rsql, null, null, null, null, null);*/
            curs = doQuery(rsql, null);

            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    stop.msStopNameDetail = curs.getString(curs.getColumnIndexOrThrow("stopnamedetail"));
                    stop.lat = curs.getDouble((curs.getColumnIndexOrThrow("lat")));
                    stop.lon = curs.getDouble((curs.getColumnIndexOrThrow("lon")));
                    stop.miStopCode = curs.getInt((curs.getColumnIndexOrThrow("stopcode")));
                    stop.miStopId = curs.getInt((curs.getColumnIndexOrThrow("stop_id")));
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + "getStopFromId - ", e.getMessage());
        }
        closeCursor(curs, " getStopFromId - ");
        return stop;
    } // getStopFromId

    @SuppressWarnings("unused")
    public String getRouteCodeFromBusNo(String busno) {
        Cursor curs = null;
        String routecode = null;
        String rsql = "SELECT routecode" +
                "\n FROM ba_route WHERE buslabel = '" + busno + "' LIMIT 1";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    routecode = curs.getString(curs.getColumnIndexOrThrow("routecode"));
                    break;
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + " getRouteCodeFromBusNo - ", e.getMessage());
        }
        closeCursor(curs, " getRouteCodeFromBusNo - ");
        return routecode;
    } // getRouteCodeFromBusNo

    public ArrayList<CBus> getDirectBuses(int iStartStopId, int iDestStopCode,
                                          int iDow, double tm) {
        ArrayList<CBus> aoBus = new ArrayList<CBus>();
        SQLiteStatement s = mBusDB.compileStatement(
                "SELECT COUNT(*) FROM vwTrip a" +
                        "\n INNER JOIN vwTrip b WHERE a.routecode = b.routecode" +
                        "\n AND a.stop_id =" + iStartStopId + " AND b.stop_id = " + iDestStopCode);

        long count = s.simpleQueryForLong();
        if (count == 0)
            return aoBus;
        Cursor curs = null;
        CBus si = null;
        /*String rsql = "SELECT a.busno busno, a.bustype bustype, a.routecode routecode, a.fstopname fstopname, a.lstopname lstopname,  " +
                "\n a.stopcode fromstopcode, b.stopcode tostopcode, a.direction direction, a.stopserial stopserial,  " +
                "\n (SELECT stopdisplayname FROM  ba_stopmaster s WHERE s.stopcode = a.stopcode) firststopnamedetail, " +
                "\n (SELECT stopdisplayname FROM  ba_stopmaster s WHERE s.stopcode = b.stopcode) laststopnamedetail " +
                "\n FROM vwTrip a " +
                "\n INNER JOIN vwTrip b ON a.routecode = b.routecode AND a.direction = b.direction " +
                "\n AND a.stop_id = " + Integer.toString(iStartStopId) +
                "\n AND b.stop_id = " + Integer.toString(iDestStopCode) +
                "\n AND b.stopserial > a.stopserial " +
                "\n ORDER BY a.stop_id, b.stop_id, busno ";*/

        String rsql = "SELECT busno, bustype, a.routecode routecode, a.fstopname fstopname, a.lstopname lstopname," +
                " a.stopcode fromstopcode, b.stopcode tostopcode, a.direction direction, a.stopserial stopserial, " +
                "(SELECT stopdisplayname FROM  ba_stopmaster s WHERE s.stopcode = a.stopcode) firststopnamedetail, " +
                "(SELECT stopdisplayname FROM  ba_stopmaster s WHERE s.stopcode = b.stopcode) laststopnamedetail FROM " +
                "(SELECT * FROM ba_routedetail a INNER JOIN ba_routedetail b ON a.routecode = b.routecode " +
                "AND a.direction = b.direction AND a.stopunique_id= " + Integer.toString(iStartStopId) + " " +
                "AND b.stopunique_id= " + Integer.toString(iDestStopCode) + " " +
                "AND b.stopserial > a.stopserial) b INNER JOIN vwTrip a ON a.routecode = b.routecode " +
                "AND a.direction = b.direction AND a.stop_id = b.stopunique_id ORDER BY a.stop_id, b.stopunique_id, busno";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    si = new CBus(curs.getInt(curs.getColumnIndexOrThrow("busno")),
                            curs.getInt(curs.getColumnIndexOrThrow("bustype")));
                    si.miStopSerial = curs.getInt(curs.getColumnIndexOrThrow("stopserial"));
                    si.msDirection = curs.getString(curs.getColumnIndexOrThrow("direction"));
                    si.msFirstStop = si.msDirection.equals("U")
                            ? curs.getString(curs.getColumnIndexOrThrow("firststopnamedetail"))
                            : curs.getString(curs.getColumnIndexOrThrow("laststopnamedetail"));
                    si.msLastStop = si.msDirection.equals("U")
                            ? curs.getString(curs.getColumnIndexOrThrow("laststopnamedetail"))
                            : curs.getString(curs.getColumnIndexOrThrow("firststopnamedetail"));
                    si.msRouteCode = curs.getString(curs.getColumnIndexOrThrow("routecode"));
                    si.miFromStopCode = curs.getInt(curs.getColumnIndexOrThrow("fromstopcode"));
                    si.miToStopCode = curs.getInt(curs.getColumnIndexOrThrow("tostopcode"));
                    si.msLastStop = curs.getString(curs.getColumnIndexOrThrow("lstopname"));
                    aoBus.add(si);
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + " getDirectBuses - ", e.getMessage());
        }
        closeCursor(curs, " getDirectBuses - ");
        return aoBus;
    } // getDirectBuses

    public ArrayList<CConnection> getConnectingBuses(int iStartCode, int iDestCode,
                                                     double dStartLat, double dStartLon,
                                                     double dDestLat, double dDestLon,
                                                     int iDow, double tm) {
        double dStartLatStray, dDestLatStray;
        String rsql;
        if (dStartLat < dDestLat) {
            dStartLatStray = dStartLat - MAX_STRAY;
            dDestLatStray = dDestLat + MAX_STRAY;
        } else {
            dStartLatStray = dDestLat - MAX_STRAY;
            dDestLatStray = dStartLat + MAX_STRAY;
        }
        if (dStartLon < dDestLon) {
        } else {
        }

        ArrayList<CConnection> aoConnection = new ArrayList<CConnection>();
        Cursor curs = null;
        CConnection si;
        rsql = "SELECT " + iStartCode + " startstopcode, " + iDestCode + " deststopcode, " +
                " a.routecode routecode1, b.routecode routecode2, a.stopcode stopcode, " +
                " (SELECT stopdisplayname FROM ba_stopmaster s WHERE s.stopcode=a.stopcode) connectingstop, * " +
                "\n FROM " +
                "\n (SELECT routecode, stopcode, stopserial FROM ba_routedetail WHERE routecode IN " +
                "\n(SELECT routecode FROM ba_routedetail WHERE stopcode=" + iStartCode + ")) a " +
                "\n INNER JOIN " +
                "\n (SELECT routecode, stopcode, stopserial FROM ba_routedetail WHERE routecode IN " +
                "\n (SELECT routecode FROM ba_routedetail WHERE stopcode=" + iDestCode + ")) b " +
                "\n ON a.stopcode = b.stopcode " +
                "\n WHERE (SELECT latu FROM ba_stopmaster s WHERE s.stopcode=a.stopcode) " +
                " BETWEEN " + dStartLatStray + " AND " + dDestLatStray +
                "\n GROUP BY a.routecode, b.routecode " +
                "\n ORDER BY startstopcode, deststopcode, a.routecode, b.routecode";
        try {
            curs = doQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    si = new CConnection();
                    si.moBus1 = new CBus(curs.getString(curs.getColumnIndexOrThrow("routecode1")));
                    si.moBus2 = new CBus(curs.getString(curs.getColumnIndexOrThrow("routecode2")));
                    si.miStartStopcode = iStartCode;
                    si.miDestStopcode = iDestCode;
                    si.miConnectionStopcode = curs.getInt(curs.getColumnIndexOrThrow("stopcode"));
                    aoConnection.add(si);
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG + " getDirectBuses - ", e.getMessage());
        }
        closeCursor(curs, " getDirectBuses - ");
        return aoConnection;
    } // getDirectBuses

    /*public ArrayList<CBus> getDirectBusesNear(double dStartLat, double dStartLon,
                                              double dDestLat, double dDestLon, int iDow, double tm, double dRadialDist) {
        ArrayList<CBus> aNearBuses = new ArrayList<CBus>();
        ArrayList<CStop> aStopsNearStart;
        ArrayList<CStop> aStopsNearDest;
        aStopsNearStart = getNearStops(dStartLat, dStartLon, dRadialDist);
        if (aStopsNearStart == null) {
            aStopsNearStart = new ArrayList<CStop>();
            if (mApp.moStartStop != null)
                aStopsNearStart.add(mApp.moStartStop);
        }
        aStopsNearDest = getNearStops(dDestLat, dDestLon, dRadialDist);
        if (aStopsNearDest == null) {
            aStopsNearDest = new ArrayList<CStop>();
            if (mApp.moStartStop != null)
                aStopsNearDest.add(mApp.moDestStop);
        }
        int iNoBuses = 0;
        for (CStop sn : aStopsNearStart) {
            for (CStop sd : aStopsNearDest) {
                ArrayList<CBus> aNB = getDirectBuses(sn.miStopCode, sd.miStopCode, iDow, tm);
                for (CBus nb : aNB) {
                    aNearBuses.add(nb);
                    iNoBuses++;
                }
                if (iNoBuses > 0)
                    return aNearBuses;
            }
        }

        return aNearBuses;
    }*/

    // Runs the query and adds it to the total query variable -
    Cursor doQuery(String sQuery, String[] args) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        try {
            String s = ste[3].getMethodName();
             //if (sbAllQueries.toString().indexOf(s) == -1)
           // sbAllQueries.append(ste[3].getMethodName() + ":\n" + sQuery + "\n" + "\n");
        } catch (Exception e) {
            SSLog_SS.e(TAG + " doQuery - ", e.getMessage());
        }
        return mBusDB.rawQuery(sQuery, args);
    }

    public double getNearCurrentBusStopsUp(Double dLat, Double dLon, String sBusLable,
                                           int fromStopSerial, int toStopSerial, int iMyStopCode, String updown) {
        float leastDis = -1;
        int busNearestSerial = -1;
        int iDirection = 0;
        int iMystopserial = 0;
        double iMyDistance = 0.0;
        ArrayList<CStop> asi = new ArrayList<CStop>();
        if (dLat <= 0 || dLon <= 0)
            return 0;
        float[] res = new float[1];
        Cursor curs;
        CStop si = null;
        // String rsql = "SELECT r.routecode, stopserial, distance, stopcode, lat, lon FROM ba_routedetail rd INNER JOIN ba_route r ON r.buslabel = '" + sBusLable + "' AND r.routecode = rd.routecode AND rd.direction = '" + updown + "'";
        /*String rsql = "SELECT r.routecode, stopserial, distance, rd.stopcode, lat, lon, rd.direction, r.buslabel FROM ba_routedetail rd INNER JOIN ba_stopmaster rs INNER JOIN ba_route r ON r.buslabel = '"
                + sBusLable + "' AND r.direction = '" + updown +
                "' AND r.routecode = rd.routecode AND rd.direction = '" +
                updown + "' AND rs.stop_id = rd.stopunique_id ORDER BY stopserial";*/
        String rsql = "SELECT r.routecode, stopserial, distance, rd.stopcode, lat, lon, rd.direction, r.buslabel FROM ba_routedetail rd INNER JOIN ba_stopmaster rs ON rs.stopcode = rd.stopcode INNER JOIN ba_route r ON r.buslabel = '" +
                sBusLable + "' AND r.direction = '" + updown + "' WHERE rd.direction = '" +
                updown + "' AND rd.routecode = r.routecode ORDER BY stopserial"; // calculate distance dus to person query
        curs = doQuery(rsql, null);
        if (curs != null) {
            for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                si = new CStop(curs.getInt(curs.getColumnIndexOrThrow("routecode")),
                        curs.getInt(curs.getColumnIndexOrThrow("stopcode")),
                        curs.getDouble(curs.getColumnIndexOrThrow("lat")),
                        curs.getDouble(curs.getColumnIndexOrThrow("lon")));
                si.setDistance(curs.getDouble(curs.getColumnIndex("distance")));
                if (si != null) {
                    Location.distanceBetween(dLat, dLon,
                            si.lat, si.lon, res);
                    si.mdDist = (int) res[0];
                    asi.add(si);
                    if (iMyStopCode == si.miStopCode) {
                        iMystopserial = curs.getInt(curs.getColumnIndexOrThrow("stopserial")); // person nearest stop serial
                    }
                    if (leastDis == -1) {
                        leastDis = si.mdDist;
                    } else if (leastDis > si.mdDist) {
                        leastDis = si.mdDist;
                        busNearestSerial = asi.size() - 1; // bus nearest stop serial
                    }
                }
            }
            iDirection = fromStopSerial - toStopSerial > 0 ? 1 : -1;
            if (iDirection == -1) {
                for (int i = busNearestSerial; i <= iMystopserial; i = i + 1) { // up distance calculate loop
                    iMyDistance += asi.get(i).mdDistance;
                }
            } else if (iDirection == 1) {
                for (int i = busNearestSerial; i >= iMystopserial; i = i - 1) { // down distance calculate loop
                    iMyDistance += asi.get(i).mdDistance;
                }
            } else {
                iMyDistance = 0.0;
            }
        }
        closeCursor(curs, " getNearStops - ");
        return iMyDistance;
    } // getNearStations

    public ArrayList<String> getNearestStations(double lat, double lon) {
        if (lat <= 0 || lon <= 0)
            return null;
        ArrayList<String> asi = new ArrayList<>();
        float[] res = new float[1];

        Cursor curs = null;

        String rsql = "SELECT distinct(stopdisplayname) as station\n" +
                "FROM ba_stopmaster \n" +
                "WHERE ABS("+lat+"- lat) + ABS("+lon +"- lon) <0.1\n" +
                "ORDER BY ABS("+lat +"- lat ) + ABS("+lon +"-lon ) ASC limit 3";
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getNearStations: ", rsql);
        } catch (Exception e) {
            SSLog.e(TAG, " getNearStations - ", e.getMessage());
        }

        if (curs != null) {
            while (curs.moveToNext()) {


                asi.add(curs.getString(curs.getColumnIndexOrThrow("station")));
            }
        }
        return asi;
    } // class DBHelper

    public ArrayList<CNearestStation> getNearestBusStation(double lat, double lon) {
        if (lat <= 0 || lon <= 0)
            return null;
        ArrayList<CNearestStation> asi = new ArrayList<>();
        float[] res = new float[1];

        Cursor curs = null;

        String rsql = "SELECT distinct(stopdisplayname) as station,\n" +
                "round((ABS( " + lat + "- lat) + ABS( " + lon + " - lon))*100,1) distance\n" +
                "FROM ba_stopmaster \n" +
                "WHERE ABS("+lat+"- lat) + ABS("+lon +"- lon) <0.1\n" +
                "ORDER BY ABS("+lat +"- lat ) + ABS("+lon +"-lon ) ASC limit 3";
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getNearStations: ", rsql);
        } catch (Exception e) {
            SSLog.e(TAG, " getNearStations - ", e.getMessage());
        }

        if (curs != null) {
            while (curs.moveToNext()) {

                CNearestStation cNearestStation = new CNearestStation();
                cNearestStation.setStation(curs.getString(curs.getColumnIndexOrThrow("station")));
                cNearestStation.setDistance(curs.getDouble(curs.getColumnIndexOrThrow("distance")));
                asi.add(cNearestStation);

            }
        }
        return asi;
    } // class DBHelper

} // class DBHelper
