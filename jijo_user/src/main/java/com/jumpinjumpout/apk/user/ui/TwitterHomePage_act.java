package com.jumpinjumpout.apk.user.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.user.Constants_user;


public class TwitterHomePage_act extends Activity {

    TextView textViewWelcomeMsg;
    TextView editTextComment;
    Button btnSendPost;
    String userName;
    String sMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sMessage = extras.getString("INTENT_MESSAGE_TWITTER");
        }
        textViewWelcomeMsg = (TextView) findViewById(R.id.welcomeMsg);
        editTextComment = (TextView) findViewById(R.id.editTextComment);
        btnSendPost = (Button) findViewById(R.id.btnSend);
        SharedPreferences preferences = getSharedPreferences(Constants_user.PREF_NAME, 0);
        userName = preferences.getString(Constants_user.KEY_USER_NAME, "");
        textViewWelcomeMsg.setText(userName);
        editTextComment.setText(sMessage);
        btnSendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PostUpdateToTwitter(TwitterHomePage_act.this, sMessage, R.drawable.logo).execute();
            }
        });
    }
}
