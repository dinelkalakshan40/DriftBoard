package com.driftboard.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.driftboard.app.R;
import com.driftboard.app.database.PostDatabaseHelper;

//show the splash screen and opens the dashboard
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the splash screen layout.
        setContentView(R.layout.activity_splash);

        // Initialize database to background thread while the splash screen is shown
        new Thread(() -> {
            // Open readable database so it is ready for use
            PostDatabaseHelper.getInstance(getApplicationContext()).getReadableDatabase();
        }).start();

        // Navigate to Dashboard after 1.5 seconds delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 1500);
    }
}
