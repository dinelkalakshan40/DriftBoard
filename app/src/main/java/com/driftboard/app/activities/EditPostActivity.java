package com.driftboard.app.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.driftboard.app.R;
import com.driftboard.app.database.PostDatabaseHelper;
import com.driftboard.app.models.Post;

//activity users to edit their saved draft posts
public class EditPostActivity extends AppCompatActivity {

    private long postId;
    private Post post;
    private PostDatabaseHelper dbHelper;

    private EditText etEditTitle, etEditContent;
    private ImageView ivEditPreview;
    private Uri currentImageUri;

    //selected image is stored and displayed preview
    private final ActivityResultLauncher<String> replacePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    currentImageUri = uri;
                    ivEditPreview.setImageURI(uri);
                }
            });


    // edit existing posts and sets up the screen buttons
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        dbHelper = PostDatabaseHelper.getInstance(this);
        postId = getIntent().getLongExtra("post_id", -1);
        post = dbHelper.getPostById(postId);

        if (post == null) {
            finish();
            return;
        }

        etEditTitle = findViewById(R.id.etEditTitle);
        etEditContent = findViewById(R.id.etEditContent);
        ivEditPreview = findViewById(R.id.ivEditPreview);

        etEditTitle.setText(post.getTitle());
        etEditContent.setText(post.getContent());

        if (post.getImageUri() != null && !post.getImageUri().isEmpty()) {
            currentImageUri = Uri.parse(post.getImageUri());
            ivEditPreview.setImageURI(currentImageUri);
        }

        findViewById(R.id.btnEditClose).setOnClickListener(v -> finish());
        findViewById(R.id.btnDiscard).setOnClickListener(v -> finish());

        findViewById(R.id.tvEditTopSave).setOnClickListener(v -> saveChanges());
        findViewById(R.id.btnSaveChanges).setOnClickListener(v -> saveChanges());

        findViewById(R.id.btnEditReplace).setOnClickListener(v -> replacePictureLauncher.launch("image/*"));
        findViewById(R.id.btnEditRetake).setOnClickListener(v -> replacePictureLauncher.launch("image/*"));
        findViewById(R.id.btnEditRemove).setOnClickListener(v -> {
            currentImageUri = null;
            ivEditPreview.setImageDrawable(null);
        });
    }

    // validate updated post details and saves them to SQLite
    private void saveChanges() {
        String title = etEditTitle.getText().toString().trim();
        String content = etEditContent.getText().toString().trim();

        if (title.isEmpty()) title = "Untitled";
        if (content.isEmpty()) {
            Toast.makeText(this, "Content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        post.setTitle(title);
        post.setContent(content);
        post.setImageUri(currentImageUri != null ? currentImageUri.toString() : "");

        // Update existing post to SQLite database
        dbHelper.updatePost(post);
        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
