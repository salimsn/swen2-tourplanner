package dev.salim.backend.user.service;

import dev.salim.backend.common.exception.AuthenticationFailedException;
import dev.salim.backend.common.exception.DuplicateResourceException;
import dev.salim.backend.user.api.AuthRequest;
import dev.salim.backend.user.api.AuthResponse;
import dev.salim.backend.user.api.RegisterRequest;
import dev.salim.backend.user.domain.UserEntity;
import dev.salim.backend.user.persistence.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Override
    public AuthResponse register(RegisterRequest request) {
        String username = normalize(request.username());
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("user", username);
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordHasher.hash(request.password()));
        UserEntity saved = userRepository.save(user);
        log.info("Registered user {}", saved.getUsername());
        return toResponse(saved);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        String username = normalize(request.username());
        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
            .orElseThrow(AuthenticationFailedException::new);

        if (!passwordHasher.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationFailedException();
        }

        log.info("User {} logged in", user.getUsername());
        return toResponse(user);
    }

    private AuthResponse toResponse(UserEntity user) {
        return new AuthResponse(user.getId(), user.getUsername(), user.getDisplayName());
    }

    private String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
