package com.smartshehar.dashboard.app.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.NetworkImageView;
import com.smartshehar.dashboard.app.R;

import lib.app.util.VolleySingleton;

public class TwitterHomePage_act extends Activity {

    TextView textViewWelcomeMsg;
    TextView editTextComment;
    Button btnSendPost;
    String userName;
    String sMessage = "", path = "", name = "";
    NetworkImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        imageView = (NetworkImageView) findViewById(R.id.imageView);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sMessage = extras.getString("INTENT_MESSAGE");
            path = extras.getString("INTENT_PATH");
            name = extras.getString("INTENT_NAME");

        }
        imageView.setImageUrl(path, VolleySingleton.getInstance(TwitterHomePage_act.this).getImageLoader());
        textViewWelcomeMsg = (TextView) findViewById(R.id.welcomeMsg);
        editTextComment = (TextView) findViewById(R.id.editTextComment);
        btnSendPost = (Button) findViewById(R.id.btnSend);
        SharedPreferences preferences = getSharedPreferences(Constants_dp.PREF_NAME, 0);
        userName = preferences.getString(Constants_dp.KEY_USER_NAME, "");
        textViewWelcomeMsg.setText(userName);
        editTextComment.setText(sMessage);
        btnSendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PostUpdateToTwitter(TwitterHomePage_act.this, sMessage, R.mipmap.ic_logo, path, name).execute();
            }
        });
    }
}
