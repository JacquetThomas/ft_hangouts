package com.cjacquet.ft.hangouts;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cjacquet.ft.hangouts.data.ContactContract.ContactEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

import static com.cjacquet.ft.hangouts.data.ContactContract.ContactEntry.COLUMN_CONTACT_LASTNAME;
import static com.cjacquet.ft.hangouts.data.ContactContract.ContactEntry.COLUMN_CONTACT_NAME;
import static com.cjacquet.ft.hangouts.data.ContactContract.ContactEntry.COLUMN_CONTACT_PHONE;
import static com.cjacquet.ft.hangouts.data.ContactContract.ContactEntry.CONTENT_URI;
import static com.cjacquet.ft.hangouts.data.ContactContract.ContactEntry._ID;

/**
 * Displays list of contacts that were entered and stored in the app.
 */
public class CatalogActivity extends BaseAppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int CONTACT_LOADER = 0;

    ContactCursorAdapter mCursorAdapter;

    private static CatalogActivity instance;

    public static CatalogActivity getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance.getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        LocaleHelper.setLocale(this, getResources().getConfiguration().locale.getLanguage());
        instance = this;
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
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
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                Uri currentContactUri = ContentUris.withAppendedId(CONTENT_URI, id);
                intent.setData(currentContactUri);

                startActivity(intent);
            }
        });

        // Init Loader
        getLoaderManager().initLoader(CONTACT_LOADER, null, this);
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
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                this.insertContacts();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                this.deleteContacts();
                return true;
            // Respond to a click on the "Change language" menu option
            case R.id.action_language_change:
                this.showPopupWindow();
                return true;
            // Respond to a click on the "Change to orange" menu option
            case R.id.action_orange_theme:
                colorTheme = Theme.ORANGE;
                setTheme(colorTheme.getValue());
                recreate();
                return true;
            // Respond to a click on the "Change to blue" menu option
            case R.id.action_blue_theme:
                colorTheme = Theme.BLUE;
                setTheme(colorTheme.getValue());
                recreate();
                return true;
            // Respond to a click on the "Change to green" menu option
            case R.id.action_green_theme:
                colorTheme = Theme.GREEN;
                setTheme(colorTheme.getValue());
                recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showPopupWindow() {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, false);

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
                switch (changeLang.getCheckedRadioButtonId()) {
                    case R.id.radioButtonEn:
                        selectedLocale = Locale.ENGLISH;
                        break;
                    case R.id.radioButtonFr:
                        selectedLocale = Locale.FRANCE;
                        break;
                    default:
                        break;
                }
                if (selectedLocale != null) {
                    LocaleHelper.setLocale(getApplicationContext(), selectedLocale.getLanguage());

                    Locale.setDefault(selectedLocale);
                    Configuration config = getBaseContext().getResources().getConfiguration();
                    config.locale = selectedLocale;
                    getBaseContext().getResources().updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());

                    popupWindow.dismiss();
                    instance = getInstance();
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

    private void insertContacts() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CONTACT_NAME, "Toto");
        contentValues.put(COLUMN_CONTACT_LASTNAME, "Terrier");
        contentValues.put(ContactEntry.COLUMN_CONTACT_PHONE, "000-000-000");
        getContentResolver().insert(ContactEntry.CONTENT_URI, contentValues);
    }

    private void deleteContacts() {
        getContentResolver().delete(CONTENT_URI, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {_ID, COLUMN_CONTACT_NAME, COLUMN_CONTACT_LASTNAME, COLUMN_CONTACT_PHONE};
        return new CursorLoader(this, CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
