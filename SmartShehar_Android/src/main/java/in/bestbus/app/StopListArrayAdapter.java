package in.bestbus.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StopListArrayAdapter extends BaseAdapter {

    // Declare Variables
    private final static String TAG = "StopListArrayAdapter: ";
    Context mContext;
    LayoutInflater inflater;
    DecimalFormat twoDForm = new DecimalFormat("#.##");
    Double dist;
    private List<CStop> stopList = null;
    private ArrayList<CStop> aoStop;
    private Activity mActivity;
    CStop cStop;

    public StopListArrayAdapter(Context context, List<CStop> stopList, Activity activity) {
        mContext = context;
        mActivity = activity;
        this.stopList = stopList;
        inflater = LayoutInflater.from(mContext);
        this.aoStop = new ArrayList<>();
        this.aoStop.addAll(stopList);
    }

    @SuppressLint("SetTextI18n")
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.stop_listview_item, parent, false);
            // Locate the TextViews in listview_item.xml
            holder.tvStopDetail = (TextView) view.findViewById(R.id.tvStopNameDetail);
            holder.tvLandmarkList = (TextView) view.findViewById(R.id.tvLandmarkList);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        try {
            dist = Double.valueOf(twoDForm.format(stopList.get(position).mdDist));
        } catch (Exception e) {
            dist = 0.0;
            SSLog.e(TAG, "getView:", e);
        }
        holder.tvStopDetail.setText(stopList.get(position).msStopNameDetail +
                (dist > 0 ? " (" + stopList.get(position).mdDist / 1000 + ")" : ""));
        holder.tvStopDetail.setTextColor(stopList.get(position).miStopColor);
        holder.tvLandmarkList.setText(stopList.get(position).msLandmarkList);

        // Listen for ListView Item Click
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                cStop = (CStop) stopList.get(position);
                String json = new Gson().toJson(cStop);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("oStop", json);
                returnIntent.putExtra("stopnamedetail", cStop.msStopNameDetail);
                mActivity.setResult(Activity.RESULT_OK, returnIntent);
                mActivity.finish();

            }
        });

        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault()).replaceAll("[^A-Za-z0-9]", "");
        //       String sStopNameDetail = "";
        stopList.clear();
        if (charText.length() == 0) {
            stopList.addAll(aoStop);
        } else {
            for (CStop wp : aoStop) {
//            	sStopNameDetail = wp.msStopNameDetail.toLowerCase(Locale.getDefault()); // .replaceAll("(?i)[\\saeiou]", "");
//                if ((wp.msStopNameDetail.toLowerCase(Locale.getDefault()) +
//                		(!TextUtils.isEmpty(wp.msLandmarkList) ? 
//                				wp.msLandmarkList.toLowerCase(Locale.getDefault()) :
//                					"")).contains(charText)) 
                if (wp.msStopNameDetailLandmarklistId.contains(charText)) {
                    stopList.add(wp);
                    // stopList.add(new CStop(wp.miStopId, wp.miStopCode, wp.msStopNameDetail, wp.lat, wp.lon));
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return stopList.size();
    }

    @Override
    public CStop getItem(int position) {
        return stopList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        TextView tvStopDetail;
        TextView tvLandmarkList;
    }


}