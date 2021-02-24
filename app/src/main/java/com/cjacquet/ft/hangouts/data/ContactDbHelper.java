package com.cjacquet.ft.hangouts.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "contacts.db";

    // Create a String that contains the SQL statement to create the contacts table
    public static final String SQL_CREATE_CONTACTS_TABLE =  "CREATE TABLE " + ContactContract.ContactEntry.TABLE_NAME + " ("
            + ContactContract.ContactEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ContactContract.ContactEntry.COLUMN_CONTACT_NAME + " TEXT NOT NULL, "
            + ContactContract.ContactEntry.COLUMN_CONTACT_LASTNAME + " TEXT, "
            + ContactContract.ContactEntry.COLUMN_CONTACT_MAIL + " TEXT, "
            + ContactContract.ContactEntry.COLUMN_CONTACT_BDAY + " TEXT, "
            + ContactContract.ContactEntry.COLUMN_CONTACT_FAV + " INTEGER DEFAULT 0, "
            + ContactContract.ContactEntry.COLUMN_CONTACT_PHONE + " TEXT);";
    public static final String SQL_DELETE_CONTACTS_TABLE = "DROP TABLE IF EXISTS " + ContactContract.ContactEntry.TABLE_NAME;

    public ContactDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CONTACTS_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_CONTACTS_TABLE);
        onCreate(db);
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
