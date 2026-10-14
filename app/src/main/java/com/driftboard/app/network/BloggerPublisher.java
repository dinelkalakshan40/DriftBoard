package com.driftboard.app.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.driftboard.app.models.Post;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// publishing posts to the Blogger API
public class BloggerPublisher {

    private static final String TAG = "BloggerPublisher";
    private final OkHttpClient client;
    private final ExecutorService executor;
    private final Handler mainHandler;

    // This interface returns the result of publishing a post
    public interface PublishCallback {
        void onSuccess(String remotePostId, String postUrl);
        void onError(String errorMessage);
    }


    // Creates the objects needed for network communication
    public BloggerPublisher() {
        this.client = new OkHttpClient();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // Publishes a post to Blogger without blocking the main thread
    public void publishPost(Post post, String blogId, String oauthToken, PublishCallback callback) {
        executor.execute(() -> {
            try {
                if (blogId == null || blogId.isEmpty()) {
                    mainHandler.post(() -> callback.onError("Blogger Blog ID is missing. Configure it in Profile -> Settings."));
                    return;
                }
                if (oauthToken == null || oauthToken.isEmpty()) {
                    mainHandler.post(() -> callback.onError("Blogger OAuth Token is missing. Configure it in Profile -> Settings."));
                    return;
                }

                String targetUrl = "https://www.googleapis.com/blogger/v3/blogs/" + blogId + "/posts/";

                JSONObject json = new JSONObject();
                json.put("kind", "blogger#post");
                json.put("title", post.getTitle());
                json.put("content", post.getContent());

                RequestBody body = RequestBody.create(
                        json.toString(),
                        MediaType.get("application/json; charset=utf-8")
                );

                Request.Builder builder = new Request.Builder()
                        .url(targetUrl)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + oauthToken);

                Request request = builder.build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String respBody = response.body().string();
                        JSONObject respJson = new JSONObject(respBody);
                        String remoteId = respJson.optString("id", "blogger_" + System.currentTimeMillis());
                        String postUrl = respJson.optString("url", "https://www.blogger.com");
                        mainHandler.post(() -> callback.onSuccess(remoteId, postUrl));
                    } else {
                        String errorMsg = "HTTP " + response.code() + " - Invalid Blog ID or Access Token.";
                        Log.w(TAG, errorMsg);
                        mainHandler.post(() -> callback.onError(errorMsg));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error connecting to Blogger API", e);
                mainHandler.post(() -> callback.onError(e.getMessage() != null ? e.getMessage() : "Network error connecting to Blogger"));
            }
        });
    }
}
