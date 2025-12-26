package com.example.disasterapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UpdateAdapter extends RecyclerView.Adapter<UpdateAdapter.ViewHolder> {

    private static final String TAG = "UpdateAdapter";
    private final Context context;
    private final List<Update> updateList;

    public UpdateAdapter(Context context, List<Update> updateList) {
        this.context = context;
        this.updateList = updateList;
        Log.d(TAG, "UpdateAdapter created with list size: " + (updateList != null ? updateList.size() : 0));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(context).inflate(R.layout.item_update, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder called for position: " + position);

        if (updateList == null || position >= updateList.size()) {
            Log.e(TAG, "Invalid position or null list");
            return;
        }

        Update update = updateList.get(position);

        if (update == null) {
            Log.e(TAG, "Update is null at position: " + position);
            return;
        }

        Log.d(TAG, "Binding update: " + update.getUserName() + " - " + update.getDescription());

        // ✅ User name (default to "Anonymous" if missing)
        String userName = update.getUserName() != null && !update.getUserName().isEmpty()
                ? update.getUserName()
                : "Anonymous";
        holder.userNameTextView.setText(userName);

        // ✅ Description (optional)
        holder.updateDescriptionTextView.setText(
                update.getDescription() != null ? update.getDescription() : ""
        );

        // ✅ Timestamp (optional)
        holder.timestampTextView.setText(
                update.getTimestamp() != null ? update.getTimestamp() : ""
        );

        // ✅ Handle profile image or avatar letter
        if (update.getProfileImageUrl() != null && !update.getProfileImageUrl().isEmpty()) {
            // Show profile photo
            holder.userProfileImageView.setVisibility(View.VISIBLE);
            holder.userAvatarTextView.setVisibility(View.GONE);

            Glide.with(context)
                    .load(update.getProfileImageUrl())
                    .placeholder(R.drawable.ic_person) // placeholder while loading
                    .error(R.drawable.ic_person)        // fallback if failed
                    .circleCrop()
                    .into(holder.userProfileImageView);
        } else {
            // No profile photo → show first letter or '?'
            holder.userProfileImageView.setVisibility(View.GONE);
            holder.userAvatarTextView.setVisibility(View.VISIBLE);

            String initial = userName != null && !userName.isEmpty()
                    ? String.valueOf(userName.charAt(0)).toUpperCase()
                    : "?";
            holder.userAvatarTextView.setText(initial);
        }

        // ✅ Handle update image (photo attached to post)
        if (update.getImageUrl() != null && !update.getImageUrl().isEmpty()) {
            holder.updateImageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(update.getImageUrl())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(holder.updateImageView);
        } else {
            holder.updateImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        int count = updateList != null ? updateList.size() : 0;
        Log.d(TAG, "getItemCount returning: " + count);
        return count;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userAvatarTextView, userNameTextView, updateDescriptionTextView, timestampTextView;
        ImageView userProfileImageView, updateImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatarTextView = itemView.findViewById(R.id.userAvatarTextView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            updateDescriptionTextView = itemView.findViewById(R.id.updateDescriptionTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            userProfileImageView = itemView.findViewById(R.id.userProfileImageView);
            updateImageView = itemView.findViewById(R.id.updateImageView);
        }
    }
}
