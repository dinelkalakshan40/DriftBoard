package com.driftboard.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.driftboard.app.R;
import com.driftboard.app.adapters.PostAdapter;
import com.driftboard.app.database.PostDatabaseHelper;
import com.driftboard.app.models.Post;

import java.util.List;

public class ViewPostsActivity extends AppCompatActivity {

    private PostDatabaseHelper dbHelper;
    private TextView tvTotalPostsBadge;
    private RecyclerView rvAllPosts;
    private PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);

        dbHelper = PostDatabaseHelper.getInstance(this);

        tvTotalPostsBadge = findViewById(R.id.tvTotalPostsBadge);
        rvAllPosts = findViewById(R.id.rvAllPosts);

        findViewById(R.id.btnViewPostsBack).setOnClickListener(v -> finish());

        rvAllPosts.setLayoutManager(new LinearLayoutManager(this));

        List<Post> posts = dbHelper.getAllPosts();
        postAdapter = new PostAdapter(posts, post -> {
            Intent intent = new Intent(ViewPostsActivity.this, PostDetailActivity.class);
            intent.putExtra("post_id", post.getId());
            startActivity(intent);
        });
        rvAllPosts.setAdapter(postAdapter);

        updatePostCount(posts.size());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPosts();
    }

    private void refreshPosts() {
        List<Post> posts = dbHelper.getAllPosts();
        if (postAdapter != null) {
            postAdapter.updateData(posts);
        }
        updatePostCount(posts.size());
    }

    private void updatePostCount(int count) {
        tvTotalPostsBadge.setText(count + " posts on device");
    }
}
