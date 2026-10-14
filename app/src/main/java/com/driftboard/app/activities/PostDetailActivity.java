package com.driftboard.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.driftboard.app.R;
import com.driftboard.app.database.PostDatabaseHelper;
import com.driftboard.app.models.Post;
import com.driftboard.app.network.BloggerPublisher;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Activity display full details of post with actions for editing, sharing, deleting, and Blogger upload
public class PostDetailActivity extends AppCompatActivity {

    private long postId;
    private Post post;
    private PostDatabaseHelper dbHelper;

    private TextView tvDetailBadge, tvAuthorMeta, tvDetailTitle, tvDetailContent;
    private ImageView ivDetailHero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        dbHelper = PostDatabaseHelper.getInstance(this);
        postId = getIntent().getLongExtra("post_id", -1);

        tvDetailBadge = findViewById(R.id.tvDetailBadge);
        tvAuthorMeta = findViewById(R.id.tvAuthorMeta);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailContent = findViewById(R.id.tvDetailContent);
        ivDetailHero = findViewById(R.id.ivDetailHero);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnDetailEdit).setOnClickListener(v -> {
            Intent intent = new Intent(PostDetailActivity.this, EditPostActivity.class);
            intent.putExtra("post_id", postId);
            startActivity(intent);
        });

        findViewById(R.id.btnDetailShare).setOnClickListener(v -> showShareBottomSheet());
        findViewById(R.id.btnDetailDelete).setOnClickListener(v -> showDeleteBottomSheet());

        View btnUploadBlogger = findViewById(R.id.btnUploadBlogger);
        if (btnUploadBlogger != null) {
            btnUploadBlogger.setOnClickListener(v -> uploadSinglePostToBlogger());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPostData();
    }

    // method load the selected post and updates the information screen.
    private void loadPostData() {
        post = dbHelper.getPostById(postId);
        if (post == null) {
            finish();
            return;
        }

        tvDetailTitle.setText(post.getTitle());
        tvDetailContent.setText(post.getContent());

        if ("synced".equalsIgnoreCase(post.getStatus())) {
            tvDetailBadge.setText("• SYNCED");
            tvDetailBadge.setBackgroundResource(R.drawable.bg_badge_synced);
            tvDetailBadge.setTextColor(getColor(R.color.badge_synced_text));
        } else {
            tvDetailBadge.setText("• DRAFT");
            tvDetailBadge.setBackgroundResource(R.drawable.bg_badge_draft);
            tvDetailBadge.setTextColor(getColor(R.color.badge_draft_text));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        String dateStr = sdf.format(new Date(post.getCreatedAt()));
        tvAuthorMeta.setText("Jordan Alvarez · " + dateStr + " · on-device");

        if (post.getImageUri() != null && !post.getImageUri().isEmpty()) {
            ivDetailHero.setImageURI(Uri.parse(post.getImageUri()));
        }
    }

    //uploads the selected post to the user's Blogger account and opens published post in a browser
    private void uploadSinglePostToBlogger() {
        if (post == null) return;
        android.content.SharedPreferences prefs = getSharedPreferences("driftboard_prefs", MODE_PRIVATE);
        String blogId = prefs.getString("blogger_blog_id", "");
        String token = prefs.getString("blogger_oauth_token", "");

        if (blogId.isEmpty() || token.isEmpty()) {
            Toast.makeText(this, "Please configure your Blogger Blog ID and OAuth Token in Profile -> Settings first.", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Uploading message to Blogger...", Toast.LENGTH_SHORT).show();
        BloggerPublisher bloggerPublisher = new BloggerPublisher();
        bloggerPublisher.publishPost(post, blogId, token, new BloggerPublisher.PublishCallback() {
            @Override
            public void onSuccess(String remotePostId, String postUrl) {
                post.setStatus("synced");
                post.setPlatform("blogger");
                post.setRemotePostId(remotePostId);
                dbHelper.updatePost(post);
                loadPostData();

                Toast.makeText(PostDetailActivity.this, "Uploaded message to Blogger successfully!", Toast.LENGTH_LONG).show();

                if (postUrl != null && !postUrl.isEmpty()) {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(postUrl));
                        startActivity(browserIntent);
                    } catch (Exception e) {
                        Toast.makeText(PostDetailActivity.this, "Could not open browser", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(PostDetailActivity.this, "Blogger Upload Failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    //display confirmation dialog before deleting a post
    private void showDeleteBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_delete, null);
        TextView tvWarning = view.findViewById(R.id.tvDeleteWarning);
        tvWarning.setText("\"" + post.getTitle() + "\" will be removed from this device. This can't be undone.");

        view.findViewById(R.id.btnCancelDelete).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnConfirmDelete).setOnClickListener(v -> {
            dbHelper.deletePost(postId);
            dialog.dismiss();
            Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show();
            finish();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    //displays the available options for sharing a post
    private void showShareBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_share, null);

        view.findViewById(R.id.btnCancelShare).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnSystemShare).setOnClickListener(v -> {
            dialog.dismiss();
            launchSystemSharesheet();
        });

        // Share  post WhatsApp
        view.findViewById(R.id.targetWhatsApp).setOnClickListener(v -> {
            dialog.dismiss();
            launchTargetShare("com.whatsapp");
        });

        //share on fb
        view.findViewById(R.id.targetMessenger).setOnClickListener(v -> {
            dialog.dismiss();
            launchTargetShare("com.facebook.orca");
        });

        view.findViewById(R.id.targetX).setOnClickListener(v -> {
            dialog.dismiss();
            launchTargetShare("com.twitter.android");
        });

        view.findViewById(R.id.targetEmail).setOnClickListener(v -> {
            dialog.dismiss();
            launchTargetShare("com.google.android.gm");
        });

        dialog.setContentView(view);
        dialog.show();
    }

    //share the post on specific application
    private void launchTargetShare(String packageName) {
        Intent shareIntent = createShareIntent();
        if (packageName != null && !packageName.isEmpty()) {
            shareIntent.setPackage(packageName);
        }
        try {
            startActivity(shareIntent);
        } catch (Exception e) {
            launchSystemSharesheet();
        }
    }

    // sharing intent containing the post title and content
    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, post.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, post.getTitle() + "\n\n" + post.getContent());

        if (post.getImageUri() != null && !post.getImageUri().isEmpty()) {
            try {
                Uri imageUri = Uri.parse(post.getImageUri());
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.setType("image/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {}
        }
        return shareIntent;
    }

    // opens Android's standard sharing menu
    private void launchSystemSharesheet() {
        Intent shareIntent = createShareIntent();
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }
}
