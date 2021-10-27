package com.smartshehar.android.app;

import android.app.Activity;
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
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import lib.app.util.CTime;


public class DBHelperTrain extends SQLiteAssetHelper {

    private final int LINE = 1;
    private final String TAG = "DBHelper: ";
    private SQLiteQueryBuilder sbAllQueries;


    //private static String DB_PATH;

    private static String DB_NAME = "sst.jet";
    private SQLiteDatabase mTrainDB;

    private final Context mContext;
    private Activity mActivity;
    private PackageInfo packageInfo;
    private CGlobals_trains mApp;
    public final static int CONNECTING_TRAIN_MIN_AFTER = 3;
    public final static int MINUTES_IN_DAY = 24 * 60;
    public final static int MAX_TRAINS = 15;
    private static final int DIRECTMINSEXTRA = 10;


    public static boolean DEBUG = true;

    public DBHelperTrain(Context context) {
        super(context, DB_NAME, null, 4);
        //DB_PATH = context.getApplicationInfo().dataDir + "/";
        sbAllQueries = new SQLiteQueryBuilder();
        this.mContext = context;
        setForcedUpgrade();
    }


    /*private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        final String dbVersion = packageInfo.versionName + packageInfo.versionCode;
       *//* final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);*//*
        String currentDbVersion = CGlobals_lib_ss.getInstance().getPersistentPreference(mContext).getString("dbVersionInfo", "-1");
        if (!currentDbVersion.equals(dbVersion)) {
            return false;
        }
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        } catch (SQLiteException e) {
            Log.d(CGlobals_trains.TAG, e.getMessage());
            //database does't exist yet.
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }*/

   /* private void copyDataBase() throws IOException {
        //Open your local db as the input stream
        InputStream myInput = mContext.getAssets().open(DB_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;
        File f = new File(DB_PATH);
        if (!f.exists()) {
            f.mkdir();
        }
        //Open the empty db as the output stream
        OutputStream myOutput = null;
        try {
            myOutput = new FileOutputStream(outFileName);
        } catch (FileNotFoundException e) {
            SSLog.e(TAG, "DBHelper.copyDataBase): ", e.getMessage());
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
        String dbVersion = packageInfo.versionName + packageInfo.versionCode;
        *//*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        Editor editpref = prefs.edit();
        editpref.putString("dbVersionInfo", dbVersion);
        editpref.commit();*//*
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(mContext).
                putString("dbVersionInfo", dbVersion).apply();

    }*/

    public void openDataBase(PackageInfo packageinfo, Activity activity,
                             CGlobals_trains app) throws SQLException {
        mActivity = activity;
        packageInfo = packageinfo;
        mApp = app;
        //  boolean dbExist = checkDataBase();
//    	DB_PATH = mContext.getApplicationInfo().dataDir + "/databases/";

        String myPath = /*DB_PATH +*/ DB_NAME;

        //if (dbExist) {
        //do nothing - database already exists
        //} else {
        // if the database does not exist, copy it from the assets folder
        try {
            //copyDataBase();
            if (mTrainDB == null)
                mTrainDB = getReadableDatabase();
            Log.d(TAG, "DB OPEN");

        } catch (Exception e) {
          SSLog.e(TAG,"openDataBase ",e.toString());
        }
        mApp.mCH.userPing("/TR", mActivity.getString(R.string.atDBCreate));
        //  }
    }

    @Override
    public synchronized void close() {

        if (mTrainDB != null)
            mTrainDB.close();
        super.close();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(CGlobals_trains.TAG, "Database being upgraded");
    }


    public Station getNearestStation(Double dLat, Double dLon, Activity activity) {
        Station si;
        ArrayList<Station> asi;
        asi = getNearStations(dLat, dLon, activity);
        if (asi != null && asi.size() > 0) {
            si = asi.get(0);
            if (si != null)
                return si;
        }
        return null;
    }

    // Get the stops within MINWALKINGDIST from oStop
    ArrayList<Station> getNearStations(Double dLat, Double dLon, Activity activity) {
        if (dLat <= 0 || dLon <= 0)
            return null;
        ArrayList<Station> asi = new ArrayList<Station>();
        float[] res = new float[1];

        Cursor curs = null;
        Station si = null;
        String rsql = "SELECT s.station_id station_id, stnabbr, stationname, lat, lon, " +
                "\n stationname || ' (' || GROUP_CONCAT(sl.linecode) || ')'  searchstr " +
                "\n FROM ta_station s INNER JOIN ta_stationline sl " +
                "\n ON s.station_id = sl.station_id ";
//				+ " GROUP BY s.station_id ORDER BY UPPER(stationname)";

        rsql = rsql + "\n WHERE ABS(lat -" + Double.toString(dLat)
                + " ) <= " + CGlobals_trains.SMINLATLONDIFF + " AND ABS(lon - " + Double.toString(dLon)
                + " ) <= " + CGlobals_trains.SMINLATLONDIFF;
        rsql = rsql + " \n GROUP BY s.station_id ORDER BY UPPER(stationname)";
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getNearStations: ", rsql);
        } catch (Exception e) {
            SSLog.e(TAG, " getNearStations - ", e.getMessage());
        }

