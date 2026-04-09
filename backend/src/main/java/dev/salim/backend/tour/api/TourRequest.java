package dev.salim.backend.tour.api;

import dev.salim.backend.tour.domain.TransportType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TourRequest(
    @NotBlank @Size(min = 3, max = 120) String name,
    @Size(max = 1500) String description,
    @NotBlank String fromLocation,
    @NotBlank String toLocation,
    @NotNull TransportType transportType,
    @Min(1) double distanceKm,
    @Min(1) int estimatedTimeMinutes,
    @Size(max = 500) String routeInformation,
    @Size(max = 512) String imagePath
) {}
