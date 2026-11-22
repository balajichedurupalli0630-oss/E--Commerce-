package com.omnicart.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.omnicart.config.AppConfig;
import com.omnicart.config.SessionManager;
import com.omnicart.model.User;
import com.omnicart.util.PasswordUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AuthApiService {
    private static final AuthApiService INSTANCE = new AuthApiService();
    private final ApiClient apiClient = ApiClient.getInstance();

    private final Map<String, User> mockUsers = new HashMap<>();

    private static Path getUsersFilePath() {
        Path p1 = Paths.get("data", "users.json");
        if (Files.exists(p1) || Files.isDirectory(Paths.get("data"))) return p1;
        Path p2 = Paths.get(System.getProperty("user.dir"), "javafx-frontend", "data", "users.json");
        if (Files.exists(p2) || Files.isDirectory(Paths.get(System.getProperty("user.dir"), "javafx-frontend", "data"))) return p2;
        return p1;
    }

    private AuthApiService() {
        initMockUsers();
        loadUsersFromDisk();
    }

    public static AuthApiService getInstance() {
        return INSTANCE;
    }

    private void initMockUsers() {
        User admin = new User(
                "user_admin_01",
                "admin",
                "System",
                "Admin",
                "admin@ecom.com",
                "admin",
                PasswordUtil.hashPassword("admin123")
        );
        admin.setImageUrl("/users/1.png");

        User customer = new User(
                "user_cust_01",
                "johndoe",
                "John",
                "Doe",
                "john.doe@example.com",
                "user",
                PasswordUtil.hashPassword("password123")
        );
        customer.setImageUrl("/users/2.png");

        User priya = new User(
                "user_priya",
                "priya",
                "Priya",
                "Sharma",
                "priya@example.com",
                "user",
                PasswordUtil.hashPassword("priya123")
        );
        priya.setImageUrl("/users/3.png");

        mockUsers.put(admin.getUsername().toLowerCase(), admin);
        mockUsers.put(customer.getUsername().toLowerCase(), customer);
        mockUsers.put(priya.getUsername().toLowerCase(), priya);
    }

    private synchronized void loadUsersFromDisk() {
        try {
            Path path = getUsersFilePath();
            if (Files.exists(path)) {
                List<User> list = apiClient.getObjectMapper().readValue(path.toFile(), new TypeReference<List<User>>() {});
                if (list != null) {
                    for (User u : list) {
                        if (u.getUsername() != null) {
                            // Ensure default passwords if missing
                            if (u.getPasswordHash() == null) {
                                if ("admin".equalsIgnoreCase(u.getUsername())) {
                                    u.setPasswordHash(PasswordUtil.hashPassword("admin123"));
                                } else {
                                    u.setPasswordHash(PasswordUtil.hashPassword("password123"));
                                }
                            }
                            mockUsers.put(u.getUsername().toLowerCase(), u);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[AuthApiService] Notice loading users from disk: " + e.getMessage());
        }
    }

    public synchronized void saveUsersToDisk() {
        try {
            Path path = getUsersFilePath();
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            apiClient.getObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValue(path.toFile(), new ArrayList<>(mockUsers.values()));
        } catch (Exception e) {
            System.err.println("[AuthApiService] Notice saving users to disk: " + e.getMessage());
        }
    }

    public CompletableFuture<List<User>> getUsers() {
        String url = AppConfig.AUTH_SERVICE_URL + "/users";
        return apiClient.getAsync(url, new TypeReference<List<User>>() {})
                .exceptionally(ex -> new ArrayList<>(mockUsers.values()));
    }

    public CompletableFuture<User> getUser(String id) {
        String url = AppConfig.AUTH_SERVICE_URL + "/users/" + id;
        return apiClient.getAsync(url, User.class)
                .exceptionally(ex -> {
                    for (User u : mockUsers.values()) {
                        if (Objects.equals(u.getId(), id)) return u;
                    }
                    return null;
                });
    }

    public CompletableFuture<User> createUser(String username, String firstName, String lastName, String email, String password) {
        String url = AppConfig.AUTH_SERVICE_URL + "/auth/register";
        Map<String, Object> body = Map.of(
                "username", username,
                "firstName", firstName,
                "lastName", lastName,
                "emailAddress", List.of(email),
                "password", password
        );

        return apiClient.postAsync(url, body, JsonNode.class)
                .thenApply(json -> {
                    try {
                        String token = json.has("token") ? json.get("token").asText() : null;
                        JsonNode userNode = json.has("user") ? json.get("user") : json;
                        User user = apiClient.getObjectMapper().treeToValue(userNode, User.class);
                        if (user != null) {
                            user.setPasswordHash(PasswordUtil.hashPassword(password));
                            mockUsers.put(user.getUsername().toLowerCase(), user);
                            saveUsersToDisk();
                            if (token != null) {
                                SessionManager.getInstance().setSession(user, token);
                            }
                        }
                        return user;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse user from auth service: " + e.getMessage(), e);
                    }
                })
                .exceptionally(ex -> {
                    // Fallback to local offline registration
                    String stableId = "user_" + username.trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
                    User newUser = new User(stableId, username, firstName, lastName, email, "user", PasswordUtil.hashPassword(password));
                    newUser.setImageUrl("/users/" + (new Random().nextInt(10) + 1) + ".png");
                    mockUsers.put(username.toLowerCase(), newUser);
                    saveUsersToDisk();
                    SessionManager.getInstance().setSession(newUser, "offline_token_" + newUser.getId());
                    return newUser;
                });
    }

    public CompletableFuture<User> deleteUser(String id) {
        String url = AppConfig.AUTH_SERVICE_URL + "/users/" + id;
        return apiClient.deleteAsync(url, User.class)
                .exceptionally(ex -> {
                    mockUsers.values().removeIf(u -> Objects.equals(u.getId(), id));
                    saveUsersToDisk();
                    return null;
                });
    }

    public CompletableFuture<User> authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null) {
            CompletableFuture<User> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Please enter both username and password."));
            return future;
        }

        String url = AppConfig.AUTH_SERVICE_URL + "/auth/login";
        Map<String, Object> body = Map.of(
                "username", username.trim(),
                "password", password
        );

        return apiClient.postAsync(url, body, JsonNode.class)
                .thenApply(json -> {
                    try {
                        String token = json.has("token") ? json.get("token").asText() : null;
                        JsonNode userNode = json.has("user") ? json.get("user") : json;
                        User user = apiClient.getObjectMapper().treeToValue(userNode, User.class);
                        if (user != null) {
                            user.setPasswordHash(PasswordUtil.hashPassword(password));
                            mockUsers.put(user.getUsername().toLowerCase(), user);
                            saveUsersToDisk();
                            if (token != null) {
                                SessionManager.getInstance().setSession(user, token);
                            }
                        }
                        return user;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse auth response: " + e.getMessage(), e);
                    }
                })
                .exceptionallyCompose(ex -> {
                    // Check if error was 401 Unauthorized (invalid credentials returned by backend)
                    String msg = ex.getMessage() != null ? ex.getMessage() : "";
                    if (msg.contains("HTTP 401") || msg.contains("401") || msg.contains("Invalid username or password")) {
                        CompletableFuture<User> failed = new CompletableFuture<>();
                        failed.completeExceptionally(new IllegalArgumentException("Invalid username or password."));
                        return failed;
                    }

                    // Otherwise backend is offline/unreachable: verify credentials using local offline store
                    return CompletableFuture.supplyAsync(() -> {
                        String uname = username.trim().toLowerCase();
                        User matched = null;

                        if (mockUsers.containsKey(uname)) {
                            matched = mockUsers.get(uname);
                        } else {
                            for (User u : mockUsers.values()) {
                                if (u.getUsername() != null && u.getUsername().equalsIgnoreCase(uname)) {
                                    matched = u;
                                    break;
                                }
                                if (u.getPrimaryEmail() != null && u.getPrimaryEmail().equalsIgnoreCase(uname)) {
                                    matched = u;
                                    break;
                                }
                            }
                        }

                        if (matched == null) {
                            throw new IllegalArgumentException("User not found. Please sign up or check username.");
                        }

                        if (matched.getPasswordHash() != null) {
                            if (!PasswordUtil.verifyPassword(password, matched.getPasswordHash())) {
                                throw new IllegalArgumentException("Invalid password. Please try again.");
                            }
                        }

                        SessionManager.getInstance().setSession(matched, "offline_token_" + matched.getId());
                        return matched;
                    });
                });
    }
}
