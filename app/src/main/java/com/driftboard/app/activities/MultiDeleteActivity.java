package com.driftboard.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.driftboard.app.R;
import com.driftboard.app.adapters.MultiSelectAdapter;
import com.driftboard.app.database.PostDatabaseHelper;
import com.driftboard.app.models.Post;

import java.util.ArrayList;
import java.util.List;

// This activity can users to select and delete multiple posts at once
public class MultiDeleteActivity extends AppCompatActivity {

    private PostDatabaseHelper dbHelper;
    private TextView tvSelectedCount;
    private Button btnBatchDelete;
    private MultiSelectAdapter adapter;
    private boolean allSelected = false;

    //method runs the multi-delete screen opens
    //loads the posts and sets up the selection and delete buttons.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_delete);

        dbHelper = PostDatabaseHelper.getInstance(this);

        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        btnBatchDelete = findViewById(R.id.btnBatchDelete);
        RecyclerView rvMultiDelete = findViewById(R.id.rvMultiDelete);

        findViewById(R.id.btnMultiCancel).setOnClickListener(v -> finish());

        // Get all saved posts from local database
        List<Post> posts = dbHelper.getAllPosts();
        adapter = new MultiSelectAdapter(posts, count -> {
            tvSelectedCount.setText(count + " selected");
            btnBatchDelete.setText("Delete " + (count > 0 ? count + " " : "") + "Posts");
        });

        rvMultiDelete.setLayoutManager(new LinearLayoutManager(this));
        rvMultiDelete.setAdapter(adapter);

        findViewById(R.id.btnSelectAll).setOnClickListener(v -> {
            if (!allSelected) {
                adapter.selectAll();
                allSelected = true;
            } else {
                adapter.deselectAll();
                allSelected = false;
            }
        });

        // Delete selected posts when delete button clicked
        btnBatchDelete.setOnClickListener(v -> {
            List<Long> ids = new ArrayList<>(adapter.getSelectedIds());
            if (ids.isEmpty()) {
                Toast.makeText(this, "No posts selected", Toast.LENGTH_SHORT).show();
                return;
            }

            int deleted = dbHelper.deleteMultiplePosts(ids);
            Toast.makeText(this, "Deleted " + deleted + " posts", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
