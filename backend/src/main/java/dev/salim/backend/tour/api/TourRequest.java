package dev.salim.backend.tour.api;

import dev.salim.backend.tour.domain.TransportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TourRequest(
    @NotBlank @Size(min = 3, max = 120) String name,
    @Size(max = 1500) String description,
    @NotBlank String fromLocation,
    @NotBlank String toLocation,
    @NotNull TransportType transportType,
    Double distanceKm,
    Integer estimatedTimeMinutes,
    @Size(max = 2000) String routeWaypoints,
    @Size(max = 8) List<@Size(max = 180) String> routeStops,
    @Size(max = 500) String routeInformation,
    @Size(max = 512) String imagePath
) {}
