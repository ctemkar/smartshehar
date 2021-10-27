package com.jumpinjumpout.apk.user;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.jumpinjumpout.apk.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
 
public class MyGroupsAdapter extends BaseAdapter implements Filterable {
//	private static final String TAG = "MyGroupsAdapter: ";
 
    // Declare Variables
    Context mContext;
    LayoutInflater inflater;
    private List<CMemberGroup> filteredData = null;
    private ArrayList<CMemberGroup> originalData = null;
    private Activity mActivity;
    DecimalFormat twoDForm = new DecimalFormat("#.##");
    Double dist;
    
    public MyGroupsAdapter(Context context, List<CMemberGroup> data, Activity activity) {
        mContext = context;
        mActivity = activity;
        this.filteredData = new ArrayList<CMemberGroup>();
        this.originalData = new ArrayList<CMemberGroup>();
        this.filteredData.addAll(data);
        inflater = LayoutInflater.from(mContext);
        

    }

    public class ViewHolder {
        TextView tvGroupName;
        TextView tvGroupDesc;
    }
 
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.address_item, parent, false);
            // Locate the TextViews in listview_item.xml
            holder.tvGroupName = (TextView) view.findViewById(R.id.tvAddressLine1);
            holder.tvGroupDesc = (TextView) view.findViewById(R.id.tvAddressLine2);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.tvGroupName.setText(filteredData.get(position).msGroupName);
        holder.tvGroupDesc.setText(filteredData.get(position).msGroupDescription);
        // Listen for ListView Item Click
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent returnIntent = new Intent();
                CMemberGroup oMemberGroup = filteredData.get(position);
                returnIntent.putExtra("group_name", oMemberGroup.msGroupName);
                mActivity.setResult(Activity.RESULT_OK ,returnIntent);        
                mActivity.finish();
           
            }
        });
 
        return view;
    }
 
    // Filter Class
    public MyGroupsAdapter(Context context, List<String> listDataHeader,
                HashMap<String, List<String>> listChildData) {
    }
     
        @Override
        public int getCount() {
            return filteredData.size();
        }
     
        @Override
        public CMemberGroup getItem(int position) {
            return filteredData.get(position);
        }
     
        @Override
        public long getItemId(int position) {
            return position;
        } 


@Override
public Filter getFilter() {

	return myFilter;
}

Filter myFilter = new Filter() {
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
		FilterResults filterResults = new FilterResults();   
		final ArrayList<CMemberGroup> list = originalData;
		filterResults.values = list;
		filterResults.count = list.size();
		return filterResults;
  }

  @SuppressWarnings("unchecked")
@Override
  protected void publishResults(CharSequence contraint, FilterResults results) {
	  filteredData = (ArrayList<CMemberGroup>) results.values;
      if (results.count > 0) {
    	  notifyDataSetChanged();
      } else {
          notifyDataSetInvalidated();
      }  
  }
 };

 

}