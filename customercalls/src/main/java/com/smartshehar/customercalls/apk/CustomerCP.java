package com.smartshehar.customercalls.apk;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by ctemkar on 23/04/2016.
 * Content provider for CustomerCP db
 */
public class CustomerCP extends ContentProvider {

    public static final String PROVIDER_NAME = "com.smartshehar.customercalls.apk.customercp";

    /** A uri to do operations on cust_master table. A content provider is identified by its uri */
    public static final Uri CONTENT_CALL_LOG_URI = Uri.parse("content://" + PROVIDER_NAME + "/call_log" );
    public static final Uri CONTENT_CUSTOMER_URI = Uri.parse("content://" + PROVIDER_NAME + "/customer" );
    /** Constants to identify the requested operation */
    private static final int CALL_LOG = 1;
    private static final int CALL_CUSTOMER = 2;

    private static final UriMatcher uriMatcher ;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "call_log", CALL_LOG);
        uriMatcher.addURI(PROVIDER_NAME, "customer", CALL_CUSTOMER);
        uriMatcher.addURI(PROVIDER_NAME, "call_log/#", CALL_LOG);
        uriMatcher.addURI(PROVIDER_NAME, "customer/#", CALL_CUSTOMER);
    }

    /** This content provider does the database operations by this object */
    CustomerCallsSQLiteDB mCustomerDB;

    /** A callback method which is invoked when the content provider is starting up */
    @Override
    public boolean onCreate() {
        mCustomerDB = new CustomerCallsSQLiteDB(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    /** A callback method which is by the default content uri */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int u = uriMatcher.match(uri);
        if(uriMatcher.match(uri)== CALL_LOG){
            Cursor cursor = mCustomerDB.getAllCallLog();
            cursor.setNotificationUri(getContext().getContentResolver(), uri );
            return cursor;
        }else{
            Cursor cursor = mCustomerDB.getCustomer();
            cursor.setNotificationUri(getContext().getContentResolver(), uri );
            return cursor;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }
}