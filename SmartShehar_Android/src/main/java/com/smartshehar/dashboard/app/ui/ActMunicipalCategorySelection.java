package com.smartshehar.dashboard.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.smartshehar.dashboard.app.R;

public class ActMunicipalCategorySelection extends AppCompatActivity {

    View.OnClickListener onClickListener;
    String sIssueDescription,sIssueitemCode;
    boolean imageVerification, vehicleNumber=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_municipal_category_selection);


        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.MF:
                        sIssueitemCode = "MF";
                        sIssueDescription =getString(R.string.MF);
                        break;
                    case R.id.UBF:
                        sIssueitemCode = "UBF";
                        sIssueDescription =getString(R.string.UBF);
                        break;
                    case R.id.UBP:
                        sIssueitemCode = "UBP";
                        sIssueDescription =getString(R.string.UBP);
                        break;
                    case R.id.EF:
                        sIssueitemCode = "EF";
                        sIssueDescription =getString(R.string.EF);
                        break;
                    case R.id.DLF:
                        sIssueitemCode = "DLF";
                        sIssueDescription =getString(R.string.DLF);
                        break;
                    case R.id.POR:
                        sIssueitemCode = "POR";
                        sIssueDescription =getString(R.string.POR);
                        break;
                    case R.id.URS:
                        sIssueitemCode = "URS";
                        sIssueDescription =getString(R.string.URS);
                        break;
                    case R.id.DUR:
                        sIssueitemCode = "DUR";
                        sIssueDescription =getString(R.string.DUR);
                        break;
                    case R.id.SRW:
                        sIssueitemCode = "SRW";
                        sIssueDescription =getString(R.string.SRW);
                        break;
                    case R.id.AG:
                        sIssueitemCode = "AG";
                        sIssueDescription =getString(R.string.AG);
                        break;
                    case R.id.GNP:
                        sIssueitemCode = "GNP";
                        sIssueDescription =getString(R.string.GNP);
                        break;
                    case R.id.SNS:
                        sIssueitemCode = "SNS";
                        sIssueDescription =getString(R.string.SNS);
                        break;
                    case R.id.CDD:
                        sIssueitemCode = "CDD";
                        sIssueDescription =getString(R.string.CDD);
                        break;
                    case R.id.GBB:
                        sIssueitemCode = "GBB";
                        sIssueDescription =getString(R.string.GBB);
                        break;
                    case R.id.ODSL:
                        sIssueitemCode = "ODSL";
                        sIssueDescription =getString(R.string.ODSL);
                        break;
                    case R.id.COSL:
                        sIssueitemCode = "COSL";
                        sIssueDescription =getString(R.string.COSL);
                        break;
                    case R.id.ODU:
                        sIssueitemCode = "ODU";
                        sIssueDescription =getString(R.string.ODU);
                        break;
                    case R.id.DD:
                        sIssueitemCode = "DD";
                        sIssueDescription =getString(R.string.DD);
                        break;
                    case R.id.UPR:
                        sIssueitemCode = "UPR";
                        sIssueDescription =getString(R.string.UPR);
                        break;
                    case R.id.MS:
                        sIssueitemCode = "MS";
                        sIssueDescription =getString(R.string.MS);
                        break;
                    case R.id.SDG:
                        sIssueitemCode = "SDG";
                        sIssueDescription =getString(R.string.SDG);
                        break;
                    case R.id.PGM:
                        sIssueitemCode = "PGM";
                        sIssueDescription =getString(R.string.PGM);
                        break;
                    case R.id.IFVS:
                        sIssueitemCode = "IFVS";
                        sIssueDescription =getString(R.string.IFVS);
                        break;
                    case R.id.IFS:
                        sIssueitemCode = "IFS";
                        sIssueDescription =getString(R.string.IFS);
                        break;
                    case R.id.HTMF:
                        sIssueitemCode = "HTMF";
                        sIssueDescription =getString(R.string.HTMF);
                        break;
                    case R.id.IER:
                        sIssueitemCode = "IER";
                        sIssueDescription =getString(R.string.IER);
                        break;
                    case R.id.IC:
                        sIssueitemCode = "IC";
                        sIssueDescription =getString(R.string.IC);
                        break;
                    case R.id.HBN:
                        sIssueitemCode = "HBN";
                        sIssueDescription =getString(R.string.HBN);
                        break;
                    case R.id.BEN:
                        sIssueitemCode = "BEN";
                        sIssueDescription =getString(R.string.BEN);
                        break;
                    case R.id.RE:
                        sIssueitemCode = "RE";
                        sIssueDescription =getString(R.string.RE);
                        break;
                    case R.id.ASE:
                        sIssueitemCode = "ASE";
                        sIssueDescription =getString(R.string.ASE);
                        break;
                    case R.id.ISLT:
                        sIssueitemCode = "ISLT";
                        sIssueDescription =getString(R.string.ISLT);
                        break;
                    case R.id.PSI:
                        sIssueitemCode = "PSI";
                        sIssueDescription =getString(R.string.PSI);
                        break;
                    case R.id.TRA:
                        sIssueitemCode = "TRA";
                        sIssueDescription =getString(R.string.TRA);
                        break;
                    case R.id.FRH:
                        sIssueitemCode = "FRH";
                        sIssueDescription =getString(R.string.FRH);
                        break;
                    case R.id.WS:
                        sIssueitemCode = "WS";
                        sIssueDescription =getString(R.string.WS);
                        break;
                    case R.id.LWP:
                        sIssueitemCode = "LWP";
                        sIssueDescription =getString(R.string.LWP);
                        break;
                    case R.id.LWL:
                        sIssueitemCode = "LWL";
                        sIssueDescription =getString(R.string.LWL);
                        break;
                    case R.id.CWS:
                        sIssueitemCode = "CWS";
                        sIssueDescription =getString(R.string.CWS);
                        break;
                    case R.id.IEC:
                        sIssueitemCode = "IEC";
                        sIssueDescription =getString(R.string.IEC);
                        break;
                    case R.id.IES:
                        sIssueitemCode = "IES";
                        sIssueDescription =getString(R.string.IES);
                        break;
                    case R.id.EG:
                        sIssueitemCode = "EG";
                        sIssueDescription =getString(R.string.EG);
                        break;
                    case R.id.ASEA:
                        sIssueitemCode = "ASEA";
                        sIssueDescription =getString(R.string.ASEA);
                        break;
                    case R.id.PMPG:
                        sIssueitemCode = "PMPG";
                        sIssueDescription =getString(R.string.PMPG);
                        break;
                    case R.id.GTF:
                        sIssueitemCode = "GTF";
                        sIssueDescription =getString(R.string.GTF);
                        break;
                    case R.id.TF:
                        sIssueitemCode = "TF";
                        sIssueDescription =getString(R.string.TF);
                        break;
                    case R.id.DPT:
                        sIssueitemCode = "DPT";
                        sIssueDescription =getString(R.string.DPT);
                        break;
                    case R.id.IDT:
                        sIssueitemCode = "IDT";
                        sIssueDescription =getString(R.string.IDT);
                        break;
                    case R.id.MGD:
                        sIssueitemCode = "MGD";
                        sIssueDescription =getString(R.string.MGD);
                        break;
                    case R.id.FG:
                        sIssueitemCode = "FG";
                        sIssueDescription =getString(R.string.FG);
                        break;
                    case R.id.TT:
                        sIssueitemCode = "TT";
                        sIssueDescription =getString(R.string.TT);
                        break;
                    case R.id.SC:
                        sIssueitemCode = "SC";
                        sIssueDescription =getString(R.string.SC);
                        break;
                    default:
                        break;

                }

                if (
                        sIssueDescription.equals("Sweepers not being seen") ||

                        sIssueDescription.equals("Mosquitos") ||

                        sIssueDescription.equals("Stray dogs gangs") ||

                        sIssueDescription.equals("Pigeon Menace") ||

                        sIssueDescription.equals("Rampant Eveteasing") ||

                        sIssueDescription.equals("Anti-Social Elements") ||

                        sIssueDescription.equals("Insufficient Street Lighting") ||

                        sIssueDescription.equals("Purse Snatching Incidents") ||

                        sIssueDescription.equals("Thefts/Robberies in the area") ||

                        sIssueDescription.equals("Fire Hazards") ||

                        sIssueDescription.equals("Water Scarcity") ||

                        sIssueDescription.equals("Low Water Pressure") ||

                        sIssueDescription.equals("Contaminated Water Supply") ||

                        sIssueDescription.equals("Illegal Electricty Connections") ||

                        sIssueDescription.equals("Irregular Electricity Supply") ||

                        sIssueDescription.equals("Anti Social Elements/Activity") ||

                        sIssueDescription.equals("Tree felling") ||

                        sIssueDescription.equals("Mangroves in danger") ||

                        sIssueDescription.equals("Fogging") ||

                        sIssueDescription.equals("Tree Trimming") ||

                        sIssueDescription.equals("Scraping")
                        ) {
                    imageVerification =false;
                } else {
                    imageVerification= true;
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
        findViewById(R.id.MF).setOnClickListener(onClickListener);
        findViewById(R.id.UBF).setOnClickListener(onClickListener);
        findViewById(R.id.UBP).setOnClickListener(onClickListener);
        findViewById(R.id.EF).setOnClickListener(onClickListener);
        findViewById(R.id.DLF).setOnClickListener(onClickListener);
        findViewById(R.id.POR).setOnClickListener(onClickListener);
        findViewById(R.id.URS).setOnClickListener(onClickListener);
        findViewById(R.id.DUR).setOnClickListener(onClickListener);
        findViewById(R.id.SRW).setOnClickListener(onClickListener);
        findViewById(R.id.AG).setOnClickListener(onClickListener);
        findViewById(R.id.GNP).setOnClickListener(onClickListener);
        findViewById(R.id.SNS).setOnClickListener(onClickListener);
        findViewById(R.id.CDD).setOnClickListener(onClickListener);
        findViewById(R.id.GBB).setOnClickListener(onClickListener);
        findViewById(R.id.ODSL).setOnClickListener(onClickListener);
        findViewById(R.id.COSL).setOnClickListener(onClickListener);
        findViewById(R.id.ODU).setOnClickListener(onClickListener);
        findViewById(R.id.DD).setOnClickListener(onClickListener);
        findViewById(R.id.UPR).setOnClickListener(onClickListener);
        findViewById(R.id.MS).setOnClickListener(onClickListener);
        findViewById(R.id.SDG).setOnClickListener(onClickListener);
        findViewById(R.id.PGM).setOnClickListener(onClickListener);
        findViewById(R.id.IFVS).setOnClickListener(onClickListener);
        findViewById(R.id.IFS).setOnClickListener(onClickListener);
        findViewById(R.id.HTMF).setOnClickListener(onClickListener);
        findViewById(R.id.IER).setOnClickListener(onClickListener);
        findViewById(R.id.IC).setOnClickListener(onClickListener);
        findViewById(R.id.HBN).setOnClickListener(onClickListener);
        findViewById(R.id.BEN).setOnClickListener(onClickListener);
        findViewById(R.id.RE).setOnClickListener(onClickListener);
        findViewById(R.id.ASE).setOnClickListener(onClickListener);
        findViewById(R.id.ISLT).setOnClickListener(onClickListener);
        findViewById(R.id.PSI).setOnClickListener(onClickListener);
        findViewById(R.id.TRA).setOnClickListener(onClickListener);
        findViewById(R.id.FRH).setOnClickListener(onClickListener);
        findViewById(R.id.WS).setOnClickListener(onClickListener);
        findViewById(R.id.LWP).setOnClickListener(onClickListener);
        findViewById(R.id.LWL).setOnClickListener(onClickListener);
        findViewById(R.id.CWS).setOnClickListener(onClickListener);
        findViewById(R.id.IEC).setOnClickListener(onClickListener);
        findViewById(R.id.IES).setOnClickListener(onClickListener);
        findViewById(R.id.EG).setOnClickListener(onClickListener);
        findViewById(R.id.ASEA).setOnClickListener(onClickListener);
        findViewById(R.id.PMPG).setOnClickListener(onClickListener);
        findViewById(R.id.GTF).setOnClickListener(onClickListener);
        findViewById(R.id.TF).setOnClickListener(onClickListener);
        findViewById(R.id.DPT).setOnClickListener(onClickListener);
        findViewById(R.id.IDT).setOnClickListener(onClickListener);
        findViewById(R.id.MGD).setOnClickListener(onClickListener);
        findViewById(R.id.FG).setOnClickListener(onClickListener);
        findViewById(R.id.TT).setOnClickListener(onClickListener);
        findViewById(R.id.SC).setOnClickListener(onClickListener);
    }


}
