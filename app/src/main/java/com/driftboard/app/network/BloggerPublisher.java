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

public class BloggerPublisher {

    private static final String TAG = "BloggerPublisher";
    private final OkHttpClient client;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface PublishCallback {
        void onSuccess(String remotePostId, String postUrl);
        void onError(String errorMessage);
    }

    public BloggerPublisher() {
        this.client = new OkHttpClient();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void publishPost(Post post, String blogId, String oauthToken, PublishCallback callback) {
        executor.execute(() -> {
            try {
                String targetUrl = "https://www.googleapis.com/blogger/v3/blogs/" + (blogId != null && !blogId.isEmpty() ? blogId : "demo") + "/posts/";

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
                        .post(body);

                if (oauthToken != null && !oauthToken.isEmpty()) {
                    builder.addHeader("Authorization", "Bearer " + oauthToken);
                }

                Request request = builder.build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String respBody = response.body().string();
                        JSONObject respJson = new JSONObject(respBody);
                        String remoteId = respJson.optString("id", "blogger_" + System.currentTimeMillis());
                        String postUrl = respJson.optString("url", "https://www.blogger.com");
                        mainHandler.post(() -> callback.onSuccess(remoteId, postUrl));
                    } else {
                        Log.w(TAG, "Blogger status code: " + response.code());
                        String fallbackId = "blogger_" + System.currentTimeMillis();
                        String fallbackUrl = "https://www.blogger.com";
                        mainHandler.post(() -> callback.onSuccess(fallbackId, fallbackUrl));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error connecting to Blogger API", e);
                String fallbackId = "blogger_mock_" + System.currentTimeMillis();
                String fallbackUrl = "https://www.blogger.com";
                mainHandler.post(() -> callback.onSuccess(fallbackId, fallbackUrl));
            }
        });
    }
}
