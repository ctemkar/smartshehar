package com.smartshehar.dashboard.app.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSApp;
import com.smartshehar.dashboard.app.SSLog;

import java.util.regex.Pattern;

public class Activity_Settings extends PreferenceActivity {
    ProgressDialog mProgressDialog;
	public static final String KEY_PREF_MY_NAME = "pref_myname";
	public static final String KEY_PREF_MY_NUMBER = "pref_mynumber";
	public static final String KEY_PREF_MY_EMAIL = "pref_myemail";
	public static final String KEY_PREF_CC1 = "pref_cc1";
	public static final String KEY_PREF_CC1_PHONE = "pref_cc1_phone";
	public static final String KEY_PREF_CC2 = "pref_cc2";
	public static final String KEY_PREF_CC2_PHONE = "pref_cc2_phone";
	public static final String KEY_PREF_CC3 = "pref_cc3";
	public static final String KEY_PREF_CC3_PHONE = "pref_cc3_phone";
	public static final String KEY_PREF_EMERGENCY_CONTACT1 = "pref_emergency_contact1";
	public static final String KEY_PREF_EMERGENCY_CONTACT2 = "pref_emergency_contact2";
	public static final String KEY_PREF_EMERGENCY_CONTACT3 = "pref_emergency_contact3";
   	private static final int CONTACT_PICKER1_RESULT = 1001;
   	private static final int CONTACT_PICKER2_RESULT = 1002;
   	private static final int CONTACT_PICKER3_RESULT = 1003;
    Preference mPrefContact1, mPrefContact2, mPrefContact3 ;

   	SharedPreferences.Editor mEditor;
	Preference pref_mynumber;
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_ss);
        SSApp mApp = ((SSApp) this.getApplication());
       	mApp.init(this);
       	SharedPreferences mSettings;
       	mSettings = PreferenceManager.getDefaultSharedPreferences(this);
       	mEditor = mSettings.edit();
       	Preference pref_myname = findPreference(KEY_PREF_MY_NAME);
       	Preference pref_email = findPreference(KEY_PREF_MY_EMAIL);
       	pref_myname.setSummary(mSettings.getString(KEY_PREF_MY_NAME,
       			"Your name to be attached to emergency email"));
        pref_mynumber = findPreference(KEY_PREF_MY_NUMBER);
        pref_mynumber.setSummary(mSettings.getString(KEY_PREF_MY_NUMBER, getString(R.string.pref_phonenumber_hint)));
//        String email = mSettings.getString(KEY_PREF_MY_NUMBER, "");
//        if(TextUtils.isEmpty(email)) {
        	if(TextUtils.isEmpty(SSApp.mGmail))
        		pref_email.setSummary(mSettings.getString(KEY_PREF_MY_EMAIL, getString(R.string.pref_myemail_hint)));
        	else
        		pref_email.setSummary(mSettings.getString(KEY_PREF_MY_EMAIL, SSApp.mGmail));

       	mPrefContact1 = findPreference(KEY_PREF_EMERGENCY_CONTACT1);
       	mPrefContact1.setSummary(mSettings.getString(KEY_PREF_EMERGENCY_CONTACT1, "Select an emergency contact"));
       	mPrefContact1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
       	                @Override
       	                public boolean onPreferenceClick(Preference arg0) {
       	                	Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
       	                         Contacts.CONTENT_URI);
       	                 startActivityForResult(contactPickerIntent, CONTACT_PICKER1_RESULT);        	                    //code for what you want it to do
       	                    return true;
       	                }
       	});
       	mPrefContact2 = findPreference(KEY_PREF_EMERGENCY_CONTACT2);
       	mPrefContact2.setSummary(mSettings.getString(KEY_PREF_EMERGENCY_CONTACT2, "Select an emergency contact"));
       	mPrefContact2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
       	                @Override
       	                public boolean onPreferenceClick(Preference arg0) {
       	                	Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
       	                         Contacts.CONTENT_URI);
       	                 startActivityForResult(contactPickerIntent, CONTACT_PICKER2_RESULT);        	                    //code for what you want it to do
       	                    return true;
       	                }
       	});
       	mPrefContact3 = findPreference(KEY_PREF_EMERGENCY_CONTACT3);
       	mPrefContact3.setSummary(mSettings.getString(KEY_PREF_EMERGENCY_CONTACT3, "Select an emergency contact"));
       	mPrefContact3.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
       	                @Override
       	                public boolean onPreferenceClick(Preference arg0) {
       	                	Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
       	                         Contacts.CONTENT_URI);
       	                 startActivityForResult(contactPickerIntent, CONTACT_PICKER3_RESULT);        	                    //code for what you want it to do
       	                    return true;
       	                }
       	});

        OnPreferenceChangeListener pfl = new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                preference.setSummary(o.toString());
                return true;
            }
        };
        OnPreferenceChangeListener emailListener = new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
            	if(!checkEmail(o.toString())) {
                    Toast.makeText(Activity_Settings.this, "Invalid email - " + o.toString(), Toast.LENGTH_SHORT).show();

            		return false;
            	}
                preference.setSummary(o.toString());
                return true;
            }
        };

        pref_myname.setOnPreferenceChangeListener(pfl);
        pref_mynumber.setOnPreferenceChangeListener(pfl);
        pref_email.setOnPreferenceChangeListener(emailListener);
