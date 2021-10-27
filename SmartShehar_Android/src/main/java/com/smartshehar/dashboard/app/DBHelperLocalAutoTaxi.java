package com.smartshehar.dashboard.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import lib.app.util.DBHelperLocal;
import lib.app.util.UserInfo;

public class DBHelperLocalAutoTaxi extends DBHelperLocal {
    //The Android's default system path of your application database.
    private static String TAG = "DBHelperLocalAutoTaxi: ";
    private static String DB_PATH;
    private static String DB_NAME = "ssd_local.jet";
    public static final String COLUMN_CALLHOME = "\n _id, date, clientms, email, imei, module, lat, lon, accuracy, locationtime, " +
            "\n provider, version, app, carrier, product, manufacturer";
    private final Context context;
    private Context cContext;
    private PackageInfo mVersionInfo;

    private static String SELECT_USERTRIP = "SELECT _id, email, city, vehicleno, vehicletype, startlat, " +
            "\n startlon, startaddr, " +
            "\n strftime(\'%d-%m-%Y at %H:%M', DATETIME(startms/1000, 'unixepoch', 'localtime')) startdatetime, " +
            "\n strftime(\'%d-%m-%Y at %H:%M', DATETIME(destms/1000, 'unixepoch', 'localtime')) destdatetime, " +
            "\n strftime(\'%d-%m-%Y at %H:%M', DATETIME(startms/1000, 'unixepoch', 'localtime')) || " +
            " CASE WHEN length(vehicleno) > 0 THEN \' (' || vehicleno || ')' ELSE '' END tripidtext, " +
            "\n destlat, destlon, destaddr, startms, destms, elapsedms, vehiclemeter, actualdistance, farecharged, " +
            "\n phonedistance, phonemeter, phonefare, estimateddistance, estimatedfare";

  /*  public static final String _ID = "_id";
    public static final String TRIP_IDTEXT = "tripidtext";
    public static final String START_DATETIME = "startdatetime";
    public static final String START_ADDR = "startaddr";
    public static final String DEST_ADDR = "destaddr";
    public static final String STARTMS = "startms";
    public static final String DESTMS = "destms";*/


