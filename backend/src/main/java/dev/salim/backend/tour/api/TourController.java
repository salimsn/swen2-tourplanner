package dev.salim.backend.tour.api;

import dev.salim.backend.tour.service.TourService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    @GetMapping
    public List<TourResponse> findAll(
        @RequestHeader("X-User-Id") UUID ownerId,
        @RequestParam(value = "search", required = false) String search
    ) {
        return tourService.findAll(ownerId, search);
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public TourExportPayload exportTours(@RequestHeader("X-User-Id") UUID ownerId) {
        return tourService.exportTours(ownerId);
    }

    @PostMapping("/import")
    public List<TourResponse> importTours(
        @RequestHeader("X-User-Id") UUID ownerId,
        @RequestBody TourExportPayload payload
    ) {
        return tourService.importTours(ownerId, payload);
    }

    @GetMapping("/{id}")
    public TourResponse findById(@RequestHeader("X-User-Id") UUID ownerId, @PathVariable UUID id) {
        return tourService.findById(ownerId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TourResponse create(
        @RequestHeader("X-User-Id") UUID ownerId,
        @Valid @RequestBody TourRequest request
    ) {
        return tourService.create(ownerId, request);
    }

    @PutMapping("/{id}")
    public TourResponse update(
        @RequestHeader("X-User-Id") UUID ownerId,
        @PathVariable UUID id,
        @Valid @RequestBody TourRequest request
    ) {
        return tourService.update(ownerId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-User-Id") UUID ownerId, @PathVariable UUID id) {
        tourService.delete(ownerId, id);
    }

    @GetMapping("/{id}/logs")
    public List<TourLogResponse> findLogs(@RequestHeader("X-User-Id") UUID ownerId, @PathVariable UUID id) {
        return tourService.findLogs(ownerId, id);
    }

    @PostMapping("/{id}/logs")
    @ResponseStatus(HttpStatus.CREATED)
    public TourLogResponse createLog(
        @RequestHeader("X-User-Id") UUID ownerId,
        @PathVariable UUID id,
        @Valid @RequestBody TourLogRequest request
    ) {
        return tourService.createLog(ownerId, id, request);
    }

    @PutMapping("/{tourId}/logs/{logId}")
    public TourLogResponse updateLog(
        @RequestHeader("X-User-Id") UUID ownerId,
        @PathVariable UUID tourId,
        @PathVariable UUID logId,
        @Valid @RequestBody TourLogRequest request
    ) {
        return tourService.updateLog(ownerId, tourId, logId, request);
    }

    @DeleteMapping("/{tourId}/logs/{logId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLog(
        @RequestHeader("X-User-Id") UUID ownerId,
        @PathVariable UUID tourId,
        @PathVariable UUID logId
    ) {
        tourService.deleteLog(ownerId, tourId, logId);
    }
}
