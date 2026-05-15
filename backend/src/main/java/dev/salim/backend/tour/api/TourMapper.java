package dev.salim.backend.tour.api;

import dev.salim.backend.route.service.RouteResult;
import dev.salim.backend.tour.domain.TourEntity;
import dev.salim.backend.tour.domain.TourLogEntity;
import dev.salim.backend.tour.service.TourInsightService;
import dev.salim.backend.tour.service.TourInsights;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TourMapper {

    private final TourInsightService tourInsightService;

    public TourResponse toResponse(TourEntity entity) {
        List<TourLogResponse> logs = entity.getLogs().stream()
            .map(this::toLogResponse)
            .toList();
        TourInsights insights = tourInsightService.calculate(entity);

        return new TourResponse(
            entity.getId(),
            entity.getOwnerId(),
            entity.getName(),
            entity.getDescription(),
            entity.getFromLocation(),
            entity.getToLocation(),
            entity.getTransportType(),
            entity.getDistanceKm(),
            entity.getEstimatedTimeMinutes(),
            entity.getRouteWaypoints(),
            parseRouteStops(entity.getRouteStops()),
            entity.getRouteInformation(),
            entity.getRouteGeoJson(),
            entity.getImagePath(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            logs,
            insights.popularity(),
            insights.popularityLabel(),
            insights.childFriendlinessScore(),
            insights.childFriendliness(),
            insights.achievementBadge()
        );
    }

    public TourLogResponse toLogResponse(TourLogEntity entity) {
        return new TourLogResponse(
            entity.getId(),
            entity.getDateTime(),
            entity.getComment(),
            entity.getDifficulty(),
            entity.getTotalDistanceKm(),
            entity.getTotalTimeMinutes(),
            entity.getRating()
        );
    }

    public void updateEntity(TourEntity entity, TourRequest request, RouteResult route) {
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setFromLocation(request.fromLocation());
        entity.setToLocation(request.toLocation());
        entity.setTransportType(request.transportType());
        entity.setDistanceKm(route.distanceKm());
        entity.setEstimatedTimeMinutes(route.estimatedTimeMinutes());
        entity.setRouteWaypoints(request.routeWaypoints());
        entity.setRouteStops(serializeRouteStops(request.routeStops()));
        entity.setRouteInformation(route.routeInformation());
        entity.setRouteGeoJson(route.routeGeoJson());
        entity.setImagePath(request.imagePath());
    }

    public void updateLogEntity(TourLogEntity entity, TourEntity tour, TourLogRequest request) {
        entity.setDateTime(request.dateTime());
        entity.setComment(request.comment());
        entity.setDifficulty(request.difficulty());
        entity.setTotalDistanceKm(tour.getDistanceKm());
        entity.setTotalTimeMinutes(request.totalTimeMinutes());
        entity.setRating(request.rating());
    }

    public TourLogEntity createLog(TourEntity tour, TourLogRequest request) {
        TourLogEntity entity = new TourLogEntity();
        entity.setTour(tour);
        updateLogEntity(entity, tour, request);
        return entity;
    }

    public TourEntity createEntity(UUID ownerId, TourRequest request, RouteResult route) {
        TourEntity entity = new TourEntity();
        entity.setOwnerId(ownerId);
        updateEntity(entity, request, route);
        return entity;
    }

    public TourExportItem toExportItem(TourEntity entity) {
        List<TourLogExportItem> logs = entity.getLogs().stream()
            .map(log -> new TourLogExportItem(
                log.getDateTime(),
                log.getComment(),
                log.getDifficulty(),
                log.getTotalDistanceKm(),
                log.getTotalTimeMinutes(),
                log.getRating()
            ))
            .toList();

        return new TourExportItem(
            entity.getName(),
            entity.getDescription(),
            entity.getFromLocation(),
            entity.getToLocation(),
            entity.getTransportType(),
            entity.getDistanceKm(),
            entity.getEstimatedTimeMinutes(),
            entity.getRouteWaypoints(),
            parseRouteStops(entity.getRouteStops()),
            entity.getRouteInformation(),
            entity.getRouteGeoJson(),
            entity.getImagePath(),
            logs
        );
    }

    public TourEntity importEntity(UUID ownerId, TourExportItem item) {
        TourEntity entity = TourEntity.builder()
            .ownerId(ownerId)
            .name(item.name())
            .description(item.description())
            .fromLocation(item.fromLocation())
            .toLocation(item.toLocation())
            .transportType(item.transportType())
            .distanceKm(item.distanceKm())
            .estimatedTimeMinutes(item.estimatedTimeMinutes())
            .routeWaypoints(item.routeWaypoints())
            .routeStops(serializeRouteStops(item.routeStops()))
            .routeInformation(item.routeInformation())
            .routeGeoJson(item.routeGeoJson())
            .imagePath(item.imagePath())
            .build();

        List<TourLogEntity> logs = item.logs() == null ? List.of() : item.logs().stream()
            .map(log -> TourLogEntity.builder()
                .tour(entity)
                .dateTime(log.dateTime())
                .comment(log.comment())
                .difficulty(log.difficulty())
                .totalDistanceKm(log.totalDistanceKm())
                .totalTimeMinutes(log.totalTimeMinutes())
                .rating(log.rating())
                .build())
            .toList();
        entity.getLogs().addAll(logs);
        return entity;
    }

    private String serializeRouteStops(List<String> routeStops) {
        if (routeStops == null) {
            return null;
        }

        String serialized = routeStops.stream()
            .map(stop -> stop == null ? "" : stop.trim())
            .filter(stop -> !stop.isBlank())
            .reduce((left, right) -> left + "\n" + right)
            .orElse("");
        return serialized.isBlank() ? null : serialized;
    }

    private List<String> parseRouteStops(String routeStops) {
        if (routeStops == null || routeStops.isBlank()) {
            return List.of();
        }

        return routeStops.lines()
            .map(String::trim)
            .filter(stop -> !stop.isBlank())
            .toList();
    }
}
