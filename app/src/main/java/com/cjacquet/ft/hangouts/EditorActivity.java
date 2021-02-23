/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cjacquet.ft.hangouts;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cjacquet.ft.hangouts.data.ContactContract.ContactEntry;
import com.cjacquet.ft.hangouts.fragments.DatePickerFragment;

/**
 * Allows user to create a new contact or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the contact's name */
    private EditText mNameEditText;

    /** EditText field to enter the contact's lastname */
    private EditText mLastnameEditText;

    /** EditText field to enter the contact's phone */
    private EditText mPhoneEditText;

    /** EditText field to enter the contact's birthday */
    private EditText mBDayEditText;

    /** EditText field to enter the contact's mail */
    private EditText mMailEditText;

    private final static int EXISTING_CONTACT_LOADER = 0;

    /** Content URI for the existing contact (null if it's a new contact) */
    private Uri mCurrentContactUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentContactUri = intent.getData();

        if (mCurrentContactUri != null) {
            setTitle(getString(R.string.editor_activity_title_edit_contact));
            getLoaderManager().initLoader(EXISTING_CONTACT_LOADER, null, this);
        } else {
            setTitle(getString(R.string.editor_activity_title_new_contact));
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_contact_name);
        mLastnameEditText = (EditText) findViewById(R.id.edit_contact_lastname);
        mPhoneEditText = (EditText) findViewById(R.id.edit_contact_phone);
        mBDayEditText = (EditText) findViewById(R.id.edit_contact_bday);
        mMailEditText = (EditText) findViewById(R.id.edit_contact_mail);

        // Setup FAB to open MessageActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_sms);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditorActivity.this, MessageActivity.class);
                intent.putExtra("phoneNumber", mPhoneEditText.getText().toString());
                intent.putExtra("contactName", mNameEditText.getText().toString());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveContacts();
                finish();
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
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
        if (res == null && updateRows != 1)
            text = getString(R.string.editor_insert_contact_failed);
        else
            text = getString(R.string.editor_insert_contact_success);

        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

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
            String name = cursor.getString(nameColumnIndex);
            String lastname = cursor.getString(lastnameColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            String mail = cursor.getString(mailColumnIndex);
            String bday = cursor.getString(bdayColumnIndex);
            String fav = cursor.getString(favColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mLastnameEditText.setText(lastname);
            mPhoneEditText.setText(phone);
            mBDayEditText.setText(bday);
            mMailEditText.setText(mail);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

}