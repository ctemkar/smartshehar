package com.smartshehar.customercalls.apk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.smartshehar.customercalls.apk.CustomerCP;
import com.smartshehar.customercalls.apk.CustomerCallsSQLiteDB;
import com.smartshehar.customercalls.apk.R;

/**
 * Created by asmita on 16/05/2016.
 */
public class CallLog_Frag extends Fragment implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{
    ListView mListView;
    SimpleCursorAdapter mAdap;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_call_log, container, false);
        mListView = (ListView) view.findViewById(R.id.list);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
      /*  ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.Planets, android.R.layout.simple_list_item_1);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);*/
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       // Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdap = new SimpleCursorAdapter(getContext(),
                R.layout.call_log_list_item,
                null,
                new String[]{CustomerCallsSQLiteDB.CUSTOMER_COLUMN_FIRST_NAME,
                        CustomerCallsSQLiteDB.CUSTOMER_COLUMN_PHONE,
                        CustomerCallsSQLiteDB.ORDER_HEADER_COLUMN_AMOUNT,
                        CustomerCallsSQLiteDB.CUSTOMER_CALL_LOG_CALL_DATE_TIME

                },
                new int[]{R.id.tvTitle, R.id.tvPhoneNo,
                        R.id.tvlastOrder, R.id.tvlastOrderDate}, 0);
        mListView.setAdapter(mAdap);
        /** Creating a loader for populating listview from sqlite database */
        /** This statement, invokes the method onCreatedLoader() */
        getActivity().getSupportLoaderManager().initLoader(0, null, this);
        getActivity().getSupportLoaderManager().restartLoader(0, null, this);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
               /* Cursor c = ((SimpleCursorAdapter) mListView.getAdapter()).getCursor();
                String phoneno = c.getString(c.getColumnIndexOrThrow(CustomerCallsSQLiteDB.CUSTOMER_COLUMN_PHONE));
                String orderamount = String.valueOf(c.getInt(c.getColumnIndexOrThrow(CustomerCallsSQLiteDB.ORDER_HEADER_COLUMN_AMOUNT)));
                CustomerCallsSQLiteDB mCustomerDB = new CustomerCallsSQLiteDB(getContext());
                CCallInfo oCC = mCustomerDB.getRowByNationalNo(phoneno);
                Intent intent = new Intent(getContext(), ShowCustomer_act.class);
                Gson gson = new Gson();
                String json = gson.toJson(oCC);
                intent.putExtra("callinfo", json);
                intent.putExtra("orderamount", orderamount);
                startActivity(intent);*/
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = CustomerCP.CONTENT_CALL_LOG_URI;
        return new CursorLoader(getContext(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdap.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        try {
            mAdap.swapCursor(null);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}