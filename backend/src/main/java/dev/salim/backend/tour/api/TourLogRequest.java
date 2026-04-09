package dev.salim.backend.tour.api;

import dev.salim.backend.tour.domain.Difficulty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record TourLogRequest(
    @NotNull Instant dateTime,
    @Size(max = 1000) String comment,
    @NotNull Difficulty difficulty,
    @Min(1) double totalDistanceKm,
    @Min(1) int totalTimeMinutes,
    @Min(1) @Max(5) int rating
) {}