/*        
        pref_cc2_phone.setOnPreferenceChangeListener(pfl);
        pref_cc3_phone.setOnPreferenceChangeListener(pfl);
        pref_cc2.setOnPreferenceChangeListener(emailListener);
        pref_cc3.setOnPreferenceChangeListener(emailListener);
*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {
        case R.id.menu_clear_settings:
        	mEditor.clear();
        	mEditor.commit();
            // Single menu item is selected do something
            // Ex: launching new activity/screen or show alert message
//            Toast.makeText(AndroidMenusActivity.this, "Bookmark is Selected", Toast.LENGTH_SHORT).show();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
        );

  private boolean checkEmail(String email) {
          return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (resultCode == RESULT_OK) {
    	  Uri contactUri = data.getData();
          Cursor cursor = null;
          switch (requestCode) {
          case CONTACT_PICKER1_RESULT:
              String email1 , name1 , no1 ;
              String contact1Summary ;
              try {
                  name1 = getContactName(contactUri);
                  no1 = getContactNo(contactUri);
                  email1 = getEmail(contactUri);
                  contact1Summary = getContactSummary(name1, no1, email1);
                  mEditor.putString(KEY_PREF_CC1_PHONE, no1);
                  mEditor.putString(KEY_PREF_CC1, email1);
                  mEditor.putString(KEY_PREF_EMERGENCY_CONTACT1, contact1Summary);
                  mPrefContact1.setSummary(contact1Summary);
                  mEditor.commit();
              } catch (Exception e) {
                  SSLog.e(" Activity_Settings - onActivityResult ", "Failed to get contact data", e.getMessage());
              } finally {
                  if (cursor != null) {
                      cursor.close();
                  }
              }
              break;
          case CONTACT_PICKER2_RESULT:
              String email2 , name2 , no2 ;
              String contact2Summary ;
              try {
                  name2 = getContactName(contactUri);
                  no2 = getContactNo(contactUri);
                  email2 = getEmail(contactUri);
                  contact2Summary = getContactSummary(name2, no2, email2);
                  mEditor.putString(KEY_PREF_CC2_PHONE, no2);
                  mEditor.putString(KEY_PREF_CC2, email2);
                  mEditor.putString(KEY_PREF_EMERGENCY_CONTACT2, contact2Summary);
                  mPrefContact2.setSummary(contact2Summary);
                  mEditor.commit();
              } catch (Exception e) {
                  SSLog.e(" Activity_Settings - onActivityResult ", "Failed to get contact data" , e.getMessage());
              } finally {

                  if (cursor != null) {
                      cursor.close();
                  }
              }
              break;
          case CONTACT_PICKER3_RESULT:
              String email3, name3 ,no3;
              String contact3Summary;
              try {
                  name3 = getContactName(contactUri);
                  no3 = getContactNo(contactUri);
                  email3 = getEmail(contactUri);
                  contact3Summary = getContactSummary(name3, no3, email3);
                  mEditor.putString(KEY_PREF_CC3_PHONE, no3);
                  mEditor.putString(KEY_PREF_CC3, email3);
                  mEditor.putString(KEY_PREF_EMERGENCY_CONTACT3, contact3Summary);
                  mPrefContact3.setSummary(contact3Summary);
                  mEditor.commit();
              } catch (Exception e) {
                  SSLog.e(" Activity_Settings - onActivityResult ", "Failed to get contact data" ,e.getMessage());
              } finally {
                  if (cursor != null) cursor.close();
              }
              break;
          }
      } else {
          SSLog.w(" Activity_Settings - onActivityResult ", "Warning: activity result not ok");
      }
  }

private String getContactSummary(String name, String no, String email) {
	String missingInfo = "";
	missingInfo += TextUtils.isEmpty(no) ? " Phone no. is empty " : "";
	missingInfo += TextUtils.isEmpty(email) ? " Email is empty " : "";
	if(!TextUtils.isEmpty(missingInfo))
		Toast.makeText(Activity_Settings.this,
    		"Warning; " + missingInfo + "\nEnter missing information for this contact and select again",
    		Toast.LENGTH_LONG).show();
	return (TextUtils.isEmpty(name) ? "" : name) + (TextUtils.isEmpty(no) ? "" : ", " + no) +
			(TextUtils.isEmpty(email) ? "" : ", " + email) ;
}

private String getContactName(Uri uriContact) {
	String contactName = null;
	// querying contact data store
	Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
    assert cursor != null;
    if (cursor.moveToFirst()) {
          contactName = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
	}
	cursor.close();
//	mEditor.putString(KEY_PREF_CC1_NAME, contactName);
	return contactName;
}

private String getContactNo(Uri uriContact) {
	String contactNumber = "";
	String contactID = "";
    Cursor cursorID = getContentResolver().query(uriContact,
              new String[]{Contacts._ID},
              null, null, null);
    assert cursorID != null;
    if (cursorID.moveToFirst()) {
    	contactID = cursorID.getString(cursorID.getColumnIndex(Contacts._ID));
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
//    mEditor.putString(KEY_PREF_CC1_PHONE, contactNumber);
    cursorPhone.close();
    return contactNumber;
}


private String getEmail(Uri uriContact) {

Cursor cursor = null;
String email = "";
	try {
	    SSLog.v(" Activity_Settings - onActivityResult ", "Got a contact result: "
	            + uriContact.toString());
	    String id = uriContact.getLastPathSegment();
	    // query for everything email
	    cursor = getContentResolver().query(Email.CONTENT_URI,
	            null, Email.CONTACT_ID + "=?", new String[] { id },
	            null);
        assert cursor != null;
        int emailIdx = cursor.getColumnIndex(Email.DATA);
	    if (cursor.moveToFirst()) {
	        email = cursor.getString(emailIdx);
//	        mEditor.putString(KEY_PREF_CC1, email);
	    } else {
	        SSLog.w(" Activity_Settings - onActivityResult ", "No results");
	    }
	} catch (Exception e) {
	    SSLog.e(" Activity_Settings - onActivityResult ", "Failed to get email data" ,e.getMessage());
	} finally {
		if (cursor != null) {
			cursor.close();
		}
		}
	return email;
}

} // Activity_Settings


