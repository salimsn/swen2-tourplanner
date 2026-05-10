package dev.salim.backend.tour.api;

import dev.salim.backend.tour.domain.Difficulty;
import java.time.Instant;

public record TourLogExportItem(
    Instant dateTime,
    String comment,
    Difficulty difficulty,
    double totalDistanceKm,
    int totalTimeMinutes,
    int rating
) {}
