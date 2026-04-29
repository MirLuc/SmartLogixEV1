package com.smartlogix.user.dto;

public record CredentialValidationResponse(
        boolean valid,
        UserResponse user
) {
}
