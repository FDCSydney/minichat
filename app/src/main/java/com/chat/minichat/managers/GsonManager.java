package com.chat.minichat.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author sbecher
 */
public class GsonManager {
    private static GsonManager mInstance;
    private final Gson mGson;

    private GsonManager() {
        if (mInstance != null)
            throw new RuntimeException("Use getInstance() to get instance of this class");
        mGson = new GsonBuilder().create();
    }

    public static synchronized GsonManager getInstance() {
        if (mInstance == null) {
            mInstance = new GsonManager();
        }
        return mInstance;
    }

    public Gson getGson() {
        return mGson;
    }

}
