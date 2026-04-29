package com.smartlogix.user.service;

import com.smartlogix.user.domain.Role;
import com.smartlogix.user.domain.UserAccount;
import com.smartlogix.user.dto.CreateUserRequest;
import com.smartlogix.user.dto.RegisterRequest;
import com.smartlogix.user.dto.UserResponse;
import com.smartlogix.user.exception.UserAlreadyExistsException;
import com.smartlogix.user.exception.UserNotFoundException;
import com.smartlogix.user.repository.UserAccountRepository;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserAccountRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        return toResponse(createUserInternal(
                request.username(),
                request.email(),
                request.password(),
                EnumSet.of(Role.USER)
        ));
    }

    public UserResponse createUser(CreateUserRequest request) {
        Set<Role> roles = request.roles() == null || request.roles().isEmpty()
                ? EnumSet.of(Role.USER)
                : EnumSet.copyOf(request.roles());
        return toResponse(createUserInternal(
                request.username(),
                request.email(),
                request.password(),
                roles
        ));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No existe el usuario " + id)));
    }

    public UserResponse updateRoles(Long id, Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Debe asignar al menos un rol");
        }
        UserAccount user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No existe el usuario " + id));
        user.setRoles(roles);
        repository.save(user);
        return toResponse(user);
    }

    public UserResponse updateStatus(Long id, boolean enabled) {
        UserAccount user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No existe el usuario " + id));
        user.setEnabled(enabled);
        repository.save(user);
        return toResponse(user);
    }

    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException("No existe el usuario " + id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserAccount getByUsername(String username) {
        return repository.findByUsernameIgnoreCase(normalizeUsername(username))
                .orElseThrow(() -> new UserNotFoundException("No existe el usuario " + username));
    }

    private UserAccount createUserInternal(String username, String email, String password, Set<Role> roles) {
        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);
        if (repository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new UserAlreadyExistsException("El usuario ya existe");
        }
        if (repository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new UserAlreadyExistsException("El correo ya existe");
        }
        UserAccount user = new UserAccount();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRoles(roles);
        repository.save(user);
        return user;
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim().toLowerCase();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
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
