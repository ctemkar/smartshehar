package com.smartshehar.dashboard.app.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;

public class ActIssueFilter extends AppCompatActivity
        implements DatePickerClass.OnDateListener {
    ProgressDialog mProgressDialog;
    CheckBox chkMyComplaints,
            chkPV, chkMV,
            chkRT,
            chkAUT,
            chkRD,
            chkCL,
            chkEN,
            chkSF,
            chkWE,
            chkOG,
            chkRS,
            chkAll, chkClosed, chkResolved;
    SimpleDateFormat ft;
    Date date;

    public static String TAG = "FilterCategoryView";
    boolean hideCatFlag = true;
    boolean mFromDateFlag, mToDateFlag, mFromMap;
    DialogFragment mFrag;
    int mMyIssueVal = 0, mShowAllVal = 1;
    String
            PVVal = "",
            MVVal = "",
            RTVal = "",
            AUTVal = "",
            RDVal = "",
            CLVal = "",
            ENVal = "",
            SFVal = "",
            WEVal = "",
            OGVal = "",
            RSVal = "",

    closedVal = "0", resolvedVal = "0",
            mPostalCode, mFromDate, mToDate, mIssueId = "";
    ImageView imgMore;

    ScrollView svFilter;
    LinearLayout svCategory;
    WebView wvReport;
    String link = Constants_dp.REPORT_FILTER_URL, mLink;
    TextView txtFromDate, txtToDate;
    EditText etPostalCode;
    boolean wvVisible;
    Connectivity mConnectivity;
    RelativeLayout rlCat;
    Location mCurrentLocation;
    public static LatLng mLatLng = null;
    Button btnReset, btnFilter, btnMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_issue_filter);
        mFrag = new DatePickerClass();
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(ActIssueFilter.this)) {
            if (!mConnectivity.connectionError(ActIssueFilter.this)) {
                if (mConnectivity.isGPSEnable(ActIssueFilter.this)) {
                    Log.d(TAG, "Internet Connection");
                }
            }
        }
        init();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mProgressDialog = new ProgressDialog(ActIssueFilter.this);
        mProgressDialog.setMessage("Loading... Please wait...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mCurrentLocation = CGlobals_db.getInstance(ActIssueFilter.this).getMyLocation(
                this);
        if (mCurrentLocation == null) {
            mCurrentLocation = new Location("");

            mCurrentLocation.setLatitude(Double.valueOf(CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(ActIssueFilter.this).getString(Constants_dp.PREF_GET_LAST_LAT,
                            String.valueOf(Constants_dp.MINCITYLAT))));
            mCurrentLocation.setLongitude(Double.valueOf(CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(ActIssueFilter.this).getString(Constants_dp.PREF_GET_LAST_LNG,
                            String.valueOf(Constants_dp.MINCITYLON))));
        }
        chkClosed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    closedVal = "1";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_CLOSED, true).apply();
                } else {
                    closedVal = "0";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_CLOSED, false).apply();
                }
            }
        });
        chkResolved.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    resolvedVal = "1";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_RESOLVED, true).apply();
                } else {
                    resolvedVal = "0";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_RESOLVED, false).apply();
                }
            }
        });
        chkMyComplaints.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    chkAll.setChecked(false);

                    mMyIssueVal = 1;
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_MY_COMPLAINTS, true).apply();

                } else {
                    mMyIssueVal = 0;
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_MY_COMPLAINTS, false).apply();
                }
            }
        });
        chkAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    chkMyComplaints.setChecked(false);
                    chkPV.setChecked(true);
                    chkMV.setChecked(true);
                    chkRT.setChecked(true);
                    chkAUT.setChecked(true);
                    chkRD.setChecked(true);
                    chkCL.setChecked(true);
                    chkEN.setChecked(true);
                    chkSF.setChecked(true);
                    chkWE.setChecked(true);
                    chkOG.setChecked(true);
                    chkRS.setChecked(true);
                    mShowAllVal = 1;
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_ALL, true).apply();

                } else {

                    chkPV.setChecked(false);
                    chkMV.setChecked(false);
                    chkRT.setChecked(false);
                    chkAUT.setChecked(false);
                    chkRD.setChecked(false);
                    chkCL.setChecked(false);
                    chkEN.setChecked(false);
                    chkSF.setChecked(false);
                    chkWE.setChecked(false);
                    chkOG.setChecked(false);
                    chkRS.setChecked(false);
                    mShowAllVal = 0;
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_ALL, false).apply();
                }
            }
        });


        chkPV.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PVVal = getString(R.string.PVVal);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_PV, true).apply();
                } else {
                    PVVal = "";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_PV, false).apply();
                }
            }
        });
        chkMV.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MVVal = getString(R.string.MVVal);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_MV, true).apply();
                } else {
                    MVVal = "";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_MV, false).apply();
                }
            }
        });
        chkRT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    RTVal = getString(R.string.RTVal);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_RT, true).apply();
                } else {
                    RTVal = "";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_RT, false).apply();
                }
            }
        });
        chkAUT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AUTVal = getString(R.string.AUTVal);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_AUT, true).apply();
                } else {
                    AUTVal = "";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_AUT, false).apply();
                }
            }
        });
        chkRD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    RDVal = getString(R.string.RDVal);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_RD, true).apply();
                } else {
                    RDVal = "";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_RD, false).apply();
                }
            }
        });
        chkCL.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    CLVal = getString(R.string.CLVal);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_CL, true).apply();
                } else {
                    CLVal = "";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_CL, false).apply();
                }
            }
        });
        chkEN.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ENVal = getString(R.string.ENVal);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_EN, true).apply();
                } else {
                    ENVal = "";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_EN, false).apply();
                }
            }
        });
        chkSF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SFVal = getString(R.string.SFVal);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_SF, true).apply();
                } else {
                    SFVal = "";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_SF, false).apply();
                }
            }
        });
        chkWE.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    WEVal = getString(R.string.WEVal);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_WE, true).apply();
                } else {
                    WEVal = "";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_WE, false).apply();
                }
            }
        });
        chkOG.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    OGVal = getString(R.string.OGVal);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_OG, true).apply();
                } else {
                    OGVal = "";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_OG, false).apply();
                }
            }
        });
        chkRS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    RSVal = getString(R.string.RSVal);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_RS, true).apply();
                } else {
                    RSVal = "";
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                            putBoolean(Constants_dp.PREF_FILTER_RS, false).apply();
                }
            }
        });


        rlCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hideCatFlag) {
                    svCategory.setVisibility(View.VISIBLE);
                    imgMore.setImageResource(R.mipmap.ic_close);
                    hideCatFlag = false;

                } else {
                    svCategory.setVisibility(View.GONE);
                    hideCatFlag = true;
                    imgMore.setImageResource(R.mipmap.ic_open);
                }
            }
        });
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                returnBack();
            }
        });
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Connectivity.checkConnected(ActIssueFilter.this)) {
                    if (!mConnectivity.connectionError(ActIssueFilter.this)) {
                        Log.d(TAG, "Internet Connection");
                    }
                } else {
                    wvReport.setVisibility(View.VISIBLE);
                    svFilter.setVisibility(View.GONE);
                    hideCatFlag = true;
                    imgMore.setImageResource(R.mipmap.ic_open);
                    mPostalCode = etPostalCode.getText().toString().trim();
                    mFromDate = txtFromDate.getText().toString().trim();
                    mToDate = txtToDate.getText().toString().trim();
                    loadUrl();
                }

            }
        });
        txtFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFromDateFlag = true;
                mToDateFlag = false;
                if (mFrag.isAdded()) {
                    return;
                }
                mFrag.show(getSupportFragmentManager(), "datePicker");
            }
        });
        txtToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFromDateFlag = false;
                mToDateFlag = true;
                if (mFrag.isAdded()) {
                    return;
                }
                mFrag.show(getSupportFragmentManager(), "datePicker");
            }
        });
       /* etPostalCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                etPostalCode.setFocusable(true);
                etPostalCode.setFocusableInTouchMode(true);
                etPostalCode.setClickable(true);
                return false;
            }
        });*/
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                txtFromDate.setText("");
                txtToDate.setText("");
                etPostalCode.setText("");
                chkResolved.setChecked(false);
                chkClosed.setChecked(false);
                chkMyComplaints.setChecked(false);
                chkAll.setChecked(true);


                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                        putString(Constants_dp.PREF_FILTER_FROM_DATE, "").apply();
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                        putString(Constants_dp.PREF_FILTER_TO_DATE, "").apply();
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                        putString(Constants_dp.PREF_FILTER_POSTAL_CODE, "").apply();
            }
        });
        etPostalCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPostalCode = s.toString();
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                        putString(Constants_dp.PREF_FILTER_POSTAL_CODE, mPostalCode).apply();
            }
        });
        setValues();
        Bundle bundle = getIntent().getBundleExtra("bundle");
        if (bundle != null) {
            mLatLng = bundle.getParcelable("location");
            mIssueId = bundle.getString("issueid");
            mPostalCode = bundle.getString("postalcode");
            mFromMap = bundle.getBoolean("mFromMap");
            if (mLatLng == null) {
                Toast.makeText(ActIssueFilter.this, "Location not found", Toast.LENGTH_LONG).show();
                finish();
            }
            wvReport.setVisibility(View.VISIBLE);
            svFilter.setVisibility(View.GONE);
            hideCatFlag = true;
            imgMore.setImageResource(R.mipmap.ic_open);
            loadUrl();
        }
    }

    private void init() {
        chkMyComplaints = (CheckBox) findViewById(R.id.chkMyComplaints);

        chkPV = (CheckBox) findViewById(R.id.chkPV);
        chkMV = (CheckBox) findViewById(R.id.chkMV);
        chkRT = (CheckBox) findViewById(R.id.chkRT);
        chkAUT = (CheckBox) findViewById(R.id.chkAUT);
        chkRD = (CheckBox) findViewById(R.id.chkRD);
        chkCL = (CheckBox) findViewById(R.id.chkCL);
        chkEN = (CheckBox) findViewById(R.id.chkEN);
        chkSF = (CheckBox) findViewById(R.id.chkSF);
        chkWE = (CheckBox) findViewById(R.id.chkWE);
        chkOG = (CheckBox) findViewById(R.id.chkOG);
        chkRS = (CheckBox) findViewById(R.id.chkRS);
        chkAll = (CheckBox) findViewById(R.id.chkAll);

        imgMore = (ImageView) findViewById(R.id.imgMore);
        btnMap = (Button) findViewById(R.id.btnMap);
        svFilter = (ScrollView) findViewById(R.id.svFilter);
        svCategory = (LinearLayout) findViewById(R.id.svCategory);
        wvReport = (WebView) findViewById(R.id.wvReport);
        btnFilter = (Button) findViewById(R.id.btnFilter);
        txtFromDate = (TextView) findViewById(R.id.txtFromDate);
        txtToDate = (TextView) findViewById(R.id.txtToDate);
        etPostalCode = (EditText) findViewById(R.id.etPostalCode);
        rlCat = (RelativeLayout) findViewById(R.id.rlCat);
        chkClosed = (CheckBox) findViewById(R.id.chkClosed);
        chkResolved = (CheckBox) findViewById(R.id.chkResolved);
        btnReset = (Button) findViewById(R.id.btnReset);
        wvReport.getSettings().setJavaScriptEnabled(true);
        wvReport.getSettings().setGeolocationEnabled(true);
        wvReport.getSettings().setLoadWithOverviewMode(true);
        wvReport.getSettings().setUseWideViewPort(true);
        wvReport.getSettings().setBuiltInZoomControls(true);
        wvReport.getSettings().setSupportZoom(true);
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_MY_COMPLAINTS, false)) {
            mMyIssueVal = 1;
            chkMyComplaints.setChecked(true);
        } else {
            mMyIssueVal = 0;
            chkMyComplaints.setChecked(false);
        }
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_ALL, true)) {
            mShowAllVal = 1;
            chkAll.setChecked(true);
        } else {
            mShowAllVal = 0;
            chkAll.setChecked(false);
        }
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_CLOSED, false)) {
            closedVal = "1";
            chkClosed.setChecked(true);
        } else {
            closedVal = "0";
            chkClosed.setChecked(false);
        }
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_RESOLVED, false)) {
            resolvedVal = "1";
            chkResolved.setChecked(true);
        } else {
            resolvedVal = "0";
            chkResolved.setChecked(false);
        }

        ///
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_PV, true)) {
            chkPV.setChecked(true);
            PVVal = getString(R.string.PVVal);
        } else {
            PVVal = "";
            chkPV.setChecked(false);
        }
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_MV, true)) {
            chkMV.setChecked(true);
            MVVal = getString(R.string.MVVal);
        } else {
            MVVal = "";
            chkMV.setChecked(false);
        }
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_RT, true)) {
            chkRT.setChecked(true);
            RTVal = getString(R.string.RTVal);
        } else {
            RTVal = "";
            chkRT.setChecked(false);
        }
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_AUT, true)) {
            chkAUT.setChecked(true);
            AUTVal = getString(R.string.AUTVal);
        } else {
            AUTVal = "";
            chkAUT.setChecked(false);
        }
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_RD, true)) {
            chkRD.setChecked(true);
            RDVal = getString(R.string.RDVal);
        } else {
            RDVal = "";
            chkRD.setChecked(false);
        }
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_CL, true)) {
            chkCL.setChecked(true);
            CLVal = getString(R.string.CLVal);
        } else {
            CLVal = "";
            chkCL.setChecked(false);
        }
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_EN, true)) {
            chkEN.setChecked(true);
            ENVal = getString(R.string.ENVal);
        } else {
            ENVal = "";
            chkEN.setChecked(false);
        }
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_SF, true)) {
            chkSF.setChecked(true);
            SFVal = getString(R.string.SFVal);
        } else {
            SFVal = "";
            chkSF.setChecked(false);
        }
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_WE, true)) {
            chkWE.setChecked(true);
            WEVal = getString(R.string.WEVal);
        } else {
            WEVal = "";
            chkWE.setChecked(false);
        }
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_OG, true)) {
            chkOG.setChecked(true);
            OGVal = getString(R.string.OGVal);
        } else {
            OGVal = "";
            chkOG.setChecked(false);
        }
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this)
                .getBoolean(Constants_dp.PREF_FILTER_RS, true)) {
            chkRS.setChecked(true);
            RSVal = getString(R.string.RSVal);
        } else {
            RSVal = "";
            chkRS.setChecked(false);
        }

        wvReport.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mProgressDialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                try {
                    mProgressDialog.dismiss();
                    view.clearCache(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadUrl() {
        try {

            wvVisible = true;

            if (!TextUtils.isEmpty(mToDate)) {
                ft = new SimpleDateFormat("dd-MM-yyyy");
                date = ft.parse(mToDate);
                ft = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                mToDate = ft.format(date) + " 23:59:59";
            } else {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                mToDate = String.valueOf(year) + "-" + (month + 1) + "-" + day + " 23:59:59";
            }
            if (!TextUtils.isEmpty(mFromDate)) {
                ft = new SimpleDateFormat("dd-MM-yyyy");
                date = ft.parse(mFromDate);
                ft = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                mFromDate = ft.format(date) + " 00:00:00";
            } else {
                mToDate = "";
            }
            SimpleDateFormat df = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT,
                    Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            mLink = link + "&showall=" + String.valueOf(mShowAllVal)
                    + "&myissue=" + String.valueOf(mMyIssueVal)
                    + "&clientdatetime=" + df.format(cal.getTime())
                    + "&email=" + CGlobals_db.msGmail
                    + "&lat=" + String.format("%.9f", mCurrentLocation.getLatitude())
                    + "&lng=" + String.format("%.9f", mCurrentLocation.getLongitude())
                    + "&appuserid=" + CGlobals_lib_ss.getInstance().
                    getPersistentPreference(ActIssueFilter.this).
                    getInt(Constants_lib_ss.PREF_APPUSERID, -1);

            if (!TextUtils.isEmpty(mPostalCode))
                mLink = mLink + "&postalcode=" + mPostalCode;
            if (!TextUtils.isEmpty(mFromDate)) {
                mLink = mLink + "&fromissuedate=" + mFromDate;
                mLink = mLink + "&toissuedate=" + mToDate;
            }
            if (!TextUtils.isEmpty(PVVal))
                mLink = mLink + "&PV=" + PVVal;
            if (!TextUtils.isEmpty(MVVal))
                mLink = mLink + "&MV=" + MVVal;
            if (!TextUtils.isEmpty(RTVal))
                mLink = mLink + "&RT=" + RTVal;
            if (!TextUtils.isEmpty(AUTVal))
                mLink = mLink + "&AT=" + AUTVal;
            if (!TextUtils.isEmpty(RDVal))
                mLink = mLink + "&RD=" + RDVal;
            if (!TextUtils.isEmpty(CLVal))
                mLink = mLink + "&CL=" + CLVal;
            if (!TextUtils.isEmpty(ENVal))
                mLink = mLink + "&EN=" + ENVal;
            if (!TextUtils.isEmpty(SFVal))
                mLink = mLink + "&SF=" + SFVal;
            if (!TextUtils.isEmpty(WEVal))
                mLink = mLink + "&WE=" + WEVal;
            if (!TextUtils.isEmpty(OGVal))
                mLink = mLink + "&OG=" + OGVal;
            if (!TextUtils.isEmpty(RSVal))
                mLink = mLink + "&RS=" + RSVal;

            if (!closedVal.equalsIgnoreCase("0"))
                mLink = mLink + "&closed=" + closedVal;
            if (!resolvedVal.equalsIgnoreCase("0"))
                mLink = mLink + "&resolved=" + resolvedVal;
            if (!TextUtils.isEmpty(mIssueId))
                mLink = mLink + "&issueid=" + mIssueId;
            if (mLatLng != null) {
                mLink = mLink + "&markerlat=" + mLatLng.latitude;
                mLink = mLink + "&markerlng=" + mLatLng.longitude;
            }

            wvReport.loadUrl(mLink);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void gotDateResult(String sDate) {
        if (mToDateFlag) {
            txtToDate.setText(sDate);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                    putString(Constants_dp.PREF_FILTER_TO_DATE, sDate).apply();
        }
        if (mFromDateFlag) {
            txtFromDate.setText(sDate);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActIssueFilter.this).
                    putString(Constants_dp.PREF_FILTER_FROM_DATE, sDate).apply();
        }
    }

    @Override
    public void onBackPressed() {
        if (mFromMap)
            finish();
        else {
            if (wvVisible) {
                wvReport.loadUrl("about:blank");
                wvReport.setVisibility(View.GONE);
                svFilter.setVisibility(View.VISIBLE);
                wvVisible = false;
            } else {
                super.onBackPressed();
                returnBack();
            }
        }
    }
    private void returnBack() {
        try {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            ActIssueFilter.this.finish();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setValues() {
        mFromDate = CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getString(Constants_dp.PREF_FILTER_FROM_DATE, "");
        mToDate = CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getString(Constants_dp.PREF_FILTER_TO_DATE, "");
        mPostalCode = CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getString(Constants_dp.PREF_FILTER_POSTAL_CODE, "");

        if (!TextUtils.isEmpty(mFromDate))
            txtFromDate.setText(mFromDate);
        if (!TextUtils.isEmpty(mToDate))
            txtToDate.setText(mToDate);
        if (!TextUtils.isEmpty(mPostalCode))
            etPostalCode.setText(mPostalCode);

        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_CLOSED, false)) {
            closedVal = "1";
        } else closedVal = "0";

        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_RESOLVED, false)) {
            resolvedVal = "1";
        } else resolvedVal = "0";

        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_MY_COMPLAINTS, false)) {
            mMyIssueVal = 1;
        } else mMyIssueVal = 0;

        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_ALL, true)) {
            mShowAllVal = 1;
        } else mShowAllVal = 0;

        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_PV, true)) {
            PVVal = getString(R.string.PVVal);
        } else PVVal = "";
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_MV, true)) {
            MVVal = getString(R.string.MVVal);
        } else MVVal = "";
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_RT, true)) {
            RTVal = getString(R.string.RTVal);
        } else RTVal = "";
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_AUT, true)) {
            AUTVal = getString(R.string.AUTVal);
        } else AUTVal = "";
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_RD, true)) {
            RDVal = getString(R.string.RDVal);
        } else RDVal = "";
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_CL, true)) {
            CLVal = getString(R.string.CLVal);
        } else CLVal = "";
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_EN, true)) {
            ENVal = getString(R.string.ENVal);
        } else ENVal = "";
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_SF, true)) {
            SFVal = getString(R.string.SFVal);
        } else SFVal = "";
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_WE, true)) {
            WEVal = getString(R.string.WEVal);
        } else WEVal = "";
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_OG, true)) {
            OGVal = getString(R.string.OGVal);
        } else OGVal = "";
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(ActIssueFilter.this).
                getBoolean(Constants_dp.PREF_FILTER_RS, true)) {
            RSVal = getString(R.string.RSVal);
        } else RSVal = "";
    }
}
