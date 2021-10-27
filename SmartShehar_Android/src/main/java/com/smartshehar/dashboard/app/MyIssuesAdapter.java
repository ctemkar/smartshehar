package com.smartshehar.dashboard.app;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartshehar.dashboard.app.ui.ActFollowupForm;
import com.smartshehar.dashboard.app.ui.Facebook_act;
import com.smartshehar.dashboard.app.ui.Twitter_act;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;
import lib.app.util.VolleySingleton;


public class MyIssuesAdapter extends RecyclerView.Adapter<MyIssuesAdapter.ViewHolder> {
    private static final String TAG = "MyIssuesAdapter";
    Context ctx;
    ArrayList<CIssue> imageArry;

    public MyIssuesAdapter(Context context, ArrayList<CIssue> imageArry) {
        this.ctx = context;
        this.imageArry = imageArry;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final NetworkImageView networkImageView;
        TextView txtIssueAddress, txtIssueType, txtIssueDate, txtIssueTime, txtSubmitter;
        ImageView ivFollowUp, ivShare, ivOne, ivTwo, ivThree, ivFour, ivFive, ivClosed, ivWarning;
        LinearLayout llStatus;

        public ViewHolder(View v) {
            super(v);
            networkImageView = (NetworkImageView) v.findViewById(R.id.im_poster);
            txtIssueAddress = (TextView) v.findViewById(R.id.txtIssueAddress);
            txtIssueType = (TextView) v.findViewById(R.id.txtIssueType);
            txtIssueTime = (TextView) v.findViewById(R.id.txtIssueTime);
            txtIssueDate = (TextView) v.findViewById(R.id.txtIssueDate);
            txtSubmitter = (TextView) v.findViewById(R.id.txtSubmitter);
            ivFollowUp = (ImageView) v.findViewById(R.id.ivFollowUp);
            ivShare = (ImageView) v.findViewById(R.id.ivShare);
            ivOne = (ImageView) v.findViewById(R.id.ivOne);
            ivTwo = (ImageView) v.findViewById(R.id.ivTwo);
            ivThree = (ImageView) v.findViewById(R.id.ivThree);
            ivFour = (ImageView) v.findViewById(R.id.ivFour);
            ivFive = (ImageView) v.findViewById(R.id.ivFive);
            ivClosed = (ImageView) v.findViewById(R.id.ivClosed);
            llStatus = (LinearLayout) v.findViewById(R.id.llStatus);
            ivWarning = (ImageView) v.findViewById(R.id.ivWarning);


        }

        public NetworkImageView getNetworkImageView() {
            return networkImageView;
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_layout, viewGroup, false);
        Log.d(TAG, "item_layout inflated");
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        try {
            Date date;
            SimpleDateFormat ft;
            String url = CGlobals_lib_ss.getPath() + imageArry.get(position)._imagepath + imageArry.get(position)._imageName;
            viewHolder.getNetworkImageView().setImageUrl(url, VolleySingleton.getInstance(ctx).getImageLoader());
            viewHolder.txtIssueAddress.setText(imageArry.get(position)._address);
            viewHolder.txtIssueType.setText(imageArry.get(position)._issue_type);
            ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT);
            date = ft.parse(imageArry.get(position)._issue_time);
            ft = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
            viewHolder.txtIssueDate.setText(ft.format(date));
            ft = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            viewHolder.txtIssueTime.setText(ft.format(date));
            viewHolder.ivOne.setTag(position);
            viewHolder.ivTwo.setTag(position);
            viewHolder.ivThree.setTag(position);
            viewHolder.ivFour.setTag(position);
            viewHolder.ivFive.setTag(position);
            viewHolder.llStatus.setTag(position);
            viewHolder.ivClosed.setTag(position);
            viewHolder.ivWarning.setTag(position);
            viewHolder.txtSubmitter.setTag(position);
            viewHolder.txtSubmitter.setVisibility(View.VISIBLE);
            viewHolder.ivWarning.setVisibility(View.GONE);
            viewHolder.ivOne.setImageResource(R.drawable.ic_gray_one);
            viewHolder.ivTwo.setImageResource(R.drawable.ic_gray_two);
            viewHolder.ivThree.setImageResource(R.drawable.ic_gray_three);
            viewHolder.ivFour.setImageResource(R.drawable.ic_gray_four);
            viewHolder.ivFive.setImageResource(R.drawable.ic_gray_five);
            viewHolder.llStatus.setVisibility(View.VISIBLE);
            viewHolder.ivClosed.setVisibility(View.GONE);

            if (imageArry.get(position).get_sentToServer() == 1)
                viewHolder.ivOne.setImageResource(R.drawable.ic_green_one);
            if (imageArry.get(position).get_approved() == 1 || imageArry.get(position).getRejected() == 1)
                viewHolder.ivTwo.setImageResource(R.drawable.ic_green_two);
            if (imageArry.get(position).get_lettersubmitted() == 1)
                viewHolder.ivThree.setImageResource(R.drawable.ic_green_three);
            if (imageArry.get(position).get_resolved_unresolved() == 1)
                viewHolder.ivFour.setImageResource(R.drawable.ic_green_four);
            if (imageArry.get(position).get_open() == 1)
                viewHolder.ivFive.setImageResource(R.drawable.ic_green_five);
            if (imageArry.get(position).get_closed() == 1) {
                viewHolder.llStatus.setVisibility(View.GONE);
                viewHolder.ivClosed.setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(imageArry.get(position).getGroup_name()))
                viewHolder.txtSubmitter.setText(imageArry.get(position).getGroup_name());
            else
                viewHolder.txtSubmitter.setVisibility(View.GONE);

            if (imageArry.get(position).get_sentToServer() == 0) {
                viewHolder.ivWarning.setVisibility(View.VISIBLE);
                viewHolder.llStatus.setVisibility(View.GONE);
            }

            viewHolder.networkImageView.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(imageArry.get(position)._imageName)) {
                viewHolder.networkImageView.setVisibility(View.GONE);
            }
            viewHolder.ivShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String message = imageArry.get(position).getTypeOffence() + " at "
                            + imageArry.get(position).getAddress() + " at "
                            + imageArry.get(position).getTimeOffence();
                    List<String> items = Arrays.asList(imageArry.get(position).getImageName().split(","));
                    String path = "/storage/sdcard0/Pictures/Smartshehar App/" + items.get(0);
                    String sPath = CGlobals_lib_ss.getPath() + imageArry.get(position)._imagepath + imageArry.get(position)._imageName;
                    String tMessage = "I just helped my city by taking action instead of just complaining. Your turn now " + Constants_dp.REPORT_FILTER_URL + "issueid=" + imageArry.get(position)._id;
                    String fMessage = "I just helped my city by taking action instead of just complaining. Your turn now ";
                    String fPath = Constants_dp.REPORT_FILTER_URL + "issueid=" + imageArry.get(position)._id;
                    File f = new File(path);
                    if (!f.exists()) {
                        new SaveImage().execute(sPath, imageArry.get(position)._imageName);

                    } else {

                        List<Intent> targetedShareIntents = new ArrayList<Intent>();

                        Intent facebookIntent = new Intent(ctx, Facebook_act.class);
                        facebookIntent.putExtra("INTENT_MESSAGE", fMessage);
                        facebookIntent.putExtra("INTENT_DD_PATH", fPath);
                        facebookIntent.putExtra("INTENT_PATH", sPath);
                        facebookIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, R.mipmap.ic_facebook);
                        facebookIntent.putExtra(Intent.EXTRA_TITLE, "Facebook");
                        facebookIntent.setAction(Intent.ACTION_SEND);
                        facebookIntent.setType("text/plain");
                        targetedShareIntents.add(facebookIntent);

