package dev.salim.backend.tour.config;

import dev.salim.backend.tour.domain.Difficulty;
import dev.salim.backend.tour.domain.TourEntity;
import dev.salim.backend.tour.domain.TourLogEntity;
import dev.salim.backend.tour.domain.TransportType;
import dev.salim.backend.tour.persistence.TourRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class TourDataInitializer implements CommandLineRunner {

    private final TourRepository tourRepository;

    @Override
    public void run(String... args) {
        if (tourRepository.count() > 0) {
            return;
        }

        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000001");

        TourEntity alpineTrail = TourEntity.builder()
            .ownerId(owner)
            .name("Alpine Ridge Trail")
            .description("Full day hike across ridges with stunning views over the valley.")
            .fromLocation("Innsbruck")
            .toLocation("Seefeld")
            .transportType(TransportType.HIKE)
            .distanceKm(18.5)
            .estimatedTimeMinutes(420)
            .routeInformation("Alpine ridge loop with hut stop")
            .imagePath("assets/tours/alpine-ridge.jpg")
            .build();

        TourLogEntity alpineLog = TourLogEntity.builder()
            .tour(alpineTrail)
            .dateTime(Instant.now().minus(3, ChronoUnit.DAYS))
            .comment("Snowy conditions on the last ridge section.")
            .difficulty(Difficulty.HARD)
            .totalDistanceKm(18.5)
            .totalTimeMinutes(440)
            .rating(4)
            .build();
        alpineTrail.getLogs().add(alpineLog);

        TourEntity danubeRide = TourEntity.builder()
            .ownerId(owner)
            .name("Danube Riverside Ride")
            .description("Relaxed bike ride along the Danube cycling path.")
            .fromLocation("Tulln")
            .toLocation("Vienna")
            .transportType(TransportType.BIKE)
            .distanceKm(34.2)
            .estimatedTimeMinutes(180)
            .routeInformation("EV6 along the Danube")
            .imagePath("assets/tours/danube-ride.jpg")
            .build();

        TourLogEntity danubeLog = TourLogEntity.builder()
            .tour(danubeRide)
            .dateTime(Instant.now().minus(10, ChronoUnit.DAYS))
            .comment("Great weather and plenty of coffee stops.")
            .difficulty(Difficulty.EASY)
            .totalDistanceKm(34.2)
            .totalTimeMinutes(190)
            .rating(5)
            .build();
        danubeRide.getLogs().add(danubeLog);

        tourRepository.saveAll(List.of(alpineTrail, danubeRide));
        log.info("Seeded initial tours for owner {}", owner);
    }
}
