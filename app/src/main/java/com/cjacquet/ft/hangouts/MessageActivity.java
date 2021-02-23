package com.cjacquet.ft.hangouts;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cjacquet.ft.hangouts.messages.Message;
import com.cjacquet.ft.hangouts.messages.MessageListAdapter;
import com.cjacquet.ft.hangouts.messages.MessageType;

import java.util.ArrayList;
import java.util.List;

import static com.cjacquet.ft.hangouts.data.ContactContract.ContactEntry.CONTENT_URI;

public class MessageActivity extends BasePermissionAppCompatActivity {
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private String otherNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        setTitle(this.getIntent().getExtras().get("contactName").toString());

        this.getReadSMSPermission(new BasePermissionAppCompatActivity.RequestPermissionAction() {
            @Override
            public void permissionDenied() {
                // TODO, task after permission is not granted
                // Show toast no sms permission
            }

            @Override
            public void permissionGranted() {
                // TODO, task after permission is granted
                // Show toast
            }
        });

        otherNumber = this.getIntent().getExtras().get("phoneNumber").toString();
        List<Message> messages = new ArrayList<>();
        messages.addAll(this.getAllMessages());

        mMessageRecycler = (RecyclerView) findViewById(R.id.recycler_gchat);
        mMessageAdapter = new MessageListAdapter(this, messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        mMessageRecycler.setLayoutManager(layoutManager);
        mMessageRecycler.setAdapter(mMessageAdapter);
    }

    public List<Message> getAllMessages() {
        List<Message> messagesList = new ArrayList<Message>();
        Message message;
        Uri messageUri = Uri.parse("content://sms/");
        ContentResolver cr = this.getContentResolver();

        if (this.otherNumber == null || this.otherNumber.isEmpty())
            return messagesList;

        Cursor c = cr.query(messageUri, null, null, null, null);
        this.startManagingCursor(c);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            do {
                if (c.getString(c
                        .getColumnIndexOrThrow("address")).contains(this.otherNumber)) {
                    message = new Message();
                    message.setId(c.getString(c.getColumnIndexOrThrow("_id")));
                    message.setAddress(c.getString(c
                            .getColumnIndexOrThrow("address")));
                    message.setText(c.getString(c.getColumnIndexOrThrow("body")));
                    message.setRead(Boolean.valueOf(c.getString(c.getColumnIndex("read"))));
                    message.setTime(Long.parseLong(c.getString(c.getColumnIndexOrThrow("date"))));
                    if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                        message.setType(MessageType.RECEIVED);
                    } else {
                        message.setType(MessageType.SENT);
                    }
                    messagesList.add(message);
                }
            } while (c.moveToNext());
        }
        c.close();
        return messagesList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                    // Navigate back to parent activity (EditorActivity)
                Intent intent = new Intent(MessageActivity.this, EditorActivity.class);
                Uri currentContactUri = ContentUris.withAppendedId(CONTENT_URI, Integer.valueOf(getIntent().getExtras().get("contactId").toString()));
                intent.setData(currentContactUri);
                    NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
