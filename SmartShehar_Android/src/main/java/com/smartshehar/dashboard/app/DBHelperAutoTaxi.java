package com.smartshehar.dashboard.app;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;

public class DBHelperAutoTaxi extends SQLiteAssetHelper {
	public static final String TAG = "DBHelperAutoTaxi: ";

	public static String SELECT_FARE = 
        " SELECT vehiclefareparameter_id _id, city, vehicletype, metertype, minimumdistance, minimumfare, minimumnightfare, fareperkm, metermovesperkm,\n " + 
       		"meterrounding, minimumwaitingminutes, waitchargeperhour, nightextra, distance_unit, \n" +
       		"luggagechargeperpiece, nightstart, nightend,  transportdescription, vehicletypedescription \n " +
       		"FROM rs_vwcityfareparameter \n";
	public static String SELECT_CARD = 
	        " SELECT _id, distance, newdayfare, newnightfare, meter, olddayfare, oldnightfare"; 


   // private static String DB_PATH;
    private static String DB_NAME = "ssa.jet";
    private SQLiteDatabase mDB;
    private Context mContext;
    

    private PackageInfo mVersionInfo;


    public DBHelperAutoTaxi(Context context) {
    	super(context, DB_NAME, null, 3);
        this.mContext = context;
       // DB_PATH = context.getApplicationInfo().dataDir + "/";
		setForcedUpgrade();
    }	

   /* private boolean checkDataBase(){
    	SQLiteDatabase checkDB = null;
		final String dbVersion = mVersionInfo.versionName + mVersionInfo.versionCode;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String currentDbVersion = prefs.getString("dbVersionInfo", "-1");
        if(!currentDbVersion.equals(dbVersion)) {
        	return false;
        }
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE|SQLiteDatabase.NO_LOCALIZED_COLLATORS);
 
    	}catch(SQLiteException e){

    		Log.d(TAG, e.getMessage());
    		//database does't exist yet.
    	}
    	if(checkDB != null){
    		checkDB.close();
    	}
    	return checkDB != null ? true : false;
    }*/

   /* private void copyDataBase() throws IOException{
 
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
    	try	{
    		myOutput = new FileOutputStream(outFileName);
    	} catch (FileNotFoundException e) {
    		SSLog.e(TAG,"DBHelper.copyDataBase): ",  e.getMessage());
    		throw(e);
    	}
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    	// Update the db version on phone
		String dbVersion = mVersionInfo.versionName + mVersionInfo.versionCode;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Editor editpref = prefs.edit();
        editpref.putString("dbVersionInfo", dbVersion);
        editpref.commit();
    }*/
 
    public void openDataBase(PackageInfo versionInfo, Context context) throws SQLException{
		mContext = context;
    	mVersionInfo = versionInfo;
      // 	boolean dbExist = checkDataBase();
    	/*if(dbExist){
    		//do nothing - database already exist
    	}else{
    		// if the database does not exist, copy it from the assets folder
    		try {
    			copyDataBase();
    		} catch (Exception e) {
        		throw new Error("Error copying database: " + e.getMessage());
        	}
    	}*/
    	//Open the database
        String myPath = /*DB_PATH +*/ DB_NAME;
        try {
	        if(mDB != null) {
	        	mDB.close();
	        	mDB = null;
	        }else{
				mDB = getReadableDatabase();
			}
        } catch(Exception e) {
        	SSLog.e(TAG,"DBHelper: openDatabase - ", e.getMessage());
        }
        if(mDB == null)
         	mDB = getReadableDatabase();
 
    } // openDataBase
 
    @Override
	public synchronized void close() {
	    if(mDB != null)
		    mDB.close();
	    super.close();
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
	}


	// Runs a general query and returns a cursor
	protected Cursor RunQuery(String sQuery, String[] sParams)	{
    	Cursor cCursor;
        try {
        	cCursor = mDB.rawQuery(sQuery, sParams);
        	return cCursor;
        }	catch (SQLException e) {
                SSLog.e(TAG, ".RunQuery: " + e.toString(),e);
                throw e;
        }catch (Exception e) {
			SSLog.e(TAG, ".RunQuery: " + e.toString(),e);
			throw e;
		}
    } // RunQuery

