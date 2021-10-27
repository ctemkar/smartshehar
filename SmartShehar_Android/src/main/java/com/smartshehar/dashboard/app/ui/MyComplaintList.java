package com.smartshehar.dashboard.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.smartshehar.dashboard.app.R;

import lib.app.util.Connectivity;


public class MyComplaintList extends ActionBarActivity {


    Connectivity mConnectivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_complaint_list);
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(MyComplaintList.this)) {
            if (!mConnectivity.connectionError(MyComplaintList.this)) {
                if (mConnectivity.isGPSEnable(MyComplaintList.this)) {
                    String TAG = "MyComplaintList";
                    Log.d(TAG, "Internet Connection");
                }
            }
        }
        Intent intent = getIntent();

        if (savedInstanceState == null) {
            RecyclerViewFragment fragment = new RecyclerViewFragment();
            Bundle args = new Bundle();
            args.putInt(RecyclerViewFragment.ARG_SUBMIT, intent.getIntExtra("selectFlag", 1));
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
            getSupportFragmentManager().addOnBackStackChangedListener(getListener());
            Log.d("MyIssuesAdapter", "fragment added ");
        }
    }

    private FragmentManager.OnBackStackChangedListener getListener()
    {
        FragmentManager.OnBackStackChangedListener result = new FragmentManager.OnBackStackChangedListener()
        {
            public void onBackStackChanged()
            {
                FragmentManager manager = getSupportFragmentManager();

                if (manager != null)
                {
                    RecyclerViewFragment currFrag = (RecyclerViewFragment)manager.
                            findFragmentById(R.id.container);
                    currFrag.onResume();
                }
            }
        };
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
