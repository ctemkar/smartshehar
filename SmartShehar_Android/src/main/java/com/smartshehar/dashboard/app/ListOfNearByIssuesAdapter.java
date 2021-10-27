package com.smartshehar.dashboard.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.smartshehar.dashboard.app.ui.ActFollowupForm;
import com.smartshehar.dashboard.app.ui.Facebook_act;
import com.smartshehar.dashboard.app.ui.Twitter_act;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;
import lib.app.util.VolleySingleton;


public class ListOfNearByIssuesAdapter extends RecyclerView.Adapter<ListOfNearByIssuesAdapter.ViewHolder> {
    private static final String TAG = "ListOfNear";

    Activity activity;
    ArrayList<CIssue> imageArry;

    public ListOfNearByIssuesAdapter(Activity activity, ArrayList<CIssue> imageArry) {
        this.activity = activity;
        this.imageArry = imageArry;

    }

//    private static final String url = "https://farm6.static.flickr.com/5473/12316534935_b423073b4b_b.jpg";

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final NetworkImageView networkImageView;
        TextView txtIssueAddress, txtIssueType, txtIssueDate,txtIssueTime,
                txtGrpName, tvLikedCount,tvUnlikCount;

        ImageView ivLike,ivUnlike,ivFollowUp,ivShare;


        public ViewHolder(View v) {
            super(v);

            networkImageView = (NetworkImageView) v.findViewById(R.id.im_poster);
            txtIssueAddress = (TextView) v.findViewById(R.id.txtIssueAddress);
            txtIssueType = (TextView) v.findViewById(R.id.txtIssueType);
            txtIssueDate = (TextView) v.findViewById(R.id.txtIssueDate);
            txtIssueTime = (TextView) v.findViewById(R.id.txtIssueTime);
            txtGrpName = (TextView) v.findViewById(R.id.txtGrpName);
            ivLike = (ImageView) v.findViewById(R.id.ivLike);
            tvLikedCount = (TextView) v.findViewById(R.id.tvLikedCount);
            ivUnlike = (ImageView) v.findViewById(R.id.ivUnlike);
            tvUnlikCount = (TextView) v.findViewById(R.id.tvUnlikCount);
            ivFollowUp = (ImageView) v.findViewById(R.id.ivFollowUp);
            ivShare = (ImageView) v.findViewById(R.id.ivShare);

        }
        public NetworkImageView getNetworkImageView() {
            return networkImageView;
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_near_by_issues, viewGroup, false);


        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        try {

            Date date;
            SimpleDateFormat ft;
            String url = CGlobals_lib_ss.getPath() + imageArry.get(position)._imagepath + imageArry.get(position)._imageName;
            Log.d(TAG, "Url " + url);
            viewHolder.getNetworkImageView().setImageUrl(url, VolleySingleton.getInstance(activity.getApplicationContext()).getImageLoader());
            viewHolder.txtIssueAddress.setText(imageArry.get(position)._address);
            viewHolder.txtIssueType.setText(imageArry.get(position)._issue_type);

            ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT);
            date = ft.parse(imageArry.get(position)._issue_time);
            ft = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
            viewHolder.txtIssueDate.setText(ft.format(date));
            ft = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            viewHolder.txtIssueTime.setText(ft.format(date));


            viewHolder.txtGrpName.setText(imageArry.get(position).group_name);
            if(TextUtils.isEmpty(imageArry.get(position).group_name))
                viewHolder.txtGrpName.setVisibility(View.GONE);
            else
                viewHolder.txtGrpName.setVisibility(View.VISIBLE);
            viewHolder.ivLike.setTag(position);
            viewHolder.tvLikedCount.setTag(position);
            viewHolder.ivUnlike.setTag(position);
            viewHolder.tvUnlikCount.setTag(position);
            viewHolder.ivFollowUp.setTag(position);
            viewHolder.tvLikedCount.setText(imageArry.get(position).getLikedcount()+" Interested");
            viewHolder.tvUnlikCount.setText(imageArry.get(position).getUnlikedcount()+" Interested");

            if(imageArry.get(position).getLiked()==1)
            {
                viewHolder.ivLike.setImageResource(R.mipmap.ic_people_count);
            }else{
                viewHolder.ivLike.setImageResource(R.mipmap.ic_people_count_gray);
            }
            if (TextUtils.isEmpty(imageArry.get(position)._imageName) || TextUtils.isEmpty(imageArry.get(position)._imagepath))
                viewHolder.networkImageView.setVisibility(View.GONE);
            else
                viewHolder.networkImageView.setVisibility(View.VISIBLE);

