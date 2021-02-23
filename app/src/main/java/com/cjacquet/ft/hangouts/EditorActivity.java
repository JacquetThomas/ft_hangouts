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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.cjacquet.ft.hangouts.data.ContactContract.ContactEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    /** TextView field to display the contact's name */
    private TextView mNameTextView;

    /** TextView field to display the contact's lastname */
    private TextView mLastnameTextView;

    /** TextView field to display the contact's phone */
    private TextView mPhoneTextView;

    /** TextView field to display the contact's birthday */
    private TextView mBDayTextView;

    /** TextView field to display the contact's mail */
    private TextView mMailTextView;

    private FloatingActionButton fab;

    private Menu menu;

    private final static int EXISTING_CONTACT_LOADER = 0;

    /** Content URI for the existing contact (null if it's a new contact) */
    private Uri mCurrentContactUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentContactUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_contact_name);
        mLastnameEditText = (EditText) findViewById(R.id.edit_contact_lastname);
        mNameTextView = (TextView) findViewById(R.id.edit_contact_name);
        mLastnameTextView = (TextView) findViewById(R.id.edit_contact_lastname);
        mPhoneEditText = (EditText) findViewById(R.id.edit_contact_phone);
        mBDayEditText = (EditText) findViewById(R.id.edit_contact_bday);
        mMailEditText = (EditText) findViewById(R.id.edit_contact_mail);
        fab = (FloatingActionButton) findViewById(R.id.fab_sms);

        if (mCurrentContactUri != null) {
            setTitle(getString(R.string.editor_activity_title_edit_contact));
            getLoaderManager().initLoader(EXISTING_CONTACT_LOADER, null, this);
            // Setup FAB to open MessageActivity
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(EditorActivity.this, MessageActivity.class);
                    intent.putExtra("phoneNumber", mPhoneEditText.getText().toString());
                    intent.putExtra("contactName", mNameEditText.getText().toString() + " " + mLastnameEditText.getText().toString());
                    intent.putExtra("contactId", mCurrentContactUri.getLastPathSegment());
                    startActivity(intent);
                }
            });
        } else {
            setTitle(getString(R.string.editor_activity_title_new_contact));
            fab.setVisibility(View.GONE);
        }
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
            case R.id.action_edit:
                this.showOption(R.id.action_save);
                this.hideOption(R.id.action_edit);
                this.switchMenuToEdit();
                this.switchFieldToEdit();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (menu.findItem(R.id.action_save).isVisible()) {
                    this.switchMenuToShow();
                    this.hideOption(R.id.action_save);
                }else {
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


    private void switchFieldToShow() {
        this.mNameTextView.setVisibility(View.VISIBLE);
        this.mNameEditText.setVisibility(View.GONE);
        this.mLastnameTextView.setVisibility(View.VISIBLE);
        this.mLastnameEditText.setVisibility(View.GONE);
    }

    private void switchFieldToEdit() {
        this.mNameTextView.setVisibility(View.GONE);
        this.mNameEditText.setVisibility(View.VISIBLE);
        this.mLastnameTextView.setVisibility(View.GONE);
        this.mLastnameEditText.setVisibility(View.VISIBLE);
    }

    private void switchMenuToShow() {
        this.showOption(R.id.action_edit);
        this.hideOption(R.id.action_save);
        setTitle(this.mNameTextView.getText().toString() + " " + this.mLastnameTextView.getText().toString());
    }

    private void switchMenuToEdit() {
        this.showOption(R.id.action_save);
        this.hideOption(R.id.action_edit);
        setTitle(R.string.editor_activity_title_edit_contact);
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
            mNameTextView.setText(name);
            mLastnameTextView.setText(lastname);
            mPhoneEditText.setText(phone);
            mBDayEditText.setText(bday);
            mMailEditText.setText(mail);

            this.switchFieldToShow();
            setTitle(this.mNameTextView.getText().toString() + " " + this.mLastnameTextView.getText().toString());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}