    public ArrayList<String> getCityList() {
		Cursor curs ;
		ArrayList<String> aCityList = new ArrayList<String>();
		String sCity;
//		String rsql = "SELECT " + COLUMN_SEARCHSTR + " AS searchstr FROM ta_station ORDER BY UPPER(stationname)";
		String rsql = "SELECT DISTINCT city FROM rs_vwcityfareparameter \n";
		try {
	        curs = RunQuery(rsql, null);
			SSLog.i(TAG + "getCityList: ", rsql);

			if(curs != null) {
				for(curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {
					sCity = curs.getString(curs.getColumnIndexOrThrow("city"));
					if(sCity != null)
						aCityList.add(sCity);
				}
				curs.close();
				curs = null;
			}
		} catch(Exception SQLException) {
			SSLog.e(TAG , "getCityList - ", SQLException.getMessage());
		}
		return aCityList;
    } // getCityList

 	public ArrayList<CFareParams> getCityFareParameters(String sCity) {
		Cursor curs = null;
		ArrayList<CFareParams> aoCfp = new ArrayList<CFareParams>();
		CFareParams oCfp;
		String rsql = SELECT_FARE + " \nWHERE city = upper('" + sCity + "')" +
				"\nORDER BY serialno";
		try {
		    curs = RunQuery(rsql, null);
		    while (curs.moveToNext()) {
				oCfp = new CFareParams(sCity, 
					curs.getString(curs.getColumnIndexOrThrow("vehicletype")));
				oCfp.sCity = sCity;
				oCfp.msVehicleType = curs.getString(curs.getColumnIndexOrThrow("vehicletype"));
				oCfp.msMeterType = curs.getString(curs.getColumnIndexOrThrow("metertype"));
				oCfp.iMinimumDistance = curs.getInt(curs.getColumnIndexOrThrow("minimumdistance"));
				oCfp.fMinimumFare = curs.getFloat(curs.getColumnIndexOrThrow("minimumfare"));				
				oCfp.fMinimumNightFare = curs.getFloat(curs.getColumnIndexOrThrow("minimumnightfare"));				
				oCfp.fFarePerKm = curs.getFloat(curs.getColumnIndexOrThrow("fareperkm"));
				oCfp.fMeterMovesPerKm = curs.getFloat(curs.getColumnIndexOrThrow("metermovesperkm"));
				oCfp.fMeterRounding = curs.getFloat(curs.getColumnIndexOrThrow("meterrounding"));
				oCfp.iMinimumWaitingMinutes = curs.getInt(curs.getColumnIndexOrThrow("minimumwaitingminutes"));
				oCfp.iWaitChargePerHour = curs.getInt(curs.getColumnIndexOrThrow("waitchargeperhour"));
				oCfp.fNightExtra = curs.getFloat(curs.getColumnIndexOrThrow("nightextra"));
				oCfp.fNightStart = curs.getFloat(curs.getColumnIndexOrThrow("nightstart"));
				oCfp.fNightEnd = curs.getFloat(curs.getColumnIndexOrThrow("nightend"));
				oCfp.iLuggageChargePerPiece = curs.getInt(curs.getColumnIndexOrThrow("luggagechargeperpiece"));
				oCfp.msTransportDescription = curs.getString(curs.getColumnIndexOrThrow("transportdescription"));
				oCfp.msVehicleTypeDescription = curs.getString(curs.getColumnIndexOrThrow("vehicletypedescription"));
				oCfp.msDistanceUnit = curs.getString(curs.getColumnIndexOrThrow("distance_unit"));
				aoCfp.add(oCfp);
			}
		} catch (Exception e){
			SSLog.e(TAG , "getCityFareParam()", e.getMessage());
			if(curs != null)
				curs.close();
			return null;
		}
		if(curs != null)
			curs.close();
		return aoCfp;
	}	// getFareParameters   
    
} // class DBHelper
