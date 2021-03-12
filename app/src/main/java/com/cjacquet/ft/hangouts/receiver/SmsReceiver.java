package com.cjacquet.ft.hangouts.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.cjacquet.ft.hangouts.contacts.ContactSummary;
import com.cjacquet.ft.hangouts.utils.CustomIntent;
import com.cjacquet.ft.hangouts.utils.Utils;

import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry.COLUMN_CONTACT_LASTNAME;
import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry.COLUMN_CONTACT_NAME;
import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry.COLUMN_CONTACT_PHONE;
import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry.CONTENT_URI;
import static com.cjacquet.ft.hangouts.database.ContactContract.ContactEntry._ID;
import static com.cjacquet.ft.hangouts.utils.SharedPreferencesConstant.SP_UNKNOWN_CONTACT;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean unknownContactString = pref.getBoolean(SP_UNKNOWN_CONTACT, false);

        if(Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())){
            Bundle bundle = intent.getExtras();
            SmsMessage[] messages = null;
            String messageAddress = "";
            String messageBody = "";
            if (bundle != null){
                try{
                    messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                    StringBuilder messageBodyBuilder = new StringBuilder();
                    for (int i = 0; i < messages.length; i++) {
                        messageAddress = messages[i].getOriginatingAddress();
                        messageBodyBuilder.append(messages[i].getMessageBody());
                    }
                    messageBody = messageBodyBuilder.toString();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            String[] projection = {_ID, COLUMN_CONTACT_NAME, COLUMN_CONTACT_LASTNAME, COLUMN_CONTACT_PHONE};
            Cursor c = context.getContentResolver().query(CONTENT_URI, projection, null, null,  null);

            ContactSummary contact;
            if ((contact = this.contactExists(c, messageAddress)) != null) {
                String name = contact.getName();
                String lastname = contact.getLastname();
                Toast.makeText(context, "Message: " + name + " " + lastname, Toast.LENGTH_SHORT).show();
                Intent broadcastReceiver = new Intent();
                broadcastReceiver.setAction(CustomIntent.RECEIVED_SMS);
                broadcastReceiver.putExtra("number", messageAddress);
                broadcastReceiver.putExtra("message", messageBody);
                context.sendBroadcast(broadcastReceiver);
            } else if (unknownContactString) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_CONTACT_NAME, messageAddress);
                contentValues.put(COLUMN_CONTACT_PHONE, messageAddress);
                Uri res = context.getContentResolver().insert(CONTENT_URI, contentValues);

                Toast.makeText(context, "Message: " + messageAddress, Toast.LENGTH_SHORT).show();

                Intent broadcastReceiver = new Intent();
                broadcastReceiver.setAction(CustomIntent.UNKNOWN_SMS_RECEIVED);
                broadcastReceiver.putExtra("uri", res);
                context.sendBroadcast(broadcastReceiver);
            }
        }
    }
    
    private ContactSummary contactExists(Cursor c, String number) {
        String searchedNumber = Utils.formatNumberSearch(number);
        if (c.moveToFirst()) {
            while(!c.isAfterLast()) {
                int id = c.getInt(c.getColumnIndex(_ID));
                String name = c.getString(c.getColumnIndex(COLUMN_CONTACT_NAME));
                String lastname = c.getString(c.getColumnIndex(COLUMN_CONTACT_LASTNAME));
                String phoneNumber = c.getString(c.getColumnIndex(COLUMN_CONTACT_PHONE));

                if (Utils.formatNumberSearch(phoneNumber).equals(searchedNumber)) {
                    return new ContactSummary(null, id, name, lastname, phoneNumber, false, null);
                }
                c.moveToNext();
            }
        }
        return null;
    }
}
