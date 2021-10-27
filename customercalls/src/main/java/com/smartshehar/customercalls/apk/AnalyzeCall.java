package com.smartshehar.customercalls.apk;


import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.smartshehar.customercalls.apk.ui.CallPopup_act;
import com.smartshehar.customercalls.apk.ui.ShowCustomer_act;

import org.json.JSONObject;

import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.SSLog_SS;

// Extend the class from BroadcastReceiver to listen when there is a incoming call
public class AnalyzeCall extends BroadcastReceiver {
    private static final String TAG = "AnalyzeCall: ";

    CustomDialog dialog;
    TelephonyManager telephonyManager;
    PhoneStateListener listener;
    Context context;
    //    String msCountryCode, msNationalNumber, msPhoneNo;
    static int iPreviousState = TelephonyManager.CALL_STATE_IDLE;
//    static String sLastNumber = "";
    private static CCallInfo moCallInfo;
    private static CCallHandle oCCallHandle ;
    private static boolean didPhoneRing = false;
    private static boolean isDialogDismissed = false;
    private static boolean haveShownNumber = false;
    WindowManager mWindowManager;
    static View mView;
    private CustomerCallsSQLiteDB ccDB;
    private long callDateTime;
    @Override
    public void onReceive(final Context context, Intent intent) {
        // If, the received action is not a type of "Phone_State", ignore it
        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            Log.d(TAG, "Got call");
            //  didPhoneRing = false;
            this.context = context;
            ccDB = new CustomerCallsSQLiteDB(context);
            oCCallHandle = new CCallHandle();
            // Fetch the number of incoming call
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            listener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    // get country-code from the phoneNumber
//                    String stateString = "";
                    switch (state) {
                        case TelephonyManager.CALL_STATE_IDLE:
//                            stateString = "Idle";
                            try {
                                mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                                if (mView != null) {
                                    mWindowManager.removeView(mView);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (iPreviousState == TelephonyManager.CALL_STATE_RINGING ||
                                    iPreviousState == TelephonyManager.CALL_STATE_OFFHOOK) {
                                if (didPhoneRing && !moCallInfo.IsIgnore()) { // only for incoming calls
                                    Intent intent = new Intent(context, ShowCustomer_act.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Gson gson = new Gson();
                                    String json = gson.toJson(moCallInfo);
                                    intent.putExtra("callinfo", json);
                                    context.getApplicationContext().startActivity(intent);
                                }
                            }
                            didPhoneRing = false;
                            iPreviousState = TelephonyManager.CALL_STATE_IDLE;

                            try {
                                dialog.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                           /* if (iPreviousState == TelephonyManager.CALL_STATE_RINGING) {
                                Intent intent = new Intent(context, ShowCustomer_act.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Gson gson = new Gson();
                                String json = gson.toJson(moCallInfo);
                                intent.putExtra("callinfo", json);
                                context.getApplicationContext().startActivity(intent);

                            }*/
                            try {
                                if (!TextUtils.isEmpty(moCallInfo.getNationalNumber())) {
                                    ccDB.setCallHandled(moCallInfo.getNationalNumber(), 1, callDateTime);
                                }
                                iPreviousState = TelephonyManager.CALL_STATE_OFFHOOK;
                                isDialogDismissed = false;
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                            break;
                        case TelephonyManager.CALL_STATE_RINGING:
                            //stateString = "Ringing";
                            didPhoneRing = true;
                            // Check if we need to handle or are already handling
                            if (iPreviousState != TelephonyManager.CALL_STATE_OFFHOOK
                                    && iPreviousState != TelephonyManager.CALL_STATE_RINGING) {
                                if (!TextUtils.isEmpty(incomingNumber)) {

                                    callDateTime =  System.currentTimeMillis();
                                    moCallInfo = new CCallInfo(true);
                                    moCallInfo.setPhoneNo(incomingNumber);

                                    searchNumber(incomingNumber, context);
                                    oCCallHandle.setMiCallLogDate(callDateTime);
                                    oCCallHandle.setMsPhoneNo(incomingNumber);
                                    oCCallHandle.setMiStorePhoneId(CGlobals_cc.mStorePhoneId);
                                    moCallInfo.setMiCustomerCallId((int) ccDB.addToCallLog(oCCallHandle));

                                }
                            }
                            iPreviousState = TelephonyManager.CALL_STATE_RINGING;
                            break;
                    }
                    // Toast.makeText(context, stateString, Toast.LENGTH_LONG).show();
                }
            };

            // Register the listener with the telephony manager
            telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    private void searchNumber(final String sPhoneNo, final Context context) {

        moCallInfo.setDateLastCall(callDateTime);
        if (isDialogDismissed)
            return;
        // First search in local list
        CCallInfo oCallInfo = ccDB.getRowByPhone(sPhoneNo);
        boolean numberFoundLocalList = false;
        if (oCallInfo != null && !TextUtils.isEmpty(oCallInfo.getNationalNumber()))
            numberFoundLocalList = true;

        // Found local, don't have to call server
        if (numberFoundLocalList) {
            moCallInfo = oCallInfo;
            if (!TextUtils.isEmpty(moCallInfo.getFirstName()) &&
                    !TextUtils.isEmpty(moCallInfo.getLastName()))
                showCallInfo(moCallInfo, context);
//            return;
        }
        //ccDB.addCustomer(moCallInfo);
//        sLastNumber = sPhoneNo;

        CGlobals_cc.getInstance().init(context);
        final boolean finalNumberFoundLocalList = numberFoundLocalList;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_cc.FIND_CUSTOMER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response);
                        if (!TextUtils.isEmpty(response.trim())) {
                            try {
                                Log.i(TAG, "Found customer");
                                JSONObject jResponse = new JSONObject(response);
                                CCallInfo oCallInfo = new CCallInfo(jResponse);
                                int cid = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "customer_id") ? -1
                                        : Integer.valueOf(jResponse.getString("customer_id"));
                                if (cid > 0) {
                                    moCallInfo = oCallInfo;
                                    moCallInfo.setPhoneNo(sPhoneNo);
                                    Log.d(TAG, "" + moCallInfo);
                                    if (!finalNumberFoundLocalList && !TextUtils.isEmpty(moCallInfo.getFirstName()) &&
                                            !TextUtils.isEmpty(moCallInfo.getLastName()))
                                        showCallInfo(moCallInfo, context);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(mContext, "Connection failed. Please try again", Toast.LENGTH_LONG).show();
                try {
                    SSLog_SS.e(TAG + " searchNumber", error.toString());
                } catch (Exception e) {
                    SSLog_SS.e(TAG + " searchNumber", e.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params;
/*                params.put("c", moCallInfo.getCountryCode());
                params.put("ph", moCallInfo.getNationalNumber());
                params.put("ic", String.valueOf(1));
                */
                params = CGlobals_cc.getInstance().common();
                params = moCallInfo.paramsPut(params);
                params.put("ic", String.valueOf(1));
                params.put("callhandled",String.valueOf(0));
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        Constants_cc.FIND_CUSTOMER_URL, context);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_cc.FIND_CUSTOMER_URL;
                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    Log.d(TAG, "url  " + url);

                } catch (Exception e) {
                    SSLog_SS.e(TAG + " FindCustomer - ", e.getMessage());
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().getRequestQueue(context).add(postRequest);


    }

    private void showCallInfo(final CCallInfo oCallInfo, final Context context) {
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isPhoneLocked = myKM.inKeyguardRestrictedInputMode();
//        moCallInfo.setDateLastCall(System.currentTimeMillis());
        if (isScreenOn(context) && !isPhoneLocked) {
/*
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
*/

            Intent intent = new Intent(context, CallPopup_act.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Gson gson = new Gson();
            String json = gson.toJson(moCallInfo);
            intent.putExtra("callinfo", json);
            context.startActivity(intent);

/*
                }
            }, 2000);
*/
        } else {
            showOnLockScreen(context);
        }

        ccDB.addCustomer(oCallInfo);
        // CGlobals_cc.getInstance().writeCallLog(this.context, maoCallLog);
    }


    class CustomDialog extends Dialog implements OnClickListener {

        public CustomDialog(Context context) {
            super(context);

        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.call_popup);
            //CGlobals_cc.getInstance().readCallLog(context);
        }

        @Override
        public void onClick(View v) {

            //           disconnectPhoneItelephony(context);
        }
    }


    boolean isScreenOn(Context context) {
        // If you use less than API20:
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.isScreenOn();
    }

    void showOnLockScreen(Context context) {
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
     /*   if (mView != null) {
            mWindowManager.removeView(mView);
        }*/

        LayoutInflater mInflater = LayoutInflater.from(context);
        mView = mInflater.inflate(R.layout.call_popup, null);
        ((TextView) mView.findViewById(R.id.tvTitle)).setText(moCallInfo.getName());
        ((TextView) mView.findViewById(R.id.tvPhoneNo)).setText(moCallInfo.getPhoneNo());
        mView.findViewById(R.id.tvPhoneNo).setVisibility(View.GONE);
        ((TextView) mView.findViewById(R.id.tvAddressLine1)).setText(moCallInfo.getAddress());
        ((TextView) mView.findViewById(R.id.tvLandmark1)).setText(moCallInfo.getLandmark1());
/*
        mView.findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mView.setVisibility(View.GONE);
            }
        });
*/
        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
/* | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON */,
                PixelFormat.RGBA_8888);

        mWindowManager.addView(mView, mLayoutParams);
    }



}

