package com.smartshehar.dashboard.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHandler extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHandler: ";

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 64;

    // Database Name
    private static final String DATABASE_NAME = "smartsheharDB";

    //  table name
    private static final String TABLE_ISSUE_DETAILS = "issue";
    private static final String TABLE_ISSUE_IMAGES = "issue_images";


    // view name
    private static final String VIEW_ISSUE = "vw_issue";


    // TrafficOffenceDetails Table Columns names
    private static final String KEY_ID = "id";
    private static final String COLUMN_UNIQUE_KEY = "uniquekey";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_ISSUE_TIME = "issue_time";
    private static final String COLUMN_ISSUE_TYPE = "issue_type";
    private static final String COLUMN_VEHICLE_NO = "vehicleno";
    private static final String COLUMN_SUBMIT_REPORT = "submitreport";
    private static final String COLUMN_SEND_TO_SERVER = "senttoserver";
    //    private static final String COLUMN_SUB_TYPE_CODE = "subtypecode";
    private static final String COLUMN_LAT = "latitude";
    private static final String COLUMN_LNG = "longitude";
    private static final String COLUMN_LOCALITY = "locality";
    private static final String COLUMN_SUBLOCALITY = "sublocality";
    private static final String COLUMN_POSTAL_CODE = "postal_code";
    private static final String COLUMN_ROUTE = "route";
    private static final String COLUMN_NEIGHBORHOOD = "neighborhood";
    private static final String COLUMN_ADMINISTRATIVE_AREA_LEVEL_2 = "administrative_area_level_2";
    private static final String COLUMN_ADMINISTRATIVE_AREA_LEVEL_1 = "administrative_area_level_1";
    private static final String COLUMN_WARD_NO = "wardno";
    private static final String COLUMN_MLA_ID = "mlaid";
    private static final String COLUMN_MP_ID = "mpid";
    private static final String COLUMN_GROUP_ID = "group_id";

    private static final String KEY_ISSUE_ID = "issue_id";
    private static final String KEY_IMAGE = "imageOne";
    private static final String KEY_VIDEO = "videoOne";
    private static final String COLUMN_IMAGE_NAME = "imageName";
    private static final String COLUMN_ISSUE_UNIQUE_KEY = "issue_uniquekey";
    private static final String COLUMN_CREATION_DATETIME = "creationDatetime";
    private static final String COLUMN_IMAGE_PATH = "imagePath";
    private static final String COLUMN_ISSUE_LOOKUP_SUBTYPE_CODE = "issue_lookup_subtype_code";
    private static final String COLUMN_ISSUE_LOOKUP_ITEM_CODE = "issue_lookup_item_code";

    Context context;


    public DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_ISSUE_DETAILS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_UNIQUE_KEY + " INTEGER,"
                + COLUMN_ADDRESS + " TEXT,"
                + COLUMN_ISSUE_TIME + " DATETIME,"
                + COLUMN_ISSUE_TYPE + " TEXT,"
                + COLUMN_VEHICLE_NO + " TEXT,"
                + COLUMN_ISSUE_LOOKUP_SUBTYPE_CODE + " TEXT DEFAULT null,"
                + COLUMN_ISSUE_LOOKUP_ITEM_CODE + " TEXT DEFAULT null,"
                + COLUMN_LAT + " DOUBLE DEFAULT 0,"
                + COLUMN_LNG + " DOUBLE DEFAULT 0,"
                + COLUMN_SUBMIT_REPORT + " INTEGER DEFAULT 0,"
                + COLUMN_SEND_TO_SERVER + " INTEGER DEFAULT 0,"
                + COLUMN_WARD_NO + " TEXT DEFAULT NULL,"
                + COLUMN_MLA_ID + " TEXT DEFAULT NULL,"
                + COLUMN_MP_ID + " TEXT DEFAULT NULL,"
                + COLUMN_GROUP_ID + " TEXT DEFAULT NULL,"
                + COLUMN_LOCALITY + " TEXT DEFAULT NULL,"
                + COLUMN_SUBLOCALITY + " TEXT DEFAULT NULL,"
                + COLUMN_POSTAL_CODE + " TEXT DEFAULT NULL,"
                + COLUMN_ROUTE + " TEXT DEFAULT NULL,"
                + COLUMN_NEIGHBORHOOD + " TEXT DEFAULT NULL,"
                + COLUMN_ADMINISTRATIVE_AREA_LEVEL_2 + " TEXT DEFAULT NULL,"
                + COLUMN_ADMINISTRATIVE_AREA_LEVEL_1 + " TEXT DEFAULT NULL )";

        String CREATE_IMAGES_TABLE = "CREATE TABLE " + TABLE_ISSUE_IMAGES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_ISSUE_ID + " INTEGER,"
                + COLUMN_ISSUE_UNIQUE_KEY + " INTEGER,"
                + KEY_IMAGE + " BLOB ,"
                + KEY_VIDEO + " TEXT,"
                + COLUMN_IMAGE_NAME + " TEXT,"
                + COLUMN_IMAGE_PATH + " TEXT,"
                + COLUMN_CREATION_DATETIME + " DATETIME )";

        String CREATE_VIEW = "create view " + VIEW_ISSUE + " as "
                + " select d.id as issue_id, d.uniquekey, d.address, " +"  d.vehicleno as vehicleno ,"+
                "d.issue_time, d.issue_type, d.submitreport as submitreport, d.senttoserver as senttoserver, " +
                "d.issue_lookup_subtype_code as issue_lookup_subtype_code, d.issue_lookup_item_code as issue_lookup_item_code,d.latitude as latitude, d.longitude as longitude, " +
                "d.locality as locality, d.sublocality as sublocality, d.postal_code as postal_code, d.route as route, " +
                "d.neighborhood as neighborhood, d.administrative_area_level_2 as administrative_area_level_2, d.administrative_area_level_1 as administrative_area_level_1," +
                "d.wardno as wardno,d.mlaid as mlaid,d.mpid as mpid,d.group_id as group_id,i.id as imageId,  i.imageOne, i.imageName, " +
                "i.imagePath as imagePath, i.issue_uniquekey as issue_uniquekey, " +
                "i.creationDateTime as creationDateTime from " + TABLE_ISSUE_DETAILS + " d LEFT OUTER JOIN  " + TABLE_ISSUE_IMAGES + " i " +
                "on d.uniquekey = i.issue_uniquekey";


        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_IMAGES_TABLE);
        db.execSQL(CREATE_VIEW);


    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        if (oldVersion < DATABASE_VERSION) {

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ISSUE_DETAILS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ISSUE_IMAGES);
            db.execSQL("DROP VIEW IF EXISTS " + VIEW_ISSUE);
            onCreate(db);
        }

        // Create tables again
