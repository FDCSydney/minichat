package com.chat.minichat.service;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.chat.minichat.R;
import com.chat.minichat.managers.RTCAudioManager;
import com.chat.minichat.models.Chat;
import com.chat.minichat.repository.MainRepository;

import org.webrtc.SurfaceViewRenderer;

public class MainService extends Service implements MainRepository.Listener {
    private static final String TAG = "MainService";
    private Boolean isServiceRunning = false;
    private String username = null;
    private NotificationManager mNotificationManager;
    private MainRepository mMainRepository;
    private RTCAudioManager mRtcAudioManager;
    private static CallReceivedListener mCallReceivedListener;
    private static CallEndedListener mCallEndedListener;
    public static SurfaceViewRenderer mLocalView;
    public static SurfaceViewRenderer mRemoteView;

    public MainService() {
        mMainRepository = MainRepository.getInstance(this);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = getSystemService(NotificationManager.class);
        mRtcAudioManager = RTCAudioManager.create(this);
        mRtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            switch (intent.getAction()) {
                case "START_SERVICE":
                    handleStartService(intent);
                    break;
                case "SETUP_VIEWS":
                    handleSetupViews(intent);
                    break;
                case "END_CALL":
                    handleEndCall(intent);
                    break;
                case "SWITCH_CAMERA":
                    handleSwitchCamera();
                    break;
                case "TOGGLE_AUDIO":
                    handleToggleAudio(intent);
                    break;
                case "TOGGLE_CAMERA":
                    handleToggleCamera(intent);
                    break;
                case "TOGGLE_AUDIO_DEVICE":
                    handleToggleAudioDevice(intent);
                    break;
                case "STOP_SERVICE":
                    handleStopService();
                    break;
                default:
                    break;
            }
        }
        return START_STICKY;
    }

    private void handleStopService() {
        mMainRepository.endCall();
        mMainRepository.logOff(() -> {
            isServiceRunning = false;
            stopSelf();
        });
    }

    private void handleToggleAudioDevice(Intent intent) {
        String type = intent.getStringExtra("type");
        RTCAudioManager.AudioDevice device;
        if (type.equals(RTCAudioManager.AudioDevice.EARPIECE.name())) {
            device = RTCAudioManager.AudioDevice.EARPIECE;
        } else if (type.equals(RTCAudioManager.AudioDevice.SPEAKER_PHONE.name())) {
            device = RTCAudioManager.AudioDevice.SPEAKER_PHONE;
        } else {
            device = null;
        }
        if (device == null) return;
        mRtcAudioManager.setDefaultAudioDevice(device);
        mRtcAudioManager.selectAudioDevice(device);
        Log.d(TAG, "handleToggleAudioDevice: " + device);
    }

    private void handleToggleCamera(Intent intent) {
        boolean isCameraMuted = intent.getBooleanExtra("shouldBeMuted", false);
        mMainRepository.toggleVideo(isCameraMuted);
    }

    private void handleToggleAudio(Intent intent) {
        boolean isAudioMuted = intent.getBooleanExtra("shouldBeMuted", false);
        mMainRepository.toggleAudio(isAudioMuted);
    }

    private void handleSwitchCamera() {
        mMainRepository.switchCamera();
    }

    private void handleEndCall(Intent intent) {
        // send signal to peer to end call
        mMainRepository.sendEndCall();
        // restartWebRtcRepo
        endCallAndRestartRepo();

    }

    private void endCallAndRestartRepo() {
        mMainRepository.endCall();
        mCallEndedListener.onCallEnded();
        mMainRepository.initWebRTCClient(username);
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
        mMainRepository.initWebRTCClient(username);
        mMainRepository.setUser(username);

    }

    private void handleSetupViews(Intent intent) {
        String target = intent.getStringExtra("target");
        boolean isCaller = intent.getBooleanExtra("isCaller", false);
        Boolean isVideoCall = intent.getBooleanExtra("isVideoCall", true);
        mMainRepository.setUser(target, username);
        // initialize widget
        mMainRepository.initLocalSurfaceView(mLocalView, isVideoCall);
        mMainRepository.initRemoteSurfaceView(mRemoteView);
        // prepare for call
        if (!isCaller) {
            mMainRepository.startCall();
        }


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
                if (mCallReceivedListener != null)
                    mCallReceivedListener.onCallReceived(chat);
                break;
            case "Declined":
                if (mCallReceivedListener != null)
                    mCallReceivedListener.onCallDeclined(chat);
                break;
            default:
                break;
        }
    }

    @Override
    public void onEndCall() {
        endCallAndRestartRepo();
    }

    public interface CallReceivedListener {
        void onCallReceived(Chat chat);

        void onCallDeclined(Chat chat);
    }

    public interface CallEndedListener {
        void onCallEnded();
    }

    public static void setCallReceivedListener(CallReceivedListener listener) {
        mCallReceivedListener = listener;
    }

    public static void setCallEndedListener(CallEndedListener listener) {
        mCallEndedListener = listener;
    }

}