    public DBHelperLocalAutoTaxi(Context cxt, PackageInfo pinfo, String anameshort, UserInfo uinfo) {
        super(cxt, pinfo, anameshort, uinfo);
        DB_NAME = anameshort.toLowerCase(Locale.ENGLISH) + "_local.jet";
        DB_PATH = cxt.getApplicationInfo().dataDir + "/";
        this.context = cxt;

    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (dbExist) {
            Log.d(TAG,"do nothing - database already exists");
            //do nothing - database already exists
        } else {
            //By calling this method an empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {
                ProgressDialog progressDialog = ProgressDialog.show(context,
                        "Please wait", "Initializing Application ...", true);
                copyDataBase();
                progressDialog.dismiss();
            } catch (IOException e) {
                throw new Error("SSA:DBHelperLocal.createDatabase - Error copying database: " + e.getMessage());
            }
        }
    }


    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        final String dbVersion = mVersionInfo.versionName + mVersionInfo.versionCode;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(cContext);
        String currentDbVersion = prefs.getString("dbVersionInfo", "-1");
        if (!currentDbVersion.equals(dbVersion)) {
            return false;
        }

        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        } catch (SQLiteException e) {
            Log.d(TAG, e.getMessage());
            //database does't exist yet.

        }

        if (checkDB != null) {

            checkDB.close();

        }

        return checkDB != null ? true : false;

    }

    private void copyDataBase() throws IOException {

        //Open your local db as the input stream
        InputStream myInput = context.getAssets().open(DB_NAME);

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
        } catch (Exception e) {
            SSLog.e(TAG, " copyDataBase - ", e.getMessage());

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
        String dbVersion = mVersionInfo.versionName + mVersionInfo.versionCode;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(cContext);
        Editor editpref = prefs.edit();
        editpref.putString("dbVersionInfo", dbVersion);
        editpref.commit();
    }

    public void openDataBase(PackageInfo versionInfo, Context context, CGlobals_db app) throws SQLException {
        cContext = context;
        mVersionInfo = versionInfo;


        boolean dbExist = checkDataBase();
        if (dbExist) {
            Log.d(TAG,"do nothing - database already exist");
            //do nothing - database already exist
        } else {
            Log.d(TAG,"if the database does not exist, copy it from the assets folder");
            // if the database does not exist, copy it from the assets folder
            try {
                copyDataBase();
            } catch (Exception e) {
                throw new Error("Error copying database: " + e.getMessage());
            }
        }

        //Open the database
        String myPath = DB_PATH + DB_NAME;
        try {
            if (mDB != null) {
                mDB.close();
                mDB = null;
            }
        } catch (Exception e) {
            SSLog.e(TAG, " openDatabase - ", e.getMessage());
        }
        if (mDB == null)
            mDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE
                    | SQLiteDatabase.NO_LOCALIZED_COLLATORS);

    } // openDataBase

    @Override
    public synchronized void close() {
        if (mDB != null)
            mDB.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SSLog.i(TAG, "Upgrading");
    }

    public CTrip saveTrip(CTrip ctrip) {
        long nRowsInserted = -1;
        try {
            nRowsInserted = mDB.insert("usertrip", null, ctrip.mRowValues);
        } catch (Exception e) {
            SSLog.e(TAG, " saveTrip - ", e.getMessage());
        }
        //SSLog.i(TAG , "saveTrip: inserted - ", Long.toString(nRowsInserted));
        if (nRowsInserted > 1) {
            ctrip._id = getTripId(ctrip.mlStartMs);
            return ctrip;
        } else
            return null;
    } // saveTrip

    public void updateTrip(CTrip ctrip, int id) {
        try {
            long nRowsUpdated = -1;
            try {
                nRowsUpdated = mDB.update("usertrip", ctrip.mRowValues, "_id =" + id, null);
            } catch (Exception e) {
                SSLog.e(TAG, "updateTrip - ", e.getMessage());
            }
            SSLog.i(TAG + "updateTrip: updated - ", Long.toString(nRowsUpdated));
        } catch (Exception e) {
            SSLog.e(TAG, "updateTrip: error - ", e.getMessage());
        }
    } // updateTrip


    public int getTripId(long iStartMs) {
        Cursor curs;
        String rsql = SELECT_USERTRIP + "\n FROM usertrip WHERE startms = " + iStartMs;
        try {
            curs = mDB.rawQuery(rsql, null);
            curs.moveToFirst();
            if (!curs.isAfterLast()) {
                return curs.getInt(curs.getColumnIndexOrThrow("_id"));
            }
        } catch (Exception e) {
            SSLog.e(TAG, "getTripId - ", e.getMessage());
        }

        return -1;
    }

    public void deleteTrip(int iTripId) {
        try {
            long nRowsDeleted = -1;
            try {
                nRowsDeleted = mDB.delete("usertrip", "_id =" + iTripId, null);
            } catch (Exception e) {
                SSLog.e(TAG, "deleteTrip", e.getMessage());
            }
            SSLog.i(TAG + "deleteTrip: deleted - ", Long.toString(nRowsDeleted));
        } catch (Exception e) {
            SSLog.e(TAG, "deleteTrip error - ", e.getMessage());
        }
    }



    public Cursor getDBCallHome() {
        Cursor curs = null;
        String rsql = "SELECT " + COLUMN_CALLHOME + " FROM callhome ";

        try {
            curs = RunQuery(rsql, null);
            if (curs != null) {
                curs.moveToFirst();
                return curs;

            } else
                return null;
        } catch (Exception e) {
            SSLog.e(TAG, "DBHelper - getDBCallHome()", e.getMessage());
            if (curs != null)
                curs.close();
            return null;
        }
    }    // getDBCallHome

    public int DBClearCallHome() {
//		String rsql = "DELETE FROM callhome "; 

        try {
            int nRowsDeleted = mDB.delete("callhome", "1", null);
            SSLog.i(TAG + "DBHelper - DBClearHome() - Deleted: ", Integer.toString(nRowsDeleted));
/*			Cursor c = ATFDb.rawQuery(rsql, null);
			if(c != null) {
				c.close();
				c = null;
			}
*/
        } catch (Exception e) {
            SSLog.e(TAG, "DBHelper - getDBCallHome()", e.getMessage());
            return 0;
        }
        return 1;
    }
/*
    protected Cursor RunQuery(String sQuery) {
        return RunQuery(sQuery, null);
    }*/

    // Runs a general query and returns a cursor
    protected Cursor RunQuery(String sQuery, String[] sParams) {
        Cursor cCursor;
        try {
            cCursor = mDB.rawQuery(sQuery, sParams);
            if (cCursor != null) {
                int nRows = cCursor.getCount();
                if (nRows > 0) {
                    cCursor.moveToFirst();
                    return cCursor;
                } else
                    return null;
            }
        } catch (SQLException e) {
            SSLog.e(TAG, ".RunQuery: " + e.toString(), e);
            throw e;
        } catch (Exception e) {
            SSLog.e(TAG, ".RunQuery: " + e.toString(), e);
            throw e;
        }
        SSLog.e(TAG,
                ".RunQuery: " + sQuery + " - " + " returned null", sQuery);
        return null;
    } // RunQuery

} // class DBHelper Local
	
	