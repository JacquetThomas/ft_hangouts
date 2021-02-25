package com.cjacquet.ft.hangouts.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ContactContract {

    public static final String CONTENT_AUTHORITY = "com.cjacquet.ft.hangouts";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_CONTACTS = "contacts";


    private ContactContract() {}

    public static final class ContactEntry implements BaseColumns {

        public static final String TABLE_NAME = "contacts";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CONTACTS);
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of contacts.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single contact.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACTS;

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_CONTACT_NAME ="name";
        public static final String COLUMN_CONTACT_LASTNAME = "lastname";
        public static final String COLUMN_CONTACT_PHONE = "phone";
        public static final String COLUMN_CONTACT_BDAY = "birthday";
        public static final String COLUMN_CONTACT_MAIL = "mail";
        public static final String COLUMN_CONTACT_FAV = "fav";
    }

}
