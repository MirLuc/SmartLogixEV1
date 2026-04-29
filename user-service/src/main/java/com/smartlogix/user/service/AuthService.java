package com.smartlogix.user.service;

import com.smartlogix.user.domain.UserAccount;
import com.smartlogix.user.dto.AuthResponse;
import com.smartlogix.user.dto.CredentialValidationRequest;
import com.smartlogix.user.dto.CredentialValidationResponse;
import com.smartlogix.user.dto.LoginRequest;
import com.smartlogix.user.dto.RegisterRequest;
import com.smartlogix.user.dto.TokenResponse;
import com.smartlogix.user.dto.TokenValidationResponse;
import com.smartlogix.user.dto.UserResponse;
import com.smartlogix.user.exception.InvalidCredentialsException;
import com.smartlogix.user.exception.UserDisabledException;
import com.smartlogix.user.exception.UserNotFoundException;
import com.smartlogix.user.repository.UserAccountRepository;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserAccountRepository repository;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserAccountRepository repository,
            UserService userService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.repository = repository;
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        UserResponse user = userService.register(request);
        UserAccount account = repository.findByUsernameIgnoreCase(user.username())
                .orElseThrow(() -> new UserNotFoundException("No existe el usuario " + user.username()));
        TokenResponse token = jwtService.generateToken(account);
        return new AuthResponse(user, token);
    }

    public AuthResponse login(LoginRequest request) {
        UserAccount user = findUser(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales invalidas"));
        if (!user.isEnabled()) {
            throw new UserDisabledException("Usuario deshabilitado");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciales invalidas");
        }
        return new AuthResponse(toResponse(user), jwtService.generateToken(user));
    }

    @Transactional(readOnly = true)
    public TokenValidationResponse validateToken(String token) {
        return jwtService.parseToken(token)
                .map(details -> new TokenValidationResponse(true, details.subject(), details.roles(), details.expiresAt()))
                .orElseGet(() -> new TokenValidationResponse(false, null, Set.of(), null));
    }

    @Transactional(readOnly = true)
    public CredentialValidationResponse validateCredentials(CredentialValidationRequest request) {
        Optional<UserAccount> user = findUser(request.username());
        if (user.isEmpty()) {
            return new CredentialValidationResponse(false, null);
        }
        UserAccount account = user.get();
        if (!account.isEnabled()) {
            return new CredentialValidationResponse(false, null);
        }
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            return new CredentialValidationResponse(false, null);
        }
        return new CredentialValidationResponse(true, toResponse(account));
    }

    private Optional<UserAccount> findUser(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            return Optional.empty();
        }
        String value = usernameOrEmail.trim();
        if (value.contains("@")) {
            return repository.findByEmailIgnoreCase(value);
        }
        return repository.findByUsernameIgnoreCase(value);
    }

    private UserResponse toResponse(UserAccount user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                Set.copyOf(user.getRoles()),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
