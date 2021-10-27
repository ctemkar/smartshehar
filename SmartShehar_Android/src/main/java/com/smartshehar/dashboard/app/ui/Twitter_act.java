package com.smartshehar.dashboard.app.ui;


import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.FragmentTaskCompleteListener;
import com.smartshehar.dashboard.app.NetworkImageView;
import com.smartshehar.dashboard.app.R;

import lib.app.util.VolleySingleton;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class Twitter_act extends Activity implements FragmentTaskCompleteListener {

    Button btnLogin;
    Twitter twitter;
    RequestToken requestToken;
    SharedPreferences sharedPreferences;
    String sMessage = "", path="",name="";
    NetworkImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enabling Strict Mode
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.twitter_act);
        imageView = (NetworkImageView) findViewById(R.id.imageView);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sMessage = extras.getString("INTENT_MESSAGE");
            path = extras.getString("INTENT_PATH");
            name =  extras.getString("INTENT_NAME");
        }
        imageView.setImageUrl(path, VolleySingleton.getInstance(Twitter_act.this).getImageLoader());
        sharedPreferences = getSharedPreferences(Constants_dp.PREF_NAME,0);
        boolean status = sharedPreferences.getBoolean(Constants_dp.KEY_TWITTER_LOGIN,false);
        if(status){
            Intent intent = new Intent(getApplicationContext(),TwitterHomePage_act.class);
            intent.putExtra("INTENT_MESSAGE",sMessage);
            intent.putExtra("INTENT_PATH",path);
            intent.putExtra("INTENT_NAME", name);
            startActivity(intent);
            finish();
        }
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 final ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(Constants_dp.KEY_CONSUMER_KEY);
                builder.setOAuthConsumerSecret(Constants_dp.KEY_CONSUMER_SECRET);
                final Configuration configuration = builder.build();
                final TwitterFactory factory = new TwitterFactory(configuration);
                twitter = factory.getInstance();
                try {
                    requestToken = twitter.getOAuthRequestToken(Constants_dp.KEY_CALLBACK_URL);
                    String authUrl = requestToken.getAuthenticationURL();
                    // Open Fragment Dialog
                    FragmentManager fragmentManager = getFragmentManager();
                    LoginDialogFragment dialogFragment = LoginDialogFragment.getInstance(authUrl);
                    dialogFragment.show(fragmentManager,"appified_tag_priyabrat");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onTaskComplete(String verifierUrl) {
        try{
            AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifierUrl);
            saveUserData(accessToken);
            long userID = accessToken.getUserId();
            final User user = twitter.showUser(userID);
            String username = user.getName();
            Toast.makeText(getApplicationContext(), "UserName is: " + username, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(),TwitterHomePage_act.class);
            intent.putExtra("INTENT_MESSAGE",sMessage);
            intent.putExtra("INTENT_PATH",path);
            intent.putExtra("INTENT_NAME", name);
            startActivity(intent);
            finish();
        }catch (Exception e){
            Log.d("Error", e + "");
        }
    }

    public void saveUserData(AccessToken accessToken){
        long userID = accessToken.getUserId();
        User user;
        try {
            user = twitter.showUser(userID);
            String username = user.getName();
			/* Storing oAuth tokens to shared preferences */
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString(Constants_dp.KEY_OAUTH_TOKEN, accessToken.getToken());
            e.putString(Constants_dp.KEY_OAUTH_SECRET, accessToken.getTokenSecret());
            e.putBoolean(Constants_dp.KEY_TWITTER_LOGIN, true);
            e.putString(Constants_dp.KEY_USER_NAME, username);
            e.commit();
        } catch (TwitterException e1) {
            e1.printStackTrace();
        }
    }
}