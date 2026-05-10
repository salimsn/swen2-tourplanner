package dev.salim.backend.user.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 80) String username,
    @NotBlank @Size(min = 6, max = 120) String password,
    @NotBlank @Size(min = 2, max = 120) String displayName
) {}
