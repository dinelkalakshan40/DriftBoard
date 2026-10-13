package com.driftboard.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.driftboard.app.R;
import com.driftboard.app.adapters.PostAdapter;
import com.driftboard.app.database.PostDatabaseHelper;
import com.driftboard.app.models.Post;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private PostDatabaseHelper dbHelper;
    private EditText etSearchInput;
    private TextView tvResultsCount;
    private RecyclerView rvSearchResults;
    private PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = PostDatabaseHelper.getInstance(this);

        etSearchInput = findViewById(R.id.etSearchInput);
        tvResultsCount = findViewById(R.id.tvResultsCount);
        rvSearchResults = findViewById(R.id.rvSearchResults);

        findViewById(R.id.btnSearchBack).setOnClickListener(v -> finish());

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        List<Post> allPosts = dbHelper.getAllPosts();
        postAdapter = new PostAdapter(allPosts, post -> {
            Intent intent = new Intent(SearchActivity.this, PostDetailActivity.class);
            intent.putExtra("post_id", post.getId());
            startActivity(intent);
        });
        rvSearchResults.setAdapter(postAdapter);
        updateCount(allPosts.size());

        etSearchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        List<Post> results = query.isEmpty() ? dbHelper.getAllPosts() : dbHelper.searchPosts(query);
        postAdapter.updateData(results);
        updateCount(results.size());
    }

    private void updateCount(int count) {
        tvResultsCount.setText(count + " results in title & message");
    }
}
