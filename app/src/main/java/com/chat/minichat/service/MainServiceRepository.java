package com.chat.minichat.service;

import android.content.Context;
import android.content.Intent;

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

    public void setUpViews(String target, Boolean isVideoCall, Boolean isCaller) {
        Intent intent = new Intent(mContext, MainService.class);
        intent.setAction(MainServiceAction.SETUP_VIEWS.getName());
        intent.putExtra("isVideoCall", isVideoCall);
        intent.putExtra("target", target);
        intent.putExtra("isCaller", isCaller);
        startServiceIntent(intent);
    }
}
