package com.cjacquet.ft.hangouts.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.cjacquet.ft.hangouts.R;

public class SmsSentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int resultCode = getResultCode();
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, context.getResources().getString(R.string.sms_send), Toast.LENGTH_SHORT).show();
        } else if (resultCode == SmsManager.RESULT_ERROR_GENERIC_FAILURE) {
            Toast.makeText(context, context.getResources().getString(R.string.sms_send_generic_failure), Toast.LENGTH_SHORT).show();
        } else if (resultCode == SmsManager.RESULT_ERROR_NO_SERVICE) {
            Toast.makeText(context, context.getResources().getString(R.string.sms_send_no_service), Toast.LENGTH_SHORT).show();
        }
    }
}