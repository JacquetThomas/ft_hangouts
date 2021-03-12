package com.cjacquet.ft.hangouts.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.cjacquet.ft.hangouts.R;
import com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry;
import com.cjacquet.ft.hangouts.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry.CONTENT_URI;

public class EditorActivity extends BaseAppCompatActivity {
    private static final int REQUEST_SMS_PERMISSION = 3004;
    private static final int REQUEST_CALL_PERMISSION = 3005;
    private String contactId;
    private String contactName;

    private boolean requestPermission;

    private EditText mNameEditText;
    private String mName;

    private EditText mLastnameEditText;
    private String mLastname;

    private EditText mPhoneEditText;
    private String mPhone;

    private EditText mBDayEditText;
    private String mBDay;
    private DatePickerDialog picker;

    private EditText mMailEditText;
    private String mMail;

    private Switch favSwitch;
    private boolean favContact;

    private FloatingActionButton fabSms;
    private FloatingActionButton fabCall;

    private Menu menu;

    private static final int EXISTING_CONTACT_LOADER = 0;

    private Uri mCurrentContactUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentContactUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_contact_name);
        mLastnameEditText = findViewById(R.id.edit_contact_lastname);
        mPhoneEditText = findViewById(R.id.edit_contact_phone);
        mBDayEditText = findViewById(R.id.edit_contact_bday);
        mMailEditText = findViewById(R.id.edit_contact_mail);
        favSwitch = findViewById(R.id.favContact);
        fabSms = findViewById(R.id.fab_sms);
        fabCall = findViewById(R.id.fab_call);

        setupActivity();
    }

    private void setupActivity() {

        mPhoneEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        mMailEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mMailEditText.isFocusable()) {
                    if (mMailEditText.getText().toString().length() > 0) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:" + mMailEditText.getText().toString()));
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                }
            }
        });
        mBDayEditText.setInputType(InputType.TYPE_NULL);
        mBDayEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBDayEditText.isFocusable())
                    return ;
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

        if (mCurrentContactUri != null) {
            contactId = mCurrentContactUri.getLastPathSegment();
            setTitle(getResources().getString(R.string.editor_activity_title_edit_contact));
            LoaderManager.LoaderCallbacks<Cursor> mCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
                    return new CursorLoader(getApplicationContext(),   // Parent activity context
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
                        mName = cursor.getString(nameColumnIndex);
                        mLastname = cursor.getString(lastnameColumnIndex);
                        mPhone = cursor.getString(phoneColumnIndex);
                        mMail = cursor.getString(mailColumnIndex);
                        mBDay = cursor.getString(bdayColumnIndex);
                        favContact = "1".equals(cursor.getString(favColumnIndex));

                        // Update the views on the screen with the values from the database
                        mNameEditText.setText(mName);
                        mLastnameEditText.setText(mLastname);
                        mPhoneEditText.setText(mPhone);
                        mBDayEditText.setText(mBDay);
                        mMailEditText.setText(mMail);
                        favSwitch.setChecked(favContact);

                        switchFieldToShow();
                        contactName = mNameEditText.getText().toString() + " " + mLastnameEditText.getText().toString();
                        setTitle(contactName);
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                    // No need to swap cursor, the change is already handle
                }
            };

            LoaderManager.getInstance(this).initLoader(EXISTING_CONTACT_LOADER, null, mCallbacks);

            // Setup FAB SMS to open MessageActivity
            fabSms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utils.getSMSPermission(getApplicationContext())) {
                        Intent intent = new Intent(EditorActivity.this, MessageActivity.class);
                        intent.putExtra("phoneNumber", mPhoneEditText.getText().toString());
                        intent.putExtra("contactName", contactName);
                        intent.putExtra("contactId", contactId);
                        startActivity(intent);
                    } else {
                        getSMSPermission();
                    }
                }
            });

            // Setup FAB Call to open call contact
            fabCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utils.getCallPermission(getApplicationContext())) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPhoneEditText.getText().toString()));
                        startActivity(intent);
                    } else {
                        getCallPermission();
                    }
                }
            });
        } else {
            setTitle(getString(R.string.editor_activity_title_new_contact));
        }

        if (mPhoneEditText.getText() != null && !mPhoneEditText.getText().toString().isEmpty()) {
            fabSms.setVisibility(View.VISIBLE);
            fabCall.setVisibility(View.VISIBLE);
        } else {
            fabSms.setVisibility(View.GONE);
            fabCall.setVisibility(View.GONE);
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
        int itemId = item.getItemId();
        // Respond to a click on the "Save" menu option
        if (itemId == R.id.action_save) {
            if (!saveContact())
                return true;
            if (contactId != null && !contactId.isEmpty())
                mCurrentContactUri = ContentUris.withAppendedId(CONTENT_URI, Long.parseLong(contactId));
            setupActivity();
            switchFieldToShow();
            switchMenuToShow();
            return true;
        }
        // Respond to a click on the "Delete" menu option
        else if (itemId == R.id.action_delete) {
            if (mCurrentContactUri != null) {
                String text = getResources().getString(R.string.editor_delete_contact_ko);
                if (this.deleteContact() == 1)
                    text = getResources().getString(R.string.editor_delete_contact_ok);
                Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                toast.show();
            }
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        // Respond to a click on the "Edit" menu option
        else if (itemId == R.id.action_edit) {
            this.showOption(R.id.action_save);
            this.hideOption(R.id.action_edit);
            this.switchMenuToEdit();
            this.switchFieldToEdit();
            return true;
        }
        // Respond to a click on the "Up" arrow button in the app bar
        else if (itemId == android.R.id.home) {
            // If we are in edit mode we clear the unsaved fields
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
        this.mNameEditText.setFocusable(false);
        this.mLastnameEditText.setFocusable(false);
        this.mPhoneEditText.setFocusable(false);
        this.mMailEditText.setFocusable(false);
        this.mBDayEditText.setFocusable(false);
        this.favSwitch.setClickable(false);
        if (mPhoneEditText.getText() != null && !mPhoneEditText.getText().toString().isEmpty()) {
            fabSms.setVisibility(View.VISIBLE);
            fabCall.setVisibility(View.VISIBLE);
        } else {
            fabSms.setVisibility(View.GONE);
            fabCall.setVisibility(View.GONE);
        }
    }

    /**
     * Enable all editable fields to enter in edit mode.
     */
    private void switchFieldToEdit() {
        this.mNameEditText.setFocusable(true);
        this.mNameEditText.setFocusableInTouchMode(true);
        this.mLastnameEditText.setFocusable(true);
        this.mLastnameEditText.setFocusableInTouchMode(true);
        this.mPhoneEditText.setFocusable(true);
        this.mPhoneEditText.setFocusableInTouchMode(true);
        this.mMailEditText.setFocusable(true);
        this.mMailEditText.setFocusableInTouchMode(true);
        this.mBDayEditText.setFocusable(true);
        this.mBDayEditText.setFocusableInTouchMode(true);
        this.favSwitch.setClickable(true);
        this.fabSms.setVisibility(View.INVISIBLE);
        this.fabCall.setVisibility(View.INVISIBLE);
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
        if (this.mLastname != null && !this.mLastname.equals(this.mLastnameEditText.getText().toString()))
            this.mLastnameEditText.setText("");
        if (this.mPhone != null && !this.mPhone.equals(this.mPhoneEditText.getText().toString()))
            this.mPhoneEditText.setText("");
        if (this.mMail != null && !this.mMail.equals(this.mMailEditText.getText().toString()))
            this.mMailEditText.setText("");
        if (this.mBDay != null && !this.mBDay.equals(this.mBDayEditText.getText().toString()))
            this.mBDayEditText.setText("");
        if (this.favContact != this.favSwitch.isChecked())
            this.favSwitch.setChecked(favContact);
    }

    /**
     * Method call to save Contact's informations.
     */
    private boolean saveContact() {
        Uri res = null;
        int updateRows = 0;
        boolean insert = false;
        String name =  mNameEditText.getText().toString().trim();
        String lastname = mLastnameEditText.getText().toString().trim();
        String phone = mPhoneEditText.getText().toString().trim();
        String mail = mMailEditText.getText().toString().trim();
        String bday = mBDayEditText.getText().toString().trim();
        boolean fav = favSwitch.isChecked();

        if (mCurrentContactUri == null && TextUtils.isEmpty(name)) {
            Toast.makeText(this, getResources().getString(R.string.editor_error_contact_minimum_info), Toast.LENGTH_SHORT).show();
            return insert;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactEntry.COLUMN_CONTACT_NAME, name);
        contentValues.put(ContactEntry.COLUMN_CONTACT_LASTNAME, lastname);
        contentValues.put(ContactEntry.COLUMN_CONTACT_PHONE, phone);
        contentValues.put(ContactEntry.COLUMN_CONTACT_MAIL, mail);
        contentValues.put(ContactEntry.COLUMN_CONTACT_BDAY, bday);
        contentValues.put(ContactEntry.COLUMN_CONTACT_FAV, fav);

        if (mCurrentContactUri != null) {
            updateRows = getContentResolver().update(mCurrentContactUri, contentValues, null, null);
        } else {
            res = getContentResolver().insert(ContactEntry.CONTENT_URI, contentValues);
        }

        CharSequence text = "";
        if (res == null && updateRows != 1) {
            text = getString(R.string.editor_insert_contact_failed);
        }
        else if (res != null || updateRows == 1) {
            if (res != null)
                contactId = res.getLastPathSegment();
            text = getString(R.string.editor_insert_contact_success);
            insert = true;
        }
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
        getSMSPermission();
        return insert;
    }

    /**
     * Method to delete a Contact.
     */
    private int deleteContact() {
        if (mCurrentContactUri != null)
            return (getContentResolver().delete(mCurrentContactUri, null, null));
        else
            return -1;
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    protected void onResume() {
        if (this.requestPermission) {
            this.paused = false;
            this.requestPermission = false;
        }
        super.onResume();
    }

    /* --------------------------- Handle permission -------------------------------- */

    /**
     * Check if we have read permission on SMS and request if not.
     */
    public void getSMSPermission() {
        if (checkSelfPermission(Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            this.requestPermission = true;
            requestPermissions(
                    new String[]{Manifest.permission.READ_SMS}, REQUEST_SMS_PERMISSION);
        }
    }

    /**
     * Check if we have read permission on Call and request if not.
     */
    public void getCallPermission() {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            this.requestPermission = true;
            requestPermissions(
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults == null || grantResults.length == 0)
            return ;
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (REQUEST_SMS_PERMISSION == requestCode) {
                Intent intent = new Intent(EditorActivity.this, MessageActivity.class);
                intent.putExtra("phoneNumber", mPhoneEditText.getText().toString());
                intent.putExtra("contactName", contactName);
                intent.putExtra("contactId", contactId);
                startActivity(intent);
            } else if (REQUEST_CALL_PERMISSION == requestCode) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPhoneEditText.getText().toString()));
                startActivity(intent);
            }
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            if (REQUEST_SMS_PERMISSION == requestCode) {
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.popup_permission_title);
                builder.setMessage(R.string.popup_sms_permission_message);

                // add the buttons
                builder.setPositiveButton(R.string.popup_permission_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // This code is for get permission from setting.
                        final Intent i = new Intent();
                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setData(Uri.parse("package:" + getPackageName()));
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(i);
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton(R.string.popup_permission_ko, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        String text = getResources().getString(R.string.no_sms_permission);
                        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            } else if (REQUEST_CALL_PERMISSION == requestCode) {
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.popup_permission_title);
                builder.setMessage(R.string.popup_call_permission_message);

                // add the buttons
                builder.setPositiveButton(R.string.popup_permission_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // This code is for get permission from setting.
                        final Intent i = new Intent();
                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setData(Uri.parse("package:" + getPackageName()));
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(i);
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton(R.string.popup_permission_ko, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        String text = getResources().getString(R.string.no_call_permission);
                        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }
}