//
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
    // Adding offence details
    public void addIssue(CIssue details) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ADDRESS, details._address); // DriverInfo Name
        values.put(COLUMN_ISSUE_TIME, details._issue_time);
        values.put(COLUMN_ISSUE_TYPE, details._issue_type);
        values.put(COLUMN_VEHICLE_NO, details._vehicle_no);
        values.put(COLUMN_UNIQUE_KEY, details.getUniqueKey());
        values.put(COLUMN_ISSUE_LOOKUP_SUBTYPE_CODE, details.getSubtypecode());
        values.put(COLUMN_ISSUE_LOOKUP_ITEM_CODE, details.getItemcode());
        values.put(COLUMN_LAT, String.valueOf(details.getLat()));
        values.put(COLUMN_LNG, String.valueOf(details.getLng()));
        values.put(COLUMN_WARD_NO, String.valueOf(details.getWardno()));
        values.put(COLUMN_MLA_ID, String.valueOf(details.getMla_id()));
        values.put(COLUMN_MP_ID, String.valueOf(details.getMp_id()));
        values.put(COLUMN_GROUP_ID, details.getGroup_id());
        values.put(COLUMN_LOCALITY, details.getLocality());
        values.put(COLUMN_SUBLOCALITY, details.getSublocality());
        values.put(COLUMN_POSTAL_CODE, details.getPostal_code());
        values.put(COLUMN_ROUTE, details.getRoute());
        values.put(COLUMN_NEIGHBORHOOD, details.getNeighborhood());
        values.put(COLUMN_ADMINISTRATIVE_AREA_LEVEL_2, details.getAdministrative_area_level_2());
        values.put(COLUMN_ADMINISTRATIVE_AREA_LEVEL_1, details.getAdministrative_area_level_1());

        try {
            int numRowsAffected = db.update(TABLE_ISSUE_DETAILS, values, COLUMN_UNIQUE_KEY + " = ?", new String[]{String.valueOf(details.getUniqueKey())});
            if (numRowsAffected < 0 || numRowsAffected == 0) {
                db.insert(TABLE_ISSUE_DETAILS, null, values);
                Log.d(TAG, "ADDED");
            }
        } catch (Exception e) {
            db.insert(TABLE_ISSUE_DETAILS, null, values);
            Log.d(TAG, "addIssue " + e.toString());
        }
        db.close(); // Closing database connection
    }

    // Adding Offence images
    public void addIssueImages(CIssue images) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(KEY_IMAGE, images._imageOne); //byte
            values.put(COLUMN_ISSUE_UNIQUE_KEY, images.issueuniquekey);
            values.put(COLUMN_IMAGE_NAME, images._imageName);
            values.put(COLUMN_CREATION_DATETIME, images._creationDateTime);
            values.put(COLUMN_IMAGE_PATH, images._imagepath);
            int numRowsAffected = db.update(TABLE_ISSUE_IMAGES,
                    values,
                    COLUMN_CREATION_DATETIME + " = ?",
                    new String[]{String.valueOf(images._creationDateTime)});
            if (numRowsAffected < 0 || numRowsAffected == 0) {
                db.insert(TABLE_ISSUE_IMAGES, null, values);
                Log.d(TAG, "ADDED images");

            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public long getLastinsertedUniqueKey() {
        long lastUniqueKey = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT uniquekey from " + TABLE_ISSUE_DETAILS + " where id = (select max(id) from " + TABLE_ISSUE_DETAILS + ")";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            lastUniqueKey = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_KEY)));
        }
        db.close();
        return lastUniqueKey;
    }

    // Deleting single contact
    public void deleteImage(String createionDateTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ISSUE_IMAGES, COLUMN_CREATION_DATETIME + " = ?",
                new String[]{String.valueOf(createionDateTime)});
        db.close();
    }

    public void sentToserver(long uniquekey, int flag) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "update " + TABLE_ISSUE_DETAILS + " set " + COLUMN_SEND_TO_SERVER + " = " + flag + " where " + COLUMN_UNIQUE_KEY + " = " + uniquekey;
        db.execSQL(sql);
        db.close();
    }

    public List<CIssue> getDraftList() {
        List<CIssue> list = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String sql = "select * from " + VIEW_ISSUE + " where " + COLUMN_SEND_TO_SERVER + " = 0";
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                do {
                    CIssue contact = new CIssue();
                    contact.setID(Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("issue_id"))));
                    contact.set_submitReport(Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("submitreport"))));
                    contact.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                    contact.setTimeOffence(cursor.getString(cursor.getColumnIndexOrThrow("issue_time")));
                    contact.setTypeOffence(cursor.getString(cursor.getColumnIndexOrThrow("issue_type")));
                    contact.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow("imageOne")));
                    contact.setImageName(cursor.getString(cursor.getColumnIndexOrThrow("imageName")));
                    contact.setUniqueKey(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow("uniquekey"))));
                    if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndexOrThrow("issue_uniquekey"))))
                        contact.setIssueuniquekey(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow("issue_uniquekey"))));
                    contact.set_creationDateTime(cursor.getString(cursor.getColumnIndexOrThrow("creationDateTime")));
                    contact.set_imagepath(cursor.getString(cursor.getColumnIndexOrThrow("imagePath")));
                    contact.setSubtypecode(cursor.getString(cursor.getColumnIndexOrThrow("issue_lookup_subtype_code")));
                    contact.setItemcode(cursor.getString(cursor.getColumnIndexOrThrow("issue_lookup_item_code")));
                    contact.setLat(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow("latitude"))));
                    contact.setLng(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow("longitude"))));
                    contact.setWardno(cursor.getString(cursor.getColumnIndexOrThrow("wardno")));
                    contact.setMla_id(cursor.getString(cursor.getColumnIndexOrThrow("mlaid")));
                    contact.setMp_id(cursor.getString(cursor.getColumnIndexOrThrow("mpid")));
                    contact.setVehicle(cursor.getString(cursor.getColumnIndexOrThrow("vehicleno")));
                    contact.setGroup_id(cursor.getString(cursor.getColumnIndexOrThrow("group_id")));
                    contact.setGroup_id(cursor.getString(cursor.getColumnIndexOrThrow("locality")));
                    contact.setGroup_id(cursor.getString(cursor.getColumnIndexOrThrow("sublocality")));
                    contact.setGroup_id(cursor.getString(cursor.getColumnIndexOrThrow("postal_code")));
                    contact.setGroup_id(cursor.getString(cursor.getColumnIndexOrThrow("route")));
                    contact.setGroup_id(cursor.getString(cursor.getColumnIndexOrThrow("neighborhood")));
                    contact.setGroup_id(cursor.getString(cursor.getColumnIndexOrThrow("administrative_area_level_2")));
                    contact.setGroup_id(cursor.getString(cursor.getColumnIndexOrThrow("administrative_area_level_1")));
                    list.add(contact);
                } while (cursor.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ER IS " + e.toString());
        }
        return list;
    }
}