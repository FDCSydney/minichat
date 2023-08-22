package com.chat.minichat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.chat.minichat.ui.CloseActivity;

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
            context.startActivity(new Intent(context, CloseActivity.class));
        }
    }
}
