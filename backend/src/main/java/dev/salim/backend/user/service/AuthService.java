package dev.salim.backend.user.service;

import dev.salim.backend.user.api.AuthRequest;
import dev.salim.backend.user.api.AuthResponse;
import dev.salim.backend.user.api.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
}
