package com.ecom.auth.service;

import com.ecom.auth.model.AuthUser;
import com.ecom.auth.util.JwtUtil;
import com.ecom.auth.util.PasswordUtil;
import com.ecom.dto.AuthResponse;
import com.ecom.dto.CreateUserRequest;
import com.ecom.dto.LoginRequest;
import com.ecom.dto.UserDTO;
import com.ecom.event.UserCreatedEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final JwtUtil jwtUtil;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final Map<String, AuthUser> userStore = new ConcurrentHashMap<>();

    private static Path getUsersStoragePath() {
        Path p1 = Paths.get("data", "users.json");
        if (Files.exists(p1) || Files.isDirectory(Paths.get("data"))) return p1;
        Path p2 = Paths.get(System.getProperty("user.dir"), "backend", "data", "users.json");
        if (Files.exists(p2) || Files.isDirectory(Paths.get(System.getProperty("user.dir"), "backend", "data"))) return p2;
        return p1;
    }

    public AuthService(JwtUtil jwtUtil, KafkaTemplate<String, Object> kafkaTemplate) {
        this.jwtUtil = jwtUtil;
        this.kafkaTemplate = kafkaTemplate;
        initMockUsers();
        loadUsersFromDisk();
    }

    private void initMockUsers() {
        AuthUser admin = new AuthUser(
                "user_admin_01",
                "admin",
                PasswordUtil.hashPassword("admin123"),
                "System",
                "Admin",
                "admin@ecom.com",
                "admin",
                "/users/1.png"
        );
        userStore.put(admin.getId(), admin);

        AuthUser customer = new AuthUser(
                "user_cust_01",
                "johndoe",
                PasswordUtil.hashPassword("password123"),
                "John",
                "Doe",
                "john.doe@example.com",
                "user",
                "/users/2.png"
        );
        userStore.put(customer.getId(), customer);

        AuthUser priya = new AuthUser(
                "user_priya",
                "priya",
                PasswordUtil.hashPassword("priya123"),
                "Priya",
                "Sharma",
                "priya@example.com",
                "user",
                "/users/3.png"
        );
        userStore.put(priya.getId(), priya);
    }

    private synchronized void loadUsersFromDisk() {
        try {
            Path path = getUsersStoragePath();
            if (Files.exists(path)) {
                List<AuthUser> loaded = objectMapper.readValue(path.toFile(), new TypeReference<List<AuthUser>>() {});
                if (loaded != null && !loaded.isEmpty()) {
                    for (AuthUser u : loaded) {
                        userStore.put(u.getId(), u);
                    }
                    log.info("Loaded {} users from disk ({})", loaded.size(), path.toAbsolutePath());
                }
            }
        } catch (Exception e) {
            log.warn("Notice loading users from disk: {}", e.getMessage());
        }
    }

    public synchronized void saveUsersToDisk() {
        try {
            Path path = getUsersStoragePath();
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), new ArrayList<>(userStore.values()));
        } catch (Exception e) {
            log.warn("Notice saving users to disk: {}", e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Username and password are required");
        }

        String identifier = request.getUsername().trim().toLowerCase();
        AuthUser matchedUser = null;

        for (AuthUser user : userStore.values()) {
            if (user.getUsername() != null && user.getUsername().equalsIgnoreCase(identifier)) {
                matchedUser = user;
                break;
            }
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(identifier)) {
                matchedUser = user;
                break;
            }
        }

        if (matchedUser == null) {
            log.warn("Login failed: User '{}' not found", identifier);
            throw new IllegalArgumentException("Invalid username or password");
        }

        if (!PasswordUtil.verifyPassword(request.getPassword(), matchedUser.getPasswordHash())) {
            log.warn("Login failed: Invalid password for user '{}'", identifier);
            throw new IllegalArgumentException("Invalid username or password");
        }

        if (Boolean.TRUE.equals(matchedUser.getBanned())) {
            log.warn("Login failed: User '{}' is banned", identifier);
            throw new IllegalStateException("Account is suspended");
        }

        String token = jwtUtil.generateToken(
                matchedUser.getId(),
                matchedUser.getUsername(),
                matchedUser.getEmail(),
                matchedUser.getRole(),
                matchedUser.getFirstName(),
                matchedUser.getLastName()
        );

        log.info("User '{}' successfully authenticated via JWT", matchedUser.getUsername());
        return new AuthResponse(token, "Bearer", matchedUser.toUserDTO());
    }

    public AuthResponse register(CreateUserRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Username and password are required");
        }

        String uname = request.getUsername().trim().toLowerCase();
        String email = (request.getEmailAddress() != null && !request.getEmailAddress().isEmpty())
                ? request.getEmailAddress().get(0).trim().toLowerCase()
                : (uname + "@example.com");

        // Validate uniqueness
        for (AuthUser u : userStore.values()) {
            if (u.getUsername() != null && u.getUsername().equalsIgnoreCase(uname)) {
                throw new IllegalArgumentException("Username is already taken");
            }
            if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(email)) {
                throw new IllegalArgumentException("Email is already registered");
            }
        }

        String id = "user_" + UUID.randomUUID().toString().substring(0, 8);
        String passwordHash = PasswordUtil.hashPassword(request.getPassword());

        AuthUser newUser = new AuthUser(
                id,
                request.getUsername().trim(),
                passwordHash,
                request.getFirstName() != null ? request.getFirstName().trim() : "",
                request.getLastName() != null ? request.getLastName().trim() : "",
                email,
                "user",
                "/users/" + (new Random().nextInt(10) + 1) + ".png"
        );

        userStore.put(newUser.getId(), newUser);
        saveUsersToDisk();

        // Publish user.created to Kafka
        try {
            UserCreatedEvent event = UserCreatedEvent.builder()
                    .username(newUser.getUsername())
                    .email(email)
                    .build();
            kafkaTemplate.send("user.created", event);
            log.info("Published user.created event to Kafka for user '{}'", newUser.getUsername());
        } catch (Exception e) {
            log.error("Failed to publish user.created event", e);
        }

        String token = jwtUtil.generateToken(
                newUser.getId(),
                newUser.getUsername(),
                newUser.getEmail(),
                newUser.getRole(),
                newUser.getFirstName(),
                newUser.getLastName()
        );

        log.info("User '{}' successfully registered with JWT", newUser.getUsername());
        return new AuthResponse(token, "Bearer", newUser.toUserDTO());
    }

    public UserDTO createUser(CreateUserRequest request) {
        AuthResponse response = register(request);
        return response.getUser();
    }

    public UserDTO getUser(String id) {
        AuthUser user = userStore.get(id);
        return user != null ? user.toUserDTO() : null;
    }

    public List<UserDTO> getUserList() {
        List<UserDTO> list = new ArrayList<>();
        for (AuthUser u : userStore.values()) {
            list.add(u.toUserDTO());
        }
        return list;
    }

    public UserDTO deleteUser(String id) {
        AuthUser removed = userStore.remove(id);
        if (removed != null) {
            saveUsersToDisk();
            log.info("Deleted user '{}'", id);
            return removed.toUserDTO();
        }
        return null;
    }

    public UserDTO validateToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        String userId = jwtUtil.extractUserId(token);
        return getUser(userId);
    }
}
