package com.smartshehar.dashboard.app.ui;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smartshehar.dashboard.app.CSms;
import com.smartshehar.dashboard.app.PermissionUtil;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSApp;
import com.smartshehar.dashboard.app.SSLog;

import java.util.List;
import java.util.Locale;


public class ActSoundAlarm extends AppCompatActivity {
	private SSApp mApp = null;
	ProgressDialog mProgressDialog;

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

	TextView mTvStatus;
	Button btnStop;
	MediaPlayer mMediaPlayer; 
	SharedPreferences mSettings; 
	String msMyName, msMyNo, msMyEmail, msCC1Phone, msCC1, msCC1Msg, msCC2Phone, msCC2, 
	msCC2Msg, msCC3Phone, msCC3, msCC3Msg;
	private static final int INITIAL_REQUEST = 1340;
	private static final String[] INITIAL_PERMS = {
			android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
			android.Manifest.permission.READ_EXTERNAL_STORAGE,
			android.Manifest.permission.SEND_SMS,
			android.Manifest.permission.RECEIVE_SMS
	};
	private void showRequirementOfPermission() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
				ActivityCompat.shouldShowRequestPermissionRationale(this,
						Manifest.permission.READ_EXTERNAL_STORAGE)||
				ActivityCompat.shouldShowRequestPermissionRationale(this,
						Manifest.permission.SEND_SMS) ||
				ActivityCompat.shouldShowRequestPermissionRationale(this,
						Manifest.permission.RECEIVE_SMS))
			customDialog(getString(R.string.read_write_storage));
	}
	private void requestPermission() {
		ActivityCompat.requestPermissions(this, INITIAL_PERMS, INITIAL_REQUEST);
	}
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		ActionBar ab = getSupportActionBar();
		assert ab != null;
		ab.setHomeButtonEnabled(true);
		setContentView(R.layout.actsoundalarm);
		mApp = ((SSApp) this.getApplication());
		mApp.init(this);
		mApp.getMyLocation();
		if (Build.VERSION.SDK_INT >= 23) {
			if (!checkPermission())
				requestPermission();
			else startFunction();
		} else
			startFunction();

    }
	private boolean checkPermission() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
				|| ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
				|| ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
				|| ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
				) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case INITIAL_REQUEST:
				// If request is cancelled, the result arrays are empty.
				if (PermissionUtil.verifyPermissions(grantResults)) {
					startFunction();
				} else {
					showRequirementOfPermission();
				}
				break;

		}
	}

	private void startFunction()
	{

		mTvStatus = (TextView) findViewById(R.id.tvStatus);
		btnStop = (Button) findViewById(R.id.btnStop);
		playAlarm();
		mSettings = PreferenceManager.getDefaultSharedPreferences(ActSoundAlarm.this);
		msMyName = mSettings.getString(KEY_PREF_MY_NAME, "");
		msMyNo = mSettings.getString(KEY_PREF_MY_NUMBER, "");
		msMyEmail = mSettings.getString(KEY_PREF_MY_EMAIL, SSApp.mGmail);
		msMyName = mSettings.getString(KEY_PREF_MY_NAME, "");
		msMyNo = mSettings.getString(KEY_PREF_MY_NUMBER, "");
		msMyEmail = mSettings.getString(KEY_PREF_MY_EMAIL, SSApp.mGmail);
		msCC1Phone = mSettings.getString(KEY_PREF_CC1_PHONE, "");
		msCC1 = mSettings.getString(KEY_PREF_CC1, "");
		msCC1Msg = mSettings.getString(KEY_PREF_CC1_MSG, "");
		msCC2Phone = mSettings.getString(KEY_PREF_CC2_PHONE, "");
		msCC2 = mSettings.getString(KEY_PREF_CC2, "");
		msCC2Msg = mSettings.getString(KEY_PREF_CC2_MSG, "");
		msCC3Phone = mSettings.getString(KEY_PREF_CC3_PHONE, "");
		msCC3 = mSettings.getString(KEY_PREF_CC3, "");
		msCC3Msg = mSettings.getString(KEY_PREF_CC3_MSG, "");
		if(!TextUtils.isEmpty(msMyNo))
			sendEmergencySms( msMyNo, msMyEmail);
		if(!TextUtils.isEmpty(msCC1Phone))
			sendEmergencySms( msCC1Phone, msCC1);
		if(!TextUtils.isEmpty(msCC2Phone))
			sendEmergencySms( msCC2Phone, msCC2 );
		if(!TextUtils.isEmpty(msCC3Phone))
			sendEmergencySms( msCC3Phone, msCC3);
		btnStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(mMediaPlayer != null)
					mMediaPlayer.stop();
				finish();
			}
		});
	}
	private void playAlarm() {
   	 try {
//    	 MediaPlayer mMediaPlayer = new MediaPlayer();
   		 mMediaPlayer = MediaPlayer.create(ActSoundAlarm.this, R.raw.soundalarm);
    	 mMediaPlayer.setLooping(true);
    	 mMediaPlayer.start();
	 } catch (Exception e) {
		 SSLog.e("ActSoundAlarm: playAlarm - ","error " ,e.getMessage());
	 }
	}

	private void sendEmergencySms( String sNo, String sEmail) {
		Location location = mApp.getMyLocation();
		if(TextUtils.isEmpty(sNo))
			return;
		String msg = "Emergency SMS ";
		msg += " ";
		if(location != null) {
			msg += " from - maps.google.com/maps?z=16&t=m&q=loc:" + 
					String.format("%.4f", location.getLatitude()) +
					"+" + String.format("%.4f", location.getLongitude());
			
		}
		if(!TextUtils.isEmpty(sEmail)) {
			msg += ". Check " + sEmail + " via SmartShehar Safety Shield- http://goo.gl/NBu8A ";
		}
		new CSms(this.getBaseContext(), sNo, msg);
	} // sendEmergencySms


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}        

	@Override
    protected void onResume() {
		if( mApp.haveNetworkConnection() == 0) {
			try {
				mTvStatus = (TextView) findViewById(R.id.tvStatus);
				mTvStatus.setText(R.string.no_internet);
			} catch (Exception e) {
				SSLog.e(" Emergency - Resume: ","error", e.getMessage());
			}
		}
        super.onResume();
    }	

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	  setContentView(R.layout.activity_emergency);
	}

	String getAddress(Location location) {
		if(location == null)
			return "";
		try {
			Geocoder geo = new Geocoder(ActSoundAlarm.this.getApplicationContext(), Locale.getDefault());
		    List<Address> addresses = geo.getFromLocation(location.getLatitude(), 
		    		location.getLongitude(), 1);
	        if (addresses.isEmpty()) {
	        	return "";
	        }
	        else {
	            if (addresses.size() > 0) {
					String address = addresses.get(0).getAddressLine(0);
					address += ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
					return address;
	            }
	        }
	    } catch (Exception e) {
	    	e.printStackTrace(); // getFromLocation() may sometimes fail
	    }
		return "";
	}

	private void customDialog(String message) {
		android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(ActSoundAlarm.this);
		builder1.setMessage(message);
		builder1.setCancelable(true);
		builder1.setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						requestPermission();
					}
				});
		builder1.setNegativeButton("CANCEL",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				});
		android.app.AlertDialog alert11 = builder1.create();
		alert11.show();
	}

} // Activity_BeSafe_Emergency





