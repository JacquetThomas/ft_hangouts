package com.cjacquet.ft.hangouts.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

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
            // query content resolver to know if the number is known if yes make a toast with the name
            // else insert with the content resolver a new contact with messageAddress as name and phone number
            // context.getContentResolver().query();

            Toast.makeText(context, "Message: " + messageAddress, Toast.LENGTH_SHORT).show();
            Intent broadcastReceiver = new Intent();
            broadcastReceiver.setAction("RECEIVED_SMS");
            broadcastReceiver.putExtra("number", messageAddress);
            broadcastReceiver.putExtra("message", messageBody);
            context.sendBroadcast(broadcastReceiver);

        }
    }
}
