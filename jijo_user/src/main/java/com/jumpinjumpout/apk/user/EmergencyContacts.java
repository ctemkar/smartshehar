package com.jumpinjumpout.apk.user;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class EmergencyContacts {
	public static final String KEY_PREF_MY_NAME = "pref_myname";
	public static final String KEY_PREF_MY_NUMBER = "pref_mynumber";
	public static final String KEY_PREF_MY_EMAIL = "pref_myemail";
	public static final String KEY_PREF_CC1 = "pref_cc1";
	public static final String KEY_PREF_CC1_PHONE = "pref_cc1_phone";
	public static final String KEY_PREF_CC1_MSG= "pref_cc1_msg";
	public static final String KEY_PREF_CC2 = "pref_cc2";
	public static final String KEY_PREF_CC2_PHONE = "pref_cc2_phone";
	public static final String KEY_PREF_CC2_MSG= "pref_cc2_msg";
	public static final String KEY_PREF_CC3 = "pref_cc3";
	public static final String KEY_PREF_CC3_PHONE = "pref_cc3_phone";
	public static final String KEY_PREF_CC3_MSG= "pref_cc3_msg";
	SharedPreferences mSettings; 
	public String msMyName, msMyNo, msMyEmail, msCC1Phone, msCC1, msCC1Msg, msCC2Phone, msCC2, 
	msCC2Msg, msCC3Phone, msCC3, msCC3Msg;

	public EmergencyContacts(Activity activity) {
		mSettings = PreferenceManager.getDefaultSharedPreferences(activity);
       	msMyName = mSettings.getString(KEY_PREF_MY_NAME, "");
       	msMyNo = mSettings.getString(KEY_PREF_MY_NUMBER, "");
       	msMyEmail = mSettings.getString(KEY_PREF_MY_EMAIL, CGlobals_user.msGmail);
       	msCC1Phone = mSettings.getString(KEY_PREF_CC1_PHONE, "");
       	msCC1 = mSettings.getString(KEY_PREF_CC1, "");
       	msCC1Msg = mSettings.getString(KEY_PREF_CC1_MSG, "");
       	msCC2Phone = mSettings.getString(KEY_PREF_CC2_PHONE, "");
       	msCC2 = mSettings.getString(KEY_PREF_CC2, "");
       	msCC2Msg = mSettings.getString(KEY_PREF_CC2_MSG, "");
       	msCC3Phone = mSettings.getString(KEY_PREF_CC3_PHONE, "");
       	msCC3 = mSettings.getString(KEY_PREF_CC3, "");
       	msCC3Msg = mSettings.getString(KEY_PREF_CC3_MSG, "");

	}

}
