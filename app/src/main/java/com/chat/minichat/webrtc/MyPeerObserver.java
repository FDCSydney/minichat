package com.chat.minichat.webrtc;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;

public class MyPeerObserver implements PeerConnection.Observer {
    /**
     * @param var1
     */
    @Override
    public void onSignalingChange(PeerConnection.SignalingState var1) {

    }

    /**
     * @param var1
     */
    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState var1) {

    }

    /**
     * @param var1
     */
    @Override
    public void onIceConnectionReceivingChange(boolean var1) {

    }

    /**
     * @param var1
     */
    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState var1) {

    }

    /**
     * @param var1
     */
    @Override
    public void onIceCandidate(IceCandidate var1) {

    }

    /**
     * @param var1
     */
    @Override
    public void onIceCandidatesRemoved(IceCandidate[] var1) {

    }

    /**
     * @param var1
     */
    @Override
    public void onAddStream(MediaStream var1) {

    }

    /**
     * @param var1
     */
    @Override
    public void onRemoveStream(MediaStream var1) {

    }

    /**
     * @param var1
     */
    @Override
    public void onDataChannel(DataChannel var1) {

    }

    /**
     *
     */
    @Override
    public void onRenegotiationNeeded() {

    }
}
