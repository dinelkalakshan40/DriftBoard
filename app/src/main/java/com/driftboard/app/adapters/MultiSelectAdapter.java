package com.driftboard.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.driftboard.app.R;
import com.driftboard.app.models.Post;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiSelectAdapter extends RecyclerView.Adapter<MultiSelectAdapter.ViewHolder> {

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int count);
    }

    private final List<Post> posts;
    private final Set<Long> selectedIds = new HashSet<>();
    private final OnSelectionChangeListener listener;

    public MultiSelectAdapter(List<Post> posts, OnSelectionChangeListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    public Set<Long> getSelectedIds() {
        return selectedIds;
    }

    public void selectAll() {
        selectedIds.clear();
        for (Post p : posts) {
            selectedIds.add(p.getId());
        }
        notifyDataSetChanged();
        if (listener != null) listener.onSelectionChanged(selectedIds.size());
    }

    public void deselectAll() {
        selectedIds.clear();
        notifyDataSetChanged();
        if (listener != null) listener.onSelectionChanged(0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multi_select, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.tvTitle.setText(post.getTitle());

        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(selectedIds.contains(post.getId()));

        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedIds.add(post.getId());
            } else {
                selectedIds.remove(post.getId());
            }
            if (listener != null) {
                listener.onSelectionChanged(selectedIds.size());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            holder.cbSelect.setChecked(!holder.cbSelect.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            tvTitle = itemView.findViewById(R.id.tvPostTitleMulti);
        }
    }
}
