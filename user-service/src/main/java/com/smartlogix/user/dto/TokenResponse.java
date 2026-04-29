package com.smartlogix.user.dto;

import java.time.OffsetDateTime;

public record TokenResponse(
        String accessToken,
        String tokenType,
        OffsetDateTime expiresAt
) {
}
