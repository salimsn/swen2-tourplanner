package dev.salim.backend.tour.config;

import dev.salim.backend.tour.domain.Difficulty;
import dev.salim.backend.tour.domain.TourEntity;
import dev.salim.backend.tour.domain.TourLogEntity;
import dev.salim.backend.tour.domain.TransportType;
import dev.salim.backend.tour.persistence.TourRepository;
import dev.salim.backend.user.domain.UserEntity;
import dev.salim.backend.user.persistence.UserRepository;
import dev.salim.backend.user.service.PasswordHasher;
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
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Override
    public void run(String... args) {
        UserEntity demoUser = ensureDemoUser();
        UUID owner = demoUser.getId();

        if (tourRepository.count() > 0) {
            return;
        }

        TourEntity alpineTrail = TourEntity.builder()
            .ownerId(owner)
            .name("Alpine Ridge Trail")
            .description("Full day hike across ridges with stunning views over the valley.")
            .fromLocation("Innsbruck")
            .toLocation("Seefeld")
            .transportType(TransportType.HIKE)
            .distanceKm(18.5)
            .estimatedTimeMinutes(420)
            .routeWaypoints("11.4041,47.2692;11.3538,47.2822;11.2805,47.3009;11.1879,47.3297")
            .routeStops("Nordkette\nReith bei Seefeld")
            .routeInformation("Alpine ridge loop with hut stop")
            .routeGeoJson("{\"type\":\"LineString\",\"coordinates\":[[11.4041,47.2692],[11.3538,47.2822],[11.2805,47.3009],[11.1879,47.3297]]}")
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
            .routeWaypoints("16.0549,48.3316;16.1250,48.3150;16.2150,48.2750;16.3738,48.2082")
            .routeStops("Klosterneuburg\nDonauinsel")
            .routeInformation("EV6 along the Danube")
            .routeGeoJson("{\"type\":\"LineString\",\"coordinates\":[[16.0549,48.3316],[16.1250,48.3150],[16.2150,48.2750],[16.3738,48.2082]]}")
            .imagePath("assets/tours/donau.jpg")
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

    private UserEntity ensureDemoUser() {
        UserEntity user = userRepository.findByUsernameIgnoreCase("demo")
            .orElseGet(this::createDemoUser);
        if (!passwordHasher.matches("password", user.getPasswordHash())) {
            user.setPasswordHash(passwordHasher.hash("password"));
            user.setDisplayName("Demo User");
            user = userRepository.save(user);
            log.info("Reset demo user password");
        }
        return user;
    }

    private UserEntity createDemoUser() {
        UserEntity user = new UserEntity();
        user.setUsername("demo");
        user.setDisplayName("Demo User");
        user.setPasswordHash(passwordHasher.hash("password"));
        return userRepository.save(user);
    }
}
