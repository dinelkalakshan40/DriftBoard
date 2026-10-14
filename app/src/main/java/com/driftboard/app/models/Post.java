package com.driftboard.app.models;

import java.io.Serializable;

//  post entity in DriftBoard
public class Post implements Serializable {
    private long id;
    private String title;
    private String content;
    private String imageUri;
    private String status; // Sync status: "draft" / "synced"
    private String platform; // Target  platform: "wordpress" / "blogger"
    private String remotePostId; // Remote server post ID assigned upon successful upload
    private long createdAt;
    private long updatedAt;

    // Default constructor
    public Post() {
        this.status = "draft";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Parameterized constructor
    public Post(String title, String content, String imageUri) {
        this.title = title;
        this.content = content;
        this.imageUri = imageUri;
        this.status = "draft";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getRemotePostId() { return remotePostId; }
    public void setRemotePostId(String remotePostId) { this.remotePostId = remotePostId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
