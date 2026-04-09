package dev.salim.backend.tour.service;

import dev.salim.backend.common.exception.ResourceNotFoundException;
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
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TourServiceImpl implements TourService {

    private static final UUID DEFAULT_OWNER = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final TourRepository tourRepository;
    private final TourLogRepository logRepository;
    private final TourMapper mapper;

    @Override
    public List<TourResponse> findAll(UUID ownerId) {
        List<TourEntity> tours = ownerId == null
            ? tourRepository.findAll()
            : tourRepository.findByOwnerId(ownerId);
        return tours.stream().map(mapper::toResponse).toList();
    }

    @Override
    public TourResponse findById(UUID id) {
        return mapper.toResponse(getTourOrThrow(id));
    }

    @Override
    public TourResponse create(UUID ownerId, TourRequest request) {
        UUID resolvedOwner = ownerId == null ? DEFAULT_OWNER : ownerId;
        TourEntity entity = mapper.createEntity(resolvedOwner, request);
        TourEntity saved = tourRepository.save(entity);
        log.info("Created tour {} for owner {}", saved.getId(), resolvedOwner);
        return mapper.toResponse(saved);
    }

    @Override
    public TourResponse update(UUID id, TourRequest request) {
        TourEntity entity = getTourOrThrow(id);
        mapper.updateEntity(entity, request);
        return mapper.toResponse(entity);
    }

    @Override
    public void delete(UUID id) {
        TourEntity entity = getTourOrThrow(id);
        tourRepository.delete(entity);
        log.info("Deleted tour {}", id);
    }

    @Override
    public List<TourLogResponse> findLogs(UUID tourId) {
        getTourOrThrow(tourId);
        return logRepository.findByTourId(tourId).stream()
            .map(mapper::toLogResponse)
            .toList();
    }

    @Override
    public TourLogResponse createLog(UUID tourId, TourLogRequest request) {
        TourEntity tour = getTourOrThrow(tourId);
        TourLogEntity entity = mapper.createLog(tour, request);
        TourLogEntity saved = logRepository.save(entity);
        log.info("Created log {} for tour {}", saved.getId(), tourId);
        return mapper.toLogResponse(saved);
    }

    @Override
    public TourLogResponse updateLog(UUID tourId, UUID logId, TourLogRequest request) {
        TourLogEntity logEntity = getLogOrThrow(logId);
        ensureBelongsToTour(tourId, logEntity);
        mapper.updateLogEntity(logEntity, request);
        return mapper.toLogResponse(logEntity);
    }

    @Override
    public void deleteLog(UUID tourId, UUID logId) {
        TourLogEntity logEntity = getLogOrThrow(logId);
        ensureBelongsToTour(tourId, logEntity);
        logRepository.delete(logEntity);
        log.info("Deleted log {} for tour {}", logId, tourId);
    }

    private TourEntity getTourOrThrow(UUID id) {
        return tourRepository.findById(id)
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
