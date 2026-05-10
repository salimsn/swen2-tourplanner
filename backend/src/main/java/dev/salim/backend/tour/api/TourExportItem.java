package dev.salim.backend.tour.api;

import dev.salim.backend.tour.domain.TransportType;
import java.util.List;

public record TourExportItem(
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
    List<TourLogExportItem> logs
) {}
