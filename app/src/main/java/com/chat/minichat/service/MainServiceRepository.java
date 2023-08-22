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

    public void sendEndCall() {
        Intent intent = new Intent(mContext, MainService.class);
        intent.setAction(MainServiceAction.END_CALL.name());
        startServiceIntent(intent);
    }

    public void switchCamera() {
        Intent intent = new Intent(mContext, MainService.class);
        intent.setAction(MainServiceAction.SWITCH_CAMERA.name());
        startServiceIntent(intent);
    }

    public void toggleAudio(boolean isAudioMuted) {
        Intent intent = new Intent(mContext, MainService.class);
        intent.setAction(MainServiceAction.TOGGLE_AUDIO.name());
        intent.putExtra("shouldBeMuted", isAudioMuted);
        startServiceIntent(intent);
    }

    public void toggleVideo(boolean isCameraMuted) {
        Intent intent = new Intent(mContext, MainService.class);
        intent.setAction(MainServiceAction.TOGGLE_CAMERA.name());
        intent.putExtra("shouldBeMuted", isCameraMuted);
        startServiceIntent(intent);
    }

    public void toggleAudioDevice(String type) {
        Intent intent = new Intent(mContext, MainService.class);
        intent.setAction(MainServiceAction.TOGGLE_AUDIO_DEVICE.name());
        intent.putExtra("type", type);
        startServiceIntent(intent);
    }

    public void stopService() {
        Intent intent = new Intent(mContext, MainService.class);
        intent.setAction(MainServiceAction.STOP_SERVICE.name());
        startServiceIntent(intent);
    }
}
