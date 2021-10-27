package com.jumpinjumpout.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.jumpinjumpout.CGlobals_dp;
import com.jumpinjumpout.www.driverprofile.R;


public class DriverList extends AppCompatActivity {
    public static CGlobals_dp mApp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_list);
        mApp = CGlobals_dp.getInstance(DriverList.this);
        mApp.init(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, RecyclerViewFragment.newInstance())
                    .commit();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DriverList.this.finish();
        Intent intent = new Intent(DriverList.this, ActDriverProfileRegistration.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.driver_list_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.searchNo).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setIconifiedByDefault(false);

        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered
                RecyclerViewFragment.mAdapter.getMyFilter().filter(newText);
                System.out.println("on text chnge text: " + newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // this is your adapter that will be filtered
                RecyclerViewFragment.mAdapter.getMyFilter().filter(query);
                System.out.println("on query submit: " + query);
                return true;
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refreshDriverList) {
            DriverList.this.finish();
            startActivity(new Intent(DriverList.this, DriverList.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
