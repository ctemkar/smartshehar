/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jumpinjumpout.apk.user.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.jumpinjumpout.apk.R;

@SuppressLint("NewApi")
public class FriendRequest_act extends AppCompatActivity {
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments representing
     * each object in a collection. We use a {@link FragmentStatePagerAdapter}
     * derivative, which will destroy and re-create fragments as needed, saving and restoring their
     * state in the process. This is important to conserve memory and is a best practice when
     * allowing navigation between objects in a potentially large collection.
     */
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    /**
     * The {@link ViewPager} that will display the object collection.
     */
    ViewPager mViewPager;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendrequest_act);
        // Create an adapter that when requested, will return a fragment representing an object in
        // the collection.
        //
        // ViewPager and its adapters use support library fragments, so we must use
        // getSupportFragmentManager.
        mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager());
        // Set up action bar.
        final ActionBar actionBar = getSupportActionBar();
        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Set up the ViewPager, attaching the adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
    }
    /**
     * A {@link FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public static class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {
        public DemoCollectionPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }
        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            if (i == 0) {
                fragment = new Fragment();
                Bundle args = new Bundle();
                args.putInt(FriendRequestAcceptReject_act.ARG_OBJECT, i + 1); // Our object is just an integer :-P
                fragment.setArguments(args);
            }
            if (i == 1) {
                fragment = new FindWorkAddress_act();
                Bundle args = new Bundle();
                args.putInt(FriendRequestAcceptReject_act.ARG_OBJECT, i + 1); // Our object is just an integer :-P
                fragment.setArguments(args);
            }
            if (i == 2) {
                fragment = new FindWorkAddress_act();
                Bundle args = new Bundle();
                args.putInt(FriendRequestAcceptReject_act.ARG_OBJECT, i + 1); // Our object is just an integer :-P
                fragment.setArguments(args);
            }
            if (i == 3) {
                fragment = new FindWorkAddress_act();
                Bundle args = new Bundle();
                args.putInt(FriendRequestAcceptReject_act.ARG_OBJECT, i + 1); // Our object is just an integer :-P
                fragment.setArguments(args);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // For this contrived example, we have a 100-object collection.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String charSequence = null;
            if (position == 0 ) {
                charSequence = "Friend Request";
            }
            if (position == 1) {
                charSequence = "Friend Request Sent";
            }
            if (position == 2) {
                charSequence = "Friend Accepted";
            }
            if (position == 3) {
                charSequence = "Friend Rejected";
            }
            return charSequence;
        }
    }
}
