package com.chat.minichat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.chat.minichat.ui.CloseActivity;

import org.jitsi.meet.sdk.BroadcastEvent;

public class MainServiceReceiver extends BroadcastReceiver {
    /**
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        MainServiceRepository mainServiceRepository =
                new MainServiceRepository(context);

        if (intent.getAction().equals("ACTION_EXIT")) {
            mainServiceRepository.stopService();
            Intent closeActivityIntent = new Intent(context, CloseActivity.class);
            closeActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(closeActivityIntent);
        }
        if(intent.getAction().equals(BroadcastEvent.Type.PARTICIPANT_LEFT.getAction())){
            Toast.makeText(context, "SOMEONE LEFT", Toast.LENGTH_SHORT).show();
        }
    }
}
