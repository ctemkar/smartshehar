package com.smartshehar.dashboard.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.smartshehar.dashboard.app.R;

public class ActTrafficCategorySelection extends AppCompatActivity {
    View.OnClickListener onClickListener;
    String sIssueDescription, sIssueitemCode;
    boolean imageVerification, vehicleNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_traffic_category_selection);
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.PNPZ:
                        sIssueitemCode = "PNPZ";
                        sIssueDescription = getString(R.string.PNPZ);
                        break;
                    case R.id.DP:
                        sIssueitemCode = "DP";
                        sIssueDescription = getString(R.string.DP);
                        break;
                    case R.id.PFGT:
                        sIssueitemCode = "PFGT";
                        sIssueDescription = getString(R.string.PFGT);
                        break;
                    case R.id.PFP:
                        sIssueitemCode = "PFP";
                        sIssueDescription = getString(R.string.PFP);
                        break;
                    case R.id.OEPV:
                        sIssueitemCode = "OEPV";
                        sIssueDescription = getString(R.string.OEPV);
                        break;
                    case R.id.PBS:
                        sIssueitemCode = "PBS";
                        sIssueDescription = getString(R.string.PBS);
                        break;
                    case R.id.PCO:
                        sIssueitemCode = "PCO";
                        sIssueDescription = getString(R.string.PCO);
                        break;
                    case R.id.PRPS:
                        sIssueitemCode = "PRPS";
                        sIssueDescription = getString(R.string.PRPS);
                        break;
                    case R.id.DRBT:
                        sIssueitemCode = "DRBT";
                        sIssueDescription = getString(R.string.DRBT);
                        break;
                    case R.id.SNSZ:
                        sIssueitemCode = "SNSZ";
                        sIssueDescription = getString(R.string.SNSZ);
                        break;
                    case R.id.TBTM:
                        sIssueitemCode = "TBTM";
                        sIssueDescription = getString(R.string.TBTM);
                        break;
                    case R.id.VLMR:
                        sIssueitemCode = "VLMR";
                        sIssueDescription = getString(R.string.VLMR);
                        break;
                    case R.id.RD:
                        sIssueitemCode = "RD";
                        sIssueDescription = getString(R.string.RD);
                        break;
                    case R.id.SJ:
                        sIssueitemCode = "SJ";
                        sIssueDescription = getString(R.string.SJ);
                        break;
                    case R.id.LC:
                        sIssueitemCode = "LC";
                        sIssueDescription = getString(R.string.LC);
                        break;
                    case R.id.IT:
                        sIssueitemCode = "IT";
                        sIssueDescription = getString(R.string.IT);
                        break;
                    case R.id.DAOW:
                        sIssueitemCode = "DAOW";
                        sIssueDescription = getString(R.string.DAOW);
                        break;
                    case R.id.OS:
                        sIssueitemCode = "OS";
                        sIssueDescription = getString(R.string.OS);
                        break;
                    case R.id.MTSH:
                        sIssueitemCode = "MTSH";
                        sIssueDescription = getString(R.string.MTSH);
                        break;
                    case R.id.SE:
                        sIssueitemCode = "SE";
                        sIssueDescription = getString(R.string.SE);
                        break;
                    case R.id.DG:
                        sIssueitemCode = "DG";
                        sIssueDescription = getString(R.string.DG);
                        break;
                    case R.id.DN:
                        sIssueitemCode = "DN";
                        sIssueDescription = getString(R.string.DN);
                        break;
                    case R.id.TB:
                        sIssueitemCode = "TB";
                        sIssueDescription = getString(R.string.TB);
                        break;
                    case R.id.PD:
                        sIssueitemCode = "PD";
                        sIssueDescription = getString(R.string.PD);
                        break;
                    case R.id.IPV:
                        sIssueitemCode = "IPV";
                        sIssueDescription = getString(R.string.IPV);
                        break;
                    case R.id.NPR:
                        sIssueitemCode = "NPR";
                        sIssueDescription = getString(R.string.NPR);
                        break;
                    case R.id.NOWR:
                        sIssueitemCode = "NOWR";
                        sIssueDescription = getString(R.string.NOWR);
                        break;
                    case R.id.HV:
                        sIssueitemCode = "HV";
                        sIssueDescription = getString(R.string.HV);
                        break;
                    case R.id.SBRJ:
                        sIssueitemCode = "SBRJ";
                        sIssueDescription = getString(R.string.SBRJ);
                        break;
                    case R.id.RP:
                        sIssueitemCode = "RP";
                        sIssueDescription = getString(R.string.RP);
                        break;
                    case R.id.RM:
                        sIssueitemCode = "RM";
                        sIssueDescription = getString(R.string.RM);
                        break;
                    case R.id.MHD:
                        sIssueitemCode = "MHD";
                        sIssueDescription = getString(R.string.MHD);
                        break;
                    case R.id.RB:
                        sIssueitemCode = "RB";
                        sIssueDescription = getString(R.string.RB);
                        break;
                    case R.id.BTR:
                        sIssueitemCode = "BTR";
                        sIssueDescription = getString(R.string.BTR);
                        break;
                    case R.id.BND:
                        sIssueitemCode = "BND";
                        sIssueDescription = getString(R.string.BND);
                        break;
                    default:
                        break;

                }
                if (sIssueDescription.equals("Signal Jumping") ||

                        sIssueDescription.equals("Lane Cutting") ||

                        sIssueDescription.equals("Illegal Turn") ||

                        sIssueDescription.equals("Driving against One-way") ||

                        sIssueDescription.equals("Overspeeding") ||

                        sIssueDescription.equals("Multi-toned or Shrill Horn") ||

                        sIssueDescription.equals("Smokey Exhaust") ||

                        sIssueDescription.equals("Dark Glasses") ||

                        sIssueDescription.equals("Dangerous goods") ||

                        sIssueDescription.equals("Traffic Bottlenecks") ||

                        sIssueDescription.equals("Pedestrians on road") ||

                        sIssueDescription.equals("Illegally parked vehicles") ||

                        sIssueDescription.equals("Need Parking Restrictions") ||

                        sIssueDescription.equals("Need One-Way Restriction") ||

                        sIssueDescription.equals("Need curbs on heavy vehicles") ||

                        sIssueDescription.equals("School Bus related jams") ||

                        sIssueDescription.equals("Refusing to ply") ||

                        sIssueDescription.equals("Rigged Meter") ||

                        sIssueDescription.equals("Meter Half Down") ||

                        sIssueDescription.equals("Rude Behavior") ||

                        sIssueDescription.equals("Breaking Traffic Rules") ||

                        sIssueDescription.equals("Badge Not Displayed")


                        ) {
                    imageVerification = false;
                } else {
                    imageVerification = true;
                }
                if (sIssueDescription.equals("Traffic Bottlenecks") ||

                        sIssueDescription.equals("Pedestrians on road") ||

                        sIssueDescription.equals("Illegally parked vehicles") ||

                        sIssueDescription.equals("Need Parking Restrictions") ||

                        sIssueDescription.equals("Need One-Way Restriction") ||

                        sIssueDescription.equals("Need curbs on heavy vehicles") ||

                        sIssueDescription.equals("School Bus related jams")) {

                    vehicleNumber = false;
                } else {
                    vehicleNumber = true;
                }
                Bundle args = new Bundle();
                args.putString("issueitemcode", sIssueitemCode);
                args.putString("issueDescription", sIssueDescription);
                args.putBoolean("imageverification", imageVerification);
                args.putBoolean("vehiclenumber",vehicleNumber);

                Intent intent = new Intent();
                intent.putExtra("data", args);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        };

        findViewById(R.id.PNPZ).setOnClickListener(onClickListener);
        findViewById(R.id.DP).setOnClickListener(onClickListener);
        findViewById(R.id.PFGT).setOnClickListener(onClickListener);
        findViewById(R.id.PFP).setOnClickListener(onClickListener);
        findViewById(R.id.OEPV).setOnClickListener(onClickListener);
        findViewById(R.id.PBS).setOnClickListener(onClickListener);
        findViewById(R.id.PCO).setOnClickListener(onClickListener);
        findViewById(R.id.PRPS).setOnClickListener(onClickListener);
        findViewById(R.id.DRBT).setOnClickListener(onClickListener);
        findViewById(R.id.SNSZ).setOnClickListener(onClickListener);
        findViewById(R.id.TBTM).setOnClickListener(onClickListener);
        findViewById(R.id.VLMR).setOnClickListener(onClickListener);
        findViewById(R.id.RD).setOnClickListener(onClickListener);
        findViewById(R.id.SJ).setOnClickListener(onClickListener);
        findViewById(R.id.LC).setOnClickListener(onClickListener);
        findViewById(R.id.IT).setOnClickListener(onClickListener);
        findViewById(R.id.DAOW).setOnClickListener(onClickListener);
        findViewById(R.id.OS).setOnClickListener(onClickListener);
        findViewById(R.id.MTSH).setOnClickListener(onClickListener);
        findViewById(R.id.SE).setOnClickListener(onClickListener);
        findViewById(R.id.DG).setOnClickListener(onClickListener);
        findViewById(R.id.DN).setOnClickListener(onClickListener);
        findViewById(R.id.TB).setOnClickListener(onClickListener);
        findViewById(R.id.PD).setOnClickListener(onClickListener);
        findViewById(R.id.IPV).setOnClickListener(onClickListener);
        findViewById(R.id.NPR).setOnClickListener(onClickListener);
        findViewById(R.id.NOWR).setOnClickListener(onClickListener);
        findViewById(R.id.HV).setOnClickListener(onClickListener);
        findViewById(R.id.SBRJ).setOnClickListener(onClickListener);
        findViewById(R.id.RP).setOnClickListener(onClickListener);
        findViewById(R.id.RM).setOnClickListener(onClickListener);
        findViewById(R.id.MHD).setOnClickListener(onClickListener);
        findViewById(R.id.RB).setOnClickListener(onClickListener);
        findViewById(R.id.BTR).setOnClickListener(onClickListener);
        findViewById(R.id.BND).setOnClickListener(onClickListener);
    }
}
