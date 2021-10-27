package com.smartshehar.dashboard.app.ui;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.smartshehar.dashboard.app.Constants_dp;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;


/**
 * Created by Priyabrat on 5/24/2015.
 */
public class PostUpdateToTwitter extends AsyncTask<Void, Void, Void> {

    private Activity activity;
    private String status;
    private int imagePath;
    private String path, imagename;
    ProgressDialog progressDialog;
    public PostUpdateToTwitter(Activity activity, String status, int imagePath ,
                               String path,String name) {
        this.activity = activity;
        this.status = status;
        this.imagePath = imagePath;
        this.path = path;
        this.imagename = name;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Please wait..");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        String status = this.status;
        try {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(Constants_dp.KEY_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(Constants_dp.KEY_CONSUMER_SECRET);
            SharedPreferences preferences = activity.getSharedPreferences(Constants_dp.PREF_NAME,0);
            String access_token = preferences.getString(Constants_dp.KEY_OAUTH_TOKEN, "");
            String access_token_secret = preferences.getString(Constants_dp.KEY_OAUTH_SECRET, "");
            AccessToken accessToken = new AccessToken(access_token, access_token_secret);
            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
            // Update status
            StatusUpdate statusUpdate = new StatusUpdate(status);
           /* InputStream is = activity.getResources().openRawResource(imagePath);
            statusUpdate.setMedia("image_to_send.jpg", is);*/
//            StatusUpdate statusUpdate = new StatusUpdate("Hello");
            URL url = new URL(path);
            URLConnection urlConnection = url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            statusUpdate.setMedia(imagename, in);

            twitter4j.Status response = null;
            try {
                response = twitter.updateStatus(statusUpdate);
                if(response==null)
                    Toast.makeText(activity, "Posted Successfully", Toast.LENGTH_LONG).show();
                else
                Toast.makeText(activity, "Posting failed", Toast.LENGTH_LONG).show();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            Log.d("Status", response.getText());
        } catch (Exception e) {
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();

    }
}
