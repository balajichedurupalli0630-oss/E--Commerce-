package com.omnicart.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.omnicart.config.AppConfig;
import com.omnicart.model.User;

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
        User admin = new User("user_admin_01", "admin", "System", "Admin", "admin@ecom.com", "admin");
        admin.setImageUrl("/users/1.png");

        User customer = new User("user_cust_01", "johndoe", "John", "Doe", "john.doe@example.com", "user");
        customer.setImageUrl("/users/2.png");

        User priya = new User("user_priya", "priya", "Priya", "Sharma", "priya@example.com", "user");
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
        String url = AppConfig.AUTH_SERVICE_URL + "/users";
        Map<String, Object> body = Map.of(
                "username", username,
                "firstName", firstName,
                "lastName", lastName,
                "emailAddress", List.of(email),
                "password", password
        );

        return apiClient.postAsync(url, body, User.class)
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
        String url = AppConfig.AUTH_SERVICE_URL + "/users/" + id;
        return apiClient.deleteAsync(url, User.class)
                .exceptionally(ex -> {
                    mockUsers.values().removeIf(u -> Objects.equals(u.getId(), id));
                    saveUsersToDisk();
                    return null;
                });
    }

    public CompletableFuture<User> authenticate(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            String uname = username.trim().toLowerCase();

            // Check mockUsers store first
            if (mockUsers.containsKey(uname)) {
                return mockUsers.get(uname);
            }

            for (User u : mockUsers.values()) {
                if (u.getUsername() != null && u.getUsername().equalsIgnoreCase(uname)) {
                    return u;
                }
                if (u.getPrimaryEmail() != null && u.getPrimaryEmail().equalsIgnoreCase(uname)) {
                    return u;
                }
            }

            // Create customer on the fly with stable deterministic ID
            String stableId = "user_" + uname.replaceAll("[^a-zA-Z0-9]", "_");
            String capName = uname.substring(0, 1).toUpperCase() + (uname.length() > 1 ? uname.substring(1) : "");
            User created = new User(stableId, username, capName, "", uname + "@example.com", "user");
            created.setImageUrl("/users/3.png");
            mockUsers.put(uname, created);
            saveUsersToDisk();
            return created;
        });
    }
}
