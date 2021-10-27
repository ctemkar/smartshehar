package com.jumpinjumpout.apk.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.ui.ShareTripTrackDriver_act;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;


/**
 * ****** Adapter class extends with BaseAdapter and implements with OnClickListener ***********
 */
public class Notification_Adapter extends BaseAdapter implements OnClickListener {
    private static String TAG = "Notification_Adapter";
    /**
     * ******** Declare Used Variables ********
     */
    //        private ListFragment listfragment;
//         private Activity activity;
    private List<CNotification> data;
    //         private static LayoutInflater inflater=null;
    public Resources res;
    CNotification cNotificationTrip = null;
    Context context;
    String msFromAddress;
    int i = 0;
    JSONObject jMsg = null;
    String sDateTimeValue;

    /**
     * **********  CustomAdapter Constructor ****************
     */
    public Notification_Adapter(Context c, ArrayList<CNotification> d) {

        /********** Take passed values **********/
        this.context = c;

        data = d;


    }

    /**
     * ***** What is the size of Passed Arraylist Size ***********
     */
    public int getCount() {

        if (data.size() <= 0)
            return 1;
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * ****** Create a holder Class to contain inflated xml file elements ********
     */
    public static class ViewHolder {


        TextView mTvDescription, mTvCreatedBy;
        //        public TextView mTvTripStatus;
        public ImageView mTvClickTrack;
        RelativeLayout mRlnear;

    }

    /**
     * *** Depends upon data size called for each row , Create each ListView row ****
     */
    public View getView(final int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            vi = mInflater.inflate(R.layout.notification_list_item, parent, false);

            holder = new ViewHolder();
            holder.mTvClickTrack = (ImageView) vi.findViewById(R.id.tvClickTrack);
            holder.mTvCreatedBy = (TextView) vi.findViewById(R.id.tvNotificationMessage);
            holder.mRlnear = (RelativeLayout) vi.findViewById(R.id.rinear);
            /************  Set holder with LayoutInflater ************/

            vi.setTag(holder);

        } else
            holder = (ViewHolder) vi.getTag();

        if (data.size() <= 0) {

        } else {


            /***** Get each Model object from Arraylist ********/
            cNotificationTrip = null;
            cNotificationTrip = (CNotification) data.get(position);
            holder.mTvCreatedBy.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(cNotificationTrip.getPlannedStartDateTime())) {
                String sDateTime = cNotificationTrip.getPlannedStartDateTime();
                StringTokenizer tk = new StringTokenizer(sDateTime);

                String date = tk.nextToken();  // <---  yyyy-mm-dd
                String time = tk.nextToken();  // <---  hh:mm:ss

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                String sTodayDate = df.format(c.getTime());

                c.add(Calendar.DATE, -1);
                String sYesterdayDate = df.format(c.getTime());


                long convertedLong = 0;
                if (date.equals(sTodayDate)) {

                    sDateTimeValue = "<b>" + time;
                } else if (date.equals(sYesterdayDate)) {
                    date = "Yesterday";
                    sDateTimeValue = "<b>" + time + "</b> " + date;
                } else {
                    sDateTimeValue = "<b>" + time + "</b> " + date;
                }

            }

            if (cNotificationTrip.isTripCommercial().equals(Constants_lib.TRIP_TYPE_COMMERCIAL)) {
                holder.mTvCreatedBy.setText(Html.fromHtml(cNotificationTrip.getNotificationText()));
                holder.mTvCreatedBy.setVisibility(View.VISIBLE);
                holder.mRlnear.setBackgroundColor(Color.parseColor("#F5DEB3"));
            } else {
                holder.mTvCreatedBy.setText(Html.fromHtml(cNotificationTrip.getNotificationText()));
                holder.mTvCreatedBy.setVisibility(View.VISIBLE);
                holder.mRlnear.setBackgroundColor(Color.parseColor("#FAFAFA"));
            }

            holder.mTvCreatedBy.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {

                        cNotificationTrip = null;
                        cNotificationTrip = (CNotification) data.get(position);
                        MyApplication.getInstance().getPersistentPreferenceEditor().
                                putInt(Constants_user.PREF_TRIP_ID, cNotificationTrip.getTripId());
                        MyApplication.getInstance().getPersistentPreferenceEditor().
                                putBoolean(Constants_user.HAS_DESTINATION, false);
                        MyApplication.getInstance().getPersistentPreferenceEditor().
                                putBoolean(Constants_user.GET_A_CAB_VALUE, false);
                        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                        Intent i = new Intent(context, ShareTripTrackDriver_act.class);
                        i.putExtra("notificationTripType", cNotificationTrip.isTripCommercial());
                        i.putExtra("notificationValue", "1");
                        i.putExtra("jdriver", cNotificationTrip.getInfo());
                        i.putExtra("driveremail", cNotificationTrip.getDriverEmail());
                        i.putExtra("countrycode", cNotificationTrip.getCountryCode());
                        i.putExtra("phoneno", cNotificationTrip.getPhoneNo());
                        context.startActivity(i);
                        SSLog.d(TAG, cNotificationTrip.msHtmlDescription);
                    } catch (Exception e) {
                        SSLog.d(TAG,
                                " OnItemClick: probably no items in list " + e.getMessage());
                    }
                }
            });

        }
        return vi;
    }

    @Override
    public void onClick(View v) {
        Log.v("CustomAdapter", "=====Row button clicked=====");
    }


    public static String getTime(Calendar cal) {
        return "" + cal.get(Calendar.HOUR_OF_DAY) + ":" +
                (cal.get(Calendar.MINUTE)) + ":" + cal.get(Calendar.SECOND);
    }
}