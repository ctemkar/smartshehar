package com.smartshehar.busdriver_apk;

/**
 * Created by user pc on 24-07-2015.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "ssb.jet";
    private SQLiteDatabase db;
    private final Context context;
    private String DB_PATH;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
        DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
    }

    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();
        if (dbExist) {

        } else {
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {

        InputStream myInput = context.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public ArrayList<String> getRouteNo() {
        String myPath = DB_PATH + DB_NAME;
        db = SQLiteDatabase.openDatabase(myPath, null,
                SQLiteDatabase.OPEN_READWRITE);
        ArrayList<String> resultList = new ArrayList<String>();

        Cursor c = db.rawQuery("SELECT buslabel from ba_routedetail GROUP BY routecode", null);
        while (c.moveToNext()) {
            String date = c.getString(c.getColumnIndex("buslabel"));
            try {
                resultList.add(date);
            } catch (Exception e) {
            }
        }
        c.close();
        db.close();
        return resultList;
    }

    public ArrayList<BusDriver_Result> getStartBus(String sRouteNo, String sdirection) {
        BusDriver_Result sr;
        String myPath = DB_PATH + DB_NAME;
        db = SQLiteDatabase.openDatabase(myPath, null,
                SQLiteDatabase.OPEN_READWRITE);
        ArrayList<BusDriver_Result> resultList = new ArrayList<BusDriver_Result>();

        Cursor c = db.rawQuery("SELECT stopname, stopserial, lat, lon from ba_routedetail WHERE buslabel = '" + sRouteNo + "' AND direction = '" + sdirection + "'", null);
        while (c.moveToNext()) {
            String stopname = c.getString(c.getColumnIndex("stopname"));
            String stopserial = c.getString(c.getColumnIndex("stopserial"));
            double lat = c.getDouble(c.getColumnIndex("lat"));
            double lon = c.getDouble(c.getColumnIndex("lon"));
            try {
                sr = new BusDriver_Result();
                sr.setStopname(stopname);
                sr.setStopserial(stopserial);
                sr.setLat(lat);
                sr.setLon(lon);
                resultList.add(sr);
            } catch (Exception e) {
            }
        }
        c.close();
        db.close();
        return resultList;
    }

    public String startBusName(String sRouteNo) {
        BusDriver_Result sr;
        String stopname = "";
        String myPath = DB_PATH + DB_NAME;
        db = SQLiteDatabase.openDatabase(myPath, null,
                SQLiteDatabase.OPEN_READWRITE);
        Cursor c = db.rawQuery("SELECT stopname, stopserial, lat, lon from ba_routedetail WHERE buslabel = '"
                + sRouteNo + "' AND direction = 'U' ORDER BY stopserial DESC LIMIT 1", null);
        while (c.moveToNext()) {
            stopname = c.getString(c.getColumnIndex("stopname"));
        }
        c.close();
        db.close();
        return stopname;
    }

    public String destinationBusName(String sRouteNo) {
        BusDriver_Result sr;
        String stopname = "";
        String myPath = DB_PATH + DB_NAME;
        db = SQLiteDatabase.openDatabase(myPath, null,
                SQLiteDatabase.OPEN_READWRITE);
        Cursor c = db.rawQuery("SELECT stopname, stopserial, lat, lon from ba_routedetail WHERE buslabel = '" +
                sRouteNo + "' AND direction = 'D' ORDER BY stopserial DESC LIMIT 1", null);
        while (c.moveToNext()) {
            stopname = c.getString(c.getColumnIndex("stopname"));
        }
        c.close();
        db.close();
        return stopname;
    }

    public ArrayList<BusDriver_Result> getDestinationBus(String sRouteNo, String direction) {
        BusDriver_Result sr;
        String myPath = DB_PATH + DB_NAME;
        db = SQLiteDatabase.openDatabase(myPath, null,
                SQLiteDatabase.OPEN_READWRITE);
        ArrayList<BusDriver_Result> resultList = new ArrayList<BusDriver_Result>();

        Cursor c = db.rawQuery("SELECT stopname, stopserial, lat, lon from ba_routedetail WHERE buslabel = \'" + sRouteNo + "\' AND direction = '" + direction + "' ORDER BY stopserial DESC", null);
        while (c.moveToNext()) {

            String stopname = c.getString(c.getColumnIndex("stopname"));
            String stopserial = c.getString(c.getColumnIndex("stopserial"));
            double lat = c.getDouble(c.getColumnIndex("lat"));
            double lon = c.getDouble(c.getColumnIndex("lon"));

            try {
                sr = new BusDriver_Result();
                sr.setStopname(stopname);
                sr.setStopserial(stopserial);
                sr.setLat(lat);
                sr.setLon(lon);
                resultList.add(sr);
            } catch (Exception e) {
            }
        }
        c.close();
        db.close();
        return resultList;
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }


}