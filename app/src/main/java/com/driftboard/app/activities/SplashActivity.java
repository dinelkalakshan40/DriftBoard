package com.driftboard.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.driftboard.app.R;
import com.driftboard.app.database.PostDatabaseHelper;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Pre-initialize database in background
        new Thread(() -> {
            PostDatabaseHelper.getInstance(getApplicationContext()).getReadableDatabase();
        }).start();

        // Navigate to Dashboard after 1.5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 1500);
    }
}
