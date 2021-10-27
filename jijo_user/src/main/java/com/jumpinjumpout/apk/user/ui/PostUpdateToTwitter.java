package com.jumpinjumpout.apk.user.ui;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.jumpinjumpout.apk.user.Constants_user;

import java.io.InputStream;

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
    ProgressDialog progressDialog;
    public PostUpdateToTwitter(Activity activity, String status, int imagePath) {
        this.activity = activity;
        this.status = status;
        this.imagePath = imagePath;
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
            builder.setOAuthConsumerKey(Constants_user.KEY_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(Constants_user.KEY_CONSUMER_SECRET);
            SharedPreferences preferences = activity.getSharedPreferences(Constants_user.PREF_NAME,0);
            String access_token = preferences.getString(Constants_user.KEY_OAUTH_TOKEN, "");
            String access_token_secret = preferences.getString(Constants_user.KEY_OAUTH_SECRET, "");
            AccessToken accessToken = new AccessToken(access_token, access_token_secret);
            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
            // Update status
            StatusUpdate statusUpdate = new StatusUpdate(status);
            InputStream is = activity.getResources().openRawResource(imagePath);
            statusUpdate.setMedia("image_to_send.jpg", is);
            twitter4j.Status response = null;
            try {
                response = twitter.updateStatus(statusUpdate);
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
        Toast.makeText(activity, "Posted Successfully", Toast.LENGTH_LONG).show();
    }
}
