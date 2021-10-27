package lib.app.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DBHelperLocal extends SQLiteOpenHelper {
    //The Android's default system path of your application database.
    private static String DB_PATH;
    private static String DB_NAME;
    protected SQLiteDatabase mDB;
    private final Context context;
    private Activity mActivity;
    private String TAG = "DBLocal: ";
    public static String
            COLUMN_CALLHOME = "\n _id, date, dt, tm, clientms, email, imei, module, lat, lon, accuracy, locationtime, " +
            "\n provider, version, app, carrier, product, manufacturer, content";

    public static final String _ID = "_id";
    UserInfo userInfo;

    /**
     * Constructor
     * Takes and keeps a reference of the passed mContext in order to access to the application assets and resources.
     *
     * @param uinfo
     * @param mContext
     */
    PackageInfo packageInfo;
    String appNameShort;
    private long mCallHomeLastRowId;
    private boolean mCallHomeToBeCleared;

    public DBHelperLocal(Context cxt, PackageInfo pinfo, String anameshort, UserInfo uinfo) {
        super(cxt, DB_NAME, null, 1);
        packageInfo = pinfo;
        appNameShort = anameshort;
        userInfo = uinfo;
        DB_PATH = cxt.getApplicationInfo().dataDir + "/";
        DB_NAME = anameshort.toLowerCase(Locale.ENGLISH) + "_local.jet";
        this.context = cxt;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (dbExist) {
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

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
        if (mDB != null)
            return true;
        SQLiteDatabase checkDB = null;
        final String dbVersion = packageInfo.versionName + packageInfo.versionCode;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String currentDbVersion = prefs.getString("dbVersionInfo", "-1");
        if (!currentDbVersion.equals(dbVersion)) {
            return false;
        }
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        } catch (SQLiteException e) {
            Log.d("DBHelperLocal: ", e.getMessage());
            //database does't exist yet.
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
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
        } catch (FileNotFoundException e) {
          //  SSLog.e("DBHelper", "copyDataBase): ", e.getMessage());
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
    }

    public void openDataBase(PackageInfo versionInfo, Activity activity) throws SQLException {
        mActivity = activity;
        boolean dbExist = checkDataBase();
        if (dbExist) {
            //do nothing - database already exist
        } else {
            // if the database does not exist, copy it from the assets folder
            try {
                copyDataBase();
            } catch (IOException e) {
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
            if (mDB == null)
                mDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE
                        | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        } catch (Exception e) {
            Log.e(TAG + "openDatabase - ", e.getMessage());
        }

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
    }

    public void writeDBCallHome(String smodule, String sContent, String slat, String slon,
                                String sacc, String sloctime, String sprovider) {
        Calendar cal = Calendar.getInstance();
        String sLastClientms = "";
//		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        try {
            ContentValues rowValues = new ContentValues();
//			rowValues.put("date", df.format(cal.getTime()));
            Date cDate = new Date();
            String dt = new SimpleDateFormat("yyyy-MM-dd", Locale.UK).format(cDate);
            String tm = new SimpleDateFormat("HH:mm:ss", Locale.UK).format(cDate);
            rowValues.put("dt", dt);
            rowValues.put("tm", tm);
            rowValues.put("clientms", Long.toString(cal.getTimeInMillis()));
            rowValues.put("email", UserInfo.mGmail);
            rowValues.put("imei", UserInfo.mIMEI);
            rowValues.put("module", smodule);
            rowValues.put("content", sContent);
            rowValues.put("lat", slat);
            rowValues.put("lon", slon);
            rowValues.put("accuracy", sacc);
            rowValues.put("locationtime", sloctime);
            rowValues.put("provider", sprovider);
            rowValues.put("version", packageInfo.versionCode);
            rowValues.put("app", appNameShort);
            rowValues.put("carrier", userInfo.msCarrier);
            rowValues.put("product", userInfo.msProduct);
            rowValues.put("manufacturer", userInfo.msManufacturer);
            try {
                String clientMs = Long.toString(cal.getTimeInMillis());
                if (!clientMs.equals(sLastClientms)) {
                    sLastClientms = Long.toString(cal.getTimeInMillis());
                    mCallHomeLastRowId = mDB.insert("callhome", null, rowValues);

                }
            } catch (SQLException e) {
                //SSLog.e(TAG, "writeDBCallHome - ", e.getMessage());
            }
            //SSLog.i(TAG, "Callhome: inserted - " + Long.toString(mCallHomeLastRowId));
//			Cursor cursor = getDBCallHome();
        } catch (Exception e) {
           // SSLog.e(TAG, "writeDBCallHome", e.getMessage());
        }

    } // writeDBCallHome

    /*
        public int deleteCallHomeLocal(int callHomeId) {
    //		mTrainDB.rawQuery("delete from table_name where _id=" + callHomeId, null);
            int deletedRows = mTrainDB.delete("callhome", " _id = " + callHomeId, null);
            return deletedRows;
        }
    */
    public Cursor getDBCallHome() {
        Cursor curs = null;
        String rsql = "SELECT " + COLUMN_CALLHOME + " FROM callhome ";

        if (mCallHomeToBeCleared)
            rsql += " WHERE _id > " + mCallHomeLastRowId;

        try {
            curs = mDB.rawQuery(rsql, null);
           // SSLog.i(TAG, "getDBCallHome - " + rsql);
            if (curs != null) {
                curs.moveToFirst();
                return curs;
            } else
                return null;
        } catch (Exception e) {
            Log.e(TAG, "getDBCallHome - " + e.getMessage());
            if (curs != null)
                curs.close();
            return null;
        }
    }    // getDBCallHome

    public int DBClearCallHome() {
        try {
            mCallHomeToBeCleared = true;
            int nRowsDeleted = mDB.delete("callhome", "1", null);
            mCallHomeToBeCleared = false;
          //  SSLog.i(TAG, "Deleted: " + Integer.toString(nRowsDeleted));
        } catch (Exception e) {
          //  SSLog.e(TAG, "ClearCallHome - ", e.getMessage());
            return 0;
        }
        return 1;
    }


} // class DBHelper Local

	