package com.jumpinjumpout.apk.user.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Contact_Results;

import java.util.ArrayList;

import lib.app.util.ContactAccessor;
import lib.app.util.ContactInfo;

abstract class AddContact_act extends Activity {
    protected static final int PICK_CONTACT_REQUEST = 88;
    protected final ContactAccessor mContactAccessor = ContactAccessor.getInstance();
    protected static String TAG = AddContact_act.class.getSimpleName();
    ArrayList<Contact_Results> contactResults = new ArrayList<Contact_Results>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String contact_Id = null;
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            loadContactInfo(data.getData());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        hideKeyboard();
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.RESULT_HIDDEN);
        }
    }

    /**
     * Load contact information on a background thread.
     */
    private void loadContactInfo(Uri contactUri) {

        /*
         * We should always run database queries on a background thread. The database may be
         * locked by some process for a long time.  If we locked up the UI thread while waiting
         * for the query to come back, we might get an "Application Not Responding" dialog.
         */
        AsyncTask<Uri, Void, ContactInfo> task = new AsyncTask<Uri, Void, ContactInfo>() {

            @Override
            protected ContactInfo doInBackground(Uri... uris) {
                return mContactAccessor.loadContact(getContentResolver(), uris[0]);
            }

            @Override
            protected void onPostExecute(ContactInfo contactInfo) {
                if (!TextUtils.isEmpty(contactInfo.getPhoneNumber()) && contactInfo.getPhoneNumber().replace(" ", "").equals(
                        CGlobals_user.getPhoneNumber().replace(" ", ""))) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Whoa! You are already there as creator of this community",
                            Toast.LENGTH_LONG).show();
                } else {
                    Contact_Results gresuli = new Contact_Results();
                    gresuli.setContactName(contactInfo.getDisplayName());
                    gresuli.setContactPhone(contactInfo.getPhoneNumber());
                    gresuli.setContactEmail(contactInfo.getEmail());

                    contactResults.add(gresuli);
                }
                updateList();
                sendInvitePeople(contactInfo);
                sendMemberInfo(contactInfo);
            }
        };

        task.execute(contactUri);
    }

    protected abstract void updateList();

    protected abstract void sendMemberInfo(ContactInfo contactInfo);

    protected abstract void sendInvitePeople(ContactInfo contactInfo);

}
