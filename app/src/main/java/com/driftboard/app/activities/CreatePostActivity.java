package com.driftboard.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.driftboard.app.R;
import com.driftboard.app.database.PostDatabaseHelper;
import com.driftboard.app.models.Post;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private FrameLayout layoutImagePreview;
    private ImageView ivPreview;
    private String currentPhotoPath;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && currentPhotoPath != null) {
                    selectedImageUri = Uri.fromFile(new File(currentPhotoPath));
                    showImagePreview(selectedImageUri);
                }
            });

    private final ActivityResultLauncher<String> selectPictureLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}
                    showImagePreview(selectedImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        layoutImagePreview = findViewById(R.id.layoutImagePreview);
        ivPreview = findViewById(R.id.ivPreview);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        findViewById(R.id.tvTopSave).setOnClickListener(v -> savePost());
        findViewById(R.id.btnSavePost).setOnClickListener(v -> savePost());

        findViewById(R.id.btnCamera).setOnClickListener(v -> dispatchTakePictureIntent());
        findViewById(R.id.btnGallery).setOnClickListener(v -> selectPictureLauncher.launch("image/*"));

        findViewById(R.id.btnRemoveImage).setOnClickListener(v -> {
            selectedImageUri = null;
            layoutImagePreview.setVisibility(View.GONE);
        });
    }

    private void showImagePreview(Uri uri) {
        layoutImagePreview.setVisibility(View.VISIBLE);
        ivPreview.setImageURI(uri);
    }

    private void dispatchTakePictureIntent() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureLauncher.launch(photoURI);
            }
        } catch (IOException ex) {
            Toast.makeText(this, "Could not open camera file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void savePost() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            title = content.length() > 25 ? content.substring(0, 25) + "..." : content;
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter message content", Toast.LENGTH_SHORT).show();
            return;
        }

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setImageUri(selectedImageUri != null ? selectedImageUri.toString() : "");
        post.setStatus("draft");

        PostDatabaseHelper.getInstance(this).insertPost(post);
        Toast.makeText(this, "Post saved locally", Toast.LENGTH_SHORT).show();
        finish();
    }
}
