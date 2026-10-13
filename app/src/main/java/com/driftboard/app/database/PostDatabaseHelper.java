package com.driftboard.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.driftboard.app.models.Post;

import java.util.ArrayList;
import java.util.List;

public class PostDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "PostDatabaseHelper";
    private static final String DATABASE_NAME = "driftboard.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_POSTS = "posts";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_PLATFORM = "platform";
    public static final String COLUMN_REMOTE_ID = "remote_id";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    private static PostDatabaseHelper instance;

    public static synchronized PostDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PostDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private PostDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_POSTS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TITLE + " TEXT NOT NULL, "
                + COLUMN_CONTENT + " TEXT NOT NULL, "
                + COLUMN_IMAGE_URI + " TEXT, "
                + COLUMN_STATUS + " TEXT DEFAULT 'draft', "
                + COLUMN_PLATFORM + " TEXT, "
                + COLUMN_REMOTE_ID + " TEXT, "
                + COLUMN_CREATED_AT + " INTEGER NOT NULL, "
                + COLUMN_UPDATED_AT + " INTEGER NOT NULL);";
        db.execSQL(createTableQuery);

        // Insert mock data matching Figma screenshots if empty
        insertMockData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        onCreate(db);
    }

    private void insertMockData(SQLiteDatabase db) {
        long now = System.currentTimeMillis();
        String[][] mocks = {
            {"Trail notes — Ella's Ridge", "Started early, fog burned off by ten and the view opened up completely. Took the east switchback instead of the usual route — slower, but worth it for the ridge line. Packed light, glad I did.", "draft"},
            {"Sourdough, attempt six", "Finally got the crumb right this time. Six attempts, but worth every dense, gummy failure along the way.", "synced"},
            {"Studio cleanup day", "Reorganized everything, found three old sketchbooks and a box of fountain pen nibs I forgot I had.", "draft"},
            {"Rain on the porch", "Just sat and listened for an hour, no notes needed. The garden smells completely different after summer storms.", "draft"},
            {"Weekend baking log", "Two loaves of sourdough and a batch of cinnamon rolls.", "synced"}
        };

        for (int i = 0; i < mocks.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_TITLE, mocks[i][0]);
            cv.put(COLUMN_CONTENT, mocks[i][1]);
            cv.put(COLUMN_IMAGE_URI, "");
            cv.put(COLUMN_STATUS, mocks[i][2]);
            cv.put(COLUMN_PLATFORM, mocks[i][2].equals("synced") ? "wordpress" : null);
            cv.put(COLUMN_CREATED_AT, now - (i * 3600000L * 12));
            cv.put(COLUMN_UPDATED_AT, now - (i * 3600000L * 12));
            db.insert(TABLE_POSTS, null, cv);
        }
    }

    public long insertPost(Post post) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TITLE, post.getTitle());
        cv.put(COLUMN_CONTENT, post.getContent());
        cv.put(COLUMN_IMAGE_URI, post.getImageUri());
        cv.put(COLUMN_STATUS, post.getStatus() != null ? post.getStatus() : "draft");
        cv.put(COLUMN_PLATFORM, post.getPlatform());
        cv.put(COLUMN_REMOTE_ID, post.getRemotePostId());
        long now = System.currentTimeMillis();
        cv.put(COLUMN_CREATED_AT, now);
        cv.put(COLUMN_UPDATED_AT, now);

        long id = db.insert(TABLE_POSTS, null, cv);
        post.setId(id);
        return id;
    }

    public List<Post> getAllPosts() {
        List<Post> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_POSTS, null, null, null, null, null, COLUMN_CREATED_AT + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToPost(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public Post getPostById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_POSTS, null, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        Post post = null;
        if (cursor != null && cursor.moveToFirst()) {
            post = cursorToPost(cursor);
            cursor.close();
        }
        return post;
    }

    public int updatePost(Post post) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TITLE, post.getTitle());
        cv.put(COLUMN_CONTENT, post.getContent());
        cv.put(COLUMN_IMAGE_URI, post.getImageUri());
        cv.put(COLUMN_STATUS, post.getStatus());
        cv.put(COLUMN_PLATFORM, post.getPlatform());
        cv.put(COLUMN_REMOTE_ID, post.getRemotePostId());
        cv.put(COLUMN_UPDATED_AT, System.currentTimeMillis());

        return db.update(TABLE_POSTS, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(post.getId())});
    }

    public int deletePost(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_POSTS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int deleteMultiplePosts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedCount = 0;
        db.beginTransaction();
        try {
            for (Long id : ids) {
                deletedCount += db.delete(TABLE_POSTS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Transaction error during multiple delete", e);
        } finally {
            db.endTransaction();
        }
        return deletedCount;
    }

    public List<Post> searchPosts(String query) {
        List<Post> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ?";
        String wild = "%" + query + "%";
        String[] selectionArgs = new String[]{wild, wild};

        Cursor cursor = db.query(TABLE_POSTS, null, selection, selectionArgs, null, null, COLUMN_CREATED_AT + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToPost(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public int getTotalCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_POSTS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getPendingUploadCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_POSTS + " WHERE " + COLUMN_STATUS + " = 'draft'", null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    private Post cursorToPost(Cursor cursor) {
        Post post = new Post();
        post.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        post.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
        post.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
        post.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)));
        post.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
        post.setPlatform(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLATFORM)));
        post.setRemotePostId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMOTE_ID)));
        post.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
        post.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)));
        return post;
    }
}
