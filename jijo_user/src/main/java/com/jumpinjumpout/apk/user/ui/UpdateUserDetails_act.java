package com.jumpinjumpout.apk.user.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UpdateUserDetails_act extends Activity {

    public RequestQueue mnVolleyRequestQueue; // initialize volley
    EditText user_name, full_name, age; // initialize Edit Text
    Spinner gender; // Spinner initialize
    Button nextto; // Button initialize
    LinearLayout lin1;
    FrameLayout framered;
    private static final String[] selectGender = {"GENDER", "MALE", "FEMALE"}; // Spinner
    private static String TAG = UpdateUserDetails_act.class.getSimpleName(); // initialize
    String getGender, userName, fullName, userAge;
    TextView textView1, textViewvisible;
    ProgressDialog mProgressDialog;
    ImageView mIvMeImage, mIvGallery, mIvShowImage;
    static final int REQUEST_PHOTO = 1;
    static final int RESULT_LOAD_IMG = 2;
    String mCurrentPhotoPath;
    String imageFileName;
    String strImag;
    Bitmap bitmap = null, resizedBitmap = null;
    private String user_Name;
    private String user_Gender;
    private String user_Full_Name;
    private String user_Age;
    private String user_profile_imagefile_name;
    private int has_user_profile = 0;
    private String user_profile_image_path;
    private int user_profile_image_Rotate = 0;
    private ImageView mIvMiror, mIvRortateRight, mIvRortateLeft;
    int imagerotation = 0;
    private static final String STATE_OUTPUT =
            "com.commonsware.cwac.cam2.playground.PictureActivity.STATE_OUTPUT";
    private Uri output = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_user_details);

        if (savedInstanceState != null) {
            output = savedInstanceState.getParcelable(STATE_OUTPUT);
        }

        if (!Environment.MEDIA_MOUNTED
                .equals(Environment.getExternalStorageState())) {
            Toast
                    .makeText(this, "Cannot access external storage!",
                            Toast.LENGTH_LONG)
                    .show();
            finish();
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        user_name = (EditText) findViewById(R.id.user_name);
        textViewvisible = (TextView) findViewById(R.id.textViewvisible);
        textView1 = (TextView) findViewById(R.id.textView1);
        full_name = (EditText) findViewById(R.id.full_name);
        age = (EditText) findViewById(R.id.age);
        lin1 = (LinearLayout) findViewById(R.id.lin1);
        nextto = (Button) findViewById(R.id.nextto);
        framered = (FrameLayout) findViewById(R.id.framered);
        mIvMeImage = (ImageView) findViewById(R.id.ivMeImage);
        mIvGallery = (ImageView) findViewById(R.id.ivGallery);
        mIvShowImage = (ImageView) findViewById(R.id.ivShowImage);
        mIvMiror = (ImageView) findViewById(R.id.ivMiror);
        mIvRortateRight = (ImageView) findViewById(R.id.ivRortateRight);
        mIvRortateLeft = (ImageView) findViewById(R.id.ivRortateLeft);

        // Create Object Spinner
        gender = (Spinner) findViewById(R.id.gender);
        // Set Spinner Value and select spinner value
        ArrayAdapter<String> adaptergender = new ArrayAdapter<String>(
                UpdateUserDetails_act.this,
                android.R.layout.simple_spinner_item, selectGender);
        adaptergender
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(adaptergender);
        // Create Object Volley library
        mnVolleyRequestQueue = Volley.newRequestQueue(this);
        nextto.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                userName = user_name.getText().toString();
                fullName = full_name.getText().toString();
                userAge = age.getText().toString();
                getGender = gender.getSelectedItem().toString();
                if (!user_name.getText().toString().trim()
                        .matches("^[a-zA-Z0-9_-]{1,20}$")) {
                    new AlertDialog.Builder(UpdateUserDetails_act.this)
                            .setTitle("User Name Validation")
                            .setMessage(
                                    "Please use valid user name. \nUser name can consist of only alphanumeric characters. \nNo special characters are allowed")
                            .setPositiveButton(R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {

                                        }
                                    })
                            .setIcon(android.R.drawable.ic_dialog_alert).show();
                } else if (userName.equals("") || fullName.trim().equals("")
                        || userAge.trim().equals("")
                        || getGender.trim().equals("GENDER")) {
                    new AlertDialog.Builder(UpdateUserDetails_act.this)
                            .setMessage("All fields are mandatory")
                            .setPositiveButton(R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            // continue with delete
                                        }
                                    })
                            .setIcon(android.R.drawable.ic_dialog_alert).show();
                } else {

                    if (!isFinishing())
                        mProgressDialog.show();
                    mProgressDialog.setMessage("checking...");
                    userName = user_name.getText().toString();
                    has_user_profile = MyApplication.getInstance().getPersistentPreference().getInt("first_Time_User", 0);
                    if (has_user_profile == 0) {
                        checkUserName();
                    } else {
                        sendUserInfo();
                    }
                }
                InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(nextto.getWindowToken(), 0);
            }
        });

        mIvMeImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(UpdateUserDetails_act.this, CameraActivity.class);
                startActivityForResult(intent, REQUEST_PHOTO);*/
                takePicture();
            }
        });

        mIvShowImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(UpdateUserDetails_act.this, CameraActivity.class);
                startActivityForResult(intent, REQUEST_PHOTO);*/
                takePicture();
            }
        });

        mIvGallery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMG);
            }
        });
        getUserProfile();

        mIvMiror.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mIvRortateRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    if (Build.VERSION.SDK_INT < 11) {
                        Toast.makeText(UpdateUserDetails_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                    } else {
                        mIvShowImage.setRotation(mIvShowImage.getRotation() + 90);
                    }
                } catch (Exception e) {
                    Toast.makeText(UpdateUserDetails_act.this, "places select image", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        mIvRortateLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (Build.VERSION.SDK_INT < 11) {
                        Toast.makeText(UpdateUserDetails_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                    } else {
                        mIvShowImage.setRotation(mIvShowImage.getRotation() - 90);
                    }
                } catch (Exception e) {
                    Toast.makeText(UpdateUserDetails_act.this, "places select your profile pict", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    private void takePicture() {
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        /*CameraActivity.IntentBuilder b =
                new CameraActivity.IntentBuilder(UpdateUserDetails_act.this);*/
        // File f = new File(getExternalFilesDir(null), "image" + timeStamp + ".png");
        // b.to(f);
        // setOutput(Uri.fromFile(f));
        /*Intent result;
        result = b.build();*/

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePicture(intent);
    }

    public void setOutput(Uri uri) {
        output = uri;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_OUTPUT, output);
    }

    public void takePicture(Intent i) {
        startActivityForResult(i, REQUEST_PHOTO);
    }

    private void setPic() {
        try {
            File auxFile = new File(mCurrentPhotoPath);
            getReducedImage(auxFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void getReducedImage(File mediaFile) {
        Bitmap b = decodeFileWithRotationIfNecessary(mediaFile);
        File f = getfileFromBitmap(b, mediaFile.getPath());
        mIvShowImage.setImageBitmap(b);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 90, bao);
        byte[] ba = bao.toByteArray();
        strImag = Base64.encodeToString(ba, Base64.DEFAULT);
    }


    private File getfileFromBitmap(Bitmap b, String path) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);


        // you can create a new file name "test.jpg" in sdcard folder.
        File f = new File(path);
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            // remember close de FileOutput
            fo.close();
            return f;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.v(TAG, "Exception caught");
            return null;
        }
        // write the bytes in file
    }


    private Bitmap decodeFileWithRotationIfNecessary(File f) {
        final int IMAGE_MAX_SIZE = 400;
        Bitmap b = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);

            BitmapFactory.decodeStream(fis, null, o);

            fis.close();

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.round(Math.log(IMAGE_MAX_SIZE
                                / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5)));
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (IOException e) {
            Log.v(TAG, "error in bitmap conversion");
            e.printStackTrace();

        }

        Bitmap bMapRotate = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
                b.getHeight(), getMatrix(f), true);

        return bMapRotate;
    }

    private Matrix getMatrix(File f) {
        Matrix mat = new Matrix();
        mat.postRotate(90);
        try {
            ExifInterface exif = new ExifInterface(f.getPath());

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, -1);

            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    Log.v(TAG, "flip horizontal");


                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:

                    Log.v(TAG, "flip vertical");
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    Log.v(TAG, "rotate 180");
                    mat.postRotate(90);

                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    Log.v(TAG, "rotate 90");

                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    Log.v(TAG, "rotate 270");
                    mat.postRotate(180);

                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    Log.v(TAG, "transpose");

                    break;
                case ExifInterface.ORIENTATION_UNDEFINED:
                    Log.v(TAG, "undefined");

                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                    Log.v(TAG, "normal");
                    mat.postRotate(270);

                    break;
                default:
                    Log.v(TAG, "default");
                    //  mat.postRotate(0);

                    break;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.v(TAG, "error in finding exif information");
        }

        return mat;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIvShowImage.setImageBitmap(null);
        bitmap = null;
        resizedBitmap = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK) {

            if (data != null) {
                Bitmap bitmap = data.getParcelableExtra("data");

                /*Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);*/
                mIvShowImage.setImageBitmap(bitmap);

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

                File file = new File(Environment.getExternalStorageDirectory() +
                        File.separator + "image" + timeStamp + ".png");

                try {
                    file.createNewFile();
                    FileOutputStream fo = new FileOutputStream(file);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                imageFileName = "image" + timeStamp + ".png";
                mCurrentPhotoPath = String.valueOf(file);

                MyApplication.getInstance().getPersistentPreferenceEditor().putString("PATH_NAME", mCurrentPhotoPath);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("IMAGE_PATH_NAME", imageFileName);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();

                byte[] ba = bytes.toByteArray();
                strImag = Base64.encodeToString(ba, Base64.DEFAULT);
                getOrientation(Uri.fromFile(file));
            }
        } else if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {

            try {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mCurrentPhotoPath = cursor.getString(columnIndex);
                imageFileName = mCurrentPhotoPath.substring(mCurrentPhotoPath.lastIndexOf("/") + 1);
                imageFileName = StringUtils.deleteWhitespace(imageFileName);
                cursor.close();
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("PATH_NAME", mCurrentPhotoPath);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("IMAGE_PATH_NAME", imageFileName);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                setPic();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (resultCode == RESULT_CANCELED) {
            mIvMeImage.setVisibility(View.VISIBLE);
            mIvGallery.setVisibility(View.VISIBLE);
        }
    }

    private String getOrientation(Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        String orientation = "landscape";
        try {
            String image = new File(uri.getPath()).getAbsolutePath();
            BitmapFactory.decodeFile(image, options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            if (imageHeight > imageWidth) {
                orientation = "portrait";
            }
        } catch (Exception e) {
            //Do nothing
        }
        return orientation;
    }

    // Create checkUserName Function
    public void checkUserName() {
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.CHECK_USER_NAME_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());

                        JSONObject jResponse;
                        try {
                            jResponse = new JSONObject(response);
                            String user_result = jResponse
                                    .getString("user_exists");
                            System.out.println("succ" + user_result);

                            resultdisplay(user_result);

                        } catch (Exception e) {
                            SSLog.e(TAG, "checkUserName", e);
                        }

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(
                        UpdateUserDetails_act.this.getBaseContext(),
                        getString(R.string.retry_internet),
                        Toast.LENGTH_LONG).show();

                SSLog.e(TAG, "checkUserName :-   ",
                        error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();

                params.put("username", userName);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }

                String url = Constants_user.CHECK_USER_NAME_URL;

                try {
                    url = url + "?" + getParams.toString();
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "checkUserName", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        mnVolleyRequestQueue.add(postRequest);

    } // checkUserName

    private void resultdisplay(String user_result) {
        if (!user_result.equals("0")) {
            progressCancel();
            framered.setBackgroundColor(Color.RED);
            textViewvisible.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(),
                    "User name exists, please chose another user name",
                    Toast.LENGTH_SHORT).show();

        } else {
            mProgressDialog.setMessage("Please wait...");
            sendUserInfo();
        }

    }

    // Create sendGroupInfo Function
    public void sendUserInfo() {
        // Send Data to Server Database Using Volley Library
        if (Build.VERSION.SDK_INT > 11) {
            imagerotation = (int) mIvShowImage.getRotation();
        }
        final String sImagePathName = MyApplication.getInstance().getPersistentPreference().getString("IMAGE_PATH_NAME", "");
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.UPDATE_APPUSER_DETAIL_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response);
                        userResult(response);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(
                        UpdateUserDetails_act.this.getBaseContext(),
                        getString(R.string.retry_internet),
                        Toast.LENGTH_LONG).show();

                SSLog.e(TAG, "sendUserInfo :-   ", error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", userName);
                params.put("fullname", fullName);
                params.put("sex", getGender);
                params.put("age", userAge);
                params.put("imagerotation", String.valueOf(imagerotation));
                if (!TextUtils.isEmpty(strImag)) {
                    params.put("userprofileimage", strImag);
                }
                if (!TextUtils.isEmpty(sImagePathName)) {
                    params.put("userprofileimagefilename", sImagePathName);
                }
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.UPDATE_APPUSER_DETAIL_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }

                String url = Constants_user.UPDATE_APPUSER_DETAIL_URL;

                try {
                    url = url + "?" + getParams.toString();
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "sendUserInfo", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        mnVolleyRequestQueue.add(postRequest);

    } // sendUpdatePosition

    private void userResult(String response) {

        JSONObject jResponse;
        try {
            jResponse = new JSONObject(response);
            String user_profile_saved = jResponse.getString("result");
            System.out.println("succ" + user_profile_saved);

            if (user_profile_saved.equals("1")) {
                setResult(1);
                try {
                    Thread.sleep(2000);
                    progressCancel();
                } catch (InterruptedException e) {
                }
                finish();
            }
        } catch (Exception e) {
            progressCancel();
            setResult(1);
            finish();
            SSLog.e(TAG, "userResult", e);
        }

    }

    public void progressCancel() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }

    }

    private void showpDialog() {
        if (!isFinishing()) {
            mProgressDialog.setMessage("Please wait ...");
            mProgressDialog.show();
        }
    }

    private void getUserProfile() {
        showpDialog();
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.GET_USER_PROFILE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        getMyProfile(response);

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                progressCancel();
                Toast.makeText(
                        UpdateUserDetails_act.this.getBaseContext(),
                        getString(R.string.retry_internet),
                        Toast.LENGTH_LONG).show();

                SSLog.e(TAG, "getUserProfile :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();

                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.GET_USER_PROFILE_URL);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }

                String url1 = Constants_user.GET_USER_PROFILE_URL;

                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "getMemberDetails", e);
                }

                return CGlobals_user.getInstance().checkParams(params);

            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);

    } // sendUpdatePosition

    private void getMyProfile(String response) {
        response = response.trim();
        if (response.trim().equals("-1")) {
            progressCancel();
            return;
        }
        JSONObject person;
        try {
            person = new JSONObject(response);
            progressCancel();
            user_Name = person.isNull("username") ? "" : person
                    .getString("username");
            user_Full_Name = person.isNull("fullname") ? "" : person
                    .getString("fullname");
            user_Age = person.isNull("age") ? "" : person.getString("age");
            user_Gender = person.isNull("sex") ? "" : person.getString("sex");
            user_profile_imagefile_name = person.isNull("userprofileimagefilename") ? "" : person
                    .getString("userprofileimagefilename");
            has_user_profile = person.isNull("has_user_profile") ? 0 : person.getInt("has_user_profile");
            user_profile_image_path = person.isNull("userprofileimagepath") ? "" : person.getString("userprofileimagepath");
            user_profile_image_Rotate = person.isNull("image_rotation") ? 0 : person.getInt("image_rotation");
            MyApplication.getInstance().getPersistentPreferenceEditor().putString("IMAGE_PATH_NAME", user_profile_imagefile_name);
            MyApplication.getInstance().getPersistentPreferenceEditor().commit();

            MyApplication.getInstance().getPersistentPreferenceEditor().putInt("first_Time_User", has_user_profile);
            MyApplication.getInstance().getPersistentPreferenceEditor().commit();
            if (has_user_profile == 1) {
                if (TextUtils.isEmpty(user_Name)) {
                    user_name.setText("");
                } else {

                    user_name.setClickable(false);
                    user_name.setFocusable(false);

                    user_name.setText(user_Name);
                }
                if (TextUtils.isEmpty(user_Full_Name)) {
                    full_name.setText("");
                } else {
                    full_name.setText(user_Full_Name);
                }
                if (TextUtils.isEmpty(user_Age)) {
                    age.setText("");
                } else {
                    age.setText(user_Age);
                }
                if (user_Gender.equals("MALE")) {
                    gender.setSelection(1);
                } else if (user_Gender.equals("FEMALE")) {
                    gender.setSelection(2);
                } else {
                    gender.setSelection(0);
                }
                if (TextUtils.isEmpty(user_profile_imagefile_name) && TextUtils.isEmpty(user_profile_image_path)) {
                    mIvShowImage.setImageResource(R.mipmap.ic_person);
                } else {
                    String url = Constants_user.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + user_profile_image_path +
                            user_profile_imagefile_name;
                    ImageRequest request = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    if (Build.VERSION.SDK_INT < 11) {
                                        Toast.makeText(UpdateUserDetails_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                    } else {
                                        mIvShowImage.setImageBitmap(bitmap);
                                    }
                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {

                                    mIvShowImage.setImageResource(R.mipmap.ic_person);
                                }
                            });
                    MyApplication.getInstance().addToRequestQueue(request);
                }
            }

        } catch (Exception e) {
            SSLog.e(TAG, "getMyProfile", e);
            progressCancel();
        }

    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }
}
