package com.smartlogix.user.controller;

import com.smartlogix.user.dto.AuthResponse;
import com.smartlogix.user.dto.CredentialValidationRequest;
import com.smartlogix.user.dto.CredentialValidationResponse;
import com.smartlogix.user.dto.LoginRequest;
import com.smartlogix.user.dto.RegisterRequest;
import com.smartlogix.user.dto.TokenValidationRequest;
import com.smartlogix.user.dto.TokenValidationResponse;
import com.smartlogix.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/validate")
    public TokenValidationResponse validate(@Valid @RequestBody TokenValidationRequest request) {
        return authService.validateToken(request.token());
    }

    @PostMapping("/validate-credentials")
    public CredentialValidationResponse validateCredentials(@Valid @RequestBody CredentialValidationRequest request) {
        return authService.validateCredentials(request);
    }
}
