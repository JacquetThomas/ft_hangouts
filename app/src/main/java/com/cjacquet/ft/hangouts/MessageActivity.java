package com.cjacquet.ft.hangouts;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.cjacquet.ft.hangouts.messages.Message;
import com.cjacquet.ft.hangouts.messages.MessageListAdapter;
import com.cjacquet.ft.hangouts.messages.MessageType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);
    }

    public List<Message> getAllMessages() {
        List<Message> messagesList = new ArrayList<Message>();
        Message message;
        Uri messageUri = Uri.parse("content://sms/");
        ContentResolver cr = this.getContentResolver();

        Cursor c = cr.query(messageUri, null, null, null, null);
        this.startManagingCursor(c);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
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
                    c.moveToNext();
                }
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }
        c.close();
        Collections.reverse(messagesList);
        return messagesList;
    }
}