package in.bestbus.app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartshehar.dashboard.app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import in.bestbus.app.ui.ActSearchBus;

public class BusListArrayAdapter extends BaseAdapter {

    // Declare Variables
    Context mContext;
    LayoutInflater inflater;
    private List<CBus> busList = null;
    private ArrayList<CBus> aoBus;
    private Activity mActivity;
    private ActSearchBus.ListClickListener mClickListener = null;

    public BusListArrayAdapter(Context context, List<CBus> busList, Activity activity,ActSearchBus.ListClickListener listener) {
        mContext = context;
        mActivity = activity;
        this.busList = busList;
        inflater = LayoutInflater.from(mContext);
        this.aoBus = new ArrayList<CBus>();
        this.aoBus.addAll(busList);
        mClickListener = listener;
    }

    public BusListArrayAdapter(Context context, List<String> listDataHeader,
                               HashMap<String, List<String>> listChildData) {
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.busno_listview_item, parent, false);
            // Locate the TextViews in listview_item.xml
            holder.sBusNo = (TextView) view.findViewById(R.id.tvBusNo);
            holder.sFromTo = (TextView) view.findViewById(R.id.tvFromTo);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.sBusNo.setText(busList.get(position).msBusLabel);
        holder.sFromTo.setText(busList.get(position).msFirstStop
                + " to " + busList.get(position).msLastStop);
//        holder.sTo.setText(busList.get(position).getPopulation());

        // Listen for ListView Item Click
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mClickListener != null)
                    mClickListener.onListClick(position);
            }
        });

        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        busList.clear();
        if (charText.length() == 0) {
            busList.addAll(aoBus);
        } else {
            for (CBus wp : aoBus) {
                if (wp.msBusLabel.toLowerCase(Locale.getDefault()).contains(charText)) {
                    busList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return busList.size();
    }

    @Override
    public CBus getItem(int position) {
        return busList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        TextView sBusNo;
        TextView sFromTo;
        TextView sTo;
    }


}