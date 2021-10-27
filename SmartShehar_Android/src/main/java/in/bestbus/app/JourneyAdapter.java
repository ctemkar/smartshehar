package in.bestbus.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSApp;
import com.smartshehar.dashboard.app.SSLog;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;

public class JourneyAdapter extends ArrayAdapter<Journey> {
    private static String TAG = JourneyAdapter.class.getSimpleName();
    Activity mActivity;
    int layoutResourceId;
    ArrayList<Journey> stopList = null;
    String sBusLable, sDirection;
    Connectivity mConnectivity;
    CStop cStop;

    public JourneyAdapter(Activity activity, int layoutResourceId, ArrayList<Journey> data, String buslavel, String direction) {
        super(activity, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mActivity = activity;
        this.stopList = data;
        sBusLable = buslavel;
        sDirection = direction;
        mConnectivity = new Connectivity();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        JourneyHolder holder = null;
        if (view == null) {
            LayoutInflater inflater = ((Activity) mActivity).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);
            holder = new JourneyHolder();
            holder.sendlocation = (ImageView) view.findViewById(R.id.sendlocation);
            holder.tvStopSerial = (TextView) view.findViewById(R.id.tvStopSerial);
            holder.tvStopnameDetail = (TextView) view.findViewById(R.id.tvStopnameDetail);
            holder.tvLandmarkList = (TextView) view.findViewById(R.id.tvLandmarkList);
            view.setTag(holder);
        } else {
            holder = (JourneyHolder) view.getTag();
        }

        Journey journey = stopList.get(position);
        holder.tvStopSerial.setText(journey.stopSerial);
        holder.tvStopnameDetail.setText(journey.stopnameDetail);
        holder.tvLandmarkList.setText(journey.msLandmarkList);
        view.setBackgroundColor(journey.color);
        if (position == CGlobals_BA.iNearestStopIndex)
            view.setBackgroundColor(Color.CYAN);
        /*holder.tvStopnameDetail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(mActivity, ActFindBus.class);
                String sStopNameDetail = stopList.get(position).stopnameDetail;
                cStop = new CStop(stopList.get(position).miStopCode,stopList.get(position).stopnameDetail,
                        stopList.get(position).mdLat,stopList.get(position).mdLon);
                String json = new Gson().toJson(cStop);
                intent.putExtra("oStop", json);
                intent.putExtra("stopnamedetail", sStopNameDetail);
                CGlobals_BA.getInstance().getPersistentPreferenceEditor(mActivity).putString("PERF_CSTOP",
                        json);
                CGlobals_BA.getInstance().getPersistentPreferenceEditor(mActivity).putString("BUS_START_STOP_ADDRESS",
                        sStopNameDetail);
                CGlobals_BA.getInstance().getPersistentPreferenceEditor(mActivity).commit();
                mActivity.startActivity(intent);
                mActivity.finish();
            }
        });*/
        holder.sendlocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Journey journey = stopList.get(position);
                if (TextUtils.isEmpty(sBusLable)) {
                    Toast.makeText(mActivity, "Not Share Bus Stop", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    new AlertDialog.Builder(mActivity)
                            .setTitle("Share Bus Location")
                            .setMessage("Thank you for sharing the stop sharing location of the bus.\nThis will help other people")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!mConnectivity.checkConnected(mActivity)) {
                                        if (!mConnectivity.connectionError(mActivity, mActivity.getString(R.string.appTitle))) {
                                            sendUserAccess(sBusLable, sDirection, journey.stopSerial);
                                        }
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                }
            }
        });

        return view;
    }

    public void sendUserAccess(final String sShareBusLable, final String sShareDirection, final String sSharestopSerial) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_bus.DA_USER_ACCESS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        userAccessSucess(response, sShareBusLable, sShareDirection, sSharestopSerial);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(mActivity, "Connection failed. Please try again", Toast.LENGTH_LONG).show();
                SSLog.e(TAG, "sendUserAccess :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("app", Constants_bus.APP_TYPE);
                params = CGlobals_db.getInstance(mActivity).getBasicMobileParams(params,
                        Constants_bus.DA_USER_ACCESS_URL, mActivity);
                String delim = "";
                StringBuilder getParams = new StringBuilder();

                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";

                }
                return CGlobals_BA.getInstance().checkParams(params);
            }
        };
        SSApp.getInstance().addVolleyRequest(postRequest, true);

    } // sendUserAccess

    private void customDialog(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(mActivity);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mActivity.finish();
                    }
                });
        builder1.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void userAccessSucess(String response, final String sShareBusLable, final String sShareDirection, final String sSharestopSerial) {
        String sAppUserId = "-1";
        int iAppUserId = -1;
        try {
            if (response.trim().equals("-1")) {
                Toast.makeText(mActivity, "Your device not registered", Toast.LENGTH_LONG).show();
                customDialog("Your device not registered");
            }
            if (!response.trim().equals("-1")) {
                JSONObject jResponse = new JSONObject(response);
                sAppUserId = jResponse.isNull("appuser_id") ? "-1" : jResponse
                        .getString("appuser_id");
                if (!sAppUserId.trim().equals("-1")) {
                    iAppUserId = Integer.parseInt(sAppUserId);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(mActivity).putInt(Constants_lib_ss.PREF_APPUSERID, iAppUserId);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(mActivity).commit();
                    shareBusLocation(sShareBusLable, sShareDirection, sSharestopSerial);
                }
            }
        } catch (Exception e) {
            SSLog.e(TAG, "sendUserAccess Response: " + response, e);
            Toast.makeText(mActivity,
                    "Bad data received, try after some time", Toast.LENGTH_LONG)
                    .show();
            customDialog("Bad data received, try after some time");
        }

    } // userAccessSuccess

    public void shareBusLocation(final String sShareBusLable, final String sShareDirection, final String sSharestopSerial) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_bus.SHARE_BUS_LOCATION_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(mActivity, "Connection failed. Please try again", Toast.LENGTH_LONG).show();
                try {
                    SSLog.e(TAG, " callBusTrip", error.toString());
                } catch (Exception e) {
                    SSLog.e(TAG, " callBusTrip", e.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("buslabel", sShareBusLable);
                params.put("direction", sShareDirection);
                params.put("ss", sSharestopSerial);
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        Constants_bus.SHARE_BUS_LOCATION_URL, mActivity);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_bus.SHARE_BUS_LOCATION_URL;
                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog_SS.e(TAG + " callBusTrip", e.getMessage());
                }
                return CGlobals_BA.getInstance().checkParams(params);
            }
        };
        CGlobals_BA.getInstance().getRequestQueue(mActivity).add(postRequest);
    }

    static class JourneyHolder {
        TextView tvStopSerial;
        TextView tvStopnameDetail;
        TextView tvLandmarkList;
        ImageView sendlocation;
    }
}
