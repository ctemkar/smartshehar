package com.jumpinjumpout.apk.user.ui;


import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.view.MenuItemCompat;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.SearchView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.jumpinjumpout.apk.BuildConfig;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.user.ImageLoader;
import com.jumpinjumpout.apk.user.Utils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;


public class ContactsListFragment extends ListFragment implements
        AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ContactsListFragment";

    private static final String STATE_PREVIOUSLY_SELECTED_KEY = "com.example.android.contactslist.ui.SELECTED_ITEM";

    private ContactsAdapter mAdapter;
    private ImageLoader mImageLoader;
    private String mSearchTerm;

    private OnContactsInteractionListener mOnContactSelectedListener;

    private int mPreviouslySelectedSearchItem = 0;

    private boolean mSearchQueryChanged;

    private boolean mIsTwoPaneLayout;

    private boolean mIsSearchResultView = false;

    public ContactsListFragment() {
    }

    public void setSearchQuery(String query) {
        if (TextUtils.isEmpty(query)) {
            mIsSearchResultView = false;
        } else {
            mSearchTerm = query;
            mIsSearchResultView = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsTwoPaneLayout = getResources().getBoolean(R.bool.has_two_panes);

        setHasOptionsMenu(true);

        mAdapter = new ContactsAdapter(getActivity());

        if (savedInstanceState != null) {

            mSearchTerm = savedInstanceState.getString(SearchManager.QUERY);
            mPreviouslySelectedSearchItem = savedInstanceState.getInt(
                    STATE_PREVIOUSLY_SELECTED_KEY, 0);
        }

        mImageLoader = new ImageLoader(getActivity(),
                getListPreferredItemHeight()) {
            @Override
            protected Bitmap processBitmap(Object data) {

                return loadContactPhotoThumbnail((String) data, getImageSize());
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_list_fragment, container,
                false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(this);
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView,
                                             int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mImageLoader.setPauseWork(true);
                } else {
                    mImageLoader.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        });

        if (mIsTwoPaneLayout) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
        if (mPreviouslySelectedSearchItem == 0) {
            getLoaderManager().initLoader(ContactsQuery.QUERY_ID, null, this);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {

            mOnContactSelectedListener = (OnContactsInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnContactsInteractionListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageLoader.setPauseWork(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        final Cursor cursor = mAdapter.getCursor();

        cursor.moveToPosition(position);

        final Uri uri = ContactsContract.Contacts.getLookupUri(cursor.getLong(ContactsQuery.ID),
                cursor.getString(ContactsQuery.LOOKUP_KEY));

        mOnContactSelectedListener.onContactSelected(uri);
        if (mIsTwoPaneLayout) {
            getListView().setItemChecked(position, true);
        }
    }

    private void onSelectionCleared() {
        mOnContactSelectedListener.onSelectionCleared();

        getListView().clearChoices();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.contact_list_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);


        if (mIsSearchResultView) {
            searchItem.setVisible(false);
        }
        if (Utils.hasHoneycomb()) {

            final SearchManager searchManager = (SearchManager) getActivity()
                    .getSystemService(Context.SEARCH_SERVICE);

            final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getActivity().getComponentName()));

            searchView
                    .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String queryText) {
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            String newFilter = !TextUtils.isEmpty(newText) ? newText
                                    : null;

                            if (mSearchTerm == null && newFilter == null) {
                                return true;
                            }
                            if (mSearchTerm != null
                                    && mSearchTerm.equals(newFilter)) {
                                return true;
                            }
                            mSearchTerm = newFilter;
                            mSearchQueryChanged = true;
                            getLoaderManager().restartLoader(
                                    ContactsQuery.QUERY_ID, null,
                                    ContactsListFragment.this);
                            return true;
                        }
                    });

            if (mSearchTerm != null) {
                final String savedSearchTerm = mSearchTerm;
                searchView.setQuery(savedSearchTerm, false);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(mSearchTerm)) {
            outState.putString(SearchManager.QUERY, mSearchTerm);

            outState.putInt(STATE_PREVIOUSLY_SELECTED_KEY, getListView()
                    .getCheckedItemPosition());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Sends a request to the People app to display the create contact
            // screen
            case R.id.menu_add_contact:
                final Intent intent = new Intent(Intent.ACTION_INSERT,
                        ContactsContract.Contacts.CONTENT_URI);
                startActivity(intent);
                break;
            // For platforms earlier than Android 3.0, triggers the search activity
            case R.id.menu_search:
                if (!Utils.hasHoneycomb()) {
                    getActivity().onSearchRequested();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // If this is the loader for finding contacts in the Contacts Provider
        // (the only one supported)
        if (id == ContactsQuery.QUERY_ID) {
            Uri contentUri;

            if (mSearchTerm == null) {
                // Since there's no search string, use the content URI that
                // searches the entire
                // Contacts table
                contentUri = ContactsQuery.CONTENT_URI;
            } else {
                // Since there's a search string, use the special content Uri
                // that searches the
                // Contacts table. The URI consists of a base Uri and the search
                // string.
                contentUri = Uri.withAppendedPath(ContactsQuery.FILTER_URI,
                        Uri.encode(mSearchTerm));
            }

            // Returns a new CursorLoader for querying the Contacts table. No
            // arguments are used
            // for the selection clause. The search string is either encoded
            // onto the content URI,
            // or no contacts search string is used. The other search criteria
            // are constants. See
            // the ContactsQuery interface.
            return new CursorLoader(getActivity(), contentUri,
                    ContactsQuery.PROJECTION, ContactsQuery.SELECTION, null,
                    ContactsQuery.SORT_ORDER);
        }

        Log.e(TAG, "onCreateLoader - incorrect ID provided (" + id + ")");
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // This swaps the new cursor into the adapter.
        if (loader.getId() == ContactsQuery.QUERY_ID) {
            mAdapter.swapCursor(data);

            // If this is a two-pane layout and there is a search query then
            // there is some additional work to do around default selected
            // search mPassengerTrip.
            if (mIsTwoPaneLayout && !TextUtils.isEmpty(mSearchTerm)
                    && mSearchQueryChanged) {
                // Selects the first mPassengerTrip in contactResults, unless this fragment has
                // been restored from a saved state (like orientation change)
                // in which case it selects the previously selected search mPassengerTrip.
                if (data != null
                        && data.moveToPosition(mPreviouslySelectedSearchItem)) {
                    // Creates the content Uri for the previously selected
                    // contact by appending the
                    // contact's ID to the Contacts table content Uri
                    final Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,
                            String.valueOf(data.getLong(ContactsQuery.ID)));
                    mOnContactSelectedListener.onContactSelected(uri);
                    getListView().setItemChecked(mPreviouslySelectedSearchItem,
                            true);
                } else {
                    // No contactResults, clear selection.
                    onSelectionCleared();
                }
                // Only restore from saved state one time. Next time fall back
                // to selecting first mPassengerTrip. If the fragment state is saved again
                // then the currently selected mPassengerTrip will once again be saved.
                mPreviouslySelectedSearchItem = 0;
                mSearchQueryChanged = false;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == ContactsQuery.QUERY_ID) {
            // When the loader is being reset, clear the cursor from the
            // adapter. This allows the
            // cursor resources to be freed.
            mAdapter.swapCursor(null);
        }
    }

    /**
     * Gets the preferred height for each mPassengerTrip in the ListView, in pixels, after
     * accounting for screen density. ImageLoader uses this value to resize
     * thumbnail images to match the ListView mPassengerTrip height.
     *
     * @return The preferred height in pixels, based on the current theme.
     */
    private int getListPreferredItemHeight() {
        final TypedValue typedValue = new TypedValue();

        // Resolve list mPassengerTrip preferred height theme attribute into typedValue
        getActivity().getTheme().resolveAttribute(
                android.R.attr.listPreferredItemHeight, typedValue, true);

        // Create a new DisplayMetrics object
        final DisplayMetrics metrics = new DisplayMetrics();

        // Populate the DisplayMetrics
        getActivity().getWindowManager().getDefaultDisplay()
                .getMetrics(metrics);

        // Return theme value based on DisplayMetrics
        return (int) typedValue.getDimension(metrics);
    }

    /**
     * Decodes and scales a contact's image from a file pointed to by a Uri in
     * the contact's data, and returns the result as a Bitmap. The column that
     * contains the Uri varies according to the platform version.
     *
     * @param photoData For platforms prior to Android 3.0, provide the Contact._ID
     *                  column value. For Android 3.0 and later, provide the
     *                  Contact.PHOTO_THUMBNAIL_URI value.
     * @param imageSize The desired target width and height of the output image in
     *                  pixels.
     * @return A Bitmap containing the contact's image, resized to fit the
     * provided image size. If no thumbnail exists, returns null.
     */
    private Bitmap loadContactPhotoThumbnail(String photoData, int imageSize) {

        // Ensures the Fragment is still added to an activity. As this method is
        // called in a
        // background thread, there's the possibility the Fragment is no longer
        // attached and
        // added to an activity. If so, no need to spend resources loading the
        // contact photo.
        if (!isAdded() || getActivity() == null) {
            return null;
        }

        // Instantiates an AssetFileDescriptor. Given a content Uri pointing to
        // an image file, the
        // ContentResolver can return an AssetFileDescriptor for the file.
        AssetFileDescriptor afd = null;

        // This "try" block catches an Exception if the file descriptor returned
        // from the Contacts
        // Provider doesn't point to an existing file.
        try {
            Uri thumbUri;
            // If Android 3.0 or later, converts the Uri passed as a string to a
            // Uri object.
            if (Utils.hasHoneycomb()) {
                thumbUri = Uri.parse(photoData);
            } else {
                // For versions prior to Android 3.0, appends the string
                // argument to the content
                // Uri for the Contacts table.
                final Uri contactUri = Uri.withAppendedPath(
                        ContactsContract.Contacts.CONTENT_URI, photoData);

                // Appends the content Uri for the Contacts.Photo table to the
                // previously
                // constructed contact Uri to yield a content URI for the
                // thumbnail image
                thumbUri = Uri.withAppendedPath(contactUri,
                        ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            }
            // Retrieves a file descriptor from the Contacts Provider. To learn
            // more about this
            // feature, read the reference documentation for
            // ContentResolver#openAssetFileDescriptor.
            afd = getActivity().getContentResolver().openAssetFileDescriptor(
                    thumbUri, "r");

            // Gets a FileDescriptor from the AssetFileDescriptor. A
            // BitmapFactory object can
            // decode the contents of a file pointed to by a FileDescriptor into
            // a Bitmap.
            FileDescriptor fileDescriptor = afd.getFileDescriptor();

            if (fileDescriptor != null) {
                // Decodes a Bitmap from the image pointed to by the
                // FileDescriptor, and scales it
                // to the specified width and height
                return ImageLoader.decodeSampledBitmapFromDescriptor(
                        fileDescriptor, imageSize, imageSize);
            }
        } catch (FileNotFoundException e) {
            // If the file pointed to by the thumbnail URI doesn't exist, or the
            // file can't be
            // opened in "read" mode, ContentResolver.openAssetFileDescriptor
            // throws a
            // FileNotFoundException.
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Contact photo thumbnail not found for contact "
                        + photoData + ": " + e.toString());
            }
        } finally {
            // If an AssetFileDescriptor was returned, try to close it
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                    // Closing a file descriptor might cause an IOException if
                    // the file is
                    // already closed. Nothing extra is needed to handle this.
                }
            }
        }

        // If the decoding failed, returns null
        return null;
    }

    /**
     * This is a subclass of CursorAdapter that supports binding Cursor columns
     * to a view layout. If those items are part of search contactResults, the search
     * string is marked by highlighting the query text. An
     * {@link AlphabetIndexer} is used to allow quicker navigation up and down
     * the ListView.
     */
    private class ContactsAdapter extends CursorAdapter implements
            SectionIndexer {
        private LayoutInflater mInflater; // Stores the layout inflater
        private AlphabetIndexer mAlphabetIndexer; // Stores the AlphabetIndexer
        // instance
        private TextAppearanceSpan highlightTextSpan; // Stores the highlight
        // text appearance style

        /**
         * Instantiates a new Contacts Adapter.
         *
         * @param context A context that has access to the app's layout.
         */
        public ContactsAdapter(Context context) {
            super(context, null, 0);

            // Stores inflater for use later
            mInflater = LayoutInflater.from(context);

            // Loads a string containing the English alphabet. To fully localize
            // the app, provide a
            // strings.xml file in res/values-<x> directories, where <x> is a
            // locale. In the file,
            // define a string with android:name="alphabet" and contents set to
            // all of the
            // alphabetic characters in the language in their proper sort order,
            // in upper case if
            // applicable.
            final String alphabet = context.getString(R.string.alphabet);

            // Instantiates a new AlphabetIndexer bound to the column used to
            // sort contact names.
            // The cursor is left null, because it has not yet been retrieved.
            mAlphabetIndexer = new AlphabetIndexer(null,
                    ContactsQuery.SORT_KEY, alphabet);

            // Defines a span for highlighting the part of a display name that
            // matches the search
            // string
            highlightTextSpan = new TextAppearanceSpan(getActivity(),
                    R.style.searchTextHiglight);
        }

        /**
         * Identifies the start of the search string in the display name column
         * of a Cursor row. E.g. If displayName was "Adam" and search query
         * (mSearchTerm) was "da" this would return 1.
         *
         * @param displayName The contact display name.
         * @return The starting position of the search string in the display
         * name, 0-based. The method returns -1 if the string is not
         * found in the display name, or if the search string is empty
         * or null.
         */
        private int indexOfSearchQuery(String displayName) {
            if (!TextUtils.isEmpty(mSearchTerm)) {
                return displayName.toLowerCase(Locale.getDefault()).indexOf(
                        mSearchTerm.toLowerCase(Locale.getDefault()));
            }
            return -1;
        }

        /**
         * Overrides newView() to inflate the list mPassengerTrip views.
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            // Inflates the list mPassengerTrip layout.
            final View itemLayout = mInflater.inflate(
                    R.layout.contact_list_item, viewGroup, false);

            // Creates a new ViewHolder in which to store handles to each view
            // resource. This
            // allows bindView() to retrieve stored references instead of
            // calling findViewById for
            // each instance of the layout.
            final ViewHolder holder = new ViewHolder();
            holder.text1 = (TextView) itemLayout
                    .findViewById(android.R.id.text1);
            holder.text2 = (TextView) itemLayout
                    .findViewById(android.R.id.text2);
            holder.icon = (QuickContactBadge) itemLayout
                    .findViewById(android.R.id.icon);
            // holder.selectnumber = (CheckBox) itemLayout
            // .findViewById(android.R.id.checkbox);
            // holder.selectnumber.isChecked();

            // Stores the resourceHolder instance in itemLayout. This makes
            // resourceHolder
            // available to bindView and other methods that receive a handle to
            // the mPassengerTrip view.
            itemLayout.setTag(holder);

            // Returns the mPassengerTrip layout view
            return itemLayout;
        }

        /**
         * Binds data from the Cursor to the provided view.
         */
        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            // Gets handles to individual view resources
            final ViewHolder holder = (ViewHolder) view.getTag();

            // For Android 3.0 and later, gets the thumbnail image Uri from the
            // current Cursor row.
            // For platforms earlier than 3.0, this isn't necessary, because the
            // thumbnail is
            // generated from the other fields in the row.
            final String photoUri = cursor
                    .getString(ContactsQuery.PHOTO_THUMBNAIL_DATA);

            final String displayName = cursor
                    .getString(ContactsQuery.DISPLAY_NAME);

            // final String displayID = cursor.getString(ContactsQuery.ID);

            final int startIndex = indexOfSearchQuery(displayName);

            if (startIndex == -1) {
                // If the user didn't do a search, or the search string didn't
                // match a display
                // name, show the display name without highlighting
                holder.text1.setText(displayName);

                if (TextUtils.isEmpty(mSearchTerm)) {
                    // If the search search is empty, hide the second line of
                    // text
                    holder.text2.setVisibility(View.GONE);
                } else {
                    // Shows a second line of text that indicates the search
                    // string matched
                    // something other than the display name
                    holder.text2.setVisibility(View.VISIBLE);
                }
            } else {
                // If the search string matched the display name, applies a
                // SpannableString to
                // highlight the search string with the displayed display name

                // Wraps the display name in the SpannableString
                final SpannableString highlightedName = new SpannableString(
                        displayName);

                // Sets the span to start at the starting point of the match and
                // end at "length"
                // characters beyond the starting point
                highlightedName.setSpan(highlightTextSpan, startIndex,
                        startIndex + mSearchTerm.length(), 0);

                // Binds the SpannableString to the display name View object
                holder.text1.setText(highlightedName);

                // Since the search string matched the name, this hides the
                // secondary message
                holder.text2.setVisibility(View.GONE);
            }

            // Processes the QuickContactBadge. A QuickContactBadge first
            // appears as a contact's
            // thumbnail image with styling that indicates it can be touched for
            // additional
            // information. When the user clicks the image, the badge expands
            // into a dialog box
            // containing the contact's details and icons for the built-in apps
            // that can handle
            // each detail type.

            // Generates the contact lookup Uri

            new Thread(new Runnable() {
                public void run() {
                    final Uri contactUri = ContactsContract.Contacts.getLookupUri(
                            cursor.getLong(ContactsQuery.ID),
                            cursor.getString(ContactsQuery.LOOKUP_KEY));

                    // Binds the contact's lookup Uri to the QuickContactBadge
                    holder.icon.assignContactUri(contactUri);

                    // Loads the thumbnail image pointed to by photoUri into the
                    // QuickContactBadge in a
                    // background worker thread
                    mImageLoader.loadImage(photoUri, holder.icon);
                }

            });
        }

        /**
         * Overrides swapCursor to move the new Cursor into the AlphabetIndex as
         * well as the CursorAdapter.
         */
        @Override
        public Cursor swapCursor(Cursor newCursor) {
            // Update the AlphabetIndexer with new cursor as well
            mAlphabetIndexer.setCursor(newCursor);
            return super.swapCursor(newCursor);
        }

        /**
         * An override of getCount that simplifies accessing the Cursor. If the
         * Cursor is null, getCount returns zero. As a result, no test for
         * Cursor == null is needed.
         */
        @Override
        public int getCount() {
            if (getCursor() == null) {
                return 0;
            }
            return super.getCount();
        }

        /**
         * Defines the SectionIndexer.getSections() interface.
         */
        @Override
        public Object[] getSections() {
            return mAlphabetIndexer.getSections();
        }

        /**
         * Defines the SectionIndexer.getPositionForSection() interface.
         */
        @Override
        public int getPositionForSection(int i) {
            if (getCursor() == null) {
                return 0;
            }
            return mAlphabetIndexer.getPositionForSection(i);
        }

        /**
         * Defines the SectionIndexer.getSectionForPosition() interface.
         */
        @Override
        public int getSectionForPosition(int i) {
            if (getCursor() == null) {
                return 0;
            }
            return mAlphabetIndexer.getSectionForPosition(i);
        }

        /**
         * A class that defines fields for each resource ID in the list mPassengerTrip
         * layout. This allows ContactsAdapter.newView() to store the IDs once,
         * when it inflates the layout, instead of calling findViewById in each
         * iteration of bindView.
         */
        private class ViewHolder {
            TextView text1;
            TextView text2;
            QuickContactBadge icon;
            // CheckBox selectnumber;
        }
    }

    /**
     * This interface must be implemented by any activity that loads this
     * fragment. When an interaction occurs, such as touching an mPassengerTrip from the
     * ListView, these callbacks will be invoked to communicate the event back
     * to the activity.
     */
    public interface OnContactsInteractionListener {
        /**
         * Called when a contact is selected from the ListView.
         *
         * @param contactUri The contact Uri.
         */
        public void onContactSelected(Uri contactUri);

        /**
         * Called when the ListView selection is cleared like when a contact
         * search is taking place or is finishing.
         */
        public void onSelectionCleared();
    }

    /**
     * This interface defines constants for the Cursor and CursorLoader, based
     * on constants defined in the
     * {@link ContactsContract.Contacts} class.
     */
    public interface ContactsQuery {

        // An identifier for the loader
        final static int QUERY_ID = 1;

        // A content URI for the Contacts table
        final static Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;

        // The search/filter query Uri
        final static Uri FILTER_URI = ContactsContract.Contacts.CONTENT_FILTER_URI;

        // The selection clause for the CursorLoader query. The search criteria
        // defined here
        // restrict contactResults to contacts that have a display name and are linked
        // to visible groups.
        // Notice that the search on the string provided by the user is
        // implemented by appending
        // the search string to CONTENT_FILTER_URI.

        final static String SELECTION = (Utils.hasHoneycomb() ? ContactsContract.Contacts._ID
                : ContactsContract.Contacts.HAS_PHONE_NUMBER)
                + "<>''"
                + " AND "
                + ContactsContract.Contacts.IN_VISIBLE_GROUP + "=1";

        // The desired sort order for the returned Cursor. In Android 3.0 and
        // later, the primary
        // sort key allows for localization. In earlier versions. use the
        // display name as the sort
        // key.
        final static String SORT_ORDER = Utils.hasHoneycomb() ? ContactsContract.Contacts.SORT_KEY_PRIMARY
                : ContactsContract.Contacts.HAS_PHONE_NUMBER;

        // The projection for the CursorLoader query. This is a list of columns
        // that the Contacts
        // Provider should return in the Cursor.
        final static String[] PROJECTION = {

                // The contact's row id
                ContactsContract.Contacts._ID,

                // A pointer to the contact that is guaranteed to be more
                // permanent than _ID. Given
                // a contact's current _ID value and LOOKUP_KEY, the Contacts
                // Provider can generate
                // a "permanent" contact URI.
                ContactsContract.Contacts.LOOKUP_KEY,

                // In platform version 3.0 and later, the Contacts table
                // contains
                // DISPLAY_NAME_PRIMARY, which either contains the contact's
                // displayable name or
                // some other useful identifier such as an email address. This
                // column isn't
                // available in earlier versions of Android, so you must use
                // Contacts.DISPLAY_NAME
                // instead.
                Utils.hasHoneycomb() ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
                        : ContactsContract.Contacts.DISPLAY_NAME,

                // In Android 3.0 and later, the thumbnail image is pointed to
                // by
                // PHOTO_THUMBNAIL_URI. In earlier versions, there is no direct
                // pointer; instead,
                // you generate the pointer from the contact's ID value and
                // constants defined in
                // android.provider.ContactsContract.Contacts.
                Utils.hasHoneycomb() ? ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
                        : ContactsContract.Contacts._ID,

                Utils.hasHoneycomb() ? ContactsContract.Contacts.HAS_PHONE_NUMBER : ContactsContract.Contacts._ID,

                // The sort order column for the returned Cursor, used by the
                // AlphabetIndexer
                SORT_ORDER,};

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int LOOKUP_KEY = 1;
        final static int DISPLAY_NAME = 2;
        final static int PHOTO_THUMBNAIL_DATA = 3;
        final static int SORT_KEY = 4;
        final static int HAS_PHONE_NUMBER = 5;
    }

}
