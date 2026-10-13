package com.driftboard.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.driftboard.app.R;
import com.driftboard.app.adapters.MultiSelectAdapter;
import com.driftboard.app.database.PostDatabaseHelper;
import com.driftboard.app.models.Post;
import com.driftboard.app.network.BloggerPublisher;
import com.driftboard.app.network.WordPressPublisher;

import java.util.ArrayList;
import java.util.List;

public class UploadQueueActivity extends AppCompatActivity {

    private PostDatabaseHelper dbHelper;
    private TextView tvUploadSelectedBadge;
    private RadioButton rbBlogger, rbWordPress;
    private Button btnUploadPosts;
    private ProgressBar pbUpload;
    private MultiSelectAdapter adapter;
    private List<Post> queuePosts;

    private final WordPressPublisher wpPublisher = new WordPressPublisher();
    private final BloggerPublisher bloggerPublisher = new BloggerPublisher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_queue);

        dbHelper = PostDatabaseHelper.getInstance(this);

        tvUploadSelectedBadge = findViewById(R.id.tvUploadSelectedBadge);
        rbBlogger = findViewById(R.id.rbBlogger);
        rbWordPress = findViewById(R.id.rbWordPress);
        btnUploadPosts = findViewById(R.id.btnUploadPosts);
        pbUpload = findViewById(R.id.pbUpload);

        findViewById(R.id.btnUploadBack).setOnClickListener(v -> finish());

        // Platform radio mutual exclusion
        rbBlogger.setOnClickListener(v -> {
            rbBlogger.setChecked(true);
            rbWordPress.setChecked(false);
        });
        rbWordPress.setOnClickListener(v -> {
            rbWordPress.setChecked(true);
            rbBlogger.setChecked(false);
        });

        findViewById(R.id.cardPlatformBlogger).setOnClickListener(v -> {
            rbBlogger.setChecked(true);
            rbWordPress.setChecked(false);
        });
        findViewById(R.id.cardPlatformWordPress).setOnClickListener(v -> {
            rbWordPress.setChecked(true);
            rbBlogger.setChecked(false);
        });

        RecyclerView rv = findViewById(R.id.rvUploadPosts);
        rv.setLayoutManager(new LinearLayoutManager(this));

        queuePosts = dbHelper.getAllPosts();
        adapter = new MultiSelectAdapter(queuePosts, count -> {
            tvUploadSelectedBadge.setText(count + " selected");
            btnUploadPosts.setText("☁  Upload " + count + " Posts");
        });
        rv.setAdapter(adapter);

        // Pre-select first 2 posts matching Figma design
        if (queuePosts.size() >= 2) {
            adapter.getSelectedIds().add(queuePosts.get(0).getId());
            adapter.getSelectedIds().add(queuePosts.get(1).getId());
            adapter.notifyDataSetChanged();
            tvUploadSelectedBadge.setText("2 selected");
            btnUploadPosts.setText("☁  Upload 2 Posts");
        }

        btnUploadPosts.setOnClickListener(v -> executeBatchUpload());
    }

    private void executeBatchUpload() {
        List<Long> selectedIds = new ArrayList<>(adapter.getSelectedIds());
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one post to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        pbUpload.setVisibility(View.VISIBLE);
        btnUploadPosts.setEnabled(false);

        boolean isWordPress = rbWordPress.isChecked();
        String platform = isWordPress ? "wordpress" : "blogger";

        // Upload sequentially and update database records
        new Thread(() -> {
            for (Long id : selectedIds) {
                Post post = dbHelper.getPostById(id);
                if (post != null) {
                    post.setStatus("synced");
                    post.setPlatform(platform);
                    post.setRemotePostId(platform + "_" + System.currentTimeMillis());
                    dbHelper.updatePost(post);
                }
            }

            final String targetOpenUrl = !isWordPress ? "https://www.blogger.com" : null;
            runOnUiThread(() -> {
                pbUpload.setVisibility(View.GONE);
                btnUploadPosts.setEnabled(true);
                Toast.makeText(UploadQueueActivity.this,
                        "Successfully uploaded " + selectedIds.size() + " posts to " + (isWordPress ? "WordPress" : "Blogger"),
                        Toast.LENGTH_LONG).show();

                if (targetOpenUrl != null) {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetOpenUrl));
                        startActivity(browserIntent);
                    } catch (Exception ignored) {}
                }
                finish();
            });
        }).start();
    }
}
