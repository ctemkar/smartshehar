package in.bestbus.app.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

public abstract class AbstractActivity extends Activity {
    /**
     * Members
     */
   // private int m_currentNavigationItem;

    /**
     *
     */
    protected abstract int getContentLayoutId();

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @SuppressLint("Recycle")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
         * Super...
         */
        super.onCreate(savedInstanceState);

    	/*
         * Content View...
    	 */
        setContentView(getContentLayoutId());

        /*
         * List Navigation...
         */

        // Up Icon + Logo + Hide title...
       /* getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayUseLogoEnabled(true);

        // Custom navigation list adapter...
        Context context = getActionBar().getThemedContext();
        TypedArray logos = getResources().obtainTypedArray(R.array.activity_logos);
        TypedArray titles = getResources().obtainTypedArray(R.array.activity_titles);
        TypedArray subtitles = getResources().obtainTypedArray(R.array.activity_subtitles);
        NavigationListAdapter navigationListApdater = new NavigationListAdapter(context, logos, titles, subtitles);

        // Custom navigation list listener...
        NavigationListListener navigationListListener = new NavigationListListener(this);

        // Set navigation mode...
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(navigationListApdater, navigationListListener);

        // Remember current navigation item...
        m_currentNavigationItem = getCurrentNavigationItem(this, titles);
    }

    *//*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     *//*
    @Override
    protected void onResume() {
    	*//*
    	 * Super...
    	 *//*
        super.onResume();

    	*//*
    	 * Select current title in navigation list (first start or on back)...
    	 *//*
        getActionBar().setSelectedNavigationItem(m_currentNavigationItem);
    }

    *//*
     * (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockActivity#onOptionsItemSelected(android.view.MenuItem)
     *//*
    @Override
    public boolean onOptionsItemSelected(MenuItem p_item) {
        switch (p_item.getItemId()) {
	    	*//*
	   		 * Home...
	   		 *//*
            case android.R.id.home:
                Intent intent = new Intent(this, ActBusDashboard.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;

	   		*//*
	         *
	         *//*
            default:
                return super.onOptionsItemSelected(p_item);
        }
    }

    *//**
     * Get navigation list index for current activity.
     *
     * @param p_activity
     * @param p_titles
     * @return
     *//*
    private int getCurrentNavigationItem(Activity p_activity, TypedArray p_titles) {
        String title = p_activity.getTitle().toString();
        int position = 0;

        for (int i = 0, n = p_titles.length(); i < n; i++) {
            if (p_titles.getString(i).equals(title)) {
                position = i;
                break;
            }
        }

        return position;
    }

    *//**
     * Custom navigation list adapter.
     *//*
    private final class NavigationListAdapter extends BaseAdapter implements SpinnerAdapter {
        *//**
         * Members
         *//*
        private LayoutInflater m_layoutInflater;
        private TypedArray m_logos;
        private TypedArray m_titles;
        private TypedArray m_subtitles;

        *//**
         * Constructor
         *//*
        public NavigationListAdapter(Context p_context, TypedArray p_logos, TypedArray p_titles, TypedArray p_subtitles) {
            m_layoutInflater = LayoutInflater.from(p_context);
            m_logos = p_logos;
            m_titles = p_titles;
            m_subtitles = p_subtitles;
        }

        *//*
         * (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         *//*
        @Override
        public int getCount() {
            return m_titles.length();
        }

        *//*
         * (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         *//*
        @Override
        public Object getItem(int p_position) {
            return p_position;
        }

        *//*
         * (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         *//*
        @Override
        public long getItemId(int p_position) {
            return m_titles.getResourceId(p_position, 0);
        }

        *//*
         * (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         *//*
        @Override
        public View getView(int p_position, View p_convertView, ViewGroup p_parent) {
			*//*
			 * View...
			 *//*
            View view = p_convertView;
            if (view == null) {
                view = m_layoutInflater.inflate(R.layout.navigation_list_item, p_parent, false);
            }

			*//*
			 * Display...
			 *//*
            // Title...
            TextView tv_title = (TextView) view.findViewById(R.id.title);
            tv_title.setText(m_titles.getString(p_position));

            // Subtitle...
            TextView tv_subtitle = ((TextView) view.findViewById(R.id.subtitle));
            tv_subtitle.setText(m_subtitles.getString(p_position));
            tv_subtitle.setVisibility("".equals(tv_subtitle.getText()) ? View.GONE : View.VISIBLE);

			*//*
			 * Return...
			 *//*
            return view;
        }

        *//*
         * (non-Javadoc)
         * @see android.widget.BaseAdapter#getDropDownView(int, android.view.View, android.view.ViewGroup)
         *//*
        @Override
        public View getDropDownView(int p_position, View p_convertView, ViewGroup p_parent) {
			*//*
			 * View...
			 *//*
            View view = p_convertView;
            if (view == null) {
                view = m_layoutInflater.inflate(R.layout.navigation_list_dropdown_item, p_parent, false);
            }

			*//*
			 * Display...
			 *//*

            // Icon...
            ImageView iv_logo = (ImageView) view.findViewById(R.id.logo);
            iv_logo.setImageDrawable(m_logos.getDrawable(p_position));

            // Title...
            TextView tv_title = (TextView) view.findViewById(R.id.title);
            tv_title.setText(m_titles.getString(p_position));

            // Subtitle...
            TextView tv_subtitle = ((TextView) view.findViewById(R.id.subtitle));
            tv_subtitle.setText(m_subtitles.getString(p_position));
            tv_subtitle.setVisibility("".equals(tv_subtitle.getText()) ? View.GONE : View.VISIBLE);

			*//*
			 * Return...
			 *//*
            return view;
        }
    }

    *//**
     * Custom navigation list listener.
     *//*
    private class NavigationListListener implements ActionBar.OnNavigationListener {
        *//**
         * Members
         *//*
        private AbstractActivity m_activity;

        *//**
         * @param p_activity
         *//*
        NavigationListListener(AbstractActivity p_activity) {
            m_activity = p_activity;
        }

        *//*
         * (non-Javadoc)
         * @see com.actionbarsherlock.app.ActionBar.OnNavigationListener#onNavigationItemSelected(int, long)
         *//*
        @Override
        public boolean onNavigationItemSelected(int p_itemPosition, long p_itemId) {
			*//*
			 * Ignore if selecting current...
			 *//*
            if (p_itemPosition == m_activity.m_currentNavigationItem) {
                return true;
            }
            m_activity.m_currentNavigationItem = p_itemPosition;
			*//*
			 * Start new activity...
			 *//*
            Intent intent = null;
            if (p_itemId == R.string.title_activity_main) {
                intent = new Intent(m_activity, ActBusMain.class);
            } else if (p_itemId == R.string.title_activity_busno) {
                intent = new Intent(m_activity, ActBusJourney.class);
            } else if (p_itemId == R.string.title_activity_bustrip) {
                intent = new Intent(m_activity, ActBusTrip.class);
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }

            return true;
        }*/
    }
}
