package com.smartlogix.user.dto;

import com.smartlogix.user.domain.Role;
import java.time.OffsetDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        Set<Role> roles,
        boolean enabled,
        OffsetDateTime createdAt
) {
}
