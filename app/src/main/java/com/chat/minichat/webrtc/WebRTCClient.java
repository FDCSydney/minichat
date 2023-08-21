package com.chat.minichat.webrtc;

import android.content.Context;

import com.chat.minichat.enums.ChatType;
import com.chat.minichat.interfaces.Callback;
import com.chat.minichat.managers.GsonManager;
import com.chat.minichat.models.Chat;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.Collections;
import java.util.List;

public class WebRTCClient {
    private static WebRTCClient mInstance;
    private final Context mContext;
    private final EglBase.Context eglBaseCtx;

    private final PeerConnectionFactory mPeerConnectionFactory;
    private SocketTransferListener mListener;
    private final GsonManager mGsonManager;
    private String mUsername;
    private PeerConnection mPeerConnection;
    private final List<PeerConnection.IceServer> mIceServers;

    private SurfaceViewRenderer mLocalSurfaceView;
    private SurfaceViewRenderer mRemoteSurfaceView;
    private MediaStream mLocalStream;
    private AudioTrack mLocalAudioTrack;
    private VideoTrack mLocalVideoTrack;
    private String localTrackId = "";
    private String localStreamId = "";
    private final AudioSource audioSrc;
    private final VideoSource videoSource;
    private SurfaceTextureHelper mSurfaceTextureHelper;

    private final CameraVideoCapturer mVideoCapturer;
    private final MediaConstraints mMediaConstraints;

