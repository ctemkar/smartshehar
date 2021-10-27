package com.jumpinjumpout.apk.user.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;

/**
 * Created by user pc on 22-04-2015.
 */
public class Notification_act extends Activity {

    private Switch mButtonToggle, mButtonToggleNotification;//, mIvGroupNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);
        refresh();
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mButtonToggle = (Switch) findViewById(R.id.btntoggle);

        mButtonToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bn) {

                MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(Constants_user.PREF_SET_SWITCH_FLAG, bn);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();

                CGlobals_user.getInstance().sendFlag(Constants_user.COL_CAB_ALERT, bn, Notification_act.this);

                if (bn == true) {
                } else {
                }
            }
        });

        mButtonToggleNotification = (Switch) findViewById(R.id.btntoggleNotification);

        mButtonToggleNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(Constants_user.PREF_SET_SWITCH_OPEN_TRIPS, b);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                CGlobals_user.getInstance().sendFlag(Constants_user.COL_OPEN_NOTIFICATION_ALERT, b, Notification_act.this);
            }
        });
        MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(Constants_user.PREF_NOTIFICATION_CLEAR_FLAG, true);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean switchOpenTrips = false;
        boolean switchCabTrips = false;
        int i = MyApplication.getInstance().getPersistentPreference().getInt(Constants_user.PERF_STRANGER_NOTIFICATION_ALLOW, -1);
        int j = MyApplication.getInstance().getPersistentPreference().getInt(Constants_user.PERF_SHOW_CAB_NOTIFICATION, -1);
        MyApplication.getInstance().getPersistentPreferenceEditor().putInt(Constants_user.PERF_STRANGER_NOTIFICATION_ALLOW, -1);
        MyApplication.getInstance().getPersistentPreferenceEditor().putInt(Constants_user.PERF_SHOW_CAB_NOTIFICATION, -1);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        if (i == 1) {
            switchOpenTrips = true;
        } else if (i == -1) {
            switchOpenTrips =
                    MyApplication.getInstance().getPersistentPreference().getBoolean(Constants_user.PREF_SET_SWITCH_OPEN_TRIPS, false);
        }
        if (j == 1) {
            switchCabTrips = true;
        } else if (j == -1) {
            switchCabTrips =
                    MyApplication.getInstance().getPersistentPreference().getBoolean(Constants_user.PREF_SET_SWITCH_FLAG, false);
        }
        mButtonToggleNotification = (Switch) findViewById(R.id.btntoggleNotification);
        mButtonToggleNotification.setChecked(switchOpenTrips);
        mButtonToggle = (Switch) findViewById(R.id.btntoggle);
        mButtonToggle.setChecked(switchCabTrips);
        MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(Constants_user.PREF_NOTIFICATION_CLEAR_FLAG, true);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();

    }

    void removeNotificationFragment() {
        NotificationList_frag fragment = (NotificationList_frag) getFragmentManager()
                .findFragmentById(R.id.notification_list_fragment_container);
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.remove(fragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    void addNotificationFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        NotificationList_frag fragment = new NotificationList_frag();
        fragmentTransaction.add(R.id.notification_list_fragment_container,
                fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void refresh() {
        removeNotificationFragment();
        addNotificationFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_notification, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(Notification_act.this, Dashboard_act.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.from(Notification_act.this)
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    NavUtils.navigateUpTo(Notification_act.this, upIntent);
                }
                return true;
            case R.id.menu_clear:
                clearNotification();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearNotification() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Notification_act.this);
        alertDialog.setTitle("Clear notifications");
        alertDialog.setMessage("This will clear all your notifications.\nThis action cannot be reversed.");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                MyApplication.getInstance()
                        .getPersistentPreferenceEditor()
                        .putString(Constants_user.PREF_NOTIFICATION_LIST_SAVED, "");
                MyApplication.getInstance()
                        .getPersistentPreferenceEditor().commit();
                refresh();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        if (!Notification_act.this.isFinishing()) {
            alertDialog.show();
        }
    }
}
