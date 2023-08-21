package com.chat.minichat.webrtc;

import org.webrtc.SessionDescription;

public class MySdpObserver implements org.webrtc.SdpObserver {
    /**
     * @param sdp
     */
    @Override
    public void onCreateSuccess(SessionDescription sdp) {

    }

    /**
     *
     */
    @Override
    public void onSetSuccess() {

    }

    /**
     * @param error
     */
    @Override
    public void onCreateFailure(String error) {

    }

    /**
     * @param error
     */
    @Override
    public void onSetFailure(String error) {

    }
}
