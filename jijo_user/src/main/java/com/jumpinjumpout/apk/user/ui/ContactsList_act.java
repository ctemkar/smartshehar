package com.jumpinjumpout.apk.user.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.jumpinjumpout.apk.R;

public class ContactsList_act extends Activity implements
		ContactsListFragment.OnContactsInteractionListener {
	private boolean isSearchResultView = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_list_act);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_HOME_AS_UP);
		if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
			String searchQuery = getIntent()
					.getStringExtra(SearchManager.QUERY);
			ContactsListFragment mContactsListFragment = (ContactsListFragment) getFragmentManager()
					.findFragmentById(R.id.contact_list);
			isSearchResultView = true;
			mContactsListFragment.setSearchQuery(searchQuery);
			String title = getString(
					R.string.contacts_list_search_results_title, searchQuery);
			setTitle(title);
		}
	}

	@Override
	public void onContactSelected(Uri contactUri) {
		String Contactid = contactUri.getLastPathSegment().toString();
		Intent intent = new Intent();
		intent.putExtra("Contactid", Contactid);
		intent.putExtra("contactUri", contactUri.toString());
		setResult(1, intent);
		finish();// finishing activity
	}

	@Override
	protected void onResume() {
		super.onResume();
		hideKeyboard();
	}

	private void hideKeyboard() {
		View view = this.getCurrentFocus();
		if (view != null) {
			InputMethodManager inputManager = (InputMethodManager) this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(),
					InputMethodManager.RESULT_HIDDEN);
		}
	}

	@Override
	public void onSelectionCleared() {
	}

	@Override
	public boolean onSearchRequested() {
		return !isSearchResultView && super.onSearchRequested();
	}

}
