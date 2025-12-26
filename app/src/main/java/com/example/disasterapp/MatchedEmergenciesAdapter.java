package com.example.disasterapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MatchedEmergenciesAdapter extends RecyclerView.Adapter<MatchedEmergenciesAdapter.ViewHolder> {

    private Context context;
    private List<EmergencyMatcher.EmergencyMatch> matches;

    public MatchedEmergenciesAdapter(Context context, List<EmergencyMatcher.EmergencyMatch> matches) {
        this.context = context;
        this.matches = matches;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_matched_emergency, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyMatcher.EmergencyMatch match = matches.get(position);
        EmergencyRequest emergency = match.getEmergency();

        // Set emergency details
        holder.typeTextView.setText(emergency.getType());
        holder.locationTextView.setText(emergency.getLocation());
        holder.descriptionTextView.setText(emergency.getDescription());

        // Set match score and reason
        holder.matchScoreTextView.setText(match.getMatchPercentage());
        holder.matchReasonTextView.setText(match.getMatchReason());

        // Set urgency with color
        holder.urgencyBadge.setText(emergency.getUrgency());
        setUrgencyColor(holder.urgencyBadge, holder.urgencyIcon, emergency.getUrgency());

        // Set match score badge color
        setMatchScoreColor(holder.matchScoreTextView, holder.matchScoreCard, match.getScore());

        // Set emergency type icon
        setEmergencyIcon(holder.typeIcon, emergency.getType());

        // Click listener to open emergency details - FIXED
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EmergencyDetailActivity.class);

            // Use the correct intent extra keys that EmergencyDetailActivity expects
            intent.putExtra("emergencyId", emergency.getId());
            intent.putExtra("emergencyType", emergency.getType());
            intent.putExtra("location", emergency.getLocation());
            intent.putExtra("description", emergency.getDescription());
            intent.putExtra("urgency", emergency.getUrgency());
            intent.putExtra("volunteers", emergency.getVolunteers());
            intent.putExtra("requiredSkills", emergency.getRequiredSkills());
            intent.putExtra("dateTime", emergency.getDateTime());

            // Optional: Add these if your EmergencyRequest class has these methods
            // Uncomment the lines below if you add these methods to EmergencyRequest

             //intent.putExtra("volunteers", emergency.getVolunteersNeeded());
             //intent.putExtra("requiredSkills", emergency.getRequiredSkills());
            // intent.putExtra("dateTime", emergency.getTimestamp());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    private void setUrgencyColor(TextView badge, ImageView icon, String urgency) {
        if (urgency == null) {
            urgency = "Medium";
        }

        int color;
        int iconRes;

        switch (urgency.toLowerCase()) {
            case "critical":
                color = Color.parseColor("#7F1D1D"); // Dark red
                iconRes = R.drawable.ic_emergency;
                break;
            case "high":
                color = Color.parseColor("#DC2626"); // Red
                iconRes = R.drawable.ic_emergency;
                break;
            case "medium":
            case "moderate":
                color = Color.parseColor("#F59E0B"); // Orange
                iconRes = R.drawable.ic_emergency;
                break;
            case "low":
                color = Color.parseColor("#10B981"); // Green
                iconRes = R.drawable.ic_emergency;
                break;
            default:
                color = Color.parseColor("#6B7280"); // Gray
                iconRes = R.drawable.ic_emergency;
        }

        badge.setTextColor(color);
        icon.setColorFilter(color);
        if (iconRes != 0) {
            icon.setImageResource(iconRes);
        }
    }

    private void setMatchScoreColor(TextView textView, CardView cardView, double score) {
        int textColor;
        int bgColor;

        if (score >= 80) {
            textColor = Color.parseColor("#047857"); // Dark green
            bgColor = Color.parseColor("#D1FAE5"); // Light green
        } else if (score >= 65) {
            textColor = Color.parseColor("#1D4ED8"); // Dark blue
            bgColor = Color.parseColor("#DBEAFE"); // Light blue
        } else if (score >= 50) {
            textColor = Color.parseColor("#7C2D12"); // Dark orange
            bgColor = Color.parseColor("#FED7AA"); // Light orange
        } else {
            textColor = Color.parseColor("#374151"); // Dark gray
            bgColor = Color.parseColor("#E5E7EB"); // Light gray
        }

        textView.setTextColor(textColor);
        cardView.setCardBackgroundColor(bgColor);
    }

    private void setEmergencyIcon(ImageView icon, String type) {
        if (type == null) return;

        int iconRes = R.drawable.ic_emergency; // default

        String typeLower = type.toLowerCase();
        if (typeLower.contains("medical") || typeLower.contains("health")) {
            iconRes = R.drawable.ic_emergency; // Use medical icon if available
        } else if (typeLower.contains("fire")) {
            iconRes = R.drawable.ic_emergency; // Use fire icon if available
        } else if (typeLower.contains("flood") || typeLower.contains("water")) {
            iconRes = R.drawable.ic_emergency; // Use flood icon if available
        }

        icon.setImageResource(iconRes);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView typeTextView, locationTextView, descriptionTextView;
        TextView urgencyBadge, matchScoreTextView, matchReasonTextView;
        ImageView typeIcon, urgencyIcon;
        CardView cardView, matchScoreCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.emergencyTypeTextView);
            locationTextView = itemView.findViewById(R.id.emergencyLocationTextView);
            descriptionTextView = itemView.findViewById(R.id.emergencyDescriptionTextView);
            urgencyBadge = itemView.findViewById(R.id.urgencyBadge);
            matchScoreTextView = itemView.findViewById(R.id.matchScoreTextView);
            matchReasonTextView = itemView.findViewById(R.id.matchReasonTextView);
            typeIcon = itemView.findViewById(R.id.typeIcon);
            urgencyIcon = itemView.findViewById(R.id.urgencyIcon);
            cardView = itemView.findViewById(R.id.emergencyCard);
            matchScoreCard = itemView.findViewById(R.id.matchScoreCard);
        }
    }
}
