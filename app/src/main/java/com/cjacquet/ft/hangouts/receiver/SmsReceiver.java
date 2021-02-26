package com.cjacquet.ft.hangouts.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
    private SharedPreferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())){
            Bundle bundle = intent.getExtras();
            SmsMessage[] messages = null;
            String messageAddress = "";
            String messageBody = "";
            if (bundle != null){
                //---retrieve the SMS message received---
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < messages.length; i++){
                        messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        messageAddress = messages[i].getOriginatingAddress();
                        messageBody += messages[i].getMessageBody();
                    }
                } catch (Exception e) {}
            }
            int i = 0;
            for (SmsMessage msg : messages) {
                System.out.println("messages array[" + i++ + "] : " + msg.getMessageBody());
            }
            System.out.println("messageAddress : " + messageAddress);

            Toast.makeText(context, "Message: " + messageAddress, Toast.LENGTH_SHORT).show();
            Intent broadcastReceiver = new Intent();
            broadcastReceiver.setAction("RECEIVED_SMS");
            broadcastReceiver.putExtra("number", messageAddress);
            broadcastReceiver.putExtra("message", messageBody);
            context.sendBroadcast(broadcastReceiver);
        }
    }
}
