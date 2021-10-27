package com.jumpinjumpout;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lib.app.util.SSLog_SS;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper: ";

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 7;

    // Database Name
    private static final String DATABASE_NAME = "DriverDB";

    //  table name
    private static final String TABLE_DRIVER_PROFILE = "driver_profile";


    Context context;


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DRIVER_PROFILE_TABLE = "CREATE TABLE " + TABLE_DRIVER_PROFILE + "(\n" +
                "driver_profile_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "unique_key  TEXT(30)," +
                "clientdatetime  TEXT," +
                "driver_firstname  TEXT(30)," +
                "driver_lastname  TEXT(30)," +
                "driver_dob  TEXT(20)," +
                "driver_email  TEXT(50)," +
                "driver_phoneno  TEXT(15) NOT NULL UNIQUE," +
                "driver_gender  TEXT(10)," +
                "driver_pancard  TEXT(12)," +
                "driver_aadhar  TEXT(20)," +
                "driver_license_no  TEXT(255)," +
                "driver_permit_flag  TEXT(2)," +
                "driver_permit_no  TEXT(255)," +
                "driver_badge_flag  TEXT(2)," +
                "driver_badge_no  TEXT(255)," +
                "driver_incity_since  TEXT(4)," +
                "cab_needed_flag  TEXT(2)," +
                "driver_vehicle_no  TEXT(255)," +
                "remark  TEXT(500)," +
                "internal_comment  TEXT(500)," +
                "address_id  int(11) DEFAULT NULL," +
                "google_address  TEXT(250) DEFAULT NULL," +
                "t_address_1  TEXT(200) DEFAULT NULL," +
                "t_address_2  TEXT(200) DEFAULT NULL," +
                "t_city_town  TEXT(30) DEFAULT NULL," +
                "t_landmark  TEXT(50) DEFAULT NULL," +
                "t_state TEXT(20) DEFAULT NULL," +
                "t_pincode  int(10) DEFAULT NULL," +
                "p_address_1  TEXT(200) DEFAULT NULL," +
                "p_address_2 TEXT(200) DEFAULT NULL," +
                "p_city_town  TEXT(30) DEFAULT NULL," +
                "p_landmark  TEXT(50) DEFAULT NULL," +
                "p_state  TEXT(20) DEFAULT NULL," +
                "p_pincode  int(10) DEFAULT NULL," +
                "other_company_code  TEXT(10) DEFAULT NULL," +
                "driver_other_company_id  TEXT(20) DEFAULT NULL," +
                "driver_image_name  TEXT(50) DEFAULT NULL," +
                "driver_image_path  TEXT(255) DEFAULT NULL," +
                "driver_image  BLOB," +
                "driver_image_datettime  TEXT(255)," +
                "pancard_image_name  TEXT(50) DEFAULT NULL," +
                "pancard_image_path  TEXT(255) DEFAULT NULL," +
                "pancard_image  BLOB," +
                "pancard_image_datettime  TEXT(255)," +
                "license_image_name  TEXT(50) DEFAULT NULL," +
                "license_image_path  TEXT(255) DEFAULT NULL," +
                "license_image  BLOB," +
                "license_image_datetime  TEXT(255)," +
                "aadhar_image_name  TEXT(50) DEFAULT NULL," +
                "aadhar_image_path  TEXT(255) DEFAULT NULL," +
                "aadhar_image  BLOB," +
                "aadhar_image_datetime  TEXT(255)," +
                "last_modified_datetime  TEXT(255)," +
                "sent_to_server_datetime  TEXT(255)," +
                "sent_to_server_flag  TEXT(2)," +
                "other_company_name  TEXT(255)," +
                "uniqueid  TEXT(255), " +
                "payment_amount  TEXT(11) DEFAULT NULL, " +
                "payment_status  TEXT(2), " +
                "receipt_no  TEXT(255) DEFAULT NULL, " +
                "receipt_date  TEXT DEFAULT NULL," +
                "referral_walk_in TEXT DEFAULT NULL," +
                "referral_supervisor TEXT DEFAULT NULL," +
                "referral_driver TEXT DEFAULT NULL," +
                "receipt_no_2 TEXT DEFAULT NULL," +
                "receipt_no_3 TEXT DEFAULT NULL," +
                "driver_backout_reason TEXT DEFAULT NULL," +
                "private_verification TEXT DEFAULT '0'," +
                "police_verification TEXT DEFAULT '0'," +
                "tr_license_exp_date TEXT(255),"+
                "receipt_date_2 TEXT(255),"+
                "receipt_date_3 TEXT(255)"+
                ");";


        db.execSQL(CREATE_DRIVER_PROFILE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        ///  it will remove all rows
        if (oldVersion < DATABASE_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DRIVER_PROFILE);
            onCreate(db);
        }


    }

    public long insertDriverProfile(HashMap<String, String> hmp, byte[] baDriver,
                                    byte[] baPan, byte[] baAadhar, byte[] baLicence) {
        SQLiteDatabase mDriverDb = this.getWritableDatabase();
        ContentValues rowValues = null;
        try {
            if (mDriverDb == null)
                return -1;
            rowValues = new ContentValues();
            String column = "", value = "";
            Set set = hmp.entrySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                column = me.getKey().toString();
                value = me.getValue().toString();
                if (!TextUtils.isEmpty(value))
                    rowValues.put(column, value);
            }



            if (baDriver != null)
                rowValues.put("driver_image", baDriver);
            if (baPan != null)
                rowValues.put("pancard_image", baPan);
            if (baAadhar != null)
                rowValues.put("aadhar_image", baAadhar);
            if (baLicence != null)
                rowValues.put("license_image", baLicence);

        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "insertDriverId", e, context);
        }
        return mDriverDb.insert(TABLE_DRIVER_PROFILE, null, rowValues);
    }

    public long insertDriverProfile(HashMap<String, String> hmp) {
        SQLiteDatabase mDriverDb = this.getWritableDatabase();
        ContentValues rowValues = null;
        try {
            if (mDriverDb == null)
                return -1;
            rowValues = new ContentValues();
            String column = "", value = "";
            Set set = hmp.entrySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                column = me.getKey().toString();
                value = me.getValue().toString();
                if (!TextUtils.isEmpty(value))
                    rowValues.put(column, value);
            }


        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "insertDriverId", e, context);
        }
        return mDriverDb.insert(TABLE_DRIVER_PROFILE, null, rowValues);
    }

    public int updateDriverProfile(String phoneno, HashMap<String, String> hmp, byte[] baDriver,
                                   byte[] baPan, byte[] baAadhar, byte[] baLicence) {
        SQLiteDatabase mDriverDb = this.getWritableDatabase();
        ContentValues rowValues = null;
        try {
            if (mDriverDb == null)
                return 0;
//            mDriverDb = this.getWritableDatabase();
            rowValues = new ContentValues();
            String column = "", value = "";
            Set set = hmp.entrySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                column = me.getKey().toString();
                value = me.getValue().toString();
                if (!TextUtils.isEmpty(value))
                    rowValues.put(column, value);
            }

            if (baDriver != null)
                rowValues.put("driver_image", baDriver);
            if (baPan != null)
                rowValues.put("pancard_image", baPan);
            if (baAadhar != null)
                rowValues.put("aadhar_image", baAadhar);
            if (baLicence != null)
                rowValues.put("license_image", baLicence);

        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "insertDriverId", e, context);
        }

        return mDriverDb.update(TABLE_DRIVER_PROFILE, rowValues, "driver_phoneno" + " = ?",
                new String[]{String.valueOf(phoneno)});
    }

    public int updateDriverProfile(String phoneno, String column, byte[] ba) {
        SQLiteDatabase mDriverDb = this.getWritableDatabase();
        ContentValues rowValues = null;
        try {
            if (mDriverDb == null)
                return 0;
//            mDriverDb = this.getWritableDatabase();
            rowValues = new ContentValues();


            if (ba != null)
                rowValues.put(column, ba);


        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "insertDriverId", e, context);
        }

        return mDriverDb.update(TABLE_DRIVER_PROFILE, rowValues, "driver_phoneno" + " = ?",
                new String[]{String.valueOf(phoneno)});
    }

    public int updateDriverProfile(String phoneno, String senttoserver) {
        SQLiteDatabase mDriverDb = this.getWritableDatabase();
        ContentValues rowValues = null;
        try {
            if (mDriverDb == null)
                return 0;
//            mDriverDb = this.getWritableDatabase();
            rowValues = new ContentValues();
            rowValues.put("sent_to_server_flag", senttoserver);

        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "insertDriverId", e, context);
        }

        return mDriverDb.update(TABLE_DRIVER_PROFILE, rowValues, "driver_phoneno" + " = ?",
                new String[]{String.valueOf(phoneno)});
    }


    public ArrayList<DriverInfo> getDriverProfile() {
        ArrayList<DriverInfo> maList = new ArrayList<>();
        SQLiteDatabase mDriverDb = this.getReadableDatabase();
        try {
            if (mDriverDb == null)
                return null;
            Cursor curs;
            String rsql = " SELECT * FROM " + TABLE_DRIVER_PROFILE + " WHERE driver_firstname IS NOT NULL " +
                    "AND driver_lastname IS NOT NULL " +
                    "AND driver_phoneno IS NOT NULL ORDER BY unique_key desc";

            curs = mDriverDb.rawQuery(rsql, null);
            SSLog_SS.i("getConnections: ", rsql);
            DriverInfo mDriverInfo;
            if (curs != null) {
                if (curs.moveToFirst()) {
                    do {
                        mDriverInfo = getObj(curs);
                        maList.add(mDriverInfo);
                    } while (curs.moveToNext());
                }
                if (curs != null)
                    curs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "insertDriverId", e, context);
        }
        return maList;
    }

    public ArrayList<DriverInfo> getDriverProfileList() {
        ArrayList<DriverInfo> maList = new ArrayList<>();
        SQLiteDatabase mDriverDb = this.getReadableDatabase();

        try {
            if (mDriverDb == null)
                return null;
            Cursor curs;
            String rsql = " SELECT * FROM " + TABLE_DRIVER_PROFILE + " WHERE driver_firstname IS NOT NULL " +
                    "AND driver_lastname IS NOT NULL \n" +
                    "AND driver_phoneno IS NOT NULL \n" +
                    "AND sent_to_server_flag == '0' \n" +
                    "ORDER BY driver_profile_id ";

            curs = mDriverDb.rawQuery(rsql, null);
            SSLog_SS.i("getConnections: ", rsql);
            DriverInfo mDriverInfo = null;
            if (curs != null) {
                if (curs.moveToFirst()) {
                    do {

                        mDriverInfo = getObj(curs);
                        maList.add(mDriverInfo);
                    } while (curs.moveToNext());
                }
                if (curs != null)
                    curs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "insertDriverId", e, context);
        }
        return maList;
    }

    private DriverInfo getObj(Cursor curs) {
        String driver_profile_id, unique_key, clientdatetime, driver_firstname, driver_lastname,
                driver_dob, driver_email, driver_phoneno, driver_gender, driver_pancard,
                driver_aadhar, driver_license_no, driver_permit_flag, driver_permit_no,
                driver_badge_flag, driver_badge_no, driver_incity_since, cab_needed_flag,
                driver_vehicle_no, remark, internal_comment, address_id, google_address,
                t_address_1, t_address_2, t_city_town, t_landmark, t_state, t_pincode,
                p_address_1, p_address_2, p_city_town, p_landmark, p_state, p_pincode, other_company_code,
                driver_other_company_id, driver_image_name, driver_image_path,
                driver_image_datettime, pancard_image_name, pancard_image_path, pancard_image_datettime,
                license_image_name, license_image_path, license_image_datetime, aadhar_image_name,
                aadhar_image_path, aadhar_image_datetime, last_modified_datetime, sent_to_server_datetime,
                sent_to_server_flag, other_company_name, uniqueid, payment_amount, payment_status,
                receipt_no, receipt_date, referral_walk_in, referral_supervisor, referral_driver, receipt_no_2,
                receipt_no_3, driver_backout_reason, private_verification, police_verification,tr_license_exp_date,
                receipt_date_2,receipt_date_3;
        byte[] driver_image, aadhar_image, pancard_image, license_image;
        DriverInfo mDriverInfo = null;
        driver_profile_id = curs.getString(curs.getColumnIndexOrThrow("driver_profile_id"));
        unique_key = curs.getString(curs.getColumnIndexOrThrow("unique_key"));
        clientdatetime = curs.getString(curs.getColumnIndexOrThrow("clientdatetime"));
        driver_firstname = curs.getString(curs.getColumnIndexOrThrow("driver_firstname"));
        driver_lastname = curs.getString(curs.getColumnIndexOrThrow("driver_lastname"));
        driver_dob = curs.getString(curs.getColumnIndexOrThrow("driver_dob"));
        driver_email = curs.getString(curs.getColumnIndexOrThrow("driver_email"));
        driver_phoneno = curs.getString(curs.getColumnIndexOrThrow("driver_phoneno"));
        driver_gender = curs.getString(curs.getColumnIndexOrThrow("driver_gender"));
        driver_pancard = curs.getString(curs.getColumnIndexOrThrow("driver_pancard"));
        driver_aadhar = curs.getString(curs.getColumnIndexOrThrow("driver_aadhar"));
        driver_license_no = curs.getString(curs.getColumnIndexOrThrow("driver_license_no"));
        driver_permit_flag = curs.getString(curs.getColumnIndexOrThrow("driver_permit_flag"));
        driver_permit_no = curs.getString(curs.getColumnIndexOrThrow("driver_permit_no"));
        driver_badge_flag = curs.getString(curs.getColumnIndexOrThrow("driver_badge_flag"));
        driver_badge_no = curs.getString(curs.getColumnIndexOrThrow("driver_badge_no"));
        driver_incity_since = curs.getString(curs.getColumnIndexOrThrow("driver_incity_since"));
        cab_needed_flag = curs.getString(curs.getColumnIndexOrThrow("cab_needed_flag"));
        driver_vehicle_no = curs.getString(curs.getColumnIndexOrThrow("driver_vehicle_no"));
        remark = curs.getString(curs.getColumnIndexOrThrow("remark"));
        internal_comment = curs.getString(curs.getColumnIndexOrThrow("internal_comment"));
        address_id = curs.getString(curs.getColumnIndexOrThrow("address_id"));
        google_address = curs.getString(curs.getColumnIndexOrThrow("google_address"));
        t_address_1 = curs.getString(curs.getColumnIndexOrThrow("t_address_1"));
        t_address_2 = curs.getString(curs.getColumnIndexOrThrow("t_address_2"));
        t_city_town = curs.getString(curs.getColumnIndexOrThrow("t_city_town"));
        t_landmark = curs.getString(curs.getColumnIndexOrThrow("t_landmark"));
        t_state = curs.getString(curs.getColumnIndexOrThrow("t_state"));
        t_pincode = curs.getString(curs.getColumnIndexOrThrow("t_pincode"));
        p_address_1 = curs.getString(curs.getColumnIndexOrThrow("p_address_1"));
        p_address_2 = curs.getString(curs.getColumnIndexOrThrow("p_address_2"));
        p_city_town = curs.getString(curs.getColumnIndexOrThrow("p_city_town"));
        p_landmark = curs.getString(curs.getColumnIndexOrThrow("p_landmark"));
        p_state = curs.getString(curs.getColumnIndexOrThrow("p_state"));
        p_pincode = curs.getString(curs.getColumnIndexOrThrow("p_pincode"));
        other_company_code = curs.getString(curs.getColumnIndexOrThrow("other_company_code"));
        driver_other_company_id = curs.getString(curs.getColumnIndexOrThrow("driver_other_company_id"));
        driver_image_name = curs.getString(curs.getColumnIndexOrThrow("driver_image_name"));
        driver_image_path = curs.getString(curs.getColumnIndexOrThrow("driver_image_path"));
        driver_image_datettime = curs.getString(curs.getColumnIndexOrThrow("driver_image_datettime"));
        pancard_image_name = curs.getString(curs.getColumnIndexOrThrow("pancard_image_name"));
        pancard_image_path = curs.getString(curs.getColumnIndexOrThrow("pancard_image_path"));
        pancard_image_datettime = curs.getString(curs.getColumnIndexOrThrow("pancard_image_datettime"));
        license_image_name = curs.getString(curs.getColumnIndexOrThrow("license_image_name"));
        license_image_path = curs.getString(curs.getColumnIndexOrThrow("license_image_path"));
        license_image_datetime = curs.getString(curs.getColumnIndexOrThrow("license_image_datetime"));
        aadhar_image_name = curs.getString(curs.getColumnIndexOrThrow("aadhar_image_name"));
        aadhar_image_path = curs.getString(curs.getColumnIndexOrThrow("aadhar_image_path"));
        aadhar_image_datetime = curs.getString(curs.getColumnIndexOrThrow("aadhar_image_datetime"));
        last_modified_datetime = curs.getString(curs.getColumnIndexOrThrow("last_modified_datetime"));
        sent_to_server_datetime = curs.getString(curs.getColumnIndexOrThrow("sent_to_server_datetime"));
        sent_to_server_flag = curs.getString(curs.getColumnIndexOrThrow("sent_to_server_flag"));
        other_company_name = curs.getString(curs.getColumnIndexOrThrow("other_company_name"));
        driver_image = curs.getBlob(curs.getColumnIndexOrThrow("driver_image"));
        pancard_image = curs.getBlob(curs.getColumnIndexOrThrow("pancard_image"));
        aadhar_image = curs.getBlob(curs.getColumnIndexOrThrow("aadhar_image"));
        license_image = curs.getBlob(curs.getColumnIndexOrThrow("license_image"));
        uniqueid = curs.getString(curs.getColumnIndexOrThrow("uniqueid"));
        payment_amount = curs.getString(curs.getColumnIndexOrThrow("payment_amount"));
        payment_status = curs.getString(curs.getColumnIndexOrThrow("payment_status"));
        receipt_no = curs.getString(curs.getColumnIndexOrThrow("receipt_no"));
        receipt_date = curs.getString(curs.getColumnIndexOrThrow("receipt_date"));
        referral_walk_in = curs.getString(curs.getColumnIndexOrThrow("referral_walk_in"));
        referral_supervisor = curs.getString(curs.getColumnIndexOrThrow("referral_supervisor"));
        referral_driver = curs.getString(curs.getColumnIndexOrThrow("referral_driver"));
        receipt_no_2 = curs.getString(curs.getColumnIndexOrThrow("receipt_no_2"));
        receipt_no_3 = curs.getString(curs.getColumnIndexOrThrow("receipt_no_3"));
        driver_backout_reason = curs.getString(curs.getColumnIndexOrThrow("driver_backout_reason"));
        private_verification = curs.getString(curs.getColumnIndexOrThrow("private_verification"));
        police_verification = curs.getString(curs.getColumnIndexOrThrow("police_verification"));
        tr_license_exp_date = curs.getString(curs.getColumnIndexOrThrow("tr_license_exp_date"));
        receipt_date_2 = curs.getString(curs.getColumnIndexOrThrow("receipt_date_2"));
        receipt_date_3 = curs.getString(curs.getColumnIndexOrThrow("receipt_date_3"));
        mDriverInfo = new DriverInfo(driver_profile_id, unique_key,
                clientdatetime, driver_firstname,
                driver_lastname, driver_dob, driver_email,
                driver_phoneno, driver_gender,
                driver_pancard, driver_aadhar,
                driver_license_no, driver_permit_flag,
                driver_permit_no, driver_badge_flag,
                driver_badge_no, driver_incity_since,
                cab_needed_flag, driver_vehicle_no,
                remark, internal_comment, address_id,
                google_address, t_address_1, t_address_2,
                t_city_town, t_landmark, t_state,
                t_pincode, p_address_1, p_address_2,
                p_city_town, p_landmark, p_state,
                p_pincode, other_company_code,
                driver_other_company_id, driver_image_name,
                driver_image_path, driver_image, driver_image_datettime,
                pancard_image_name, pancard_image_path, pancard_image,
                pancard_image_datettime, license_image_name,
                license_image_path, license_image, license_image_datetime,
                aadhar_image_name, aadhar_image_path, aadhar_image,
                aadhar_image_datetime, last_modified_datetime, sent_to_server_datetime,
                sent_to_server_flag, other_company_name, uniqueid, payment_amount,
                payment_status, receipt_no, receipt_date, referral_walk_in,
                referral_supervisor, referral_driver, receipt_no_2,
                receipt_no_3, driver_backout_reason, private_verification, police_verification,
                tr_license_exp_date,receipt_date_2,receipt_date_3);
        return mDriverInfo;
    }


    public ArrayList<DriverInfo> getDriverProfile(String phoneno) {
        ArrayList<DriverInfo> maList = new ArrayList<>();
        String driver_profile_id, unique_key, clientdatetime, driver_firstname, driver_lastname,
                driver_dob, driver_email, driver_phoneno, driver_gender, driver_pancard,
                driver_aadhar, driver_license_no, driver_permit_flag, driver_permit_no,
                driver_badge_flag, driver_badge_no, driver_incity_since, cab_needed_flag,
                driver_vehicle_no, remark, internal_comment, address_id, google_address,
                t_address_1, t_address_2, t_city_town, t_landmark, t_state, t_pincode,
                p_address_1, p_address_2, p_city_town, p_landmark, p_state, p_pincode, other_company_code,
                driver_other_company_id, driver_image_name, driver_image_path,
                driver_image_datettime, pancard_image_name, pancard_image_path, pancard_image_datettime,
                license_image_name, license_image_path, license_image_datetime, aadhar_image_name,
                aadhar_image_path, aadhar_image_datetime, last_modified_datetime, sent_to_server_datetime,
                sent_to_server_flag, other_company_name, uniqueid, payment_amount, payment_status,
                receipt_no, receipt_date, referral_walk_in, referral_supervisor, referral_driver, receipt_no_2,
                receipt_no_3, driver_backout_reason, private_verification, police_verification,tr_license_exp_date,
                receipt_date_2,receipt_date_3;
        byte[] driver_image, aadhar_image, pancard_image, license_image;
        try {
            SQLiteDatabase mDriverDb = this.getReadableDatabase();
            if (mDriverDb == null)
                return null;
            Cursor curs;
            String rsql = " SELECT * FROM " + TABLE_DRIVER_PROFILE + " WHERE driver_phoneno = " + phoneno;

            curs = mDriverDb.rawQuery(rsql, null);
            SSLog_SS.i("getConnections: ", rsql);
            DriverInfo mDriverInfo;
            if (curs != null) {
                curs.moveToFirst();
                driver_profile_id = curs.getString(curs.getColumnIndexOrThrow("driver_profile_id"));
                unique_key = curs.getString(curs.getColumnIndexOrThrow("unique_key"));
                clientdatetime = curs.getString(curs.getColumnIndexOrThrow("clientdatetime"));
                driver_firstname = curs.getString(curs.getColumnIndexOrThrow("driver_firstname"));
                driver_lastname = curs.getString(curs.getColumnIndexOrThrow("driver_lastname"));
                driver_dob = curs.getString(curs.getColumnIndexOrThrow("driver_dob"));
                driver_email = curs.getString(curs.getColumnIndexOrThrow("driver_email"));
                driver_phoneno = curs.getString(curs.getColumnIndexOrThrow("driver_phoneno"));
                driver_gender = curs.getString(curs.getColumnIndexOrThrow("driver_gender"));
                driver_pancard = curs.getString(curs.getColumnIndexOrThrow("driver_pancard"));
                driver_aadhar = curs.getString(curs.getColumnIndexOrThrow("driver_aadhar"));
                driver_license_no = curs.getString(curs.getColumnIndexOrThrow("driver_license_no"));
                driver_permit_flag = curs.getString(curs.getColumnIndexOrThrow("driver_permit_flag"));
                driver_permit_no = curs.getString(curs.getColumnIndexOrThrow("driver_permit_no"));
                driver_badge_flag = curs.getString(curs.getColumnIndexOrThrow("driver_badge_flag"));
                driver_badge_no = curs.getString(curs.getColumnIndexOrThrow("driver_badge_no"));
                driver_incity_since = curs.getString(curs.getColumnIndexOrThrow("driver_incity_since"));
                cab_needed_flag = curs.getString(curs.getColumnIndexOrThrow("cab_needed_flag"));
                driver_vehicle_no = curs.getString(curs.getColumnIndexOrThrow("driver_vehicle_no"));
                remark = curs.getString(curs.getColumnIndexOrThrow("remark"));
                internal_comment = curs.getString(curs.getColumnIndexOrThrow("internal_comment"));
                address_id = curs.getString(curs.getColumnIndexOrThrow("address_id"));
                google_address = curs.getString(curs.getColumnIndexOrThrow("google_address"));
                t_address_1 = curs.getString(curs.getColumnIndexOrThrow("t_address_1"));
                t_address_2 = curs.getString(curs.getColumnIndexOrThrow("t_address_2"));
                t_city_town = curs.getString(curs.getColumnIndexOrThrow("t_city_town"));
                t_landmark = curs.getString(curs.getColumnIndexOrThrow("t_landmark"));
                t_state = curs.getString(curs.getColumnIndexOrThrow("t_state"));
                t_pincode = curs.getString(curs.getColumnIndexOrThrow("t_pincode"));
                p_address_1 = curs.getString(curs.getColumnIndexOrThrow("p_address_1"));
                p_address_2 = curs.getString(curs.getColumnIndexOrThrow("p_address_2"));
                p_city_town = curs.getString(curs.getColumnIndexOrThrow("p_city_town"));
                p_landmark = curs.getString(curs.getColumnIndexOrThrow("p_landmark"));
                p_state = curs.getString(curs.getColumnIndexOrThrow("p_state"));
                p_pincode = curs.getString(curs.getColumnIndexOrThrow("p_pincode"));
                other_company_code = curs.getString(curs.getColumnIndexOrThrow("other_company_code"));
                driver_other_company_id = curs.getString(curs.getColumnIndexOrThrow("driver_other_company_id"));
                driver_image_name = curs.getString(curs.getColumnIndexOrThrow("driver_image_name"));
                driver_image_path = curs.getString(curs.getColumnIndexOrThrow("driver_image_path"));
                driver_image_datettime = curs.getString(curs.getColumnIndexOrThrow("driver_image_datettime"));
                pancard_image_name = curs.getString(curs.getColumnIndexOrThrow("pancard_image_name"));
                pancard_image_path = curs.getString(curs.getColumnIndexOrThrow("pancard_image_path"));
                pancard_image_datettime = curs.getString(curs.getColumnIndexOrThrow("pancard_image_datettime"));
                license_image_name = curs.getString(curs.getColumnIndexOrThrow("license_image_name"));
                license_image_path = curs.getString(curs.getColumnIndexOrThrow("license_image_path"));
                license_image_datetime = curs.getString(curs.getColumnIndexOrThrow("license_image_datetime"));
                aadhar_image_name = curs.getString(curs.getColumnIndexOrThrow("aadhar_image_name"));
                aadhar_image_path = curs.getString(curs.getColumnIndexOrThrow("aadhar_image_path"));
                aadhar_image_datetime = curs.getString(curs.getColumnIndexOrThrow("aadhar_image_datetime"));
                last_modified_datetime = curs.getString(curs.getColumnIndexOrThrow("last_modified_datetime"));
                sent_to_server_datetime = curs.getString(curs.getColumnIndexOrThrow("sent_to_server_datetime"));
                sent_to_server_flag = curs.getString(curs.getColumnIndexOrThrow("sent_to_server_flag"));
                other_company_name = curs.getString(curs.getColumnIndexOrThrow("other_company_name"));
                driver_image = curs.getBlob(curs.getColumnIndexOrThrow("driver_image"));
                pancard_image = curs.getBlob(curs.getColumnIndexOrThrow("pancard_image"));
                aadhar_image = curs.getBlob(curs.getColumnIndexOrThrow("aadhar_image"));
                license_image = curs.getBlob(curs.getColumnIndexOrThrow("license_image"));
                uniqueid = curs.getString(curs.getColumnIndexOrThrow("uniqueid"));
                payment_amount = curs.getString(curs.getColumnIndexOrThrow("payment_amount"));
                payment_status = curs.getString(curs.getColumnIndexOrThrow("payment_status"));
                receipt_no = curs.getString(curs.getColumnIndexOrThrow("receipt_no"));
                receipt_date = curs.getString(curs.getColumnIndexOrThrow("receipt_date"));
                referral_walk_in = curs.getString(curs.getColumnIndexOrThrow("referral_walk_in"));
                referral_supervisor = curs.getString(curs.getColumnIndexOrThrow("referral_supervisor"));
                referral_driver = curs.getString(curs.getColumnIndexOrThrow("referral_driver"));
                receipt_no_2 = curs.getString(curs.getColumnIndexOrThrow("receipt_no_2"));
                receipt_no_3 = curs.getString(curs.getColumnIndexOrThrow("receipt_no_3"));
                driver_backout_reason = curs.getString(curs.getColumnIndexOrThrow("driver_backout_reason"));
                private_verification = curs.getString(curs.getColumnIndexOrThrow("private_verification"));
                police_verification = curs.getString(curs.getColumnIndexOrThrow("police_verification"));
                tr_license_exp_date = curs.getString(curs.getColumnIndexOrThrow("tr_license_exp_date"));
                receipt_date_2 = curs.getString(curs.getColumnIndexOrThrow("receipt_date_2"));
                receipt_date_3 = curs.getString(curs.getColumnIndexOrThrow("receipt_date_3"));
                mDriverInfo = new DriverInfo(driver_profile_id, unique_key,
                        clientdatetime, driver_firstname,
                        driver_lastname, driver_dob, driver_email,
                        driver_phoneno, driver_gender,
                        driver_pancard, driver_aadhar,
                        driver_license_no, driver_permit_flag,
                        driver_permit_no, driver_badge_flag,
                        driver_badge_no, driver_incity_since,
                        cab_needed_flag, driver_vehicle_no,
                        remark, internal_comment, address_id,
                        google_address, t_address_1, t_address_2,
                        t_city_town, t_landmark, t_state,
                        t_pincode, p_address_1, p_address_2,
                        p_city_town, p_landmark, p_state,
                        p_pincode, other_company_code,
                        driver_other_company_id, driver_image_name,
                        driver_image_path, driver_image, driver_image_datettime,
                        pancard_image_name, pancard_image_path, pancard_image,
                        pancard_image_datettime, license_image_name,
                        license_image_path, license_image, license_image_datetime,
                        aadhar_image_name, aadhar_image_path, aadhar_image,
                        aadhar_image_datetime, last_modified_datetime, sent_to_server_datetime,
                        sent_to_server_flag, other_company_name, uniqueid, payment_amount,
                        payment_status, receipt_no, receipt_date, referral_walk_in,
                        referral_supervisor, referral_driver, receipt_no_2,
                        receipt_no_3, driver_backout_reason, private_verification, police_verification,
                        tr_license_exp_date, receipt_date_2,receipt_date_3);
                maList.add(mDriverInfo);


                if (curs != null)
                    curs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "get driver profile", e, context);
        }
        return maList;
    }

}