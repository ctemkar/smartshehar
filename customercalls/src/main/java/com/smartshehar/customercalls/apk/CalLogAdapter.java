package com.smartshehar.customercalls.apk;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.smartshehar.customercalls.apk.ui.ShowCustomer_act;

import java.util.List;

/**
 * Created by ctemkar on 09/03/2016.
 * Phone log adapter
 */

public class CalLogAdapter extends RecyclerView.Adapter<CalLogAdapter.ContactViewHolder> {
    /*
    public static class ContactInfo {
        public String name;
        public String surname;
        public String email;
        public static final String NAME_PREFIX = "Name_";
        public static final String SURNAME_PREFIX = "Surname_";
        public static final String EMAIL_PREFIX = "email_";
    }
    */
    private List<CCallInfo> contactList;
    private Context context;

    public CalLogAdapter(List<CCallInfo> contactList) {
        this.contactList = contactList;
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    @Override
    public void onBindViewHolder(ContactViewHolder contactViewHolder, int i) {
        CCallInfo ci = contactList.get(i);
        contactViewHolder.tvTitle.setText(ci.getName());
        contactViewHolder.tvPhoneNo.setText(ci.getNationalNumber());
        contactViewHolder.tvAddressLine1.setText(ci.getAddress());
        contactViewHolder.tvLandmark.setText(ci.getLandmark1Disp());
        Gson gson = new Gson();
        String json = gson.toJson(ci);
        contactViewHolder.tvCallInfo.setText(json);

//        contactViewHolder.tvTitle.setText(g);
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).

                inflate(R.layout.customer_list_item, viewGroup, false);

        return new ContactViewHolder(itemView);
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        protected TextView tvTitle;
        protected TextView tvPhoneNo;
        protected TextView tvLandmark;
        protected TextView tvAddressLine1;
        protected TextView tvAddressLine2;
        protected TextView tvCallInfo; // hidden field storing callinfo object as json

        public ContactViewHolder(View v) {
            super(v);
            tvTitle = (TextView) v.findViewById(R.id.tvTitle);
            tvPhoneNo = (TextView) v.findViewById(R.id.tvPhoneNo);
            tvLandmark = (TextView) v.findViewById(R.id.tvLandmark1);
            tvAddressLine1 = (TextView) v.findViewById(R.id.tvAddressLine1);
            tvAddressLine2 = (TextView) v.findViewById(R.id.tvAddressLine2);
            tvCallInfo = (TextView) v.findViewById(R.id.tvCallInfo);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), tvTitle.getText(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(v.getContext(), ShowCustomer_act.class);
                    intent.putExtra("callinfo", tvCallInfo.getText());
                    intent.putExtra("phoneno", tvPhoneNo.getText());
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

}