                        Intent twitterIntent = new Intent(ctx, Twitter_act.class);
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
                        try {
                            Intent chooser = Intent.createChooser(targetedShareIntents.remove(0), "Smartshehar issue sharing..");
                            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
                            ctx.startActivity(chooser);
                        } catch (Exception e) {
                            e.printStackTrace();
                            SSLog.e(TAG,"ivShare ",e.toString());
                        }
                    }
                }
            });


            viewHolder.ivFollowUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(ctx, ActFollowupForm.class);
                        intent.putExtra(ActFollowupForm.ARG_ISSUE_ID, imageArry.get(position).getID());
                        ctx.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG, "onBindViewHolder", e.toString());
        }
    }

    @Override
    public int getItemCount() {
        return imageArry.size();
    }

    private Intent getShareIntent(String type, String subject, String text, File f, String path, String sPath) {
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");

        // gets the list of intents that can be loaded.
        List<ResolveInfo> resInfo = ctx.getPackageManager().queryIntentActivities(share, 0);
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
                        share.setAction(Intent.ACTION_SEND);
                        share.setType("image/*");

                    } else {
                        share.putExtra(Intent.EXTRA_SUBJECT, subject);
                        share.putExtra(Intent.EXTRA_TEXT, text);
                        share.setAction(Intent.ACTION_SEND);
                        share.setType("text/plain");
                    }
                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }
            if (!found)
                return null;
            else
                return share;
        }
        return null;
    }

    class SaveImage extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;

        SaveImage() {
            dialog = new ProgressDialog(ctx);
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
                    MediaScannerConnection.scanFile(ctx,
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
