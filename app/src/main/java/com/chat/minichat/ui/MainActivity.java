package com.chat.minichat.ui;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.chat.minichat.R;
import com.chat.minichat.adapters.MainRecyclerViewAdapter;
import com.chat.minichat.databinding.ActivityMainBinding;
import com.chat.minichat.enums.ChatType;
import com.chat.minichat.fragments.CallFragment;
import com.chat.minichat.managers.GsonManager;
import com.chat.minichat.models.Chat;
import com.chat.minichat.models.User;
import com.chat.minichat.repository.MainRepository;
import com.chat.minichat.service.MainService;
import com.chat.minichat.service.MainServiceRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends BaseActivity implements MainService.CallReceivedListener {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding mBinding;
    private MainRepository mRepository;

    private String username;

    private MainRecyclerViewAdapter mAdapter;

    private Boolean isVideoCall = false;
    private Bundle mSaveInstanceState;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);
        mSaveInstanceState = savedInstanceState;
        setSupportActionBar(mBinding.customToolbar.mainToolbar);

        init();
    }

    private void init() {
        mRepository = MainRepository.getInstance(this);
        prepareRecyclerView();
        username = getIntent().getStringExtra("username");
        if (username == null) finish();
        Glide.with(this)
                .load("")
                .placeholder(R.mipmap.pp_placeholder)
                .fitCenter()
                .into(mBinding.customToolbar.currentUserPp);
        mBinding.customToolbar.currentUserStatus.setBackgroundColor(Color.parseColor("#00cc00"));
        mBinding.customToolbar.createRoom.setOnClickListener(view -> {
            Toast.makeText(this, "Will Create Room!", Toast.LENGTH_SHORT).show();
        });
        subScribeObservers();
        startService();
    }

    private void prepareRecyclerView() {
        mBinding.userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.userRecyclerView.setHasFixedSize(true);
    }

    private void subScribeObservers() {
        MainService.setCallReceivedListener(this);
        mRepository.observeUsersStatus(users -> {
            List<User> filteredUser = users.stream().filter(user -> !Objects.equals(user.getName(), username))
                    .collect(Collectors.toList());
            mAdapter = new MainRecyclerViewAdapter(this, filteredUser);
            mBinding.userRecyclerView.setAdapter(mAdapter);
//            TODO: implement specific notifier
//            mAdapter.notifyDataSetChanged();
            mAdapter.setOnClickListener(new MainRecyclerViewAdapter.ClickListener() {
                @Override
                public void onAudioCallClicked(User user) {
                    isVideoCall = false;
                    mUser = user;
                    if (checkPermissions()) {
                        mRepository.sendConnectionRequest(username, user.getName(), isVideoCall, status -> {
                            executeCallToFragment(status, isVideoCall, user.getName(), true);
                        });
                    } else {
                        requestPermissions();
                    }
                }

                @Override
                public void onVideoCallClicked(User user) {
                    isVideoCall = true;
                    mUser = user;
                    if (checkPermissions()) {
                        mRepository.sendConnectionRequest(username, user.getName(), isVideoCall, status -> {
                            executeCallToFragment(status, isVideoCall, user.getName(), true);
                        });
                    } else {
                        requestPermissions();
                    }
                }
            });

        });

    }

    private void startService() {
        new MainServiceRepository(this).startService(username);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // do here audio or video call
                mRepository.sendConnectionRequest(username, mUser.getName(), isVideoCall, status -> {
                    executeCallToFragment(status, isVideoCall, mUser.getName(), true);
                });
            } else {
                Toast.makeText(this, "Camera and mic permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void executeCallToFragment(Boolean status, Boolean isVideoCall, String username, Boolean isCaller) {
        if (!status) {
            Toast.makeText(MainActivity.this,
                    "No response from target caller", Toast.LENGTH_SHORT).show();
            return;
        }
        // move to call fragment to start call
        if (mSaveInstanceState != null) return;
        mBinding.customToolbar.mainToolbar.setVisibility(View.GONE);
        CallFragment fragment = new CallFragment();
        Bundle bundle = new Bundle();
        bundle.putString("target", username);
        bundle.putBoolean("isVideoCall", isVideoCall);
        bundle.putBoolean("isCaller", isCaller);
        fragment.setArguments(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_call_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * @param chat
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onCallReceived(Chat chat) {
        runOnUiThread(() -> {
            boolean isVideoCall = chat.getType() == ChatType.StartVideoCall;
            String callText = isVideoCall ? "video" : "audio";
            mBinding.incomingCallTitleTv.setText(chat.getSender() + " wants to " + callText + " call with you.");
            mBinding.incomingCallLayout.setVisibility(View.VISIBLE);
            mBinding.acceptButton.setOnClickListener(view -> {
                if (checkPermissions()) {
                    mBinding.incomingCallLayout.setVisibility(View.GONE);
                    executeCallToFragment(true, isVideoCall, chat.getSender(), false);
                }
            });
            mBinding.declineButton.setOnClickListener(view -> {
                mBinding.incomingCallLayout.setVisibility(View.GONE);
            });
        });
    }
}