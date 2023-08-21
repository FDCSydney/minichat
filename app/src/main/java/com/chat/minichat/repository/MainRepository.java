package com.chat.minichat.repository;

import android.content.Context;

import com.chat.minichat.enums.ChatType;
import com.chat.minichat.enums.UserStatus;
import com.chat.minichat.firebaseClient.FirebaseClient;
import com.chat.minichat.interfaces.Callback;
import com.chat.minichat.managers.GsonManager;
import com.chat.minichat.models.Chat;
import com.chat.minichat.webrtc.MyPeerObserver;
import com.chat.minichat.webrtc.WebRTCClient;

import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

public class MainRepository implements WebRTCClient.SocketTransferListener {
    private static MainRepository mInstance;
    private Listener mListener;
    private final FirebaseClient mFirebaseClient;

    private String mTarget;
    private String mCurrentUser;
    private WebRTCClient mWebRTCClient;
    private GsonManager mGsonManager;

    private SurfaceViewRenderer mRemoteView;

    private MainRepository(Context context) {
        this.mFirebaseClient = new FirebaseClient();
        mWebRTCClient = WebRTCClient.getInstance(context);
        mGsonManager = GsonManager.getInstance();
    }

    public static synchronized MainRepository getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MainRepository(context);
        }
        return mInstance;
    }

    public void login(String username, String password, Callback.LoginCallback callback) {
        this.mFirebaseClient.login(username, password, callback);
    }

    public void observeUsersStatus(Callback.UserCallBack callback) {
        this.mFirebaseClient.observeUsersStatus(callback);
    }

    public void initFirebase(String username) {
        mFirebaseClient.subscribeForLatestEvent(username, (chat) -> {
            mListener.onLatestEventReceived(chat);
            switch (chat.getType().toString()) {
                case "Offer":
                    mWebRTCClient.onRemoteSessionReceived(new SessionDescription(SessionDescription.Type.OFFER,
                            chat.getData()));
                    mWebRTCClient.answer(mTarget);
                    break;
                case "Answer":
                    mWebRTCClient.onRemoteSessionReceived(new SessionDescription(SessionDescription.Type.ANSWER,
                            chat.getData()));
                    mWebRTCClient.answer(mTarget);
                    break;
                case "IceCandidate":
                    IceCandidate iceCandidate = null;

                    try{
                        iceCandidate = mGsonManager.getGson().fromJson(chat.getData(), IceCandidate.class);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(iceCandidate == null) return;
                    mWebRTCClient.addIceCandidateToPeer(iceCandidate);
                    break;
                case "EndCall":
                    mListener.onEndCall();
                    break;
                default:
                    break;
            }
        });
    }

    public void sendConnectionRequest(
            String sender,
            String target,
            Boolean isVideoCall,
            Callback.SendMessageCallback callback) {
        ChatType type = isVideoCall ? ChatType.StartVideoCall : ChatType.StartAudioCall;
        mFirebaseClient.sendMessageToOtherClient(new Chat(sender, type, target), callback);
    }

    public void setUser(String target, String current) {
        this.mTarget = target;
        this.mCurrentUser = current;
    }


    public void initWebRTCClient(String username) {
        mWebRTCClient.setSocketTransferListener(this);
        mWebRTCClient.initializeWebRTCClient(username, new MyPeerObserver() {
            @Override
            public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {
                super.onAddTrack(receiver, mediaStreams);
                try {
                    VideoTrack remoteVideoTrack = mediaStreams[0].videoTracks.get(0);
                    remoteVideoTrack.addSink(mRemoteView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onIceCandidate(IceCandidate var1) {
                super.onIceCandidate(var1);
                if (var1 != null) {
                    mWebRTCClient.sendIceCandidate(mTarget, var1);
                }

            }

            @Override
            public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                super.onConnectionChange(newState);
                if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                    // change status to in call
                    updateStatus(username, UserStatus.IN_CALL);
                    // clear latest events in db
                    clearLatestEvent(username);
                }
            }
        });
    }

    public void initLocalSurfaceView(SurfaceViewRenderer view, Boolean isVideoCall) {
        mWebRTCClient.initLocalSurfaceView(view, isVideoCall);
    }

    public void initRemoteSurfaceView(SurfaceViewRenderer view) {
        mWebRTCClient.initRemoteSurfaceView(view);
        mRemoteView = view;
    }

    public void startCall() {
        mWebRTCClient.call(mTarget);
    }

    public void endCall() {
        mWebRTCClient.closeConnection();
        updateStatus(mCurrentUser, UserStatus.ONLINE);
    }

    public void sendEndCall() {
        onTransferEventToSocket(new Chat(mTarget, ChatType.EndCall));
    }

    public void toggleAudio(Boolean shouldBeMuted) {
        mWebRTCClient.toggleAudio(shouldBeMuted);
    }

    public void toggleVideo(Boolean shouldBeMuted) {
        mWebRTCClient.toggleVideo(shouldBeMuted);
    }

    public void switchCamera() {
        mWebRTCClient.switchCamera();
    }

    private void clearLatestEvent(String username) {
        mFirebaseClient.removeLatestEvent(username);
    }

    private void updateStatus(String username, UserStatus status) {
        mFirebaseClient.updateMyStatus(username, status);
    }


    /**
     * @param chat
     */
    @Override
    public void onTransferEventToSocket(Chat chat) {
        mFirebaseClient.sendMessageToOtherClient(chat, status -> {
        });
    }

    public interface Listener {
        void onLatestEventReceived(Chat chat);
        void onEndCall();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }
}
