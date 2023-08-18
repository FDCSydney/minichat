package com.chat.minichat.managers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDbManager {
    private static FirebaseDbManager mInstance;
    private final FirebaseDatabase mFirebaseDatabase;

    private FirebaseDbManager() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    public static synchronized FirebaseDbManager getInstance() {
        if (mInstance == null) {
            mInstance = new FirebaseDbManager();
        }
        return mInstance;
    }

    public FirebaseDatabase getFirebaseDb() {
        return mFirebaseDatabase;
    }

    public DatabaseReference getReference() {
        return mFirebaseDatabase.getReference();
    }
}
