package dev.salim.backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.salim.backend.common.exception.AuthenticationFailedException;
import dev.salim.backend.common.exception.DuplicateResourceException;
import dev.salim.backend.user.api.AuthRequest;
import dev.salim.backend.user.api.AuthResponse;
import dev.salim.backend.user.api.RegisterRequest;
import dev.salim.backend.user.persistence.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_createsUser() {
        AuthResponse response = authService.register(registerRequest("salim"));

        assertThat(response.id()).isNotNull();
        assertThat(response.username()).isEqualTo("salim");
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void register_normalizesUsername() {
        AuthResponse response = authService.register(registerRequest("  SALIM  "));

        assertThat(response.username()).isEqualTo("salim");
    }

    @Test
    void register_rejectsDuplicateUsername() {
        authService.register(registerRequest("salim"));

        assertThatThrownBy(() -> authService.register(registerRequest("SALIM")))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void login_returnsUserForValidCredentials() {
        authService.register(registerRequest("salim"));

        AuthResponse response = authService.login(new AuthRequest("salim", "secret123"));

        assertThat(response.displayName()).isEqualTo("Salim");
    }

    @Test
    void login_rejectsInvalidPassword() {
        authService.register(registerRequest("salim"));

        assertThatThrownBy(() -> authService.login(new AuthRequest("salim", "wrong-password")))
            .isInstanceOf(AuthenticationFailedException.class);
    }

    private RegisterRequest registerRequest(String username) {
        return new RegisterRequest(username, "secret123", "Salim");
    }
}
