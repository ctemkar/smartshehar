package com.jumpinjumpout.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.CGlobals_dp;
import com.jumpinjumpout.CMember;
import com.jumpinjumpout.Constants_dp;
import com.jumpinjumpout.DatePickerClass;
import com.jumpinjumpout.DriverInfo;
import com.jumpinjumpout.PrefUtils;
import com.jumpinjumpout.SSApp;
import com.jumpinjumpout.www.driverprofile.R;

import org.apache.commons.lang3.text.WordUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import lib.app.util.CAddress;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.FetchAddressIntentService;
import lib.app.util.GeoHelper;
import lib.app.util.SSLog_SS;
import lib.app.util.ui.CameraActivity;


public class ActDriverProfileRegistration extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        DatePickerClass.OnDateListener {
    boolean driverPic, licencePic, panPic, aadharPic;
    String creationDateTimeDriver = "", creationDateTimeLicence = "", creationDateTimePan = "", creationDateTimeAadhar = "";
    public String /*strImag,*/ imageName, mCurrentPhotoPath, strDriverImg, strPanImg, strAadharImg, strLicenceImg;
    byte[] ba = null, baDriver = null, baPan = null, baAadhar = null, baLicence = null;
    long iUniqueKey = 0;
    TextView txtByWhome;
    Location mCurrentLocation;
    public AddressResultReceiver mResultReceiver;
    boolean doubleBackToExitPressedOnce = false, backPressed = true;
    public EditText etNameFirst, etPhoneNo, etVehicleNo, etLicenceNo, etNameLast, etAadharNo,
            etTempAddrOne, etTempAddrTwo, etTempCity, etTempState, etTempPincode, etTempLandmark,
            etPerAddrOne, etPerAddrTwo, etPerCity, etPerState, etPerPincode, etPerLandmark,
            etDriverEmailid, etPan, etPermitNo, etBadgeNo, etStay, etRemark, etInternalComment,
            etEnterName, etOLAId, etUBRId, etMRUId, etBMCId, etECAId, etTABId, etTFSId,
            etVIRId, etPRIId, etARYId, etBNYId, etOTHId, tvAge,/* etPaymentAmount,*/
            etReceiptNo, etReceiptDae, etNameOfPerson, etReceiptNoTwo, etReceiptNoThree, /*etBackoutReason,*/
            etExpDate, etReceiptDateTwo, etReceiptDateThree;
    public ImageView imgDriver, imgAadhar, imgLicence, imgPan, imgCancelRedDriver, imgCancelRedTwo,
            imgCameraDriver, imgCameraPan, imgCameraAadhar, imgCameraLicence;
    public TextView takephoto, txtPersonal,
            txtDriving, txtRequirement, txtPhoto, tvAdd, tvTime;
    public static final String TAG = "ActRegistration";
    public String postalcode = "", cityname = "", sublocality = "", formatted_address = "",
            route = "", neighborhood = "", administrative_area_level_2 = "", administrative_area_level_1 = "", mAddressOutput;
    public double latitude = Constants_lib_ss.INVALIDLAT, longitude = Constants_lib_ss.INVALIDLNG;
    public static final int REQUEST_PHOTO = 1;
    GoogleApiClient mGoogleApiClient;
    Connectivity mConnectivity;
    GeoHelper geoHelper;
    Date dNow;
    SimpleDateFormat ft;
    RadioGroup rgGender, rgPermit, rgBadge, rgCab, /*rgPaymentSatus,*/
            rgHowDriverCome,
            rgReferByWhome, rgPayment;
    RadioButton rbGender, rbPermit, rbBadge, rbCab, rbPay, rbHowDriverCome,
            rbReferByWhome, rbPayment;
    String mAdd, mClientDateTime, mFirstName, mLastName, mDob, dispDate, mTempLineOne, mTempLineTwo, mImageClientDateTime,
            mTempCity, mTempState, mTempPincode, mTempLandmark,
            mPerLineOne, mPerLineTwo, mGender, mPanNo, mAadharNo, mLicenceNo, mPermitFlag, mPermitNo,
            mBadgeFlag, mBadgeNo, mStayInMumbai, mOtherName, mVehicleNo,
            mPerCity, mPerState, mPerPincode, mPerLandmark, mPhoneNo, mDriverEmailID,
            mCabNeedFlag, mRemark, mInternalComment, mOLAId, mUBRId, mMRUId, mBMCId,
            mECAId, mTABId, mTFSId, mVIRId, mPRIId, mARYId, mBNYId, mOTHId,
            mOtherLookup, mPaymentAmount, mPaymentFlag, mReceiptNo, mReceiptNo2, mReceiptNo3, mReceiptDate,
            mDisRecDate, mUniqueId, mExpDate, mDispExpDate, mReferralWalkin, mReferByWhome, mNameOfPerson,
    /*mBackoutReason,*/ mReceiptDateTwo, mDisRecDateTwo, mReceiptDateThree, mDisRecDateThree;
    Bitmap bitmap;
    CheckBox chkAdd, /*chkBackOut,*/
            chkOLA, chkUBR, chkMRU, chkBMC, chkECA,
            chkTAB, chkTFS, chkVIR, chkPRI, chkARY, chkBNY, chkOTH;
    DialogFragment dateFragment;
    CGlobals_dp mApp = null;
    String mDriverImgName, mDriverImgPath, mDriverImgClientDateTime,
            mPanImgName, mPanImgPath, mPanImgClientDateTime,
            mAdharImgName, mAdharImgPath, mAdharImgClientDateTime,
            mLicenseImgName, mLicenseImgPath, mLicenseImgClientDateTime, oldPhoneNo, driverprofileid;
    boolean saveData, dateFrag, dobFlag, receiptFlag, expFlag, receiptTwoFlag, receiptThreeFlag;
    RelativeLayout rlAdd;
    boolean bRec2Flag, bRec3Flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_profile);
        geoHelper = new GeoHelper();
        mGoogleApiClient = new GoogleApiClient.
                Builder(ActDriverProfileRegistration.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(ActDriverProfileRegistration.this)
                .addOnConnectionFailedListener(ActDriverProfileRegistration.this)
                .build();
        mGoogleApiClient.connect();
        mConnectivity = new Connectivity();
        mApp = CGlobals_dp.getInstance(ActDriverProfileRegistration.this);
        mApp.init(this);

        dateFragment = new DatePickerClass();
        dNow = new Date();
        ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT,
                Locale.getDefault());
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setUpUi();
       /* mlReasons = Arrays.asList(getResources().getStringArray(R.array.reasons));
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mlReasons);
        spnBackoutReason.setAdapter(mAdapter);*/

        Bundle extras = getIntent().getBundleExtra("data");
        if (extras != null) {
            oldPhoneNo = extras.getString("driverphoneno");
            saveData = extras.getBoolean("savedData");
            getDriverProfile();
        } else {
            iUniqueKey = System.currentTimeMillis();
            getAddress();
        }
        try {
            if (savedInstanceState != null) {
                mDob = savedInstanceState.getString("mDob");
                dispDate = savedInstanceState.getString("dispDate");
                if (!TextUtils.isEmpty(dispDate))
                    tvAge.setText(dispDate);

                findViewById(R.id.svPersonl).setVisibility(View.GONE);
                findViewById(R.id.svProfile).setVisibility(View.GONE);
                findViewById(R.id.svRequirement).setVisibility(View.GONE);
                findViewById(R.id.svPhoto).setVisibility(View.VISIBLE);
                driverPic = savedInstanceState.getBoolean("driverPic");
                licencePic = savedInstanceState.getBoolean("licencePic");
                panPic = savedInstanceState.getBoolean("panPic");
                aadharPic = savedInstanceState.getBoolean("aadharPic");
                mDriverImgName = savedInstanceState.getString("mDriverImgName");
                mDriverImgPath = savedInstanceState.getString("mDriverImgPath");
                strDriverImg = savedInstanceState.getString("strDriverImg");
                mDriverImgClientDateTime = savedInstanceState.getString("mDriverImgClientDateTime");
                if (!TextUtils.isEmpty(mDriverImgPath)) {
                    bitmap = null;
                    File photo = new File(mDriverImgPath);
                    bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath());
                    imgDriver.setImageBitmap(bitmap);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bao);
                    baDriver = bao.toByteArray();
                    imgDriver.setVisibility(View.VISIBLE);
                    findViewById(R.id.imgCancelRedDriver).setVisibility(View.VISIBLE);
                    if (bao != null) {
                        bao.flush();
                        bao.close();
                    }
                }
                mAdharImgName = savedInstanceState.getString("mAdharImgName");
                mAdharImgPath = savedInstanceState.getString("mAdharImgPath");
                strAadharImg = savedInstanceState.getString("strAadharImg");
                mAdharImgClientDateTime = savedInstanceState.getString("mAdharImgClientDateTime");

                if (!TextUtils.isEmpty(mAdharImgPath)) {
                    bitmap = null;
                    File photo = new File(mAdharImgPath);
                    bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath());
                    imgAadhar.setImageBitmap(bitmap);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bao);
                    baAadhar = bao.toByteArray();
                    imgAadhar.setVisibility(View.VISIBLE);
                    findViewById(R.id.imgCancelRedAadhar).setVisibility(View.VISIBLE);
                    if (bao != null) {
                        bao.flush();
                        bao.close();
                    }
                }

                mPanImgName = savedInstanceState.getString("mPanImgName");
                mPanImgPath = savedInstanceState.getString("mPanImgPath");
                strPanImg = savedInstanceState.getString("strPanImg");
                mPanImgClientDateTime = savedInstanceState.getString("mPanImgClientDateTime");

                if (!TextUtils.isEmpty(mPanImgPath)) {
                    bitmap = null;
                    File photo = new File(mPanImgPath);
                    bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath());
                    imgPan.setImageBitmap(bitmap);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bao);
                    baPan = bao.toByteArray();
                    imgPan.setVisibility(View.VISIBLE);
                    findViewById(R.id.imgCancelRedPan).setVisibility(View.VISIBLE);
                    if (bao != null) {
                        bao.flush();
                        bao.close();
                    }
                }

                mLicenseImgName = savedInstanceState.getString("mLicenseImgName");
                mLicenseImgPath = savedInstanceState.getString("mLicenseImgPath");
                strLicenceImg = savedInstanceState.getString("strLicenceImg");
                mLicenseImgClientDateTime = savedInstanceState.getString("mLicenseImgClientDateTime");

                if (!TextUtils.isEmpty(mLicenseImgPath)) {
                    bitmap = null;
                    File photo = new File(mLicenseImgPath);
                    bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath());
                    imgLicence.setImageBitmap(bitmap);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bao);
                    baLicence = bao.toByteArray();
                    imgLicence.setVisibility(View.VISIBLE);
                    findViewById(R.id.imgCancelRedLicence).setVisibility(View.VISIBLE);
                    if (bao != null) {
                        bao.flush();
                        bao.close();
                    }
                }
                setPic();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        chkAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etPerAddrOne.setText(etTempAddrOne.getText());
                    etPerAddrTwo.setText(etTempAddrTwo.getText());
                    etPerCity.setText(etTempCity.getText());
                    etPerState.setText(etTempState.getText());
                    etPerPincode.setText(etTempPincode.getText());
                    etPerLandmark.setText(etTempLandmark.getText());
                } else {
                    etPerAddrOne.setText("");
                    etPerAddrTwo.setText("");
                    etPerCity.setText("");
                    etPerState.setText("");
                    etPerPincode.setText("");
                    etPerLandmark.setText("");
                }
            }
        });
        chkOLA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etOLAId.setVisibility(View.VISIBLE);
                } else {
                    etOLAId.setVisibility(View.GONE);
                }
            }
        });
        chkUBR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etUBRId.setVisibility(View.VISIBLE);
                } else {
                    etUBRId.setVisibility(View.GONE);
                }
            }
        });
        chkMRU.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etMRUId.setVisibility(View.VISIBLE);
                } else {
                    etMRUId.setVisibility(View.GONE);
                }
            }
        });
        chkBMC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etBMCId.setVisibility(View.VISIBLE);
                } else {
                    etBMCId.setVisibility(View.GONE);
                }
            }
        });
        chkECA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etECAId.setVisibility(View.VISIBLE);
                } else {
                    etECAId.setVisibility(View.GONE);
                }
            }
        });
        chkTAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etTABId.setVisibility(View.VISIBLE);
                } else {
                    etTABId.setVisibility(View.GONE);
                }
            }
        });
        chkTFS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etTFSId.setVisibility(View.VISIBLE);
                } else {
                    etTFSId.setVisibility(View.GONE);
                }
            }
        });
        chkVIR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etVIRId.setVisibility(View.VISIBLE);
                } else {
                    etVIRId.setVisibility(View.GONE);
                }
            }
        });
        chkPRI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etPRIId.setVisibility(View.VISIBLE);
                } else {
                    etPRIId.setVisibility(View.GONE);
                }
            }
        });
        chkARY.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etARYId.setVisibility(View.VISIBLE);
                } else {
                    etARYId.setVisibility(View.GONE);
                }
            }
        });
        chkBNY.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etBNYId.setVisibility(View.VISIBLE);
                } else {
                    etBNYId.setVisibility(View.GONE);
                }
            }
        });
        chkOTH.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etOTHId.setVisibility(View.VISIBLE);
                    etEnterName.setVisibility(View.VISIBLE);
                } else {
                    etOTHId.setVisibility(View.GONE);
                    etEnterName.setVisibility(View.GONE);
                }
            }
        });
        tvAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!dateFrag) {
                        dobFlag = true;
                        receiptFlag = false;
                        expFlag = false;
                        receiptTwoFlag = false;
                        receiptThreeFlag = false;
                        dateFragment.show(getSupportFragmentManager(), "datePicker");
                        dateFragment.setCancelable(false);
                        dateFragment.getDialog().setCanceledOnTouchOutside(false);
                        dateFrag = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        etReceiptDae.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!dateFrag) {
                        dobFlag = false;
                        receiptFlag = true;
                        expFlag = false;
                        receiptTwoFlag = false;
                        receiptThreeFlag = false;
                        dateFragment.show(getSupportFragmentManager(), "datePicker");
                        dateFragment.setCancelable(false);
                        dateFragment.getDialog().setCanceledOnTouchOutside(false);
                        dateFrag = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        etExpDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!dateFrag) {
                        dobFlag = false;
                        receiptFlag = false;
                        expFlag = true;
                        receiptTwoFlag = false;
                        receiptThreeFlag = false;
                        dateFragment.show(getSupportFragmentManager(), "datePicker");
                        dateFragment.setCancelable(false);
                        dateFragment.getDialog().setCanceledOnTouchOutside(false);
                        dateFrag = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        etReceiptDateTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!dateFrag) {
                        dobFlag = false;
                        receiptFlag = false;
                        expFlag = false;
                        receiptTwoFlag = true;
                        receiptThreeFlag = false;
                        dateFragment.show(getSupportFragmentManager(), "datePicker");
                        dateFragment.setCancelable(false);
                        dateFragment.getDialog().setCanceledOnTouchOutside(false);
                        dateFrag = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        etReceiptDateThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!dateFrag) {
                        dobFlag = false;
                        receiptFlag = false;
                        expFlag = false;
                        receiptTwoFlag = false;
                        receiptThreeFlag = true;
                        dateFragment.show(getSupportFragmentManager(), "datePicker");
                        dateFragment.setCancelable(false);
                        dateFragment.getDialog().setCanceledOnTouchOutside(false);
                        dateFrag = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        txtPersonal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtPersonal.setBackgroundResource(R.drawable.frame_selected);
                txtDriving.setBackgroundResource(R.drawable.thin_frame);
                txtRequirement.setBackgroundResource(R.drawable.thin_frame);
                txtPhoto.setBackgroundResource(R.drawable.thin_frame);
                findViewById(R.id.svPersonl).setVisibility(View.VISIBLE);
                findViewById(R.id.svProfile).setVisibility(View.GONE);
                findViewById(R.id.svRequirement).setVisibility(View.GONE);
                findViewById(R.id.svPhoto).setVisibility(View.GONE);

            }
        });
        txtDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                personalInfoValidation();

            }
        });
        txtRequirement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personalInfoValidation();
                driverProfileValidation();

            }
        });
        txtPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requirementValidation();
            }
        });
        imgCameraDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrefUtils.setKioskModeActive(false, getApplicationContext());
                driverPic = true;
                licencePic = false;
                panPic = false;
                aadharPic = false;
                if (TextUtils.isEmpty(creationDateTimeDriver)) {
                    creationDateTimeDriver = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT).format(new Date());
                }
                openCamera();
            }
        });
        imgCameraPan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrefUtils.setKioskModeActive(false, getApplicationContext());
                driverPic = false;
                licencePic = false;
                panPic = true;
                aadharPic = false;
                if (TextUtils.isEmpty(creationDateTimePan)) {
                    creationDateTimePan = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT).format(new Date());
                }
                openCamera();
            }
        });
        imgCameraAadhar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrefUtils.setKioskModeActive(false, getApplicationContext());
                driverPic = false;
                licencePic = false;
                panPic = false;
                aadharPic = true;
                if (TextUtils.isEmpty(creationDateTimeAadhar)) {
                    creationDateTimeAadhar = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT).format(new Date());
                }
                openCamera();
            }
        });
        imgCameraLicence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrefUtils.setKioskModeActive(false, getApplicationContext());
                driverPic = false;
                licencePic = true;
                panPic = false;
                aadharPic = false;
                if (TextUtils.isEmpty(creationDateTimeLicence)) {
                    creationDateTimeLicence = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT).format(new Date());
                }

                openCamera();
            }
        });

        findViewById(R.id.rbPermitYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPermitNo.setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.rbPermitNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPermitNo.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.rbBadgeYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etBadgeNo.setVisibility(View.VISIBLE);
                findViewById(R.id.txtYears).setVisibility(View.GONE);
                etStay.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.rbBadgeNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etBadgeNo.setVisibility(View.GONE);
                findViewById(R.id.txtYears).setVisibility(View.VISIBLE);
                etStay.setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.rbReferral).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rgReferByWhome.setVisibility(View.VISIBLE);
                txtByWhome.setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.rbWalkin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rgReferByWhome.setVisibility(View.GONE);
                txtByWhome.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.rbSupervisor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etNameOfPerson.setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.rbDriver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etNameOfPerson.setVisibility(View.VISIBLE);
            }
        });
      /*  spnBackoutReason.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBackoutReason = mlReasons.get(position);

            }
        });*/

        rlAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bRec2Flag) {
                    etReceiptNoTwo.setVisibility(View.VISIBLE);
                    etReceiptDateTwo.setVisibility(View.VISIBLE);
                    bRec2Flag = true;
                } else {
                    if (!bRec3Flag) {
                        rlAdd.setVisibility(View.GONE);
                        etReceiptNoThree.setVisibility(View.VISIBLE);
                        etReceiptDateThree.setVisibility(View.VISIBLE);
                        bRec3Flag = true;
                    }
                }
            }
        });
        PrefUtils.setKioskModeActive(true, getApplicationContext());
    }


    private void getDriverProfile() {

        setEnabled(false);
        ArrayList<DriverInfo> maList = mApp.mDBHelper.getDriverProfile(oldPhoneNo);

        String sUniqueKey;
        DriverInfo maDriverInfo;
        try {

            for (int j = 0; j < maList.size(); j++) {
                maDriverInfo = maList.get(j);
                sUniqueKey = maDriverInfo.getUnique_key();
                if (!TextUtils.isEmpty(sUniqueKey))
                    iUniqueKey = Long.parseLong(sUniqueKey);
                mClientDateTime = maDriverInfo.getClientdatetime();
                tvTime.setText(mClientDateTime);
                mFirstName = maDriverInfo.getDriver_firstname();
                etNameFirst.setText(mFirstName);
                mLastName = maDriverInfo.getDriver_lastname();
                etNameLast.setText(mLastName);
                mDob = maDriverInfo.getDriver_dob();
                ft = new SimpleDateFormat("yyyy-MM-dd");
                Date date = ft.parse(mDob);
                ft = new SimpleDateFormat("dd MMM, yyyy");
                dispDate = ft.format(date);
                tvAge.setText(dispDate);
                mPaymentAmount = maDriverInfo.getPayment_amount();


                if (!TextUtils.isEmpty(mPaymentAmount)) {
                    RadioButton payment = null;
                    if (mPaymentAmount.equalsIgnoreCase(getString(R.string.five_hundred))) {
                        payment = (RadioButton) findViewById(R.id.rb500);
                    } else if (mPaymentAmount.equalsIgnoreCase(getString(R.string.three_thousand))) {
                        payment = (RadioButton) findViewById(R.id.rb3000);
                    } else if (mPaymentAmount.equalsIgnoreCase(getString(R.string.three_thousand_five_hundred))) {
                        payment = (RadioButton) findViewById(R.id.rb3500);
                    }
                    payment.setChecked(true);
                }


                mUniqueId = maDriverInfo.getUniqueid();
                //mPaymentFlag = maDriverInfo.getPayment_status();
                /*if (!TextUtils.isEmpty(mPaymentFlag)) {
                    RadioButton payment;
                    if (mPaymentFlag.equalsIgnoreCase("1")) {
                        payment = (RadioButton) findViewById(R.id.rbPayYes);
                    } else {
                        payment = (RadioButton) findViewById(R.id.rbPayNo);
                    }
                    payment.setChecked(true);
                }*/
                mReferralWalkin = maDriverInfo.getReferral_walk_in();
                if (!TextUtils.isEmpty(mReferralWalkin)) {
                    RadioButton referral_walk_in;
                    if (mReferralWalkin.equals(getString(R.string.referral))) {
                        referral_walk_in = (RadioButton) findViewById(R.id.rbReferral);

                    } else {
                        referral_walk_in = (RadioButton) findViewById(R.id.rbWalkin);
                    }
                    referral_walk_in.setChecked(true);
                }
                String mReferByWhome_supervisor = maDriverInfo.getReferral_supervisor();
                if (!TextUtils.isEmpty(mReferByWhome_supervisor)) {
                    rgReferByWhome.setVisibility(View.VISIBLE);
                    txtByWhome.setVisibility(View.VISIBLE);
                    RadioButton referral_supervisor = (RadioButton) findViewById(R.id.rbSupervisor);
                    etNameOfPerson.setText(mReferByWhome_supervisor);
                    etNameOfPerson.setVisibility(View.VISIBLE);
                    referral_supervisor.setChecked(true);
                }
                String mReferByWhome_driver = maDriverInfo.getReferral_driver();
                if (!TextUtils.isEmpty(mReferByWhome_driver)) {
                    rgReferByWhome.setVisibility(View.VISIBLE);
                    txtByWhome.setVisibility(View.VISIBLE);
                    RadioButton referral_driver = (RadioButton) findViewById(R.id.rbDriver);
                    etNameOfPerson.setText(mReferByWhome_driver);
                    etNameOfPerson.setVisibility(View.VISIBLE);
                    referral_driver.setChecked(true);
                }

                mReceiptNo = maDriverInfo.getReceipt_no();
                if (!TextUtils.isEmpty(mReceiptNo)) {
                    etReceiptNo.setText(mReceiptNo);
                }
                mReceiptNo2 = maDriverInfo.getReceipt_no_2();
                if (!TextUtils.isEmpty(mReceiptNo2)) {
                    etReceiptNoTwo.setText(mReceiptNo2);
                    etReceiptNoTwo.setVisibility(View.VISIBLE);
                }
                mReceiptNo3 = maDriverInfo.getReceipt_no_3();
                if (!TextUtils.isEmpty(mReceiptNo3)) {
                    etReceiptNoThree.setText(mReceiptNo3);
                    etReceiptNoThree.setVisibility(View.VISIBLE);
                }

                mReceiptDate = maDriverInfo.getReceipt_date();
                if (!TextUtils.isEmpty(mReceiptDate)) {
                    ft = new SimpleDateFormat("yyyy-MM-dd");
                    date = ft.parse(mReceiptDate);
                    ft = new SimpleDateFormat("dd MMM, yyyy");
                    mDisRecDate = ft.format(date);
                    etReceiptDae.setText(dispDate);
                }

                mReceiptDateTwo = maDriverInfo.getReceipt_date_2();
                if (!TextUtils.isEmpty(mReceiptDateTwo)) {
                    ft = new SimpleDateFormat("yyyy-MM-dd");
                    date = ft.parse(mReceiptDateTwo);
                    ft = new SimpleDateFormat("dd MMM, yyyy");
                    mDisRecDateTwo = ft.format(date);
                    etReceiptDateTwo.setText(mDisRecDateTwo);
                    etReceiptDateTwo.setVisibility(View.VISIBLE);
                }

                mReceiptDateThree = maDriverInfo.getReceipt_date_3();
                if (!TextUtils.isEmpty(mReceiptDateThree)) {
                    ft = new SimpleDateFormat("yyyy-MM-dd");
                    date = ft.parse(mReceiptDateThree);
                    ft = new SimpleDateFormat("dd MMM, yyyy");
                    mDisRecDateThree = ft.format(date);
                    etReceiptDateThree.setText(mDisRecDateThree);
                    etReceiptDateThree.setVisibility(View.VISIBLE);
                    rlAdd.setVisibility(View.GONE);
                }
                mExpDate = maDriverInfo.getTr_license_exp_date();
                if (!TextUtils.isEmpty(mExpDate)) {
                    ft = new SimpleDateFormat("yyyy-MM-dd");
                    date = ft.parse(mExpDate);
                    ft = new SimpleDateFormat("dd MMM, yyyy");
                    mDispExpDate = ft.format(date);
                    etExpDate.setText(mDispExpDate);
                }

                mDriverEmailID = maDriverInfo.getDriver_email();
                etDriverEmailid.setText(mDriverEmailID);
                mPhoneNo = maDriverInfo.getDriver_phoneno();
                etPhoneNo.setText(mPhoneNo);
                mGender = maDriverInfo.getDriver_gender();
                if (!TextUtils.isEmpty(mGender)) {
                    RadioButton gender;
                    if (mGender.equalsIgnoreCase("Male"))
                        gender = (RadioButton) findViewById(R.id.rbMale);
                    else
                        gender = (RadioButton) findViewById(R.id.rbFemale);
                    gender.setChecked(true);
                }

                mPanNo = maDriverInfo.getDriver_pancard();
                etPan.setText(mPanNo);
                mAadharNo = maDriverInfo.getDriver_aadhar();
                etAadharNo.setText(mAadharNo);
                mLicenceNo = maDriverInfo.getDriver_license_no();
                etLicenceNo.setText(mLicenceNo);
                mPermitFlag = maDriverInfo.getDriver_permit_flag();
                if (!TextUtils.isEmpty(mPermitFlag)) {
                    RadioButton permit;
                    if (rbValues(mPermitFlag)) {
                        permit = (RadioButton) findViewById(R.id.rbPermitYes);
                        etPermitNo.setVisibility(View.VISIBLE);
                    } else {
                        permit = (RadioButton) findViewById(R.id.rbPermitNo);
                        etPermitNo.setVisibility(View.GONE);
                    }
                    permit.setChecked(true);
                }
                mPermitNo = maDriverInfo.getDriver_permit_no();
                etPermitNo.setText(mPermitNo);
                mBadgeFlag = maDriverInfo.getDriver_badge_flag();
                if (!TextUtils.isEmpty(mBadgeFlag)) {
                    RadioButton badge;
                    if (rbValues(mBadgeFlag)) {
                        badge = (RadioButton) findViewById(R.id.rbBadgeYes);
                        etBadgeNo.setVisibility(View.VISIBLE);
                        etStay.setVisibility(View.GONE);
                    } else {
                        badge = (RadioButton) findViewById(R.id.rbBadgeNo);
                        etBadgeNo.setVisibility(View.GONE);
                        etStay.setVisibility(View.VISIBLE);
                    }
                    badge.setChecked(true);
                }
                //
                mBadgeNo = maDriverInfo.getDriver_badge_no();
                etBadgeNo.setText(mBadgeNo);
                mStayInMumbai = maDriverInfo.getDriver_incity_since();
                etStay.setText(mStayInMumbai);
                mCabNeedFlag = maDriverInfo.getCab_needed_flag();
                if (!TextUtils.isEmpty(mCabNeedFlag)) {
                    RadioButton cab;
                    if (rbValues(mCabNeedFlag)) {
                        cab = (RadioButton) findViewById(R.id.rgCabYes);
                    } else {
                        cab = (RadioButton) findViewById(R.id.rgCabNo);
                    }
                    cab.setChecked(true);
                }
                //
                mVehicleNo = maDriverInfo.getDriver_vehicle_no();
                etVehicleNo.setText(mVehicleNo);
                mRemark = maDriverInfo.getRemark();
                etRemark.setText(mRemark);
                mInternalComment = maDriverInfo.getInternal_comment();
                etInternalComment.setText(mInternalComment);
                formatted_address = maDriverInfo.getGoogle_address();
                tvAdd.setText(formatted_address);
                mTempLineOne = maDriverInfo.getT_address_1();
                etTempAddrOne.setText(mTempLineOne);
                mTempLineTwo = maDriverInfo.getT_address_2();
                etTempAddrTwo.setText(mTempLineTwo);
                mTempCity = maDriverInfo.getT_city_town();
                etTempCity.setText(mTempCity);
                mTempLandmark = maDriverInfo.getT_landmark();
                etTempLandmark.setText(mTempLandmark);
                mTempState = maDriverInfo.getT_state();
                etTempState.setText(mTempState);
                mTempPincode = maDriverInfo.getT_pincode();
                etTempPincode.setText(mTempPincode);
                mPerLineOne = maDriverInfo.getP_address_1();
                etPerAddrOne.setText(mPerLineOne);
                mPerLineTwo = maDriverInfo.getP_address_2();
                etPerAddrTwo.setText(mPerLineTwo);
                mPerCity = maDriverInfo.getP_city_town();
                etPerCity.setText(mPerCity);
                mPerLandmark = maDriverInfo.getP_landmark();
                etPerLandmark.setText(mPerLandmark);
                mPerState = maDriverInfo.getP_state();
                etPerState.setText(mPerState);
                mPerPincode = maDriverInfo.getP_pincode();
                etPerPincode.setText(mPerPincode);
                mOtherName = maDriverInfo.getOther_company_name();
                //chkBackOut.setVisibility(View.VISIBLE);
                //     etBackoutReason.setVisibility(View.VISIBLE);
                //  spnBackoutReason.setVisibility(View.VISIBLE);


                etEnterName.setText(mOtherName);
                mDriverImgPath = maDriverInfo.getDriver_image_path();
                mDriverImgName = maDriverInfo.getDriver_image_name();
                mDriverImgClientDateTime = maDriverInfo.getDriver_image_datettime();
                baDriver = maDriverInfo.getDriver_image();
                if (baDriver != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(baDriver, 0, baDriver.length);
                    imgDriver.setVisibility(View.VISIBLE);
                    findViewById(R.id.imgCancelRedDriver).setVisibility(View.VISIBLE);
                    imgDriver.setImageBitmap(bitmap);
                } else {

                    if (!Connectivity.checkConnected(ActDriverProfileRegistration.this)) {
                        if (!connectionError(ActDriverProfileRegistration.this, "Driver Profile App requires Internet Connection to download Driver Photo." +
                                "\n\n" +
                                "Turn mobile data on?")) {
                            Log.d(TAG, "Internet Connection");
                        }
                    }
                    String url = Constants_dp.GET_DRIVER_IMAGE_URL + mDriverImgPath + mDriverImgName;
                    ImageRequest request = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {

                                    imgDriver.setImageBitmap(bitmap);
                                    imgDriver.setVisibility(View.VISIBLE);
                                    findViewById(R.id.imgCancelRedDriver).setVisibility(View.VISIBLE);

                                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bao);
                                    baDriver = bao.toByteArray();
                                    mApp.mDBHelper.updateDriverProfile(mPhoneNo, "driver_image", baDriver);
                                    if (bao != null) {
                                        try {
                                            bao.flush();
                                            bao.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                }
                            });
                    SSApp.getInstance().addToRequestQueue(request);
                }
                mPanImgPath = maDriverInfo.getPancard_image_path();
                mPanImgName = maDriverInfo.getPancard_image_name();
                mPanImgClientDateTime = maDriverInfo.getPancard_image_datettime();
                baPan = maDriverInfo.getPancard_image();
                if (baPan != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(baPan, 0, baPan.length);
                    imgPan.setVisibility(View.VISIBLE);
                    findViewById(R.id.imgCancelRedPan).setVisibility(View.VISIBLE);
                    imgPan.setImageBitmap(bitmap);
                } else {
                    String url = Constants_dp.GET_DRIVER_IMAGE_URL + mPanImgPath + mPanImgName;
                    ImageRequest request = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {

                                    imgPan.setImageBitmap(bitmap);
                                    imgPan.setVisibility(View.VISIBLE);
                                    findViewById(R.id.imgCancelRedPan).setVisibility(View.VISIBLE);

                                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bao);
                                    baPan = bao.toByteArray();
                                    mApp.mDBHelper.updateDriverProfile(mPhoneNo, "pancard_image", baPan);
                                    if (bao != null) {
                                        try {
                                            bao.flush();
                                            bao.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                }
                            });
                    SSApp.getInstance().addToRequestQueue(request);
                }

                mAdharImgPath = maDriverInfo.getAadhar_image_path();
                mAdharImgName = maDriverInfo.getAadhar_image_name();
                mAdharImgClientDateTime = maDriverInfo.getAadhar_image_datetime();
                baAadhar = maDriverInfo.getAadhar_image();
                if (baAadhar != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(baAadhar, 0, baAadhar.length);
                    imgAadhar.setVisibility(View.VISIBLE);
                    findViewById(R.id.imgCancelRedAadhar).setVisibility(View.VISIBLE);
                    imgAadhar.setImageBitmap(bitmap);
                } else {
                    String url = Constants_dp.GET_DRIVER_IMAGE_URL + mAdharImgPath + mAdharImgName;
                    ImageRequest request = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {

                                    imgAadhar.setImageBitmap(bitmap);
                                    imgAadhar.setVisibility(View.VISIBLE);
                                    findViewById(R.id.imgCancelRedAadhar).setVisibility(View.VISIBLE);

                                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bao);
                                    baAadhar = bao.toByteArray();
                                    mApp.mDBHelper.updateDriverProfile(mPhoneNo, "aadhar_image", baAadhar);
                                    if (bao != null) {
                                        try {
                                            bao.flush();
                                            bao.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                }
                            });
                    SSApp.getInstance().addToRequestQueue(request);
                }

                mLicenseImgPath = maDriverInfo.getLicense_image_path();
                mLicenseImgName = maDriverInfo.getLicense_image_name();
                mLicenseImgClientDateTime = maDriverInfo.getLicense_image_datetime();
                baLicence = maDriverInfo.getLicense_image();
                if (baLicence != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(baLicence, 0, baLicence.length);
                    imgLicence.setVisibility(View.VISIBLE);
                    findViewById(R.id.imgCancelRedLicence).setVisibility(View.VISIBLE);
                    imgLicence.setImageBitmap(bitmap);
                } else {
                    String url = Constants_dp.GET_DRIVER_IMAGE_URL + mLicenseImgPath + mLicenseImgName;
                    ImageRequest request = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {

                                    imgLicence.setImageBitmap(bitmap);
                                    imgLicence.setVisibility(View.VISIBLE);
                                    findViewById(R.id.imgCancelRedLicence).setVisibility(View.VISIBLE);

                                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bao);
                                    baLicence = bao.toByteArray();
                                    mApp.mDBHelper.updateDriverProfile(mPhoneNo, "license_image", baLicence);
                                    if (bao != null) {
                                        try {
                                            bao.flush();
                                            bao.close();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }

                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {

                                }
                            });
                    SSApp.getInstance().addToRequestQueue(request);
                }
                mOtherLookup = maDriverInfo.getOther_company_code();
                if (!TextUtils.isEmpty(mOtherLookup)) {
                    getOtherCompanyInfo(mOtherLookup);
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "displayData", e, ActDriverProfileRegistration.this);
            e.printStackTrace();
        }
    }

    private void getOtherCompanyInfo(String mOtherLookup) {

        Type type = new TypeToken<ArrayList<CMember>>() {
        }.getType();
        ArrayList<CMember> maMember = new Gson().fromJson(mOtherLookup, type);
        for (CMember cMember : maMember) {
            if (cMember.getOther_company_code().equalsIgnoreCase("OLA")) {
                chkOLA.setChecked(true);
                etOLAId.setVisibility(View.VISIBLE);
                etOLAId.setText(cMember.getDriver_other_company_id());
            }
            if (cMember.getOther_company_code().equalsIgnoreCase("UBR")) {
                chkUBR.setChecked(true);
                etUBRId.setVisibility(View.VISIBLE);
                etUBRId.setText(cMember.getDriver_other_company_id());
            }
            if (cMember.getOther_company_code().equalsIgnoreCase("MRU")) {
                chkMRU.setChecked(true);
                etMRUId.setVisibility(View.VISIBLE);
                etMRUId.setText(cMember.getDriver_other_company_id());
            }
            if (cMember.getOther_company_code().equalsIgnoreCase("BMC")) {
                chkBMC.setChecked(true);
                etBMCId.setVisibility(View.VISIBLE);
                etBMCId.setText(cMember.getDriver_other_company_id());
            }
            if (cMember.getOther_company_code().equalsIgnoreCase("ECA")) {
                chkECA.setChecked(true);
                etECAId.setVisibility(View.VISIBLE);
                etECAId.setText(cMember.getDriver_other_company_id());
            }
            if (cMember.getOther_company_code().equalsIgnoreCase("TAB")) {
                chkTAB.setChecked(true);
                etTABId.setVisibility(View.VISIBLE);
                etTABId.setText(cMember.getDriver_other_company_id());
            }
            if (cMember.getOther_company_code().equalsIgnoreCase("TFS")) {
                chkTFS.setChecked(true);
                etTFSId.setVisibility(View.VISIBLE);
                etTFSId.setText(cMember.getDriver_other_company_id());
            }
            if (cMember.getOther_company_code().equalsIgnoreCase("VIR")) {
                chkVIR.setChecked(true);
                etVIRId.setVisibility(View.VISIBLE);
                etVIRId.setText(cMember.getDriver_other_company_id());
            }
            if (cMember.getOther_company_code().equalsIgnoreCase("PRI")) {
                chkPRI.setChecked(true);
                etPRIId.setVisibility(View.VISIBLE);
                etPRIId.setText(cMember.getDriver_other_company_id());
            }
            if (cMember.getOther_company_code().equalsIgnoreCase("ARY")) {
                chkARY.setChecked(true);
                etARYId.setVisibility(View.VISIBLE);
                etARYId.setText(cMember.getDriver_other_company_id());
            }
            if (cMember.getOther_company_code().equalsIgnoreCase("BNY")) {
                chkBNY.setChecked(true);
                etBNYId.setVisibility(View.VISIBLE);
                etBNYId.setText(cMember.getDriver_other_company_id());
            }
            if (cMember.getOther_company_code().equalsIgnoreCase("OTH")) {
                chkOTH.setChecked(true);
                etOTHId.setVisibility(View.VISIBLE);
                etOTHId.setText(cMember.getDriver_other_company_id());
                etEnterName.setVisibility(View.VISIBLE);
            }
        }


    }


    private void personalInfoValidation() {
        mClientDateTime = tvTime.getText().toString().trim();
        mFirstName = etNameFirst.getText().toString().trim();
        mLastName = etNameLast.getText().toString().trim();
        mPhoneNo = etPhoneNo.getText().toString().trim();
        mDriverEmailID = etDriverEmailid.getText().toString().trim();
        int selectedGender = rgGender.getCheckedRadioButtonId();
        if (selectedGender != -1) {
            rbGender = (RadioButton) findViewById(selectedGender);
            mGender = rbGender.getText().toString().trim();
        }
        if (TextUtils.isEmpty(mFirstName)) {
            Toast.makeText(this, "Need " + getString(R.string.name_first), Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(mLastName)) {
            Toast.makeText(this, "Need " + getString(R.string.name_last), Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(mDob)) {
            Toast.makeText(this, "Need " + getString(R.string.age), Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(mPhoneNo)) {
            Toast.makeText(this, "Need " + getString(R.string.phone_no), Toast.LENGTH_LONG).show();
            return;
        }
        if (!isValid(mPhoneNo, 10)) {
            Toast.makeText(this, getString(R.string.phone_no) + " is Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        if (!TextUtils.isEmpty(mDriverEmailID)) {
            if (!isValidMail(mDriverEmailID)) {
                Toast.makeText(this, getString(R.string.driver_email_id) + " is Invalid", Toast.LENGTH_LONG).show();
                return;
            }
        }
        txtPersonal.setBackgroundResource(R.drawable.thin_frame);
        txtDriving.setBackgroundResource(R.drawable.frame_selected);
        txtRequirement.setBackgroundResource(R.drawable.thin_frame);
        txtPhoto.setBackgroundResource(R.drawable.thin_frame);
        findViewById(R.id.svPersonl).setVisibility(View.GONE);
        findViewById(R.id.svProfile).setVisibility(View.VISIBLE);
        findViewById(R.id.svRequirement).setVisibility(View.GONE);
        findViewById(R.id.svPhoto).setVisibility(View.GONE);

    }

    private void driverProfileValidation() {
        ArrayList<CMember> maCMembers = new ArrayList<>();
        if (maCMembers.size() > 0) {
            maCMembers.clear();
        }
        mPhoneNo = etPhoneNo.getText().toString().trim();
        int selectedPermit = rgPermit.getCheckedRadioButtonId();
        mPermitNo = etPermitNo.getText().toString().trim();
        if (selectedPermit == -1) {
            Toast.makeText(this, "Please select " + getString(R.string.permit), Toast.LENGTH_LONG).show();
            return;
        }
        rbPermit = (RadioButton) findViewById(selectedPermit);
        mPermitFlag = rbValues(rbPermit.getText().toString().trim()) ? "Y" : "N";
        if (rbValues(rbPermit.getText().toString().trim())) {
            if (TextUtils.isEmpty(mPermitNo)) {
                Toast.makeText(this, "Please enter " + getString(R.string.permit_no), Toast.LENGTH_LONG).show();
                return;
            }
        }

        int selectedBadge = rgBadge.getCheckedRadioButtonId();
        mBadgeNo = etBadgeNo.getText().toString().trim();
        mStayInMumbai = etStay.getText().toString().trim();
        if (selectedBadge == -1) {
            Toast.makeText(this, "Please select " + getString(R.string.badge), Toast.LENGTH_LONG).show();
            return;
        }
        rbBadge = (RadioButton) findViewById(selectedBadge);
        mBadgeFlag = rbValues(rbBadge.getText().toString().trim()) ? "Y" : "N";
        if (rbValues(rbBadge.getText().toString().trim())) {
            if (TextUtils.isEmpty(mBadgeNo)) {
                Toast.makeText(this, "Please enter " + getString(R.string.badge_no), Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            if (TextUtils.isEmpty(mStayInMumbai)) {
                Toast.makeText(this, "Please enter year", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (!TextUtils.isEmpty(mStayInMumbai)) {
            if (!isValid(mStayInMumbai, 4)) {
                Toast.makeText(this, "Invalid year", Toast.LENGTH_LONG).show();
                return;
            }
        }
        mOLAId = etOLAId.getText().toString().trim();
        mUBRId = etUBRId.getText().toString().trim();
        mMRUId = etMRUId.getText().toString().trim();
        mBMCId = etBMCId.getText().toString().trim();
        mECAId = etECAId.getText().toString().trim();
        mTABId = etTABId.getText().toString().trim();
        mTFSId = etTFSId.getText().toString().trim();
        mVIRId = etVIRId.getText().toString().trim();
        mPRIId = etPRIId.getText().toString().trim();
        mARYId = etARYId.getText().toString().trim();
        mBNYId = etBNYId.getText().toString().trim();
        mOTHId = etOTHId.getText().toString().trim();
        mOtherName = etEnterName.getText().toString().trim();
        if (chkOTH.isChecked()) {
            if (TextUtils.isEmpty(mOtherName)) {
                Toast.makeText(this, "Please enter other name", Toast.LENGTH_LONG).show();
                return;
            }
        }
        if (chkOLA.isChecked()) {
            maCMembers.add(new CMember("", "OLA", mOLAId));
        }
        if (chkUBR.isChecked()) {
            maCMembers.add(new CMember("", "UBR", mUBRId));
        }
        if (chkMRU.isChecked()) {
            maCMembers.add(new CMember("", "MRU", mMRUId));
        }
        if (chkBMC.isChecked()) {
            maCMembers.add(new CMember("", "BMC", mBMCId));
        }
        if (chkECA.isChecked()) {
            maCMembers.add(new CMember("", "ECA", mECAId));
        }
        if (chkTAB.isChecked()) {
            maCMembers.add(new CMember("", "TAB", mTABId));
        }
        if (chkTFS.isChecked()) {
            maCMembers.add(new CMember("", "TFS", mTFSId));
        }
        if (chkVIR.isChecked()) {
            maCMembers.add(new CMember("", "VIR", mVIRId));
        }
        if (chkPRI.isChecked()) {
            maCMembers.add(new CMember("", "PRI", mPRIId));
        }
        if (chkARY.isChecked()) {
            maCMembers.add(new CMember("", "ARY", mARYId));
        }
        if (chkBNY.isChecked()) {
            maCMembers.add(new CMember("", "BNY", mBNYId));
        }
        if (chkOTH.isChecked()) {
            maCMembers.add(new CMember("", "OTH", mOTHId));
        }
        if (maCMembers.size() > 0) {
            mOtherLookup = new Gson().toJson(maCMembers);
        }
        txtPersonal.setBackgroundResource(R.drawable.thin_frame);
        txtDriving.setBackgroundResource(R.drawable.thin_frame);
        txtRequirement.setBackgroundResource(R.drawable.frame_selected);
        txtPhoto.setBackgroundResource(R.drawable.thin_frame);
        findViewById(R.id.svPersonl).setVisibility(View.GONE);
        findViewById(R.id.svProfile).setVisibility(View.GONE);
        findViewById(R.id.svRequirement).setVisibility(View.VISIBLE);
        findViewById(R.id.svPhoto).setVisibility(View.GONE);


    }

    private void requirementValidation() {
        mClientDateTime = tvTime.getText().toString().trim();
        mFirstName = etNameFirst.getText().toString().trim();
        mLastName = etNameLast.getText().toString().trim();
        mPhoneNo = etPhoneNo.getText().toString().trim();
        mDriverEmailID = etDriverEmailid.getText().toString().trim();
        int selectedGender = rgGender.getCheckedRadioButtonId();
        if (selectedGender != -1) {
            rbGender = (RadioButton) findViewById(selectedGender);
            mGender = rbGender.getText().toString().trim();
        }
        if (TextUtils.isEmpty(mFirstName)) {
            Toast.makeText(this, "Need " + getString(R.string.name_first), Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(mLastName)) {
            Toast.makeText(this, "Need " + getString(R.string.name_last), Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(mDob)) {
            Toast.makeText(this, "Need " + getString(R.string.age), Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(mPhoneNo)) {
            Toast.makeText(this, "Need " + getString(R.string.phone_no), Toast.LENGTH_LONG).show();
            return;
        }
        if (!isValid(mPhoneNo, 10)) {
            Toast.makeText(this, getString(R.string.phone_no) + " is Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        if (!TextUtils.isEmpty(mDriverEmailID)) {
            if (!isValidMail(mDriverEmailID)) {
                Toast.makeText(this, getString(R.string.driver_email_id) + " is Invalid", Toast.LENGTH_LONG).show();
                return;
            }
        }

        txtPersonal.setBackgroundResource(R.drawable.thin_frame);
        txtDriving.setBackgroundResource(R.drawable.thin_frame);
        txtRequirement.setBackgroundResource(R.drawable.thin_frame);
        txtPhoto.setBackgroundResource(R.drawable.frame_selected);
        findViewById(R.id.svPersonl).setVisibility(View.GONE);
        findViewById(R.id.svProfile).setVisibility(View.GONE);
        findViewById(R.id.svRequirement).setVisibility(View.GONE);
        findViewById(R.id.svPhoto).setVisibility(View.VISIBLE);


    }

    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValid(String value, int digits) {
        int len = value.length();
        return len == digits;
    }

    private void setUpUi() {
        rlAdd = (RelativeLayout) findViewById(R.id.rlAdd);
        chkAdd = (CheckBox) findViewById(R.id.chkAdd);
        tvAdd = (TextView) findViewById(R.id.tvAdd);
        tvTime = (TextView) findViewById(R.id.tvTime);
        etNameFirst = (EditText) findViewById(R.id.etNameFirst);
        etNameLast = (EditText) findViewById(R.id.etNameLast);
        tvAge = (EditText) findViewById(R.id.tvAge);
        tvAge.setFocusable(false);
        tvAge.setClickable(true);
        txtByWhome = (TextView) findViewById(R.id.txtByWhome);
        etReceiptNo = (EditText) findViewById(R.id.etReceiptNo);
        etReceiptDae = (EditText) findViewById(R.id.etReceiptDae);
        etReceiptDae.setFocusable(false);
        etReceiptDae.setClickable(true);
        etTempAddrOne = (EditText) findViewById(R.id.etTempAddrOne);
        etTempAddrTwo = (EditText) findViewById(R.id.etTempAddrTwo);
        etTempCity = (EditText) findViewById(R.id.etTempCity);
        etTempState = (EditText) findViewById(R.id.etTempState);
        etTempPincode = (EditText) findViewById(R.id.etTempPincode);
        etTempLandmark = (EditText) findViewById(R.id.etTempLandmark);
        etPan = (EditText) findViewById(R.id.etPan);
        etPerAddrOne = (EditText) findViewById(R.id.etPerAddrOne);
        etPerAddrTwo = (EditText) findViewById(R.id.etPerAddrTwo);
        etPerCity = (EditText) findViewById(R.id.etPerCity);
        etPerState = (EditText) findViewById(R.id.etPerState);
        etPerPincode = (EditText) findViewById(R.id.etPerPincode);
        etPerLandmark = (EditText) findViewById(R.id.etPerLandmark);
        etDriverEmailid = (EditText) findViewById(R.id.etDriverEmailid);
        etAadharNo = (EditText) findViewById(R.id.etAadharNo);
        etPermitNo = (EditText) findViewById(R.id.etPermitNo);
        etBadgeNo = (EditText) findViewById(R.id.etBadgeNo);
        etStay = (EditText) findViewById(R.id.etStay);
        rgGender = (RadioGroup) findViewById(R.id.rgGender);
       /* rgPaymentSatus = (RadioGroup) findViewById(R.id.rgPaymentSatus);*/
        rgPermit = (RadioGroup) findViewById(R.id.rgPermit);
        rgBadge = (RadioGroup) findViewById(R.id.rgBadge);
        rgCab = (RadioGroup) findViewById(R.id.rgCab);
        txtPersonal = (TextView) findViewById(R.id.txtPersonal);
        txtDriving = (TextView) findViewById(R.id.txtDriving);
        txtRequirement = (TextView) findViewById(R.id.txtRequirement);
        txtPhoto = (TextView) findViewById(R.id.txtPhoto);
        etPhoneNo = (EditText) findViewById(R.id.etPhoneNo);
        etLicenceNo = (EditText) findViewById(R.id.etLicenceNo);
        tvTime.setText(ft.format(dNow));
        etInternalComment = (EditText) findViewById(R.id.etInternalComment);
        etRemark = (EditText) findViewById(R.id.etRemark);
        imgDriver = (ImageView) findViewById(R.id.imgDriver);
        imgAadhar = (ImageView) findViewById(R.id.imgAadhar);
        imgLicence = (ImageView) findViewById(R.id.imgLicence);
        imgPan = (ImageView) findViewById(R.id.imgPan);
        imgCameraDriver = (ImageView) findViewById(R.id.imgCameraDriver);
        imgCameraPan = (ImageView) findViewById(R.id.imgCameraPan);
        imgCameraAadhar = (ImageView) findViewById(R.id.imgCameraAadhar);
        imgCameraLicence = (ImageView) findViewById(R.id.imgCameraLicence);
        imgCancelRedDriver = (ImageView) findViewById(R.id.imgCancelRedDriver);
        imgCancelRedTwo = (ImageView) findViewById(R.id.imgCancelRedLicence);
        etVehicleNo = (EditText) findViewById(R.id.etVehicleNo);
        takephoto = (TextView) findViewById(R.id.takephoto);
        chkOLA = (CheckBox) findViewById(R.id.chkOLA);
        chkUBR = (CheckBox) findViewById(R.id.chkUBR);
        chkMRU = (CheckBox) findViewById(R.id.chkMRU);
        chkBMC = (CheckBox) findViewById(R.id.chkBMC);
        chkECA = (CheckBox) findViewById(R.id.chkECA);
        chkTAB = (CheckBox) findViewById(R.id.chkTAB);
        chkTFS = (CheckBox) findViewById(R.id.chkTFS);
        chkVIR = (CheckBox) findViewById(R.id.chkVIR);
        chkPRI = (CheckBox) findViewById(R.id.chkPRI);
        chkARY = (CheckBox) findViewById(R.id.chkARY);
        chkBNY = (CheckBox) findViewById(R.id.chkBNY);
        chkOTH = (CheckBox) findViewById(R.id.chkOTH);
        etEnterName = (EditText) findViewById(R.id.etEnterName);
        etOLAId = (EditText) findViewById(R.id.etOLAId);
        etUBRId = (EditText) findViewById(R.id.etUBRId);
        etMRUId = (EditText) findViewById(R.id.etMRUId);
        etBMCId = (EditText) findViewById(R.id.etBMCId);
        etECAId = (EditText) findViewById(R.id.etECAId);
        etTABId = (EditText) findViewById(R.id.etTABId);
        etTFSId = (EditText) findViewById(R.id.etTFSId);
        etVIRId = (EditText) findViewById(R.id.etVIRId);
        etPRIId = (EditText) findViewById(R.id.etPRIId);
        etARYId = (EditText) findViewById(R.id.etARYId);
        etBNYId = (EditText) findViewById(R.id.etBNYId);
        etOTHId = (EditText) findViewById(R.id.etOTHId);
        rgHowDriverCome = (RadioGroup) findViewById(R.id.rgHowDriverCome);
        rgReferByWhome = (RadioGroup) findViewById(R.id.rgReferByWhome);
        etNameOfPerson = (EditText) findViewById(R.id.etNameOfPerson);

        rgPayment = (RadioGroup) findViewById(R.id.rgPayment);
        etReceiptNoTwo = (EditText) findViewById(R.id.etReceiptNoTwo);
        etReceiptNoThree = (EditText) findViewById(R.id.etReceiptNoThree);
        //chkBackOut = (CheckBox) findViewById(R.id.chkBackOut);
        // etBackoutReason = (EditText) findViewById(R.id.etBackoutReason);
        etExpDate = (EditText) findViewById(R.id.etExpDate);
        etExpDate.setFocusable(false);
        etExpDate.setClickable(true);
        etReceiptDateTwo = (EditText) findViewById(R.id.etReceiptDateTwo);
        etReceiptDateTwo.setFocusable(false);
        etReceiptDateTwo.setClickable(true);
        etReceiptDateThree = (EditText) findViewById(R.id.etReceiptDateThree);
        etReceiptDateThree.setFocusable(false);
        etReceiptDateThree.setClickable(true);
        //  spnBackoutReason = (MaterialBetterSpinner) findViewById(R.id.spnBackoutReason);

    }

    private void AlertBoxDone() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Your id is " + mUniqueId +
                getString(R.string.successfull_entered));
        alertDialogBuilder.setPositiveButton(R.string.about_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        Intent i = new Intent(getApplicationContext(), ActDriverProfileRegistration.class);
                        finish();
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void getAddress() {

        mResultReceiver = new AddressResultReceiver(new Handler());
        mCurrentLocation = CGlobals_dp.getInstance(ActDriverProfileRegistration.this).
                getMyLocation(ActDriverProfileRegistration.this);
        if (mCurrentLocation != null) {
            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();
            tvAdd.setText(latitude + "," + longitude);
            tvAdd.setHint("Getting Address");
            geoHelper.getAddress(ActDriverProfileRegistration.this,
                    mCurrentLocation, onGeoHelperResult);
            String loc = new Gson().toJson(mCurrentLocation);
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(ActDriverProfileRegistration.this).
                    putString(Constants_dp.PREF_MY_LOCATION, loc).apply();
        }
    }

    private GeoHelper.GeoHelperResult onGeoHelperResult = new GeoHelper.GeoHelperResult() {
        @Override
        public void gotAddress(CAddress addr) {
            if (addr != null) {
                if (addr.hasLatitude() || addr.hasLongitude()) {
                    setText(addr);
                }
            } else {
                Toast.makeText(ActDriverProfileRegistration.this, "Connection is failed." +
                        " Please try again later.", Toast.LENGTH_LONG).show();
            }
        }
    };

    private void setText(CAddress oAddr) {
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
            tvAdd.setText(formatted_address);
        }
        CGlobals_lib_ss.getInstance().addRecentAddress(oAddr);

    }

    private boolean rbValues(String rbText) {

        return rbText.equalsIgnoreCase(getString(R.string.yes)) || rbText.equalsIgnoreCase("Y");
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        startIntentService(mCurrentLocation);
        Toast.makeText(ActDriverProfileRegistration.this, "CHANGING LOCATION ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "connected");
    }


    @Override
    public void gotDateResult(String sDate) {
        try {
            Date date;
            if (TextUtils.isEmpty(sDate))
                return;
            if (dobFlag) {
                mDob = sDate;
                ft = new SimpleDateFormat("yyyy-MM-dd");
                date = ft.parse(mDob);
                ft = new SimpleDateFormat("dd MMM, yyyy");
                dispDate = ft.format(date);
                tvAge.setText(dispDate);
            }
            if (receiptFlag) {
                mReceiptDate = sDate;
                ft = new SimpleDateFormat("yyyy-MM-dd");
                date = ft.parse(mReceiptDate);
                ft = new SimpleDateFormat("dd MMM, yyyy");
                mDisRecDate = ft.format(date);
                etReceiptDae.setText(mDisRecDate);
            }
            if (expFlag) {
                mExpDate = sDate;
                ft = new SimpleDateFormat("yyyy-MM-dd");
                date = ft.parse(mExpDate);
                ft = new SimpleDateFormat("dd MMM, yyyy");
                mDispExpDate = ft.format(date);
                etExpDate.setText(mDispExpDate);
            }
            if (receiptTwoFlag) {
                mReceiptDateTwo = sDate;
                ft = new SimpleDateFormat("yyyy-MM-dd");
                date = ft.parse(mReceiptDateTwo);
                ft = new SimpleDateFormat("dd MMM, yyyy");
                mDisRecDateTwo = ft.format(date);
                etReceiptDateTwo.setText(mDisRecDateTwo);
            }
            if (receiptThreeFlag) {
                mReceiptDateThree = sDate;
                ft = new SimpleDateFormat("yyyy-MM-dd");
                date = ft.parse(mReceiptDateThree);
                ft = new SimpleDateFormat("dd MMM, yyyy");
                mDisRecDateThree = ft.format(date);
                etReceiptDateThree.setText(mDisRecDateThree);
            }
            dateFrag = false;
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {

            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Display the address string or an error message sent from the
            // intent service.
            mAddressOutput = resultData
                    .getString(Constants_lib_ss.RESULT_DATA_KEY);
            // Show a toast message if an address was found.
            if (resultCode == Constants_lib_ss.SUCCESS_RESULT) {
                Log.d(TAG, "ADDRESS IS ----> " + mAddressOutput);
                tvAdd.setText(mAddressOutput);

            }
        }
    }


    protected void startIntentService(Location location) {

        try {
            Intent intent = new Intent(ActDriverProfileRegistration.this,
                    FetchAddressIntentService.class);
            intent.putExtra(Constants_lib_ss.RECEIVER,
                    mResultReceiver);
            intent.putExtra(Constants_lib_ss.LOCATION_DATA_EXTRA,
                    location);
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // startIntentService

    private void openCamera() {
        try {
            Intent intent = new Intent(ActDriverProfileRegistration.this, CameraActivity.class);
            intent.putExtra("PHOTO_DIRECTORY", getString(R.string.app_label));
            startActivityForResult(intent, REQUEST_PHOTO);

        } catch (Exception e) {
            Log.d(TAG, "EXC " + e.toString());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PHOTO) {

            if (resultCode == Activity.RESULT_OK && data != null) {
                Bundle extra = data.getBundleExtra("data");
                imageName = extra.getString("imageName");
                mCurrentPhotoPath = extra.getString("imageFilePath");
                ba = extra.getByteArray("imageByte");
//                strImag = extra.getString("imageString");
                setPic();

            } else {
                Toast.makeText(this, "No picture returned by the camera. Is your memory full?",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
        if (requestCode == CGlobals_dp.REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:

                    getAddress();
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");

                    break;
                default:
                    break;
            }
        }

    }

    private void setPic() {
        try {

            bitmap = null;
            bitmap = BitmapFactory.decodeByteArray(ba, 0, ba.length);
            if (driverPic) {
                baDriver = ba;
                strDriverImg = Base64.encodeToString(baDriver, Base64.DEFAULT);
                imgDriver.setImageBitmap(bitmap);
                mDriverImgName = imageName;
                mDriverImgPath = mCurrentPhotoPath;
                mDriverImgClientDateTime = creationDateTimeDriver;
                mImageClientDateTime = creationDateTimeDriver;
                imgDriver.setVisibility(View.VISIBLE);
                findViewById(R.id.imgCancelRedDriver).setVisibility(View.VISIBLE);
            }
            if (panPic) {
                baPan = ba;
                strPanImg = Base64.encodeToString(baPan, Base64.DEFAULT);
                imgPan.setImageBitmap(bitmap);
                mPanImgName = imageName;
                mPanImgPath = mCurrentPhotoPath;
                mPanImgClientDateTime = creationDateTimeDriver;
                mImageClientDateTime = creationDateTimeDriver;
                imgPan.setVisibility(View.VISIBLE);
                findViewById(R.id.imgCancelRedPan).setVisibility(View.VISIBLE);
            }
            if (aadharPic) {
                baAadhar = ba;
                strAadharImg = Base64.encodeToString(baAadhar, Base64.DEFAULT);
                imgAadhar.setImageBitmap(bitmap);
                mAdharImgName = imageName;
                mAdharImgPath = mCurrentPhotoPath;
                mAdharImgClientDateTime = creationDateTimeDriver;
                mImageClientDateTime = creationDateTimeDriver;
                imgAadhar.setVisibility(View.VISIBLE);
                findViewById(R.id.imgCancelRedAadhar).setVisibility(View.VISIBLE);

            }
            if (licencePic) {
                baLicence = ba;
                strLicenceImg = Base64.encodeToString(baLicence, Base64.DEFAULT);
                imgLicence.setImageBitmap(bitmap);
                mLicenseImgName = imageName;
                mLicenseImgPath = mCurrentPhotoPath;
                mLicenseImgClientDateTime = creationDateTimeDriver;
                mImageClientDateTime = creationDateTimeDriver;
                imgLicence.setVisibility(View.VISIBLE);
                findViewById(R.id.imgCancelRedLicence).setVisibility(View.VISIBLE);
            }
            bitmap = null;
            PrefUtils.setKioskModeActive(true, getApplicationContext());
        } catch (Exception e) {
            Log.d(TAG, "setPic: " + e.toString());
            e.printStackTrace();

        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong("iUniqueKey", iUniqueKey);
        savedInstanceState.putString("mClientDateTime", mClientDateTime);
        savedInstanceState.putString("mDob", mDob);
        savedInstanceState.putString("dispDate", dispDate);
        savedInstanceState.putString("formatted_address", formatted_address);
        savedInstanceState.putString("mFirstName", mFirstName);
        savedInstanceState.putString("mLastName", mLastName);
        savedInstanceState.putString("mTempLineOne", mTempLineOne);
        savedInstanceState.putString("mTempLineTwo", mTempLineTwo);
        savedInstanceState.putString("mTempCity", mTempCity);
        savedInstanceState.putString("mTempState", mTempState);
        savedInstanceState.putString("mTempPincode", mTempPincode);
        savedInstanceState.putString("mTempLandmark", mTempLandmark);
        savedInstanceState.putString("mPerLineOne", mPerLineOne);
        savedInstanceState.putString("mPerLineTwo", mPerLineTwo);
        savedInstanceState.putString("mPerCity", mPerCity);
        savedInstanceState.putString("mPerState", mPerState);
        savedInstanceState.putString("mPerPincode", mPerPincode);
        savedInstanceState.putString("mPerLandmark", mPerLandmark);
        savedInstanceState.putString("mDriverEmailID", mDriverEmailID);
        savedInstanceState.putString("mPhoneNo", mPhoneNo);
        savedInstanceState.putString("mGender", mGender);
        savedInstanceState.putString("mPermitFlag", mPermitFlag);
        savedInstanceState.putString("mPermitNo", mPermitNo);
        savedInstanceState.putString("mBadgeFlag", mBadgeFlag);
        savedInstanceState.putString("mBadgeNo", mBadgeNo);
        savedInstanceState.putString("mStayInMumbai", mStayInMumbai);
        savedInstanceState.putString("mOtherLookup", mOtherLookup);
        savedInstanceState.putString("mOtherName", mOtherName);
        savedInstanceState.putString("mCabNeedFlag", mCabNeedFlag);
        savedInstanceState.putString("mRemark", mRemark);
        savedInstanceState.putString("mInternalComment", mInternalComment);
        savedInstanceState.putString("mPanNo", mPanNo);
        savedInstanceState.putString("mAadharNo", mAadharNo);
        savedInstanceState.putString("mLicenceNo", mLicenceNo);
        savedInstanceState.putBoolean("driverPic", driverPic);
        savedInstanceState.putBoolean("licencePic", licencePic);
        savedInstanceState.putBoolean("panPic", panPic);
        savedInstanceState.putBoolean("aadharPic", aadharPic);
        savedInstanceState.putString("mDriverImgName", mDriverImgName);
        savedInstanceState.putString("mDriverImgPath", mDriverImgPath);
        savedInstanceState.putString("strDriverImg", strDriverImg);
        savedInstanceState.putString("mDriverImgClientDateTime", mDriverImgClientDateTime);
        savedInstanceState.putString("mAdharImgName", mAdharImgName);
        savedInstanceState.putString("mAdharImgPath", mAdharImgPath);
        savedInstanceState.putString("strAadharImg", strAadharImg);
        savedInstanceState.putString("mAdharImgClientDateTime", mAdharImgClientDateTime);
        savedInstanceState.putString("mPanImgName", mPanImgName);
        savedInstanceState.putString("mPanImgPath", mPanImgPath);
        savedInstanceState.putString("strPanImg", strPanImg);
        savedInstanceState.putString("mPanImgClientDateTime", mPanImgClientDateTime);
        savedInstanceState.putString("mLicenseImgName", mLicenseImgName);
        savedInstanceState.putString("mLicenseImgPath", mLicenseImgPath);
        savedInstanceState.putString("strLicenceImg", strLicenceImg);
        savedInstanceState.putString("mLicenseImgClientDateTime", mLicenseImgClientDateTime);
//        savedInstanceState.putString("mExpDate", mExpDate);
//        savedInstanceState.putString("mDispExpDate", mDispExpDate);
//
//        mExpDate = savedInstanceState.getString("mExpDate", mExpDate);
//        mDispExpDate = savedInstanceState.getString("mDispExpDate", mDispExpDate);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        System.gc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imgDriver.setImageBitmap(null);
        imgPan.setImageBitmap(null);
        imgLicence.setImageBitmap(null);
        imgAadhar.setImageBitmap(null);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        System.gc();

    }

    @Override
    protected void onStop() {
        super.onStop();
        System.gc();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuItem menuList, menuDone, menuEdit, menuUpload;

        menuDone = menu.add(Menu.NONE, 1000, Menu.NONE, R.string.done);
        menuList = menu.add(Menu.NONE, 2000, Menu.NONE, R.string.list);
        menuUpload = menu.add(Menu.NONE, 3000, Menu.NONE, R.string.upload);
        menuEdit = menu.add(Menu.NONE, 4000, Menu.NONE, R.string.edit);

        MenuItemCompat.setShowAsAction(menuUpload, MenuItem.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(menuList, MenuItem.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(menuDone, MenuItem.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(menuEdit, MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menuUpload.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Intent intent = new Intent(ActDriverProfileRegistration.this, ActUploadData.class);
                startActivity(intent);

                return false;
            }
        });
        menuList.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mFirstName = etNameFirst.getText().toString();
                mLastName = etNameLast.getText().toString();
                dispDate = tvAge.getText().toString();
                mPhoneNo = etPhoneNo.getText().toString();
                if (baDriver == null && TextUtils.isEmpty(mFirstName) &&
                        TextUtils.isEmpty(mLastName) && TextUtils.isEmpty(dispDate) &&
                        TextUtils.isEmpty(mPhoneNo)) {
                    Intent intent = new Intent(ActDriverProfileRegistration.this, DriverList.class);
                    startActivity(intent);
                    ActDriverProfileRegistration.this.finish();
                } else {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActDriverProfileRegistration.this);
                    alertDialogBuilder.setMessage("You will lose information entered. Are you sure?");
                    alertDialogBuilder.setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    try {
                                        Intent intent = new Intent(ActDriverProfileRegistration.this, DriverList.class);
                                        startActivity(intent);
                                        ActDriverProfileRegistration.this.finish();
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

                return false;
            }
        });

        menuEdit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                menuEdit.setVisible(false);
                menuDone.setVisible(true);
                setEnabled(true);
                return false;
            }
        });


        menuDone.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {


                mAdd = tvAdd.getText().toString().trim();
                mClientDateTime = tvTime.getText().toString().trim();
                mFirstName = etNameFirst.getText().toString().trim();
                mLastName = etNameLast.getText().toString().trim();
                mTempLineOne = etTempAddrOne.getText().toString().trim();
                mTempLineTwo = etTempAddrTwo.getText().toString().trim();
                mTempCity = etTempCity.getText().toString().trim();
                mTempState = etTempState.getText().toString().trim();
                mTempPincode = etTempPincode.getText().toString().trim();
                mTempLandmark = etTempLandmark.getText().toString().trim();
                mPerLineOne = etPerAddrOne.getText().toString().trim();
                mPerLineTwo = etPerAddrTwo.getText().toString().trim();
                mPerCity = etPerCity.getText().toString().trim();
                mPerState = etPerState.getText().toString().trim();
                mPerPincode = etPerPincode.getText().toString().trim();
                mPerLandmark = etPerLandmark.getText().toString().trim();
                mPhoneNo = etPhoneNo.getText().toString().trim();
                mDriverEmailID = etDriverEmailid.getText().toString().trim();
                mVehicleNo = etVehicleNo.getText().toString().trim();
                int selectedGender = rgGender.getCheckedRadioButtonId();
                if (selectedGender != -1) {
                    rbGender = (RadioButton) findViewById(selectedGender);
                    mGender = rbGender.getText().toString().trim();
                }
                ArrayList<CMember> maCMembers = new ArrayList<>();
                if (maCMembers.size() > 0) {
                    maCMembers.clear();
                }
                int selectedPermit = rgPermit.getCheckedRadioButtonId();
                mPermitNo = etPermitNo.getText().toString().trim();
                if (selectedPermit == -1) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Please select " + getString(R.string.permit), Toast.LENGTH_LONG).show();
                    return false;
                }
                rbPermit = (RadioButton) findViewById(selectedPermit);
                mPermitFlag = rbValues(rbPermit.getText().toString().trim()) ? "Y" : "N";
                if (rbValues(rbPermit.getText().toString().trim())) {
                    if (TextUtils.isEmpty(mPermitNo)) {
                        Toast.makeText(ActDriverProfileRegistration.this, "Please enter " + getString(R.string.permit_no), Toast.LENGTH_LONG).show();
                        return false;
                    }
                }


                int selectedBadge = rgBadge.getCheckedRadioButtonId();
                mBadgeNo = etBadgeNo.getText().toString().trim();
                mStayInMumbai = etStay.getText().toString().trim();
                if (selectedBadge == -1) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Please select " + getString(R.string.badge), Toast.LENGTH_LONG).show();
                    return false;
                }

                rbBadge = (RadioButton) findViewById(selectedBadge);
                mBadgeFlag = rbValues(rbBadge.getText().toString().trim()) ? "Y" : "N";
                if (rbValues(rbBadge.getText().toString().trim())) {
                    if (TextUtils.isEmpty(mBadgeNo)) {
                        Toast.makeText(ActDriverProfileRegistration.this, "Please enter " + getString(R.string.badge_no), Toast.LENGTH_LONG).show();
                        return false;
                    }
                } else {
                    if (TextUtils.isEmpty(mStayInMumbai)) {
                        Toast.makeText(ActDriverProfileRegistration.this, "Please enter year", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }

                if (!TextUtils.isEmpty(mStayInMumbai)) {
                    if (!isValid(mStayInMumbai, 4)) {
                        Toast.makeText(ActDriverProfileRegistration.this, "Invalid year", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
                mOLAId = etOLAId.getText().toString().trim();
                mUBRId = etUBRId.getText().toString().trim();
                mMRUId = etMRUId.getText().toString().trim();
                mBMCId = etBMCId.getText().toString().trim();
                mECAId = etECAId.getText().toString().trim();
                mTABId = etTABId.getText().toString().trim();
                mTFSId = etTFSId.getText().toString().trim();
                mVIRId = etVIRId.getText().toString().trim();
                mPRIId = etPRIId.getText().toString().trim();
                mARYId = etARYId.getText().toString().trim();
                mBNYId = etBNYId.getText().toString().trim();
                mOTHId = etOTHId.getText().toString().trim();
                mOtherName = etEnterName.getText().toString().trim();
                if (chkOTH.isChecked()) {
                    if (TextUtils.isEmpty(mOtherName)) {
                        Toast.makeText(ActDriverProfileRegistration.this, "Please enter other name", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
                if (chkOLA.isChecked()) {
                    maCMembers.add(new CMember(driverprofileid, "OLA", mOLAId));
                }
                if (chkUBR.isChecked()) {
                    maCMembers.add(new CMember(driverprofileid, "UBR", mUBRId));
                }
                if (chkMRU.isChecked()) {
                    maCMembers.add(new CMember(driverprofileid, "MRU", mMRUId));
                }
                if (chkBMC.isChecked()) {
                    maCMembers.add(new CMember(driverprofileid, "BMC", mBMCId));
                }
                if (chkECA.isChecked()) {
                    maCMembers.add(new CMember(driverprofileid, "ECA", mECAId));
                }
                if (chkTAB.isChecked()) {
                    maCMembers.add(new CMember(driverprofileid, "TAB", mTABId));
                }
                if (chkTFS.isChecked()) {
                    maCMembers.add(new CMember(driverprofileid, "TFS", mTFSId));
                }
                if (chkVIR.isChecked()) {
                    maCMembers.add(new CMember(driverprofileid, "VIR", mVIRId));
                }
                if (chkPRI.isChecked()) {
                    maCMembers.add(new CMember(driverprofileid, "PRI", mPRIId));
                }
                if (chkARY.isChecked()) {
                    maCMembers.add(new CMember(driverprofileid, "ARY", mARYId));
                }
                if (chkBNY.isChecked()) {
                    maCMembers.add(new CMember(driverprofileid, "BNY", mBNYId));
                }
                if (chkOTH.isChecked()) {
                    maCMembers.add(new CMember(driverprofileid, "OTH", mOTHId));
                }
                if (maCMembers.size() > 0) {
                    mOtherLookup = new Gson().toJson(maCMembers);
                }
                mPanNo = etPan.getText().toString().trim();
                mAadharNo = etAadharNo.getText().toString().trim();
                mLicenceNo = etLicenceNo.getText().toString().trim();

                int selectedCab = rgCab.getCheckedRadioButtonId();
                if (selectedCab != -1) {
                    rbCab = (RadioButton) findViewById(selectedCab);
                    mCabNeedFlag = rbValues(rbCab.getText().toString().trim()) ? "Y" : "N";
                }
                mRemark = etRemark.getText().toString().trim();
                mInternalComment = etInternalComment.getText().toString().trim();


                if (TextUtils.isEmpty(mFirstName)) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Need " + getString(R.string.name_first), Toast.LENGTH_LONG).show();
                    return false;
                }
                if (TextUtils.isEmpty(mLastName)) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Need " + getString(R.string.name_last), Toast.LENGTH_LONG).show();
                    return false;
                }
                if (TextUtils.isEmpty(mDob)) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Need " + getString(R.string.age), Toast.LENGTH_LONG).show();
                    return false;
                }
                if (TextUtils.isEmpty(mPerLineOne)) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Need Permenant" + getString(R.string.add_one), Toast.LENGTH_LONG).show();
                    return false;
                }
                if (TextUtils.isEmpty(mPerLineTwo)) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Need Permenant" + getString(R.string.add_two), Toast.LENGTH_LONG).show();
                    return false;
                }
                if (TextUtils.isEmpty(mPerCity)) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Need Permenant" + getString(R.string.city_town), Toast.LENGTH_LONG).show();
                    return false;
                }
                if (TextUtils.isEmpty(mPerState)) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Need Permenant" + getString(R.string.state), Toast.LENGTH_LONG).show();
                    return false;
                }
                if (TextUtils.isEmpty(mPerPincode)) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Need Permenant" + getString(R.string.pincode), Toast.LENGTH_LONG).show();
                    return false;
                }
                if (!TextUtils.isEmpty(mTempPincode)) {
                    if (!isValid(mTempPincode, 6)) {
                        Toast.makeText(ActDriverProfileRegistration.this, "Temporary " + getString(R.string.pincode) + " is wrong", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
                if (!isValid(mPerPincode, 6)) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Permenant " + getString(R.string.pincode) + " is wrong", Toast.LENGTH_LONG).show();
                    return false;
                }
                /*if (TextUtils.isEmpty(mPerLandmark)) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Need Permenant" + getString(R.string.landmark), Toast.LENGTH_LONG).show();
                    return false;
                }*/
                if (TextUtils.isEmpty(mGender)) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Need Driver " + getString(R.string.gender), Toast.LENGTH_LONG).show();
                    return false;
                }
                if (TextUtils.isEmpty(mPhoneNo)) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Need " + getString(R.string.phone_no), Toast.LENGTH_LONG).show();
                    return false;
                }
                if (!isValid(mPhoneNo, 10)) {
                    Toast.makeText(ActDriverProfileRegistration.this, getString(R.string.phone_no) + " is Invalid", Toast.LENGTH_LONG).show();
                    return false;
                }
                /// uncomment the following code ///
                if (!TextUtils.isEmpty(mDriverEmailID)) {
                    if (!isValidMail(mDriverEmailID)) {
                        Toast.makeText(ActDriverProfileRegistration.this, getString(R.string.driver_email_id) + " is Invalid", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
                if (TextUtils.isEmpty(mPermitFlag)) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Please select " + getString(R.string.permit), Toast.LENGTH_LONG).show();
                    return false;
                }
                if (rbValues(mPermitFlag))
                    if (TextUtils.isEmpty(mPermitNo)) {
                        Toast.makeText(ActDriverProfileRegistration.this, "Please enter " + getString(R.string.permit_no), Toast.LENGTH_LONG).show();
                        return false;
                    }
                if (TextUtils.isEmpty(mBadgeFlag)) {
                    Toast.makeText(ActDriverProfileRegistration.this, "Please select " + getString(R.string.badge), Toast.LENGTH_LONG).show();
                    return false;
                }

                if (!TextUtils.isEmpty(mStayInMumbai)) {
                    if (!isValid(mStayInMumbai, 4)) {
                        Toast.makeText(ActDriverProfileRegistration.this, "Invalid year", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
                mLicenceNo = etLicenceNo.getText().toString().trim();
                /// uncomment the following code ///
                if (TextUtils.isEmpty(mLicenceNo)) {
                    Toast.makeText(ActDriverProfileRegistration.this,
                            "Need " + getString(R.string.driver_licence_no), Toast.LENGTH_LONG).show();
                    return false;
                }

                if (TextUtils.isEmpty(mExpDate)) {
                    Toast.makeText(ActDriverProfileRegistration.this,
                            "Need " + getString(R.string.exp_date), Toast.LENGTH_LONG).show();
                    return false;
                }

                if (baDriver == null) {
                    Toast.makeText(ActDriverProfileRegistration.this,
                            "Need " + getString(R.string.driver_photo), Toast.LENGTH_LONG).show();
                    return false;
                }
                if (baLicence == null) {
                    Toast.makeText(ActDriverProfileRegistration.this,
                            "Need " + getString(R.string.licence_photo), Toast.LENGTH_LONG).show();
                    return false;
                }
                dNow = new Date();
                ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT,
                        Locale.getDefault());
                String modifydate = ft.format(dNow);

                if (TextUtils.isEmpty(mUniqueId)) {
                    try {
                        final Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        mUniqueId = WordUtils.capitalize(String.valueOf(mFirstName.charAt(0)))
                                + WordUtils.capitalize(String.valueOf(mLastName.charAt(0))) + "_"
                                + (("0" + (month + 1))) + "_" + year + "_" + mPhoneNo;
                    } catch (Exception e) {
                        e.printStackTrace();
                        SSLog_SS.e(TAG, "menuDone", e, ActDriverProfileRegistration.this);
                    }

                }

                int selectedReferralWalkIn = rgHowDriverCome.getCheckedRadioButtonId();
                if (selectedReferralWalkIn != -1) {
                    rbHowDriverCome = (RadioButton) findViewById(selectedReferralWalkIn);
                    mReferralWalkin = rbHowDriverCome.getText().toString();
                } else {
                    Toast.makeText(ActDriverProfileRegistration.this,
                            "how did the driver come to cab-e?", Toast.LENGTH_LONG).show();
                    return false;
                }
                int selectedReferByWhome = rgReferByWhome.getCheckedRadioButtonId();

                if (mReferralWalkin.equals(getString(R.string.referral))) {
                    if (selectedReferByWhome == -1) {
                        Toast.makeText(ActDriverProfileRegistration.this,
                                "please select driver reference", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
                if (selectedReferByWhome != -1) {
                    rbReferByWhome = (RadioButton) findViewById(selectedReferByWhome);
                    mReferByWhome = rbReferByWhome.getText().toString();
                    mNameOfPerson = etNameOfPerson.getText().toString();
                    if (TextUtils.isEmpty(mNameOfPerson)) {
                        Toast.makeText(ActDriverProfileRegistration.this,
                                "please enter name of the person", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }

                mReceiptNo2 = etReceiptNoTwo.getText().toString();
                mReceiptNo3 = etReceiptNoThree.getText().toString();

                int selectedPayAmnt = rgPayment.getCheckedRadioButtonId();
                if (selectedPayAmnt != -1) {
                    rbPayment = (RadioButton) findViewById(selectedPayAmnt);
                    mPaymentAmount = rbPayment.getText().toString();
                } else {
                    Toast.makeText(ActDriverProfileRegistration.this,
                            "Need " + getString(R.string.payment_amount), Toast.LENGTH_LONG).show();
                    return false;
                }

                mReceiptNo = etReceiptNo.getText().toString();
                if (!TextUtils.isEmpty(mReceiptNo)) {
                    if (TextUtils.isEmpty(mReceiptDate)) {
                        Toast.makeText(ActDriverProfileRegistration.this,
                                "Please enter the first receipt date", Toast.LENGTH_LONG).show();
                        return false;
                    }
                } else {
                    Toast.makeText(ActDriverProfileRegistration.this,
                            "Please enter the first receipt no", Toast.LENGTH_LONG).show();
                    return false;
                }

                if (!TextUtils.isEmpty(mReceiptNo2)) {
                    if (TextUtils.isEmpty(mReceiptDateTwo)) {
                        Toast.makeText(ActDriverProfileRegistration.this,
                                "Please enter the second receipt date", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }

                if (!TextUtils.isEmpty(mReceiptNo3)) {
                    if (TextUtils.isEmpty(mReceiptDateThree)) {
                        Toast.makeText(ActDriverProfileRegistration.this,
                                "Please enter the third receipt date", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }

                HashMap<String, String> hmp = new HashMap<>();
                if (!TextUtils.isEmpty(mReceiptDateTwo))
                    hmp.put("receipt_date_2", mReceiptDateTwo);
                if (!TextUtils.isEmpty(mReceiptDateThree))
                    hmp.put("receipt_date_3", mReceiptDateThree);
                if (!TextUtils.isEmpty(mExpDate))
                    hmp.put("tr_license_exp_date", mExpDate);
               /* if (!TextUtils.isEmpty(mBackoutReason))
                    hmp.put("driver_backout_reason", mBackoutReason);*/
                if (!TextUtils.isEmpty(mReceiptNo2))
                    hmp.put("receipt_no_2", mReceiptNo2);
                if (!TextUtils.isEmpty(mReceiptNo3))
                    hmp.put("receipt_no_3", mReceiptNo3);
                if (!TextUtils.isEmpty(mReferralWalkin))
                    hmp.put("referral_walk_in", mReferralWalkin);


                if (!TextUtils.isEmpty(mReferByWhome) && !TextUtils.isEmpty(mNameOfPerson)) {
                    if (mReferByWhome.equals(getString(R.string.supervisor)))
                        hmp.put("referral_supervisor", mNameOfPerson);
                    else
                        hmp.put("referral_driver", mNameOfPerson);
                }

                if (!TextUtils.isEmpty(String.valueOf(iUniqueKey)))
                    hmp.put("unique_key", String.valueOf(iUniqueKey));
                if (!TextUtils.isEmpty(mClientDateTime))
                    hmp.put("clientdatetime", mClientDateTime);
                if (!TextUtils.isEmpty(formatted_address))
                    hmp.put("google_address", formatted_address);
                if (!TextUtils.isEmpty(mFirstName))
                    hmp.put("driver_firstname", mFirstName);
                if (!TextUtils.isEmpty(mLastName))
                    hmp.put("driver_lastname", mLastName);
                if (!TextUtils.isEmpty(mDob))
                    hmp.put("driver_dob", mDob);
                if (!TextUtils.isEmpty(mTempLineOne))
                    hmp.put("t_address_1", mTempLineOne);
                if (!TextUtils.isEmpty(mTempLineTwo))
                    hmp.put("t_address_2", mTempLineTwo);
                if (!TextUtils.isEmpty(mTempCity))
                    hmp.put("t_city_town", mTempCity);
                if (!TextUtils.isEmpty(mTempState))
                    hmp.put("t_state", mTempState);
                if (!TextUtils.isEmpty(mTempPincode))
                    hmp.put("t_pincode", mTempPincode);
                if (!TextUtils.isEmpty(mTempLandmark))
                    hmp.put("t_landmark", mTempLandmark);
                if (!TextUtils.isEmpty(mPerLineOne))
                    hmp.put("p_address_1", mPerLineOne);
                if (!TextUtils.isEmpty(mPerLineTwo))
                    hmp.put("p_address_2", mPerLineTwo);
                if (!TextUtils.isEmpty(mPerCity))
                    hmp.put("p_city_town", mPerCity);
                if (!TextUtils.isEmpty(mPerState))
                    hmp.put("p_state", mPerState);
                if (!TextUtils.isEmpty(mPerPincode))
                    hmp.put("p_pincode", mPerPincode);
                if (!TextUtils.isEmpty(mPerLandmark))
                    hmp.put("p_landmark", mPerLandmark);
                if (!TextUtils.isEmpty(mDriverEmailID))
                    hmp.put("driver_email", mDriverEmailID);
                if (!TextUtils.isEmpty(mPhoneNo))
                    hmp.put("driver_phoneno", mPhoneNo);
                if (!TextUtils.isEmpty(mGender))
                    hmp.put("driver_gender", mGender);
                if (!TextUtils.isEmpty(mPermitFlag))
                    hmp.put("driver_permit_flag", mPermitFlag);
                if (!TextUtils.isEmpty(mPermitNo))
                    hmp.put("driver_permit_no", mPermitNo);
                if (!TextUtils.isEmpty(mBadgeFlag))
                    hmp.put("driver_badge_flag", mBadgeFlag);
                if (!TextUtils.isEmpty(mBadgeNo))
                    hmp.put("driver_badge_no", mBadgeNo);
                if (!TextUtils.isEmpty(mStayInMumbai))
                    hmp.put("driver_incity_since", mStayInMumbai);
                if (!TextUtils.isEmpty(mOtherLookup))
                    hmp.put("other_company_code", mOtherLookup);
                if (!TextUtils.isEmpty(mOtherName))
                    hmp.put("other_company_name", mOtherName);
                if (!TextUtils.isEmpty(mCabNeedFlag))
                    hmp.put("cab_needed_flag", mCabNeedFlag);
                if (!TextUtils.isEmpty(mRemark))
                    hmp.put("remark", mRemark);
                if (!TextUtils.isEmpty(mInternalComment))
                    hmp.put("internal_comment", mInternalComment);
                if (!TextUtils.isEmpty(mPanNo))
                    hmp.put("driver_pancard", mPanNo);
                if (!TextUtils.isEmpty(mAadharNo))
                    hmp.put("driver_aadhar", mAadharNo);
                if (!TextUtils.isEmpty(mLicenceNo))
                    hmp.put("driver_license_no", mLicenceNo);
                if (!TextUtils.isEmpty(modifydate))
                    hmp.put("last_modified_datetime", modifydate);
                if (!TextUtils.isEmpty(mDriverImgName))
                    hmp.put("driver_image_name", mDriverImgName);
                if (!TextUtils.isEmpty(mDriverImgPath))
                    hmp.put("driver_image_path", mDriverImgPath);
                hmp.put("sent_to_server_flag", "0");
                if (!TextUtils.isEmpty(mDriverImgClientDateTime))
                    hmp.put("driver_image_datettime", mDriverImgClientDateTime);
                if (!TextUtils.isEmpty(mPanImgName))
                    hmp.put("pancard_image_name", mPanImgName);
                if (!TextUtils.isEmpty(mPanImgPath))
                    hmp.put("pancard_image_path", mPanImgPath);
                if (!TextUtils.isEmpty(mPanImgClientDateTime))
                    hmp.put("pancard_image_datettime", mPanImgClientDateTime);
                if (!TextUtils.isEmpty(mAdharImgName))
                    hmp.put("aadhar_image_name", mAdharImgName);
                if (!TextUtils.isEmpty(mAdharImgPath))
                    hmp.put("aadhar_image_path", mAdharImgPath);
                if (!TextUtils.isEmpty(mAdharImgClientDateTime))
                    hmp.put("aadhar_image_datetime", mAdharImgClientDateTime);
                if (!TextUtils.isEmpty(mLicenseImgName))
                    hmp.put("license_image_name", mLicenseImgName);
                if (!TextUtils.isEmpty(mLicenseImgPath))
                    hmp.put("license_image_path", mLicenseImgPath);
                if (!TextUtils.isEmpty(mLicenseImgClientDateTime))
                    hmp.put("license_image_datetime", mLicenseImgClientDateTime);
                if (!TextUtils.isEmpty(mVehicleNo))
                    hmp.put("driver_vehicle_no", mVehicleNo);
                if (!TextUtils.isEmpty(mPaymentAmount))
                    hmp.put("payment_amount", mPaymentAmount);
                if (!TextUtils.isEmpty(mPaymentFlag))
                    hmp.put("payment_status", mPaymentFlag);
                if (!TextUtils.isEmpty(mReceiptDate))
                    hmp.put("receipt_date", mReceiptDate);
                if (!TextUtils.isEmpty(mReceiptNo))
                    hmp.put("receipt_no", mReceiptNo);
                if (!TextUtils.isEmpty(mUniqueId))
                    hmp.put("uniqueid", mUniqueId);
                if (saveData) {
                    int status = mApp.mDBHelper.updateDriverProfile(oldPhoneNo, hmp, baDriver, baPan, baAadhar, baLicence);
                    if (status == 1) {
                        Toast.makeText(ActDriverProfileRegistration.this,
                                "Successfully updated", Toast.LENGTH_LONG).show();
                        ActDriverProfileRegistration.this.finish();
                    } else {
                        Toast.makeText(ActDriverProfileRegistration.this,
                                "Sorry! Updation failed.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    long status = mApp.mDBHelper.insertDriverProfile(hmp, baDriver, baPan, baAadhar, baLicence);
                    if (status != -1)
                        AlertBoxDone();
                    else {
                        Toast.makeText(ActDriverProfileRegistration.this,
                                "Sorry! Duplicate contact no.", Toast.LENGTH_LONG).show();
                    }
                }
                if (bitmap != null)
                    bitmap.recycle();
                bitmap = null;
                return false;
            }
        });
        if (!saveData) {
            menuEdit.setVisible(false);
            menuDone.setVisible(true);
            menuList.setVisible(true);
        } else {
            menuEdit.setVisible(true);
            menuDone.setVisible(false);
            menuList.setVisible(false);
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.driver_registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.item_deactivate:
                PrefUtils.setKioskModeActive(false, getApplicationContext());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity();
                }else {
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setEnabled(boolean value) {
        rlAdd.setEnabled(value);
        etReceiptNo.setEnabled(value);
        etReceiptDae.setEnabled(value);
        etNameFirst.setEnabled(value);
        etNameLast.setEnabled(value);
        etTempAddrOne.setEnabled(value);
        etTempAddrTwo.setEnabled(value);
        etTempCity.setEnabled(value);
        etTempState.setEnabled(value);
        etTempPincode.setEnabled(value);
        etTempLandmark.setEnabled(value);
        etPerAddrOne.setEnabled(value);
        etPerAddrTwo.setEnabled(value);
        etPerCity.setEnabled(value);
        etPerState.setEnabled(value);
        etPerPincode.setEnabled(value);
        etPerLandmark.setEnabled(value);
        etPhoneNo.setEnabled(value);
        etDriverEmailid.setEnabled(value);
        etVehicleNo.setEnabled(value);
        etPermitNo.setEnabled(value);
        etBadgeNo.setEnabled(value);
        etStay.setEnabled(value);
        etOLAId.setEnabled(value);
        etUBRId.setEnabled(value);
        etMRUId.setEnabled(value);
        etBMCId.setEnabled(value);
        etECAId.setEnabled(value);
        etTABId.setEnabled(value);
        etTFSId.setEnabled(value);
        etVIRId.setEnabled(value);
        etPRIId.setEnabled(value);
        etARYId.setEnabled(value);
        etBNYId.setEnabled(value);
        etOTHId.setEnabled(value);
        etEnterName.setEnabled(value);
        etRemark.setEnabled(value);
        etInternalComment.setEnabled(value);
        etPan.setEnabled(value);
        etAadharNo.setEnabled(value);
        etLicenceNo.setEnabled(value);
        tvAge.setEnabled(value);
        chkAdd.setEnabled(value);
        chkOLA.setEnabled(value);
        chkUBR.setEnabled(value);
        chkMRU.setEnabled(value);
        chkBMC.setEnabled(value);
        chkECA.setEnabled(value);
        chkTAB.setEnabled(value);
        chkTFS.setEnabled(value);
        chkVIR.setEnabled(value);
        chkPRI.setEnabled(value);
        chkARY.setEnabled(value);
        chkBNY.setEnabled(value);
        chkOTH.setEnabled(value);
        imgCameraDriver.setEnabled(value);
        imgCameraPan.setEnabled(value);
        imgCameraAadhar.setEnabled(value);
        imgCameraLicence.setEnabled(value);
        findViewById(R.id.rbMale).setEnabled(value);
        findViewById(R.id.rbFemale).setEnabled(value);
        findViewById(R.id.rbPermitYes).setEnabled(value);
        findViewById(R.id.rbPermitNo).setEnabled(value);
        findViewById(R.id.rbBadgeYes).setEnabled(value);
        findViewById(R.id.rbBadgeNo).setEnabled(value);
        findViewById(R.id.rgCabYes).setEnabled(value);
        findViewById(R.id.rgCabNo).setEnabled(value);
        findViewById(R.id.rbWalkin).setEnabled(value);
        findViewById(R.id.rbReferral).setEnabled(value);
        findViewById(R.id.rbSupervisor).setEnabled(value);
        findViewById(R.id.rbDriver).setEnabled(value);
        findViewById(R.id.rb500).setEnabled(value);
        findViewById(R.id.rb3000).setEnabled(value);
        findViewById(R.id.rb3500).setEnabled(value);
        etNameOfPerson.setEnabled(value);
        etReceiptNoTwo.setEnabled(value);
        etReceiptDateTwo.setEnabled(value);
        etReceiptNoThree.setEnabled(value);
        etReceiptDateThree.setEnabled(value);
        //chkBackOut.setEnabled(value);
        //  spnBackoutReason.setEnabled(value);
        //  spnBackoutReason.setClickable(false);
        //etBackoutReason.setEnabled(value);
        etExpDate.setEnabled(value);

    }

    @Override
    public void onBackPressed() {
        if (backPressed) {
            PrefUtils.setKioskModeActive(true, getApplicationContext());
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            ActDriverProfileRegistration.this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            ActDriverProfileRegistration.this.finish();
        }
    }

    private boolean connectionError(final Context context, String msg) {
        String settingOk = "Setting", settingCancel = "Skip";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            settingOk = "Yes";
            settingCancel = "No";

        }

        if (Connectivity.checkConnected(context)) {
            return false;
        } else {
            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(context);
            alertDialog.setTitle("No Internet Connection");
            alertDialog.setMessage(msg);
            alertDialog.setPositiveButton(settingOk, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {


                    try {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            Connectivity.setMobileDataEnabled(context, true);

                        } else {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            context.startActivity(intent);
                        }
                        ((Activity) context).finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Please start your internet manually", Toast.LENGTH_SHORT).show();
                    }

                }

            });
            alertDialog.setNegativeButton(settingCancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    ((Activity) context).finish();
                    dialog.cancel();
                }

            });
            alertDialog.show();

            return true;
        }
    }

    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (blockedKeys.contains(event.getKeyCode())) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

}