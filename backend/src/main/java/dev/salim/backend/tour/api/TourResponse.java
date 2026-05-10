package dev.salim.backend.tour.api;

import dev.salim.backend.tour.domain.TransportType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TourResponse(
    UUID id,
    UUID ownerId,
    String name,
    String description,
    String fromLocation,
    String toLocation,
    TransportType transportType,
    double distanceKm,
    int estimatedTimeMinutes,
    String routeWaypoints,
    List<String> routeStops,
    String routeInformation,
    String routeGeoJson,
    String imagePath,
    Instant createdAt,
    Instant updatedAt,
    List<TourLogResponse> logs,
    int popularity,
    String popularityLabel,
    int childFriendlinessScore,
    String childFriendliness,
    String achievementBadge
) {}
