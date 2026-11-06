package com.omnicart.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.omnicart.config.AppConfig;
import com.omnicart.config.SessionManager;
import com.omnicart.model.Order;
import com.omnicart.model.OrderChartData;
import com.omnicart.model.OrderItem;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class OrderApiService {
    private static final OrderApiService INSTANCE = new OrderApiService();
    private final ApiClient apiClient = ApiClient.getInstance();

    private final List<Order> localOrders = new ArrayList<>();

    private static Path getOrdersFilePath() {
        Path p1 = Paths.get("data", "orders.json");
        if (Files.exists(p1) || Files.isDirectory(Paths.get("data"))) return p1;
        Path p2 = Paths.get(System.getProperty("user.dir"), "javafx-frontend", "data", "orders.json");
        if (Files.exists(p2) || Files.isDirectory(Paths.get(System.getProperty("user.dir"), "javafx-frontend", "data"))) return p2;
        return p1;
    }

    private OrderApiService() {
        loadOrdersFromDisk();
    }

    public static OrderApiService getInstance() {
        return INSTANCE;
    }

    private synchronized void loadOrdersFromDisk() {
        try {
            Path path = getOrdersFilePath();
            if (Files.exists(path)) {
                List<Order> loaded = apiClient.getObjectMapper().readValue(
                        path.toFile(),
                        new TypeReference<List<Order>>() {}
                );
                if (loaded != null && !loaded.isEmpty()) {
                    localOrders.clear();
                    localOrders.addAll(loaded);
                    System.out.println("[OrderApiService] Successfully loaded " + loaded.size() + " orders from " + path.toAbsolutePath());
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("[OrderApiService] Notice loading orders from disk: " + e.getMessage());
        }
        initFallbackOrders();
        saveOrdersToDisk();
    }

    public synchronized void saveOrdersToDisk() {
        try {
            Path path = getOrdersFilePath();
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            apiClient.getObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValue(path.toFile(), localOrders);
            System.out.println("[OrderApiService] Successfully saved " + localOrders.size() + " orders to " + path.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("[OrderApiService] Notice saving orders to disk: " + e.getMessage());
        }
    }

    public CompletableFuture<List<Order>> getUserOrders() {
        com.omnicart.model.User current = SessionManager.getInstance().getCurrentUser();
        String currentUserId = current != null ? current.getId() : "user_priya";
        String currentUsername = current != null ? current.getUsername() : "priya";
        String currentEmail = current != null ? current.getPrimaryEmail() : "priya@example.com";

        String url = AppConfig.ORDER_SERVICE_URL + "/user-orders?userId=" + currentUserId;
        return apiClient.getAsync(url, new TypeReference<List<Order>>() {})
                .thenApply(orders -> {
                    if (orders != null && !orders.isEmpty()) {
                        for (Order ord : orders) {
                            recordLocalOrderInternal(ord);
                        }
                        saveOrdersToDisk();
                    }
                    return filterForUser(localOrders, currentUserId, currentUsername, currentEmail);
                })
                .exceptionally(ex -> filterForUser(localOrders, currentUserId, currentUsername, currentEmail));
    }

    private List<Order> filterForUser(List<Order> source, String userId, String username, String email) {
        if (source == null) return new ArrayList<>();
        return source.stream()
                .filter(o -> matchesCustomer(o, userId, username, email))
                .collect(Collectors.toList());
    }

    private boolean matchesCustomer(Order o, String userId, String username, String email) {
        if (o == null) return false;
        // 1. Direct userId match
        if (userId != null && !userId.isBlank() && userId.equalsIgnoreCase(o.getUserId())) {
            return true;
        }
        // 2. Direct email match
        if (email != null && !email.isBlank() && o.getEmail() != null && email.equalsIgnoreCase(o.getEmail())) {
            return true;
        }
        // 3. Username match in userId or email (e.g. "priya" matches "user_priya" or "priya@example.com")
        if (username != null && !username.isBlank()) {
            String unameLower = username.toLowerCase();
            if (o.getUserId() != null && o.getUserId().toLowerCase().contains(unameLower)) {
                return true;
            }
            if (o.getEmail() != null && o.getEmail().toLowerCase().contains(unameLower)) {
                return true;
            }
        }
        // 4. Default user match for johndoe / user_cust_01
        if (("johndoe".equalsIgnoreCase(username) || "user_cust_01".equalsIgnoreCase(userId))
                && ("user_cust_01".equalsIgnoreCase(o.getUserId()) || (o.getEmail() != null && o.getEmail().toLowerCase().contains("john.doe")))) {
            return true;
        }
        return false;
    }

    public CompletableFuture<List<Order>> getAllOrders(Integer limit) {
        String url = AppConfig.ORDER_SERVICE_URL + "/orders" + (limit != null ? "?limit=" + limit : "");
        return apiClient.getAsync(url, new TypeReference<List<Order>>() {})
                .thenApply(orders -> {
                    if (orders != null && !orders.isEmpty()) {
                        for (Order ord : orders) {
                            recordLocalOrderInternal(ord);
                        }
                        saveOrdersToDisk();
                    }
                    return (List<Order>) new ArrayList<>(localOrders);
                })
                .exceptionally(ex -> (List<Order>) new ArrayList<>(localOrders));
    }

    public CompletableFuture<List<OrderChartData>> getOrderChart() {
        String url = AppConfig.ORDER_SERVICE_URL + "/order-chart";
        return apiClient.getAsync(url, new TypeReference<List<OrderChartData>>() {})
                .exceptionally(ex -> getFallbackChartData());
    }

    public CompletableFuture<Boolean> cancelOrder(String orderId) {
        if (orderId == null) return CompletableFuture.completedFuture(false);
        for (Order o : localOrders) {
            if (orderId.equals(o.getId())) {
                o.setStatus("cancelled");
            }
        }
        saveOrdersToDisk();

        String url = AppConfig.ORDER_SERVICE_URL + "/orders/" + orderId + "/cancel";
        return apiClient.putAsync(url, null, Object.class)
                .thenApply(res -> true)
                .exceptionally(ex -> true);
    }

    public synchronized void recordLocalOrder(Order order) {
        recordLocalOrderInternal(order);
        saveOrdersToDisk();

        // Push to backend order service if running
        try {
            String url = AppConfig.ORDER_SERVICE_URL + "/orders";
            apiClient.postAsync(url, order, Object.class).exceptionally(ex -> null);
        } catch (Exception ignored) {}
    }

    private synchronized void recordLocalOrderInternal(Order order) {
        if (order == null) return;
        localOrders.removeIf(o -> o.getId() != null && o.getId().equals(order.getId()));
        localOrders.add(0, order);
    }

    private void initFallbackOrders() {
        localOrders.add(new Order("ord_priya_101", "user_priya", "priya@example.com", 15980, "success",
                List.of(
                        new OrderItem("Columbia Alpine Thermal Jacket", 1, 8990),
                        new OrderItem("Nike Air Essentials Pullover", 1, 6990)
                ), Instant.now().minusSeconds(3600)));

        localOrders.add(new Order("ord_66f3a1b01", "user_cust_01", "john.doe@example.com", 7980, "success",
                List.of(new OrderItem("Adidas CoreFit T-Shirt", 2, 3990)), Instant.now().minusSeconds(86400 * 2)));

        localOrders.add(new Order("ord_66f3a1b02", "user_cust_02", "sarah.connor@example.com", 6990, "success",
                List.of(new OrderItem("Nike Air Essentials Pullover", 1, 6990)), Instant.now().minusSeconds(86400 * 3)));

        localOrders.add(new Order("ord_66f3a1b03", "user_cust_03", "michael.j@example.com", 5990, "pending",
                List.of(new OrderItem("Puma Ultra Warm Zip", 1, 5990)), Instant.now().minusSeconds(86400 * 4)));

        localOrders.add(new Order("ord_66f3a1b04", "user_cust_04", "emma.watson@example.com", 4990, "success",
                List.of(new OrderItem("Under Armour StormFleece", 1, 4990)), Instant.now().minusSeconds(86400 * 5)));

        localOrders.add(new Order("ord_66f3a1b05", "user_cust_05", "david.beck@example.com", 8990, "failed",
                List.of(new OrderItem("Columbia Alpine Thermal Jacket", 1, 8990)), Instant.now().minusSeconds(86400 * 6)));
    }

    public List<OrderChartData> getFallbackChartData() {
        return List.of(
                new OrderChartData("January", 186L, 140L),
                new OrderChartData("February", 305L, 260L),
                new OrderChartData("March", 237L, 195L),
                new OrderChartData("April", 173L, 150L),
                new OrderChartData("May", 209L, 185L),
                new OrderChartData("June", 214L, 190L)
        );
    }
}
