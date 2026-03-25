package dev.salim.backend.tour.service;

import dev.salim.backend.tour.api.TourLogRequest;
import dev.salim.backend.tour.api.TourLogResponse;
import dev.salim.backend.tour.api.TourRequest;
import dev.salim.backend.tour.api.TourResponse;
import java.util.List;
import java.util.UUID;

public interface TourService {
    List<TourResponse> findAll(UUID ownerId);
    TourResponse findById(UUID id);
    TourResponse create(UUID ownerId, TourRequest request);
    TourResponse update(UUID id, TourRequest request);
    void delete(UUID id);

    List<TourLogResponse> findLogs(UUID tourId);
    TourLogResponse createLog(UUID tourId, TourLogRequest request);
    TourLogResponse updateLog(UUID tourId, UUID logId, TourLogRequest request);
    void deleteLog(UUID tourId, UUID logId);
}
