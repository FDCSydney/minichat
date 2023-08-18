package com.chat.minichat.ui;

import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class BaseActivity extends AppCompatActivity {
    public static final int PERMISSION_REQUEST_CODE = 123; // Use any unique code

    public boolean checkPermissions() {
        int cameraPermission = checkSelfPermission(android.Manifest.permission.CAMERA);
        int audioPermission = checkSelfPermission(android.Manifest.permission.RECORD_AUDIO);
        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                audioPermission == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE
        );
    }


}
