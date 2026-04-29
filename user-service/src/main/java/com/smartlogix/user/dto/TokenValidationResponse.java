package com.smartlogix.user.dto;

import com.smartlogix.user.domain.Role;
import java.time.OffsetDateTime;
import java.util.Set;

public record TokenValidationResponse(
        boolean valid,
        String subject,
        Set<Role> roles,
        OffsetDateTime expiresAt
) {
}