            viewHolder.ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(imageArry.get(position).getLiked()==1)
                    {
                        addLikedCount(String.valueOf(imageArry.get(position).getID()),"liked",0);
                    }else{
                        addLikedCount(String.valueOf(imageArry.get(position).getID()),"liked",1);
                    }

                }
            });

            viewHolder.ivUnlike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addLikedCount(String.valueOf(imageArry.get(position).getID()),"unliked",1);
                }
            });
            viewHolder.ivFollowUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(activity, ActFollowupForm.class);
                        intent.putExtra(ActFollowupForm.ARG_ISSUE_ID, imageArry.get(position).getID());
                        activity.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            viewHolder.ivShare.setTag(position);
            viewHolder.ivShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message = imageArry.get(position).getTypeOffence() + " at "
                            + imageArry.get(position).getAddress() + " at "
                            + imageArry.get(position).getTimeOffence();
                    List<String> items = Arrays.asList(imageArry.get(position).getImageName().split(","));
                    String path = "/storage/sdcard0/Pictures/Smartshehar App/" + items.get(0);
                    String sPath = CGlobals_lib_ss.getPath() + imageArry.get(position)._imagepath + imageArry.get(position)._imageName;
                    String tMessage = "I just helped my city by taking action instead of just complaining. Your turn now "+Constants_dp.REPORT_FILTER_URL+"issueid="+imageArry.get(position)._id;
                    String fMessage = "I just helped my city by taking action instead of just complaining. Your turn now ";
                    String fPath = Constants_dp.REPORT_FILTER_URL+"issueid="+imageArry.get(position)._id;
                    File f = new File(path);
                    if (!f.exists()) {
                        new SaveImage().execute(sPath, imageArry.get(position)._imageName);

                    } else {

                        List<Intent> targetedShareIntents = new ArrayList<Intent>();

                        Intent facebookIntent = new Intent(activity.getApplicationContext(), Facebook_act.class);
                        facebookIntent.putExtra("INTENT_MESSAGE", fMessage);
                        facebookIntent.putExtra("INTENT_DD_PATH",fPath);
                        facebookIntent.putExtra("INTENT_PATH", sPath);
                        facebookIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, R.mipmap.ic_facebook);
                        facebookIntent.putExtra(Intent.EXTRA_TITLE, "Facebook");
                        facebookIntent.setAction(Intent.ACTION_SEND);
                        facebookIntent.setType("text/plain");
                        targetedShareIntents.add(facebookIntent);

                        Intent twitterIntent = new Intent(activity.getApplicationContext(), Twitter_act.class);
                        twitterIntent.putExtra("INTENT_MESSAGE", tMessage);
                        twitterIntent.putExtra("INTENT_PATH", sPath);
                        twitterIntent.putExtra("INTENT_NAME", imageArry.get(position)._imageName);
                        twitterIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, R.mipmap.ic_twitter);
                        twitterIntent.putExtra(Intent.EXTRA_TITLE, "Twitter");
                        twitterIntent.setAction(Intent.ACTION_SEND);
                        twitterIntent.setType("text/plain");
                        targetedShareIntents.add(twitterIntent);

                        Intent gmailIntent = getShareIntent("gmail", "I just helped my city by taking action instead of just complaining. Check this Out...", message, f, path, sPath);
                        if (gmailIntent != null)
                            targetedShareIntents.add(gmailIntent);
                        Intent whatsappIntent = getShareIntent("whatsapp", "subject", message, f, path, sPath);
                        if (whatsappIntent != null)
                            targetedShareIntents.add(whatsappIntent);
                        Intent chooser = Intent.createChooser(targetedShareIntents.remove(0), "Smartshehar issue sharing..");
                        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
                        activity.startActivity(chooser);
                    }
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return imageArry.size();
    }

    public void addLikedCount(final String issueid, final String col, final int val) {
        try {

                final String url = Constants_dp.ADD_ISSUE_LIKE_COUNT_URL;
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                if (!response.trim().equals("-1")) {
                                    Intent intent = activity.getIntent();
                                    activity.finish();
                                    activity.startActivity(intent);

                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d(TAG, "error is " + error.toString());
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("issueid", issueid);
                        params.put(col, String.valueOf(val));


                        params = CGlobals_db.getInstance(activity).getBasicMobileParams(params,
                                url, activity);

                        Log.d(TAG, "PARAM IS " + params);
                        return CGlobals_db.getInstance(activity).checkParams(params);
                    }
                };
                CGlobals_db.getInstance(activity).getRequestQueue(activity).add(postRequest);


        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }
    private Intent getShareIntent(String type, String subject, String text, File f, String path, String sPath) {
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");

        // gets the list of intents that can be loaded.
        List<ResolveInfo> resInfo = activity.getPackageManager().queryIntentActivities(share, 0);
        System.out.println("resinfo: " + resInfo);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(type) ||
                        info.activityInfo.name.toLowerCase().contains(type)) {

                    if (f.exists()) {
                        Uri imageUri = Uri.fromFile(f);
                        share.putExtra(Intent.EXTRA_SUBJECT, subject);
                        share.putExtra(Intent.EXTRA_TEXT, text);
                        share.putExtra(Intent.EXTRA_STREAM, imageUri);
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        share.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        share.setAction(Intent.ACTION_SEND);
                        share.setType("image/*");

                    } else {
                        share.putExtra(Intent.EXTRA_SUBJECT, subject);
                        share.putExtra(Intent.EXTRA_TEXT, text);
                        share.setType("text/plain");
                    }
                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }
            if (!found)
                return null;

            return share;
        }
        return null;
    }

    private void downloadImage(String path) {

    }

    class SaveImage extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;
        SaveImage()
        {
            dialog = new ProgressDialog(activity);
            dialog.setCancelable(false);
            dialog.setMessage("Wait! Storing image..");
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.show();
        }
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            String path;
            try {
                path = params[0];
                url = new URL(path);
                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                // String root = Environment.getExternalStorageDirectory().toString();
                // File myDir = new File(root + "/saved_images");
                File myDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "SmartShehar App");
                myDir.mkdirs();

                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String fname = params[1];
                File file = new File(myDir, fname);
                try {
                    MediaScannerConnection.scanFile(activity.getApplicationContext(),
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
                if (file.exists()) file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    image.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
        }
    }
}
