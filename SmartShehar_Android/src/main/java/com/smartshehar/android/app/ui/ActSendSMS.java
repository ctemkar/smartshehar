package com.smartshehar.android.app.ui;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.smartshehar.android.app.CGlobals_trains;
import com.smartshehar.android.app.CSms;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;



public class ActSendSMS extends AppCompatActivity {
    private static String TAG = "ActSendSMS: ";
    private TextView tvSelectedPhone;
    private CGlobals_trains mApp = null;
    static final int PICK_CONTACT = 1;
    String mSelectedNumber = "", mSelectedName = "", mSelectedEmail;
    CSms mSms = null;
    String smsText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            setContentView(R.layout.act_send_sms);
            ActionBar ab = getSupportActionBar();
            assert ab != null;
            ab.setTitle("Send SMS");
//		((MyApplication) getApplication()).getTracker(MyApplication.TrackerName.APP_TRACKER);

            mApp = CGlobals_trains.getInstance();
            mApp.init(this);
            mApp.mCH.userPing(getString(R.string.pageSendSMS), "");

            Intent i = getIntent();
            String sMessage = i.getExtras().getString("smstext");
            if (!TextUtils.isEmpty(sMessage)) {
                smsText = getString(R.string.courtesySmartShehar) +
                        ":  " + sMessage;
            } else {
                Toast.makeText(ActSendSMS.this, "Please refresh the app.", Toast.LENGTH_LONG).show();
                ActSendSMS.this.finish();
            }
            tvSelectedPhone = (TextView) findViewById(R.id.tvSelectedPhone);
            TextView tvMessage = (TextView) findViewById(R.id.tvMessage);
            Button btnSendSMS = (Button) findViewById(R.id.btnSendSMS);
            if (!TextUtils.isEmpty(smsText))
                tvMessage.setText(smsText);

            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);
            btnSendSMS.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        mSms = new CSms(ActSendSMS.this,
                                mSelectedNumber.replace(" ", ""),
                                smsText);

                        JSONObject jo = new JSONObject();
                        try {
                            jo.put("friendname", mSelectedName);
                            jo.put("friendmobile", mSelectedNumber);
                            jo.put("friendemail", mSelectedEmail);
                            jo.put("smstext", smsText);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mApp.mCH.userPing(getString(R.string.pageSendSMS),
                                jo.toString());
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "SMS failed, please try again later!",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    finish();
                }
            });
        } catch (Exception e) {
            SSLog.e(TAG, "btnSendSMS.setOnClickListener", e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
//		GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        //Stop the analytics tracking
//		GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            finish();
            return;
        }

        try {
            Uri result = data.getData();
            Log.v(TAG, "Got a result: " + result.toString());
            // get the contact id from the Uri
            String id = result.getLastPathSegment();
            Uri uri = data.getData();
            // query for phone numbers for the selected contact id
            Cursor c = getContentResolver().query(
                    Phone.CONTENT_URI, null,
                    Phone.CONTACT_ID + "=?",
                    new String[]{id}, null);
            assert c != null;
            int phoneIdx = c.getColumnIndex(Phone.NUMBER);
            int phoneType = c.getColumnIndex(Phone.TYPE);
            int iNameIdx = c.getColumnIndex(Phone.DISPLAY_NAME);
            final int iEmailIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

            if (c.getCount() > 1) { // contact has multiple phone numbers
                final CharSequence[] numbers = new CharSequence[c.getCount()];
                int i = 0;
                if (c.moveToFirst()) {
                    while (!c.isAfterLast()) { // for each phone number, add it to the numbers array
                        String type = (String) Phone.getTypeLabel(this.getResources(), c.getInt(phoneType), ""); // insert a type string in front of the number
                        String number = type + ": " + c.getString(phoneIdx);
                        numbers[i++] = number;
                        mSelectedName = c.getString(iNameIdx);
                        mSelectedEmail = c.getString(iEmailIdx) != null ? c.getString(iEmailIdx) : "";
                        c.moveToNext();
                    }
                    // build and show a simple dialog that allows the user to select a number
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Select no. for " + mSelectedName);
                    builder.setItems(numbers, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            String number = (String) numbers[item];
                            int index = number.indexOf(":");
                            number = number.substring(index + 2);
                            mSelectedNumber = number;
                            tvSelectedPhone.setText(mSelectedName + " - " + mSelectedNumber);
                            //	                loadContactInfo(number); // do something with the selected number
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.setOwnerActivity(this);
                    alert.show();

                } else Log.w(TAG, "No results");
            } else if (c.getCount() == 1) {
                c.moveToFirst();
                mSelectedNumber = c.getString(phoneIdx);
                mSelectedName = c.getString(iNameIdx);
                mSelectedEmail = c.getString(iEmailIdx) != null ? c.getString(iEmailIdx) : "";
                tvSelectedPhone.setText(mSelectedName + " - " + mSelectedNumber);
                // contact has a single phone number, so there's no need to display a second dialog
            }
            String displayName , emailAddress, phoneNumber ;
            ArrayList<String> contactlist = new ArrayList<>();
            ContentResolver cr = getContentResolver();
            Cursor cursor = cr.query(uri, null, null, null, null);
            assert cursor != null;
            while (cursor.moveToNext()) {
                emailAddress = "";
                phoneNumber = "";
                displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String id2 = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor emails = cr.query(Email.CONTENT_URI, null, Email.CONTACT_ID + " = " + id2, null, null);
                assert emails != null;
                while (emails.moveToNext()) {
                    mSelectedEmail = emails.getString(emails.getColumnIndex(Email.DATA));
                    break;
                }
                emails.close();
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id2}, null);
                    assert pCur != null;
                    while (pCur.moveToNext()) {
                        phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        break;
                    }
                    pCur.close();
                }
                contactlist.add("DisplayName: " + displayName + ", PhoneNumber: " + phoneNumber + ", EmailAddress: " + emailAddress + "\n");
            }
            cursor.close();
        } catch (Exception e) {
            SSLog.e(TAG, "onActivityResult", e);
        }
    }

    @Override
    protected void onPause() {
        if (mSms != null) {
            mSms.unregisterReceivers();
        }
        super.onPause();
    }
} // ActSendSMS