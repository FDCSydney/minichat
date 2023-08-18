package com.chat.minichat.service;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.chat.minichat.R;
import com.chat.minichat.models.Chat;
import com.chat.minichat.repository.MainRepository;

public class MainService extends Service implements MainRepository.Listener {
    private static final String TAG = "MainService";
    private Boolean isServiceRunning = false;
    private String username = null;
    private NotificationManager mNotificationManager;
    private final MainRepository mMainRepository = new MainRepository();

    private Listener mListener;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = getSystemService(NotificationManager.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            switch (intent.getAction()) {
                case "START_SERVICE":
                    handleStartService(intent);
                    break;
                default:
                    break;
            }
        }
        return START_STICKY;
    }

    /**
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleStartService(Intent intent) {
        // handle start foreground service
        if (isServiceRunning) return;
        isServiceRunning = true;
        username = intent.getStringExtra("username");
        startServiceNotification();

        // set up clients
        mMainRepository.setListener(this);
        mMainRepository.initFirebase(username);

    }

    private void startServiceNotification() {
        NotificationChannel notificationChannel = new NotificationChannel("channel1", "foreground", NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(notificationChannel);
        startForeground(1, new NotificationCompat.Builder(this,
                "channel1").setSmallIcon(R.mipmap.ic_launcher).build());
    }

    /**
     * @param chat
     */
    @Override
    public void onLatestEventReceived(Chat chat) {
        if (!chat.isValid()) return;
        switch (chat.getType().toString()) {
            case "StartAudioCall":
            case "StartVideoCall":
                mListener.onCallReceived(chat);
                break;
            default:
                break;
        }
    }

    public interface Listener {
        void onCallReceived(Chat chat);
    }

    public void onSetListener(Listener listener) {
        mListener = listener;
    }

}
