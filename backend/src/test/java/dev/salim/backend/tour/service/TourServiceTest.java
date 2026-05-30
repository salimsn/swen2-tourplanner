package dev.salim.backend.tour.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.salim.backend.common.exception.ResourceNotFoundException;
import dev.salim.backend.tour.api.TourExportPayload;
import dev.salim.backend.tour.api.TourLogRequest;
import dev.salim.backend.tour.api.TourLogResponse;
import dev.salim.backend.tour.api.TourRequest;
import dev.salim.backend.tour.api.TourResponse;
import dev.salim.backend.tour.domain.Difficulty;
import dev.salim.backend.tour.domain.TransportType;
import dev.salim.backend.tour.persistence.TourLogRepository;
import dev.salim.backend.tour.persistence.TourRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TourServiceTest {

    private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID OTHER_OWNER = UUID.fromString("00000000-0000-0000-0000-000000000202");

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
    void createTour_persistsTourWithComputedFallbackRoute() {
        TourResponse response = tourService.create(OWNER, defaultTourRequest());

        assertThat(response.id()).isNotNull();
        assertThat(response.ownerId()).isEqualTo(OWNER);
        assertThat(response.routeGeoJson()).contains("LineString");
        assertThat(tourRepository.count()).isEqualTo(1);
    }

    @Test
    void createTour_calculatesDistanceAndTimeWhenRequestDoesNotContainManualValues() {
        TourResponse response = tourService.create(OWNER, new TourRequest(
            "Auto Calculated Tour",
            "Distance and time come from route data",
            "Tulln",
            "Vienna",
            TransportType.BIKE,
            null,
            null,
            "16.0549,48.3316;16.3738,48.2082",
            List.of(),
            "",
            "image.jpg"
        ));

        assertThat(response.distanceKm()).isGreaterThan(1);
        assertThat(response.estimatedTimeMinutes()).isGreaterThan(1);
    }

    @Test
    void findAll_returnsOnlyToursForOwner() {
        tourService.create(OWNER, defaultTourRequest());
        tourService.create(OTHER_OWNER, alternateTourRequest());

        List<TourResponse> tours = tourService.findAll(OWNER, null);

        assertThat(tours).hasSize(1);
        assertThat(tours.getFirst().ownerId()).isEqualTo(OWNER);
    }

    @Test
    void findById_rejectsTourFromOtherOwner() {
        TourResponse otherTour = tourService.create(OTHER_OWNER, defaultTourRequest());

        assertThatThrownBy(() -> tourService.findById(OWNER, otherTour.id()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateTour_changesAttributes() {
        TourResponse created = tourService.create(OWNER, defaultTourRequest());

        TourResponse updated = tourService.update(OWNER, created.id(), alternateTourRequest());

        assertThat(updated.name()).isEqualTo("Updated Ride");
        assertThat(updated.transportType()).isEqualTo(TransportType.CAR);
        assertThat(updated.routeInformation()).isEqualTo("updated route");
    }

    @Test
    void deleteTour_removesTourAndCascadeLogs() {
        TourResponse created = tourService.create(OWNER, defaultTourRequest());
        tourService.createLog(OWNER, created.id(), defaultLogRequest("Great family tour", Difficulty.EASY, 5));

        tourService.delete(OWNER, created.id());

        assertThat(tourRepository.count()).isZero();
        assertThat(logRepository.count()).isZero();
    }

    @Test
    void createLog_addsLogToTour() {
        TourResponse created = tourService.create(OWNER, defaultTourRequest());

        TourLogResponse log = tourService.createLog(OWNER, created.id(), defaultLogRequest("Sunny day", Difficulty.EASY, 5));

        assertThat(logRepository.findAll()).hasSize(1);
        assertThat(tourService.findLogs(OWNER, created.id())).hasSize(1);
        assertThat(log.totalDistanceKm()).isEqualTo(created.distanceKm());
    }

    @Test
    void updateLog_changesLogValues() {
        TourResponse created = tourService.create(OWNER, defaultTourRequest());
        TourLogResponse log = tourService.createLog(OWNER, created.id(), defaultLogRequest("Old", Difficulty.EASY, 3));

        TourLogResponse updated = tourService.updateLog(
            OWNER,
            created.id(),
            log.id(),
            defaultLogRequest("Updated log", Difficulty.HARD, 4)
        );

        assertThat(updated.comment()).isEqualTo("Updated log");
        assertThat(updated.difficulty()).isEqualTo(Difficulty.HARD);
        assertThat(updated.rating()).isEqualTo(4);
    }

    @Test
    void deleteLog_removesOnlySelectedLog() {
        TourResponse created = tourService.create(OWNER, defaultTourRequest());
        TourLogResponse first = tourService.createLog(OWNER, created.id(), defaultLogRequest("First", Difficulty.EASY, 4));
        tourService.createLog(OWNER, created.id(), defaultLogRequest("Second", Difficulty.MODERATE, 5));

        tourService.deleteLog(OWNER, created.id(), first.id());

        assertThat(tourService.findLogs(OWNER, created.id())).hasSize(1);
    }

    @Test
    void updateLog_rejectsLogFromAnotherTour() {
        TourResponse firstTour = tourService.create(OWNER, defaultTourRequest());
        TourResponse secondTour = tourService.create(OWNER, alternateTourRequest());
        TourLogResponse log = tourService.createLog(OWNER, firstTour.id(), defaultLogRequest("Wrong", Difficulty.EASY, 4));

        assertThatThrownBy(() -> tourService.updateLog(OWNER, secondTour.id(), log.id(), defaultLogRequest("Nope", Difficulty.HARD, 1)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void searchFindsTourName() {
        tourService.create(OWNER, defaultTourRequest());

        List<TourResponse> results = tourService.findAll(OWNER, "Default");

        assertThat(results).hasSize(1);
    }

    @Test
    void searchFindsTourLogComment() {
        TourResponse tour = tourService.create(OWNER, defaultTourRequest());
        tourService.createLog(OWNER, tour.id(), defaultLogRequest("Waterfall stop", Difficulty.EASY, 5));

        List<TourResponse> results = tourService.findAll(OWNER, "waterfall");

        assertThat(results).extracting(TourResponse::id).containsExactly(tour.id());
    }

    @Test
    void searchFindsRouteStops() {
        TourResponse tour = tourService.create(OWNER, defaultTourRequest());

        List<TourResponse> results = tourService.findAll(OWNER, "durnstein");

        assertThat(results).extracting(TourResponse::id).containsExactly(tour.id());
        assertThat(tour.routeStops()).contains("Durnstein", "Spitz");
    }

    @Test
    void searchFindsComputedPopularityLabel() {
        TourResponse tour = tourService.create(OWNER, defaultTourRequest());
        tourService.createLog(OWNER, tour.id(), defaultLogRequest("One", Difficulty.EASY, 4));
        tourService.createLog(OWNER, tour.id(), defaultLogRequest("Two", Difficulty.EASY, 5));

        List<TourResponse> results = tourService.findAll(OWNER, "popular");

        assertThat(results).hasSize(1);
    }

    @Test
    void searchFindsComputedChildFriendliness() {
        TourResponse tour = tourService.create(OWNER, new TourRequest(
            "Short Park Loop",
            "Easy route",
            "Park",
            "Playground",
            TransportType.HIKE,
            2.0,
            30,
            "16.30,48.20;16.31,48.21",
            List.of("Small Park, Vienna"),
            "Small loop",
            "image.jpg"
        ));

        List<TourResponse> results = tourService.findAll(OWNER, "child-friendly");

        assertThat(results).extracting(TourResponse::id).contains(tour.id());
    }

    @Test
    void responseContainsComputedInsights() {
        TourResponse tour = tourService.create(OWNER, defaultTourRequest());

        assertThat(tour.popularity()).isZero();
        assertThat(tour.childFriendlinessScore()).isBetween(0, 100);
        assertThat(tour.achievementBadge()).isNotBlank();
    }

    @Test
    void exportTours_containsOnlyOwnedToursAndLogs() {
        TourResponse owned = tourService.create(OWNER, defaultTourRequest());
        tourService.createLog(OWNER, owned.id(), defaultLogRequest("Export me", Difficulty.EASY, 5));
        tourService.create(OTHER_OWNER, alternateTourRequest());

        TourExportPayload export = tourService.exportTours(OWNER);

        assertThat(export.tours()).hasSize(1);
        assertThat(export.tours().getFirst().logs()).hasSize(1);
        assertThat(export.tours().getFirst().name()).isEqualTo("Default Tour");
    }

    @Test
    void importTours_createsToursAndLogsForOwner() {
        TourResponse original = tourService.create(OWNER, defaultTourRequest());
        tourService.createLog(OWNER, original.id(), defaultLogRequest("Import me", Difficulty.MODERATE, 4));
        TourExportPayload export = tourService.exportTours(OWNER);
        tourService.delete(OWNER, original.id());

        List<TourResponse> imported = tourService.importTours(OWNER, export);

        assertThat(imported).hasSize(1);
        assertThat(imported.getFirst().logs()).hasSize(1);
        assertThat(imported.getFirst().ownerId()).isEqualTo(OWNER);
    }

    @Test
    void importTours_acceptsEmptyTourList() {
        List<TourResponse> imported = tourService.importTours(OWNER, new TourExportPayload("tour-planner-export-v1", Instant.now(), List.of()));

        assertThat(imported).isEmpty();
    }

    private TourRequest defaultTourRequest() {
        return new TourRequest(
            "Default Tour",
            "A scenic ride through vineyards",
            "Krems",
            "Melk",
            TransportType.BIKE,
            25.0,
            120,
            "16.0549,48.3316;16.1250,48.3150;16.3738,48.2082",
            List.of("Durnstein", "Spitz"),
            "Danube route",
            "image.jpg"
        );
    }

    private TourRequest alternateTourRequest() {
        return new TourRequest(
            "Updated Ride",
            "Updated description",
            "Vienna",
            "Graz",
            TransportType.CAR,
            200.0,
            120,
            "16.3738,48.2082;15.4395,47.0707",
            List.of("Wien Hauptbahnhof"),
            "updated route",
            "image.png"
        );
    }

    private TourLogRequest defaultLogRequest(String comment, Difficulty difficulty, int rating) {
        return new TourLogRequest(
            Instant.now(),
            comment,
            difficulty,
            difficulty == Difficulty.EASY ? 45 : 280,
            rating
        );
    }
}
