package dev.salim.backend.tour.api;

import dev.salim.backend.tour.service.TourService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    public List<TourResponse> findAll(@RequestParam(value = "ownerId", required = false) UUID ownerId) {
        return tourService.findAll(ownerId);
    }

    @GetMapping("/{id}")
    public TourResponse findById(@PathVariable UUID id) {
        return tourService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TourResponse create(
        @RequestParam(value = "ownerId", required = false) UUID ownerId,
        @Valid @RequestBody TourRequest request
    ) {
        return tourService.create(ownerId, request);
    }

    @PutMapping("/{id}")
    public TourResponse update(@PathVariable UUID id, @Valid @RequestBody TourRequest request) {
        return tourService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        tourService.delete(id);
    }

    @GetMapping("/{id}/logs")
    public List<TourLogResponse> findLogs(@PathVariable UUID id) {
        return tourService.findLogs(id);
    }

    @PostMapping("/{id}/logs")
    @ResponseStatus(HttpStatus.CREATED)
    public TourLogResponse createLog(@PathVariable UUID id, @Valid @RequestBody TourLogRequest request) {
        return tourService.createLog(id, request);
    }

    @PutMapping("/{tourId}/logs/{logId}")
    public TourLogResponse updateLog(
        @PathVariable UUID tourId,
        @PathVariable UUID logId,
        @Valid @RequestBody TourLogRequest request
    ) {
        return tourService.updateLog(tourId, logId, request);
    }

    @DeleteMapping("/{tourId}/logs/{logId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLog(@PathVariable UUID tourId, @PathVariable UUID logId) {
        tourService.deleteLog(tourId, logId);
    }
}
