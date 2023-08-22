package com.chat.minichat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.bumptech.glide.Glide;
import com.chat.minichat.R;
import com.chat.minichat.databinding.UserBinding;
import com.chat.minichat.models.User;

import java.util.List;

public class MainRecyclerViewAdapter extends Adapter {

    private final List<User> mUsers;

    private MainRecyclerViewHolder mMainRecyclerViewHolder;
    private ClickListener mCallback;
    private MainRecyclerViewHolder mViewHolder;
    private Context mContext;

    public MainRecyclerViewAdapter(Context context, List<User> users) {
        mUsers = users;
        mContext = context;
    }

    /**
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return view holder
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mViewHolder = new MainRecyclerViewHolder(UserBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false));
        return mViewHolder;
    }

    /**
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        User user = mUsers.get(position);
        mViewHolder = (MainRecyclerViewHolder) holder;
        if(user != null) mViewHolder.setValue(user);
    }

    /**
     * @return size of list
     */
    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class MainRecyclerViewHolder extends RecyclerView.ViewHolder {
        private final UserBinding mUserBinding;

        public MainRecyclerViewHolder(@NonNull UserBinding binding) {
            super(binding.getRoot());
            mUserBinding = binding;
        }

        public void setValue(User user) {
            //TODO: Update user avatar using Glide
            Glide.with(mContext)
                    .load("")
                    .placeholder(R.mipmap.pp_placeholder)
                    .fitCenter()
                    .into(mUserBinding.pp);
            String name = user.getName();
            mUserBinding.username.setText(name != null ? name: "");
            switch (user.getStatus()) {
                case "ONLINE":
                    mUserBinding.status.setBackgroundColor(Color.parseColor("#00cc00"));
                    break;
                case "OFFLINE":
                    mUserBinding.status.setBackgroundColor(Color.parseColor("#4f4e4a"));
                    break;
                default:
                    mUserBinding.status.setBackgroundColor(Color.parseColor("#a80510"));
                    break;
            }
            mUserBinding.aCall.setOnClickListener(view -> {
                mCallback.onAudioCallClicked(user);
            });

            mUserBinding.vCall.setOnClickListener(view -> {
                mCallback.onVideoCallClicked(user);
            });
        }
    }

    public interface ClickListener {
        void onAudioCallClicked(User user);

        void onVideoCallClicked(User user);
    }

    public void setOnClickListener(ClickListener callback) {
        mCallback = callback;
    }
}