    private WebRTCClient(Context context) {
        mContext = context;
        mGsonManager = GsonManager.getInstance();
        eglBaseCtx = EglBase.create().getEglBaseContext();
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(mContext)
                        .createInitializationOptions());
        mPeerConnectionFactory = createPeerConnectionFactory();
        mIceServers = Collections.singletonList(PeerConnection.IceServer.builder("turn:a.relay.metered.ca:443?transport=tcp")
                .setUsername("83eebabf8b4cce9d5dbcb649")
                .setPassword("2D7JvfkOQtBdYW3R").createIceServer());
        videoSource = mPeerConnectionFactory.createVideoSource(false);
        audioSrc = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
        mVideoCapturer = getVideoCapturer(context);
        mMediaConstraints = new MediaConstraints();
        mMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        init();
    }

    public static synchronized WebRTCClient getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new WebRTCClient(context);
        }
        return mInstance;
    }

    // installation of requirements
    private void init() {
        initPeerConnectionFactory();
    }

    private void initPeerConnectionFactory() {
        PeerConnectionFactory.InitializationOptions options = PeerConnectionFactory.InitializationOptions.builder(mContext)
                .setEnableInternalTracer(true).setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .createInitializationOptions();
        PeerConnectionFactory.initialize(options);
    }

    public void initializeWebRTCClient(String username, PeerConnection.Observer observer) {
        this.mUsername = username;
        this.mPeerConnection = createPeerConnection(observer);
        this.localStreamId = username + "_stream";
        this.localTrackId = username + "_tract";
    }

    private PeerConnection createPeerConnection(PeerConnection.Observer observer) {
        return mPeerConnectionFactory.createPeerConnection(mIceServers, observer);
    }

    private PeerConnectionFactory createPeerConnectionFactory() {
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.disableNetworkMonitor = false;
        options.disableEncryption = false;

        return PeerConnectionFactory.builder()
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBaseCtx))
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBaseCtx, true, true))
                .setOptions(options)
                .createPeerConnectionFactory();
    }

    // negotiate to peer
    public void call(String target) {
        mPeerConnection.createOffer(new MySdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                super.onCreateSuccess(sdp);
                mPeerConnection.setLocalDescription(new MySdpObserver() {
                    @Override
                    public void onSetSuccess() {
                        super.onSetSuccess();
                        mListener.onTransferEventToSocket(new Chat(mUsername, target, sdp.description, ChatType.Offer));
                    }
                }, sdp);
            }
        }, mMediaConstraints);
    }

    public void answer(String target) {
        mPeerConnection.createAnswer(new MySdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                super.onCreateSuccess(sdp);
                mPeerConnection.setLocalDescription(new MySdpObserver() {
                    @Override
                    public void onSetSuccess() {
                        super.onSetSuccess();
                        mListener.onTransferEventToSocket(new Chat(mUsername, target, sdp.description, ChatType.Answer));
                    }
                }, sdp);
            }
        }, mMediaConstraints);
    }

    public void onRemoteSessionReceived(SessionDescription sessionDescription) {
        mPeerConnection.setRemoteDescription(new MySdpObserver(), sessionDescription);
    }

    public void closeConnection() {
        try {
            mVideoCapturer.dispose();
            mLocalStream.dispose();
            mPeerConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchCamera() {
        mVideoCapturer.switchCamera(null);
    }

    public void toggleAudio(Boolean shouldBeMuted) {
        if (shouldBeMuted) {
            mLocalStream.removeTrack(mLocalAudioTrack);
        } else {
            mLocalStream.addTrack(mLocalAudioTrack);
        }
    }

    public void toggleVideo(Boolean shouldBeMuted) {
        try {
            if (shouldBeMuted) {
                stopCapturingCamera();
            } else {
                startCapturingCamera(mLocalSurfaceView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addIceCandidateToPeer(IceCandidate iceCandidate) {
        mPeerConnection.addIceCandidate(iceCandidate);
    }

    public void sendIceCandidate(String target, IceCandidate iceCandidate) {
        addIceCandidateToPeer(iceCandidate);
        mListener.onTransferEventToSocket(new Chat(mUsername, target, mGsonManager.getGson().toJson(iceCandidate), ChatType.IceCandidate));
    }

    // streaming
    private void initSurfaceView(SurfaceViewRenderer renderer) {
        if(renderer == null) return;
        renderer.setMirror(false);
        renderer.setEnableHardwareScaler(true);
        renderer.init(eglBaseCtx, null);
    }

    public void initLocalSurfaceView(SurfaceViewRenderer localView, Boolean isVideoCall) {
        this.mLocalSurfaceView = localView;
        initSurfaceView(localView);
        startLocalStreaming(localView, isVideoCall);
    }

    public void initRemoteSurfaceView(SurfaceViewRenderer remoteSurfaceView) {
        this.mRemoteSurfaceView = remoteSurfaceView;
        initSurfaceView(remoteSurfaceView);
    }

    private void startLocalStreaming(SurfaceViewRenderer localView, Boolean isVideoCall) {
        mLocalStream = mPeerConnectionFactory.createLocalMediaStream(localStreamId);
        mLocalAudioTrack = mPeerConnectionFactory.createAudioTrack(localTrackId + "_audio", audioSrc);
        mLocalStream.addTrack(mLocalAudioTrack);
        if (isVideoCall) {
            startCapturingCamera(localView);
        }
        mPeerConnection.addTrack(mLocalAudioTrack);
    }

    private void startCapturingCamera(SurfaceViewRenderer localView) {
        mSurfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().getName(), eglBaseCtx);
        mVideoCapturer.initialize(mSurfaceTextureHelper, mContext, videoSource.getCapturerObserver());
        mVideoCapturer.startCapture(720, 480, 20);
        mLocalVideoTrack = mPeerConnectionFactory.createVideoTrack(localTrackId + "_video", videoSource);
        mLocalVideoTrack.addSink(localView);
        mLocalStream.addTrack(mLocalVideoTrack);
        mPeerConnection.addTrack(mLocalVideoTrack);
    }

    private void stopCapturingCamera() {
        mVideoCapturer.dispose();
        mLocalVideoTrack.removeSink(mLocalSurfaceView);
        mLocalSurfaceView.clearImage();
        mLocalStream.removeTrack(mLocalVideoTrack);
        mLocalVideoTrack.dispose();
    }

    private CameraVideoCapturer getVideoCapturer(Context context) {
        Camera2Enumerator enumerator = new Camera2Enumerator(context);
        String[] deviceNames = enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null);
            }
        }
        throw new IllegalStateException();
    }


    public interface SocketTransferListener {
        void onTransferEventToSocket(Chat chat);
    }

    public void setSocketTransferListener(SocketTransferListener listener) {
        mListener = listener;
    }
}
