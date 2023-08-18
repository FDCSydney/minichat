package com.chat.minichat.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class MainServiceRepository {
    private final Context mContext;

    public MainServiceRepository(Context context) {
        mContext = context;
    }

    public void startService(String username) {
        Intent intent = new Intent(mContext, MainService.class);
        intent.putExtra("username", username);
        intent.setAction(MainServiceAction.START_SERVICE.getName());
        startServiceIntent(intent);
    }

    public void startServiceIntent(Intent intent) {
        mContext.startForegroundService(intent);
    }
}
