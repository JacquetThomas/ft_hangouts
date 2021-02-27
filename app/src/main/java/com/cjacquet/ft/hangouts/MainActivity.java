package com.cjacquet.ft.hangouts;

import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.cjacquet.ft.hangouts.contacts.ContactCursorAdapter;
import com.cjacquet.ft.hangouts.contacts.EditorActivity;
import com.cjacquet.ft.hangouts.utils.LocaleHelper;
import com.cjacquet.ft.hangouts.utils.Theme;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
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

    ContactCursorAdapter mCursorAdapter;

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

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the contact data
        ListView contactListView = (ListView) findViewById(R.id.lvItems);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        contactListView.setEmptyView(emptyView);

        mCursorAdapter = new ContactCursorAdapter(this, null);
        contactListView.setAdapter(mCursorAdapter);

        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                Uri currentContactUri = ContentUris.withAppendedId(CONTENT_URI, id);
                intent.setData(currentContactUri);

                startActivity(intent);
            }
        });

        // Init Loader
        LoaderManager.LoaderCallbacks<Cursor> mCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String[] projection = {_ID, COLUMN_CONTACT_NAME, COLUMN_CONTACT_LASTNAME, COLUMN_CONTACT_PHONE};
                return new CursorLoader(getApplicationContext(), CONTENT_URI, projection, null, null, null);
            }

            @Override
            public void onLoadFinished(androidx.loader.content.Loader<Cursor> loader, Cursor cursor) {
                mCursorAdapter.swapCursor(cursor);
            }

            @Override
            public void onLoaderReset(androidx.loader.content.Loader<Cursor> loader) {
                mCursorAdapter.swapCursor(null);
            }
        };
        LoaderManager.getInstance(this).initLoader(CONTACT_LOADER, null, mCallbacks);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
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
        } else if (itemId == R.id.action_language_change) {
            this.showPopupWindow();
            return true;
            // Respond to a click on the "Change to orange" menu option
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
        }
        return super.onOptionsItemSelected(item);
    }

    public void showPopupWindow() {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        final PopupWindow popupWindow = new PopupWindow(popupView, WRAP_CONTENT, WRAP_CONTENT, false);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(20);
        }
        popupWindow.showAtLocation(getWindow().getDecorView().getRootView(), Gravity.CENTER, 0, 0);
        this.setupRadioGroup(popupView);
        this.setupValidateCancel(popupWindow, popupView);
    }

    private void setupValidateCancel(final PopupWindow popupWindow, final View popupView) {
        Button cancel = popupView.findViewById(R.id.button_cancel);
        Button validate = popupView.findViewById(R.id.button_validate);

        validate.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                RadioGroup changeLang = popupView.findViewById(R.id.language_radio_group);
                Locale selectedLocale = null;
                int checkedRadioButtonId = changeLang.getCheckedRadioButtonId();

                if (checkedRadioButtonId == R.id.radioButtonEn) {
                    selectedLocale = Locale.ENGLISH;
                } else if (checkedRadioButtonId == R.id.radioButtonFr) {
                    selectedLocale = Locale.FRANCE;
                }

                if (selectedLocale != null) {
                    LocaleHelper.setLocale(getApplicationContext(), selectedLocale.getLanguage());

                    Locale.setDefault(selectedLocale);
                    Configuration config = getBaseContext().getResources().getConfiguration();
                    config.locale = selectedLocale;
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());

                    popupWindow.dismiss();
                    recreate();
                }
            }
        });

        cancel.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void setupRadioGroup(final View view) {
        RadioButton switcherFr = view.findViewById(R.id.radioButtonFr);
        RadioButton switcherEn = view.findViewById(R.id.radioButtonEn);

        // Get current locale
        String locale = LocaleHelper.getLanguage(this);
        switch (locale) {
            case "fr":
                switcherFr.setChecked(true);
                switcherEn.setChecked(false);
                break;
            case "en":
                switcherEn.setChecked(true);
                switcherFr.setChecked(false);
                break;
            default:
                break;
        }
    }

    private void deleteContacts() {
        getContentResolver().delete(CONTENT_URI, null, null);
    }
}
