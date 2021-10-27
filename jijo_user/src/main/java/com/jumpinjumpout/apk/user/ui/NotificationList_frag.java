package com.jumpinjumpout.apk.user.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.CNotification;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;
import com.jumpinjumpout.apk.user.Notification_Adapter;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class NotificationList_frag extends TripsList_frag {
    protected static final String TAG = "NotificationList_frag: ";
    Notification_Adapter adapter;
    String sNotificationValue;
    boolean isListEmpty = false;
    ListView mListView;
    ArrayList<CNotification> arrayCNotification;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notification_list_act, container,
                false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mListView = getListView();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        populateListFromNotifications();
        super.onResume();
    }

    void populateListFromNotifications() {
        try {
            sNotificationValue = MyApplication.getInstance()
                    .getPersistentPreference()
                    .getString(Constants_user.PREF_NOTIFICATION_LIST_SAVED, "");
            ArrayList<String> arraySNotification = new ArrayList<String>();
            arrayCNotification = new ArrayList<CNotification>();
            if (TextUtils.isEmpty(sNotificationValue)) {
                isListEmpty = true;
            } else {
                isListEmpty = true;
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                arraySNotification = new Gson().fromJson(sNotificationValue, type);
                for (int i = 0; i < arraySNotification.size() && i < Constants_user.MAX_NOTIFICATIONS; i++) {
                    arrayCNotification.add(new CNotification(arraySNotification.get(i).toString(), getActivity()));
                }
                if (arraySNotification.size() == 0) {
                    isListEmpty = true;
                } else {
                    mListView.setAdapter(new Notification_Adapter(getActivity(), arrayCNotification));
                }
            }
        } catch (Exception e) {
            SSLog.e(TAG, "init", e);
        }
    }
}
