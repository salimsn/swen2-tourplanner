package dev.salim.backend.tour.api;

import dev.salim.backend.tour.domain.Difficulty;
import java.time.Instant;
import java.util.UUID;

public record TourLogResponse(
    UUID id,
    Instant dateTime,
    String comment,
    Difficulty difficulty,
    double totalDistanceKm,
    int totalTimeMinutes,
    int rating
) {}
