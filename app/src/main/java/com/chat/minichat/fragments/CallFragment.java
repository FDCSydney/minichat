package com.chat.minichat.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chat.minichat.databinding.FragmentCallBinding;
import com.chat.minichat.service.MainService;
import com.chat.minichat.service.MainServiceRepository;

/**
 * @author sbecher
 */
public class CallFragment extends Fragment {
    private FragmentCallBinding mBinding;
    private String mTarget;
    private Boolean mIsCaller = true;
    private Boolean mIsVideoCall = true;
    private MainServiceRepository mMainServiceRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentCallBinding.inflate(inflater, container, false);
        mMainServiceRepository = new MainServiceRepository(getContext());
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        Bundle bundle = getArguments();
        assert bundle != null;
        mTarget = bundle.getString("target");
        mIsVideoCall = bundle.getBoolean("isVideoCall");
        mIsCaller = bundle.getBoolean("isCaller");
        mBinding.callTitleTv.setText("Call with " + mTarget);
        if (!mIsVideoCall) {
            mBinding.screenShareButton.setVisibility(View.GONE);
            mBinding.switchCameraButton.setVisibility(View.GONE);
            mBinding.toggleCameraButton.setVisibility(View.GONE);
        }
        MainService.mLocalView = mBinding.localView;
        MainService.mRemoteView = mBinding.remoteView;
        mMainServiceRepository.setUpViews(mTarget, mIsVideoCall, mIsCaller);
    }


}