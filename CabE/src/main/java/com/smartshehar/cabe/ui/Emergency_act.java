package com.smartshehar.cabe.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartshehar.cabe.Constants_CabE;
import com.smartshehar.cabe.R;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.CSms;
import lib.app.util.SSLog_SS;

/**
 * Created by jijo_soumen on 25/04/2016.
 * CabE Emergency save contact and show some emergency contact like police 100
 */
public class Emergency_act extends AppCompatActivity {

    private static String TAG = "Emergency_act: ";
    LinearLayout llCallPolice, llHelp;
    TextView tvEmergencyNumberSaved;
    ImageView ivCloseEmergency;
    private static final int CONTACT_PICKER_RESULT = 1011;
    String msPhNo;
    CSms mSms = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergency_act);
        llCallPolice = (LinearLayout) findViewById(R.id.llCallPolice);
        llCallPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + "100"));
                startActivity(callIntent);
            }
        });
        llHelp = (LinearLayout) findViewById(R.id.llHelp);
        tvEmergencyNumberSaved = (TextView) findViewById(R.id.tvEnergencyNumberSaved);
        llHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
            }
        });
        msPhNo = CGlobals_lib_ss.getInstance().getPersistentPreference(Emergency_act.this)
                .getString(Constants_CabE.KEY_PREF_EMERGENCY_CC_PHONE, "");
        if (!TextUtils.isEmpty(msPhNo)) {
            tvEmergencyNumberSaved.setText("Emergency Contact- " + msPhNo);
        }
        mSms = new CSms(Emergency_act.this);
        /*msPhNo = CGlobals_lib_ss.getInstance().getPersistentPreference(Emergency_act.this)
                .getString(Constants_CabE.KEY_PREF_EMERGENCY_CC_PHONE, "");
        msEmail = CGlobals_lib_ss.getInstance().getPersistentPreference(Emergency_act.this)
                .getString(Constants_CabE.KEY_PREF_EMERGENCY_EMAIL, "");
        msName = CGlobals_lib_ss.getInstance().getPersistentPreference(Emergency_act.this)
                .getString(Constants_CabE.KEY_PREF_EMERGENCY_NAME, "");
        if (!TextUtils.isEmpty(msPhNo)) {
            sendEmergencySms(msPhNo, msEmail);
        }
        if (!TextUtils.isEmpty(msEmail)) {
            Intent intent = new Intent(Emergency_act.this, WalkWithMe_act.class);
            startActivity(intent);
        }*/
        ivCloseEmergency = (ImageView) findViewById(R.id.ivCloseEmergency);
        ivCloseEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONTACT_PICKER_RESULT) {
            if (resultCode == RESULT_OK) {
                Uri contactUri = data.getData();
                String email1, name1, no1;
                try {
                    name1 = getContactName(contactUri);
                    no1 = getContactNo(contactUri);
                    email1 = getEmail(contactUri);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(Emergency_act.this)
                            .putString(Constants_CabE.KEY_PREF_EMERGENCY_CC_PHONE, no1);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(Emergency_act.this)
                            .putString(Constants_CabE.KEY_PREF_EMERGENCY_EMAIL, email1);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(Emergency_act.this)
                            .putString(Constants_CabE.KEY_PREF_EMERGENCY_NAME, name1);
                    tvEmergencyNumberSaved.setText("Emergency Contact- " + no1);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(Emergency_act.this).commit();
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "Failed to get contact data", e, Emergency_act.this);
                }
            }
        }
    }

    private String getContactName(Uri uriContact) {
        String contactName = null;
        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
        assert cursor != null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        cursor.close();
        return contactName;
    }

    private String getContactNo(Uri uriContact) {
        String contactNumber = "";
        String contactID = "";
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);
        assert cursorID != null;
        if (cursorID.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }
        cursorID.close();
        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                new String[]{contactID}, null);

        assert cursorPhone != null;
        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        cursorPhone.close();
        return contactNumber;
    }

    private String getEmail(Uri uriContact) {

        Cursor cursor = null;
        String email = "";
        try {
            SSLog_SS.v(TAG, "Got a contact result: "
                    + uriContact.toString());
            String id = uriContact.getLastPathSegment();
            // query for everything email
            cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?", new String[]{id},
                    null);
            assert cursor != null;
            int emailIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
            if (cursor.moveToFirst()) {
                email = cursor.getString(emailIdx);
            } else {
                SSLog_SS.w(TAG, "No results");
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "Failed to get email data", e, Emergency_act.this);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return email;
    }

    /*private String getContactSummary(String name, String no, String email) {
        String missingInfo = "";
        missingInfo += TextUtils.isEmpty(no) ? " Phone no. is empty " : "";
        missingInfo += TextUtils.isEmpty(email) ? " Email is empty " : "";
        if (!TextUtils.isEmpty(missingInfo))
            Toast.makeText(Emergency_act.this,
                    "Warning; " + missingInfo + "\nEnter missing information for this contact and select again",
                    Toast.LENGTH_LONG).show();
        return (TextUtils.isEmpty(name) ? "" : name) + (TextUtils.isEmpty(no) ? "" : ", " + no) +
                (TextUtils.isEmpty(email) ? "" : ", " + email);
    }*/// ContactSummary

    /*@SuppressLint("DefaultLocale")
    private void sendEmergencySms(String sNo, String sEmail) {
        Location location = CGlobals_lib_ss.getInstance().getMyLocation();
        if (TextUtils.isEmpty(sNo))
            return;
        String msg = "Emergency SMS ";
//		if(!TextUtils.isEmpty(sName))
//			msg += " from " + sName + " ";
        msg += " from - maps.google.com/maps?z=16&t=m&q=loc:" +
                String.format("%.4f", location.getLatitude()) +
                "+" + String.format("%.4f", location.getLongitude());
//        if (location == null) {
////			msg += " lat, long - " + String.format("%.4f", location.getLatitude()) +
////				", " + String.format("%.4f", location.getLongitude());
//           Log.d(TAG,"LOCATION IS EMPTY");
//
//        }else{
        msg += " via SmartShehar Safety Shield http://goo.gl/NBu8A ";
//        }
        if (!TextUtils.isEmpty(sEmail)) {
            msg += ". Check " + sEmail + "";
        }
        //new CSms(Emergency_act.this, sNo, msg);
        mSms.sendSMS(sNo, msg);
    }*/ // sendEmergencySms
}
