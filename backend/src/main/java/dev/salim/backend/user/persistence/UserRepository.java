package dev.salim.backend.user.persistence;

import dev.salim.backend.user.domain.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByUsernameIgnoreCase(String username);
    Optional<UserEntity> findByUsernameIgnoreCase(String username);
}
