package com.cjacquet.ft.hangouts.messages;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cjacquet.ft.hangouts.BaseAppCompatActivity;
import com.cjacquet.ft.hangouts.R;
import com.cjacquet.ft.hangouts.contacts.EditorActivity;
import com.cjacquet.ft.hangouts.receiver.SmsDeliveredReceiver;
import com.cjacquet.ft.hangouts.receiver.SmsSentReceiver;
import com.cjacquet.ft.hangouts.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry.CONTENT_URI;

public class MessageActivity extends BaseAppCompatActivity {
    private RecyclerView mMessageRecycler;
    private static MessageListAdapter mMessageAdapter;
    private String otherNumber;
    private static List<Message> messages;
    private static EditText messageToSend;
    IntentFilter intentFilter;

    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Utils.getSMSPermission(getApplicationContext()) && intent.getExtras().get("number").toString().equals(otherNumber)) {
                Message newMessage = new Message(intent.getExtras().get("message").toString(), Utils.formatNumber(intent.getExtras().get("number").toString()), MessageType.RECEIVED);
                mMessageAdapter.updateData(messages, newMessage);
                mMessageAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.getSMSPermission(getApplicationContext())) {
            onBackPressed();
            String text = getResources().getString(R.string.no_sms_permission);
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
            toast.show();
            return ;
        }
        setContentView(R.layout.activity_message_list);
        setTitle(this.getIntent().getExtras().get("contactName").toString());
        otherNumber = this.getIntent().getExtras().get("phoneNumber").toString();
        messages = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((Button)findViewById(R.id.button_gchat_send)).setTextColor(getResources().getColor(colorTheme.getPrimaryColorId(), super.getTheme()));
        }

        /* --------------- Register the receiver --------------- */
        intentFilter = new IntentFilter();
        intentFilter.addAction("RECEIVED_SMS");
        registerReceiver(intentReceiver, intentFilter);

        /* --------------- Handle message to send --------------- */
        messageToSend = findViewById(R.id.edit_gchat_message);
        Button sendButton = findViewById(R.id.button_gchat_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messageToSend.getText().toString().isEmpty())
                    return ;
                Message newMessage = new Message(messageToSend.getText().toString(), Utils.formatNumber(otherNumber), MessageType.SENT);
                messageToSend.setText("");
                // Close keyboard
                getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                );
                messages.add(newMessage);
                mMessageAdapter.updateData(messages, newMessage);
                mMessageAdapter.notifyDataSetChanged();
                SmsManager smsManager = SmsManager.getDefault();
                ArrayList<String> dividedMessages = smsManager.divideMessage(newMessage.getText());
                ArrayList<PendingIntent> sentPendingIntents = new ArrayList<>();
                ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<>();
                PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0,
                        new Intent(getApplicationContext(), SmsSentReceiver.class), 0);

                PendingIntent deliveredPI = PendingIntent.getBroadcast(getApplicationContext(), 0,
                        new Intent(getApplicationContext(), SmsDeliveredReceiver.class), 0);
                for (int i = 0; i < dividedMessages.size(); i++) {
                    sentPendingIntents.add(i, sentPI);

                    deliveredPendingIntents.add(i, deliveredPI);
                }
                smsManager.sendMultipartTextMessage(newMessage.getAddress(), null, dividedMessages, sentPendingIntents, deliveredPendingIntents);
            }
        });

        mMessageRecycler = findViewById(R.id.recycler_gchat);
        mMessageAdapter = new MessageListAdapter(this, messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        mMessageRecycler.setLayoutManager(layoutManager);
        mMessageRecycler.setAdapter(mMessageAdapter);
    }

    public List<Message> getAllMessages(String otherNumber) {
        List<Message> messagesList = new ArrayList<>();
        Message message;
        Uri messageUri = Uri.parse("content://sms/");
        ContentResolver cr = this.getApplicationContext().getContentResolver();

        if (otherNumber == null || otherNumber.isEmpty())
            return messagesList;

        Cursor c = cr.query(messageUri, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                if (c.getString(c
                        .getColumnIndexOrThrow("address")).contains(otherNumber)) {
                    message = new Message();
                    message.setId(c.getString(c.getColumnIndexOrThrow("_id")));
                    message.setAddress(c.getString(c
                            .getColumnIndexOrThrow("address")));
                    message.setText(c.getString(c.getColumnIndexOrThrow("body")));
                    message.setRead(Boolean.parseBoolean(c.getString(c.getColumnIndex("read"))));
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
                Uri currentContactUri = ContentUris.withAppendedId(CONTENT_URI, Integer.parseInt(getIntent().getExtras().get("contactId").toString()));
                intent.setData(currentContactUri);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Bundle bundle = getIntent().getExtras();
        Intent intent = new Intent(MessageActivity.this, EditorActivity.class);
        Uri currentContactUri = ContentUris.withAppendedId(CONTENT_URI, Integer.parseInt(bundle.get("contactId").toString()));
        intent.setData(currentContactUri);
        NavUtils.navigateUpTo(this, intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(intentReceiver);
        } catch (Exception e) {
            Log.e(this.getPackageName(), e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        intentFilter = new IntentFilter();
        intentFilter.addAction("RECEIVED_SMS");
        registerReceiver(intentReceiver, intentFilter);
        messages.clear();
        messages.addAll(this.getAllMessages(otherNumber));
        mMessageAdapter.updateData(messages, null);
        mMessageAdapter.notifyDataSetChanged();
    }
}
