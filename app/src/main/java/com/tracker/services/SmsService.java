package com.tracker.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by shubhamlamba on 2/20/18.
 */

public class SmsService extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        // Get Bundle object contained in the SMS intent passed in
        Bundle bundle = intent.getExtras();
        SmsMessage[] messages = null;
        String msgBody = "";
        String senderName = "";
        String msgTime = "";

        if (bundle != null) {
            try {
                // Get the SMS message
                Object[] pdus = (Object[]) bundle.get("pdus");
                messages = new SmsMessage[pdus.length];

                for (int i = 0; i < messages.length; i++) {

                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    senderName = messages[i].getOriginatingAddress();
                    msgBody += messages[i].getMessageBody();
                    msgTime = String.valueOf(messages[i].getTimestampMillis());

//                LocalBroadcastManager.getInstance(context).sendBroadcast(smsIntent);
                }

                if (!TextUtils.isEmpty(msgBody) && senderName != null && senderName.length() <= 9) {

                    String otp = msgBody.replaceAll("[ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz]", "");
                    EventBus.getDefault().post(otp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
