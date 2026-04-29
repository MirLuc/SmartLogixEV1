package com.smartlogix.user.dto;

public record AuthResponse(
        UserResponse user,
        TokenResponse token
) {
}
