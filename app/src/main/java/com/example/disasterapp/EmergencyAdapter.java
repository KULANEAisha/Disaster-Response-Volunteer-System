package com.example.disasterapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.ViewHolder> {

    private Context context;
    private List<EmergencyRequest> emergencyList;

    public EmergencyAdapter(Context context, List<EmergencyRequest> emergencyList) {
        this.context = context;
        this.emergencyList = emergencyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_emergency_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyRequest request = emergencyList.get(position);

        // Text assignments with null checks
        holder.typeTextView.setText(request.getType() != null ? request.getType() : "Unknown");
        holder.locationTextView.setText(request.getLocation() != null ? request.getLocation() : "Unknown location");
        holder.dateTimeTextView.setText(request.getDateTime() != null ? request.getDateTime() : "No date");
        holder.urgencyTextView.setText(request.getUrgency() != null ? request.getUrgency() : "Unknown");

        // Truncate description (only show first 6 words)
        String description = request.getDescription() != null ? request.getDescription() : "No description";
        holder.descriptionTextView.setText(truncateDescription(description, 6));

        // Volunteer label
        String volunteersText = request.getVolunteers();
        if (volunteersText != null && !volunteersText.isEmpty()) {
            holder.volunteersTextView.setText("Volunteers needed: " + volunteersText);
        } else {
            holder.volunteersTextView.setText("Volunteers needed: 0");
        }

        // Urgency color logic
        String urgency = request.getUrgency();
        int urgencyColor;
        if (urgency == null || urgency.isEmpty()) {
            urgencyColor = context.getResources().getColor(android.R.color.darker_gray);
        } else if (urgency.equalsIgnoreCase("Low")) {
            urgencyColor = context.getResources().getColor(android.R.color.holo_green_light);
        } else if (urgency.equalsIgnoreCase("Medium")) {
            urgencyColor = context.getResources().getColor(android.R.color.holo_orange_light);
        } else if (urgency.equalsIgnoreCase("High")) {
            urgencyColor = context.getResources().getColor(android.R.color.holo_orange_dark);
        } else if (urgency.equalsIgnoreCase("Critical")) {
            urgencyColor = context.getResources().getColor(android.R.color.holo_red_light);
        } else {
            urgencyColor = context.getResources().getColor(android.R.color.darker_gray);
        }
        holder.urgencyTextView.setBackgroundTintList(ColorStateList.valueOf(urgencyColor));

        // Click listener (opens detail page)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EmergencyDetailActivity.class);
            intent.putExtra("emergencyId", request.getId());
            intent.putExtra("emergencyType", request.getType());
            intent.putExtra("location", request.getLocation());
            intent.putExtra("urgency", request.getUrgency());
            intent.putExtra("description", request.getDescription());
            intent.putExtra("volunteers", request.getVolunteers());
            intent.putExtra("dateTime", request.getDateTime());
            intent.putExtra("requiredSkills", request.getRequiredSkills());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return emergencyList.size();
    }

    // Helper: truncate description text
    private String truncateDescription(String description, int wordCount) {
        if (description == null || description.isEmpty()) {
            return "";
        }
        String[] words = description.split("\\s+");
        StringBuilder truncated = new StringBuilder();
        for (int i = 0; i < Math.min(wordCount, words.length); i++) {
            if (i > 0) truncated.append(" ");
            truncated.append(words[i]);
        }
        if (words.length > wordCount) {
            truncated.append("...");
        }
        return truncated.toString();
    }

    // ViewHolder class
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView typeTextView, descriptionTextView, locationTextView,
                dateTimeTextView, volunteersTextView, urgencyTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            volunteersTextView = itemView.findViewById(R.id.volunteersTextView);
            urgencyTextView = itemView.findViewById(R.id.urgencyTextView);
        }
    }
}