package com.example.appsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!"android.provider.Telephony.SMS RECEIVED".equals(intent.getAction())){
            return;
        }
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }
        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null){
            return;
        }
        String format = bundle.getString("format");
        StringBuilder mensagem = new StringBuilder();

        for (Object pdu : pdus){
            byte[] data = (byte[]) pdu;
            String msg = SmsMessage.createFromPdu(data, format).getMessageBody();
            mensagem.append(msg);
        }
        Toast.makeText(context, mensagem.toString(), Toast.LENGTH_SHORT).show();
    }
}