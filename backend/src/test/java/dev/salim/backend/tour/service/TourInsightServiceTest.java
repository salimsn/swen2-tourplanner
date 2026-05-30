package dev.salim.backend.tour.service;

import static org.assertj.core.api.Assertions.assertThat;

import dev.salim.backend.tour.domain.Difficulty;
import dev.salim.backend.tour.domain.TourEntity;
import dev.salim.backend.tour.domain.TourLogEntity;
import dev.salim.backend.tour.domain.TransportType;
import org.junit.jupiter.api.Test;

class TourInsightServiceTest {

    private final TourInsightService service = new TourInsightService();

    @Test
    void calculate_labelsNewToursAsNew() {
        TourInsights insights = service.calculate(tour(8, 60));

        assertThat(insights.popularity()).isZero();
        assertThat(insights.popularityLabel()).isEqualTo("New");
    }

    @Test
    void calculate_labelsMultipleLogsAsPopular() {
        TourEntity tour = tour(8, 60);
        tour.getLogs().add(log(tour, Difficulty.EASY, 4));
        tour.getLogs().add(log(tour, Difficulty.EASY, 5));

        TourInsights insights = service.calculate(tour);

        assertThat(insights.popularityLabel()).isEqualTo("Popular");
    }

    @Test
    void calculate_scoresShortEasyToursAsChildFriendly() {
        TourEntity tour = tour(2, 30);
        tour.getLogs().add(log(tour, Difficulty.EASY, 5));

        TourInsights insights = service.calculate(tour);

        assertThat(insights.childFriendliness()).isEqualTo("Child-friendly");
        assertThat(insights.childFriendlinessScore()).isGreaterThan(75);
    }

    @Test
    void calculate_scoresMediumCarTripsAsChildFriendly() {
        TourEntity tour = tour(55, 55, TransportType.CAR);

        TourInsights insights = service.calculate(tour);

        assertThat(insights.childFriendliness()).isEqualTo("Child-friendly");
        assertThat(insights.achievementBadge()).isEqualTo("Family drive");
    }

    @Test
    void calculate_doesNotLabelCarTripsAsEnduranceRoutes() {
        TourEntity tour = tour(120, 130, TransportType.CAR);

        TourInsights insights = service.calculate(tour);

        assertThat(insights.achievementBadge()).isNotEqualTo("Endurance route");
        assertThat(insights.achievementBadge()).isEqualTo("Scenic drive");
    }

    @Test
    void searchableText_containsComputedBadge() {
        TourEntity tour = tour(2, 45);
        tour.getLogs().add(log(tour, Difficulty.EASY, 5));

        String searchable = service.searchableText(tour);

        assertThat(searchable).contains("quick win");
    }

    private TourEntity tour(double distanceKm, int estimatedTimeMinutes) {
        return tour(distanceKm, estimatedTimeMinutes, TransportType.HIKE);
    }

    private TourEntity tour(double distanceKm, int estimatedTimeMinutes, TransportType transportType) {
        return TourEntity.builder()
            .name("Park Loop")
            .description("Easy")
            .fromLocation("Start")
            .toLocation("Finish")
            .transportType(transportType)
            .distanceKm(distanceKm)
            .estimatedTimeMinutes(estimatedTimeMinutes)
            .routeInformation("Loop")
            .imagePath("image.jpg")
            .build();
    }

    private TourLogEntity log(TourEntity tour, Difficulty difficulty, int rating) {
        return TourLogEntity.builder()
            .tour(tour)
            .difficulty(difficulty)
            .comment("Nice")
            .totalDistanceKm(2)
            .totalTimeMinutes(35)
            .rating(rating)
            .build();
    }
}
