package com.chat.minichat.firebaseClient;

import androidx.annotation.NonNull;

import com.chat.minichat.enums.UserStatus;
import com.chat.minichat.interfaces.Callback;
import com.chat.minichat.managers.FirebaseDbManager;
import com.chat.minichat.managers.GsonManager;
import com.chat.minichat.models.Chat;
import com.chat.minichat.models.User;
import com.chat.minichat.utils.Constants;
import com.chat.minichat.utils.MyEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FirebaseClient {
    private final FirebaseDbManager mFirebaseDbManager;
    private String username;
    private final GsonManager mGsonManager;

    public FirebaseClient() {
        mFirebaseDbManager = FirebaseDbManager.getInstance();
        mGsonManager = GsonManager.getInstance();
    }

    public void login(String username, String password, Callback.LoginCallback callback) {
        DatabaseReference dbRef = this.mFirebaseDbManager.getReference();
        dbRef.addListenerForSingleValueEvent(new MyEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(username)) {
                    //check pass if user exist
                    Object pass = snapshot.child(username).child(Constants.FirebaseField.PASSWORD).getValue();
                    if (pass != null && pass.toString().equals(password)) {
                        dbRef.child(username).child(Constants.FirebaseField.STATUS).setValue(UserStatus.ONLINE)
                                .addOnCompleteListener(task -> {
                                    callback.onComplete(true, null);
                                })
                                .addOnFailureListener(e -> {
                                    callback.onComplete(false, e.getMessage());
                                });
                    } else {
                        // password is wrong, notify user
                        callback.onComplete(false, "PassWord is Incorrect");
                    }
                } else {
                    // user does not exist register user
                    dbRef.child(username).child(Constants.FirebaseField.PASSWORD).setValue(password)
                            .addOnCompleteListener(task -> {
                                dbRef.child(username).child(Constants.FirebaseField.STATUS).setValue(UserStatus.ONLINE)
                                        .addOnCompleteListener(task1 -> {
                                            callback.onComplete(true, "Successful User creation.");

                                        })
                                        .addOnFailureListener(e -> {
                                            callback.onComplete(false, e.getMessage());
                                        });
                            })
                            .addOnFailureListener(e -> {
                                callback.onComplete(false, e.getMessage());
                            });
                }
            }
        });
    }

    public void observeUsersStatus(Callback.UserCallBack callBack) {
        //TODO: to be converted to volley request to comply requirements in on boarding
        DatabaseReference dbRef = this.mFirebaseDbManager.getReference();
        dbRef.addValueEventListener(new MyEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                List<Map.Entry<String, String>> users = StreamSupport
//                        .stream(snapshot.getChildren().spliterator(), false)
//                        .filter(user -> !Objects.equals(user.getKey(), currentUsername()))
//                        .map(snap ->
//                                new AbstractMap.SimpleEntry<>(snap.getKey(), Objects.requireNonNull(snap.child(Constants.FirebaseField.STATUS).getValue()).toString()))
//                        .collect(Collectors.toList());
//                callBack.onObserveStatus(users);
                Iterable<DataSnapshot> dataSnapshotIterable = snapshot.getChildren();
                List<User> users = new ArrayList<>();
                for (DataSnapshot snap : dataSnapshotIterable) {
                    String key = snap.getKey();
                    String json = Objects.requireNonNull(snap.getValue()).toString();
                    User userModel = mGsonManager.getGson().fromJson(json, User.class);
                    userModel.setName(key);
                    users.add(userModel);
                }
                callBack.onObserveStatus(users);
            }
        });
    }

    public void sendMessageToOtherClient(Chat chat, Callback.SendMessageCallback callback) {
        String convertedMessage = mGsonManager.getGson().toJson(chat);
        DatabaseReference dbRef = this.mFirebaseDbManager.getReference();
        dbRef.child(chat.getTarget()).child(Constants.FirebaseField.LATEST_EVENT).setValue(convertedMessage)
                .addOnCompleteListener(res -> {
                    callback.getMessageStatus(true);
                })
                .addOnFailureListener(err -> {
                    callback.getMessageStatus(false);
                });
    }

    public void subscribeForLatestEvent(String username, Listener listener) {
        try {
            DatabaseReference dbRef = this.mFirebaseDbManager.getReference();
            dbRef.child(username).child(Constants.FirebaseField.LATEST_EVENT)
                    .addValueEventListener(new MyEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            super.onDataChange(snapshot);
                            Chat chat = null;
                            try {
                                chat = mGsonManager.getGson().fromJson(snapshot.getValue().toString(), Chat.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (chat != null) {
                                listener.onLatestEventReceived(chat );
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateMyStatus(String username, UserStatus status) {
        DatabaseReference dbRef = this.mFirebaseDbManager.getReference();
        dbRef.child(username).child(Constants.FirebaseField.STATUS).setValue(status.name());
    }

    public void removeLatestEvent(String username) {
        DatabaseReference dbRef = this.mFirebaseDbManager.getReference();
        dbRef.child(username).child(Constants.FirebaseField.LATEST_EVENT).setValue(null);
    }


    public interface Listener {
        void onLatestEventReceived(Chat chat);
    }
}
