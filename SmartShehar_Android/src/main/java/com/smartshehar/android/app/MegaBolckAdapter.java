package com.smartshehar.android.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.smartshehar.android.app.ui.ActMegBlock;
import com.smartshehar.android.app.ui.ActMegaBlockMoreDetail;
import com.smartshehar.dashboard.app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;


/**
 * Created by asmita on 04-11-2015.
 */
public class MegaBolckAdapter extends RecyclerView.Adapter<MegaBolckAdapter.ViewHolder> {
    private static final String TAG = "MegaBolckAdapter";
    private int lastPosition = -1;
    Context ctx;
    Connectivity mConnectivity;

    public MegaBolckAdapter(Context ctx) {
        this.ctx = ctx;
        mConnectivity = new Connectivity();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtInfo, txtUrl;

        public ViewHolder(View v) {
            super(v);
            txtInfo = (TextView) v.findViewById(R.id.txtInfo);
            txtUrl = (TextView) v.findViewById(R.id.txtUrl);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_megablock, viewGroup, false);
        Log.d(TAG, "item_layout inflated");
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        String lineCode = "", fromDt = "", toDt, sMessage = "", sDirection = "", sLine = "",
                info = "";
        int iUp = 0, iDn = 0, iSlow = 0, iFast = 0;
        Date date = null;
        SpannableString content;
        SimpleDateFormat dateFormat,  outputDateFormat, outputFromTimeFormat, outputToTimeFormat;
//        DateFormat dateFormat;
        if (ActMegBlock.megaBlockArrayList.size() > 0) {
            CGlobals_trains.MegaBlock megablock = ActMegBlock.megaBlockArrayList.get(position);
            lineCode = megablock.lineCode;
            sMessage = megablock.sMessage;
            if (TextUtils.isEmpty(sMessage)) {
//                date = new Date();
                dateFormat = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT, Locale.ENGLISH);

                fromDt = megablock.fromDt;
                try {
                    date = dateFormat.parse(fromDt);
//                    dateFormat.format(date);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                outputDateFormat = new SimpleDateFormat("E, dd MMM", Locale.ENGLISH);
                outputFromTimeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                outputToTimeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                info = "<b>" + lineCode + "</b> - <b>" + outputDateFormat.format(date)
                        + "</b> at <b>" + outputFromTimeFormat.format(date)
                        + "</b> to <b>";
                toDt = megablock.toDt;
                try {
                    date = dateFormat.parse(toDt);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                info += outputToTimeFormat.format(date) + "</b> from <b>"
                        + megablock.fromstation + "</b> to <b>" + megablock.tostation + "</b> ";

                iDn = megablock.iDn;
                iUp = megablock.iUp;
                iFast = megablock.iFast;
                iSlow = megablock.iSlow;
                if (iDn == 1 && iUp == 1) {
                    sDirection = "(both direction)";
                    info += sDirection;
                }
                if (iDn == 1 && iUp == 0) {
                    sDirection = "(down)";
                    info += sDirection;
                }
                if (iDn == 0 && iUp == 1) {
                    sDirection = "(up)";
                    info += sDirection;
                }
                if (iSlow == 1 && iFast == 0) {
                    sLine = " on <b>Slow</b> line only";
                    info += sLine;
                }
                if (iSlow == 0 && iFast == 1) {
                    sLine = " on <b>Fast</b> line only";
                    info += sLine;
                }
                if (iSlow == 1 && iFast == 1) {
                    sLine = " on both <b>Slow </b> & <b>Fast</b> line";
                    info += sLine;
                }
                viewHolder.txtInfo.setText(Html.fromHtml(info));
            } else {
                info = "<b>" + lineCode + "</b> - " + sMessage;
                viewHolder.txtInfo.setText(Html.fromHtml(info));
            }
            viewHolder.txtUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mConnectivity.checkConnected(ctx)) {
                        //               		EasyTracker.getInstance(ActTrainDashboard.this).set(Fields.SCREEN_NAME, "Mega Block Screen");
                        if (!mConnectivity.connectionError(ctx)) {
                            Intent intent = new Intent(ctx, ActMegaBlockMoreDetail.class);
                            String link = ActMegBlock.megaBlockArrayList.get(position).link;
                            intent.putExtra("link",link);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            ctx.startActivity(intent);
                        }
                    } else {
                        Toast.makeText(ctx, "Please turn your internet connection on to use this feature", Toast.LENGTH_LONG).show();

                    }

                }
            });


        }
    }

    @Override
    public int getItemCount() {
        return ActMegBlock.megaBlockArrayList.size();
    }
}
