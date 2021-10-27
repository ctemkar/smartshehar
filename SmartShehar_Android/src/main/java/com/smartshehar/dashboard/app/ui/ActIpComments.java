package com.smartshehar.dashboard.app.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.CIpComments;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.IpCommentAdapter;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.ui.CameraActivity;

public class ActIpComments extends AppCompatActivity {
    Connectivity mConnectivity;
    private static String TAG = "ActIpComments";
    String mOffencDetailId;
    RecyclerView rvIpComment;
    IpCommentAdapter mAdapter;
    ArrayList<CIpComments> mlist;
    Date date;
    SimpleDateFormat ft;
    String mProgressComment;
    EditText etProgressComment;
    Button btnProgressDone;
    ImageView iv_camera, iv_picture;
    Bitmap bitmap;
    static final int REQUEST_PHOTO = 1;
    byte[] baOne;
    String imageName;
    String mCurrentPhotoPath;
    String ba1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_ip_comments);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        iv_camera = (ImageView) findViewById(R.id.iv_camera);
        iv_picture = (ImageView) findViewById(R.id.iv_picture);
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(ActIpComments.this)) {
            if (!mConnectivity.connectionError(ActIpComments.this)) {
                if (mConnectivity.isGPSEnable(ActIpComments.this)) {
                    Log.d(TAG, "Internet Connection");
                }
            }
        }
        etProgressComment = (EditText) findViewById(R.id.etProgressComment);
        btnProgressDone = (Button) findViewById(R.id.btnProgressDone);
        rvIpComment = (RecyclerView) findViewById(R.id.rvIpComment);
        rvIpComment.setLayoutManager(new LinearLayoutManager(this));
        mlist = new ArrayList<>();
        if (getIntent() != null)
            mOffencDetailId = getIntent().getStringExtra("odi");
        if (!TextUtils.isEmpty(mOffencDetailId))
            getIntermediateProgress();
        if (ActFollowupForm.mResolvedUnresolvedFlag) {
            findViewById(R.id.rlParent).setVisibility(View.GONE);
            findViewById(R.id.v_seperator).setVisibility(View.GONE);
        }
        btnProgressDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                mProgressComment = etProgressComment.getText().toString();
                if (!TextUtils.isEmpty(mProgressComment) && !TextUtils.isEmpty(mOffencDetailId))
                    sendIntermediateProgress();
                else
                    Toast.makeText(ActIpComments.this, "Please add a comment", Toast.LENGTH_LONG).show();
            }
        });
        iv_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = null;
                openCamera();
            }
        });
        iv_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = null;
                openCamera();
            }
        });
    }
