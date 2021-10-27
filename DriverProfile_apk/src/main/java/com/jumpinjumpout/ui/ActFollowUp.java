package com.jumpinjumpout.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jumpinjumpout.CGlobals_dp;
import com.jumpinjumpout.DriverInfo;
import com.jumpinjumpout.PrefUtils;
import com.jumpinjumpout.www.driverprofile.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ActFollowUp extends AppCompatActivity {
    CheckBox chkInitialPayment, chkFullPayment, chkPrivateVerification,
            chkPoliceVerification, chkBackOut;
    EditText etBackoutReason;
    Button btnDone;
    LinearLayout llFollowup, llBackout;
    TextView txtDriverName, txtPhoneNo,txtBackoutReason;
    RelativeLayout rlBackoutReason;
    RadioGroup rgBackout;
    RadioButton rbNoDocs, rbChangeMind, rbOther;
    ImageView imgMore;
    boolean openFlag;
    String msPhone, msBackoutReason;
    CGlobals_dp mApp = null;
    boolean doubleBackToExitPressedOnce = false, backPressed = true;
    DriverInfo maDriverInfo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_follow_up);
        mApp = CGlobals_dp.getInstance(ActFollowUp.this);
        mApp.init(this);
        init();
        Bundle extras = getIntent().getBundleExtra("data");
        if (extras != null) {
            msPhone = extras.getString("driverphoneno");

            getDriverProfile();
        }
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (chkBackOut.isChecked()) {
                    RadioButton rbReason;
                    int selectedReason = rgBackout.getCheckedRadioButtonId();
                    if (selectedReason != -1) {
                        rbReason = (RadioButton) findViewById(selectedReason);
                        msBackoutReason = rbReason.getText().toString().trim();
                        if (msBackoutReason.equals("Other")) {
                            if (!TextUtils.isEmpty(etBackoutReason.getText().toString()))
                                msBackoutReason = etBackoutReason.getText().toString();
                            else {
                                Toast.makeText(ActFollowUp.this,
                                        "Please Select backout Reason", Toast.LENGTH_LONG).show();
                                return;
                            }

                        }

                    } else {
                        Toast.makeText(ActFollowUp.this,
                                "Please Select backout Reason", Toast.LENGTH_LONG).show();
                        return;
                    }
                }else{
                    msBackoutReason="-1";
                }


                HashMap<String, String> hmp = new HashMap<>();
                if (chkPrivateVerification.isChecked())
                    hmp.put("private_verification", "1");
                else
                    hmp.put("private_verification", "0");
                if (chkPoliceVerification.isChecked())
                    hmp.put("police_verification", "1");
                else
                    hmp.put("police_verification", "0");
                    hmp.put("driver_backout_reason", msBackoutReason);
                
                hmp.put("sent_to_server_flag", "0");
                int status = mApp.mDBHelper.updateDriverProfile(msPhone, hmp, null, null, null, null);
                if (status == 1) {
//                    Toast.makeText(ActFollowUp.this,
//                            "Successfully updated", Toast.LENGTH_LONG).show();
                    ActFollowUp.this.finish();
                } else {
                    Toast.makeText(ActFollowUp.this,
                            "Sorry! Updation failed.", Toast.LENGTH_LONG).show();
                }
            }

        });
        chkBackOut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    rlBackoutReason.setVisibility(View.VISIBLE);
                else {
                    rlBackoutReason.setVisibility(View.GONE);
                    rbChangeMind.setChecked(false);
                    rbOther.setChecked(false);
                    rbNoDocs.setChecked(false);
                    msBackoutReason = "";
                    etBackoutReason.setVisibility(View.GONE);
                }
            }
        });
        rlBackoutReason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!openFlag) {
                    llBackout.setVisibility(View.VISIBLE);
                    imgMore.setImageResource(R.mipmap.ic_close);
                    openFlag = true;
                } else {
                    llBackout.setVisibility(View.GONE);
                    imgMore.setImageResource(R.mipmap.ic_open);
                    openFlag = false;
                    //  rbChangeMind.setChecked(false);
                    //  rbOther.setChecked(false);
                    //rbNoDocs.setChecked(false);
                    //msBackoutReason="";
                    if(!rbOther.isChecked())
                    {
                        etBackoutReason.setVisibility(View.GONE);
                        msBackoutReason="";
                    }
                }
            }
        });
        rbOther.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etBackoutReason.setVisibility(View.VISIBLE);
                    llBackout.setVisibility(View.GONE);
                    imgMore.setImageResource(R.mipmap.ic_open);
                    txtBackoutReason.setText(getString(R.string.other));

                } else {
                    etBackoutReason.setVisibility(View.GONE);
                }
            }
        });
        rbChangeMind.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    llBackout.setVisibility(View.GONE);
                    imgMore.setImageResource(R.mipmap.ic_open);
                    txtBackoutReason.setText(getString(R.string.mind_changed));

                }
            }
        });
        rbNoDocs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    llBackout.setVisibility(View.GONE);
                    imgMore.setImageResource(R.mipmap.ic_open);
                    txtBackoutReason.setText(getString(R.string.no_document));

                }
            }
        });


    }

    private void getDriverProfile() {

        ArrayList<DriverInfo> maList = mApp.mDBHelper.getDriverProfile(msPhone);

        try {
            for (int j = 0; j < maList.size(); j++) {
                maDriverInfo = maList.get(j);

                txtDriverName.setText(maDriverInfo.getDriver_firstname() + " " + maDriverInfo.getDriver_lastname());
                txtPhoneNo.setText(maDriverInfo.getDriver_phoneno());
                if (!TextUtils.isEmpty(maDriverInfo.getPayment_amount())) {
                    if (maDriverInfo.getPayment_amount().equals(getString(R.string.five_hundred)) ||
                            maDriverInfo.getPayment_amount().equals(getString(R.string.three_thousand))) {
                        chkInitialPayment.setChecked(true);
                        chkInitialPayment.setClickable(false);
                    } else if (maDriverInfo.getPayment_amount().equals(getString(R.string.three_thousand_five_hundred))) {

                        chkInitialPayment.setChecked(true);
                        chkFullPayment.setChecked(true);
                        chkInitialPayment.setClickable(false);
                        chkFullPayment.setClickable(false);

                    } else {
                        chkInitialPayment.setChecked(false);
                        chkFullPayment.setChecked(false);
                        chkInitialPayment.setClickable(true);
                        chkFullPayment.setClickable(true);

                    }
                } else {
                    chkInitialPayment.setChecked(false);
                    chkFullPayment.setChecked(false);
                    chkInitialPayment.setClickable(true);
                    chkFullPayment.setClickable(true);

                }
            }
            if (maDriverInfo.getPrivate_verification().equals("1"))
                chkPrivateVerification.setChecked(true);
            else
                chkPrivateVerification.setChecked(false);
            if (maDriverInfo.getPolice_verification().equals("1"))
                chkPoliceVerification.setChecked(true);
            else
                chkPoliceVerification.setChecked(false);

            if (!TextUtils.isEmpty(maDriverInfo.getDriver_backout_reason())) {
                if (!maDriverInfo.getDriver_backout_reason().equals("-1")) {
                    chkBackOut.setChecked(true);
                    rlBackoutReason.setVisibility(View.VISIBLE);
                    llBackout.setVisibility(View.VISIBLE);
                    imgMore.setImageResource(R.mipmap.ic_close);
                    if (maDriverInfo.getDriver_backout_reason().equalsIgnoreCase(getString(R.string.mind_changed))) {
                        rbChangeMind.setChecked(true);
                    } else if (maDriverInfo.getDriver_backout_reason().equalsIgnoreCase(getString(R.string.no_document))) {
                        rbNoDocs.setChecked(true);
                    } else {
                        rbOther.setChecked(true);
                        etBackoutReason.setVisibility(View.VISIBLE);
                        etBackoutReason.setText(maDriverInfo.getDriver_backout_reason());
                    }
                }
            }


        } catch (Exception e) {

        }
    }

    private void init() {
        chkInitialPayment = (CheckBox) findViewById(R.id.chkInitialPayment);
        chkFullPayment = (CheckBox) findViewById(R.id.chkFullPayment);
        chkPrivateVerification = (CheckBox) findViewById(R.id.chkPrivateVerification);
        chkPoliceVerification = (CheckBox) findViewById(R.id.chkPoliceVerification);
        chkBackOut = (CheckBox) findViewById(R.id.chkBackOut);
        etBackoutReason = (EditText) findViewById(R.id.etBackoutReason);
        txtBackoutReason = (TextView) findViewById(R.id.txtBackoutReason);
        btnDone = (Button) findViewById(R.id.btnDone);
        llFollowup = (LinearLayout) findViewById(R.id.llFollowup);
        llBackout = (LinearLayout) findViewById(R.id.llBackout);
        txtDriverName = (TextView) findViewById(R.id.txtDriverName);
        txtPhoneNo = (TextView) findViewById(R.id.txtPhoneNo);
        rlBackoutReason = (RelativeLayout) findViewById(R.id.rlBackoutReason);

        rgBackout = (RadioGroup) findViewById(R.id.rgBackout);
        rbNoDocs = (RadioButton) findViewById(R.id.rbNoDocs);
        rbChangeMind = (RadioButton) findViewById(R.id.rbChangeMind);
        rbOther = (RadioButton) findViewById(R.id.rbOther);
        imgMore = (ImageView) findViewById(R.id.imgMore);

    }

    @Override
    public void onBackPressed() {
        if (backPressed) {
            PrefUtils.setKioskModeActive(true, getApplicationContext());
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            ActFollowUp.this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            ActFollowUp.this.finish();
        }
    }
}
