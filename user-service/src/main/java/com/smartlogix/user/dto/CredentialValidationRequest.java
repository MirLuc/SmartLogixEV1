package com.smartlogix.user.dto;

import jakarta.validation.constraints.NotBlank;

public record CredentialValidationRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
