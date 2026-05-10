package dev.salim.backend.tour.service;

import dev.salim.backend.common.exception.ResourceNotFoundException;
import dev.salim.backend.route.service.RouteResult;
import dev.salim.backend.route.service.RouteService;
import dev.salim.backend.tour.api.TourExportItem;
import dev.salim.backend.tour.api.TourExportPayload;
import dev.salim.backend.tour.api.TourLogRequest;
import dev.salim.backend.tour.api.TourLogResponse;
import dev.salim.backend.tour.api.TourMapper;
import dev.salim.backend.tour.api.TourRequest;
import dev.salim.backend.tour.api.TourResponse;
import dev.salim.backend.tour.domain.TourEntity;
import dev.salim.backend.tour.domain.TourLogEntity;
import dev.salim.backend.tour.persistence.TourLogRepository;
import dev.salim.backend.tour.persistence.TourRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TourServiceImpl implements TourService {

    private final TourRepository tourRepository;
    private final TourLogRepository logRepository;
    private final TourMapper mapper;
    private final RouteService routeService;
    private final TourInsightService tourInsightService;

    @Override
    public List<TourResponse> findAll(UUID ownerId, String search) {
        List<TourEntity> tours = tourRepository.findByOwnerId(ownerId);
        if (search != null && !search.isBlank()) {
            String needle = search.trim().toLowerCase(Locale.ROOT);
            tours = tours.stream()
                .filter(tour -> tourInsightService.searchableText(tour).contains(needle))
                .toList();
        }
        return tours.stream()
            .map(mapper::toResponse)
            .toList();
    }

    @Override
    public TourResponse findById(UUID ownerId, UUID id) {
        return mapper.toResponse(getTourOrThrow(ownerId, id));
    }

    @Override
    public TourResponse create(UUID ownerId, TourRequest request) {
        RouteResult route = routeService.resolveRoute(request);
        TourEntity entity = mapper.createEntity(ownerId, request, route);
        TourEntity saved = tourRepository.save(entity);
        log.info("Created tour {} for owner {}", saved.getId(), ownerId);
        return mapper.toResponse(saved);
    }

    @Override
    public TourResponse update(UUID ownerId, UUID id, TourRequest request) {
        TourEntity entity = getTourOrThrow(ownerId, id);
        RouteResult route = routeService.resolveRoute(request);
        mapper.updateEntity(entity, request, route);
        return mapper.toResponse(entity);
    }

    @Override
    public void delete(UUID ownerId, UUID id) {
        TourEntity entity = getTourOrThrow(ownerId, id);
        tourRepository.delete(entity);
        log.info("Deleted tour {}", id);
    }

    @Override
    public List<TourLogResponse> findLogs(UUID ownerId, UUID tourId) {
        getTourOrThrow(ownerId, tourId);
        return logRepository.findByTourId(tourId).stream()
            .map(mapper::toLogResponse)
            .toList();
    }

    @Override
    public TourLogResponse createLog(UUID ownerId, UUID tourId, TourLogRequest request) {
        TourEntity tour = getTourOrThrow(ownerId, tourId);
        TourLogEntity entity = mapper.createLog(tour, request);
        TourLogEntity saved = logRepository.save(entity);
        log.info("Created log {} for tour {}", saved.getId(), tourId);
        return mapper.toLogResponse(saved);
    }

    @Override
    public TourLogResponse updateLog(UUID ownerId, UUID tourId, UUID logId, TourLogRequest request) {
        getTourOrThrow(ownerId, tourId);
        TourLogEntity logEntity = getLogOrThrow(logId);
        ensureBelongsToTour(tourId, logEntity);
        mapper.updateLogEntity(logEntity, request);
        return mapper.toLogResponse(logEntity);
    }

    @Override
    public void deleteLog(UUID ownerId, UUID tourId, UUID logId) {
        getTourOrThrow(ownerId, tourId);
        TourLogEntity logEntity = getLogOrThrow(logId);
        ensureBelongsToTour(tourId, logEntity);
        logRepository.delete(logEntity);
        log.info("Deleted log {} for tour {}", logId, tourId);
    }

    @Override
    public TourExportPayload exportTours(UUID ownerId) {
        List<TourEntity> tours = tourRepository.findByOwnerId(ownerId);
        return new TourExportPayload(
            "tour-planner-export-v1",
            Instant.now(),
            tours.stream().map(mapper::toExportItem).toList()
        );
    }

    @Override
    public List<TourResponse> importTours(UUID ownerId, TourExportPayload payload) {
        List<TourEntity> imported = (payload.tours() == null ? List.<TourExportItem>of() : payload.tours()).stream()
            .map(item -> mapper.importEntity(ownerId, item))
            .toList();
        tourRepository.saveAll(imported);
        log.info("Imported {} tours for owner {}", imported.size(), ownerId);
        return findAll(ownerId, null);
    }

    private TourEntity getTourOrThrow(UUID ownerId, UUID id) {
        return tourRepository.findByIdAndOwnerId(id, ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("tour", id.toString()));
    }

    private TourLogEntity getLogOrThrow(UUID id) {
        return logRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("tourLog", id.toString()));
    }

    private void ensureBelongsToTour(UUID tourId, TourLogEntity logEntity) {
        UUID entityTourId = logEntity.getTour().getId();
        if (!entityTourId.equals(tourId)) {
            throw new ResourceNotFoundException("tourLog", logEntity.getId().toString());
        }
    }
}
