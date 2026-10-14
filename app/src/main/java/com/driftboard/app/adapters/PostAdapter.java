package com.driftboard.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.driftboard.app.R;
import com.driftboard.app.models.Post;

import java.util.List;

// This adapter displays saved posts cards with their sync status
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {


    // Listener used to handle clicks on  post card
    public interface OnItemClickListener {
        // Sends the clicked post back to the activity
        void onItemClick(Post post);
    }

    private List<Post> postList;
    private final OnItemClickListener listener;

    // Receives posts and click listener from the activity
    public PostAdapter(List<Post> postList, OnItemClickListener listener) {
        this.postList = postList;
        this.listener = listener;
    }

    // Updates adapter dataset and refreshes view
    public void updateData(List<Post> newPosts) {
        this.postList = newPosts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_card, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.tvPostNumber.setText(String.valueOf(position + 1));
        holder.tvPostTitle.setText(post.getTitle());
        holder.tvPostSnippet.setText(post.getContent());

        // Bind status badge styling based on draft vs synced state
        if ("synced".equalsIgnoreCase(post.getStatus())) {
            holder.tvStatusBadge.setText("synced");
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_synced);
            holder.tvStatusBadge.setTextColor(holder.itemView.getContext().getColor(R.color.badge_synced_text));
        } else {
            holder.tvStatusBadge.setText("draft");
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_draft);
            holder.tvStatusBadge.setTextColor(holder.itemView.getContext().getColor(R.color.badge_draft_text));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    // Holds references to views inside each post card
    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvPostNumber, tvPostTitle, tvPostSnippet, tvStatusBadge;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPostNumber = itemView.findViewById(R.id.tvPostNumber);
            tvPostTitle = itemView.findViewById(R.id.tvPostTitle);
            tvPostSnippet = itemView.findViewById(R.id.tvPostSnippet);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}
