package com.chat.minichat.firebaseClient;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.chat.minichat.managers.RequestManager;
import com.chat.minichat.utils.ApiRoutes;
import com.chat.minichat.utils.enums.UserStatus;
import com.chat.minichat.interfaces.Callback;
import com.chat.minichat.managers.FirebaseDbManager;
import com.chat.minichat.managers.GsonManager;
import com.chat.minichat.models.Chat;
import com.chat.minichat.models.User;
import com.chat.minichat.utils.Constants;
import com.chat.minichat.utils.MyEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FirebaseClient {
    private final FirebaseDbManager mFirebaseDbManager;
    private String username;
    private final GsonManager mGsonManager;
    private final RequestManager mRequestManager;
    private final Context mContext;

    public FirebaseClient(Context context) {
        mFirebaseDbManager = FirebaseDbManager.getInstance();
        mGsonManager = GsonManager.getInstance();
        mRequestManager = RequestManager.getInstance(context);
        mContext = context;
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
        DatabaseReference dbRef = this.mFirebaseDbManager.getReference();
        dbRef.addValueEventListener(new MyEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                                chat = mGsonManager.getGson().fromJson(Objects.requireNonNull(snapshot.getValue()).toString(), Chat.class);
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

    public void logOff(Runnable runnable, String username) {
        DatabaseReference dbRef = this.mFirebaseDbManager.getReference();
        dbRef.child(username).child(Constants.FirebaseField.STATUS).setValue(UserStatus.OFFLINE)
                .addOnSuccessListener(res -> {
                    runnable.run();
                });
    }

//    public void createRoom(String username, Callback.ChatConnectionRequestCallback callback, String secretKey) {
//        DatabaseReference dbRef = mFirebaseDbManager.getReference();
//        dbRef.child(username).child(Constants.FirebaseField.ROOM_ID).setValue(secretKey)
//                .addOnSuccessListener(res->{
//                    callback.isSuccess(true);
//                });
//    }
public void createRoom(String username, final Callback.ChatConnectionRequestCallback callback, String secretKey) {
    String url = ApiRoutes.FIREBASE_ROOT_URL + username + ".json";

    JSONObject roomData = new JSONObject();
    try {
        roomData.put(Constants.FirebaseField.ROOM_ID, secretKey);
    } catch (JSONException e) {
        e.printStackTrace();
    }

    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.PATCH, url, roomData,
            response -> {
                callback.isSuccess(true);
            },
            error -> {
                // Handle error
                callback.isSuccess(false);
            });
    mRequestManager.getRequestQueue().add(jsonObjectRequest);
}



    public interface Listener {
        void onLatestEventReceived(Chat chat);
    }
}
