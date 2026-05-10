package dev.salim.backend.tour.persistence;

import dev.salim.backend.tour.domain.TourEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourRepository extends JpaRepository<TourEntity, UUID> {
    List<TourEntity> findByOwnerId(UUID ownerId);
    Optional<TourEntity> findByIdAndOwnerId(UUID id, UUID ownerId);
}
