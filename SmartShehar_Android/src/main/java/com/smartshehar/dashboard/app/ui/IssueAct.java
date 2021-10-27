package com.smartshehar.dashboard.app.ui;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.gson.Gson;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.CIssue;
import com.smartshehar.dashboard.app.CRecentCategory;

import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.DataBaseHandler;
import com.smartshehar.dashboard.app.ListAdapter;
import com.smartshehar.dashboard.app.PermissionUtil;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSApp;
import com.smartshehar.dashboard.app.SSLog;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lib.app.util.CAddress;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.ClearableEditText;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.GeoHelper;
import lib.app.util.MyLocation;
import lib.app.util.SSLog_SS;
import lib.app.util.ui.CameraActivity;
import lib.app.util.ui.GalleryActivity;


public abstract class IssueAct extends AppCompatActivity implements
        View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    static final int REQUEST_PHOTO = 1;
    private static String TAG = "IssueAct";
    public AddressResultReceiver mResultReceiver;
    protected String mAddressOutput;
    EditText etIssueAdd, etIssueTime;
    Spinner spnWardNo;
    ClearableEditText etVehicleNo;
    TextView txtTypeOffence;
    Connectivity mConnectivity;
    public static Activity mActivity;
    String postalcode = "", cityname = "", sublocality = "", formatted_address = "",
            route = "", neighborhood = "", administrative_area_level_2 = "", administrative_area_level_1 = "";
    double latitude = Constants_lib_ss.INVALIDLAT, longitude = Constants_lib_ss.INVALIDLNG;

    String sIssueDescription = "", ba1 = "", ba2 = "",
            issueid = "", mCurrentPhotoPathOne = "", mCurrentPhotoPathTwo = "";
    DataBaseHandler db;
    private static final int RESULT_GALLERY_IMG = 100;
    ImageView imgOne, imgTwo, ivRecent, ivNoPlate;
    boolean pictureOne = false, pictureTwo = false;
    byte[] baOne = null, baTwo = null;
    String creationDateTimeOne = "", creationDateTimeTwo = "";
    long iUniqueKey = 0;

    String imageNameOne = "", imageNameTwo = "";

    Location mCurrentLocation;
    Bitmap bitmap;
    String issue_time = "", issueAddress = "", sIssueitemcode;
    TextView takephoto, txtWard, tvWard;

    protected abstract String getTrafficType();

    GoogleApiClient mGoogleApiClient = null;
    String issueCat;
    boolean isBtnDoneFlag = false,
            isMailSent, imageVerification = true, vehicleNumber = false;
    ArrayList<String> mWardList;
    ListAdapter wardNoAdapter;
    String mWardno, mVehicleNo;
    boolean poorNetFlag = false;
    GeoHelper geoHelper;
    private static final long FASTEST_INTERVAL = 1000 * 10;
    private static final long INTERVAL = 1000 * 10;
    LocationRequest mLocationRequest;
    private ArrayList<CRecentCategory> maoRecentCat;
    private ArrayList<String> masRecentCatList;
    Button btnSubmitIssue;
    private final static int MAX_RECENT_CATS = 25;
    private ListAdapter mRecentCatsAdapter;
    static boolean active = false;
    ProgressBar pbSubmit;
    Snackbar snackbar;
    private CoordinatorLayout coordinatorLayout;
    private static final int INITIAL_REQUEST = 1338;
    private static final String[] INITIAL_PERMS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };

    private void showRequirementOfPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA))
            customDialog(getString(R.string.camera_permission));
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, INITIAL_PERMS, INITIAL_REQUEST);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doGotIt(false);
        setContentView(R.layout.traffic_violation_act);
        mActivity = this;
        createLocationRequest();
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermission())
                requestPermission();
        }
        geoHelper = new GeoHelper();
        mGoogleApiClient = new GoogleApiClient.Builder(IssueAct.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(IssueAct.this)
                .addOnConnectionFailedListener(IssueAct.this)
                .build();
        mGoogleApiClient.connect();
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(IssueAct.this)) {
            if (!mConnectivity.connectionError(IssueAct.this, getString(R.string.no_iternet_to_submit_issue))) {
                Log.d(TAG, "Internet Connection");
            }
        }
        MyLocation myLocation = new MyLocation(
                SSApp.mRequestQueue, IssueAct.this, mGoogleApiClient);
        myLocation.getLocation(this, onLocationResult);
        mCurrentLocation = CGlobals_lib_ss.getInstance().getMyLocation(this);
        CGlobals_db.getInstance(IssueAct.this).turnGPSOn(IssueAct.this, mGoogleApiClient);
        Constants_dp.db = new DataBaseHandler(IssueAct.this);

        CGlobals_lib_ss.getInstance()
                .getPersistentPreferenceEditor(IssueAct.this).
                putString("CURRENTPATHone", "").commit();
        CGlobals_lib_ss.getInstance()
                .getPersistentPreferenceEditor(IssueAct.this).
                putString("CURRENTPATHtwo", "").commit();


        mWardList = new ArrayList<>();
        init();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        masRecentCatList = new ArrayList<>();
        maoRecentCat = readRecentCat();
        db = new DataBaseHandler(this);

        findViewById(R.id.img_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.llFirstImage).setVisibility(View.GONE);
                findViewById(R.id.ll_secondeImage).setVisibility(View.VISIBLE);


            }
        });
        findViewById(R.id.img_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.llFirstImage).setVisibility(View.VISIBLE);
                findViewById(R.id.ll_secondeImage).setVisibility(View.GONE);


            }
        });

        spnWardNo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mWardno = mWardList.get(position);

                if (!mWardno.equalsIgnoreCase("Select")) {
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(IssueAct.this).
                            putString(Constants_dp.PREF_WARD, mWardno).apply();
                    tvWard.setText(mWardno);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ivRecent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (masRecentCatList != null && masRecentCatList.size() > 0) {
                    masRecentCatList.clear();
                    maoRecentCat = readRecentCat();
                    final ArrayList<CRecentCategory> aoRecentCat = new ArrayList<>();
                    for (CRecentCategory rc : maoRecentCat) {
                        if (rc.getIssueType().equals(getTrafficType()))
                            aoRecentCat.add(rc);
                    }
                    new android.app.AlertDialog.Builder(IssueAct.this)
                            .setTitle("Recent Categories")
                            .setAdapter(mRecentCatsAdapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int position) {
                                    sIssueDescription = aoRecentCat.get(position).getIssueDescription();
                                    sIssueitemcode = aoRecentCat.get(position).getIssueitemcode();
                                    imageVerification = aoRecentCat.get(position).getImageVerification();
                                    vehicleNumber = aoRecentCat.get(position).getVehicleNumber();
                                    if (imageVerification) {
                                        takephoto.setText("Photo is mandatory");
                                    } else {
                                        takephoto.setText("Photo is optional");
                                    }
                                    txtTypeOffence.setText(sIssueDescription);

                                }

                            }).create().show();
                } else {
                    showSnackbar("No Recent Categories");
                }
            }
        });
        try {
            if (savedInstanceState != null) {
                bitmap = null;
                issueid = savedInstanceState.getString("issueid");
                iUniqueKey = savedInstanceState.getLong("iUniqueKey");
                pictureOne = savedInstanceState.getBoolean("pictureOne");
                pictureTwo = savedInstanceState.getBoolean("pictureTwo");
                creationDateTimeOne = savedInstanceState.getString("creationDateTimeOne");
                creationDateTimeTwo = savedInstanceState.getString("creationDateTimeTwo");
                Constants_dp.GET_LAST_UNIQUE_KEY = iUniqueKey;
                sIssueitemcode = savedInstanceState.getString("issueitemcode");
                issueAddress = savedInstanceState.getString("issueaddress");
                etIssueAdd.setText(issueAddress);
                issue_time = savedInstanceState.getString("issuedatetime");

                SimpleDateFormat ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT,
                        Locale.getDefault());
                Date dNow = ft.parse(issue_time);
                String date, time;
                ft = new SimpleDateFormat("yyyy-MM-dd");
                date = ft.format(dNow);
                ft = new SimpleDateFormat("HH:mm:ss");
                time = ft.format(dNow);
                etIssueTime.setText("Date " + date + " Time " + time);

                postalcode = savedInstanceState.getString("postalcode");
                cityname = savedInstanceState.getString("cityname");
                sublocality = savedInstanceState.getString("sublocality");
                route = savedInstanceState.getString("route");
                neighborhood = savedInstanceState.getString("neighborhood");
                administrative_area_level_2 = savedInstanceState.getString("administrative_area_level_2");
                administrative_area_level_1 = savedInstanceState.getString("administrative_area_level_1");
                latitude = savedInstanceState.getDouble("latitude");
                longitude = savedInstanceState.getDouble("longitude");
                sIssueDescription = savedInstanceState.getString("sIssueDescription");
                if (!TextUtils.isEmpty(sIssueDescription)) {
                    txtTypeOffence.setText(sIssueDescription);
                }

                mWardList = savedInstanceState.getStringArrayList("mWardList");
                assert mWardList != null;
                wardNoAdapter = new ListAdapter(IssueAct.this, mWardList);
//                wardNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnWardNo.setAdapter(wardNoAdapter);

                mWardno = savedInstanceState.getString("wardno");
                if (!TextUtils.isEmpty(mWardno)) {
                    spnWardNo.setSelection(getIndex(spnWardNo, mWardno));
                }
                etVehicleNo.setText(savedInstanceState.getString("vehicleno"));
                if (savedInstanceState.getByteArray("baOne") != null) {
                    baOne = savedInstanceState.getByteArray("baOne");
                }
                if (savedInstanceState.getByteArray("baTwo") != null) {
                    baTwo = savedInstanceState.getByteArray("baTwo");
                }
                mCurrentPhotoPathOne = savedInstanceState.getString("mCurrentPhotoPath");
                mCurrentPhotoPathTwo = savedInstanceState.getString("mCurrentPhotoPathTwo");

                imageNameOne = savedInstanceState.getString("imageNameOne");
                imageNameTwo = savedInstanceState.getString("imageNameTwo");
                ba1 = savedInstanceState.getString("ba1");
                ba2 = savedInstanceState.getString("ba2");
                if (!TextUtils.isEmpty(mCurrentPhotoPathOne)) {
                    bitmap = null;
                    File photo = new File(mCurrentPhotoPathOne);
                    bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath());
                    imgOne.setImageBitmap(bitmap);

                    findViewById(R.id.imgOne).setVisibility(View.VISIBLE);
                    findViewById(R.id.imgCancelRedOne).setVisibility(View.VISIBLE);
                    findViewById(R.id.takephoto).setVisibility(View.GONE);
                    ivNoPlate.setVisibility(View.GONE);
                    findViewById(R.id.img_right).setVisibility(View.VISIBLE);

                }
                if (!TextUtils.isEmpty(mCurrentPhotoPathTwo)) {
                    bitmap = null;
                    File photo = new File(mCurrentPhotoPathTwo);
                    bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath());
                    imgTwo.setImageBitmap(bitmap);
                    findViewById(R.id.txtExtra).setVisibility(View.GONE);
                    findViewById(R.id.imgTwo).setVisibility(View.VISIBLE);
                    findViewById(R.id.imgCancelRedTwo).setVisibility(View.VISIBLE);
                }

                setPic();
            } else {
                if (iUniqueKey == 0) {
                    iUniqueKey = System.currentTimeMillis();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
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
                if (!PermissionUtil.verifyPermissions(grantResults))
                    showRequirementOfPermission();
                break;

        }
    }

    private void customDialog(String message) {
        android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(IssueAct.this);
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

    private void doGotIt(boolean alwayaShow) {
        CGlobals_db.getInstance(this).gotIt(this,
                Constants_dp.PREF_GOTIT_ACTIVE_TRIPS, alwayaShow, getString(R.string.helplinetext1), "");
    }


    private void getAddress() {

        mResultReceiver = new AddressResultReceiver(new Handler());

    }

    void init() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        etVehicleNo = (ClearableEditText) findViewById(R.id.etVehicleNo);
        etIssueAdd = (EditText) findViewById(R.id.etAdd_offence);
        etIssueTime = (EditText) findViewById(R.id.etTime_offence);
        txtTypeOffence = (TextView) findViewById(R.id.txtTypeOffence);
        spnWardNo = (Spinner) findViewById(R.id.spnWardNo);
        imgOne = (ImageView) findViewById(R.id.imgOne);
        imgTwo = (ImageView) findViewById(R.id.imgTwo);
        ivNoPlate = (ImageView) findViewById(R.id.ivNoPlate);
        takephoto = (TextView) findViewById(R.id.takephoto);
        txtWard = (TextView) findViewById(R.id.txtWard);
        tvWard = (TextView) findViewById(R.id.tvWard);
        ivRecent = (ImageView) findViewById(R.id.ivRecent);
        btnSubmitIssue = (Button) findViewById(R.id.btnSubmitIssue);
        pbSubmit = (ProgressBar) findViewById(R.id.pbSubmit);
        setDate();
        imgOne.setOnClickListener(this);
        imgTwo.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_parking_violation, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refreshLocation) {

            try {
                CGlobals_db.getInstance(IssueAct.this).turnGPSOn(IssueAct.this, mGoogleApiClient);
                mCurrentLocation = CGlobals_lib_ss.getInstance().getMyLocation(this);
                if (baOne == null) {
                    if (mCurrentLocation != null) {
                        etIssueAdd.setText(mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
                        latitude = mCurrentLocation.getLatitude();
                        longitude = mCurrentLocation.getLongitude();
                        geoHelper.getAddress(IssueAct.this, mCurrentLocation, onGeoHelperResult);
                    }
                }
            } catch (Exception e) {
                SSLog.e(TAG, "refreshLocation", e);
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(final View view) {
        try {
            int id = view.getId();
            if (id == R.id.imgTakePicOne) {
                pictureOne = true;
                pictureTwo = false;
                if (TextUtils.isEmpty(creationDateTimeOne)) {
                    creationDateTimeOne = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT).format(new Date());
                }
                openCamera();

            } else if (id == R.id.imgTakePicTwo) {
                pictureOne = false;
                pictureTwo = true;
                if (TextUtils.isEmpty(creationDateTimeTwo)) {
                    creationDateTimeTwo = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT).format(new Date());
                }
                openCamera();

            } else if (view.getId() == R.id.imgCancelRedOne) {

                try {
                    if (!creationDateTimeOne.equals("")) {
                        DeleteAlertBox(R.id.imgOne, R.id.imgCancelRedOne, R.id.ivNoPlate, creationDateTimeOne, "one");
                    } else {
                        showSnackbar("Please take picture");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (view.getId() == R.id.imgCancelRedTwo) {

                try {
                    if (!creationDateTimeTwo.equals("")) {
                        DeleteAlertBox(R.id.imgTwo, R.id.imgCancelRedTwo, R.id.txtExtra, creationDateTimeTwo, "two");
                    } else {
                        showSnackbar("Please take picture");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (view.getId() == R.id.imgGalleryOne) {
                pictureOne = true;
                pictureTwo = false;
                if (TextUtils.isEmpty(creationDateTimeOne)) {
                    creationDateTimeOne = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT).format(new Date());
                }
                openGallery();
            } else if (view.getId() == R.id.imgGalleryTwo) {
                pictureOne = false;
                pictureTwo = true;

                if (TextUtils.isEmpty(creationDateTimeTwo)) {
                    creationDateTimeTwo = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT).format(new Date());
                }
                openGallery();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected");
        mCurrentLocation = CGlobals_lib_ss.getInstance().getMyLocation(this);
        if (mCurrentLocation != null) {
            etIssueAdd.setText(mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();
            geoHelper.getAddress(IssueAct.this, mCurrentLocation, onGeoHelperResult);
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "connectionResult " + connectionResult.toString());
    }

    private void openCamera() {

        bitmap = null;
        Intent intent = new Intent(IssueAct.this, CameraActivity.class);
        intent.putExtra("PHOTO_DIRECTORY", getString(R.string.appTitle));
        startActivityForResult(intent, REQUEST_PHOTO);

    }

    private void openGallery() {
        bitmap = null;
        Intent intent = new Intent(IssueAct.this, GalleryActivity.class);
        intent.putExtra("PHOTO_DIRECTORY", getString(R.string.appTitle));
        startActivityForResult(intent, RESULT_GALLERY_IMG);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        bitmap = null;
        if (requestCode == REQUEST_PHOTO) {

            if (resultCode == Activity.RESULT_OK && data != null) {
                Bundle extra = data.getBundleExtra("data");
                if (pictureOne) {
                    baOne = extra.getByteArray("imageByte");
                    imageNameOne = extra.getString("imageName");
                    mCurrentPhotoPathOne = extra.getString("imageFilePath");
                    ba1 = Base64.encodeToString(baOne, Base64.DEFAULT);
                }
                if (pictureTwo) {
                    baTwo = extra.getByteArray("imageByte");
                    imageNameTwo = extra.getString("imageName");
                    mCurrentPhotoPathTwo = extra.getString("imageFilePath");
                    ba2 = Base64.encodeToString(baTwo, Base64.DEFAULT);
                }
                setPic();
            } else {
                showSnackbar("No picture returned by the camera. Is your memory full?");
            }
        }
        if (requestCode == RESULT_GALLERY_IMG) {
            try {
                if (resultCode == RESULT_OK && data != null) {
                    Bundle extra = data.getBundleExtra("data");
                    if (pictureOne) {
                        baOne = extra.getByteArray("imageByte");
                        imageNameOne = extra.getString("imageName");
                        mCurrentPhotoPathOne = extra.getString("imageFilePath");
                        ba1 = Base64.encodeToString(baOne, Base64.DEFAULT);
                    }
                    if (pictureTwo) {
                        baTwo = extra.getByteArray("imageByte");
                        imageNameTwo = extra.getString("imageName");
                        mCurrentPhotoPathTwo = extra.getString("imageFilePath");
                        ba2 = Base64.encodeToString(baTwo, Base64.DEFAULT);
                    }
                    setPic();
                } else {
                    showSnackbar("No picture returned by the gallery. Is your memory full?");

                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Set" + e.toString());
            }
        }
        if (requestCode == Constants_lib_ss.TYPELIST_SUBTYPE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Bundle extra = data.getBundleExtra("data");
                    sIssueDescription = extra.getString("issueDescription");
                    sIssueitemcode = extra.getString("issueitemcode");
                    imageVerification = extra.getBoolean("imageverification");
                    vehicleNumber = extra.getBoolean("vehiclenumber");
                    addRecentCat(sIssueDescription, sIssueitemcode, imageVerification, vehicleNumber, getTrafficType());
                    writeRecentCat();
                    if (imageVerification) {
                        takephoto.setText("Photo is mandatory");
                    } else {
                        takephoto.setText("Photo is optional");
                    }
                    txtTypeOffence.setText(sIssueDescription);
                }
            }
        }
        if (requestCode == CGlobals_db.REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    mCurrentLocation = CGlobals_lib_ss.getInstance().getMyLocation(this);
                    if (mCurrentLocation != null) {
                        latitude = mCurrentLocation.getLatitude();
                        longitude = mCurrentLocation.getLongitude();
                        etIssueAdd.setText(mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
                        etIssueAdd.setHint("Getting your address ");
                        geoHelper.getAddress(IssueAct.this, mCurrentLocation, onGeoHelperResult);
                    } else {
                        startLocationUpdates();
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    showSnackbar("You can not submit issue without location");

                    this.finish();
                    break;
                default:
                    showSnackbar("You can not submit issue without location");

                    this.finish();

                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAddressOutput = resultData
                    .getString(Constants_lib_ss.RESULT_DATA_KEY);

            if (resultCode == Constants_lib_ss.SUCCESS_RESULT) {
                if (!TextUtils.isEmpty(mAddressOutput)) {
                    etIssueAdd.setText(mAddressOutput);
                    if (TextUtils.isEmpty(issueid))
                        checkAddrTime();
                }
            }
        }
    }


    private GeoHelper.GeoHelperResult onGeoHelperResult = new GeoHelper.GeoHelperResult() {
        @Override
        public void gotAddress(CAddress addr) {
            if (addr != null) {
                if (addr.hasLatitude() || addr.hasLongitude()) {
                    setData(addr);

                }
            } else {
                showSnackbar(getString(R.string.interneterr));
            }
        }
    };

    @Override
    public void onLocationChanged(Location location) {

        mCurrentLocation = location;
        if (mCurrentLocation != null && baOne == null) {
            etIssueAdd.setText(location.getLatitude() + ", " + location.getLongitude());
            String json = new Gson().toJson(mCurrentLocation);
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(IssueAct.this).
                    putString(Constants_dp.PREF_MY_LOCATION, json);
            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();
            etIssueAdd.setText(latitude + "," + longitude);
            etIssueAdd.setHint("Getting address ");
            geoHelper.getAddress(IssueAct.this, mCurrentLocation, onGeoHelperResult);
            stopLocationUpdates();
        }
    }


    private void setData(CAddress oAddr) {
        getWard(oAddr.getLatitude(), oAddr.getLongitude());
        if (!TextUtils.isEmpty(oAddr.getPostalCode()))
            postalcode = oAddr.getPostalCode();
        cityname = oAddr.getCity();
        sublocality = oAddr.getSubLocality1();
        route = oAddr.getRoute();
        neighborhood = oAddr.getMsNeighborhodd();
        administrative_area_level_2 = oAddr.getAdminstrativeArea2();
        administrative_area_level_1 = oAddr.getAdminstraveArea1();
        if (!TextUtils.isEmpty(oAddr.getAddress())) {
            formatted_address = oAddr.getAddress();
            etIssueAdd.setText(formatted_address);
            stopLocationUpdates();
            if (TextUtils.isEmpty(issueid))
                checkAddrTime();

        }

        CGlobals_lib_ss.getInstance().addRecentAddress(oAddr);
        if (!cityname.equalsIgnoreCase("MUMBAI")) {
            findViewById(R.id.rlWard).setVisibility(View.GONE);
            txtWard.setVisibility(View.GONE);
            findViewById(R.id.txtWard).setVisibility(View.GONE);
        }
    }

    private void setPic() {
        try {
            if (pictureOne) {
                bitmap = BitmapFactory.decodeByteArray(baOne, 0, baOne.length);
                ivNoPlate.setVisibility(View.GONE);
                imgOne.setImageBitmap(bitmap);
                findViewById(R.id.imgOne).setVisibility(View.VISIBLE);
                findViewById(R.id.imgCancelRedOne).setVisibility(View.VISIBLE);
                findViewById(R.id.takephoto).setVisibility(View.GONE);
                findViewById(R.id.img_right).setVisibility(View.VISIBLE);
                findViewById(R.id.pbOne).setVisibility(View.VISIBLE);
                findViewById(R.id.tvSendingImageOne).setVisibility(View.VISIBLE);
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(IssueAct.this).putString("CURRENTPATHone", mCurrentPhotoPathOne).commit();
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(IssueAct.this).putString("CURRENTIMAGENAMEone", imageNameOne).commit();
                if (baOne != null) {
                    db.addIssueImages(new CIssue(iUniqueKey,
                            baOne, imageNameOne, creationDateTimeOne, ba1, mCurrentPhotoPathOne));
                }
            }
            if (pictureTwo) {
                bitmap = BitmapFactory.decodeByteArray(baTwo, 0, baTwo.length);
                findViewById(R.id.txtExtra).setVisibility(View.GONE);
                findViewById(R.id.imgTwo).setVisibility(View.VISIBLE);
                findViewById(R.id.imgCancelRedTwo).setVisibility(View.VISIBLE);
                findViewById(R.id.pbTwo).setVisibility(View.VISIBLE);
                findViewById(R.id.tvSendingImageTwo).setVisibility(View.VISIBLE);
                imgTwo.setImageBitmap(bitmap);
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(IssueAct.this).putString("CURRENTPATHtwo", mCurrentPhotoPathTwo).commit();
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(IssueAct.this).putString("CURRENTIMAGENAMEtwo", imageNameTwo).commit();
                if (baTwo != null) {
                    db.addIssueImages(new CIssue(iUniqueKey,
                            baTwo, imageNameTwo, creationDateTimeTwo, ba2, mCurrentPhotoPathTwo));
                }
            }
            bitmap = null;
            sendIssueImage();
        } catch (Exception e) {
            Log.d(TAG, "setPic: " + e.toString());
            e.printStackTrace();
        }
    }

    public void checkAddrTime() {
        try {
            issueAddress = etIssueAdd.getText().toString();
            issueCat = txtTypeOffence.getText().toString();
            mVehicleNo = etVehicleNo.getText().toString();
            if (getTrafficType().equals("MU_"))
                mWardno = spnWardNo.getSelectedItem().toString();
            db.addIssue(new CIssue(iUniqueKey, issueAddress,
                    issue_time, sIssueDescription, mVehicleNo, 0,
                    sIssueitemcode, latitude, longitude, mWardno, "", "", "", cityname,
                    sublocality, postalcode, route, neighborhood, administrative_area_level_2, administrative_area_level_1));
            if (!TextUtils.isEmpty(issueAddress) || !TextUtils.isEmpty(issue_time))
                addIssue();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendIssueImage() {
        try {
            final String url = Constants_dp.ADD_ISSUE_IMAGES_URL;
            if (TextUtils.isEmpty(issueid)) {
                poorNetFlag = true;
                db.sentToserver(iUniqueKey, 0);
                findViewById(R.id.pbOne).setVisibility(View.GONE);
                findViewById(R.id.tvSendingImageOne).setVisibility(View.GONE);
                findViewById(R.id.pbTwo).setVisibility(View.GONE);
                findViewById(R.id.tvSendingImageTwo).setVisibility(View.GONE);
            } else {
                HashMap<String, String> hmp = new HashMap<>();
                if (!TextUtils.isEmpty(issueid))
                    hmp.put("issueid", issueid);
                hmp.put("issueuniquekey", Long.toString(iUniqueKey));
                if (pictureOne) {
                    hmp.put("issueimage", ba1);
                    hmp.put("issueimagefilename", imageNameOne);
                    hmp.put("creationdatetime", creationDateTimeOne);
                    hmp.put("issueimagefilepath", mCurrentPhotoPathOne);
                }
                if (pictureTwo) {
                    hmp.put("issueimage", ba2);
                    hmp.put("issueimagefilename", imageNameTwo);
                    hmp.put("creationdatetime", creationDateTimeTwo);
                    hmp.put("issueimagefilepath", mCurrentPhotoPathTwo);
                }
                if (!Connectivity.checkConnected(IssueAct.this)) {
                    db.sentToserver(iUniqueKey, 0);
                    findViewById(R.id.pbOne).setVisibility(View.GONE);
                    findViewById(R.id.tvSendingImageOne).setVisibility(View.GONE);
                    findViewById(R.id.pbTwo).setVisibility(View.GONE);
                    findViewById(R.id.tvSendingImageTwo).setVisibility(View.GONE);

                } else {
                    addImage(IssueAct.this, url, hmp);
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void resetData() {
        mCurrentLocation = CGlobals_lib_ss.getInstance().getMyLocation(this);
        if (mCurrentLocation != null) {
            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();
            geoHelper.getAddress(IssueAct.this, mCurrentLocation, onGeoHelperResult);
        }
        findViewById(R.id.txtExtra).setVisibility(View.VISIBLE);
        ivNoPlate.setVisibility(View.VISIBLE);
        findViewById(R.id.imgOne).setVisibility(View.GONE);
        findViewById(R.id.imgTwo).setVisibility(View.GONE);
        findViewById(R.id.imgCancelRedTwo).setVisibility(View.GONE);
        findViewById(R.id.imgCancelRedOne).setVisibility(View.GONE);
        findViewById(R.id.pbOne).setVisibility(View.GONE);
        findViewById(R.id.pbTwo).setVisibility(View.GONE);
        findViewById(R.id.tvSendingImageOne).setVisibility(View.GONE);
        findViewById(R.id.tvSendingImageTwo).setVisibility(View.GONE);
    }

    private void setDate() {
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT,
                Locale.getDefault());
        issue_time = ft.format(dNow);
        String date, time;
        ft = new SimpleDateFormat("yyyy-MM-dd");
        date = ft.format(dNow);
        ft = new SimpleDateFormat("HH:mm:ss");
        time = ft.format(dNow);
        etIssueTime.setText("Date " + date + " Time " + time);
    }

    private void DeleteAlertBox(final int imgView, final int imgCancelRedButton, final int textView, final String creationTime, final String index) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(IssueAct.this);
        alertDialogBuilder.setMessage(R.string.delete_warning);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (index.equals("one")) {
                            baOne = null;
                            findViewById(R.id.pbOne).setVisibility(View.GONE);
                            findViewById(R.id.tvSendingImageOne).setVisibility(View.GONE);
                        } else if (index.equals("two")) {
                            baTwo = null;
                            findViewById(R.id.pbTwo).setVisibility(View.GONE);
                            findViewById(R.id.tvSendingImageTwo).setVisibility(View.GONE);
                            final HashMap<String, String> hmp = new HashMap<>();
                            hmp.put("issueid", issueid);
                            hmp.put("creationdatetime", creationDateTimeTwo);
                            deleteImagesFromServer(IssueAct.this, hmp);
                        }
                        db.deleteImage(creationTime);
                        findViewById(imgView).setVisibility(View.GONE);
                        findViewById(imgCancelRedButton).setVisibility(View.GONE);
                        findViewById(textView).setVisibility(View.VISIBLE);
                        findViewById(R.id.takephoto).setVisibility(View.VISIBLE);
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void sendEmail() {
        String imagefilename = "";
        imagefilename = imagefilename + ", images/" + imageNameOne + ","
                + ", images/" + imageNameTwo + ",";

        final HashMap<String, String> hmp = new HashMap<>();
        hmp.put("issueid", issueid);
        hmp.put("uniquekey", Long.toString(iUniqueKey));
        hmp.put("issueimagefilename", imagefilename);

        final String url = Constants_dp.ISSUE_EMAIL_URL;
        sendEmailUrl(IssueAct.this, url, hmp);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (baOne == null && TextUtils.isEmpty(issueid)) {
            getAddress();
            setDate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        writeRecentCat();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            imgOne.setImageBitmap(null);
            imgTwo.setImageBitmap(null);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
                System.gc();
            }
        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG, "onDestroy", e);
        }
    }

    @Override
    protected void onStop() {
        try {
            writeRecentCat();
        } catch (Exception e) {
            SSLog.e("IssueAct:", " onStop - ", e.getMessage());

        }
        super.onStop();
        active = false;
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onBackPressed() {
        issueCat = txtTypeOffence.getText().toString();
        if (baOne == null && TextUtils.isEmpty(issueCat)) {
            IssueAct.this.finish();
        } else {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(getString(R.string.lose_info));
            alertDialogBuilder.setPositiveButton(android.R.string.yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            try {
                                IssueAct.this.finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
            alertDialogBuilder.setNegativeButton(android.R.string.no,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }


    public void addIssue() {

        final String url = Constants_dp.ADD_ISSUE_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response.trim();
                        if (!response.trim().equals("-1") || !TextUtils.isEmpty(response)) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                issueid = jsonObject.getString("issue_id");
                                if (!TextUtils.isEmpty(issueid)) {
                                    Constants_dp.GET_LAST_UNIQUE_KEY = db.getLastinsertedUniqueKey();
                                    if (!poorNetFlag) {
                                        db.sentToserver(iUniqueKey, 1);
                                    } else {
                                        db.sentToserver(iUniqueKey, 0);
                                    }
                                    if (isBtnDoneFlag) {
                                        if (!poorNetFlag && !isMailSent) {
                                            sendEmail();
                                            Log.d(TAG, "SEND MAIL");
                                        }
                                        if (getTrafficType().equals("MU_"))
                                            showInformationalDialog(IssueAct.this, getString(R.string.municipalissuesubmitted));
                                        else
                                            showInformationalDialog(IssueAct.this, getString(R.string.trafficissuesubmitted));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                SSLog.e(TAG, "addIssue", e.getMessage());
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isBtnDoneFlag) {
                    if (getTrafficType().equals("MU_"))
                        showInformationalDialog(IssueAct.this, getString(R.string.municipalissuesubmitted));
                    else
                        showInformationalDialog(IssueAct.this, getString(R.string.trafficissuesubmitted));

                }
                db.sentToserver(iUniqueKey, 0);
                Log.d(TAG, url + " Error: " + error.getMessage());
                SSLog.e(TAG, "addIssue", error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                issueAddress = etIssueAdd.getText().toString().trim();
                if (!TextUtils.isEmpty(issueAddress)) {
                    params.put("issueaddress", issueAddress);
                }
                if (!TextUtils.isEmpty(issue_time)) {
                    params.put("issuedatetime", issue_time);
                }
                if (latitude == Constants_lib_ss.INVALIDLAT && longitude == Constants_lib_ss.INVALIDLNG) {
                    latitude = mCurrentLocation.getLatitude();
                    longitude = mCurrentLocation.getLongitude();
                }
                if (!TextUtils.isEmpty(Long.toString(iUniqueKey))) {
                    params.put("uniquekey", Long.toString(iUniqueKey));
                }


                if (isBtnDoneFlag) {
                    /*if (!poorNetFlag)*/
                    params.put("submitreport", String.valueOf(1));
                    if (!TextUtils.isEmpty(sIssueitemcode)) {
                        params.put("issueitemcode", sIssueitemcode);
                    }
                    if (getTrafficType().equals("MU_"))
                        mWardno = spnWardNo.getSelectedItem().toString().trim();
                    if (mWardno.equalsIgnoreCase("Select")) {
                        mWardno = "";
                    }
                    if (!TextUtils.isEmpty(mWardno)) {
                        params.put("ward", mWardno);
                    }
                } else
                    params.put("submitreport", String.valueOf(0));
                if (getTrafficType().equals("TR_") && !TextUtils.isEmpty(etVehicleNo.getText().toString())) {
                    params.put("vehicleno", etVehicleNo.getText().toString());
                }

                if (!TextUtils.isEmpty(sublocality)) {
                    params.put("sublocality", sublocality);
                }
                if (!TextUtils.isEmpty(cityname)) { //locality
                    params.put("locality", cityname);
                }
                if (!TextUtils.isEmpty(postalcode)) {
                    params.put("postalcode", postalcode);
                }
                if (!TextUtils.isEmpty(route)) {
                    params.put("route", route);
                }

                if (!TextUtils.isEmpty(neighborhood)) {
                    params.put("neighborhood", neighborhood);
                }
                if (!TextUtils.isEmpty(administrative_area_level_2)) {
                    params.put("administrative_area_level_2", administrative_area_level_2);
                }
                if (!TextUtils.isEmpty(administrative_area_level_1)) {
                    params.put("administrative_area_level_1", administrative_area_level_1);
                }
                if (latitude != Constants_lib_ss.INVALIDLAT) {
                    params.put("latitude", Double.toString(latitude));
                }
                if (longitude != Constants_lib_ss.INVALIDLNG) {
                    params.put("longitude", Double.toString(longitude));
                }
                if (!TextUtils.isEmpty(mVehicleNo))
                    params.put("vehicleno", mVehicleNo);
                params.put("discard_report", String.valueOf("0"));

                params = CGlobals_db.getInstance(IssueAct.this).getBasicMobileParams(params,
                        url, IssueAct.this);
                return CGlobals_db.getInstance(IssueAct.this).checkParams(params);
            }
        };
        CGlobals_db.getInstance(IssueAct.this).getRequestQueue(IssueAct.this).add(postRequest);
    }

    public void addImage(final Context context, final String url, final HashMap<String, String> hmp) {

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response.trim();
                        if (!response.trim().equals("-1") || !TextUtils.isEmpty(response)) {
                            try {
                                if (!poorNetFlag) {
                                    Constants_dp.db.sentToserver(Constants_dp.GET_LAST_UNIQUE_KEY, 1);
                                } else {
                                    Constants_dp.db.sentToserver(Constants_dp.GET_LAST_UNIQUE_KEY, 0);
                                }

                                if (pictureOne) {
                                    findViewById(R.id.pbOne).setVisibility(View.GONE);
                                    findViewById(R.id.tvSendingImageOne).setVisibility(View.GONE);
                                }
                                if (pictureTwo) {
                                    findViewById(R.id.pbTwo).setVisibility(View.GONE);
                                    findViewById(R.id.tvSendingImageTwo).setVisibility(View.GONE);
                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            poorNetFlag = true;
                            Constants_dp.db.sentToserver(Constants_dp.GET_LAST_UNIQUE_KEY, 0);
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                findViewById(R.id.pbOne).setVisibility(View.GONE);
                findViewById(R.id.tvSendingImageOne).setVisibility(View.GONE);
                findViewById(R.id.pbTwo).setVisibility(View.GONE);
                findViewById(R.id.tvSendingImageTwo).setVisibility(View.GONE);
                Constants_dp.db.sentToserver(Constants_dp.GET_LAST_UNIQUE_KEY, 0);
                poorNetFlag = true;
                SSLog.e(TAG, url + " Error: ", error);

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                String column, value;
                Map<String, String> params = new HashMap<>();

                Set set = hmp.entrySet();
                for (Object aSet : set) {
                    Map.Entry me = (Map.Entry) aSet;
                    column = me.getKey().toString();
                    value = me.getValue().toString();
                    params.put(column, value);
                }
                params = CGlobals_db.getInstance(context).getBasicMobileParams(params,
                        url, context);

                return CGlobals_db.getInstance(context).checkParams(params);
            }
        };
        CGlobals_db.getInstance(context).getRequestQueue(context).add(postRequest);

    }

    public void sendEmailUrl(final Context context, final String url, final HashMap<String, String> hmp) {
        isMailSent = true;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response.trim();
                        if (!response.trim().equals("-1")) {
                            Log.d(TAG, "email not sent");
                            isMailSent = true;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isMailSent = false;
                db.sentToserver(iUniqueKey, 0);
                SSLog.e(TAG, "sendEmailUrl", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                String column, value;
                Map<String, String> params = new HashMap<>();

                Set set = hmp.entrySet();
                for (Object aSet : set) {
                    Map.Entry me = (Map.Entry) aSet;
                    column = me.getKey().toString();
                    value = me.getValue().toString();
                    params.put(column, value);

                }
                params = CGlobals_db.getInstance(context).getBasicMobileParams(params,
                        url, context);

                return CGlobals_db.getInstance(context).checkParams(params);
            }
        };
        CGlobals_db.getInstance(context).getRequestQueue(context).add(postRequest);
    }

    private void deleteImagesFromServer(final Context context, final HashMap<String, String> hmp) {
        final String url = Constants_dp.DELETE_CANCELLED_IMAGE_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response.trim();
                        if (!response.trim().equals("-1")) {
                            Log.d(TAG, url + "image is deleted");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, url + " Error: " + error.getMessage());
                SSLog.e(TAG, "sendEmailUrl", error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                String column, value;
                Map<String, String> params = new HashMap<>();

                Set set = hmp.entrySet();
                for (Object aSet : set) {
                    Map.Entry me = (Map.Entry) aSet;
                    column = me.getKey().toString();
                    value = me.getValue().toString();
                    params.put(column, value);
                }
                params = CGlobals_db.getInstance(context).getBasicMobileParams(params,
                        url, context);
                return CGlobals_db.getInstance(context).checkParams(params);
            }
        };
        CGlobals_db.getInstance(context).getRequestQueue(context).add(postRequest);
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        issueAddress = etIssueAdd.getText().toString().trim();
        issueCat = txtTypeOffence.getText().toString().trim();

        savedInstanceState.putString("issueid", issueid);
        savedInstanceState.putLong("iUniqueKey", iUniqueKey);
        savedInstanceState.putBoolean("pictureOne", pictureOne);
        savedInstanceState.putBoolean("pictureTwo", pictureTwo);
        savedInstanceState.putString("creationDateTimeOne", creationDateTimeOne);
        savedInstanceState.putString("creationDateTimeTwo", creationDateTimeTwo);

        savedInstanceState.putString("imageNameOne", imageNameOne);
        savedInstanceState.putString("imageNameTwo", imageNameTwo);

        savedInstanceState.putString("ba1", ba1);
        savedInstanceState.putString("ba2", ba2);

        savedInstanceState.putString("mCurrentPhotoPath", mCurrentPhotoPathOne);
        savedInstanceState.putString("mCurrentPhotoPathTwo", mCurrentPhotoPathTwo);
        savedInstanceState.putString("sIssueDescription", issueCat);

        try {
            if (getTrafficType().equals("MU_"))
                mWardno = spnWardNo.getSelectedItem().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        savedInstanceState.putString("wardno", mWardno);
        savedInstanceState.putString("vehicleno", etVehicleNo.getText().toString());
        savedInstanceState.putByteArray("baOne", baOne);
        savedInstanceState.putByteArray("baTwo", baTwo);
        savedInstanceState.putStringArrayList("mWardList", mWardList);
        savedInstanceState.putString("issueitemcode", sIssueitemcode);
        savedInstanceState.putString("issueaddress", issueAddress);
        savedInstanceState.putString("issuedatetime", issue_time);
        savedInstanceState.putString("postalcode", postalcode);
        savedInstanceState.putString("cityname", cityname);
        savedInstanceState.putString("sublocality", sublocality);
        savedInstanceState.putString("route", route);
        savedInstanceState.putString("neighborhood", neighborhood);
        savedInstanceState.putString("administrative_area_level_2", administrative_area_level_2);
        savedInstanceState.putString("administrative_area_level_1", administrative_area_level_1);
        savedInstanceState.putDouble("latitude", latitude);
        savedInstanceState.putDouble("longitude", longitude);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void showInformationalDialog(Context context, String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(context.getApplicationContext().
                getString(R.string.appTitle));
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        try {
                            resetData();
                            Intent intent = new Intent(IssueAct.this, MyComplaintList.class);
                            startActivity(intent);
                            findViewById(R.id.btnSubmitIssue).setVisibility(View.GONE);
                            IssueAct.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        if (active)
            alertDialog.show();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(IssueAct.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(IssueAct.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: " + pendingResult.toString());
    }

    private void writeRecentCat() {
        new Thread(new Runnable() {
            public void run() {
                StringBuilder sb = new StringBuilder();
                String lineDelim = "";
                int iLen = maoRecentCat.size();
                if (iLen < 1)
                    return;
                String s;
                for (int i = 0; i < iLen; i++) {
                    s = maoRecentCat.get(i).getIssueDescription()
                            + ";" + maoRecentCat.get(i).getIssueitemcode()
                            + ";" + Boolean.toString(maoRecentCat.get(i).getImageVerification())
                            + ";" + Boolean.toString(maoRecentCat.get(i).getVehicleNumber())
                            + ";" + maoRecentCat.get(i).getIssueType();
                    if (!TextUtils.isEmpty(s)) {
                        sb.append(lineDelim).append(s);
                        lineDelim = "\n";
                    }
                }
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(IssueAct.this).
                        putString(Constants_dp.PREF_RECENT_CAT, sb.toString()).apply();
            }
        }).start();
    } // writeRecentCat

    void addRecentCat(String issueDescription, String issueitemcode, boolean imageVerification, boolean vehicleNumber, String issueType) {
        if (maoRecentCat == null)
            maoRecentCat = new ArrayList<>();
        if (masRecentCatList == null)
            masRecentCatList = new ArrayList<>();
        maoRecentCat.add(0, new CRecentCategory(issueDescription, issueitemcode, imageVerification, vehicleNumber, issueType)); // add as first trip FIFO
        int iLen = maoRecentCat.size();
        if (iLen > MAX_RECENT_CATS)
            maoRecentCat.remove(iLen - 1);
        iLen = maoRecentCat.size();
        CRecentCategory rcategory;
        for (int i = 1; i < iLen; i++) {
            rcategory = maoRecentCat.get(i);
            if (rcategory.getIssueDescription().equals(issueDescription)) {
                maoRecentCat.remove(i);
                break;
            }
        }
        masRecentCatList.clear();
        for (CRecentCategory tr : maoRecentCat) {
            masRecentCatList.add(tr.getIssueDescription());
        }

        mRecentCatsAdapter = new ListAdapter(IssueAct.this,
                masRecentCatList);

    }

    ArrayList<CRecentCategory> readRecentCat() {
        ArrayList<CRecentCategory> aRC = new ArrayList<>();


        String sRecentCats = CGlobals_lib_ss.getInstance().getPersistentPreference(IssueAct.this)
                .getString(Constants_dp.PREF_RECENT_CAT,
                        null);
        SSLog.i(TAG, "readRecentTrips - " + sRecentCats);
        if (sRecentCats == null)
            return aRC;
        String catPair;
        String[] s;


        String iDescription, iCode, iType;
        boolean iPic, iVehicle;
        if (!TextUtils.isEmpty(sRecentCats) & sRecentCats.trim().length() > 0) {
            String as[] = sRecentCats.split("\n");
            for (String a : as) {
                catPair = a;
                if (catPair != null) {

                    try {
                        SSLog.i(TAG, "Recent Cats: " + catPair);
                        s = catPair.split(";");
                        iDescription = s[0];
                        iCode = s[1];
                        iPic = Boolean.parseBoolean(s[2]);
                        iVehicle = Boolean.parseBoolean(s[3]);
                        iType = s[4];
                        aRC.add(new CRecentCategory(iDescription, iCode, iPic, iVehicle, iType));

                    } catch (Exception e) {
                        SSLog.e(TAG, "readRecentCat - ", e.getMessage());
                    }
                }
            }
        }
        masRecentCatList.clear();
        for (CRecentCategory tr : aRC) {
            if (tr.getIssueType().equals(getTrafficType()))
                masRecentCatList.add(tr.getIssueDescription());
        }
        mRecentCatsAdapter = new ListAdapter(IssueAct.this,
                masRecentCatList);

        return aRC;
    } // readRecentTrips

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            CGlobals_lib_ss.setMyLocation(location, false, IssueAct.this);

            try {
                CGlobals_lib_ss.setMyLocation(location, false, IssueAct.this);

            } catch (Exception e) {
                SSLog_SS.e(TAG, "LocationResult", e, IssueAct.this);
            }
        }
    };

    public void showSnackbar(String msg) {
        snackbar = Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public int getIndex(Spinner spinner, String myString) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        return index;
    }

    private void getWard(final double lat, final double lng) {
        final String url = Constants_dp.GET_WARD_FROM_LAT_LNG_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response.trim();
                        JSONObject jobj;
                        if (!response.trim().equals("-1") || !TextUtils.isEmpty(response)) {
                            try {
                                jobj = new JSONObject(response);
                                mWardno = jobj.getString("ward");
                                if (!TextUtils.isEmpty(mWardno)) {
                                    tvWard.setText(mWardno);
                                    spnWardNo.setSelection(getIndex(spnWardNo, mWardno));
                                    mWardno = CGlobals_lib_ss.getInstance().getPersistentPreference(IssueAct.this).
                                            getString(Constants_dp.PREF_WARD, "");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                SSLog.e(TAG, "getWard", e.getMessage());
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                SSLog.e(TAG, "getWard", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (lat != Constants_lib_ss.INVALIDLAT) {
                    params.put("latitude", Double.toString(lat));
                }
                if (lng != Constants_lib_ss.INVALIDLAT) {
                    params.put("longitude", Double.toString(lng));
                }
                params = CGlobals_db.getInstance(IssueAct.this).getBasicMobileParams(params,
                        url, IssueAct.this);
                return CGlobals_db.getInstance(IssueAct.this).checkParams(params);
            }
        };
        CGlobals_db.getInstance(IssueAct.this).getRequestQueue(IssueAct.this).add(postRequest);
    }
}