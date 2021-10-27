package in.bestbus.app;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartshehar.dashboard.app.R;


// XXX compiler bug in javac 1.5.0_07-164, we need to implement Filterable
// to make compilation work
public class BusListAdapter extends CursorAdapter {
    CGlobals_BA mApp;
    int curId;
    @SuppressWarnings("unused")
    private ContentResolver mContent;

    @SuppressWarnings("deprecation")
    public BusListAdapter(Context context, Cursor c, CGlobals_BA app) {
        super(context, c);
        mApp = app;
        mContent = context.getContentResolver();
    }

    /*
                  public StationListAdapter(Context context, Cursor c) {
                    super(context, c);
                    mContent = context.getContentResolver();
                }
    */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final LinearLayout view = (LinearLayout) inflater.inflate(
                R.layout.buslist_item, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //           ((TextView) view).setText(cursor.getString(COLUMN_DISPLAY_NAME));
        TextView tv = (TextView) view.findViewById(R.id.listtxtitem);
//              tv.setText(cursor.getString(COLUMN_DISPLAY_NAME));
        tv.setText(cursor.getString(cursor.getColumnIndexOrThrow("busno")).trim());
    }

    @Override
    public String convertToString(Cursor cursor) {
        curId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        return cursor.getString(cursor.getColumnIndexOrThrow("busno")).trim();
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (constraint != null)
            return mApp.mDBHelperBus.getBusNosCursor(constraint.toString());
        else
            return null;
    /*            FilterQueryProvider filter = getFilterQueryProvider();
                if (filter != null) {
                    return filter.runQuery(constraint);
                }
                return null;
     //           Uri uri = Uri.withAppendedPath(
     //                   Contacts.CONTENT_FILTER_URI,
//                        Uri.encode(constraint.toString()));
//                return mContent.query(uri, CONTACT_PROJECTION, null, null, null);
    */
    }
}

