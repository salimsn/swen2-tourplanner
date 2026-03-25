package dev.salim.backend.tour.service;

import static org.assertj.core.api.Assertions.assertThat;

import dev.salim.backend.tour.api.TourLogRequest;
import dev.salim.backend.tour.api.TourRequest;
import dev.salim.backend.tour.api.TourResponse;
import dev.salim.backend.tour.domain.Difficulty;
import dev.salim.backend.tour.domain.TransportType;
import dev.salim.backend.tour.persistence.TourLogRepository;
import dev.salim.backend.tour.persistence.TourRepository;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TourServiceTest {

    @Autowired
    private TourService tourService;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourLogRepository logRepository;

    @AfterEach
    void cleanUp() {
        logRepository.deleteAll();
        tourRepository.deleteAll();
    }

    @Test
    void createTour_persistsTour() {
        TourResponse response = tourService.create(null, defaultTourRequest());
        assertThat(response.id()).isNotNull();
        assertThat(tourRepository.count()).isEqualTo(1);
    }

    @Test
    void updateTour_changesAttributes() {
        TourResponse created = tourService.create(null, defaultTourRequest());

        TourRequest update = new TourRequest(
            "Updated Ride",
            "Updated description",
            "Vienna",
            "Graz",
            TransportType.TRAIN,
            200,
            120,
            "updated route",
            "image.png"
        );

        TourResponse updated = tourService.update(created.id(), update);
        assertThat(updated.name()).isEqualTo("Updated Ride");
        assertThat(updated.transportType()).isEqualTo(TransportType.TRAIN);
    }

    @Test
    void createLog_addsLogToTour() {
        TourResponse created = tourService.create(null, defaultTourRequest());
        TourLogRequest logRequest = new TourLogRequest(
            Instant.now(),
            "Sunny day",
            Difficulty.EASY,
            30,
            150,
            5
        );

        tourService.createLog(created.id(), logRequest);

        assertThat(logRepository.findAll()).hasSize(1);
        assertThat(tourService.findLogs(created.id())).hasSize(1);
    }

    private TourRequest defaultTourRequest() {
        return new TourRequest(
            "Default Tour",
            "A scenic ride through vineyards",
            "Krems",
            "Melk",
            TransportType.BIKE,
            25,
            120,
            "Danube route",
            "image.jpg"
        );
    }
}
