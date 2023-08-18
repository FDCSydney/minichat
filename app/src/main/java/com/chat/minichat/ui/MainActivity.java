package com.chat.minichat.ui;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.chat.minichat.R;
import com.chat.minichat.adapters.MainRecyclerViewAdapter;
import com.chat.minichat.databinding.ActivityMainBinding;
import com.chat.minichat.fragments.CallFragment;
import com.chat.minichat.models.Chat;
import com.chat.minichat.models.User;
import com.chat.minichat.repository.MainRepository;
import com.chat.minichat.service.MainService;
import com.chat.minichat.service.MainServiceRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends BaseActivity implements MainService.Listener {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding mBinding;
    private MainRepository mRepository;

    private String username;

    private MainRecyclerViewAdapter mAdapter;

    private Boolean isVideoCall = false;
    private Bundle mSaveInstanceState;
    private MainService mMainService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);
        mSaveInstanceState = savedInstanceState;
        setSupportActionBar(mBinding.customToolbar.mainToolbar);
        mRepository = new MainRepository();
        mMainService = new MainService();
        init();
        prepareRecyclerView();
    }

    private void init() {
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
        mMainService.onSetListener(this);
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
                    if (checkPermissions()) {
                        mRepository.sendConnectionRequest(username, user.getName(), isVideoCall, status -> {
//                            executeCallToFragment(status);
                        });
                    } else {
                        requestPermissions();
                    }
                }

                @Override
                public void onVideoCallClicked(User user) {
                    isVideoCall = true;
                    if (checkPermissions()) {
                        mRepository.sendConnectionRequest(username,user.getName(), isVideoCall, status -> {
//                            executeCallToFragment(status);
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
//                executeCallToFragment(true);
            } else {
                Toast.makeText(this, "Camera and mic permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void executeCallToFragment(Boolean status) {
        if (!status) {
            Toast.makeText(MainActivity.this,
                    "No response from target caller", Toast.LENGTH_SHORT).show();
            return;
        }
        // move to call fragment to start call
        if (mSaveInstanceState != null) return;
        mBinding.customToolbar.mainToolbar.setVisibility(View.INVISIBLE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_call_container, new CallFragment())
                .addToBackStack(null)
                .commit();
    }

    /**
     * @param chat
     */
    @Override
    public void onCallReceived(Chat chat) {
        Log.d(TAG, "onCallReceived: " + chat.toString());
        runOnUiThread(()->{
            Toast.makeText(this, "this is chat", Toast.LENGTH_SHORT).show();
        });
    }
}