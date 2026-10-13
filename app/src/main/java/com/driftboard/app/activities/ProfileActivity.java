package com.driftboard.app.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.driftboard.app.R;
import com.driftboard.app.database.PostDatabaseHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        findViewById(R.id.btnProfileBack).setOnClickListener(v -> finish());

        int count = PostDatabaseHelper.getInstance(this).getTotalCount();
        TextView menuStorage = findViewById(R.id.menuStorage);
        menuStorage.setText("💾  Local storage — " + count + " posts");

        findViewById(R.id.menuPosts).setOnClickListener(v -> finish());
        findViewById(R.id.menuSettings).setOnClickListener(v -> Toast.makeText(this, "Settings & API configurations", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_delete, null);

        TextView tvTitle = (TextView) view.findViewById(R.id.tvDeleteWarning);
        tvTitle.setText("Log out of DriftBoard?\nYour local posts will remain safely on this device.");

        view.findViewById(R.id.btnCancelDelete).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnConfirmDelete).setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            finish();
        });

        dialog.setContentView(view);
        dialog.show();
    }
}
