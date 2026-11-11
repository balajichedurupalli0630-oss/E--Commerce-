package com.omnicart.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.omnicart.config.AppConfig;
import com.omnicart.model.User;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AuthApiService {
    private static final AuthApiService INSTANCE = new AuthApiService();
    private final ApiClient apiClient = ApiClient.getInstance();
    private final Map<String, User> mockUsers = new HashMap<>();

    private static Path getUsersFilePath() {
        return Files.exists(Path.of("data")) ? Path.of("data", "users.json") : Path.of("javafx-frontend", "data", "users.json");
    }

    private AuthApiService() {
        initMockUsers();
        loadUsersFromDisk();
    }

    public static AuthApiService getInstance() {
        return INSTANCE;
    }

    private void initMockUsers() {
        addMock(new User("user_admin_01", "admin", "System", "Admin", "admin@ecom.com", "admin"), "/users/1.png");
        addMock(new User("user_cust_01", "johndoe", "John", "Doe", "john.doe@example.com", "user"), "/users/2.png");
        addMock(new User("user_priya", "priya", "Priya", "Sharma", "priya@example.com", "user"), "/users/3.png");
    }

    private void addMock(User u, String img) {
        u.setImageUrl(img);
        mockUsers.put(u.getUsername().toLowerCase(), u);
    }

    private synchronized void loadUsersFromDisk() {
        try {
            Path path = getUsersFilePath();
            if (Files.exists(path)) {
                List<User> list = apiClient.getObjectMapper().readValue(path.toFile(), new TypeReference<List<User>>() {});
                if (list != null) {
                    list.stream().filter(u -> u.getUsername() != null)
                            .forEach(u -> mockUsers.put(u.getUsername().toLowerCase(), u));
                }
            }
        } catch (Exception ignored) {}
    }

    public synchronized void saveUsersToDisk() {
        try {
            Path path = getUsersFilePath();
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            apiClient.getObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValue(path.toFile(), new ArrayList<>(mockUsers.values()));
        } catch (Exception ignored) {}
    }

    public CompletableFuture<List<User>> getUsers() {
        return apiClient.getAsync(AppConfig.AUTH_SERVICE_URL + "/users", new TypeReference<List<User>>() {})
                .exceptionally(ex -> new ArrayList<>(mockUsers.values()));
    }

    public CompletableFuture<User> getUser(String id) {
        return apiClient.getAsync(AppConfig.AUTH_SERVICE_URL + "/users/" + id, User.class)
                .exceptionally(ex -> mockUsers.values().stream()
                        .filter(u -> Objects.equals(u.getId(), id))
                        .findFirst().orElse(null));
    }

    public CompletableFuture<User> createUser(String username, String firstName, String lastName, String email, String password) {
        Map<String, Object> body = Map.of(
                "username", username, "firstName", firstName, "lastName", lastName,
                "emailAddress", List.of(email), "password", password
        );
        return apiClient.postAsync(AppConfig.AUTH_SERVICE_URL + "/users", body, User.class)
                .exceptionally(ex -> {
                    String stableId = "user_" + username.trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
                    User newUser = new User(stableId, username, firstName, lastName, email, "user");
                    newUser.setImageUrl("/users/" + (new Random().nextInt(10) + 1) + ".png");
                    mockUsers.put(username.toLowerCase(), newUser);
                    saveUsersToDisk();
                    return newUser;
                });
    }

    public CompletableFuture<User> deleteUser(String id) {
        return apiClient.deleteAsync(AppConfig.AUTH_SERVICE_URL + "/users/" + id, User.class)
                .exceptionally(ex -> {
                    mockUsers.values().removeIf(u -> Objects.equals(u.getId(), id));
                    saveUsersToDisk();
                    return null;
                });
    }

    public CompletableFuture<User> authenticate(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            String uname = username.trim().toLowerCase();
            if (mockUsers.containsKey(uname)) return mockUsers.get(uname);

            for (User u : mockUsers.values()) {
                if (uname.equalsIgnoreCase(u.getUsername()) || uname.equalsIgnoreCase(u.getPrimaryEmail())) {
                    return u;
                }
            }

            String stableId = "user_" + uname.replaceAll("[^a-zA-Z0-9]", "_");
            String capName = Character.toUpperCase(uname.charAt(0)) + (uname.length() > 1 ? uname.substring(1) : "");
            User created = new User(stableId, username, capName, "", uname + "@example.com", "user");
            created.setImageUrl("/users/3.png");
            mockUsers.put(uname, created);
            saveUsersToDisk();
            return created;
        });
    }
}
