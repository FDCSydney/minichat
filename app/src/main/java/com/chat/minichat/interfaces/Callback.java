package com.chat.minichat.interfaces;

import com.chat.minichat.models.User;

import java.util.List;

public class Callback {
    public interface LoginCallback {
        void onComplete(Boolean isSuccess, String error);
    }

    public interface UserCallBack {
        void onObserveStatus(List<User> users);
    }

    public interface SendMessageCallback{
        void getMessageStatus(Boolean messageStatus);
    }

    public interface ChatConnectionRequestCallback{
        void isSuccess(Boolean isSuccess);
    }

    public interface StopServiceCallback{
        void onSuccess(Boolean status);
    }
}
