package dev.salim.backend.route.service;

public record RouteResult(
    double distanceKm,
    int estimatedTimeMinutes,
    String routeInformation,
    String routeGeoJson
) {}
