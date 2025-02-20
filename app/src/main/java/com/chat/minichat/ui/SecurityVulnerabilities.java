package com.chat.minichat.ui;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

public class SecurityVulnerabilities extends Activity {

    // Hardcoded API Key (Vulnerability)
    private static final String API_KEY = "1234567890abcdef";

    // Hardcoded database credentials (Vulnerability)
    private static final String DB_USERNAME = "admin";
    private static final String DB_PASSWORD = "password123";

    // Memory Leak: Static reference to an Activity
    private static SecurityVulnerabilities leakedActivity;

    // Memory Leak: Anonymous inner class with Handler
    private Handler handler = new Handler();

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Simulating an API request and database connection
        connectToDatabase();
        makeApiRequest();

        // Memory Leak: Holding a static reference to the activity
        leakedActivity = this;

        // Memory Leak: Handler posts a delayed Runnable that holds an implicit reference to Activity
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Leak", "This runnable references the activity and causes a leak!");
            }
        }, 60000); // 60 seconds delay
    }

    private void connectToDatabase() {
        Log.d("Security", "Connecting to database with:");
        Log.d("Security", "Username: " + DB_USERNAME);
        Log.d("Security", "Password: " + DB_PASSWORD);
    }

    private void makeApiRequest() {
        Log.d("Security", "Making API request with key: " + API_KEY);
    }
}
