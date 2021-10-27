package com.smartshehar.dashboard.app.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.CSms;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.PermissionUtil;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSApp;
import com.smartshehar.dashboard.app.SSLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.ui.CameraActivity;

public class Activity_BeSafe_Emergency extends AppCompatActivity {
    private static final String TAG = "Acitivity_Emergency: ";

    private CGlobals_lib_ss mApp = null;
    ProgressDialog mProgressDialog;
    private static final int MAX_SMALL_PICTURE_SIZE = 300;
    private static final int MAX_BIG_PICTURE_SIZE = 600;
    static final int REQUEST_PHOTO = 1;

    Bitmap bitmap = null;
    TextView mTvStatus;
    SharedPreferences mSettings;
    String msMyName, msMyNo, msMyEmail, msCC1Phone, msCC1, msCC1Msg, msCC2Phone, msCC2,
            msCC2Msg, msCC3Phone, msCC3, msCC3Msg;
    Bitmap resizedBitmap = null;
    String mCurrentPhotoPath;
    Bundle savedInstanceState;
    private static final int INITIAL_REQUEST = 1339;
    private static final String[] INITIAL_PERMS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS
    };

    private void showRequirementOfPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.SEND_SMS) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECEIVE_SMS))
            customDialog(getString(R.string.camera__sms_permission));
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, INITIAL_PERMS, INITIAL_REQUEST);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        this.savedInstanceState = savedInstanceState;
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermission())
                requestPermission();
            else startFunction();
        } else
            startFunction();
    } // sendEmergencySms

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                ) {
            return false;
        } else {
            return true;
        }
    }

    private void customDialog(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(Activity_BeSafe_Emergency.this);
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
        AlertDialog alert11 = builder1.create();
        alert11.show();
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

    private void startFunction() {
        mApp = CGlobals_lib_ss.getInstance();
        mApp.init(this);
        mApp.getMyLocation();
        mTvStatus = (TextView) findViewById(R.id.tvStatus);
        mSettings = PreferenceManager.getDefaultSharedPreferences(Activity_BeSafe_Emergency.this);
        msMyName = mSettings.getString(Constants_dp.KEY_PREF_MY_NAME, "");
        msMyNo = mSettings.getString(Constants_dp.KEY_PREF_MY_NUMBER, "");
        msMyEmail = mSettings.getString(Constants_dp.KEY_PREF_MY_EMAIL, SSApp.mGmail);

        msCC1Phone = mSettings.getString(Constants_dp.KEY_PREF_CC1_PHONE, "");
        msCC1 = mSettings.getString(Constants_dp.KEY_PREF_CC1, "");
        msCC1Msg = mSettings.getString(Constants_dp.KEY_PREF_CC1_MSG, "");
        msCC2Phone = mSettings.getString(Constants_dp.KEY_PREF_CC2_PHONE, "");
        msCC2 = mSettings.getString(Constants_dp.KEY_PREF_CC2, "");
        msCC2Msg = mSettings.getString(Constants_dp.KEY_PREF_CC2_MSG, "");
        msCC3Phone = mSettings.getString(Constants_dp.KEY_PREF_CC3_PHONE, "");
        msCC3 = mSettings.getString(Constants_dp.KEY_PREF_CC3, "");
        msCC3Msg = mSettings.getString(Constants_dp.KEY_PREF_CC3_MSG, "");

        if (savedInstanceState != null) {
            msMyName = savedInstanceState.getString("msMyName");
            msMyNo = savedInstanceState.getString("msMyNo");
            msMyEmail = savedInstanceState.getString("msMyEmail");
            msCC1Phone = savedInstanceState.getString("msCC1Phone");
            msCC1 = savedInstanceState.getString("msCC1");
            msCC1Msg = savedInstanceState.getString("msCC1Msg");
            msCC2Phone = savedInstanceState.getString("msCC2Phone");
            msCC2 = savedInstanceState.getString("msCC2");
            msCC2Msg = savedInstanceState.getString("msCC2Msg");
            msCC3Phone = savedInstanceState.getString("msCC3Phone");
            msCC3 = savedInstanceState.getString("msCC3");
            msCC3Msg = savedInstanceState.getString("msCC3Msg");
            mCurrentPhotoPath = Constants_lib_ss.PREF_CAMERA_PHOTO_PATH;
            setPic(mCurrentPhotoPath);
            Activity_BeSafe_Emergency.this.finish();

        } else {
            Intent intent = new Intent(Activity_BeSafe_Emergency.this, CameraActivity.class);
            intent.putExtra("PHOTO_DIRECTORY", getString(R.string.appTitle));
            startActivityForResult(intent, REQUEST_PHOTO);
            if (!TextUtils.isEmpty(msMyNo))
                sendEmergencySms(msMyNo, msMyEmail);
            if (!TextUtils.isEmpty(msCC1Phone))
                sendEmergencySms(msCC1Phone, msCC1);
            if (!TextUtils.isEmpty(msCC2Phone))
                sendEmergencySms(msCC2Phone, msCC2);
            if (!TextUtils.isEmpty(msCC3Phone))
                sendEmergencySms(msCC3Phone, msCC3);
        }
    }

    private void sendEmergencySms(String sNo, String sEmail) {
        Location location = mApp.getMyLocation();
        if (TextUtils.isEmpty(sNo))
            return;
        String msg = "Emergency SMS ";
        msg += " from - maps.google.com/maps?z=16&t=m&q=loc:" +
                String.format("%.4f", location.getLatitude()) +
                "+" + String.format("%.4f", location.getLongitude());
        msg += " via SmartShehar Safety Shield http://goo.gl/NBu8A ";

        if (!TextUtils.isEmpty(sEmail)) {
            msg += ". Check " + sEmail + "";
        }
        new CSms(this.getBaseContext(), sNo, msg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Toast.makeText(this, "SmartShehar Home", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            bitmap = null;
            resizedBitmap = null;

            if (requestCode == REQUEST_PHOTO && resultCode == Activity.RESULT_OK) {

                if (data != null) {
                    Bundle extra = data.getBundleExtra("data");
                    mCurrentPhotoPath = extra.getString("imageFilePath");
                    setPic(mCurrentPhotoPath);

                }
            } else { // result code not ok {
                Toast.makeText(this, "No picture returned by the camera. Is your memory full?",
                        Toast.LENGTH_SHORT)
                        .show();
                SSLog.e("Camera: ", "No picture returned", "No picture");

            }
            super.onActivityResult(requestCode, resultCode, data);
            Activity_BeSafe_Emergency.this.finish();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                    .show();
            SSLog.e("Camera", "error", e.toString());
        }
    }

    private void setPic(String photoPath) {
        try {
            File photo = new File(photoPath);
            Uri selectedImage = Uri.fromFile(photo);

            getContentResolver().notifyChange(selectedImage, null);
            ContentResolver cr = getContentResolver();
            bitmap = MediaStore.Images.Media
                    .getBitmap(cr, selectedImage);

            mApp.mProgressDialog = ProgressDialog.show(this,
                    null, "Loading ...", true);
            mApp.mProgressDialog.getWindow().setGravity(Gravity.BOTTOM);

            UploadPicture(bitmap);

            Toast.makeText(getApplicationContext(), "Picture sent to server. You should get an email soon",
                    Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        if (CGlobals_lib_ss.haveNetworkConnection() == 0) {
            try {
                mTvStatus = (TextView) findViewById(R.id.tvStatus);
                mTvStatus.setText(R.string.no_internet);
            } catch (Exception e) {
                SSLog.e(" Emergency - Resume: ", "error", e.getMessage());
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
        if (location == null)
            return "";
        try {
            Geocoder geo = new Geocoder(Activity_BeSafe_Emergency.this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            if (addresses.isEmpty()) {
                return "";
            } else {
                if (addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    address += ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                    return address;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
        return "";
    }

    private void UploadPicture(Bitmap bitmap) {

        try {
            final Calendar cal = Calendar.getInstance();
            final String sTime = Long.toString(cal.getTimeInMillis());

            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            int ratio, newHeight, newWidth;
            int iMaxPictureSize = MAX_SMALL_PICTURE_SIZE;
            if (Connectivity.isConnectedFast(Activity_BeSafe_Emergency.this))
                iMaxPictureSize = MAX_BIG_PICTURE_SIZE;
            if (height > width)
                ratio = height / iMaxPictureSize;
            else
                ratio = width / iMaxPictureSize;
            newHeight = height / ratio;
            newWidth = width / ratio;

            resizedBitmap =
                    Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);

            byte[] ba = bao.toByteArray();
            final String ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
            final String url = Constants_dp.SAFTEY_SHIELD_PHP_PATH + "postImage.php";

            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                Log.d(TAG, "Image sent");
                            } else {
                                Toast.makeText(Activity_BeSafe_Emergency.this, "Failed!", Toast.LENGTH_LONG).show();

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
//

                }
            }) {
                @Override
                protected Map<String, String> getParams() {

                    Map<String, String> params = new HashMap<>();
                    params.put("cc", msMyEmail);
                    params.put("image", ba1);
                    params.put("filename",
                            SSApp.mGmail + "_" + sTime + ".jpg");
                    params.put("y", Integer.toString(cal.get(Calendar.YEAR)));
                    params.put("o", Integer.toString(cal.get(Calendar.MONTH)));
                    params.put("d", Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
                    params.put("h", Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
                    params.put("m", Integer.toString(cal.get(Calendar.MINUTE)));
                    params.put("s", Integer.toString(cal.get(Calendar.SECOND)));
                    SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(Activity_BeSafe_Emergency.this);
                    String msName = mSettings.getString(Constants_dp.KEY_PREF_MY_NAME, "");
                    String msPhone = mSettings.getString(Constants_dp.KEY_PREF_MY_NUMBER, "");
                    params.put("name", msName);
                    params.put("phone", msPhone);
                    if (!TextUtils.isEmpty(msCC1))
                        params.put("cc1", msCC1);
                    if (!TextUtils.isEmpty(msCC2))
                        params.put("cc2", msCC2);
                    if (!TextUtils.isEmpty(msCC3))
                        params.put("cc3", msCC3);
                    params.put("cc1phone", msCC1Phone);
                    params.put("cc2phone", msCC2Phone);
                    params.put("cc3phone", msCC3Phone);
                    params.put("cc1msg", msCC1Msg);
                    params.put("cc2msg", msCC2Msg);
                    params.put("cc3msg", msCC3Msg);


                    params = CGlobals_db.getInstance(Activity_BeSafe_Emergency.this).getBasicMobileParams(params,
                            url, Activity_BeSafe_Emergency.this);
                    return CGlobals_db.getInstance(Activity_BeSafe_Emergency.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(Activity_BeSafe_Emergency.this).getRequestQueue(Activity_BeSafe_Emergency.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        bitmap = null;
        resizedBitmap = null;

    }


    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("msMyName", msMyName);
        savedInstanceState.putString("msMyNo", msMyNo);
        savedInstanceState.putString("msMyEmail", msMyEmail);
        savedInstanceState.putString("msCC1Phone", msCC1Phone);
        savedInstanceState.putString("msCC1", msCC1);
        savedInstanceState.putString("msCC1Msg", msCC1Msg);
        savedInstanceState.putString("msCC2Phone", msCC2Phone);
        savedInstanceState.putString("msCC2", msCC2);
        savedInstanceState.putString("msCC2Msg", msCC2Msg);
        savedInstanceState.putString("msCC3Phone", msCC3Phone);
        savedInstanceState.putString("msCC3", msCC3);
        savedInstanceState.putString("msCC3Msg", msCC3Msg);
        super.onSaveInstanceState(savedInstanceState);
    }
} // Activity_BeSafe_Emergency





