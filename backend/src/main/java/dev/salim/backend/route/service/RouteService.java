package dev.salim.backend.route.service;

import dev.salim.backend.tour.api.TourRequest;

public interface RouteService {
    RouteResult resolveRoute(TourRequest request);
}
