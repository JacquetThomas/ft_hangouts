package com.cjacquet.ft.hangouts.contacts;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.cjacquet.ft.hangouts.R;
import com.cjacquet.ft.hangouts.database.ContactContract;

public class ContactCursorAdapter extends CursorAdapter {

    public ContactCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvFullName = view.findViewById(R.id.name);
        TextView tvPhone = view.findViewById(R.id.summary);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_CONTACT_NAME));
        String lastname = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_CONTACT_LASTNAME));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_CONTACT_PHONE));

        tvFullName.setText(name + " " + lastname);
        tvPhone.setText(String.valueOf(phone));
    }
}