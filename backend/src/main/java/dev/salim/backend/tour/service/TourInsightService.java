package dev.salim.backend.tour.service;

import dev.salim.backend.tour.domain.Difficulty;
import dev.salim.backend.tour.domain.TourEntity;
import dev.salim.backend.tour.domain.TourLogEntity;
import org.springframework.stereotype.Service;

@Service
public class TourInsightService {

    public TourInsights calculate(TourEntity tour) {
        int logCount = tour.getLogs().size();
        int childScore = childFriendlinessScore(tour);
        return new TourInsights(
            logCount,
            popularityLabel(logCount),
            childScore,
            childFriendlinessLabel(childScore),
            achievementBadge(tour, logCount)
        );
    }

    public String searchableText(TourEntity tour) {
        TourInsights insights = calculate(tour);
        StringBuilder builder = new StringBuilder();
        append(builder, tour.getName());
        append(builder, tour.getDescription());
        append(builder, tour.getFromLocation());
        append(builder, tour.getToLocation());
        append(builder, tour.getTransportType().name());
        append(builder, tour.getRouteInformation());
        append(builder, String.valueOf(tour.getDistanceKm()));
        append(builder, String.valueOf(tour.getEstimatedTimeMinutes()));
        append(builder, tour.getRouteWaypoints());
        append(builder, tour.getRouteStops());
        append(builder, String.valueOf(insights.popularity()));
        append(builder, insights.popularityLabel());
        append(builder, String.valueOf(insights.childFriendlinessScore()));
        append(builder, insights.childFriendliness());
        append(builder, insights.achievementBadge());
        for (TourLogEntity log : tour.getLogs()) {
            append(builder, log.getComment());
            append(builder, log.getDifficulty().name());
            append(builder, String.valueOf(log.getRating()));
            append(builder, String.valueOf(log.getTotalDistanceKm()));
            append(builder, String.valueOf(log.getTotalTimeMinutes()));
        }
        return builder.toString().toLowerCase();
    }

    private int childFriendlinessScore(TourEntity tour) {
        double distancePenalty = Math.min(35, tour.getDistanceKm() * 1.2);
        double timePenalty = Math.min(30, tour.getEstimatedTimeMinutes() / 20.0);
        double difficultyPenalty = tour.getLogs().stream()
            .mapToInt(this::difficultyPenalty)
            .average()
            .orElse(8);
        int score = (int) Math.round(100 - distancePenalty - timePenalty - difficultyPenalty);
        return Math.max(0, Math.min(100, score));
    }

    private int difficultyPenalty(TourLogEntity log) {
        Difficulty difficulty = log.getDifficulty();
        int base = switch (difficulty) {
            case EASY -> 0;
            case MODERATE -> 12;
            case HARD -> 28;
        };
        int timePenalty = log.getTotalTimeMinutes() > 240 ? 8 : 0;
        int distancePenalty = log.getTotalDistanceKm() > 15 ? 8 : 0;
        return base + timePenalty + distancePenalty;
    }

    private String popularityLabel(int logCount) {
        if (logCount >= 5) {
            return "Very popular";
        }
        if (logCount >= 2) {
            return "Popular";
        }
        if (logCount == 1) {
            return "Started";
        }
        return "New";
    }

    private String childFriendlinessLabel(int score) {
        if (score >= 75) {
            return "Child-friendly";
        }
        if (score >= 50) {
            return "Family option";
        }
        if (score >= 30) {
            return "With caution";
        }
        return "Adults only";
    }

    private String achievementBadge(TourEntity tour, int logCount) {
        double averageRating = tour.getLogs().stream()
            .mapToInt(TourLogEntity::getRating)
            .average()
            .orElse(0);
        if (logCount >= 5 && averageRating >= 4.5) {
            return "Community favorite";
        }
        if (tour.getDistanceKm() >= 50) {
            return "Endurance route";
        }
        if (tour.getEstimatedTimeMinutes() <= 90 && averageRating >= 4) {
            return "Quick win";
        }
        if (childFriendlinessScore(tour) >= 75) {
            return "Family pick";
        }
        return "Explorer";
    }

    private void append(StringBuilder builder, String value) {
        if (value != null && !value.isBlank()) {
            builder.append(value).append(' ');
        }
    }
}
