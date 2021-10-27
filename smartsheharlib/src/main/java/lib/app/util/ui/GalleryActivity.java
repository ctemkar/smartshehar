
package lib.app.util.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lib.app.util.Connectivity;
import lib.app.util.SSLog_SS;


public class GalleryActivity extends AppCompatActivity {
    static final String TAG = "GalleryActivity: ";
    static File photoFile;
    static String PHOTO_DIRECTORY;
    String mCurrentPhotoPath;
    public static Uri uri = null;
    public static String imageName = "";
    Bitmap bitmap, resizedBitmap;
    private static final int MAX_SMALL_PICTURE_SIZE = 300;
    private static final int MAX_BIG_PICTURE_SIZE = 500;
    private static final int RESULT_LOAD_IMG = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Intent intent = getIntent();
            if (intent != null) {
                PHOTO_DIRECTORY = intent.getStringExtra("PHOTO_DIRECTORY");
            }
            photoFile = getOutputMediaFile();
            if(photoFile != null)
            {  mCurrentPhotoPath = photoFile.getAbsolutePath();
             dispatchTakePictureIntent();}else{
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            SSLog_SS.e(TAG,"onCreate "+e.toString());
        }
    }

    private void dispatchTakePictureIntent() {

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMG);
    }


    private void saveImageInFolder(Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(photoFile);//FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                    galleryAddPic(photoFile);
//                    bitmap  = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static File getOutputMediaFile() {
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
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + imageName);
        return mediaFile;
    }

    private void galleryAddPic(File file) {
        MediaScannerConnection.scanFile(this,
                new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    private void setPic(String mPhotoPath) {
        try {
            bitmap = null;
            resizedBitmap = null;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = false;
            bmOptions. inSampleSize = 4;
            bmOptions. inPurgeable = true ;
            bitmap = BitmapFactory.decodeFile(mPhotoPath, bmOptions);
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            int ratio, newHeight, newWidth;
            int iMaxPictureSize = MAX_SMALL_PICTURE_SIZE;
            if (Connectivity.isConnectedFast(GalleryActivity.this))
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

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bao);
            byte[] ba = bao.toByteArray();
//            String strImag = Base64.encodeToString(ba, Base64.DEFAULT);

            if(bao!=null)
            {
                bao.flush();
                bao.close();
            }
            saveImageInFolder(resizedBitmap);
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
            GalleryActivity.this.finish();
            overridePendingTransition(0, 0);

        } catch (Exception e) {
            Log.d(TAG, "Set *****************" + e.toString());
            SSLog_SS.e(TAG, e.toString());
            SSLog_SS.e(TAG, "ERROR IS " + e.toString());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String mPhotoPath = cursor.getString(columnIndex);
            cursor.close();
            setPic(mPhotoPath);
        } else {
            finish();
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
