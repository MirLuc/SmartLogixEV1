package com.smartlogix.user.service;

import com.smartlogix.user.domain.Role;
import java.time.OffsetDateTime;
import java.util.Set;

public record TokenDetails(
        String subject,
        Set<Role> roles,
        OffsetDateTime expiresAt
) {
}
