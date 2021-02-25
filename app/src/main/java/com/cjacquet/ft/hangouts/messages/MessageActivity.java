package com.cjacquet.ft.hangouts.messages;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cjacquet.ft.hangouts.BaseAppCompatActivity;
import com.cjacquet.ft.hangouts.MainActivity;
import com.cjacquet.ft.hangouts.R;
import com.cjacquet.ft.hangouts.utils.Utils;
import com.cjacquet.ft.hangouts.contacts.EditorActivity;
import com.cjacquet.ft.hangouts.receiver.SmsDeliveredReceiver;
import com.cjacquet.ft.hangouts.receiver.SmsSentReceiver;

import java.util.ArrayList;
import java.util.List;

import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry.CONTENT_URI;

public class MessageActivity extends BaseAppCompatActivity {
    private RecyclerView mMessageRecycler;
    private static MessageListAdapter mMessageAdapter;
    private String otherNumber;
    private static boolean permission;
    private static List<Message> messages;
    private static EditText messageToSend;
    private static final int REQUEST_READ_SMS_PERMISSION = 3004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        setTitle(this.getIntent().getExtras().get("contactName").toString());

        this.permission = false;

        otherNumber = this.getIntent().getExtras().get("phoneNumber").toString();
        messages = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((Button)findViewById(R.id.button_gchat_send)).setTextColor(getResources().getColor(colorTheme.getPrimaryColorId(), MainActivity.getInstance().getTheme()));
        }

        messageToSend = findViewById(R.id.edit_gchat_message);

        Button sendButton = findViewById(R.id.button_gchat_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messageToSend.getText().toString().isEmpty())
                    return ;
                Message newMessage = new Message(messageToSend.getText().toString(), Utils.formatNumber(otherNumber));
                mMessageAdapter.updateData(messages, newMessage);
                mMessageAdapter.notifyDataSetChanged();
                messageToSend.setText("");
                SmsManager smsManager = SmsManager.getDefault();
                ArrayList<String> dividedMessages = smsManager.divideMessage(newMessage.getText());
                ArrayList<PendingIntent> sentPendingIntents = new ArrayList<>();
                ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<>();
                PendingIntent sentPI = PendingIntent.getBroadcast(MainActivity.getContext(), 0,
                        new Intent(MainActivity.getContext(), SmsSentReceiver.class), 0);

                PendingIntent deliveredPI = PendingIntent.getBroadcast(MainActivity.getContext(), 0,
                        new Intent(MainActivity.getContext(), SmsDeliveredReceiver.class), 0);
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

        permission = this.getReadSMSPermission();
        if (permission)
            messages.addAll(this.getAllMessages(otherNumber));
    }

    public static List<Message> getAllMessages(String otherNumber) {
        List<Message> messagesList = new ArrayList<>();
        Message message;
        Uri messageUri = Uri.parse("content://sms/");
        ContentResolver cr = MainActivity.getContext().getContentResolver();

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

    /* --------------------------- Handle permission -------------------------------- */

    /**
     * Check if we have read permission on SMS and request if not.
     */
    public boolean getReadSMSPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_SMS}, REQUEST_READ_SMS_PERMISSION);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (REQUEST_READ_SMS_PERMISSION == requestCode) {
                MessageActivity.permission = true;
                messages = MessageActivity.getAllMessages(otherNumber);
                mMessageAdapter.updateData(messages, null);
                mMessageAdapter.notifyDataSetChanged();
            }

        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            if (REQUEST_READ_SMS_PERMISSION == requestCode) {
                String text = getResources().getString(R.string.no_sms_permission);
                Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
                toast.show();
                onBackPressed();
            }
        }
    }
}