        if (curs != null) {
            for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                si = setStationInfoFromCursor(curs);
                if (si != null) {
                    Location.distanceBetween(dLat, dLon,
                            si.mdLat, si.mdLon, res);
                    si.dDist = (int) res[0];
                    if (si.dDist <= CGlobals_trains.MINWALKINGDIST)
                        asi.add(si);

                }
            }
            try {
                curs.close();
                curs = null;
            } catch (Exception e) {
                SSLog.e("DBHelper: ", "getNearStations", " cursor problem");
            }
        }

        Collections.sort(asi, new Comparator<Object>() {

            public int compare(Object o1, Object o2) {
                Station s1 = (Station) o1;
                Station s2 = (Station) o2;
                return (int) (s1.dDist - s2.dDist);
            }
        });
        return asi;
    } // getNearStations

    // For populating the start and destination stations
    public ArrayList<String> getStationNames() {
        Cursor curs = null;
        ArrayList<String> aStationNames = new ArrayList<String>();
        String sStationName;
//		String rsql = "SELECT " + COLUMN_SEARCHSTR + " AS searchstr FROM ta_station ORDER BY UPPER(stationname)";
        String rsql = "SELECT stationname || ' (' || GROUP_CONCAT(sl.linecode) || ')' AS searchstr " +
                " FROM ta_station s INNER JOIN ta_stationline sl " +
                " ON s.station_id = sl.station_id " +
                " GROUP BY s.station_id ORDER BY UPPER(stationname)";
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getStationNames: ", rsql);

            if (curs != null) {
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    sStationName = curs.getString(curs.getColumnIndexOrThrow("searchstr"));
                    if (sStationName != null)
                        aStationNames.add(sStationName);

                }
                curs.close();
                curs = null;
            }
        } catch (Exception SQLException) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getStationNames", SQLException.getMessage());

        }
        return aStationNames;
    } // getStationNames

    public ArrayList<Station> getStations() {
        Cursor curs = null;
        ArrayList<Station> aStations = new ArrayList<Station>();
        String sStationName;
//		String rsql = "SELECT " + COLUMN_SEARCHSTR + " AS searchstr FROM ta_station ORDER BY UPPER(stationname)";
        String rsql = "SELECT s.station_id station_id, stationname || ' (' || GROUP_CONCAT(sl.linecode) || ')' AS searchstr " +
                " FROM ta_station s INNER JOIN ta_stationline sl " +
                " ON s.station_id = sl.station_id " +
                " GROUP BY s.station_id ORDER BY UPPER(stationname)";
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getStations: ", rsql);
            if (curs != null) {
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    sStationName = curs.getString(curs.getColumnIndexOrThrow("searchstr"));
                    if (sStationName != null)
                        aStations.add(new Station(curs.getInt(curs.getColumnIndexOrThrow("station_id")),
                                sStationName));

                }
                curs.close();
                curs = null;
            }
        } catch (Exception SQLException) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getStationNames", SQLException.getMessage());

        }
        return aStations;
    } // getStations

    public ArrayList<Station> getStationNamesTo(int stationId) { // destinations from this station
        Cursor curs = null;
        ArrayList<Station> aStationNames = new ArrayList<Station>();
        Station oStation;
// 		String rsql = "SELECT " + COLUMN_SEARCHSTR + " AS searchstr FROM ta_station ORDER BY UPPER(stationname)";
        String rsql = "SELECT s.station_id station_id, searchstr FROM vw_StationLineConcat s " +
                "\n INNER JOIN ta_stationconnections c ON s.station_id = c.deststation_id " +
                " AND c.startstation_id=" + stationId +
                " GROUP BY searchstr ORDER BY UPPER(searchstr)";
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getStationNamesTo: ", rsql);

            if (curs != null) {
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    oStation = new Station(curs.getInt(curs.getColumnIndexOrThrow("station_id")),
                            curs.getString(curs.getColumnIndexOrThrow("searchstr")));
                    if (oStation != null)
                        aStationNames.add(oStation);

                }
                curs.close();
                curs = null;
            }
        } catch (Exception SQLException) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getStationNames", SQLException.getMessage());

        }
        return aStationNames;
    }

    // For populating the start and destination stations
    public Cursor getStationNamesCursor(String filter) {
        Cursor curs = null;
        filter = filter.trim();

        String searchstr1 = " HAVING searchstr LIKE  '" + filter + "%' ";
        String searchstr2 = " HAVING searchstr LIKE  '%" + filter + "%' AND searchstr NOT LIKE '" + filter + "%' ";
        String orderBy = " ORDER BY ord";
        String sql = "\n sl.stationline_id as _id, \nstationname || ' (' || GROUP_CONCAT(sl.linecode) || ')' AS searchstr " +
                "\n FROM ta_station s INNER JOIN ta_stationline sl " +
                "\n ON s.station_id = sl.station_id " +
                "\n GROUP BY s.station_id \n";
/*		+
                " HAVING searchstr LIKE  '%" + filter.trim() + "%' " +
				" ORDER BY UPPER(stationname)";*/
        String rsql = "SELECT 1 as ord, " + sql + searchstr1 +
                " UNION SELECT 2 as ord, " + sql + searchstr2 + orderBy;
        if (TextUtils.isEmpty(filter)) {
            rsql = " SELECT " + sql + " ORDER BY UPPER(stationname)";
        }
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getStationNamesCursor: ", rsql);

            if (curs != null) {
                curs.moveToFirst();
            }
        } catch (Exception e) {
            SSLog.e("DBHelper ", "getStationNamesCurser", e.getMessage());
        }
        if (curs != null)
            return curs;
        else
            return new MatrixCursor(new String[]{"_id"}); // never return null
    }

    public ArrayList<CDirection> getTrainDestinations(Station oStation, boolean mbTowardsMerge) {
        ArrayList<CDirection> aTrainDestinations = new ArrayList<CDirection>();
        String grpby = " GROUP BY towardsstation_id ";
        if (mbTowardsMerge)
            grpby = " GROUP BY mrg ";
        String rsql = " SELECT GROUP_CONCAT(stationname || ' (' || t.linecode || ')') AS stationname,  mrg, " +
                "\n towardsstation_id FROM ta_towardsstation t " +
                "\n INNER JOIN ta_station ON ta_station.station_id = t.towardsstation_id " +
                "\n INNER JOIN ta_line l ON t.linecode = l.linecode " +
                "\n WHERE t.station_id = " + oStation.miStationId +
                grpby +
                "\n ORDER BY line_id, stationname ";

        Cursor curs = doQuery(rsql, null);
        SSLog.i("getTrainDestinations: ", rsql);

        try {
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    aTrainDestinations.add(
                            new CDirection(curs.getString(curs.getColumnIndexOrThrow("stationname")),
                                    "", -1,
                                    curs.getInt(curs.getColumnIndexOrThrow("towardsstation_id")),
                                    curs.getString(curs.getColumnIndexOrThrow("mrg"))
                            ));
                }
                curs.close();
                curs = null;
            } else
                return null;
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper (554) - getTrainDestinations", e.getMessage());
            if (curs != null)
                curs.close();
            return null;
        }

        return aTrainDestinations;

    }

    public ArrayList<CTrainAtStation> getTrainsTowards(Station moStart, String sTowardStations, String sMergeId,
                                                       boolean bTowardsMerge, int iTowardsStationId, CTime tm) {

        ArrayList<CTrainAtStation> aTrainsAtStation = new ArrayList<CTrainAtStation>();
///  		Calendar calendar = Calendar.getInstance();
        Cursor curs = null;
        String msMergeCond = " tw.mrg = '" + sMergeId + "' ";
        String sUnMergeCond = " sc.towardsstation_id = tw.towardsstation_id ";
        if (!bTowardsMerge) {
            msMergeCond = " tw.towardsstation_id=" + iTowardsStationId;
            sUnMergeCond = " (sc.towardsstation_id = tw.towardsstation_id OR sc.towardsstation_id2 = " + iTowardsStationId + " )";
        }
        int lastfifteenMin = tm.miTimeMin - 15;
        String rsql = "SELECT sc.train_id AS train_id, sc.mins mins, speeddescription, car, sc.directioncode AS directioncode, line_id, sc.linecode linecode, colour, \n  " +
                "\n splcode, sundayonly, notonsunday, holiday, sc.route_id AS route_id, sc.trainspeedcode trainspeedcode, " +
                "\n sc.trainname trainname, platformno,  indicatorspeedcode, platformside, " +
                " line_id, splcode, sc.route_id AS route_id, sc.trainspeedcode trainspeedcode, " +
                "\n(CASE WHEN sc.mins < " + lastfifteenMin + " THEN sc.mins + 1440 ELSE sc.mins END) - " + lastfifteenMin + " diff," +
                "\n sc.trainno trainno, sc.platformno platformno, sc.indicatorspeedcode AS indicatorspeedcode, platformside, " +
                "\n (SELECT stationname FROM ta_station s WHERE s.station_id = " + moStart.miStationId + ") AS stationname, " +
                " \n(SELECT stationname FROM ta_station s WHERE s.station_id = sc.firststation_id)  AS firststation, " +
                "\n (SELECT stationname FROM ta_station s WHERE s.station_id = sc.laststation_id) AS laststation " +
                "\n FROM vw_sc_t_r_rd_l sc \n" +
                "\n INNER JOIN ta_towardsstation tw ON " + sUnMergeCond +
                "\n AND tw.linecode = sc.linecode AND " +
//			" tw.mrg = '" + sMergeId + "' " +
                msMergeCond +
                " AND tw.station_id = " + moStart.miStationId +
                " \n WHERE sc.station_id = " + moStart.miStationId +
                " AND laststation != stationname " +
                "\nGROUP BY train_id " +
                " ORDER BY diff LIMIT 50  ";
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getTrainsTowards: ", rsql);
        } catch (Exception e) {
            SSLog.e("DBHelper: ", "getTrainsTowards -", e.getMessage());
        }
        CTrainAtStation ct;
        try {
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    ct = new CTrainAtStation(curs.getInt(curs.getColumnIndexOrThrow("train_id")),
                            curs.getInt(curs.getColumnIndexOrThrow("route_id")),
                            curs.getInt(curs.getColumnIndexOrThrow("line_id")),
                            curs.getString(curs.getColumnIndexOrThrow("platformno")),
                            curs.getString(curs.getColumnIndexOrThrow("trainno")),
                            curs.getString(curs.getColumnIndexOrThrow("linecode")),
                            curs.getString(curs.getColumnIndexOrThrow("colour")),
                            curs.getString(curs.getColumnIndexOrThrow("splcode")),
                            curs.getInt(curs.getColumnIndexOrThrow("sundayonly")),
                            curs.getInt(curs.getColumnIndexOrThrow("notonsunday")),
                            curs.getInt(curs.getColumnIndexOrThrow("holiday")),
                            curs.getString(curs.getColumnIndexOrThrow("indicatorspeedcode")),
                            curs.getString(curs.getColumnIndexOrThrow("trainspeedcode")),
                            curs.getString(curs.getColumnIndexOrThrow("firststation")),
                            curs.getString(curs.getColumnIndexOrThrow("laststation")),
                            curs.getString(curs.getColumnIndexOrThrow("directioncode")),
                            curs.getInt(curs.getColumnIndexOrThrow("car")),
                            "",
                            curs.getInt(curs.getColumnIndexOrThrow("mins")));
                    ct.moTrainTime = new CTime(curs.getInt(curs.getColumnIndexOrThrow("mins")));
                    ct.miDiff = curs.getInt(curs.getColumnIndexOrThrow("diff"));
                    ct.msTrainName = curs.getString(curs.getColumnIndexOrThrow("trainname"));
                    if (ct != null)
                        aTrainsAtStation.add(ct);

                }
                curs.close();
                curs = null;
            } else
                return null;
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getTrainsAtStations", e.getMessage());
            if (curs != null)
                curs.close();
            return null;
        }
        return aTrainsAtStation;
    } // getTrainsTowards


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


    public Station getStationFromSearchStr(String searchstr) {
        Cursor curs = null;
        Station si = null;
        int posBracket = searchstr.indexOf("(");
        if (posBracket < 0)
            return null;
        String st = searchstr.substring(0, posBracket).trim();
        String rsql = "SELECT s.station_id station_id, stationname, stnabbr, lat, lon, stationname || ' (' || GROUP_CONCAT(sl.linecode) || ')'  searchstr " +
                " FROM ta_station s INNER JOIN ta_stationline sl " +
                " ON s.station_id = sl.station_id ";
        rsql = rsql + " WHERE stationname LIKE '%" + st + "%'";
        rsql = rsql + " GROUP BY s.station_id ORDER BY UPPER(stationname)";

//		String rsql = SELECT_TRAININFO + " FROM ta_station WHERE stationname = '" + st + "'";
        ;
//		rsql = rsql + " WHERE stationcode = " + msStartStopCode;
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getStationFromSearchStr: ", rsql);

            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    si = setStationInfoFromCursor(curs);
                    if (si.msSearchStr.equals(searchstr)) {
                        curs.close();
                        return si;
                    }
                }
                curs.close();
                curs = null;
            } else
                return null;
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getTrainsAtStations", e.getMessage());
            if (curs != null)
                curs.close();
            return null;
        }

        return si;
    }

    public class Route {
        public int miStartStationId;
        public int miDestStationId;
        public int miConnStationId;
        public int miConn;
        double mdTotalTravelTime;
        public String msStartStation;
        public String msDestStation;
        public String msConnStation;
        ArrayList<Schedule> maoScheduleSegment1;
        ArrayList<Schedule> maoScheduleSegment2;
        ArrayList<Connection> maoConnection;
        public String msStartStationAbbr;
        public String msDestStationAbbr;
        public String msConnStationAbbr;
        public String msLineCode;
    }

    public ArrayList<Route> getDestinationRoutes(int iStartStationId, int iDestStationId,
                                                 CTime tm, String sFilterTrains) {

        ArrayList<Route> aoRoute = new ArrayList<Route>();
        Route route;
        Cursor curs = null;
        String sCond = "";
        if (sFilterTrains.equals("Direct"))
            sCond = "AND connection_id <= 0 ";
        if (sFilterTrains.equals("Default"))
            sCond = "AND priority <= 3";
        if (sFilterTrains.equals("Default"))
            sCond = "AND priority <= 3";

        String rsql = "SELECT startstation_id, deststation_id, connection_id, linecode, " +
                "\n (SELECT stationname FROM ta_station WHERE station_id=startstation_id) startstation, " +
                "\n (SELECT stnabbr FROM ta_station WHERE station_id=startstation_id) startstationabbr, " +
                "\n (SELECT stationname FROM ta_station WHERE station_id=deststation_id) deststation, " +
                "\n (SELECT stnabbr FROM ta_station WHERE station_id=deststation_id) deststationabbr, " +
                "\n (SELECT stationname FROM ta_station WHERE station_id=connection_id) connstation, " +
                "\n (SELECT stnabbr FROM ta_station WHERE station_id=connection_id) connstationabbr " +
                "\n FROM ta_stationconnections " +
                "\n WHERE startstation_id = " + iStartStationId + " AND deststation_id = " + iDestStationId +
                "\n " + sCond +
                "\n ORDER BY connection_id";


        try {
            curs = doQuery(rsql, null);
            SSLog.i("getDestinationRoutes: ", rsql);
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    route = new Route();
                    route.miStartStationId = curs.getInt(curs.getColumnIndexOrThrow("startstation_id"));
                    route.miDestStationId = curs.getInt(curs.getColumnIndexOrThrow("deststation_id"));
                    route.miConnStationId = curs.getInt(curs.getColumnIndexOrThrow("connection_id"));
                    route.msLineCode = curs.getString(curs.getColumnIndexOrThrow("linecode"));
                    route.msStartStation = curs.getString(curs.getColumnIndexOrThrow("startstation"));
                    route.msStartStationAbbr = curs.getString(curs.getColumnIndexOrThrow("startstationabbr"));
                    route.msDestStation = curs.getString(curs.getColumnIndexOrThrow("deststation"));
                    route.msDestStationAbbr = curs.getString(curs.getColumnIndexOrThrow("deststationabbr"));
                    route.msConnStation = curs.getString(curs.getColumnIndexOrThrow("connstation"));
                    route.msConnStationAbbr = curs.getString(curs.getColumnIndexOrThrow("connstationabbr"));
                    route.miConn = 0;
                    if (route.miConnStationId > 0)
                        route.miConn = 1;
                    aoRoute.add(route);
                }
                curs.close();
            }
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getRoute()", e.getMessage());
            if (curs != null)
                curs.close();
            return null;
        }

        return aoRoute;

    }    // getRoute1


    class Schedule {
        int miTrainId;
        int miStationId;
        double mdStartTime, mdDestTime, mdTravelTime;
        CTime moTravelTime;
        String msStartTime, msDestTime, msTravelTime;
        int miRouteId, miLineId, miPlatform, miCar;
        boolean mbSundayOnly, mbNotOnSunday, mbHoliday;
        String msPlatform, msTrainNo;
        String msLine, msSpl, msSpeedAbbr, msSpeed, msFirstStation, msTowardsStationName,
                msLastStation, msDirectionCode, msDirection;
        double dTrainTime;
        String msTrainTime, msColour;

        DecimalFormat dec = new DecimalFormat("#0.00");
        public String msPlatformSide;
        public String msLineCode;
        public String msEMU;
        public String msTrainSpeedCode;
        public String msIndicatorSpeed;
        public String msStation;

        void setTimes(double startTime, double destTime) {
            if (startTime >= 24) {
                startTime = startTime - 24;
            }
            mdStartTime = startTime;
            msStartTime = dec.format(startTime >= 13 ? startTime - 12 : startTime);
            if (destTime >= 24) {
                destTime = destTime - 24;
            }
            mdDestTime = destTime;
            msDestTime = dec.format(destTime >= 13 ? destTime - 12 : destTime);
            moTravelTime = timeDifference(startTime, destTime);
        }

    } // Schedule


    // Converts decimal time into time and gets difference in decimal
    private CTime timeDifference(double iTime1, double iTime2) {
        int iHr1 = (int) iTime1;
        int iMin1 = (int) (((iTime1 + .009) - iHr1) * 100);
        int iHr2 = (int) iTime2;
        int iMin2 = (int) (((iTime2 + .009) - iHr2) * 100);
        int iHr, iMin;
        iMin = iMin2 - iMin1;
        if (iMin >= 0) {
            iHr = iHr2 - iHr1;
        } else {
            iMin = iMin2 + 60 - iMin1;
            iHr = iHr2 - iHr1 - 1;
        }
        CTime tm = new CTime(iHr, iMin);
        return tm;
    }

    public class Connection {
        public int miStartMin;
        public boolean mbDirect;
        public int miDestMin;
        public int miTrainId1;
        public int miTrainId2;
        public double mdStartTime;
        public double mdDestTime;
        double mdTravelTime;
        CTime moTravelTime;
        public String msStartTime;
        public String msDestTime;
        String msTravelTime;
        public boolean mbSundayOnly, mbNotOnSunday, mbHoliday;
        String msPlatform, msTrainNo;
        String msLine;
        public String msSpl1;
        public String msSpl2;
        String msSpeedAbbr;
        String msSpeed;
        String msTowardsStationName;
        String msDirectionCode;
        String msDirection;
        double dTrainTime;
        String msTrainTime;
        public String msColour1;
        public String msColour2;
        public String msTrainNo1;
        public String msTrainNo2;


        DecimalFormat dec = new DecimalFormat("#0.00");
        public String msEMU;
        public String msTrainSpeedCode1;
        public String msDestStation;
        public String msStartStation;
        public String msConnStation;
        public double mdConnTime1;
        public String msConnTime1;
        public double mdConnTime2;
        public String msConnTime2;
        public int miConnMin1, miConnMin2;
        public String msStartStationAbbr;
        public String msDestStationAbbr;
        public String msConnStationAbbr;
        public CTime moTravelTime1, moTravelTime2;
        public String msConnSpeed1;
        public String msConnSpeed2;
        public String msLineCode1;
        public String msLineCode2;
        public String msPlatform1;
        public String msPlatform2;
        public String msPlatformSide1;
        public String msPlatformSide2;
        public int miStartStationId;
        public int miDestStationId;
        public int miConnStationId;
        public String msFirstStation1;
        public String msLastStation1;
        public String msFirstStation2;
        public String msLastStation2;
        public int miCar1;
        public int miCar2;
        public int miSortMins;

        Connection() {
            msStartStationAbbr = "";
            msDestStationAbbr = "";
            msConnStationAbbr = "";
            msConnSpeed1 = "";
            msConnSpeed2 = "";
            msLineCode1 = "";
            msLineCode2 = "";
            miConnMin1 = -1;
            miConnMin2 = -1;
            mbDirect = false;

        }

        void TripTime(int startMins, int destMins, int connMins1, int connMins2) {
            CTime st = new CTime(startMins);
            CTime dt = new CTime(destMins);
            CTime c1 = new CTime(connMins1);
            CTime c2 = new CTime(connMins2);
            mdStartTime = st.mdTm;
            msStartTime = st.msTm;
            mdDestTime = dt.mdTm;
            msDestTime = dt.msTm;
            miStartMin = startMins;
            miDestMin = destMins;
            mdConnTime1 = 0;
            mdConnTime2 = 0;
            msConnTime1 = "";
            msConnTime2 = "";
            if (connMins1 > 0) {
                mdConnTime1 = c1.mdTm;
                msConnTime1 = c1.msTm;
                moTravelTime1 = new CTime(connMins1 - startMins);
            }
            if (connMins2 > 0) {
                mdConnTime2 = c2.mdTm;
                msConnTime2 = c2.msTm;
                moTravelTime2 = new CTime(destMins - connMins2);
            }

        }


    }

    public long startStationsTable(int iStartStationId, int iConnectionStationId, CTime tm, int isSunday, String trainspeedcode) {
        long count = 0;
        String sSundayCond = "";
        String sTrainSpeedCond = "";
        if (isSunday == 1)
            sSundayCond = "AND ( a.sundayonly = 1 OR a.notonsunday = 0)";
        else
            sSundayCond = "AND a.sundayonly = 0";

        if (trainspeedcode.equals("Slow"))
            sTrainSpeedCond = "AND a.trainspeedcode = 'S'";
        tm.miTimeMin = tm.miTimeMin - 10;
        mTrainDB.execSQL("DROP TABLE IF EXISTS con1");
        String rsql = "CREATE TABLE con1 AS " +
                "\n SELECT a.train_id trainid1,a.notonsunday anot, b.notonsunday bnot, a.trainno trainno1,  " +
                "\n CASE WHEN a.mins < " + tm.miTimeMin + " THEN a.mins + 1440 ELSE a.mins END startmins,  " +
                "\n CASE WHEN b.mins < " + tm.miTimeMin + " OR a.mins < " + tm.miTimeMin + " THEN b.mins + 1440 ELSE b.mins END connmins1,  " +
                "\n a.trainno trainno1, a.car car1, a.splcode splcode1, a.emu emu1, a.trainspeedcode trainspeedcode1, " +
                "\n (SELECT stationname FROM ta_station s WHERE s.station_id = a.firststation_id) AS firststation1, " +
                "\n (SELECT stationname FROM ta_station s WHERE s.station_id = a.laststation_id) AS laststation1, " +
                "\n a.directioncode directioncode1, " +
                "\n a.indicatorspeedcode indicatorspeedcode1, a.colour colour1, a.linecode linecode1, a.platformno platform1, " +
                "\n b.platformside platformside1, b.mins connmins1,  " +
                "\n (CASE WHEN a.mins < " + tm.miTimeMin + " THEN a.mins + " + MINUTES_IN_DAY + " ELSE a.mins END) - " + tm.miTimeMin + " diff1 " +
                "\n FROM vw_route a INNER JOIN vw_route b ON a.train_id = b.train_id AND b.station_id = " + iConnectionStationId +
                "\n AND b.stationserial > a.stationserial " +
                "\n WHERE a.station_id = '" + iStartStationId + "'" +
                "\n " + sSundayCond +
                "\n " + sTrainSpeedCond +
                "\nORDER by diff1 " +
                "\n LIMIT " + MAX_TRAINS;
        try {
            mTrainDB.execSQL(rsql);
            SQLiteStatement s = mTrainDB.compileStatement("SELECT COUNT(*) FROM con1");
            count = s.simpleQueryForLong();
            SSLog.i("startStationsTable: ", rsql);
            SSLog.i("con1: ", "Added rows");
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - startStationsTable() - con1: ", e.getMessage());
        }
        return count;
    }


    public void destStationsTable(int iStartStationId, int iDestStationId, int iConnectionStationId, CTime tm, int isSunday, String trainspeedcode) {
        String sMinMins = getFromDb("SELECT connmins1 FROM con1 ORDER BY diff1 LIMIT 1");

        mTrainDB.execSQL("DROP TABLE IF EXISTS con2;");

        String sTrainSpeedCond = "";
        String sSundayCond = "";
        if (isSunday == 1)
            sSundayCond = "AND ( a.sundayonly = 1 OR a.notonsunday = 0)";
        else
            sSundayCond = "AND a.sundayonly = 0";

        if (trainspeedcode.equals("Slow"))
            sTrainSpeedCond = "AND a.trainspeedcode = 'S'";
        String rsql = "CREATE TABLE con2 AS " +
                "\n SELECT a.train_id trainid2,a.notonsunday anot, b.notonsunday bnot, a.trainno trainno2, " +
                "\n CASE WHEN a.mins < " + tm.miTimeMin + " THEN a.mins + 1440 ELSE a.mins END connmins2,  " +
                "\n CASE WHEN b.mins < " + tm.miTimeMin + " OR a.mins < " + tm.miTimeMin + " THEN b.mins + 1440 ELSE b.mins END destmins,  " +
                "\n a.trainno trainno, a.car car2, a.splcode splcode2, a.emu emu2, a.trainspeedcode trainspeedcode2, " +
                "\n (SELECT stationname FROM ta_station s WHERE s.station_id = a.firststation_id) AS firststation2, " +
                "\n (SELECT stationname FROM ta_station s WHERE s.station_id = a.laststation_id) AS laststation2, " +
                "\n a.directioncode directioncode2, " +
                "\n a.indicatorspeedcode indicatorspeedcode2, a.colour colour2, a.linecode linecode2, a.platformno platform2, " +
                "\n b.platformside platformside2, " +
                "\n (CASE WHEN a.mins < " + tm.miTimeMin + " THEN a.mins + " + MINUTES_IN_DAY + " ELSE a.mins END) - "
                + tm.miTimeMin + " diff2 " +
                "\n FROM vw_route a INNER JOIN vw_route b ON a.train_id = b.train_id AND b.station_id = " + iDestStationId +
                "\n AND b.stationserial > a.stationserial " +
                "\n WHERE a.station_id = '" + iConnectionStationId + "'" +
                "\n AND (CASE WHEN a.mins < " + tm.miTimeMin + " THEN a.mins + " +
                MINUTES_IN_DAY + " ELSE a.mins END) " +
                " >  " + sMinMins +
                "\n " + sSundayCond +
                "\n " + sTrainSpeedCond +
                "\n ORDER by diff2 " +
                "\n LIMIT " + MAX_TRAINS;


        try {
            mTrainDB.execSQL(rsql);
            SSLog.i("destStationsTable: ", rsql);
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - destStationsTable() - con2: ", e.getMessage());
        }

    } // destStationsTable


    public ArrayList<Connection> getConnections(int iStartStationId, int iDestStationId,
                                                int iConnectionStationId, String sStartStation, String sDestStation, String sConnStation,
                                                String sStartStationAbbr, String sDestStationAbbr, String sConnStationAbbr, CTime tm) {
        ArrayList<Connection> aoConnection = new ArrayList<Connection>();
        Connection connection;
        Cursor curs = null;


        String rsql = " SELECT trainid1, trainno1, trainno2, startmins, connmins1, car1, car2,  " +
                "\n firststation1, laststation1, firststation2, laststation2, colour1, colour2,  " +
                "\n trainid2, trainno2, connmins2, destmins, indicatorspeedcode1, linecode1, indicatorspeedcode2, linecode2, " +
                "\n splcode1, splcode2, platform1, platform2, platformside1, platformside2" +
                "\n FROM con1 c1 " +
                "\n INNER JOIN con2 c2 ON c2.connmins2 > c1.connmins1 + " +
                CONNECTING_TRAIN_MIN_AFTER +
                " WHERE connmins1 > startmins " +
/*				" BETWEEN c1.connmins1+" CONNECTING_TRAIN_MIN_AFTER + " AND " +
                " c1.connmins1+ " + CONNECTING_TRAIN_MIN_UPTO  + */
                " GROUP BY trainid1, trainid2 " +
                "\n ORDER BY destmins " +
                "\n LIMIT " + +MAX_TRAINS;


        try {
            curs = doQuery(rsql, null);
            SSLog.i("getConnections: ", rsql);
            int mSundayOnly, mNotSunday;
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    connection = new Connection();
                    connection.miStartStationId = iStartStationId;
                    connection.miDestStationId = iDestStationId;
                    connection.miConnStationId = iConnectionStationId;
                    connection.msStartStation = sStartStation;
                    connection.msDestStation = sDestStation;
                    connection.msConnStation = sConnStation;
                    connection.msStartStationAbbr = sStartStationAbbr;
                    connection.msDestStationAbbr = sDestStationAbbr;
                    connection.msConnStationAbbr = sConnStationAbbr;
//					connection.miStationId = curs.getInt(curs.getColumnIndexOrThrow("station_id"));
                    connection.miTrainId1 = curs.getInt(curs.getColumnIndexOrThrow("trainid1"));
                    connection.miTrainId2 = curs.getInt(curs.getColumnIndexOrThrow("trainid2"));
                    connection.msTrainNo1 = curs.getString(curs.getColumnIndexOrThrow("trainno1"));
                    connection.msTrainNo2 = curs.getString(curs.getColumnIndexOrThrow("trainno2"));
                    connection.TripTime(curs.getInt(curs.getColumnIndexOrThrow("startmins")),
                            curs.getInt(curs.getColumnIndexOrThrow("destmins")),
                            curs.getInt(curs.getColumnIndexOrThrow("connmins1")),
                            curs.getInt(curs.getColumnIndexOrThrow("connmins2")));
                    connection.miConnMin1 = curs.getInt(curs.getColumnIndexOrThrow("connmins1"));
                    connection.miConnMin2 = curs.getInt(curs.getColumnIndexOrThrow("connmins2"));
                    connection.miStartMin = curs.getInt(curs.getColumnIndexOrThrow("startmins"));
                    connection.miDestMin = curs.getInt(curs.getColumnIndexOrThrow("destmins"));
                    connection.msConnSpeed1 = curs.getString(curs.getColumnIndexOrThrow("indicatorspeedcode1"));
                    connection.msConnSpeed2 = curs.getString(curs.getColumnIndexOrThrow("indicatorspeedcode2"));
                    connection.msLineCode1 = curs.getString(curs.getColumnIndexOrThrow("linecode1"));
                    connection.msLineCode2 = curs.getString(curs.getColumnIndexOrThrow("linecode2"));
                    connection.msPlatform1 = curs.getString(curs.getColumnIndexOrThrow("platform1"));
                    connection.msPlatform2 = curs.getString(curs.getColumnIndexOrThrow("platform2"));
                    connection.msPlatformSide1 = curs.getString(curs.getColumnIndexOrThrow("platformside1"));
                    connection.msPlatformSide2 = curs.getString(curs.getColumnIndexOrThrow("platformside2"));
                    connection.msFirstStation1 = curs.getString(curs.getColumnIndexOrThrow("firststation1"));
                    connection.msLastStation1 = curs.getString(curs.getColumnIndexOrThrow("laststation1"));
                    connection.msFirstStation2 = curs.getString(curs.getColumnIndexOrThrow("firststation2"));
                    connection.msLastStation2 = curs.getString(curs.getColumnIndexOrThrow("laststation2"));
                    connection.msColour1 = curs.getString(curs.getColumnIndexOrThrow("colour1"));
                    connection.msColour2 = curs.getString(curs.getColumnIndexOrThrow("colour2"));
                    connection.miCar1 = curs.getInt(curs.getColumnIndexOrThrow("car1"));
                    connection.miCar2 = curs.getInt(curs.getColumnIndexOrThrow("car2"));
                    connection.msSpl1 = curs.getString(curs.getColumnIndexOrThrow("splcode1"));
                    connection.msSpl2 = curs.getString(curs.getColumnIndexOrThrow("splcode2"));

                    connection.miSortMins =
                            connection.mbDirect ? connection.miDestMin - DIRECTMINSEXTRA : connection.miDestMin;

                    aoConnection.add(connection);
                }
                curs.close();
                return aoConnection;
            }
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getConnections()", e.getMessage());
            if (curs != null)
                curs.close();
            return null;
        }
        return null;

    } // getConnections2(..)

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
            SSLog.i("getStationFromId: ", rsql);

            if (curs != null) {
                curs.moveToFirst();

                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    si = setStationInfoFromCursor(curs);
                }
                curs.close();
                curs = null;
            } else
                return null;
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getStationFromId", e.getMessage());
            if (curs != null)
                curs.close();
            return null;
        }

        return si;
    } // getStationFromId

    public Station getFirstStation(int miTrainId) {
        Cursor curs = null;
        Station si = null;
        String rsql = " SELECT v.station_id station_id, stnabbr, stationname, lat, lon, searchstr " +
                "\n FROM vw_sc_t_r_rd_l v WHERE stationserial=1 AND train_id=" + miTrainId;
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getFirstStation: ", rsql);

            if (curs != null) {
                curs.moveToFirst();

                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    si = setStationInfoFromCursor(curs);
                }
                curs.close();
                curs = null;
            } else
                return null;
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getFirstStation", e.getMessage());
            if (curs != null)
                curs.close();
            return null;
        }

        return si;

    } // getFirstStation

    public Station getLastStation(int miTrainId) {
        Cursor curs = null;
        Station si = null;
        String rsql = " SELECT v.station_id station_id, stnabbr, stationname, lat, lon, searchstr " +
                "\n FROM vw_sc_t_r_rd_l v WHERE  station_id=laststation_id AND train_id=" + miTrainId;
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getLastStation: ", rsql);
            if (curs != null) {
                curs.moveToFirst();

                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    si = setStationInfoFromCursor(curs);
                }
                curs.close();
                curs = null;
            } else
                return null;
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getLastStation ", e.getMessage());
            if (curs != null)
                curs.close();
            return null;
        }

        return si;

    } // getFirstStation

    public ArrayList<Station> getTrainTrip(int iTrainId, int iStartStationId, int iDestStationId) {
        int startSerial = 0, destSerial = 0;
        Cursor curs1 = null;
        String rsql = " SELECT v.stationserial startserial,  " +
                "\n (SELECT v2.stationserial destserial FROM vw_route v2 " +
                "\n WHERE v2.train_id = v.train_id AND v2.station_id = " + iDestStationId + ") destserial " +
                "\n FROM vw_route v WHERE train_id=" + iTrainId + " AND v.station_id=" + iStartStationId;
        try {
            curs1 = doQuery(rsql, null);
            SSLog.i("getTrainTrip: ", rsql);
            if (curs1 != null) {
                curs1.moveToFirst();

                for (curs1.moveToFirst(); !curs1.isAfterLast(); curs1.moveToNext()) {
                    startSerial = curs1.getInt(curs1.getColumnIndexOrThrow("startserial"));
                    destSerial = curs1.getInt(curs1.getColumnIndexOrThrow("destserial"));
                }
                curs1.close();
                curs1 = null;
            } else
                return null;
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getLastStation ", e.getMessage());
            if (curs1 != null)
                curs1.close();
            return null;
        }

        String cond = "\n AND stationserial BETWEEN " + startSerial + " AND " + destSerial +
                "\n ORDER BY stationserial ";
        if (destSerial < startSerial)
            cond = "\n AND stationserial BETWEEN " + destSerial + " AND " + startSerial +
                    "\n ORDER BY stationserial DESC";

        Cursor curs = null;
        Station station;
        ArrayList<Station> aoStation = new ArrayList<Station>();
        rsql = "SELECT train_id, trainno, lat, lon, line_id, linecode, splcode, car, emu, mins, route_id, " +
                "\n mins, trainspeedcode trainspeed, mins, station_id, stationname, directioncode, line, platformno, " +
                "\n sc.indicatorspeedcode AS indicatorspeedcode, stationserial, sc.platformside AS platformside, " +
                "\n (SELECT stationname FROM ta_station s WHERE s.station_id = sc.firststation_id) AS firststation, " +
                "\n (SELECT stationname FROM ta_station s WHERE s.station_id = sc.laststation_id) AS laststation " +
                "\n FROM vw_sc_t_r_rd_l sc " +
                "\n WHERE sc.train_id= " + iTrainId + cond;

        try {
            curs = doQuery(rsql, null);
            SSLog.i("getTrainTrip: ", rsql);
            if (curs != null) {
                curs.moveToFirst();
                for (curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
                    station = setTripFromCursor(curs);
                    aoStation.add(station);
                }
                curs.close();
                curs = null;
            } else
                return null;
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getLastStation ", e.getMessage());
            if (curs != null)
                curs.close();
            return null;
        }

        return aoStation;

    }

    private Station setTripFromCursor(Cursor curs) {
        try {
            Station station = new Station(
                    curs.getInt(curs.getColumnIndexOrThrow("train_id")),
                    curs.getInt(curs.getColumnIndexOrThrow("route_id")),
                    curs.getInt(curs.getColumnIndexOrThrow("line_id")),
                    curs.getInt(curs.getColumnIndexOrThrow("station_id")),
                    curs.getString(curs.getColumnIndexOrThrow("platformno")),
                    curs.getString(curs.getColumnIndexOrThrow("platformside")),
                    curs.getString(curs.getColumnIndexOrThrow("trainno")),
                    curs.getString(curs.getColumnIndexOrThrow("linecode")),
                    curs.getString(curs.getColumnIndexOrThrow("splcode")),
                    curs.getString(curs.getColumnIndexOrThrow("car")),
                    curs.getString(curs.getColumnIndexOrThrow("emu")),
                    curs.getString(curs.getColumnIndexOrThrow("trainspeed")),
                    curs.getString(curs.getColumnIndexOrThrow("indicatorspeedcode")),
                    curs.getString(curs.getColumnIndexOrThrow("stationname")),
                    curs.getString(curs.getColumnIndexOrThrow("firststation")),
                    curs.getString(curs.getColumnIndexOrThrow("laststation")),
                    curs.getString(curs.getColumnIndexOrThrow("directioncode")),
                    curs.getInt(curs.getColumnIndexOrThrow("mins"))
            );

            station.mdLat = curs.getDouble(curs.getColumnIndexOrThrow("lat"));
            station.mdLon = curs.getDouble(curs.getColumnIndexOrThrow("lon"));
            station.miTrainTimeMin = curs.getInt(curs.getColumnIndexOrThrow("mins"));

            return station;
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - RunQuery: ", e.getMessage());
            return null;
        }
    }


    public ArrayList<Connection> getDirect(String sTableName, int iStartStationId,
                                           int iConnectionStationId, CTime tm, int isSunday, String trainspeedcode) {
        ArrayList<Connection> aoConnection = new ArrayList<Connection>();
        Connection connection;
        Cursor curs = null;
        String sSundayCond = "";
        String sTrainSpeedCond = "";
        if (isSunday == 1)
            sSundayCond = "AND ( a.sundayonly = 1 OR a.notonsunday = 0)";
        else
            sSundayCond = "AND a.sundayonly = 0";

        if (trainspeedcode.equals("Slow"))
            sTrainSpeedCond = "AND a.trainspeedcode = 'S'";

        mTrainDB.execSQL("DROP TABLE IF EXISTS " + sTableName);
        tm.miTimeMin = tm.miTimeMin - 10;
        String rsql = "CREATE TABLE " + sTableName + " AS " +
                "\n SELECT a.train_id trainid,  a.notonsunday anot, b.notonsunday bnot, " +
                "\n CASE WHEN a.mins < " + tm.miTimeMin + " THEN a.mins + 1440 ELSE a.mins END startmins,  " +
                "\n CASE WHEN b.mins < " + tm.miTimeMin + " OR a.mins < " + tm.miTimeMin + " THEN b.mins + 1440 ELSE b.mins END destmins,  " +
                "\n a.trainno trainno, a.car car, a.splcode splcode, a.emu emu, a.trainspeedcode trainspeedcode, " +
                "\n (SELECT stationname FROM ta_station s WHERE s.station_id = a.firststation_id) AS firststation, " +
                "\n (SELECT stationname FROM ta_station s WHERE s.station_id = a.laststation_id) AS laststation, " +
                "\n a.directioncode directioncode, " +
                "\n a.indicatorspeedcode indicatorspeedcode, a.colour colour, a.linecode linecode, a.platformno platform, " +
                "\n b.platformside platformside, a.splcode splcode1, b.mins connmins1,  " +
                "\n (CASE WHEN a.mins < " + tm.miTimeMin + " THEN a.mins + " + MINUTES_IN_DAY + " ELSE a.mins END) - " + tm.miTimeMin + " diff " +
                "\n FROM vw_route a INNER JOIN vw_route b ON a.train_id = b.train_id AND b.station_id = " + iConnectionStationId +
                "\n AND b.stationserial > a.stationserial " +
                "\n WHERE a.station_id = '" + iStartStationId + "' " +
                "\n " + sTrainSpeedCond +
                "\n " + sSundayCond +
                "\nORDER by diff" +
                "\n LIMIT " + MAX_TRAINS;
        try {
            mTrainDB.execSQL(rsql);
            SSLog.i("con1: ", "Added rows");
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getDirect: ", e.getMessage());
        }

        rsql = "SELECT * FROM " + sTableName;
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getDirect: ", rsql);
            if (curs != null) {
                if (curs.moveToFirst()) {
                    do {
                        connection = new Connection();
//				connection.miStationId = curs.getInt(curs.getColumnIndexOrThrow("station_id"));
                        connection.miTrainId1 = curs.getInt(curs.getColumnIndexOrThrow("trainid"));
                        connection.TripTime(curs.getInt(curs.getColumnIndexOrThrow("startmins")),
                                curs.getInt(curs.getColumnIndexOrThrow("destmins")), 0, 0);
                        connection.miStartMin = curs.getInt(curs.getColumnIndexOrThrow("startmins"));
                        connection.miDestMin = curs.getInt(curs.getColumnIndexOrThrow("destmins"));
                        connection.msPlatform1 = curs.getString(curs.getColumnIndexOrThrow("platform"));
                        connection.msPlatformSide1 = curs.getString(curs.getColumnIndexOrThrow("platformside"));
                        connection.msTrainNo = curs.getString(curs.getColumnIndexOrThrow("trainno"));
                        connection.msLineCode1 = curs.getString(curs.getColumnIndexOrThrow("linecode"));
                        connection.msSpl1 = curs.getString(curs.getColumnIndexOrThrow("splcode"));
                        connection.msColour1 = curs.getString(curs.getColumnIndexOrThrow("colour"));
                        connection.miCar1 = curs.getInt(curs.getColumnIndexOrThrow("car"));
                        connection.msEMU = curs.getString(curs.getColumnIndexOrThrow("emu"));
                        connection.msTrainSpeedCode1 = curs.getString(curs.getColumnIndexOrThrow("trainspeedcode"));
                        connection.msConnSpeed1 = curs.getString(curs.getColumnIndexOrThrow("indicatorspeedcode"));
//				connection.msStation = curs.getString(curs.getColumnIndexOrThrow("stationname"));
                        connection.msFirstStation1 = curs.getString(curs.getColumnIndexOrThrow("firststation"));
                        connection.msLastStation1 = curs.getString(curs.getColumnIndexOrThrow("laststation"));
                        connection.msDirectionCode = curs.getString(curs.getColumnIndexOrThrow("directioncode"));
                        connection.mbDirect = true;
                        connection.msTrainNo1 = connection.msTrainNo;
                        connection.miSortMins = connection.miDestMin - DIRECTMINSEXTRA;
                        aoConnection.add(connection);
                    } while (curs.moveToNext());
                }
                curs.close();
                return aoConnection;
            } else
                return null;
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getDirectConnections()", e.getMessage());
            if (curs != null)
                curs.close();
            return null;
        }
    } // getDirect

    public class Fare {
        public String msFrom = "", msTo = "", msLineCode = "", msVia = "";
        public int miFare, miFare1st,
                miFarePass1m, miFarePass3m,
                miFarePass6m, miFarePass1y,
                miFarePass1m1st, miFarePass3m1st,
                miFarePass6m1st, miFarePass1y1st, mToken, mSmartcard, m45trips;
        String msFare, msFare1st,
                msFarePass1m, msFarePass3m,
                msFarePass6m, msFarePass1y,
                msFarePass1m1st, msFarePass3m1st,
                msFarePass6m1st, msFarePass1y1st;

    }

    public ArrayList<Fare> getFareCursor(int iStartStationId, int sortOrder) {
        String tempLineCode = "", mLineCode = null;
        ArrayList<Fare> aoFare = new ArrayList<Fare>();
        Fare fare;
        Cursor curs = null;
        String sortCondition = "";
        String rsql = "SELECT _id, linecode, via, tostation, fare, fare1st, " +
                "\n farepass1m, farepass3m, farepass1m1st, farepass3m1st,\n " +
                " farepass6m, farepass6m1st, farepass1y, farepass1y1st FROM vw_fare" +
                " WHERE from_id =" + iStartStationId;
        if (sortOrder == LINE)
            sortCondition = "\n ORDER BY linecode DESC, from_id ";
        else
            sortCondition = "\n ORDER BY tostation ";

        rsql = rsql + sortCondition;
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getFareCursor: ", rsql);
            if (curs != null) {
                while (curs.moveToNext()) {
                    fare = new Fare();
//				fare.msFrom = curs.getString(curs.getColumnIndexOrThrow("fromstation"));
                    fare.msTo = curs.getString(curs.getColumnIndexOrThrow("tostation"));
                    mLineCode = curs.getString(curs.getColumnIndexOrThrow("linecode"));
                    if (!tempLineCode.equalsIgnoreCase(mLineCode)) {
                        fare.msLineCode = mLineCode;
                        tempLineCode = mLineCode;
                    } else {
                        fare.msLineCode = " ";
                    }

                    fare.msVia = curs.getString(curs.getColumnIndexOrThrow("via"));
                    fare.miFare = curs.getInt(curs.getColumnIndexOrThrow("fare"));
                    fare.miFare1st = curs.getInt(curs.getColumnIndexOrThrow("fare1st"));
                    fare.miFarePass1m = curs.getInt(curs.getColumnIndexOrThrow("farepass1m"));
                    fare.miFarePass3m = curs.getInt(curs.getColumnIndexOrThrow("farepass3m"));
                    fare.miFarePass1m1st = curs.getInt(curs.getColumnIndexOrThrow("farepass1m1st"));
                    fare.miFarePass3m1st = curs.getInt(curs.getColumnIndexOrThrow("farepass3m1st"));
                    fare.miFarePass6m = curs.getInt(curs.getColumnIndexOrThrow("farepass6m"));
                    fare.miFarePass6m1st = curs.getInt(curs.getColumnIndexOrThrow("farepass6m1st"));
                    fare.miFarePass1y = curs.getInt(curs.getColumnIndexOrThrow("farepass1y"));
                    fare.miFarePass1y1st = curs.getInt(curs.getColumnIndexOrThrow("farepass1y1st"));


                    fare.msFare = curs.getString(curs.getColumnIndexOrThrow("fare"));
                    fare.msFare1st = curs.getString(curs.getColumnIndexOrThrow("fare1st"));
                    fare.msFarePass1m = curs.getString(curs.getColumnIndexOrThrow("farepass1m"));
                    fare.msFarePass3m = curs.getString(curs.getColumnIndexOrThrow("farepass3m"));
                    fare.msFarePass1m1st = curs.getString(curs.getColumnIndexOrThrow("farepass1m1st"));
                    fare.msFarePass3m1st = curs.getString(curs.getColumnIndexOrThrow("farepass3m1st"));
                    fare.msFarePass6m = curs.getString(curs.getColumnIndexOrThrow("farepass6m"));
                    fare.msFarePass6m1st = curs.getString(curs.getColumnIndexOrThrow("farepass6m1st"));
                    fare.msFarePass1y = curs.getString(curs.getColumnIndexOrThrow("farepass1y"));
                    fare.msFarePass1y1st = curs.getString(curs.getColumnIndexOrThrow("farepass1y1st"));


                    aoFare.add(fare);
                }
                return aoFare;
            } else
                return null;
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getFareList()", e.getMessage());
            if (curs != null)
                curs.close();
            return null;
        }
    } // getFareList

    public ArrayList<Fare> getFare(int iStartStationId, int iDestStationId, int sortOrder) {
        Fare fare;
        ArrayList<Fare> aoFare = new ArrayList<Fare>();
        Cursor curs = null;
        String sortCondition = "";
        String rsql = "SELECT _id, linecode, via, tostation, fare, fare1st," +
                "\n farepass1m, farepass3m, farepass1m1st, farepass3m1st,\n " +
                " farepass6m, farepass6m1st, farepass1y, farepass1y1st FROM vw_fare" +
                "\n WHERE from_id =" + iStartStationId + " AND to_id=" + iDestStationId;
        if (sortOrder == LINE)
            sortCondition = "\n ORDER BY linecode DESC, from_id ";
        else
            sortCondition = "\n ORDER BY tostation ";

        sortCondition = "\n ORDER BY farepass1m1st, via ";
        rsql = rsql + sortCondition;
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getFareCursor: ", rsql);
            if (curs != null) {
                while (curs.moveToNext()) {
                    fare = new Fare();
                    fare.msTo = curs.getString(curs.getColumnIndexOrThrow("tostation"));
                    fare.msLineCode = curs.getString(curs.getColumnIndexOrThrow("linecode"));
                    fare.msVia = curs.getString(curs.getColumnIndexOrThrow("via"));
                    fare.miFare = curs.getInt(curs.getColumnIndexOrThrow("fare"));
                    fare.miFare1st = curs.getInt(curs.getColumnIndexOrThrow("fare1st"));
                    fare.miFarePass1m = curs.getInt(curs.getColumnIndexOrThrow("farepass1m"));
                    fare.miFarePass3m = curs.getInt(curs.getColumnIndexOrThrow("farepass3m"));
                    fare.miFarePass1m1st = curs.getInt(curs.getColumnIndexOrThrow("farepass1m1st"));
                    fare.miFarePass3m1st = curs.getInt(curs.getColumnIndexOrThrow("farepass3m1st"));
                    fare.miFarePass6m = curs.getInt(curs.getColumnIndexOrThrow("farepass6m"));
                    fare.miFarePass6m1st = curs.getInt(curs.getColumnIndexOrThrow("farepass6m1st"));
                    fare.miFarePass1y = curs.getInt(curs.getColumnIndexOrThrow("farepass1y"));
                    fare.miFarePass1y1st = curs.getInt(curs.getColumnIndexOrThrow("farepass1y1st"));
                    aoFare.add(fare);
                }
            }
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getFareList()", e.getMessage());
            if (curs != null)
                curs.close();
        }
        if (curs != null)
            curs.close();
        return aoFare;
    } // getFare


    /*static String HTMLRED = "<span style=\"color:#FF0000\">";*/
    public String getTrains(int startmins, String tbl) {
        Cursor curs = null;
        String workSql = null;
///		DecimalFormat dec = new DecimalFormat("00");

        String rsql = " SELECT GROUP_CONCAT('  <b>' ||  tm || '</b> ' || linecode|| ' ' || " +
                "\n CASE WHEN indicatorspeedcode = 'F' THEN " +
                "\"<font color=\'red\'><b>F</b></font>\"" + " ELSE indicatorspeedcode END || ' ' || laststation) scheduleline " +
                " FROM (SELECT linecode, indicatorspeedcode, tm, laststation, " +
                "\n CASE WHEN mins < " + startmins + " THEN mins + 1441 ELSE mins END minorder " +
                "\n FROM " + tbl + " ORDER BY minorder LIMIT 25 )" +
                "\n GROUP BY 'A' ";

        String scheduleline;
        StringBuilder sb = new StringBuilder();
        try {
            curs = doQuery(rsql, null);
            SSLog.i("getTrains: ", rsql);
/// 			String delim = "";
            if (curs != null) {
                if (curs.moveToFirst()) {
                    do {

                        scheduleline = curs.getString(curs.getColumnIndexOrThrow("scheduleline"));

                        sb.append(scheduleline);
                    } while (curs.moveToNext());
                }
                if (curs != null)
                    curs.close();
                workSql = sb.toString();
                return workSql;
            } else
                return null;
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - getFareList()", e.getMessage());
            if (curs != null)
                curs.close();
            return null;
        }
    }


    public int createHomeWorkTable(int iHomeStationId, int iWorkStationId) {
        mTrainDB.execSQL("DROP TABLE IF EXISTS l_home");
        String sql = "CREATE TABLE l_home AS " +
                " SELECT ss.linecode linecode, ss.indicatorspeedcode indicatorspeedcode, ss.mins mins, " +
                "\n SUBSTR('000' || CAST(ss.mins / 60 AS TEXT), -2, 2) || ':' || SUBSTR('000' || CAST(ss.mins % 60 AS TEXT), -2, 2) tm, " +
                "\n (SELECT stationname FROM ta_station s WHERE ss.laststation_id = s.station_id) laststation " +
                "\n FROM ta_stationconnections c " +
                "\n INNER JOIN vw_sc_t_r_rd_l ss ON c.startstation_id = ss.station_id " +
                "\n AND ss.train_id =sd.train_id " +
                "\n INNER JOIN vw_sc_t_r_rd_l sd ON " +
                "\n CASE WHEN connection_id > 0 THEN connection_id ELSE deststation_id END = sd.station_id " +
                "\n WHERE startstation_id= " + Integer.toString(iWorkStationId) +
                "\n AND deststation_id=" + Integer.toString(iHomeStationId) +
                "\n AND sd.stationserial > ss.stationserial" +
                "\n GROUP BY ss.train_id ORDER BY mins";
        try {
            mTrainDB.execSQL(sql);
            SSLog.i("l_home: ", sql);
            SSLog.i("l_home: ", "Added rows");
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - l_home: ", e.getMessage());
        }

//		createTable(sql, "l_home");
        mTrainDB.execSQL("DROP TABLE IF EXISTS l_work");
        sql = "CREATE TABLE l_work AS " +
                " SELECT ss.linecode linecode, ss.indicatorspeedcode indicatorspeedcode, ss.mins mins, " +
                "\n SUBSTR('000' || CAST(ss.mins / 60 AS TEXT), -2, 2) || ':' || SUBSTR('000' || CAST(ss.mins % 60 AS TEXT), -2, 2) tm, " +
                "\n (SELECT stationname FROM ta_station s WHERE ss.laststation_id = s.station_id) laststation " +
                "\n FROM ta_stationconnections c " +
                "\n INNER JOIN vw_sc_t_r_rd_l ss ON c.startstation_id = ss.station_id " +
                "\n AND ss.train_id =sd.train_id " +
                "\n INNER JOIN vw_sc_t_r_rd_l sd ON " +
                "\n CASE WHEN connection_id > 0 THEN connection_id ELSE deststation_id END = sd.station_id " +
                "\n WHERE startstation_id= " + Integer.toString(iHomeStationId) +
                "\n AND deststation_id=" + Integer.toString(iWorkStationId) +
                "\n AND sd.stationserial > ss.stationserial" +
                "\n GROUP BY ss.train_id ORDER BY mins";
        ;
        try {
            mTrainDB.execSQL(sql);
            SSLog.i("l_work: ", sql);
            SSLog.i("l_home: ", "Added rows");
        } catch (Exception e) {
            SSLog.e(CGlobals_trains.TAG, "DBHelper - l_work: ", e.getMessage());
        }

//		createTable(sql, "l_work");

        return 0;
    }


    // Runs the query and adds it to the total query variable -
    Cursor doQuery(String sQuery, String[] args) {
        if (DEBUG) {
            final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
            try {
                String s = ste[3].getMethodName();
                // if (sbAllQueries.toString().indexOf(s) == -1)
                //    sbAllQueries.append(ste[3].getMethodName() + ":\n" + sQuery + "\n" + "\n");
                //			sbAllQueries.append(sQuery + "\n" + "\n");
            } catch (Exception e) {
                SSLog.e(TAG, " doQuery - ", e.getMessage());
            }
        }
        return mTrainDB.rawQuery(sQuery, args);
    }

    public String getFromDb(String sql) {
        Cursor c = doQuery(sql, null);
        if (c.getCount() == 1) {
            c.moveToFirst();
            return c.getString(0);

        }
        c.moveToFirst();
        return null;
    }

    public ArrayList<String> getNearestStations(double lat, double lon) {
        if (lat <= 0 || lon <= 0)
            return null;
        ArrayList<String> asi = new ArrayList<>();
        float[] res = new float[1];

        Cursor curs = null;
        String rsql = "SELECT stationname || ' (' || GROUP_CONCAT(sl.linecode) || ')'  station\n" +
                "(ABS( " + lat + "- lat) + ABS( " + lon + " - lon))/1000 distance" +
                "FROM ta_station s INNER JOIN ta_stationline sl  ON s.station_id = sl.station_id  \n" +
                "GROUP BY s.station_id \n" +
                "HAVING ABS( " + lat + "- lat) + ABS( " + lon + " - lon) < 0.04\n" +
                "ORDER BY ABS( " + lat + " -lat) + ABS( " + lon + " - lon) ASC limit 3";
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

    public ArrayList<CNearestStation> getNearestStation(double lat, double lon) {
        if (lat <= 0 || lon <= 0)
            return null;
        ArrayList<CNearestStation> asi = new ArrayList<>();
        float[] res = new float[1];

        Cursor curs = null;
        try {
            String rsql = "SELECT stationname || ' (' || GROUP_CONCAT(sl.linecode) || ')'  station,\n" +
                    "round((ABS( " + lat + "- lat) + ABS( " + lon + " - lon))*100,1) distance\n" +
                    "FROM ta_station s INNER JOIN ta_stationline sl  ON s.station_id = sl.station_id  \n" +
                    "GROUP BY s.station_id \n" +
                    "HAVING ABS( " + lat + "- lat) + ABS( " + lon + " - lon) < 0.04\n" +
                    "ORDER BY ABS( " + lat + " -lat) + ABS( " + lon + " - lon) ASC limit 3";
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
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            SSLog.e(TAG,"getNearestStation ",e.toString());
        }
        return asi;
    }

}

	
