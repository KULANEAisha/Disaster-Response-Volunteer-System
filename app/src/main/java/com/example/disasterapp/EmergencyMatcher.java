package com.example.disasterapp;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmergencyMatcher {
    private static final String TAG = "EmergencyMatcher";

    // Scoring weights
    private static final double SKILL_MATCH_WEIGHT = 40.0;
    private static final double URGENCY_WEIGHT = 35.0;
    private static final double DISTANCE_WEIGHT = 25.0;

    /**
     * Match a volunteer to emergencies and return sorted list by match score
     */
    public static List<EmergencyMatch> matchVolunteerToEmergencies(
            Volunteer volunteer,
            List<EmergencyRequest> emergencies) {

        List<EmergencyMatch> matches = new ArrayList<>();

        for (EmergencyRequest emergency : emergencies) {
            double score = calculateMatchScore(volunteer, emergency);
            matches.add(new EmergencyMatch(emergency, score));
        }

        // Sort by score (highest first)
        Collections.sort(matches, (m1, m2) -> Double.compare(m2.getScore(), m1.getScore()));

        return matches;
    }

    /**
     * Calculate match score between volunteer and emergency (0-100)
     */
    private static double calculateMatchScore(Volunteer volunteer, EmergencyRequest emergency) {
        double skillScore = calculateSkillScore(volunteer, emergency);
        double urgencyScore = calculateUrgencyScore(emergency);
        double distanceScore = calculateDistanceScore(volunteer, emergency);

        // Weighted total score
        double totalScore = (skillScore * SKILL_MATCH_WEIGHT +
                urgencyScore * URGENCY_WEIGHT +
                distanceScore * DISTANCE_WEIGHT) / 100.0;

        Log.d(TAG, String.format("Emergency: %s | Skills: %.1f | Urgency: %.1f | Distance: %.1f | Total: %.1f",
                emergency.getType(), skillScore, urgencyScore, distanceScore, totalScore));

        return totalScore;
    }

    /**
     * Calculate skill match score (0-100)
     */
    private static double calculateSkillScore(Volunteer volunteer, EmergencyRequest emergency) {
        List<String> volunteerSkills = volunteer.getSkills();
        String emergencyType = emergency.getType();

        if (emergencyType == null || emergencyType.isEmpty()) {
            return 50.0; // Neutral score
        }

        if (volunteerSkills == null || volunteerSkills.isEmpty()) {
            return 30.0; // Low score but not zero - anyone can help
        }

        // Map emergency types to relevant skills
        double maxScore = 0.0;

        for (String skill : volunteerSkills) {
            double score = getSkillRelevanceScore(skill.toLowerCase(), emergencyType.toLowerCase());
            if (score > maxScore) {
                maxScore = score;
            }
        }

        return maxScore;
    }

    /**
     * Get relevance score for a skill against emergency type
     */
    private static double getSkillRelevanceScore(String skill, String emergencyType) {
        // Medical emergencies
        if (emergencyType.contains("medical") || emergencyType.contains("health") ||
                emergencyType.contains("injury") || emergencyType.contains("accident")) {
            if (skill.contains("medical") || skill.contains("first aid") ||
                    skill.contains("nurse") || skill.contains("doctor") ||
                    skill.contains("health")) {
                return 100.0;
            }
            if (skill.contains("cpr") || skill.contains("emergency")) {
                return 90.0;
            }
        }

        // Fire emergencies
        if (emergencyType.contains("fire")) {
            if (skill.contains("fire") || skill.contains("firefight")) {
                return 100.0;
            }
            if (skill.contains("rescue") || skill.contains("emergency")) {
                return 70.0;
            }
        }

        // Flood emergencies
        if (emergencyType.contains("flood") || emergencyType.contains("water")) {
            if (skill.contains("water") || skill.contains("swim") ||
                    skill.contains("rescue") || skill.contains("boat")) {
                return 100.0;
            }
        }

        // Search and rescue
        if (emergencyType.contains("search") || emergencyType.contains("rescue") ||
                emergencyType.contains("missing")) {
            if (skill.contains("search") || skill.contains("rescue")) {
                return 100.0;
            }
        }

        // Earthquake/Building collapse
        if (emergencyType.contains("earthquake") || emergencyType.contains("building") ||
                emergencyType.contains("collapse")) {
            if (skill.contains("rescue") || skill.contains("construction") ||
                    skill.contains("engineer")) {
                return 100.0;
            }
        }

        // Food/Shelter assistance
        if (emergencyType.contains("food") || emergencyType.contains("shelter") ||
                emergencyType.contains("displaced")) {
            if (skill.contains("logistics") || skill.contains("distribution") ||
                    skill.contains("coordination")) {
                return 90.0;
            }
        }

        // General emergency response skills
        if (skill.contains("emergency") || skill.contains("response") ||
                skill.contains("first aid")) {
            return 70.0;
        }

        // Communication/coordination
        if (skill.contains("communication") || skill.contains("coordination")) {
            return 60.0;
        }

        // Basic helping
        if (skill.contains("volunteer") || skill.contains("community")) {
            return 50.0;
        }

        return 30.0; // Base score - everyone can help somehow
    }

    /**
     * Calculate urgency score (0-100)
     */
    private static double calculateUrgencyScore(EmergencyRequest emergency) {
        String urgency = emergency.getUrgency();

        if (urgency == null) return 50.0;

        switch (urgency.toLowerCase()) {
            case "critical":
                return 100.0;
            case "high":
                return 85.0;
            case "medium":
            case "moderate":
                return 60.0;
            case "low":
                return 35.0;
            default:
                return 50.0;
        }
    }

    /**
     * Calculate distance score (0-100)
     * Note: This is simplified. In production, use actual GPS coordinates
     */
    private static double calculateDistanceScore(Volunteer volunteer, EmergencyRequest emergency) {
        String volunteerArea = volunteer.getServiceArea();
        String emergencyLocation = emergency.getLocation();

        if (volunteerArea == null || emergencyLocation == null) {
            return 50.0; // Neutral if location data missing
        }

        // Simple string matching for location
        // In production, use GPS coordinates and actual distance calculation
        volunteerArea = volunteerArea.toLowerCase();
        emergencyLocation = emergencyLocation.toLowerCase();

        // Exact match
        if (volunteerArea.equals(emergencyLocation)) {
            return 100.0;
        }

        // Contains match (e.g., "Nairobi CBD" contains "Nairobi")
        if (volunteerArea.contains(emergencyLocation) || emergencyLocation.contains(volunteerArea)) {
            return 80.0;
        }

        // Check for common area keywords
        String[] volunteerWords = volunteerArea.split("\\s+");
        String[] emergencyWords = emergencyLocation.split("\\s+");

        for (String vWord : volunteerWords) {
            for (String eWord : emergencyWords) {
                if (vWord.equals(eWord) && vWord.length() > 3) { // Ignore short words
                    return 65.0;
                }
            }
        }

        // Different locations
        return 40.0;
    }

    /**
     * Filter emergencies by minimum match score
     */
    public static List<EmergencyMatch> filterByMinimumScore(
            List<EmergencyMatch> matches, double minScore) {
        List<EmergencyMatch> filtered = new ArrayList<>();
        for (EmergencyMatch match : matches) {
            if (match.getScore() >= minScore) {
                filtered.add(match);
            }
        }
        return filtered;
    }

    /**
     * Get the best match (highest score)
     */
    public static EmergencyMatch getBestMatch(List<EmergencyMatch> matches) {
        if (matches == null || matches.isEmpty()) {
            return null;
        }
        return matches.get(0); // Already sorted
    }

    /**
     * Inner class to hold emergency with its match score
     */
    public static class EmergencyMatch {
        private EmergencyRequest emergency;
        private double score;
        private String matchReason;

        public EmergencyMatch(EmergencyRequest emergency, double score) {
            this.emergency = emergency;
            this.score = score;
            this.matchReason = generateMatchReason(score);
        }

        private String generateMatchReason(double score) {
            if (score >= 80) {
                return "Excellent match for your skills!";
            } else if (score >= 65) {
                return "Great match - you can help here";
            } else if (score >= 50) {
                return "Good match - your skills are needed";
            } else if (score >= 35) {
                return "Moderate match - general help needed";
            } else {
                return "You can still help make a difference";
            }
        }

        public EmergencyRequest getEmergency() {
            return emergency;
        }

        public double getScore() {
            return score;
        }

        public String getMatchReason() {
            return matchReason;
        }

        public String getMatchPercentage() {
            return String.format("%.0f%% match", score);
        }
    }
}