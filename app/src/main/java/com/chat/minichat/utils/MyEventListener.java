package com.chat.minichat.utils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MyEventListener implements ValueEventListener {
    /**
     * @param snapshot The current data at the location
     */
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {

    }

    /**
     * @param error A description of the error that occurred
     */
    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
}
