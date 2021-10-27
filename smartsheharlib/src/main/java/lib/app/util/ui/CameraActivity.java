package lib.app.util.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;


public class CameraActivity extends AppCompatActivity {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    static final String TAG = "CameraActivity: ";
    static final int REQUEST_TAKE_PHOTO = 1;
    static File photoFile;
    static String PHOTO_DIRECTORY;
    String mCurrentPhotoPath;
    public static Uri uri = null;
    public static String imageName = "";
    Bitmap bitmap, resizedBitmap;
    private static final int MAX_SMALL_PICTURE_SIZE = 300;
    private static final int MAX_BIG_PICTURE_SIZE = 500;
//    static final int REQUEST_PHOTO = 1555;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_camera);
        Intent intent = getIntent();
        if (intent != null) {
            PHOTO_DIRECTORY = intent.getStringExtra("PHOTO_DIRECTORY");
        }
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            try {
                String photo_File = savedInstanceState.getString("photoFile");
                if (TextUtils.isEmpty(photo_File))
                    return;
                photoFile = new File(photo_File);
                Constants_lib_ss.PREF_CAMERA_PHOTO_PATH = photo_File;
                imageName = savedInstanceState.getString("imageName");
                Constants_lib_ss.PREF_IMAGE_NAME = imageName;
                photoFile = new File(Constants_lib_ss.PREF_CAMERA_PHOTO_PATH);
                galleryAddPic(photoFile);
                setPic(photoFile);
            } catch (Exception e) {
                Toast.makeText(CameraActivity.this, "error in savedInstanceState is " + e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            dispatchTakePictureIntent();
        }
    }
    private void dispatchTakePictureIntent() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
        // start the image capture Intent
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
       /* } else {
            com.commonsware.cwac.cam2.CameraActivity.IntentBuilder b =
                    new com.commonsware.cwac.cam2.CameraActivity.IntentBuilder(CameraActivity.this);
            Intent result;
            result = b.build();
            takePicture(result);
        }*/
    }

   /* public void takePicture(Intent i) {
        startActivityForResult(i, REQUEST_PHOTO);
    }*/

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {

        try {
            photoFile = getOutputMediaFile(type);
            uri = Uri.fromFile(photoFile);
        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "getOutputMediaFileUri " + e);
        }

        return uri;
    }

    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), PHOTO_DIRECTORY);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(PHOTO_DIRECTORY, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageName = "IMG_" + timeStamp + ".png";
        // imageNamePng = "IMG_" + timeStamp;
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + imageName);
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void galleryAddPic(File file) {
        try {
            MediaScannerConnection.scanFile(this,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "galleryAddPic " + e);
        }
    }

    private void setPic(File pFile) {
        try {
            bitmap = null;
            resizedBitmap = null;
            mCurrentPhotoPath = pFile.getAbsolutePath();
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = 4;// calculateInSampleSize(bmOptions,100, 100);
            bmOptions.inPurgeable = true;
            bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            int ratio, newHeight, newWidth;
            int iMaxPictureSize = MAX_SMALL_PICTURE_SIZE;
            if (Connectivity.isConnectedFast(CameraActivity.this))
                iMaxPictureSize = MAX_BIG_PICTURE_SIZE;
            if (height > width)
                ratio = height / iMaxPictureSize;
            else
                ratio = width / iMaxPictureSize;
            if (ratio == 0)
                ratio = 1;
            newHeight = height / ratio;
            newWidth = width / ratio;
            resizedBitmap =
                    Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
         /*   int iMaxPictureSize = MAX_SMALL_PICTURE_SIZE;
            if (Connectivity.isConnectedFast(CameraActivity.this))
                iMaxPictureSize = MAX_BIG_PICTURE_SIZE;
            resizedBitmap = getResizedBitmap(bitmap, iMaxPictureSize);*/
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bao);
            byte[] ba = bao.toByteArray();
//            String strImag = Base64.encodeToString(ba, Base64.DEFAULT);

            if (bao != null) {
                bao.flush();
                bao.close();
            }
            // Get the dimensions of the View
            Bundle args = new Bundle();
            args.putString("imageFilePath", mCurrentPhotoPath);
            args.putString("imageName", imageName);
            args.putByteArray("imageByte", ba);
//            args.putString("imageString", strImag.toString());

            Intent intent = new Intent();
            intent.putExtra("data", args);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            setResult(RESULT_OK, intent);
            CameraActivity.this.finish();
            overridePendingTransition(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "setPic " + e.toString());
        }

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
                if (photoFile != null) {
                    galleryAddPic(photoFile);
                    setPic(photoFile);
                }
            } else {
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG, "onActivityResult " + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bitmap = null;
        resizedBitmap = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("photoFile", String.valueOf(photoFile));
        savedInstanceState.putString("imageName", imageName);
        super.onSaveInstanceState(savedInstanceState);
    }
}