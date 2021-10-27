package com.smartshehar.customercalls.apk;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.util.HashMap;

@ReportsCrashes(
        formUri = "http://www.smartshehar.com/alpha/smartsheharapp/v17/svr/php/acra/acra_ss_report.php",
         mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)


public class CCApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        HashMap<String,String> ACRAData = new HashMap<>();
        AccountManager manager = (AccountManager) this
                .getSystemService(Context.ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        String msGmail = null;

        for (Account account : list) {
            if (account.type.equalsIgnoreCase("com.google")) {
                msGmail = account.name;
                break;
            }
        }

        ACRA.getErrorReporter().putCustomData("useremail", msGmail);
        ACRA.getErrorReporter().putCustomData("email", msGmail);
        ACRA.getErrorReporter().putCustomData("appname", "customercalls");

        super.onCreate();
    }
}