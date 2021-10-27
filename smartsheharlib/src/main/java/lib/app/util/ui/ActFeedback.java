package lib.app.util.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import lib.app.util.Connectivity;
import smartsheharcom.www.smartsheharlib.R;


public class ActFeedback extends Activity {

    Button mBtnEmail, mBtnAd, mBtnRateAndroid, mBtnFacebook, mBtnTwitter;
    TextView mTvWebSite, mTvVersion;
    private String mAppTitle = "", mAppCode = "", mVersionName = "", feedback_email = "", feedback_facebook = "",
            feedback_twitter = "", feedback_website = "", feedback_Packagname = "";
    Connectivity mConnectivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ActionBar ab = getSupportActionBar();
        //ab.setHomeButtonEnabled(true);

        setContentView(R.layout.actfeedback);
        mConnectivity = new Connectivity();
        mBtnEmail = (Button) findViewById(R.id.btnEmail);
        mBtnFacebook = (Button) findViewById(R.id.btnFacebook);
        mBtnTwitter = (Button) findViewById(R.id.btnTwitter);
        mBtnRateAndroid = (Button) findViewById(R.id.btnRateAndroid);
        mBtnAd = (Button) findViewById(R.id.btnAdvertise);
        mTvWebSite = (TextView) findViewById(R.id.tvWebSite);
        mTvVersion = (TextView) findViewById(R.id.tvVersion);
        try {

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mAppTitle = extras.getString("app_label");
                mAppCode = extras.getString("appCode");
                mVersionName = extras.getString("versionName");
                feedback_email = extras.getString("USER_FEEDBACK_EMAIL");
                feedback_facebook = extras.getString("FACEBOOK_POST");
                feedback_twitter = extras.getString("TWITTER_POST");
                feedback_website = extras.getString("WEB_SITE");
                feedback_Packagname = extras.getString("PACKAGE_NAME");
            }
        } catch (Exception e) {
            e.printStackTrace();
//            SSLog.e("ActFeedback: Create", "OnCreate", e);
        }
        if (!mConnectivity.checkConnected(ActFeedback.this)) {
            if (!mConnectivity.connectionError(ActFeedback.this, mAppTitle + " App requires Internet Connection to function." +
                    "\n\nTurn mobile data on?")) {
                if (mConnectivity.isGPSEnable(ActFeedback.this)) {

                }
            }
        }
        mTvVersion.setText("Version: " + mAppCode + "-" + mVersionName);
        final String sVersion = " (" + mAppCode + "-" + mVersionName + ")";
        mBtnEmail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    String aEmailList[] = {feedback_email};
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, aEmailList);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                            getString(R.string.feedback_for) + " " + mAppTitle + " " + sVersion);
                    emailIntent.setType("plain/text");
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(ActFeedback.this,
                            "Couldn't find an email client", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBtnAd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);

                    String aEmailList[] = {feedback_email};
                    //			String aEmailCCList[] = { "user3@fakehost.com","user4@fakehost.com"};
                    //			String aEmailBCCList[] = { "priteshpanchigar@gmail.com" };

                    emailIntent.putExtra(Intent.EXTRA_EMAIL, aEmailList);
                    //			emailIntent.putExtra(android.content.Intent.EXTRA_CC, aEmailCCList);
                    //			emailIntent.putExtra(android.content.Intent.EXTRA_BCC, aEmailBCCList);

                    emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                            getString(R.string.advertise_interest) +
                                    mAppTitle + sVersion);

                    emailIntent.setType("plain/text");
                    //			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "My message body.");

                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
//                    SSLog.e("SSA Feedback - Adverise", "Cannot find an email client", e);
                    Toast.makeText(ActFeedback.this,
                            "Couldn't find an email client", Toast.LENGTH_SHORT).show();
                }
            }

        });
        mTvWebSite.setText(feedback_website);
        mTvWebSite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String url = feedback_website;
                Uri uri = Uri.parse(url);
                uri.buildUpon().appendQueryParameter("fromApp", "SSA");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

        mBtnRateAndroid.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Uri uri = Uri.parse(feedback_Packagname);
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(ActFeedback.this,
                            "Couldn't launch the market", Toast.LENGTH_LONG).show();
                }
            }
        });

        mBtnFacebook.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String url = feedback_facebook;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(ActFeedback.this,
                            "Couldn't launch Facebook", Toast.LENGTH_LONG).show();
                }
            }
        });

        mBtnTwitter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    String url = feedback_twitter;
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(ActFeedback.this,
                            "Couldn't launch Twitter", Toast.LENGTH_LONG).show();
                }
            }
        });

        setupUI();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    void setupUI() {

    } // setupUI ends

    @Override
    public void finish() {
        super.finish();
    }

} //Feedback Activity

