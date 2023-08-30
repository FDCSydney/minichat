package com.chat.minichat.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.chat.minichat.R;
import com.chat.minichat.adapters.MainRecyclerViewAdapter;
import com.chat.minichat.databinding.ActivityMainBinding;
import com.chat.minichat.models.Chat;
import com.chat.minichat.models.User;
import com.chat.minichat.repository.MainRepository;
import com.chat.minichat.service.MainService;
import com.chat.minichat.service.MainServiceRepository;
import com.chat.minichat.ui.fragments.CallFragment;
import com.chat.minichat.utils.enums.ChatType;
import com.chat.minichat.utils.enums.UserStatus;

//import org.jitsi.meet.sdk.BroadcastEvent;
//import org.jitsi.meet.sdk.JitsiMeet;
//import org.jitsi.meet.sdk.JitsiMeetActivity;
//import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends BaseActivity implements MainService.CallReceivedListener,
        CallFragment.BackPressListener {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding mBinding;
    private MainRepository mRepository;

    private String username;

    private MainRecyclerViewAdapter mAdapter;

    private boolean isVideoCall;
    private boolean isHost;
    private Bundle mSaveInstanceState;
    private User mUser;
    private Chat mChat;
    private CallFragment mCallFragment;

    private MainServiceRepository mMainServiceRepository;
    private BroadcastReceiver mBroadcastReceiver;

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
        mCallFragment = new CallFragment();
        mMainServiceRepository = new MainServiceRepository(this);
        prepareRecyclerView();
        prepareJitsi();
        setUpConferenceCallReceiver();
        username = getIntent().getStringExtra("username");
        if (username == null) finish();
        Glide.with(this)
                .load("")
                .placeholder(R.mipmap.pp_placeholder)
                .fitCenter()
                .into(mBinding.customToolbar.currentUserPp);
        mBinding.customToolbar.currentUserStatus.setBackgroundColor(Color.parseColor("#00cc00"));
        mBinding.customToolbar.createRoom.setOnClickListener(view -> {
            String roomID = String.valueOf(System.currentTimeMillis());
            mRepository.createRoom(username, roomID, status -> {
                if (!status) return;
                Toast.makeText(this, "Room Created!", Toast.LENGTH_SHORT).show();
                isHost = true;
                new Handler().postDelayed(() -> {
                    joinRoom(roomID);
                }, 1000);
            });
        });
        mBinding.customToolbar.currentUserPp.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm")
                    .setMessage("Are you sure to log out?")
                    .setCancelable(true)
                    .setPositiveButton("log out", (dialog, i) -> {
                        dialog.cancel();
                        mRepository.updateStatus(username, UserStatus.OFFLINE);
                        finish();
                    }).setNegativeButton("Cancel", (dialog, i) -> dialog.cancel())
                    .create()
                    .show();

        });
        subScribeObservers();
        startService();
    }

    private void prepareRecyclerView() {
        mBinding.userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.userRecyclerView.setHasFixedSize(true);
    }

    public void setUpConferenceCallReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onBroadcastReceived(intent);
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    private void subScribeObservers() {
        MainService.setCallReceivedListener(this);
        mRepository.observeUsersStatus(users -> {
            List<User> filteredUser = users.stream().filter(user -> !Objects.equals(user.getName(), username))
                    .collect(Collectors.toList());
            mAdapter = new MainRecyclerViewAdapter(this, filteredUser);
            mBinding.userRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
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

                /**
                 * @param user
                 */
                @Override
                public void onJoinRoomClicked(User user) {
                    joinRoom(user.getRoomId());
                }
            });

        });

    }

    private void startService() {
        mMainServiceRepository.startService(username);
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
        if (requestCode == PERMISSION_CALL_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                mBinding.incomingCallLayout.setVisibility(View.GONE);
                executeCallToFragment(true, mChat.getType() == ChatType.StartVideoCall, mChat.getSender(), false);
            }
        }
    }

    private void executeCallToFragment(Boolean status, Boolean isVideoCall, String name, Boolean isCaller) {
        if (!status) {
            Toast.makeText(MainActivity.this,
                    "No response from target caller", Toast.LENGTH_SHORT).show();
            return;
        }
        // move to call fragment to start call
        if (mSaveInstanceState != null) return;
        mCallFragment.setBackPressedListener(this);
        mBinding.customToolbar.mainToolbar.setVisibility(View.GONE);
        Bundle bundle = new Bundle();
        bundle.putString("target", name);
        bundle.putBoolean("isVideoCall", isVideoCall);
        bundle.putBoolean("isCaller", isCaller);
        mCallFragment.setArguments(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_call_container, mCallFragment)
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
            mChat = chat;
            boolean isVideo = chat.getType() == ChatType.StartVideoCall;
            String callText = isVideo ? "video" : "audio";
            mBinding.incomingCallTitleTv.setText(chat.getSender() + " wants to " + callText + " call with you.");
            mBinding.incomingCallLayout.setVisibility(View.VISIBLE);
            mBinding.acceptButton.setOnClickListener(view -> {
                if (checkPermissions()) {
                    mBinding.incomingCallLayout.setVisibility(View.GONE);
                    executeCallToFragment(true, isVideo, chat.getSender(), false);
                } else {
                    requestReceiverPermissions();
                }
            });
            mBinding.declineButton.setOnClickListener(view -> {
                mBinding.incomingCallLayout.setVisibility(View.GONE);
                mRepository.sendConnectionRequest(chat.getTarget(), chat.getSender(), ChatType.Declined, status -> {
                });
            });
        });
    }

    /**
     *
     */
    @Override
    public void onCallDeclined(Chat chat) {
        runOnUiThread(() -> {
            getSupportFragmentManager().beginTransaction().remove(mCallFragment).commit();
            mRepository.clearLatestEvent(chat.getSender());
            mRepository.clearLatestEvent(chat.getTarget());
            Toast.makeText(this, "Callee Rejected you call", Toast.LENGTH_SHORT).show();
        });

    }

    private void prepareJitsi() {
//        JitsiMeetConferenceOptions defaultOptions;
//        try {
//            defaultOptions = new JitsiMeetConferenceOptions.Builder()
//                    .setServerURL(new URL("https://8x8.vc"))
//                    .setToken("eyJraWQiOiJ2cGFhcy1tYWdpYy1jb29raWUtNzQ2NjBjMjA2YjlhNGUyNDgzYzJmYzBjOTNjOGQ2ZTcvMGY5YTQyLVNBTVBMRV9BUFAiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJqaXRzaSIsImlzcyI6ImNoYXQiLCJpYXQiOjE2OTMzNjQ1MjQsImV4cCI6MTY5MzM3MTcyNCwibmJmIjoxNjkzMzY0NTE5LCJzdWIiOiJ2cGFhcy1tYWdpYy1jb29raWUtNzQ2NjBjMjA2YjlhNGUyNDgzYzJmYzBjOTNjOGQ2ZTciLCJjb250ZXh0Ijp7ImZlYXR1cmVzIjp7ImxpdmVzdHJlYW1pbmciOnRydWUsIm91dGJvdW5kLWNhbGwiOnRydWUsInNpcC1vdXRib3VuZC1jYWxsIjpmYWxzZSwidHJhbnNjcmlwdGlvbiI6dHJ1ZSwicmVjb3JkaW5nIjp0cnVlfSwidXNlciI6eyJoaWRkZW4tZnJvbS1yZWNvcmRlciI6ZmFsc2UsIm1vZGVyYXRvciI6dHJ1ZSwibmFtZSI6ImZkYy5zeWRuZXliZSIsImlkIjoiZ29vZ2xlLW9hdXRoMnwxMTc1NjA2NjU2MjU3OTIzNDA1MTMiLCJhdmF0YXIiOiIiLCJlbWFpbCI6ImZkYy5zeWRuZXliZUBnbWFpbC5jb20ifX0sInJvb20iOiIqIn0.Z-fr2UxBd8Qi6AiS1Z_AOJ68q5DKAIDQvv-jgzwY_MxHWVRvUyZb4kM9mCaLdxnQsKp4peCxAw747-52FKVMjGrSFSUsoP_iF5rGfZYohh9ubXAOua6HjRDMrOTeQEKy4xAhS3Q_A09126lDfOAtWphVn-pSIIFdD1gbbQMZjOtK18sYacmBMP2i_wK92AKi48V9n3vLyLf6-o5PuduYJyrOHIurSHmqDuy28bvcwQY_EDCLR5nta3zf_12ddEVtYrUJ08pXDLcZTT9hGSKkaC_u7DEgGjYfmU14WeJqK3Qo4500AkS5RWEw_x9a8KUSGQ3KBatcpyItEyJDhiT8Tw")
//                    .setFeatureFlag("welcomepage.enabled", false)
//                    .build();
//        } catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
//        JitsiMeet.setDefaultConferenceOptions(defaultOptions);
    }


    // connect to room with jitsi
    public void joinRoom(String roomID) {
//        String roomName = "vpaas-magic-cookie-74660c206b9a4e2483c2fc0c93c8d6e7/" + roomID;
//        JitsiMeetConferenceOptions options
//                = new JitsiMeetConferenceOptions.Builder()
//                .setRoom(roomName)
//                .setAudioMuted(true)
//                .setVideoMuted(true)
//                .setFeatureFlag("welcomepage.enabled", false)
//                .build();
//        JitsiMeetActivity.launch(this, options);
//        registerForBroadcastMessages();
    }

    public void onBroadcastReceived(Intent intent) {
//        if (intent == null) return;
//        BroadcastEvent event = new BroadcastEvent(intent);
//        if ((event.getType() == BroadcastEvent.Type.CONFERENCE_TERMINATED) && isHost) {
//            mRepository.removeRoom(username, status -> {
//            });
//        }
        ;

    }

    private void registerForBroadcastMessages() {
//        IntentFilter intentFilter = new IntentFilter();
//        for (BroadcastEvent.Type type : BroadcastEvent.Type.values()) {
//            intentFilter.addAction(type.getAction());
//        }
//        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    /**
     *
     */
    @Override
    public void onFragmentBackPressed() {
        getSupportFragmentManager().beginTransaction().remove(mCallFragment).commit();
        mBinding.customToolbar.mainToolbar.setVisibility(View.VISIBLE);
        mMainServiceRepository.sendEndCall();
    }

    /**
     *
     */
    @Override
    public void onEndCall() {
        mBinding.customToolbar.mainToolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (mCallFragment != null && mCallFragment.mBackPressListener != null && mCallFragment.isVisible()) {
            mCallFragment.mBackPressListener.onFragmentBackPressed();
            mCallFragment.mHandler.removeCallbacks(mCallFragment.timerRunnable);
        } else {
            super.onBackPressed();
            mMainServiceRepository.stopService();
        }
    }
}