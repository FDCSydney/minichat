package com.chat.minichat.repository;

import com.chat.minichat.enums.ChatType;
import com.chat.minichat.firebaseClient.FirebaseClient;
import com.chat.minichat.interfaces.Callback;
import com.chat.minichat.models.Chat;

public class MainRepository {
    private Listener mListener;
    private final FirebaseClient mFirebaseClient;

    public MainRepository() {
        this.mFirebaseClient = new FirebaseClient();
    }

    public void login(String username, String password, Callback.LoginCallback callback) {
        this.mFirebaseClient.login(username, password, callback);
    }

    public void observeUsersStatus(Callback.UserCallBack callback) {
        this.mFirebaseClient.observeUsersStatus(callback);
    }

    public void initFirebase(String username) {
        mFirebaseClient.subscribeForLatestEvent(username, (chat) -> {
            mListener.onLatestEventReceived(chat);
//            switch (chat.getType()) {
//                default:
//                    break;
//            }
        });
    }

    public void sendConnectionRequest(
            String sender,
            String target ,
            Boolean isVideoCall ,
            Callback.SendMessageCallback callback){
        ChatType type = isVideoCall ? ChatType.StartVideoCall:ChatType.StartAudioCall;
        mFirebaseClient.sendMessageToOtherClient(new Chat(sender,type,target), callback);
    }

    public interface Listener {
        void onLatestEventReceived(Chat chat);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }
}
