package com.ecom.auth.service;

import com.ecom.auth.entity.UserEntity;
import com.ecom.auth.repository.UserRepository;
import com.ecom.dto.AuthResponseDTO;
import com.ecom.dto.CreateUserRequest;
import com.ecom.dto.LoginRequest;
import com.ecom.dto.UserDTO;
import com.ecom.event.UserCreatedEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       KafkaTemplate<String, Object> kafkaTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void seedInitialUsers() {
        if (userRepository.count() == 0) {
            log.info("Seeding default users into PostgreSQL authdb...");

            UserEntity admin = new UserEntity(
                    "user_admin_01",
                    "admin",
                    "admin@ecom.com",
                    passwordEncoder.encode("admin123"),
                    "System",
                    "Admin",
                    "admin",
                    "/users/1.png"
            );
            userRepository.save(admin);

            UserEntity customer = new UserEntity(
                    "user_cust_01",
                    "johndoe",
                    "john.doe@example.com",
                    passwordEncoder.encode("password123"),
                    "John",
                    "Doe",
                    "user",
                    "/users/2.png"
            );
            userRepository.save(customer);

            UserEntity priya = new UserEntity(
                    "user_priya_01",
                    "priya",
                    "priya@example.com",
                    passwordEncoder.encode("password123"),
                    "Priya",
                    "Sharma",
                    "user",
                    "/users/3.png"
            );
            userRepository.save(priya);

            log.info("Default users seeded successfully (admin, johndoe, priya).");
        }
    }

    public AuthResponseDTO login(LoginRequest request) {
        if (request.getIdentifier() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Username/email and password are required");
        }

        String identifier = request.getIdentifier().trim().toLowerCase();
        Optional<UserEntity> userOpt = userRepository.findByUsername(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(identifier);
        }

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        UserEntity user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(token, user.toDTO());
    }

    public AuthResponseDTO register(CreateUserRequest request) {
        String email = (request.getEmailAddress() != null && !request.getEmailAddress().isEmpty())
                ? request.getEmailAddress().get(0).trim().toLowerCase()
                : (request.getUsername() + "@example.com").toLowerCase();

        String username = request.getUsername() != null ? request.getUsername().trim().toLowerCase() : email.split("@")[0];

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already in use: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use: " + email);
        }

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : "password123";

        String stableId = "user_" + UUID.randomUUID().toString().substring(0, 8);
        UserEntity user = new UserEntity(
                stableId,
                username,
                email,
                passwordEncoder.encode(rawPassword),
                request.getFirstName() != null ? request.getFirstName() : "",
                request.getLastName() != null ? request.getLastName() : "",
                "user",
                "/users/" + (new Random().nextInt(10) + 1) + ".png"
        );

        user = userRepository.save(user);

        // Publish user.created to Kafka
        try {
            UserCreatedEvent event = UserCreatedEvent.builder()
                    .username(user.getUsername())
                    .email(email)
                    .build();
            kafkaTemplate.send("user.created", event);
            log.info("Published user.created event to Kafka for user {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to publish user.created event", e);
        }

        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(token, user.toDTO());
    }

    public List<UserDTO> getUserList() {
        return userRepository.findAll().stream()
                .map(UserEntity::toDTO)
                .toList();
    }

    public UserDTO getUser(String id) {
        return userRepository.findById(id)
                .map(UserEntity::toDTO)
                .orElse(null);
    }

    public UserDTO createUser(CreateUserRequest request) {
        AuthResponseDTO response = register(request);
        return response.getUser();
    }

    public UserDTO deleteUser(String id) {
        Optional<UserEntity> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            userRepository.delete(user);
            return user.toDTO();
        }
        return null;
    }

    public UserDTO getUserFromToken(String token) {
        if (token == null || !jwtService.validateToken(token)) {
            return null;
        }
        String userId = jwtService.extractUserId(token);
        return getUser(userId);
    }
}
