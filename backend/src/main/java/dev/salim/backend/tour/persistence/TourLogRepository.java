package dev.salim.backend.tour.persistence;

import dev.salim.backend.tour.domain.TourLogEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourLogRepository extends JpaRepository<TourLogEntity, UUID> {
    List<TourLogEntity> findByTourId(UUID tourId);
}
