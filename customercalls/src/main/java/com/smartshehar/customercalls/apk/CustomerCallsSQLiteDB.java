package com.smartshehar.customercalls.apk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by ctemkar on 22/04/2016.
 * Holds the customer call local tables that will be synced to server
 */

public class CustomerCallsSQLiteDB extends SQLiteOpenHelper {

    private static final String TAG = "CustomerCallsSQLiteDB: ";
    // Database definition
    private static final int DATABASE_VERSION = 21;
    private static final String DATABASE_NAME = "CustomerCalls.jet";
    public static final String CUSTOMER_TABLE_NAME = "customer";
    public static final String CUSTOMER_COLUMN_ID = "_id";
    public static final String CUSTOMER_COLUMN_ID_LOCAL = "customer_id_local";
    public static final String CUSTOMER_CALL_ID = "customer_calls_id";
    public static final String CUSTOMER_COLUMN_FIRST_NAME = "first_name";
    public static final String CUSTOMER_COLUMN_LAST_NAME = "last_name";
    public static final String CUSTOMER_COLUMN_COMPANY = "company";
    public static final String CUSTOMER_COLUMN_COUNTRY_CODE = "country_code";
    public static final String CUSTOMER_COLUMN_PHONE = "phone";
    public static final String CUSTOMER_COLUMN_FLAT = "flat";
    public static final String CUSTOMER_COLUMN_WING = "wing";
    public static final String CUSTOMER_COLUMN_FLOOR = "floor";
    public static final String CUSTOMER_COLUMN_COMPLEX = "complex";
    public static final String CUSTOMER_COLUMN_BUILDING = "building";
    public static final String CUSTOMER_COLUMN_AREA = "area";
    public static final String CUSTOMER_COLUMN_ROAD = "road";
    public static final String CUSTOMER_COLUMN_LANDMARK_1 = "landmark_1";
    public static final String CUSTOMER_COLUMN_LANDMARK_2 = "landmark_2";
    public static final String CUSTOMER_COLUMN_CITY = "city";
    public static final String CUSTOMER_COLUMN_STATE = "state";
    public static final String CUSTOMER_COLUMN_COUNTRY = "country";
    public static final String CUSTOMER_COLUMN_POSTAL_CODE = "postal_code";
    public static final String CUSTOMER_COLUMN_CREATED = "created";
    public static final String CUSTOMER_COLUMN_LAST_CALL_DATETIME = "last_call";
    public static final String CUSTOMER_COLUMN_LAST_UPDATE = "last_updated";
    public static final String CUSTOMER_VIRTUAL_COLUMN_ADDRESS = "address";
    public static final String CUSTOMER_COLUMN_IS_DELETED = "is_deleted";
    public static final String CUSTOMER_COLUMN_IS_IGNORED = "is_ignored";
    public static final String CUSTOMER_SENT_TO_SERVER_DATETIME = "sent_to_server_datetime";
    // Order summary table
    public static final String ORDER_HEADER_TABLE_NAME = "order_header";
    public static final String ORDER_HEADER_COLUMN_ID = "_id";
    public static final String ORDER_HEADER_COLUMN_STORE_PHONE_ID = "store_phone_id";
    public static final String ORDER_HEADER_COLUMN_PHONE_NO = "phone";
    public static final String ORDER_HEADER_COLUMN_COUNTRY_CODE = "country_code";
    public static final String ORDER_HEADER_COLUMN_DATE = "order_date";
    public static final String ORDER_HEADER_COLUMN_AMOUNT = "amount";
    public static final String ORDER_HEADER_COLUMN_SENT_TO_SERVER = "sent_to_server";


    // Customer call log
    public static final String CUSTOMER_CALL_LOG_TABLE_NAME = "customer_call_log";
    public static final String CUSTOMER_CALL_LOG_ID = "_id";
    public static final String CUSTOMER_CALL_LOG_ID_SERVER = "call_log_id";
    public static final String CUSTOMER_CALL_LOG_CALL_DATE_TIME = "call_datetime";
    public static final String CUSTOMER_CALL_LOG_PHONE = "call_phoneno";
    public static final String CUSTOMER_CALL_LOG_CALL_HANDLED = "call_handled";
    public static final String CUSTOMER_CALL_LOG_STORE_PHONE_ID = "store_phone_id";
    public static final String CUSTOMER_CALL_LOG_COUNTRY_CODE = "country_code";
    public static final String CUSTOMER_CALL_LOG_SENT_TO_SERVER_DATETIME = "sent_to_server_datetime";


    private SQLiteDatabase mDB;

    public static final int HIDE_IGNORE_DELETE = 1;
    public static final String ORDER_HEADER_TOTAL_AMOUNT = "totalamount";
    public static final String ORDER_HEADER_TOTAL_ORDER = "totalorder";

    public CustomerCallsSQLiteDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mDB = getWritableDatabase();
    }

    /*Override this function to create a new table*/
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CUSTOMER_TABLE = "CREATE TABLE " + CUSTOMER_TABLE_NAME + "("
                + CUSTOMER_COLUMN_ID_LOCAL + " INTEGER PRIMARY KEY," + CUSTOMER_COLUMN_ID + " INT,"
                + CUSTOMER_COLUMN_FIRST_NAME + " TEXT,"
                + CUSTOMER_COLUMN_LAST_NAME + " TEXT, " + CUSTOMER_COLUMN_COMPANY + " TEXT, "
                + CUSTOMER_COLUMN_COUNTRY_CODE + " TEXT, " + CUSTOMER_COLUMN_PHONE + " TEXT, "
                + CUSTOMER_COLUMN_FLAT + " TEXT, " + CUSTOMER_COLUMN_WING + " TEXT, "
                + CUSTOMER_COLUMN_FLOOR + " TEXT, " + CUSTOMER_COLUMN_COMPLEX + " TEXT, "
                + CUSTOMER_COLUMN_BUILDING + " TEXT, " + CUSTOMER_COLUMN_AREA + " TEXT, "
                + CUSTOMER_COLUMN_ROAD + " TEXT, " + CUSTOMER_COLUMN_LANDMARK_1 + " TEXT, "
                + CUSTOMER_COLUMN_LANDMARK_2 + " TEXT, " + CUSTOMER_COLUMN_CITY + " TEXT, "
                + CUSTOMER_COLUMN_STATE + " TEXT, " + CUSTOMER_COLUMN_COUNTRY + " TEXT, "
                + CUSTOMER_COLUMN_POSTAL_CODE + " TEXT, " + CUSTOMER_COLUMN_CREATED + " INT, "
                + CUSTOMER_COLUMN_LAST_CALL_DATETIME + " INT, " + CUSTOMER_COLUMN_LAST_UPDATE + " INT, "
                + CUSTOMER_COLUMN_IS_DELETED + " INT, " + CUSTOMER_COLUMN_IS_IGNORED + " INT, "
                + CUSTOMER_CALL_ID + " INT, " + CUSTOMER_SENT_TO_SERVER_DATETIME + " INT DEFAULT 0 "
                + ")";
        db.execSQL(CREATE_CUSTOMER_TABLE);
        db.execSQL("CREATE UNIQUE INDEX idx_cc_phone ON " + CUSTOMER_TABLE_NAME + "(" +
                CUSTOMER_COLUMN_COUNTRY_CODE + "," + CUSTOMER_COLUMN_PHONE + ")");

        String CREATE_ORDER_HEADER_TABLE = "CREATE TABLE " + ORDER_HEADER_TABLE_NAME + "("
                + ORDER_HEADER_COLUMN_ID + " INTEGER PRIMARY KEY,"
                + ORDER_HEADER_COLUMN_STORE_PHONE_ID + " INT, "
                + ORDER_HEADER_COLUMN_PHONE_NO + " TEXT, "
                + ORDER_HEADER_COLUMN_COUNTRY_CODE + " TEXT,"
                + ORDER_HEADER_COLUMN_DATE + " INT, "
                + ORDER_HEADER_COLUMN_AMOUNT + " INT, "
                + CUSTOMER_CALL_ID + " INT, "
                + ORDER_HEADER_COLUMN_SENT_TO_SERVER + " INT DEFAULT 0 "
                + ")";
        db.execSQL(CREATE_ORDER_HEADER_TABLE);
        String CREATE_CALL_LOG_TABLE = "CREATE TABLE " + CUSTOMER_CALL_LOG_TABLE_NAME + "("
                + CUSTOMER_CALL_LOG_ID + " INTEGER PRIMARY KEY,"
                + CUSTOMER_CALL_LOG_ID_SERVER + " INT,"
                + CUSTOMER_CALL_LOG_CALL_DATE_TIME + " INT, "
                + CUSTOMER_CALL_LOG_PHONE + " TEXT,  "
                + CUSTOMER_CALL_LOG_COUNTRY_CODE + " TEXT, "
                + CUSTOMER_CALL_LOG_CALL_HANDLED + " INT, "
                + CUSTOMER_CALL_LOG_STORE_PHONE_ID + " INT, "
                + CUSTOMER_CALL_LOG_SENT_TO_SERVER_DATETIME + " INT DEFAULT 0 "
                + ")";
        db.execSQL(CREATE_CALL_LOG_TABLE);


    }

    /*Override this function to upgrade your table design / structure*/
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop the old table if exists
        db.execSQL("DROP TABLE IF EXISTS " + CUSTOMER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ORDER_HEADER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CUSTOMER_CALL_LOG_TABLE_NAME);
        // Create tables again
        onCreate(db);
    }

    public Cursor getAllCallLog() {
        String sWhereHide = "";
       /* switch (hideRows) {
            case HIDE_IGNORE_DELETE:
                sWhereHide = " AND ((" + CUSTOMER_COLUMN_IS_IGNORED + " IS NULL OR " +
                        CUSTOMER_COLUMN_IS_IGNORED + "= 0) AND (" +
                        CUSTOMER_COLUMN_IS_DELETED + " IS NULL OR " +
                        CUSTOMER_COLUMN_IS_DELETED + " = 0))";
                break;

        }*/
        Cursor rows = null;
        try {
            String sql = "SELECT c." + CUSTOMER_COLUMN_ID + ", " + CUSTOMER_COLUMN_FIRST_NAME
                    + ", c." + CUSTOMER_COLUMN_PHONE + ", " + CUSTOMER_CALL_ID + ", " +
                    "\nCASE WHEN LENGTH(" + CUSTOMER_COLUMN_FLAT + ") > 0 THEN " +
                    CUSTOMER_COLUMN_FLAT + " || ' ' ELSE '' END || " +
                    "\nCASE WHEN LENGTH(" + CUSTOMER_COLUMN_BUILDING + ") > 0 THEN " +
                    CUSTOMER_COLUMN_BUILDING + " || ' ' ELSE '' END || " +
                    "\nCASE WHEN LENGTH(" + CUSTOMER_COLUMN_ROAD + ") > 0 THEN " +
                    CUSTOMER_COLUMN_ROAD + "  ELSE '' END " + CUSTOMER_VIRTUAL_COLUMN_ADDRESS + ", " +
                    CUSTOMER_COLUMN_LANDMARK_1 + ", " +
                    "\nCASE WHEN " + CUSTOMER_CALL_LOG_CALL_DATE_TIME + " <0 THEN '' ELSE " +
                    "\nCASE WHEN date(" + CUSTOMER_CALL_LOG_CALL_DATE_TIME +
                    "/ 1000, 'unixepoch', 'localtime') = date('now')" +
                    " THEN strftime ('%H:%M', time(" + CUSTOMER_CALL_LOG_CALL_DATE_TIME +
                    " / 1000, 'unixepoch', 'localtime'))" +
                    " ELSE datetime (" + CUSTOMER_CALL_LOG_CALL_DATE_TIME + "/ 1000, 'unixepoch', 'localtime') END END " + CUSTOMER_CALL_LOG_CALL_DATE_TIME + ", " +
                    "\n(SELECT SUM(" + ORDER_HEADER_COLUMN_AMOUNT + ") FROM " + ORDER_HEADER_TABLE_NAME +
                    "\nWHERE " + ORDER_HEADER_COLUMN_COUNTRY_CODE + " = c." + CUSTOMER_COLUMN_COUNTRY_CODE + " AND " +
                    "\n" + ORDER_HEADER_COLUMN_PHONE_NO + " = c." + CUSTOMER_COLUMN_PHONE + " GROUP BY " + ORDER_HEADER_COLUMN_PHONE_NO + ", " + ORDER_HEADER_COLUMN_COUNTRY_CODE + ") AS " + ORDER_HEADER_TOTAL_AMOUNT + ", " +
                    "\n(SELECT COUNT(" + ORDER_HEADER_COLUMN_AMOUNT + ") FROM " + ORDER_HEADER_TABLE_NAME +
                    "\nWHERE " + ORDER_HEADER_COLUMN_COUNTRY_CODE + " = c." + CUSTOMER_COLUMN_COUNTRY_CODE + " AND " +
                    "\n" + ORDER_HEADER_COLUMN_PHONE_NO + " = c." + CUSTOMER_COLUMN_PHONE + " GROUP BY " + ORDER_HEADER_COLUMN_PHONE_NO + ", " + ORDER_HEADER_COLUMN_COUNTRY_CODE + ") AS " + ORDER_HEADER_TOTAL_ORDER +
                    ",\n(SELECT " + ORDER_HEADER_COLUMN_AMOUNT + " FROM " + ORDER_HEADER_TABLE_NAME + " WHERE " + CUSTOMER_CALL_ID + " = l." + CUSTOMER_CALL_LOG_ID + ") AS " + ORDER_HEADER_COLUMN_AMOUNT +
                    " \nFROM " + CUSTOMER_TABLE_NAME + " c INNER JOIN " + CUSTOMER_CALL_LOG_TABLE_NAME + " l " +
                    "\nON c." + CUSTOMER_COLUMN_PHONE + " = l." + CUSTOMER_CALL_LOG_PHONE + " AND " +
                    "\nc." + CUSTOMER_COLUMN_COUNTRY_CODE + " = l." + CUSTOMER_CALL_LOG_COUNTRY_CODE +
                    "\n WHERE NOT ifnull(c." + CUSTOMER_COLUMN_PHONE + ", '') = '' " +
                    /*sWhereHide +*/
                    "\n ORDER BY l." + CUSTOMER_CALL_LOG_CALL_DATE_TIME + " DESC";
            rows = mDB.rawQuery(sql, null);
            Log.d(TAG, DatabaseUtils.dumpCursorToString(rows));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    /*addUser() will add a new User to database*/
    public void addCustomer(CCallInfo oCC) {

        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(oCC.getFirstName()))
            values.put(CUSTOMER_COLUMN_FIRST_NAME, oCC.getFirstName());
        if (!TextUtils.isEmpty(oCC.getLastName()))
            values.put(CUSTOMER_COLUMN_LAST_NAME, oCC.getLastName());
        if (!TextUtils.isEmpty(oCC.getCompany()))
            values.put(CUSTOMER_COLUMN_COMPANY, oCC.getCompany());
        if (!TextUtils.isEmpty(oCC.getCountryCode()))
            values.put(CUSTOMER_COLUMN_COUNTRY_CODE, oCC.getCountryCode());
        if (!TextUtils.isEmpty(oCC.getNationalNumber()))
            values.put(CUSTOMER_COLUMN_PHONE, oCC.getNationalNumber());
        if (!TextUtils.isEmpty(oCC.getFlatNo()))
            values.put(CUSTOMER_COLUMN_FLAT, oCC.getFlatNo());
        if (!TextUtils.isEmpty(oCC.getWing()))
            values.put(CUSTOMER_COLUMN_WING, oCC.getWing());
        if (!TextUtils.isEmpty(oCC.getFloor()))
            values.put(CUSTOMER_COLUMN_FLOOR, oCC.getFloor());
        if (!TextUtils.isEmpty(oCC.getComplex()))
            values.put(CUSTOMER_COLUMN_COMPLEX, oCC.getComplex());
        if (!TextUtils.isEmpty(oCC.getBulding()))
            values.put(CUSTOMER_COLUMN_BUILDING, oCC.getBulding());
        if (!TextUtils.isEmpty(oCC.getArea()))
            values.put(CUSTOMER_COLUMN_AREA, oCC.getArea());
        if (!TextUtils.isEmpty(oCC.getRoad()))
            values.put(CUSTOMER_COLUMN_ROAD, oCC.getRoad());
        if (!TextUtils.isEmpty(oCC.getLandmark1()))
            values.put(CUSTOMER_COLUMN_LANDMARK_1, oCC.getLandmark1());
        if (!TextUtils.isEmpty(oCC.getLandmark2()))
            values.put(CUSTOMER_COLUMN_LANDMARK_2, oCC.getLandmark2());
        if (!TextUtils.isEmpty(oCC.getCity()))
            values.put(CUSTOMER_COLUMN_CITY, oCC.getCity());
        if (!TextUtils.isEmpty(oCC.getState()))
            values.put(CUSTOMER_COLUMN_STATE, oCC.getState());
        if (!TextUtils.isEmpty(oCC.getCountry()))
            values.put(CUSTOMER_COLUMN_COUNTRY, oCC.getCountry());
        if (!TextUtils.isEmpty(oCC.getPostalCode()))
            values.put(CUSTOMER_COLUMN_POSTAL_CODE, oCC.getPostalCode());
        values.put(CUSTOMER_COLUMN_CREATED, oCC.getDateCreated());
        if (oCC.getDateLastCall() > 0)
            values.put(CUSTOMER_COLUMN_LAST_CALL_DATETIME, oCC.getDateLastCall());
        values.put(CUSTOMER_COLUMN_IS_DELETED, oCC.IsDeleted());
        values.put(CUSTOMER_COLUMN_IS_IGNORED, oCC.IsIgnore());
        values.put(CUSTOMER_COLUMN_LAST_UPDATE, oCC.getDateUpdated());
        if (oCC.getMiCustomerCallId() > 0)
            values.put(CUSTOMER_CALL_ID, oCC.getMiCustomerCallId());
        if (oCC.getCustomerId() > 0)
            values.put(CUSTOMER_COLUMN_ID, oCC.getCustomerId());
        values.put(CUSTOMER_COLUMN_LAST_UPDATE, oCC.getDateUpdated());

        long rowid = mDB.insertWithOnConflict(CUSTOMER_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        Log.d(TAG, "Rowid: " + rowid);
        if (rowid == -1) {
            String sWhere = CUSTOMER_COLUMN_COUNTRY_CODE + "='" + oCC.getCountryCode() +
                    "' AND " + CUSTOMER_COLUMN_PHONE + "='" + oCC.getNationalNumber() + "'";
            int rowsUpdated = mDB.update(CUSTOMER_TABLE_NAME, values, sWhere, null);
            Log.d(TAG, "Rows updated: " + rowsUpdated);
        }

    }

    public void addOrder(COrderHeader oCO) {

        ContentValues values = new ContentValues();
        values.put(ORDER_HEADER_COLUMN_PHONE_NO, oCO.getMsPhone());
        values.put(ORDER_HEADER_COLUMN_COUNTRY_CODE, oCO.getMsCountryCode());
        values.put(ORDER_HEADER_COLUMN_STORE_PHONE_ID, oCO.getMiStorePhoneId());
        values.put(ORDER_HEADER_COLUMN_DATE, oCO.getMiOrderDate());
        values.put(ORDER_HEADER_COLUMN_AMOUNT, oCO.getMiOrderAmount());
        values.put(CUSTOMER_CALL_ID, oCO.getMiCustomerCallId());

        long rowid = mDB.insertWithOnConflict(ORDER_HEADER_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        Log.d(TAG, "Rowid: " + rowid);
    }

    public long addToCallLog(CCallHandle oCH) {
        ContentValues values = new ContentValues();
        values.put(CUSTOMER_CALL_LOG_CALL_DATE_TIME, oCH.getMiCallLogDate());
        values.put(CUSTOMER_CALL_LOG_PHONE, oCH.getMsNationalNumber());
        values.put(CUSTOMER_CALL_LOG_COUNTRY_CODE, oCH.getMsCountryCode());
        values.put(CUSTOMER_CALL_LOG_STORE_PHONE_ID, oCH.getMiStorePhoneId());

        long rowid = mDB.insertWithOnConflict(CUSTOMER_CALL_LOG_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        Log.d(TAG, "Rowid: " + rowid);
        return rowid;
    }

    public CCallInfo getRowById(long id) {
        return getRow(" WHERE customer_id_local = " + id);
    }

    public CCallInfo getRowByPhone(String phone) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phone, Locale.getDefault().getCountry());
            if (phoneUtil.isValidNumber(numberProto)) {
                Log.d("TAG", "Country Code: " + numberProto.getCountryCode());
            } else {
                Log.d("TAG", "Invalid number format: " + phone);
            }
            String sWhere = " WHERE " + CUSTOMER_COLUMN_COUNTRY_CODE + " = " +
                    String.valueOf(numberProto.getCountryCode()) + " AND " +
                    CUSTOMER_COLUMN_PHONE + " = " +
                    String.valueOf(numberProto.getNationalNumber());
            return getRow(sWhere);
        } catch (NumberParseException e) {
            Log.d(TAG, "Unable to parse phoneNumber " + e.toString());
        }
        return null;
    }


    public CCallInfo getRow(String whr) {
        CCallInfo oCC = new CCallInfo();
        String sql = "SELECT " + CUSTOMER_COLUMN_ID_LOCAL + ", " + CUSTOMER_COLUMN_ID + ", " + CUSTOMER_CALL_ID + ", " +
                CUSTOMER_COLUMN_FIRST_NAME + ", " + CUSTOMER_COLUMN_COMPLEX + ", " +
                CUSTOMER_COLUMN_LAST_NAME + ", " + CUSTOMER_COLUMN_COMPANY + ", " +
                CUSTOMER_COLUMN_COUNTRY_CODE + ", " + CUSTOMER_COLUMN_PHONE + ", " +
                CUSTOMER_COLUMN_FLAT + ", " + CUSTOMER_COLUMN_WING + ", " +
                CUSTOMER_COLUMN_BUILDING + ", " + CUSTOMER_COLUMN_AREA + ", " +
                CUSTOMER_COLUMN_ROAD + ", " + CUSTOMER_COLUMN_LANDMARK_1 + ", " +
                CUSTOMER_COLUMN_LANDMARK_2 + ", " + CUSTOMER_COLUMN_STATE + ", " +
                CUSTOMER_COLUMN_COUNTRY + ", " + CUSTOMER_COLUMN_POSTAL_CODE + ", " +
                CUSTOMER_COLUMN_CREATED + ", " + CUSTOMER_COLUMN_LAST_CALL_DATETIME + ", " +
                CUSTOMER_COLUMN_LAST_UPDATE + ", " + CUSTOMER_COLUMN_FLOOR + ", " +
                CUSTOMER_COLUMN_IS_IGNORED + ", " + CUSTOMER_COLUMN_IS_DELETED +
                " FROM " + CUSTOMER_TABLE_NAME + " " + whr;

        Cursor cursor = mDB.rawQuery(sql, null);
        int iIgnored, iDeleted;
        if (cursor.moveToFirst()) {

            oCC.setCustomerId(cursor.getLong(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_ID)));
            oCC.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_FIRST_NAME)));
            oCC.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_LAST_NAME)));
            oCC.setCompany(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_COMPANY)));
            oCC.setCountryCode(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_COUNTRY_CODE)));
            oCC.setNationalNumber(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_PHONE)));
            oCC.setFlatNo(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_FLAT)));
            oCC.setWing(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_WING)));
            oCC.setBulding(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_BUILDING)));
            oCC.setArea(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_AREA)));
            oCC.setRoad(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_ROAD)));
            oCC.setLandmark1(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_LANDMARK_1)));
            oCC.setLandmark2(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_LANDMARK_2)));
            oCC.setState(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_STATE)));
            oCC.setCountry(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_COUNTRY)));
            oCC.setPostalCode(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_POSTAL_CODE)));
            oCC.setDateCreated(cursor.getLong(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_CREATED)));
            oCC.setDateLastCall(cursor.getLong(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_LAST_CALL_DATETIME)));
            oCC.setDateUpdated(cursor.getLong(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_LAST_UPDATE)));
            oCC.setMiCustomerCallId(cursor.getInt(cursor.getColumnIndex(CUSTOMER_CALL_ID)));
            oCC.setComplexOrColony(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_COMPLEX)));
            oCC.setFloor(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_FLOOR)));
            iIgnored = cursor.getInt(cursor.getColumnIndex(CUSTOMER_COLUMN_IS_IGNORED));
            if (iIgnored == 1)
                oCC.setIgnore(true);
            else oCC.setIgnore(false);
            iDeleted = cursor.getInt(cursor.getColumnIndex(CUSTOMER_COLUMN_IS_DELETED));
            if (iDeleted == 1)
                oCC.setDeleted(true);
            else oCC.setDeleted(false);

        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return oCC;
    }

    public CCallInfo getRowByNationalNo(String phone) {

        try {

            String sWhere = " WHERE " +
                    CUSTOMER_COLUMN_PHONE + " = " +
                    String.valueOf(phone);
            return getRow(sWhere);
        } catch (Exception e) {
            Log.d(TAG, "phoneNumber " + e.toString());
        }
        return null;
    }


    public void setIgnore(String phoneno, int val) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneno, Locale.getDefault().getCountry());
            if (phoneUtil.isValidNumber(numberProto)) {
                Log.d("TAG", "Country Code: " + numberProto.getCountryCode());
                setBooleanFlag(String.valueOf(numberProto.getNationalNumber()), String.valueOf(numberProto.getCountryCode()), CUSTOMER_COLUMN_IS_IGNORED, val, CUSTOMER_TABLE_NAME);
            } else {
                Log.d("TAG", "Invalid number format: " + phoneno);
            }
        } catch (NumberParseException e) {
            Log.d(TAG, "Unable to parse phoneNumber " + e.toString());
        }


    }
 /*   public void setDelete(String phoneno, String countrycode, int val) {
        setBooleanFlag(phoneno, countrycode,
                CUSTOMER_COLUMN_IS_DELETED, val, CUSTOMER_TABLE_NAME);


    }*/

    private void setBooleanFlag(String phoneno, String countrycode, String sFlag, int val, String sTable) {
        if (!TextUtils.isEmpty(phoneno)) {
            ContentValues values = new ContentValues();
            values.put(sFlag, val);
            String sWhere = CUSTOMER_COLUMN_PHONE + "=" + phoneno +
                    "\nAND " + CUSTOMER_COLUMN_COUNTRY_CODE + " = " + countrycode;
            int rowsUpdated = mDB.update(sTable, values, sWhere, null);
            Log.d(TAG, "Rows updated: " + rowsUpdated);
        }

    }

    public void setCallHandled(String phoneno, int val, long date) {
        if (!TextUtils.isEmpty(phoneno)) {
            ContentValues values = new ContentValues();
            values.put(CUSTOMER_CALL_LOG_CALL_HANDLED, val);
            String sWhere = CUSTOMER_CALL_LOG_PHONE + " = " + phoneno + " AND "
                    + CUSTOMER_CALL_LOG_CALL_DATE_TIME + " = " + date;
            int rowsUpdated = mDB.update(CUSTOMER_CALL_LOG_TABLE_NAME, values, sWhere, null);
            Log.d(TAG, "Rows updated: " + rowsUpdated);
        }

    }


    public void updateCustomer(String table, String column1, long val1,
                               String column2, int val2) {
        ContentValues values = new ContentValues();
        values.put(column1, val1);
        String sWhere = column2 + " = " + val2;
        int rowsUpdated = mDB.update(table, values, sWhere, null);
        Log.d(TAG, "Rows updated: " + rowsUpdated);
    }

    public void updateCallLog(String table, String column1, long val1, String column2,
                              int val2, String column3, int val3) {
        ContentValues values = new ContentValues();
        values.put(column1, val1);
        values.put(column2, val2);
        String sWhere = column3 + " = " + val3;
        int rowsUpdated = mDB.update(table, values, sWhere, null);
        Log.d(TAG, "Rows updated: " + rowsUpdated);
    }

    public void updateOrder(String table, String column1, long val1,
                            String column2, int val2) {
        ContentValues values = new ContentValues();
        values.put(column1, val1);
        String sWhere = column2 + " = " + val2;
        int rowsUpdated = mDB.update(table, values, sWhere, null);
        Log.d(TAG, "Rows updated: " + rowsUpdated);
    }


    public ArrayList<CCallHandle> getCallLog() {
        ArrayList<CCallHandle> maoCallHandles = new ArrayList<>();
        String sql = "SELECT " + CUSTOMER_CALL_LOG_ID + ", " + CUSTOMER_CALL_LOG_CALL_DATE_TIME + ", "
                + CUSTOMER_CALL_LOG_PHONE + ", " + CUSTOMER_CALL_LOG_CALL_HANDLED + ", "
                + CUSTOMER_CALL_LOG_STORE_PHONE_ID + ", " + CUSTOMER_CALL_LOG_COUNTRY_CODE
                + " FROM " + CUSTOMER_CALL_LOG_TABLE_NAME + " WHERE "
                + CUSTOMER_CALL_LOG_SENT_TO_SERVER_DATETIME + " = " + 0;
        Cursor cursor = mDB.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                CCallHandle oCH = new CCallHandle();
                oCH.setMiCallLogId(cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_CALL_LOG_ID)));
                oCH.setMiCallLogDate(cursor.getLong(cursor.getColumnIndexOrThrow(CUSTOMER_CALL_LOG_CALL_DATE_TIME)));
                oCH.setMsNationalNumber(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_CALL_LOG_PHONE)));
                oCH.setMsCountryCode(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_CALL_LOG_COUNTRY_CODE)));
                oCH.setMiStorePhoneId(cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_CALL_LOG_STORE_PHONE_ID)));
                oCH.setMiCallHandled(cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_CALL_LOG_CALL_HANDLED)));
                maoCallHandles.add(oCH);
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return maoCallHandles;
    }

    public Cursor getCustomer() {
        String sql = "SELECT c." + CUSTOMER_COLUMN_ID + ", " + CUSTOMER_COLUMN_FIRST_NAME
                + ", c." + CUSTOMER_COLUMN_PHONE + ", " +
                "\nCASE WHEN LENGTH(" + CUSTOMER_COLUMN_FLAT + ") > 0 THEN " +
                CUSTOMER_COLUMN_FLAT + " || ' ' ELSE '' END || " +
                "\nCASE WHEN LENGTH(" + CUSTOMER_COLUMN_BUILDING + ") > 0 THEN " +
                CUSTOMER_COLUMN_BUILDING + " || ' ' ELSE '' END || " +
                "\nCASE WHEN LENGTH(" + CUSTOMER_COLUMN_ROAD + ") > 0 THEN " +
                CUSTOMER_COLUMN_ROAD + "  ELSE '' END " + CUSTOMER_VIRTUAL_COLUMN_ADDRESS + ", " +
                CUSTOMER_COLUMN_LANDMARK_1 + ", " + ORDER_HEADER_COLUMN_AMOUNT + ", " +
                "\n" + ORDER_HEADER_COLUMN_DATE + " AS " + CUSTOMER_CALL_LOG_CALL_DATE_TIME + ", " +
                "\nSUM(" + ORDER_HEADER_COLUMN_AMOUNT + ")  AS " + ORDER_HEADER_TOTAL_AMOUNT + ", " +
                "\nCOUNT(" + ORDER_HEADER_COLUMN_AMOUNT + ") AS " + ORDER_HEADER_TOTAL_ORDER +
                " \nFROM " + CUSTOMER_TABLE_NAME + " c INNER JOIN " + ORDER_HEADER_TABLE_NAME + " o " +
                "\nON c." + CUSTOMER_COLUMN_PHONE + " = o." + ORDER_HEADER_COLUMN_PHONE_NO + " AND " +
                "\nc." + CUSTOMER_COLUMN_COUNTRY_CODE + " = o." + ORDER_HEADER_COLUMN_COUNTRY_CODE +
                "\n WHERE NOT ifnull(c." + CUSTOMER_COLUMN_PHONE + ", '') = '' " +
                "\nGROUP BY c." + CUSTOMER_COLUMN_PHONE + ", c." + CUSTOMER_COLUMN_COUNTRY_CODE +
                "\n ORDER BY c." + CUSTOMER_COLUMN_ID;
        Cursor cursor = mDB.rawQuery(sql, null);
        return cursor;
    }

    public ArrayList<CCallInfo> getCustomerList() {
        ArrayList<CCallInfo> maoCallInfo = new ArrayList<>();
        String sql = "SELECT " + CUSTOMER_COLUMN_ID_LOCAL + ", " + CUSTOMER_COLUMN_ID + ", " + CUSTOMER_CALL_ID + ", " +
                CUSTOMER_COLUMN_FIRST_NAME + ", " + CUSTOMER_COLUMN_COMPLEX + ", " +
                CUSTOMER_COLUMN_LAST_NAME + ", " + CUSTOMER_COLUMN_COMPANY + ", " +
                CUSTOMER_COLUMN_COUNTRY_CODE + ", " + CUSTOMER_COLUMN_PHONE + ", " +
                CUSTOMER_COLUMN_FLAT + ", " + CUSTOMER_COLUMN_WING + ", " +
                CUSTOMER_COLUMN_BUILDING + ", " + CUSTOMER_COLUMN_AREA + ", " +
                CUSTOMER_COLUMN_ROAD + ", " + CUSTOMER_COLUMN_LANDMARK_1 + ", " +
                CUSTOMER_COLUMN_LANDMARK_2 + ", " + CUSTOMER_COLUMN_STATE + ", " +
                CUSTOMER_COLUMN_COUNTRY + ", " + CUSTOMER_COLUMN_POSTAL_CODE + ", " +
                CUSTOMER_COLUMN_CREATED + ", " + CUSTOMER_COLUMN_LAST_CALL_DATETIME + ", " +
                CUSTOMER_COLUMN_LAST_UPDATE + ", " + CUSTOMER_COLUMN_FLOOR + ", " +
                CUSTOMER_COLUMN_IS_IGNORED + ", " + CUSTOMER_COLUMN_IS_DELETED +
                " FROM " + CUSTOMER_TABLE_NAME + " WHERE " + CUSTOMER_COLUMN_LAST_UPDATE + " > " + CUSTOMER_SENT_TO_SERVER_DATETIME;
        Cursor cursor = mDB.rawQuery(sql, null);
        int iIgnored, iDeleted;
        if (cursor.moveToFirst()) {
            do {
                CCallInfo oCC = new CCallInfo();
                oCC.setGetMiCustomerLocalId(cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_ID_LOCAL)));
                oCC.setCustomerId(cursor.getLong(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_ID)));
                oCC.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_FIRST_NAME)));
                oCC.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_LAST_NAME)));
                oCC.setCompany(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_COMPANY)));
                oCC.setCountryCode(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_COUNTRY_CODE)));
                oCC.setNationalNumber(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_PHONE)));
                oCC.setFlatNo(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_FLAT)));
                oCC.setWing(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_WING)));
                oCC.setBulding(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_BUILDING)));
                oCC.setArea(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_AREA)));
                oCC.setRoad(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_ROAD)));
                oCC.setLandmark1(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_LANDMARK_1)));
                oCC.setLandmark2(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_LANDMARK_2)));
                oCC.setState(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_STATE)));
                oCC.setCountry(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_COUNTRY)));
                oCC.setPostalCode(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_POSTAL_CODE)));
                oCC.setDateCreated(cursor.getLong(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_CREATED)));
                oCC.setDateLastCall(cursor.getLong(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_LAST_CALL_DATETIME)));
                oCC.setDateUpdated(cursor.getLong(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_LAST_UPDATE)));
                oCC.setMiCustomerCallId(cursor.getInt(cursor.getColumnIndex(CUSTOMER_CALL_ID)));
                oCC.setComplexOrColony(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_COMPLEX)));
                oCC.setFloor(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_COLUMN_FLOOR)));
                iIgnored = cursor.getInt(cursor.getColumnIndex(CUSTOMER_COLUMN_IS_IGNORED));
                if (iIgnored == 1)
                    oCC.setIgnore(true);
                else oCC.setIgnore(false);
                iDeleted = cursor.getInt(cursor.getColumnIndex(CUSTOMER_COLUMN_IS_DELETED));
                if (iDeleted == 1)
                    oCC.setDeleted(true);
                else oCC.setDeleted(false);
                maoCallInfo.add(oCC);
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return maoCallInfo;
    }

    public ArrayList<COrderHeader> getOrder() {
        ArrayList<COrderHeader> maCO = new ArrayList<>();
        try {
            String sql = "SELECT o." + ORDER_HEADER_COLUMN_ID + ", o." + ORDER_HEADER_COLUMN_STORE_PHONE_ID + ", o."
                    + ORDER_HEADER_COLUMN_PHONE_NO + ", o." + ORDER_HEADER_COLUMN_COUNTRY_CODE + ", o."
                    + ORDER_HEADER_COLUMN_DATE + ", o." + ORDER_HEADER_COLUMN_AMOUNT + ", c."
                    + CUSTOMER_CALL_LOG_ID_SERVER
                    + "\nFROM " + ORDER_HEADER_TABLE_NAME + " o INNER JOIN " + CUSTOMER_CALL_LOG_TABLE_NAME + " c "
                    + "\nON o." + CUSTOMER_CALL_ID + " = " + " c." + CUSTOMER_CALL_LOG_ID
                    + " WHERE " + ORDER_HEADER_COLUMN_DATE + " > " + ORDER_HEADER_COLUMN_SENT_TO_SERVER;
            Cursor cursor = mDB.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                do {
                    COrderHeader oCH = new COrderHeader();
                    oCH.setMiOrederId(cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_HEADER_COLUMN_ID)));
                    oCH.setMiStorePhoneId(cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_HEADER_COLUMN_STORE_PHONE_ID)));
                    oCH.setMsPhone(cursor.getString(cursor.getColumnIndexOrThrow(ORDER_HEADER_COLUMN_PHONE_NO)));
                    oCH.setMsCountryCode(cursor.getString(cursor.getColumnIndexOrThrow(ORDER_HEADER_COLUMN_COUNTRY_CODE)));
                    oCH.setMiOrderDate(cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_HEADER_COLUMN_DATE)));
                    oCH.setMiOrderAmount(cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_HEADER_COLUMN_AMOUNT)));
                    oCH.setMiCustomerCallId(cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_CALL_LOG_ID_SERVER)));

                    maCO.add(oCH);
                } while (cursor.moveToNext());
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maCO;
    }
}