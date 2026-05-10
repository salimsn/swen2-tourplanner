package dev.salim.backend.tour.service;

import dev.salim.backend.tour.api.TourLogRequest;
import dev.salim.backend.tour.api.TourLogResponse;
import dev.salim.backend.tour.api.TourExportPayload;
import dev.salim.backend.tour.api.TourRequest;
import dev.salim.backend.tour.api.TourResponse;
import java.util.List;
import java.util.UUID;

public interface TourService {
    List<TourResponse> findAll(UUID ownerId, String search);
    TourResponse findById(UUID ownerId, UUID id);
    TourResponse create(UUID ownerId, TourRequest request);
    TourResponse update(UUID ownerId, UUID id, TourRequest request);
    void delete(UUID ownerId, UUID id);

    List<TourLogResponse> findLogs(UUID ownerId, UUID tourId);
    TourLogResponse createLog(UUID ownerId, UUID tourId, TourLogRequest request);
    TourLogResponse updateLog(UUID ownerId, UUID tourId, UUID logId, TourLogRequest request);
    void deleteLog(UUID ownerId, UUID tourId, UUID logId);

    TourExportPayload exportTours(UUID ownerId);
    List<TourResponse> importTours(UUID ownerId, TourExportPayload payload);
}
