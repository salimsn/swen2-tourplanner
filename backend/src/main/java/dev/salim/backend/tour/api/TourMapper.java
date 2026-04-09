package dev.salim.backend.tour.api;

import dev.salim.backend.tour.domain.TourEntity;
import dev.salim.backend.tour.domain.TourLogEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TourMapper {

    public TourResponse toResponse(TourEntity entity) {
        List<TourLogResponse> logs = entity.getLogs().stream()
            .map(this::toLogResponse)
            .toList();

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
            entity.getRouteInformation(),
            entity.getImagePath(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            logs
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

    public void updateEntity(TourEntity entity, TourRequest request) {
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setFromLocation(request.fromLocation());
        entity.setToLocation(request.toLocation());
        entity.setTransportType(request.transportType());
        entity.setDistanceKm(request.distanceKm());
        entity.setEstimatedTimeMinutes(request.estimatedTimeMinutes());
        entity.setRouteInformation(request.routeInformation());
        entity.setImagePath(request.imagePath());
    }

    public void updateLogEntity(TourLogEntity entity, TourLogRequest request) {
        entity.setDateTime(request.dateTime());
        entity.setComment(request.comment());
        entity.setDifficulty(request.difficulty());
        entity.setTotalDistanceKm(request.totalDistanceKm());
        entity.setTotalTimeMinutes(request.totalTimeMinutes());
        entity.setRating(request.rating());
    }

    public TourLogEntity createLog(TourEntity tour, TourLogRequest request) {
        TourLogEntity entity = new TourLogEntity();
        entity.setTour(tour);
        updateLogEntity(entity, request);
        return entity;
    }

    public TourEntity createEntity(UUID ownerId, TourRequest request) {
        TourEntity entity = new TourEntity();
        entity.setOwnerId(ownerId);
        updateEntity(entity, request);
        return entity;
    }
}
