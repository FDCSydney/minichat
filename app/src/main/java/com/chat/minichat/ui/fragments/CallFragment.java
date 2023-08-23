package com.chat.minichat.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.chat.minichat.R;
import com.chat.minichat.databinding.FragmentCallBinding;
import com.chat.minichat.managers.RTCAudioManager;
import com.chat.minichat.service.MainService;
import com.chat.minichat.service.MainServiceRepository;

/**
 * @author sbecher
 */
public class CallFragment extends Fragment implements MainService.CallEndedListener {
    private FragmentCallBinding mBinding;

    private boolean isMicrophoneMuted;
    private boolean isCameraMuted;
    private boolean isSilentMode;
    public BackPressListener mBackPressListener;
    public Handler mHandler;
    public Runnable timerRunnable;
    private MainServiceRepository mMainServiceRepository;
    public int timerValue = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentCallBinding.inflate(inflater, container, false);
        mMainServiceRepository = new MainServiceRepository(getContext());
        mHandler = new Handler();
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        MainService.setCallEndedListener(this);
        // timer
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                timerValue++;
                mBinding.callTimerTv.setText(convertToHumanTime(timerValue));
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.post(timerRunnable);

        Bundle bundle = getArguments();
        assert bundle != null;
        String mTarget = bundle.getString("target");
        Boolean mIsVideoCall = bundle.getBoolean("isVideoCall");
        Boolean mIsCaller = bundle.getBoolean("isCaller");
        mBinding.callTitleTv.setText("Call with " + mTarget);
        if (!mIsVideoCall) {
            mBinding.screenShareButton.setVisibility(View.GONE);
            mBinding.switchCameraButton.setVisibility(View.GONE);
            mBinding.toggleCameraButton.setVisibility(View.GONE);
        }
        MainService.mLocalView = mBinding.localView;
        MainService.mRemoteView = mBinding.remoteView;
        mMainServiceRepository.setUpViews(mTarget, mIsVideoCall, mIsCaller);

        mBinding.endCallButton.setOnClickListener(view -> {
            mMainServiceRepository.sendEndCall();
        });
        mBinding.switchCameraButton.setOnClickListener(view -> {
            mMainServiceRepository.switchCamera();
        });
        setUpToggleCamera();
        setUpToggleMicrophone();
        setUpToggleAudioDevice();
    }

    private String convertToHumanTime(int seconds) {
        int minutes = seconds / 60;
        seconds %= 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void setUpToggleCamera() {
        mBinding.toggleCameraButton.setOnClickListener(view -> {
            if (!isCameraMuted) {
                mBinding.toggleCameraButton.setImageResource(R.drawable.ic_camera_on);
                mMainServiceRepository.toggleVideo(true);
            } else {
                mBinding.toggleCameraButton.setImageResource(R.drawable.ic_camera_off);
                mMainServiceRepository.toggleVideo(false);
            }

            isCameraMuted = !isCameraMuted;
        });
    }

    private void setUpToggleMicrophone() {
        mBinding.toggleMicrophoneButton.setOnClickListener(view -> {
            if (!isMicrophoneMuted) {
                mBinding.toggleMicrophoneButton.setImageResource(R.drawable.ic_mic_on);
                mMainServiceRepository.toggleAudio(true);
            } else {
                mBinding.toggleMicrophoneButton.setImageResource(R.drawable.ic_mic_off);
                mMainServiceRepository.toggleAudio(false);
            }
            isMicrophoneMuted = !isMicrophoneMuted;
        });
    }

    private void setUpToggleAudioDevice() {
        mBinding.toggleAudioDevice.setOnClickListener(view -> {
            if (!isSilentMode) {
                mBinding.toggleAudioDevice.setImageResource(R.drawable.ic_ear_off);
                mMainServiceRepository.toggleAudioDevice(RTCAudioManager.AudioDevice.EARPIECE.name());
            } else {
                mBinding.toggleAudioDevice.setImageResource(R.drawable.ic_ear);
                mMainServiceRepository.toggleAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE.name());
            }
            isSilentMode = !isSilentMode;
        });
    }

    /**
     *
     */
    @Override
    public void onCallEnded() {
        FragmentManager manager = getParentFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(this).commit();
    }

    @Override
    public void onDestroy() {
        MainService.mLocalView.release();
        MainService.mLocalView = null;
        MainService.mRemoteView.release();
        MainService.mRemoteView = null;
        super.onDestroy();

    }

    public interface BackPressListener {
        void onFragmentBackPressed();
    }

    public void setBackPressedListener(BackPressListener listener) {
        mBackPressListener = listener;
    }
}