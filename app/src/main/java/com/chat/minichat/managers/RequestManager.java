package com.chat.minichat.managers;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RequestManager {
    private static RequestManager mInstance;
    private final Context mContext;

    private RequestQueue mRequestQueue;

    private RequestManager(Context context) {
        if (mInstance != null)
            throw new RuntimeException("Use getInstance() to get instance of this class");
        mContext = context.getApplicationContext();
        mRequestQueue = this.getRequestQueue();
    }

    public static synchronized RequestManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RequestManager(context);
        }
        return mInstance;
    }

    RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }


}
