package com.cjacquet.ft.hangouts;

import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.NavUtils;

import com.cjacquet.ft.hangouts.data.ContactContract.ContactEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

import static com.cjacquet.ft.hangouts.data.ContactContract.ContactEntry.CONTENT_URI;

/**
 * Allows user to create a new contact or edit an existing one.
 */
public class EditorActivity extends BasePausableAppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String contactId;

    /** EditText field to enter the contact's name */
    private EditText mNameEditText;
    private String mName;

    /** EditText field to enter the contact's lastname */
    private EditText mLastnameEditText;
    private String mLastname;

    /** EditText field to enter the contact's phone */
    private EditText mPhoneEditText;
    private String mPhone;

    /** EditText field to enter the contact's birthday */
    private EditText mBDayEditText;
    private String mBDay;
    private DatePickerDialog picker;

    /** EditText field to enter the contact's mail */
    private EditText mMailEditText;
    private String mMail;

    private FloatingActionButton fab;

    private Menu menu;

    private String contactName;

    private final static int EXISTING_CONTACT_LOADER = 0;

    /** Content URI for the existing contact (null if it's a new contact) */
    private Uri mCurrentContactUri;

    @Override
    public void recreate() {
        super.recreate();
        setupActivity();
    }

    private void setupActivity() {
        if (mCurrentContactUri != null) {
            contactId = mCurrentContactUri.getLastPathSegment();
            setTitle(getString(R.string.editor_activity_title_edit_contact));
            getLoaderManager().initLoader(EXISTING_CONTACT_LOADER, null, this);
            // Setup FAB to open MessageActivity
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(EditorActivity.this, MessageActivity.class);
                    intent.putExtra("phoneNumber", mPhoneEditText.getText().toString());
                    intent.putExtra("contactName", contactName);
                    intent.putExtra("contactId", contactId);
                    startActivity(intent);
                }
            });
        } else {
            setTitle(getString(R.string.editor_activity_title_new_contact));
            fab.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentContactUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_contact_name);
        mLastnameEditText = (EditText) findViewById(R.id.edit_contact_lastname);
        mPhoneEditText = (EditText) findViewById(R.id.edit_contact_phone);
        mBDayEditText = (EditText) findViewById(R.id.edit_contact_bday);
        mMailEditText = (EditText) findViewById(R.id.edit_contact_mail);
        fab = (FloatingActionButton) findViewById(R.id.fab_sms);

        mBDayEditText.setInputType(InputType.TYPE_NULL);
        mBDayEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(EditorActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                mBDayEditText.setText(Utils.setFormattedDate(year, monthOfYear, dayOfMonth));
                            }
                        }, year, month, day);
                picker.show();
            }
        });
        setupActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        if (mCurrentContactUri != null) {
            this.showOption(R.id.action_edit);
            this.hideOption(R.id.action_save);
        } else {
            this.showOption(R.id.action_save);
            this.hideOption(R.id.action_edit);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveContacts();
                if (contactId != null && !contactId.isEmpty())
                    mCurrentContactUri = ContentUris.withAppendedId(CONTENT_URI, Long.parseLong(contactId));
                recreate();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                String text = "Error, cannot delete contact.";
                if (this.deleteContact() == 1)
                    text = "Contact succesfully deleted.";
                Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                toast.show();
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_edit:
                this.showOption(R.id.action_save);
                this.hideOption(R.id.action_edit);
                this.switchMenuToEdit();
                this.switchFieldToEdit();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (menu.findItem(R.id.action_save).isVisible()) {
                    this.resetUnsavedFields();
                    this.switchMenuToShow();
                    this.switchFieldToShow();
                } else {
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
            }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void hideOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    private void showOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }

    /**
     * Disable all editable fields to enter in show mode.
     */
    private void switchFieldToShow() {
        this.mNameEditText.setEnabled(false);
        this.mLastnameEditText.setEnabled(false);
        this.mPhoneEditText.setEnabled(false);
        this.mMailEditText.setEnabled(false);
        this.mBDayEditText.setEnabled(false);
        this.fab.setVisibility(View.VISIBLE);
    }

    /**
     * Enable all editable fields to enter in edit mode.
     */
    private void switchFieldToEdit() {
        this.mNameEditText.setEnabled(true);
        this.mLastnameEditText.setEnabled(true);
        this.mPhoneEditText.setEnabled(true);
        this.mMailEditText.setEnabled(true);
        this.mBDayEditText.setEnabled(true);
        this.fab.setVisibility(View.INVISIBLE);
    }

    /**
     * Change menu's icons visibility pass in show mode.
     */
    private void switchMenuToShow() {
        this.showOption(R.id.action_edit);
        this.hideOption(R.id.action_save);
        setTitle(this.mNameEditText.getText().toString() + " " + this.mLastnameEditText.getText().toString());
    }

    /**
     * Change menu's icons visibility pass in edit mode.
     */
    private void switchMenuToEdit() {
        this.showOption(R.id.action_save);
        this.hideOption(R.id.action_edit);
        setTitle(R.string.editor_activity_title_edit_contact);
    }

    /**
     * Method call to reset field whom change but has got savec.
     */
    private void resetUnsavedFields() {
        if (!this.mName.equals(this.mNameEditText.getText().toString()))
            this.mNameEditText.setText("");
        if (!this.mLastname.equals(this.mLastnameEditText.getText().toString()))
            this.mLastnameEditText.setText("");
        if (!this.mPhone.equals(this.mPhoneEditText.getText().toString()))
            this.mPhoneEditText.setText("");
        if (!this.mMail.equals(this.mMailEditText.getText().toString()))
            this.mMailEditText.setText("");
        if (!this.mBDay.equals(this.mBDayEditText.getText().toString()))
            this.mBDayEditText.setText("");
    }

    /**
     * Method call to save Contact's informations.
     */
    private void saveContacts() {
        Uri res = null;
        int updateRows = 0;
        String name =  mNameEditText.getText().toString().trim();
        String lastname = mLastnameEditText.getText().toString().trim();
        String phone = mPhoneEditText.getText().toString().trim();
        String mail = mMailEditText.getText().toString().trim();
        String bday = mBDayEditText.getText().toString().trim();

        if (mCurrentContactUri == null && TextUtils.isEmpty(name) && TextUtils.isEmpty(lastname)
                && TextUtils.isEmpty(phone))
            return ;

        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactEntry.COLUMN_CONTACT_NAME, name);
        contentValues.put(ContactEntry.COLUMN_CONTACT_LASTNAME, lastname);
        contentValues.put(ContactEntry.COLUMN_CONTACT_PHONE, phone);
        contentValues.put(ContactEntry.COLUMN_CONTACT_MAIL, mail);
        contentValues.put(ContactEntry.COLUMN_CONTACT_BDAY, bday);

        if (mCurrentContactUri != null) {
            updateRows = getContentResolver().update(mCurrentContactUri, contentValues, null, null);
        } else {
            res = getContentResolver().insert(ContactEntry.CONTENT_URI, contentValues);
        }

        CharSequence text;
        if (res == null && updateRows != 1) {
            contactId = res.getLastPathSegment();
            text = getString(R.string.editor_insert_contact_failed);
        }
        else
            text = getString(R.string.editor_insert_contact_success);

        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Method to delete a Contact.
     * @return
     */
    private int deleteContact() {
        return (getContentResolver().delete(mCurrentContactUri, null, null));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all contact attributes, define a projection that contains
        // all columns from the contact table
        String[] projection = {
                ContactEntry._ID,
                ContactEntry.COLUMN_CONTACT_NAME,
                ContactEntry.COLUMN_CONTACT_LASTNAME,
                ContactEntry.COLUMN_CONTACT_PHONE,
                ContactEntry.COLUMN_CONTACT_MAIL,
                ContactEntry.COLUMN_CONTACT_BDAY,
                ContactEntry.COLUMN_CONTACT_FAV
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentContactUri,         // Query the content URI for the current contact
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Find the columns of contact attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_NAME);
            int lastnameColumnIndex = cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_LASTNAME);
            int phoneColumnIndex = cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_PHONE);
            int mailColumnIndex = cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_MAIL);
            int bdayColumnIndex = cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_BDAY);
            int favColumnIndex = cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_FAV);

            // Extract out the value from the Cursor for the given column index
            this.mName = cursor.getString(nameColumnIndex);
            this.mLastname = cursor.getString(lastnameColumnIndex);
            this.mPhone = cursor.getString(phoneColumnIndex);
            this.mMail = cursor.getString(mailColumnIndex);
            this.mBDay = cursor.getString(bdayColumnIndex);
            String fav = cursor.getString(favColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(this.mName);
            mLastnameEditText.setText(this.mLastname);
            mPhoneEditText.setText(this.mPhone);
            mBDayEditText.setText(this.mBDay);
            mMailEditText.setText(this.mMail);

            this.switchFieldToShow();
            this.contactName = this.mNameEditText.getText().toString() + " " + this.mLastnameEditText.getText().toString();
            setTitle(this.contactName);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}