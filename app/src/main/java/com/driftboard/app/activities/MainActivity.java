package com.driftboard.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.driftboard.app.R;
import com.driftboard.app.adapters.PostAdapter;
import com.driftboard.app.database.PostDatabaseHelper;
import com.driftboard.app.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

// MainActivity displays the dashboard, recent posts, statistics, and navigation options
public class MainActivity extends AppCompatActivity {
    //read saved posts from SQLite
    private PostDatabaseHelper dbHelper;
    private TextView tvTotalPostsCount, tvPendingUploadCount;
    private RecyclerView rvRecentPosts;
    private PostAdapter postAdapter;

    // This method runs when the dashboard screen is created.
    // It connects the layout views, loads saved posts, and sets up click actions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = PostDatabaseHelper.getInstance(this);

        NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);

        tvTotalPostsCount = findViewById(R.id.tvTotalPostsCount);
        tvPendingUploadCount = findViewById(R.id.tvPendingUploadCount);
        rvRecentPosts = findViewById(R.id.rvRecentPosts);

        rvRecentPosts.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(dbHelper.getAllPosts(), post -> {
            Intent intent = new Intent(MainActivity.this, PostDetailActivity.class);
            intent.putExtra("post_id", post.getId());
            startActivity(intent);
        });
        rvRecentPosts.setAdapter(postAdapter);

        // Quick Actions UI mapping
        MaterialCardView cardNewPost = findViewById(R.id.cardNewPost);
        MaterialCardView cardSearch = findViewById(R.id.cardSearch);
        MaterialCardView cardViewPosts = findViewById(R.id.cardViewPosts);
        MaterialCardView cardDeletePosts = findViewById(R.id.cardDeletePosts);
        FloatingActionButton fabNewPost = findViewById(R.id.fabNewPost);

        cardNewPost.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CreatePostActivity.class)));
        fabNewPost.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CreatePostActivity.class)));

        cardSearch.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        cardViewPosts.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ViewPostsActivity.class)));

        cardDeletePosts.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MultiDeleteActivity.class)));

        // Profile Avatar Click
        findViewById(R.id.viewProfileAvatar).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        // Bottom Navigation panel and  routing
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                if (nestedScrollView != null) {
                    nestedScrollView.post(() -> nestedScrollView.smoothScrollTo(0, 0));
                }
                return true;
            } else if (id == R.id.nav_posts) {
                startActivity(new Intent(MainActivity.this, ViewPostsActivity.class));
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                return true;
            } else if (id == R.id.nav_upload) {
                startActivity(new Intent(MainActivity.this, UploadQueueActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    // refreshes the post counts and recent posts
    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboardData();
    }

    // Refresh dashboard statistics counters and recent post cards from SQLite
    private void refreshDashboardData() {
        int total = dbHelper.getTotalCount();
        int pending = dbHelper.getPendingUploadCount();
        tvTotalPostsCount.setText(String.valueOf(total));
        tvPendingUploadCount.setText(String.valueOf(pending));

        List<Post> posts = dbHelper.getAllPosts();
        postAdapter.updateData(posts);
    }
}