private void openCamera()
{
    Intent intent = new Intent(ActIpComments.this, CameraActivity.class);
    intent.putExtra("PHOTO_DIRECTORY", getString(R.string.appTitle));
    startActivityForResult(intent, REQUEST_PHOTO);
}
    private void sendIntermediateProgress() {
        try {
            final ProgressDialog pd = new ProgressDialog(ActIpComments.this);
            pd.setMessage("Please wait..."); //
            pd.show();
            final String url = Constants_dp.ADD_INTERMEDIATE_PROGRESS_URL;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {

                                etProgressComment.setText("");
                                iv_picture.setImageResource(R.drawable.ic_municipal_issue);


                                imageName = "";
                                ba1 = "";
                                mCurrentPhotoPath = "";
                                getIntermediateProgress();
                            } else {
                                Toast.makeText(ActIpComments.this, "Please try again later", Toast.LENGTH_LONG).show();
                            }
                            if (pd.isShowing())
                                pd.dismiss();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (pd.isShowing())
                        pd.dismiss();

                    SSLog.e(TAG, "sendIntermediateProgress", error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params.put("issueid", String.valueOf(mOffencDetailId));

                    if (!TextUtils.isEmpty(mProgressComment))
                        params.put("ipc", mProgressComment);
                    if (!TextUtils.isEmpty(ba1)) {
                        params.put("ip_image_name", imageName);
                        params.put("image", ba1);
                    }
                    params = CGlobals_db.getInstance(ActIpComments.this).getBasicMobileParams(params,
                            url, ActIpComments.this);
                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_db.getInstance(ActIpComments.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(ActIpComments.this).getRequestQueue(ActIpComments.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getIntermediateProgress() {
        try {
            if (Connectivity.checkConnected(ActIpComments.this)) {
                final ProgressDialog pd = new ProgressDialog(ActIpComments.this);
                pd.setMessage("Please wait..."); //
                pd.show();
                final String url = Constants_dp.GET__INTERMEDIATE_PROGRESS_URL;

                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                if (!response.trim().equals("-1")) {
                                    parseComments(response);
                                    Log.d(TAG, "" + response);
                                } else {
                                    Toast.makeText(ActIpComments.this, "Empty list", Toast.LENGTH_LONG).show();

                                }
                                if (pd.isShowing())
                                    pd.dismiss();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (pd.isShowing())
                            pd.dismiss();
                        Toast.makeText(ActIpComments.this, getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        if (!TextUtils.isEmpty(mOffencDetailId))
                            params.put("odi", mOffencDetailId);
                        params = CGlobals_db.getInstance(ActIpComments.this).getBasicMobileParams(params,
                                url, ActIpComments.this);

                        Log.d(TAG, "PARAM IS " + params);
                        return CGlobals_db.getInstance(ActIpComments.this).checkParams(params);
                    }
                };
                CGlobals_db.getInstance(ActIpComments.this).getRequestQueue(ActIpComments.this).add(postRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());

        }
    }

    private void parseComments(String response) {
        if (mlist.size() > 0)
            mlist.clear();
        JSONObject jObj;
        JSONArray jArr;
        String mComment, mDate, mImagePath, mImageName;
        CIpComments mCIpComments;
        try {
            jArr = new JSONArray(response);
            for (int i = 0; i < jArr.length(); i++) {
                jObj = jArr.getJSONObject(i);
                mComment = jObj.isNull("intermediate_progress_comment") ? "" :
                        jObj.getString("intermediate_progress_comment");
                mDate = jObj.isNull("intermediate_progress_clientdatetime") ? "" :
                        jObj.getString("intermediate_progress_clientdatetime");
                mImagePath = jObj.isNull("ip_image_path") ? "" :
                        jObj.getString("ip_image_path");
                mImageName = jObj.isNull("ip_image_name") ? "" :
                        jObj.getString("ip_image_name");
                ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT);
                date = ft.parse(mDate);
                ft = new SimpleDateFormat("dd MMM, yyyy   hh:mm a", Locale.getDefault());
                mCIpComments = new CIpComments(mComment, ft.format(date), "", "", "", mImagePath,mImageName);
                mlist.add(mCIpComments);
            }
            android.app.FragmentManager manager = getFragmentManager();
            mAdapter = new IpCommentAdapter(ActIpComments.this, mlist,manager);
            rvIpComment.setAdapter(mAdapter);
        } catch (Exception e) {
            SSLog.e(TAG, "parseComments", e.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        bitmap = null;
        if (requestCode == REQUEST_PHOTO) {

            if (resultCode == Activity.RESULT_OK && data != null) {
                Bundle extra = data.getBundleExtra("data");

                baOne = extra.getByteArray("imageByte");
                imageName = extra.getString("imageName");
                mCurrentPhotoPath = extra.getString("imageFilePath");
                ba1 = Base64.encodeToString(baOne, Base64.DEFAULT);

                setPic();
            } else {
                //showSnackbar("No picture returned by the camera. Is your memory full?");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setPic() {
        try {
            bitmap = BitmapFactory.decodeByteArray(baOne, 0, baOne.length);


            iv_picture.setImageBitmap(bitmap);
            bitmap = null;
            //  sendIssueImage();
        } catch (Exception e) {
            Log.d(TAG, "setPic: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            iv_picture.setImageBitmap(null);

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
}
