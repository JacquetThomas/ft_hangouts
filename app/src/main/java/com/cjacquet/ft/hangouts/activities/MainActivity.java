package com.cjacquet.ft.hangouts.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.cjacquet.ft.hangouts.R;
import com.cjacquet.ft.hangouts.contacts.ContactListAdapter;
import com.cjacquet.ft.hangouts.contacts.ContactSummary;
import com.cjacquet.ft.hangouts.utils.CustomIntent;
import com.cjacquet.ft.hangouts.utils.LocaleHelper;
import com.cjacquet.ft.hangouts.utils.Theme;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry.COLUMN_CONTACT_BDAY;
import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry.COLUMN_CONTACT_FAV;
import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry.COLUMN_CONTACT_LASTNAME;
import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry.COLUMN_CONTACT_NAME;
import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry.COLUMN_CONTACT_PHONE;
import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry.CONTENT_URI;
import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry._ID;

/**
 * Displays list of contacts that were entered and stored in the app.
 */
public class MainActivity extends BaseAppCompatActivity {

    private static final int CONTACT_LOADER = 0;

    private List<ContactSummary> contactSummaries = new ArrayList<>();
    private ContactListAdapter mContactAdapter;
    private HashMap<String, Integer> mapIndex = new LinkedHashMap<>();
    private RecyclerView.SmoothScroller smoothScroller;
    private LinearLayoutManager layoutManager = new LinearLayoutManager(this);

    private IntentFilter intentFilter;
    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Uri uri = Uri.parse(intent.getExtras().get("uri").toString());
            getContentResolver().notifyChange(uri, null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = pref.getString(getSpPrefLang(), null);
        if (lang != null) {
            LocaleHelper.setLocale(this, lang);
        } else {
            LocaleHelper.setLocale(this, getResources().getConfiguration().locale.getLanguage());
        }
        setContentView(R.layout.activity_main_list);

        /* --------------- Register the receiver --------------- */
        intentFilter = new IntentFilter();
        intentFilter.addAction(CustomIntent.UNKNOWN_SMS_RECEIVED);
        registerReceiver(intentReceiver, intentFilter);
        /* ----------------------------------------------------- */

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        if (contactSummaries.isEmpty())
            emptyView.setVisibility(View.VISIBLE);
        else
            emptyView.setVisibility(View.GONE);

        // Find the ListView which will be populated with the contact data
        RecyclerView contactRecyclerView = findViewById(R.id.listview_contact);
        mContactAdapter = new ContactListAdapter(contactSummaries);
        contactRecyclerView.setLayoutManager(layoutManager);
        contactRecyclerView.setAdapter(mContactAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(contactRecyclerView.getContext(),
                layoutManager.getOrientation());
        contactRecyclerView.addItemDecoration(dividerItemDecoration);
        smoothScroller = new LinearSmoothScroller(this) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        // Init Loader
        LoaderManager.LoaderCallbacks<Cursor> mCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                mapIndex.clear();
                contactSummaries.clear();
                mContactAdapter.updateData(contactSummaries);
                String orderBy = COLUMN_CONTACT_FAV + " DESC, " + "upper(" + COLUMN_CONTACT_NAME + ") ASC";
                String[] projection = {_ID, COLUMN_CONTACT_NAME, COLUMN_CONTACT_LASTNAME, COLUMN_CONTACT_PHONE, COLUMN_CONTACT_FAV, COLUMN_CONTACT_BDAY};

                return new CursorLoader(getApplicationContext(), CONTENT_URI, projection, null, null,  orderBy);
            }

            @Override
            public void onLoadFinished(androidx.loader.content.Loader<Cursor> loader, Cursor cursor) {
                contactSummaries.clear();
                mapIndex.clear();
                LinearLayout indexLayout = findViewById(R.id.side_index);
                indexLayout.removeAllViews();
                cursor.moveToFirst();
                int i = 0;
                while(!cursor.isAfterLast()) {
                    int id = cursor.getInt(cursor.getColumnIndex(_ID));
                    String name = cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_NAME));
                    String lastname = cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_LASTNAME));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_PHONE));
                    boolean fav = "1".equals(cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_FAV)));
                    String bDay = cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_BDAY));
                    String index = null;
                    if (fav)
                        index = "*";
                    else
                        index = name.substring(0, 1).toUpperCase();
                    if (mapIndex.get(index) == null) {
                        mapIndex.put(index, i++);
                        contactSummaries.add(new ContactSummary(index, -1, index, null, null, fav, null));
                    }
                    i++;
                    contactSummaries.add(new ContactSummary(null, id, name, lastname, phoneNumber, fav, bDay));
                    cursor.moveToNext();
                }
                displayIndex();
                mContactAdapter.updateData(contactSummaries);
                mContactAdapter.notifyDataSetChanged();
                View emptyView = findViewById(R.id.empty_view);
                if (contactSummaries.isEmpty())
                    emptyView.setVisibility(View.VISIBLE);
                else
                    emptyView.setVisibility(View.GONE);
            }

            @Override
            public void onLoaderReset(androidx.loader.content.Loader<Cursor> loader) {
                loader.reset();
            }
        };
        LoaderManager.getInstance(this).initLoader(CONTACT_LOADER, null, mCallbacks);
    }

    private void displayIndex() {
        LinearLayout indexLayout = findViewById(R.id.side_index);

        List<String> indexList = new ArrayList<>(mapIndex.keySet());
        for (String index : indexList) {
            TextView textView = (TextView) getLayoutInflater().inflate(
                    R.layout.side_idex_item, null);
            textView.setText(index);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv = v.findViewById(R.id.side_index_item);
                    Integer index = mapIndex.get(tv.getText().toString());
                    if (index != null) {
                        smoothScroller.setTargetPosition(index);
                        layoutManager.startSmoothScroll(smoothScroller);
                    }
                }
            });
            textView.setClickable(true);
            indexLayout.addView(textView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        int itemId = item.getItemId();// Respond to a click on the "Delete all entries" menu option
        if (itemId == R.id.action_delete_all_entries) {
            this.deleteContacts();
            return true;
            // Respond to a click on the "Change language" menu option
        } else if (itemId == R.id.action_orange_theme) {
            setTheme(Theme.ORANGE.getThemeId());
            recreate();
            return true;
            // Respond to a click on the "Change to blue" menu option
        } else if (itemId == R.id.action_blue_theme) {
            setTheme(Theme.BLUE.getThemeId());
            recreate();
            return true;
            // Respond to a click on the "Change to green" menu option
        } else if (itemId == R.id.action_green_theme) {
            setTheme(Theme.GREEN.getThemeId());
            recreate();
            return true;
        } else if (itemId == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.action_add_contact) {
            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(CustomIntent.UNKNOWN_SMS_RECEIVED);
        registerReceiver(intentReceiver, intentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(intentReceiver);
        } catch (Exception e) {
            Log.e(this.getPackageName(), e.getMessage());
        }
    }

    private void deleteContacts() {
        getContentResolver().delete(CONTENT_URI, null, null);
    }
}
