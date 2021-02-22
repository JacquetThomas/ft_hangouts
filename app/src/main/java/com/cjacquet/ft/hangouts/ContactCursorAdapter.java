package com.cjacquet.ft.hangouts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.cjacquet.ft.hangouts.data.ContactContract;

/**
 * {@link ContactCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of contact data as its data source. This adapter knows
 * how to create list items for each row of contact data in the {@link Cursor}.
 */
public class ContactCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ContactCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ContactCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the contact data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current contact can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvFullName = (TextView) view.findViewById(R.id.name);
        TextView tvPhone = (TextView) view.findViewById(R.id.summary);
        Button smsButton = view.findViewById(R.id.button_sms);
        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_CONTACT_NAME));
        String lastname = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_CONTACT_LASTNAME));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_CONTACT_PHONE));
        // Populate fields with extracted properties
        tvFullName.setText(name + " " + lastname);
        tvPhone.setText(String.valueOf(phone));
        smsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MessageActivity.class);
                View parentView = v.getRootView();
                intent.putExtra("phoneNumber", ((TextView)parentView.findViewById(R.id.summary)).getText().toString());
                intent.putExtra("contactName", ((TextView)parentView.findViewById(R.id.name)).getText().toString());
                v.getContext().startActivity(intent);
            }
        });
    }
}