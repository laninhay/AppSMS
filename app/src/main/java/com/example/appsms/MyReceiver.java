package com.example.appsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            return;
        }

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        String format = bundle.getString("format");
        StringBuilder mensagemCompleta = new StringBuilder();
        String remetente = "";

        //reconstrói a mensagem
        for (Object pdu : pdus) {
            byte[] data = (byte[]) pdu;
            SmsMessage sms = SmsMessage.createFromPdu(data, format);
            mensagemCompleta.append(sms.getMessageBody());
            if (remetente.isEmpty()) {
                remetente = sms.getOriginatingAddress();
            }
        }

        String msgBody = mensagemCompleta.toString();

        //lógica de filtro e salvamento
        SharedPreferences prefs = context.getSharedPreferences("AppSMSPrefs", Context.MODE_PRIVATE);
        String filtro = prefs.getString("filter_word", "");

        //verifica se o filtro não é vazio e se a mensagem contém a palavra (ignorando maiúsculas/minúsculas)
        if (!filtro.isEmpty() && msgBody.toLowerCase().contains(filtro.toLowerCase())) {
            salvarMensagem(context, prefs, remetente, msgBody);
            Toast.makeText(context, "SMS Capturado e Salvo!", Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarMensagem(Context context, SharedPreferences prefs, String sender, String body) {
        try {
            //recupera a lista existente
            String jsonString = prefs.getString("saved_messages", "[]");
            JSONArray jsonArray = new JSONArray(jsonString);

            //cria o novo objeto de mensagem
            JSONObject msgObj = new JSONObject();
            msgObj.put("sender", sender);
            msgObj.put("body", body);

            //adiciona e salva
            jsonArray.put(msgObj);
            prefs.edit().putString("saved_messages", jsonArray.toString()).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}