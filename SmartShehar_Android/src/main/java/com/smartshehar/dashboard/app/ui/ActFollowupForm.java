package com.smartshehar.dashboard.app.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.DataBaseHandler;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSApp;
import com.smartshehar.dashboard.app.SSLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.ui.CameraActivity;
import lib.app.util.ui.GalleryActivity;

public class ActFollowupForm extends AppCompatActivity implements
        TimePickerClass.OnCompleteListener,
        DatePickerClass.OnDateListener {
    public static final String ARG_ISSUE_ID = "issueid";
    private static String TAG = "ActFollowupForm";
    Connectivity mConnectivity;
    LinearLayout llComment, llWardAddr, llLetterDetails, llUploadLetter, llParent;
    TextView tvIssueName, tvIssueDate, tvIssueTime, tvIssueAdd, tvLetterDateTime,
            txtApprovalStaust, txtApprovalDateTime, tvLetterStatus, txtWardAddr,
            txtUploadLetterHead;
    RadioButton rbResolved, rbNoResolution, rbYes, rbNo;
    String mIssueid;
    ImageView imgLetter, imgCamera, imgGallery, imgCancelRed, ivIssuePhoto, imgIpComment, imgHelp,
            iv_step_submission, iv_step_four, iv_step_five, iv_step_six, iv_step_seven, ivHowdoesitwork,
            imgLetterHelp;
    boolean timeClassShow, dateClassShow;
    Bitmap bitmap;
    byte[] ba;
    static final int REQUEST_PHOTO = 1;
    private static final int RESULT_LOAD_IMG = 2;
    EditText etComment, etDept, etToWhom, etDesg,
            etSubmittedOn, etRemark, etCloseComment;
    String mLetterImageName, mImagePath, mLetterDateTime, mStatusDateTime, mClosedDateTime,
            mResolvedComment, mIssueImageName, mSubmittedBy, mAllIssueName,
            mDept, mToWhom, mDesg, mSubmittedOn, mRemark, mLetterDept, mLetterWhom, mLetterDesg, mLetterRemark,
            mSendToAuthority, mRejected, mRejectedReason,
            mSendToAuthorityDateTime, mOtherReasonCodeComment, mClosedComment, mLetterImagPath,
            mImgPath, mUniqueKey, mOffenceAddress, mOffenceTime, mOffenceType, strImg, mIssueTypeCode, mWard;
    DialogFragment dateFragment;
    DialogFragment timeFragment;
    Date date;
    SimpleDateFormat ft;
    boolean isResolved, isNoResolution, isYes, isNo, mLetterDetailsFlag;
    Button btnDone, btnLetterDone, btnCloseDone, btnHowdoesitwork, btnDiscard, btnSentMail;
    TextView txtIssueStatus, tvComment, txtEmailStatus, txtClosedStatus, txtClosedComment,tvProgressStatus,
            tvFComment, tvSComment, tvFCommentDate, tvSCommentDate, tvResolvedDate, tvClosed, txtClosedDate;
    public static boolean mResolvedUnresolvedFlag = false;
    String departmentCode;
    RelativeLayout rlProgress, rlStatus, issue_submission_status, rlLetterSubmission,rlCloseIssue,llApproval;
    DataBaseHandler db = null;
    boolean showLetterDetails, showWardOffice;
    Snackbar snackbar;
    private CoordinatorLayout coordinatorLayout;
    RadioGroup rgStatus, rgClosed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_followup_form);
        init();
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(ActFollowupForm.this)) {
            if (!mConnectivity.connectionError(ActFollowupForm.this)) {
                if (mConnectivity.isGPSEnable(ActFollowupForm.this)) {
                    Log.d(TAG, "Internet Connection");
                }
            }
        }
        dateFragment = new DatePickerClass();
        timeFragment = new TimePickerClass();
        db = new DataBaseHandler(ActFollowupForm.this);
        displayData();

        imgLetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(mLetterImageName) && !TextUtils.isEmpty(mLetterImagPath)) {
                    ActShowIssuePhoto.mImageName = mLetterImageName;
                    ActShowIssuePhoto.mImagePath = mLetterImagPath;
                    android.app.FragmentManager manager = getFragmentManager();
                    ActShowIssuePhoto newFragment = new ActShowIssuePhoto();
                    newFragment.show(manager, "viewImage");
                }


            }
        });
        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmap = null;

                mLetterDateTime = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT).format(new Date());
                Intent intent = new Intent(ActFollowupForm.this, CameraActivity.class);
                intent.putExtra("PHOTO_DIRECTORY", getString(R.string.appTitle));
                startActivityForResult(intent, REQUEST_PHOTO);


            }
        });
        imgGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLetterDateTime = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT).format(new Date());
                Intent intent = new Intent(ActFollowupForm.this, GalleryActivity.class);
                intent.putExtra("PHOTO_DIRECTORY", getString(R.string.appTitle));
                startActivityForResult(intent, RESULT_LOAD_IMG);

            }
        });

        imgCancelRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    DeleteAlertBox();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        rbResolved.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isResolved = true;
                    isNoResolution = false;
                    etComment.setText("");
                    llComment.setVisibility(View.VISIBLE);
                }
            }
        });
        rbNoResolution.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isResolved = false;
                    isNoResolution = true;
                    etComment.setText("");
                    llComment.setVisibility(View.VISIBLE);
                }
            }
        });
        rbYes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isNo = false;
                    isYes = true;
                    etCloseComment.setText("");
                    etCloseComment.setVisibility(View.VISIBLE);
                    btnCloseDone.setVisibility(View.VISIBLE);
                }
            }
        });
        rbNo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isYes = false;
                    isNo = true;
                    etCloseComment.setText("");
                    etCloseComment.setVisibility(View.VISIBLE);
                    btnCloseDone.setVisibility(View.VISIBLE);
                }
            }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResolvedComment = etComment.getText().toString();
                if (!TextUtils.isEmpty(mResolvedComment))
                    changeResolveStatus();
                else
                    showSnackbar("Please add a comment");
            }
        });
        btnCloseDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                mClosedComment = etCloseComment.getText().toString();
                if (!TextUtils.isEmpty(mClosedComment))
                    changeCloseStatus();
                else showSnackbar("Please add a comment");

            }
        });
        etSubmittedOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    if (!dateClassShow) {
                        timeClassShow = false;
                        dateFragment.show(getSupportFragmentManager(), "datePicker");
                        dateClassShow = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SSLog.e(TAG, "DATE PICKER ", e.toString());
                }


            }
        });
        etSubmittedOn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                return false;
            }
        });
        btnLetterDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                mDept = etDept.getText().toString();
                mToWhom = etToWhom.getText().toString();
                mDesg = etDesg.getText().toString();
                mRemark = etRemark.getText().toString();
                String letter_client_date_time = etSubmittedOn.getText().toString();
                if (!TextUtils.isEmpty(mDept) || !TextUtils.isEmpty(mToWhom) ||
                        !TextUtils.isEmpty(mDesg) || !TextUtils.isEmpty(mRemark))
                    if (!TextUtils.isEmpty(letter_client_date_time)) {
                        uploadLetterDetails();
                    } else {
                        showSnackbar("Please enter submitted date and time");
                    }
                else
                    showSnackbar("Please enter feilds");
            }
        });
        ivIssuePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(mIssueImageName)) {
                    ActShowIssuePhoto.mImageName = mIssueImageName;
                    ActShowIssuePhoto.mImagePath = mImgPath;
                    android.app.FragmentManager manager = getFragmentManager();
                    ActShowIssuePhoto newFragment = new ActShowIssuePhoto();
                    newFragment.show(manager, "viewImage");
                }
            }
        });
        imgIpComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mIssueid)) {
                    Intent intent = new Intent(ActFollowupForm.this, ActIpComments.class);
                    intent.putExtra("odi", mIssueid);
                    startActivity(intent);
                }
            }
        });

        ivHowdoesitwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!showWardOffice) {
                    showWardOffice = true;
                    getWardAddress();
                } else {
                    showWardOffice = false;
                    llWardAddr.setVisibility(View.GONE);
                }

            }
        });
        imgHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CGlobals_db.showInformationalDialog(ActFollowupForm.this, getString(R.string.beforeApproval));
            }
        });
        imgLetterHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!showLetterDetails) {
                    showLetterDetails = true;
                    llLetterDetails.setVisibility(View.VISIBLE);
                    btnLetterDone.setVisibility(View.GONE);
                    etDept.setInputType(InputType.TYPE_NULL);
                    etDesg.setInputType(InputType.TYPE_NULL);
                    etToWhom.setInputType(InputType.TYPE_NULL);
                    etRemark.setInputType(InputType.TYPE_NULL);
                    if (TextUtils.isEmpty(mLetterRemark))
                        etRemark.setVisibility(View.GONE);
                    etSubmittedOn.setVisibility(View.GONE);
                } else {
                    showLetterDetails = false;
                    llLetterDetails.setVisibility(View.GONE);
                }

            }
        });
        btnHowdoesitwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (departmentCode.equals("MU"))
                    CGlobals_db.showInformationalDialog(ActFollowupForm.this, getString(R.string.municipalwork));
                if (departmentCode.equals("TR")) {
                    CGlobals_db.showInformationalDialog(ActFollowupForm.this, getString(R.string.trafficwork));
                }
            }
        });

        btnDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discardAlertBox();
            }
        });
        btnSentMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Long lUniqueKey = Long.parseLong(mUniqueKey);
                    db.sentToserver(lUniqueKey, 1);
                    HashMap<String, String> hmp = new HashMap<>();
                    hmp.put("issueid", mIssueid);
                    hmp.put("issueimagefilename", mAllIssueName);
                    hmp.put("uniquekey", mUniqueKey);
                    hmp.put("issueaddress", mOffenceAddress);
                    hmp.put("issuedatetime", mOffenceTime);
                    hmp.put("submitreport", "1");
                    if (!TextUtils.isEmpty(mIssueTypeCode)) {
                        hmp.put("issueitemcode", mIssueTypeCode);
                    }
                    if (!TextUtils.isEmpty(mWard) || !mWard.equalsIgnoreCase("Select")) {
                        hmp.put("ward", mWard);
                    }
                    updateIssue(hmp, Constants_dp.ADD_ISSUE_URL);
                    sendEmailUrl(Constants_dp.ISSUE_EMAIL_URL, hmp);
                    customAlertDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showSnackbar(String msg) {
        snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);

        snackbar.show();
    }

    private void init() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        tvIssueName = (TextView) findViewById(R.id.tvIssueName);
        tvIssueTime = (TextView) findViewById(R.id.tvIssueTime);
        tvIssueDate = (TextView) findViewById(R.id.tvIssueDate);
        tvIssueAdd = (TextView) findViewById(R.id.tvIssueAdd);
        tvLetterStatus = (TextView) findViewById(R.id.tvLetterStatus);
        imgLetter = (ImageView) findViewById(R.id.imgLetter);
        imgCamera = (ImageView) findViewById(R.id.imgCamera);
        imgGallery = (ImageView) findViewById(R.id.imgGallery);
        imgCancelRed = (ImageView) findViewById(R.id.imgCancelRed);
        ivIssuePhoto = (ImageView) findViewById(R.id.ivIssuePhoto);
        rlStatus = (RelativeLayout) findViewById(R.id.rlStatus);
        issue_submission_status = (RelativeLayout) findViewById(R.id.issue_submission_status);
        rlLetterSubmission = (RelativeLayout) findViewById(R.id.rlLetterSubmission);
        rlCloseIssue = (RelativeLayout) findViewById(R.id.rlCloseIssue);
        rlProgress = (RelativeLayout) findViewById(R.id.rlProgress);
        rbResolved = (RadioButton) findViewById(R.id.rbResolved);
        rbNoResolution = (RadioButton) findViewById(R.id.rbNoResolution);
        rbYes = (RadioButton) findViewById(R.id.rbYes);
        rbNo = (RadioButton) findViewById(R.id.rbNo);
        btnDone = (Button) findViewById(R.id.btnSubmitIssue);
        iv_step_submission = (ImageView) findViewById(R.id.iv_step_submission);
        btnDiscard = (Button) findViewById(R.id.btnDiscard);
        btnSentMail = (Button) findViewById(R.id.btnSentMail);
        etComment = (EditText) findViewById(R.id.etComment);
        txtIssueStatus = (TextView) findViewById(R.id.txtIssueStatus);
        txtClosedStatus = (TextView) findViewById(R.id.txtClosedStatus);
        txtClosedComment = (TextView) findViewById(R.id.txtClosedComment);
        tvProgressStatus = (TextView) findViewById(R.id.tvProgressStatus);
        tvComment = (TextView) findViewById(R.id.tvComment);
        btnLetterDone = (Button) findViewById(R.id.btnLetterDone);
        etDept = (EditText) findViewById(R.id.etDept);
        etToWhom = (EditText) findViewById(R.id.etToWhom);
        etDesg = (EditText) findViewById(R.id.etDesg);
        etSubmittedOn = (EditText) findViewById(R.id.etSubmittedOn);
        etRemark = (EditText) findViewById(R.id.etRemark);
        tvLetterDateTime = (TextView) findViewById(R.id.tvLetterDateTime);
        txtUploadLetterHead = (TextView) findViewById(R.id.txtUploadLetterHead);
        txtEmailStatus = (TextView) findViewById(R.id.txtEmailStatus);
        txtIssueStatus = (TextView) findViewById(R.id.txtIssueStatus);
        txtApprovalStaust = (TextView) findViewById(R.id.txtApprovalStaust);
        txtApprovalDateTime = (TextView) findViewById(R.id.txtApprovalDateTime);
        btnCloseDone = (Button) findViewById(R.id.btnCloseDone);
        etCloseComment = (EditText) findViewById(R.id.etCloseComment);
        imgIpComment = (ImageView) findViewById(R.id.imgIpComment);
        tvFComment = (TextView) findViewById(R.id.tvFComment);
        tvSComment = (TextView) findViewById(R.id.tvSComment);
        imgHelp = (ImageView) findViewById(R.id.imgHelp);
        btnHowdoesitwork = (Button) findViewById(R.id.btnHowdoesitwork);
        iv_step_four = (ImageView) findViewById(R.id.iv_step_four);
        iv_step_five = (ImageView) findViewById(R.id.iv_step_five);
        iv_step_six = (ImageView) findViewById(R.id.iv_step_six);
        iv_step_seven = (ImageView) findViewById(R.id.iv_step_seven);
        ivHowdoesitwork = (ImageView) findViewById(R.id.ivHowdoesitwork);
        imgLetterHelp = (ImageView) findViewById(R.id.imgLetterHelp);
        txtWardAddr = (TextView) findViewById(R.id.txtWardAddr);
        tvFCommentDate = (TextView) findViewById(R.id.tvFCommentDate);
        tvSCommentDate = (TextView) findViewById(R.id.tvSCommentDate);
        tvResolvedDate = (TextView) findViewById(R.id.tvResolvedDate);
        tvClosed = (TextView) findViewById(R.id.tvClosed);
        txtClosedDate = (TextView) findViewById(R.id.txtClosedDate);
        llComment = (LinearLayout) findViewById(R.id.llComment);
        rgStatus = (RadioGroup) findViewById(R.id.rgStatus);
        rgClosed = (RadioGroup) findViewById(R.id.rgClosed);
        llWardAddr = (LinearLayout) findViewById(R.id.llWardAddr);
        llLetterDetails = (LinearLayout) findViewById(R.id.llLetterDetails);
        llUploadLetter = (LinearLayout) findViewById(R.id.llUploadLetter);
        llParent = (LinearLayout) findViewById(R.id.llParent);
        llApproval = (RelativeLayout) findViewById(R.id.llApproval);
    }

    private void displayData() {
        try {
            Intent getIntent = getIntent();
            Bundle extras = getIntent.getExtras();
            mIssueid = String.valueOf(extras.getInt(ARG_ISSUE_ID));
            if (!TextUtils.isEmpty(mIssueid) || !mIssueid.equalsIgnoreCase("0")) {
                datafromserver(mIssueid);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ER " + e.toString());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            imgLetter.setImageBitmap(null);
            ivIssuePhoto.setImageBitmap(null);
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

    private void datafromserver(final String issueid) {
        try {
            if (Connectivity.checkConnected(ActFollowupForm.this)) {
                final ProgressDialog pd = new ProgressDialog(ActFollowupForm.this);
                pd.setMessage("Please wait..."); //
                pd.show();
                final String url = Constants_dp.ISSUE_REPORT_URL;

                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                if (!response.trim().equals("-1")) {
                                    dataList(response);

                                } else {
                                    showSnackbar("List is Empty.");
                                }
                                if (pd.isShowing())
                                    pd.dismiss();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (pd.isShowing())
                            pd.dismiss();
                        showSnackbar(getString(R.string.connection_error));
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();

                        params.put("issueid", issueid);
                        params = CGlobals_db.getInstance(ActFollowupForm.this).getBasicMobileParams(params,
                                url, ActFollowupForm.this);

                        Log.d(TAG, "PARAM IS " + params);
                        return CGlobals_db.getInstance(ActFollowupForm.this).checkParams(params);
                    }
                };
                CGlobals_db.getInstance(ActFollowupForm.this).getRequestQueue(ActFollowupForm.this).add(postRequest);
            }
        } catch (Exception e) {

            SSLog.e(TAG, "ERROR IS ", e);
        }
    }

    private void dataList(String response) {
        String imgPath;
        int mSubmitIssue;
        ImageRequest request;
        JSONArray jarr;
        JSONObject jObj;
        if (TextUtils.isEmpty(response))
            return;
        try {

            jarr = new JSONArray(response);
            if (jarr.length() == 0)
                return;
            jObj = jarr.getJSONObject(0);
            departmentCode = jObj.isNull("department_code") ? "" : jObj.getString("department_code");
            mIssueid = jObj.isNull("issue_id") ? "" : jObj.getString("issue_id");
            mUniqueKey = jObj.isNull("unique_key") ? "" : jObj.getString("unique_key");
            mOffenceAddress = jObj.isNull("issue_address") ? "" : jObj.getString("issue_address");
            mOffenceTime = jObj.isNull("issue_time") ? "" : jObj.getString("issue_time");
            mIssueTypeCode = jObj.isNull("issue_item_code") ? "" : jObj.getString("issue_item_code");
            mWard = jObj.isNull("ward") ? "" : jObj.getString("ward");
            mOffenceType = jObj.isNull("issue_item_description") ? "" : jObj.getString("issue_item_description");
            mImgPath = jObj.isNull("issue_image_path") ? "" : jObj.getString("issue_image_path");
            mLetterImagPath = jObj.isNull("letter_image_path") ? "" : jObj.getString("letter_image_path");
            ActShowIssuePhoto.mImagePath = mImgPath;
            mSubmitIssue = Integer.parseInt(jObj.isNull("submit_report") ? "0" : jObj.getString("submit_report"));

            // set Issue name date and time //
            tvIssueName.setText(mOffenceType);
            ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT);
            date = ft.parse(mOffenceTime);
            ft = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
            tvIssueDate.setText(ft.format(date));
            ft = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            tvIssueTime.setText(ft.format(date));
            tvIssueAdd.setText(mOffenceAddress);
            // set Issue name date and time //
            // set Issue Image//
            mAllIssueName = jObj.isNull("issue_images") ? "" : jObj.getString("issue_images");
            if (!TextUtils.isEmpty(mAllIssueName) || !TextUtils.isEmpty(mImgPath)) {

                List<String> items = Arrays.asList(mAllIssueName.split(","));
                mAllIssueName = "";
                for (int i = 0; i < items.size(); i++) {
                    mIssueImageName = items.get(0);
                    mAllIssueName = mAllIssueName + ",images/" + items.get(i);
                }
                imgPath = CGlobals_lib_ss.getPath() + mImgPath + mIssueImageName;
                request = new ImageRequest(imgPath,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                ivIssuePhoto.setImageBitmap(bitmap);
                            }
                        }, 0, 0, null,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                            }
                        });
                SSApp.getInstance().addToRequestQueue(request);
            } else {
                ivIssuePhoto.setVisibility(View.GONE);
            }

            if (mSubmitIssue == 1) {
                iv_step_submission.setImageResource(R.drawable.ic_checked);
                txtEmailStatus.setText(getString(R.string.issue_submitted_successfully));
                issue_submission_status.setVisibility(View.VISIBLE);
            } else {
                btnDiscard.setVisibility(View.VISIBLE);
                btnSentMail.setVisibility(View.VISIBLE);
            }

            mSendToAuthority = jObj.isNull("sent_to_authority") ? "" : jObj.getString("sent_to_authority");
            mRejected = jObj.isNull("rejected") ? "" : jObj.getString("rejected");
            mRejectedReason = jObj.isNull("reject_reason") ? "" : jObj.getString("reject_reason");
            mSendToAuthorityDateTime = jObj.isNull("approved_rejected_clientdatetime") ? "" : jObj.getString("approved_rejected_clientdatetime");
            mOtherReasonCodeComment = jObj.isNull("other_reason_code_comment") ? "" : jObj.getString("other_reason_code_comment");
            if (departmentCode.equals("MU"))
                ivHowdoesitwork.setVisibility(View.VISIBLE);
            else
                ivHowdoesitwork.setVisibility(View.GONE);
            if (Integer.valueOf(mSendToAuthority) == 1 && Integer.valueOf(mRejected) == 0) {
                txtApprovalStaust.setText(getString(R.string.approved));
                llApproval.setVisibility(View.VISIBLE);
                if (departmentCode.equals("MU"))
                    rlLetterSubmission.setVisibility(View.VISIBLE);
                else
                    btnHowdoesitwork.setVisibility(View.VISIBLE);
                issue_submission_status.setVisibility(View.GONE);
            }

            if (Integer.valueOf(mSendToAuthority) == 0 && Integer.valueOf(mRejected) == 1) {
                llApproval.setVisibility(View.VISIBLE);
                String reason;
                if (TextUtils.isEmpty(mRejectedReason) || mRejectedReason.equalsIgnoreCase("Other"))
                    reason = mOtherReasonCodeComment;
                else
                    reason = mRejectedReason;
                txtApprovalStaust.setText(getString(R.string.rejected));
                if (!TextUtils.isEmpty(reason)) {
                    txtApprovalStaust.append(" because " + reason);
                }
                btnHowdoesitwork.setVisibility(View.GONE);
                issue_submission_status.setVisibility(View.GONE);
                ivHowdoesitwork.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(mSendToAuthorityDateTime)) {
                ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT);
                date = ft.parse(mSendToAuthorityDateTime);
                ft = new SimpleDateFormat("dd MMM, yyyy '  ' hh:mm a", Locale.getDefault());
                txtApprovalDateTime.setText(ft.format(date));
            }

            mLetterDept = jObj.isNull("letter_submitted_dept") ? "" : jObj.getString("letter_submitted_dept");
            mLetterWhom = jObj.isNull("letter_submitted_to") ? "" : jObj.getString("letter_submitted_to");
            mLetterDesg = jObj.isNull("letter_submitted_desg") ? "" : jObj.getString("letter_submitted_desg");
            mLetterRemark = jObj.isNull("letter_remark") ? "" : jObj.getString("letter_remark");
            mLetterImageName = jObj.isNull("letter_image_name") ? "" : jObj.getString("letter_image_name");
            mLetterDateTime = jObj.isNull("letter_submitted_datetime") ? "" : jObj.getString("letter_submitted_datetime");
            mSubmittedBy = jObj.isNull("letter_submit_by") ? "" : jObj.getString("letter_submit_by");
            int letter_submited = Integer.parseInt(jObj.isNull("letter_upload_notification") ? "0" : jObj.getString("letter_upload_notification"));
            if (letter_submited == 1 && !TextUtils.isEmpty(mLetterImagPath)) {
                llLetterDetails.setVisibility(View.GONE);
                imgPath = CGlobals_lib_ss.getPath() + mLetterImagPath + mLetterImageName;
                request = new ImageRequest(imgPath,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                imgLetter.setImageBitmap(bitmap);
                            }
                        }, 0, 0, null,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                            }
                        });
                SSApp.getInstance().addToRequestQueue(request);
                txtUploadLetterHead.setVisibility(View.GONE);
                imgLetter.setVisibility(View.VISIBLE);
                tvLetterStatus.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(mSubmittedBy)) {
                    tvLetterStatus.append(" by " + mSubmittedBy);
                }
                ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT);
                date = ft.parse(mLetterDateTime);
                ft = new SimpleDateFormat("dd MMM, yyyy '  ' hh:mm a", Locale.getDefault());
                tvLetterDateTime.setText(ft.format(date));
                iv_step_four.setImageResource(R.drawable.ic_checked);
                imgCancelRed.setVisibility(View.GONE);
                tvLetterDateTime.setVisibility(View.VISIBLE);
                rlStatus.setVisibility(View.VISIBLE);
                rlProgress.setVisibility(View.VISIBLE);
                imgCamera.setVisibility(View.GONE);
                imgGallery.setVisibility(View.GONE);
                imgLetterHelp.setVisibility(View.VISIBLE);
                etDept.setText(mLetterDept);
                etDesg.setText(mLetterDesg);
                etToWhom.setText(mLetterWhom);
                etRemark.setText(mLetterRemark);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    llUploadLetter.setBackground(null);
            } else {
                tvLetterDateTime.setVisibility(View.GONE);
                imgLetter.setVisibility(View.GONE);
                imgCancelRed.setVisibility(View.GONE);
                imgCamera.setVisibility(View.VISIBLE);
                imgGallery.setVisibility(View.VISIBLE);
                tvLetterStatus.setVisibility(View.GONE);
                rlStatus.setVisibility(View.GONE);
                rlProgress.setVisibility(View.GONE);

            }
            int resolved = Integer.parseInt(jObj.isNull("resolved") ? "0" : jObj.getString("resolved"));
            int unresolved = Integer.parseInt(jObj.isNull("unresolved") ? "0" : jObj.getString("unresolved"));
            mSubmittedBy = jObj.isNull("resolved_unresolved_by") ? "" : jObj.getString("resolved_unresolved_by");
            mStatusDateTime = jObj.isNull("resolved_unresolved_datetime") ? "" : jObj.getString("resolved_unresolved_datetime");
            mResolvedComment = jObj.isNull("unresolved_comment") ? "" : jObj.getString("unresolved_comment");

            if (resolved == 1 || unresolved == 1) {
                txtIssueStatus.setText(getString(R.string.resolved_status));
                txtIssueStatus.setVisibility(View.VISIBLE);
                tvClosed.setVisibility(View.VISIBLE);
                rgClosed.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(mSubmittedBy)) {
                    txtIssueStatus.append(" by " + mSubmittedBy);
                    tvClosed.append(" as marked by " + mSubmittedBy + " ?");
                } else {
                    tvClosed.append("?");
                }
                tvComment.setText("Comment: ");
                tvComment.append(mResolvedComment);
                rgStatus.setVisibility(View.GONE);
                tvComment.setVisibility(View.VISIBLE);
                rlCloseIssue.setVisibility(View.VISIBLE);
                tvProgressStatus.setVisibility(View.GONE);
                mResolvedUnresolvedFlag = true;
                iv_step_six.setImageResource(R.drawable.ic_checked);
                etComment.setVisibility(View.GONE);
                btnDone.setVisibility(View.GONE);

            }
            if (!TextUtils.isEmpty(mStatusDateTime)) {
                ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT);
                date = ft.parse(mStatusDateTime);
                ft = new SimpleDateFormat("dd MMM, yyyy   hh:mm a", Locale.getDefault());
                tvResolvedDate.setText(ft.format(date));
                tvResolvedDate.setVisibility(View.VISIBLE);
            }

            int closed = Integer.parseInt(jObj.isNull("closed") ? "0" : jObj.getString("closed"));
            int opened = Integer.parseInt(jObj.isNull("opened") ? "0" : jObj.getString("opened"));
            mClosedDateTime = jObj.isNull("closed_opened_datetime") ? "" : jObj.getString("closed_opened_datetime");
            mClosedComment = jObj.isNull("closed_opened_comment") ? "" : jObj.getString("closed_opened_comment");
            mSubmittedBy = jObj.isNull("closed_opened_by") ? "" : jObj.getString("closed_opened_by");
            if (closed == 1 || opened == 1) {
                ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT);
                date = ft.parse(mClosedDateTime);
                ft = new SimpleDateFormat("dd MMM, yyyy    HH:mm", Locale.getDefault());
                txtClosedDate.setText(ft.format(date));
                txtClosedComment.setText("Comment: ");
                txtClosedComment.append(mClosedComment);
                txtClosedDate.setVisibility(View.VISIBLE);
                txtClosedComment.setVisibility(View.VISIBLE);
                rgClosed.setVisibility(View.GONE);
                btnCloseDone.setVisibility(View.GONE);
                etCloseComment.setVisibility(View.GONE);
                iv_step_seven.setImageResource(R.drawable.ic_checked);
                txtClosedStatus.setVisibility(View.VISIBLE);
            }
            if (closed == 1) {
                txtClosedStatus.setText("Closed");
            } else {
                txtClosedStatus.setText("Issue is not yet closed.");
            }

            llParent.setVisibility(View.VISIBLE);
            getIntermediateProgress();
        } catch (Exception e1) {
            e1.printStackTrace();
            SSLog.e(TAG, "dataList", e1);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        bitmap = null;

        if (requestCode == REQUEST_PHOTO) {

            if (resultCode == Activity.RESULT_OK && data != null) {
                Bundle extra = data.getBundleExtra("data");
                mLetterImageName = extra.getString("imageName");
                mImagePath = extra.getString("imageFilePath");
                ba = extra.getByteArray("imageByte");
                strImg = Base64.encodeToString(ba, Base64.DEFAULT);

                setPic();
            } else {
                showSnackbar("No picture returned by the camera. Is your memory full?");
            }
        }

        if (requestCode == RESULT_LOAD_IMG) {
            try {
                if (resultCode == RESULT_OK && data != null) {
                    Bundle extra = data.getBundleExtra("data");
                    mLetterImageName = extra.getString("imageName");
                    mImagePath = extra.getString("imageFilePath");
                    ba = extra.getByteArray("imageByte");
                    strImg = Base64.encodeToString(ba, Base64.DEFAULT);

                    setPic();
                } else {
                    showSnackbar("No picture returned by the gallery. Is your memory full?");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Set" + e.toString());

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setPic() {
        try {

            bitmap = null;
            bitmap = BitmapFactory.decodeByteArray(ba, 0, ba.length);
            txtUploadLetterHead.setVisibility(View.GONE);
            imgLetter.setImageBitmap(bitmap);
            imgLetter.setVisibility(View.VISIBLE);
            imgCancelRed.setVisibility(View.VISIBLE);
            sendImage(strImg);

        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG, "setPic: ", e);
        }
    }

    private void sendImage(final String strImg) {
        try {
            final ProgressDialog pd = new ProgressDialog(ActFollowupForm.this);
            pd.setMessage("Please wait..."); //
            pd.show();
            final String url = Constants_dp.ADD_LETTERHEAD_DETAILS_URL;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (!response.trim().equals("-1")) {
                                try {

                                    ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT);
                                    date = ft.parse(mLetterDateTime);
                                    ft = new SimpleDateFormat("dd MMM, yyyy 'at' HH:mm", Locale.getDefault());
                                    llLetterDetails.setVisibility(View.VISIBLE);
                                    mLetterDetailsFlag = true;
                                    imgLetter.setVisibility(View.VISIBLE);

                                    Log.d(TAG, "image sent successfully");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    SSLog.e(TAG, "sendImage", e);
                                }
                                if (pd.isShowing())
                                    pd.dismiss();
                                showSnackbar("Sent successfully");
                            } else {
                                mLetterDetailsFlag = false;
                                imgLetter.setVisibility(View.GONE);
                                txtUploadLetterHead.setVisibility(View.VISIBLE);
                                imgCancelRed.setVisibility(View.GONE);
                                imgLetter.setImageResource(0);
                                showSnackbar("Please try once");
                            }
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (pd.isShowing())
                        pd.dismiss();
                    try {
                        ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT);
                        date = ft.parse(mLetterDateTime);
                        ft = new SimpleDateFormat("dd MMM, yyyy 'at' HH:mm", Locale.getDefault());
                        llLetterDetails.setVisibility(View.VISIBLE);
                        mLetterDetailsFlag = true;
                        showSnackbar("Sent successfully");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    SSLog.e(TAG, "sendImage", error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params.put("letterimagename", mLetterImageName);
                    params.put("letterdatetime", mLetterDateTime);
                    params.put("issueid", String.valueOf(mIssueid));
                    params.put("letterimage", strImg);
                    params = CGlobals_db.getInstance(ActFollowupForm.this).getBasicMobileParams(params,
                            url, ActFollowupForm.this);

                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_db.getInstance(ActFollowupForm.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(ActFollowupForm.this).getRequestQueue(ActFollowupForm.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteLetterHead() {
        try {
            final ProgressDialog pd = new ProgressDialog(ActFollowupForm.this);
            pd.setMessage("Please wait..."); //
            pd.show();
            final String url = Constants_dp.DELETE_LETTERHEAD_INFO_URL;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                imgLetter.setVisibility(View.GONE);
                                imgCancelRed.setVisibility(View.GONE);
                                txtUploadLetterHead.setVisibility(View.VISIBLE);
                                tvLetterDateTime.setVisibility(View.GONE);
                            } else {
                                showSnackbar("Please try once");
                            }
                            if (pd.isShowing())
                                pd.dismiss();
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (pd.isShowing())
                        pd.dismiss();
                    SSLog.e(TAG, "deleteLetterHead", error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("issueid", String.valueOf(mIssueid));
                    params = CGlobals_db.getInstance(ActFollowupForm.this).getBasicMobileParams(params,
                            url, ActFollowupForm.this);
                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_db.getInstance(ActFollowupForm.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(ActFollowupForm.this).getRequestQueue(ActFollowupForm.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeResolveStatus() {
        try {
            final ProgressDialog pd = new ProgressDialog(ActFollowupForm.this);
            pd.setMessage("Please wait..."); //
            pd.show();
            final String url = Constants_dp.UPDATE_RESOLVED_UNRESOLVED_URL;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                if (isResolved)
                                    sendMail();

                                Intent intent = getIntent();
                                overridePendingTransition(0, 0);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(intent);

                            } else {
                                etComment.setText("");
                                llComment.setVisibility(View.VISIBLE);
                                rgStatus.setVisibility(View.VISIBLE);
                                showSnackbar("Please try once");
                            }
                            if (pd.isShowing())
                                pd.dismiss();
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (pd.isShowing())
                        pd.dismiss();
                    rlStatus.setVisibility(View.VISIBLE);
                    rlProgress.setVisibility(View.GONE);//////////////////////////////////
                    rgStatus.setVisibility(View.VISIBLE);

                    SSLog.e(TAG, "changeResolveStatus", error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    int i;
                    params.put("issueid", String.valueOf(mIssueid));
                    i = (isResolved) ? 1 : 0;
                    params.put("resolved", String.valueOf(i));
                    i = (isNoResolution) ? 1 : 0;
                    params.put("unresolved", String.valueOf(i));
                    if (!TextUtils.isEmpty(mResolvedComment))
                        params.put("unresolvedcomment", mResolvedComment);
                    params = CGlobals_db.getInstance(ActFollowupForm.this).getBasicMobileParams(params,
                            url, ActFollowupForm.this);
                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_db.getInstance(ActFollowupForm.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(ActFollowupForm.this).getRequestQueue(ActFollowupForm.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeCloseStatus() {
        try {
            final ProgressDialog pd = new ProgressDialog(ActFollowupForm.this);
            pd.setMessage("Please wait..."); //
            pd.show();
            final String url = Constants_dp.UPDATE_CLOSED_OPENED_URL;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {


                                if (isYes)
                                    sendClosedMail();
                                Intent intent = getIntent();
                                overridePendingTransition(0, 0);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(intent);

                            } else {
                                etCloseComment.setText("");
                                etCloseComment.setVisibility(View.VISIBLE);
                                btnCloseDone.setVisibility(View.VISIBLE);
                                rgClosed.setVisibility(View.VISIBLE);
                                tvClosed.setVisibility(View.VISIBLE);
                                showSnackbar("Please try once");
                            }
                            if (pd.isShowing())
                                pd.dismiss();
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (pd.isShowing())
                        pd.dismiss();
                    etCloseComment.setVisibility(View.VISIBLE);
                    btnCloseDone.setVisibility(View.VISIBLE);
                    rgClosed.setVisibility(View.VISIBLE);
                    tvClosed.setVisibility(View.VISIBLE);

                    SSLog.e(TAG, "changeCloseStatus", error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    int i;
                    params.put("issueid", String.valueOf(mIssueid));
                    i = (isYes) ? 1 : 0;
                    params.put("closed", String.valueOf(i));
                    i = (isNo) ? 1 : 0;
                    params.put("opened", String.valueOf(i));
                    if (!TextUtils.isEmpty(mClosedComment))
                        params.put("closedopenedcomment", mClosedComment);
                    params = CGlobals_db.getInstance(ActFollowupForm.this).getBasicMobileParams(params,
                            url, ActFollowupForm.this);
                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_db.getInstance(ActFollowupForm.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(ActFollowupForm.this).getRequestQueue(ActFollowupForm.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendMail() {
        try {
            final String url = Constants_dp.RESOLVED_ISSUE_EMAIL_URL;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                Log.d(TAG, "Mail is sent");
                            } else {
                                showSnackbar("Mail not sent");
                            }

                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    SSLog.e(TAG, "sendMail", error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params.put("issueid", String.valueOf(mIssueid));
                    if (!TextUtils.isEmpty(mAllIssueName)) {
                        params.put("offenceimagefilename", mAllIssueName);
                    }
                    params = CGlobals_db.getInstance(ActFollowupForm.this).getBasicMobileParams(params,
                            url, ActFollowupForm.this);
                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_db.getInstance(ActFollowupForm.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(ActFollowupForm.this).getRequestQueue(ActFollowupForm.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendClosedMail() {
        try {
            final String url = Constants_dp.ISSUE_CLOSED_EMAIL_URL;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                Log.d(TAG, "Mail is sent");
                            } else {
                                showSnackbar("Mail not sent");
                            }

                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    SSLog.e(TAG, "sendClosedMail", error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params.put("issueid", String.valueOf(mIssueid));
                    if (!TextUtils.isEmpty(mAllIssueName)) {
                        params.put("offenceimagefilename", mAllIssueName);
                    }
                    params = CGlobals_db.getInstance(ActFollowupForm.this).getBasicMobileParams(params,
                            url, ActFollowupForm.this);
                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_db.getInstance(ActFollowupForm.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(ActFollowupForm.this).getRequestQueue(ActFollowupForm.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void DeleteAlertBox() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActFollowupForm.this);
        alertDialogBuilder.setMessage(R.string.delete_warning);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        mLetterDetailsFlag = false;
                        llLetterDetails.setVisibility(View.GONE);
                        deleteLetterHead();
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

    private void uploadLetterDetails() {
        try {
            final ProgressDialog pd = new ProgressDialog(ActFollowupForm.this);
            pd.setMessage("Please wait..."); //
            pd.show();
            final String url = Constants_dp.UPDATE_LETTER_DETAILS_URL;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {

                                Intent intent = getIntent();
                                overridePendingTransition(0, 0);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(intent);
                            }
                            if (pd.isShowing())
                                pd.dismiss();
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (pd.isShowing())
                        pd.dismiss();


                    SSLog.e(TAG, "uploadLetterDetails", error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("issueid", String.valueOf(mIssueid));
                    params.put("lettersubmittedto", mToWhom);
                    params.put("lettersubmitteddept", mDept);
                    params.put("lettersubmitteddesg", mDesg);
                    params.put("letterremark", mRemark);
                    params.put("clientdatetime", mSubmittedOn);
                    params = CGlobals_db.getInstance(ActFollowupForm.this).getBasicMobileParams(params,
                            url, ActFollowupForm.this);
                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_db.getInstance(ActFollowupForm.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(ActFollowupForm.this).getRequestQueue(ActFollowupForm.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void gotTimeResult(String sTime) {
        try {
            mSubmittedOn = mSubmittedOn + " " + sTime + ":00";
            ft = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            date = ft.parse(mSubmittedOn);
            ft = new SimpleDateFormat("dd MMM, yyyy 'at' HH:mm", Locale.getDefault());
            etSubmittedOn.setText(ft.format(date));
            dateClassShow = false;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void gotDateResult(String sDate) {
        try {
            if (TextUtils.isEmpty(sDate))
                return;
            mSubmittedOn = sDate;
            etSubmittedOn.setText(mSubmittedOn);
            if (!timeClassShow) {
                timeFragment.show(getSupportFragmentManager(), "timePicker");
                timeClassShow = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG, "gotDateResult", e.toString());
        }
    }

    @Override
    public void onBackPressed() {

        if (mLetterDetailsFlag) {
            lostDataAlertBox();
        } else {
            super.onBackPressed();
        }
    }

    private void lostDataAlertBox() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActFollowupForm.this);
        alertDialogBuilder.setMessage(R.string.lose_info);
        alertDialogBuilder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        ActFollowupForm.this.finish();
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void discardAlertBox() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActFollowupForm.this);
        alertDialogBuilder.setMessage(R.string.discard_warning);
        alertDialogBuilder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Long lUniqueKey = Long.parseLong(mUniqueKey);
                        db.sentToserver(lUniqueKey, 1);
                        HashMap<String, String> hmp = new HashMap<>();
                        hmp.put("uniquekey", mUniqueKey);
                        hmp.put("issueaddress", mOffenceAddress);
                        hmp.put("issuedatetime", mOffenceTime);
                        hmp.put("submitreport", "0");
                        hmp.put("discard_report", String.valueOf("1"));
                        if (!TextUtils.isEmpty(mIssueTypeCode)) {
                            hmp.put("issueitemcode", mIssueTypeCode);
                        }
                        if (!TextUtils.isEmpty(mWard) || !mWard.equalsIgnoreCase("Select")) {
                            hmp.put("ward", mWard);
                        }

                        updateIssue(hmp, Constants_dp.ADD_ISSUE_URL);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_followup_form, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refreshFollowupForm:
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getIntermediateProgress() {

        try {
            if (Connectivity.checkConnected(ActFollowupForm.this)) {
                final String url = Constants_dp.GET__INTERMEDIATE_PROGRESS_URL;

                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                if (!response.trim().equals("-1")) {
                                    parseComments(response);
                                    Log.d(TAG, "" + response);
                                } else {
                                    showSnackbar("List is empty");
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showSnackbar(getString(R.string.connection_error));
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        if (!TextUtils.isEmpty(mIssueid))
                            params.put("odi", mIssueid);
                        params = CGlobals_db.getInstance(ActFollowupForm.this).getBasicMobileParams(params,
                                url, ActFollowupForm.this);

                        Log.d(TAG, "PARAM IS " + params);
                        return CGlobals_db.getInstance(ActFollowupForm.this).checkParams(params);
                    }
                };
                CGlobals_db.getInstance(ActFollowupForm.this).getRequestQueue(ActFollowupForm.this).add(postRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());

        }
    }

    private void getWardAddress() {

        try {
            final ProgressDialog pd = new ProgressDialog(ActFollowupForm.this);
            pd.setMessage("Please wait..."); //
            pd.show();
            if (Connectivity.checkConnected(ActFollowupForm.this)) {
                final String url = Constants_dp.GET__WARD_ADDRESS_URL;

                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                if (!response.trim().equals("-1")) {

                                    txtWardAddr.setText(response);
                                    llWardAddr.setVisibility(View.VISIBLE);
                                } else {
                                    showSnackbar("List is empty");
                                }
                                if (pd.isShowing())
                                    pd.dismiss();

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (pd.isShowing())
                            pd.dismiss();
                        showSnackbar(getString(R.string.connection_error));
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        if (!TextUtils.isEmpty(mWard))
                            params.put("ward", mWard);
                        params = CGlobals_db.getInstance(ActFollowupForm.this).getBasicMobileParams(params,
                                url, ActFollowupForm.this);

                        Log.d(TAG, "PARAM IS " + params);
                        return CGlobals_db.getInstance(ActFollowupForm.this).checkParams(params);
                    }
                };
                CGlobals_db.getInstance(ActFollowupForm.this).getRequestQueue(ActFollowupForm.this).add(postRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());

        }
    }

    private void parseComments(String response) {
        JSONObject jObj;
        JSONArray jArr;
        String mComment, mDate, mText;
        try {
            jArr = new JSONArray(response);
            for (int i = 0; i < jArr.length(); i++) {
                jObj = jArr.getJSONObject(i);
                mComment = jObj.isNull("intermediate_progress_comment") ? "" :
                        jObj.getString("intermediate_progress_comment");
                mDate = jObj.isNull("intermediate_progress_clientdatetime") ? "" :
                        jObj.getString("intermediate_progress_clientdatetime");
                ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT);
                date = ft.parse(mDate);
                ft = new SimpleDateFormat("dd MMM, yyyy   hh:mm a", Locale.getDefault());
                mText = mComment;

                if (i == 0) {
                    tvFComment.setText("1. ");
                    tvFComment.append(mText);
                    tvFComment.setVisibility(View.VISIBLE);
                    tvFCommentDate.setText(ft.format(date));
                    tvFCommentDate.setVisibility(View.VISIBLE);
                }
                if (i == 1) {
                    tvSComment.setText("2. ");
                    tvSComment.append(mText);
                    tvSComment.setVisibility(View.VISIBLE);
                    tvSCommentDate.setText(ft.format(date));
                    tvSCommentDate.setVisibility(View.VISIBLE);
                }

            }
            iv_step_five.setImageResource(R.drawable.ic_checked);

        } catch (Exception e) {
            SSLog.e(TAG, "parseComments", e.toString());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(mIssueid))
            getIntermediateProgress();
    }

    private void updateIssue(final HashMap<String, String> hmp, final String url) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (!response.trim().equals("-1") || !TextUtils.isEmpty(response)) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String issueId = jsonObject.getString("issue_id");
                                if (TextUtils.isEmpty(issueId)) {
                                    db.sentToserver(Long.parseLong(mUniqueKey), 0);
                                    showSnackbar("Poor Network Connection");
                                } else {
                                    db.sentToserver(Long.parseLong(mUniqueKey), 1);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            showSnackbar("Poor Network Connection");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, url + " Error: " + error.getMessage());
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
                params = CGlobals_db.getInstance(ActFollowupForm.this).getBasicMobileParams(params,
                        url, ActFollowupForm.this);
                Log.d(TAG, "PARAM IS " + params);
                return CGlobals_db.getInstance(ActFollowupForm.this).checkParams(params);
            }
        };
        CGlobals_db.getInstance(ActFollowupForm.this).getRequestQueue(ActFollowupForm.this).add(postRequest);
    }

    private void sendEmailUrl(final String url, final HashMap<String, String> hmp) {

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response.trim();
                        if (!response.trim().equals("-1")) {
                            Log.d(TAG, "email sent");

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                db.sentToserver(Long.parseLong(mUniqueKey), 0);
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
                params = CGlobals_db.getInstance(ActFollowupForm.this).getBasicMobileParams(params,
                        url, ActFollowupForm.this);
                Log.d(TAG, "PARAM IS " + params);
                return CGlobals_db.getInstance(ActFollowupForm.this).checkParams(params);
            }
        };
        CGlobals_db.getInstance(ActFollowupForm.this).getRequestQueue(ActFollowupForm.this).add(postRequest);
    }

    private void customAlertDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Thank you! Your complaint will be submitted to authorities after our team reviews it. You can check the status of your complaints by visiting \"My Complaints\" from the dashboard.");
        alertDialogBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        try {


                            Intent intent = getIntent();
                            overridePendingTransition(0, 0);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
}
