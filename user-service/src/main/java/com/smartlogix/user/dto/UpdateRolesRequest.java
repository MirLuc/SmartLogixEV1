package com.smartlogix.user.dto;

import com.smartlogix.user.domain.Role;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UpdateRolesRequest(
        @NotEmpty Set<Role> roles
) {
}
