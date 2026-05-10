package dev.salim.backend.user.api;

import java.util.UUID;

public record AuthResponse(
    UUID id,
    String username,
    String displayName
) {}
