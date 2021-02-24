package com.cjacquet.ft.hangouts;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cjacquet.ft.hangouts.messages.Message;
import com.cjacquet.ft.hangouts.messages.MessageListAdapter;
import com.cjacquet.ft.hangouts.messages.MessageType;

import java.util.ArrayList;
import java.util.List;

import static com.cjacquet.ft.hangouts.data.ContactContract.ContactEntry.CONTENT_URI;

public class MessageActivity extends BasePausableAppCompatActivity {
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private String otherNumber;
    private static boolean permission;
    private static List<Message> messages;
    private final static int REQUEST_READ_SMS_PERMISSION = 3004;
    private final static String APP_NAME = "ft_hangouts";
    public final static String READ_SMS_PERMISSION_NOT_GRANTED = "Please allow " + APP_NAME + " to access your SMS from setting";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        setTitle(this.getIntent().getExtras().get("contactName").toString());

        this.permission = false;

        otherNumber = this.getIntent().getExtras().get("phoneNumber").toString();
        messages = new ArrayList<>();
        if (permission)
            messages.addAll(this.getAllMessages(otherNumber));

        mMessageRecycler = (RecyclerView) findViewById(R.id.recycler_gchat);
        mMessageAdapter = new MessageListAdapter(this, messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        mMessageRecycler.setLayoutManager(layoutManager);
        mMessageRecycler.setAdapter(mMessageAdapter);

        this.getReadSMSPermission();
    }

    public static List<Message> getAllMessages(String otherNumber) {
        List<Message> messagesList = new ArrayList<Message>();
        Message message;
        Uri messageUri = Uri.parse("content://sms/");
        ContentResolver cr = CatalogActivity.getContext().getContentResolver();

        if (otherNumber == null || otherNumber.isEmpty())
            return messagesList;

        Cursor c = cr.query(messageUri, null, null, null, null);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            do {
                if (c.getString(c
                        .getColumnIndexOrThrow("address")).contains(otherNumber)) {
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


    /* --------------------------- Handle permission -------------------------------- */

    public void getReadSMSPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED)) {
                System.out.println("getReadPermission: Permission not granted yet, make a request");
                requestPermissions(new String[]{Manifest.permission.READ_SMS}, REQUEST_READ_SMS_PERMISSION);
                return;
            }
            MessageActivity.permission = true;
            messages = MessageActivity.getAllMessages(otherNumber);
            mMessageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("onRequestPermissionsResult: result of permission is bacl");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            System.out.println("onRequestPermissionsResult: permission is granted ");
            if (REQUEST_READ_SMS_PERMISSION == requestCode) {
                MessageActivity.permission = true;
                messages = MessageActivity.getAllMessages(otherNumber);
                mMessageAdapter.notifyDataSetChanged();
            }

        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            System.out.println("onRequestPermissionsResult: permission is denied");
            if (REQUEST_READ_SMS_PERMISSION == requestCode) {
                System.out.println("onRequestPermissionsResult: REQUEST_READ_SMS_PERMISSION Permission Denied");
                System.out.println("onRequestPermissionsResult: Will open settings to grant sms from there");

                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.popup_permission_title);
                builder.setMessage(R.string.popup_permission_message);

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
                    }
                });
                builder.setNegativeButton(R.string.popup_permission_ko, null);

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }
}
