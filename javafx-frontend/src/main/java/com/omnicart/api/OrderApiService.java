package com.omnicart.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.omnicart.config.AppConfig;
import com.omnicart.config.SessionManager;
import com.omnicart.model.Order;
import com.omnicart.model.OrderChartData;
import com.omnicart.model.OrderItem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class OrderApiService {
    private static final OrderApiService INSTANCE = new OrderApiService();
    private final ApiClient apiClient = ApiClient.getInstance();
    private final List<Order> localOrders = new ArrayList<>();

    private static Path getOrdersFilePath() {
        return Files.exists(Path.of("data")) ? Path.of("data", "orders.json") : Path.of("javafx-frontend", "data", "orders.json");
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
                List<Order> loaded = apiClient.getObjectMapper().readValue(path.toFile(), new TypeReference<List<Order>>() {});
                if (loaded != null && !loaded.isEmpty()) {
                    localOrders.addAll(loaded);
                    return;
                }
            }
        } catch (Exception ignored) {}
        initFallbackOrders();
        saveOrdersToDisk();
    }

    public synchronized void saveOrdersToDisk() {
        try {
            Path path = getOrdersFilePath();
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            apiClient.getObjectMapper().writerWithDefaultPrettyPrinter().writeValue(path.toFile(), localOrders);
        } catch (Exception ignored) {}
    }

    public CompletableFuture<List<Order>> getUserOrders() {
        var user = SessionManager.getInstance().getCurrentUser();
        String uid = user != null ? user.getId() : "user_priya";
        String uname = user != null ? user.getUsername() : "priya";
        String email = user != null ? user.getPrimaryEmail() : "priya@example.com";

        return apiClient.getAsync(AppConfig.ORDER_SERVICE_URL + "/user-orders?userId=" + uid, new TypeReference<List<Order>>() {})
                .thenApply(orders -> {
                    if (orders != null && !orders.isEmpty()) {
                        orders.forEach(this::recordLocalOrderInternal);
                        saveOrdersToDisk();
                        return deduplicateOrders(orders);
                    }
                    return filterForUser(localOrders, uid, uname, email);
                })
                .exceptionally(ex -> filterForUser(localOrders, uid, uname, email));
    }

    private List<Order> deduplicateOrders(List<Order> list) {
        if (list == null) return new ArrayList<>();
        Map<String, Order> map = new LinkedHashMap<>();
        for (Order o : list) {
            if (o != null && o.getId() != null) {
                map.putIfAbsent(o.getId(), o);
            }
        }
        return new ArrayList<>(map.values());
    }

    private List<Order> filterForUser(List<Order> source, String userId, String username, String email) {
        if (source == null) return new ArrayList<>();
        List<Order> filtered = source.stream().filter(o -> matchesCustomer(o, userId, username, email)).toList();
        return deduplicateOrders(filtered);
    }

    private boolean matchesCustomer(Order o, String userId, String username, String email) {
        if (o == null) return false;
        String uid = o.getUserId() != null ? o.getUserId().toLowerCase() : "";
        String em = o.getEmail() != null ? o.getEmail().toLowerCase() : "";
        String u = username != null ? username.toLowerCase() : "";

        if (userId != null && !userId.isBlank() && userId.equalsIgnoreCase(o.getUserId())) return true;
        if (email != null && !email.isBlank() && email.equalsIgnoreCase(o.getEmail())) return true;
        if (!u.isBlank() && (uid.contains(u) || em.contains(u))) return true;
        return ("johndoe".equalsIgnoreCase(u) || "user_cust_01".equalsIgnoreCase(userId))
                && ("user_cust_01".equalsIgnoreCase(uid) || em.contains("john.doe"));
    }

    public CompletableFuture<List<Order>> getAllOrders(Integer limit) {
        String url = AppConfig.ORDER_SERVICE_URL + "/orders" + (limit != null ? "?limit=" + limit : "");
        return apiClient.getAsync(url, new TypeReference<List<Order>>() {})
                .thenApply(orders -> {
                    if (orders != null && !orders.isEmpty()) {
                        orders.forEach(this::recordLocalOrderInternal);
                        saveOrdersToDisk();
                        return deduplicateOrders(orders);
                    }
                    return deduplicateOrders(localOrders);
                })
                .exceptionally(ex -> deduplicateOrders(localOrders));
    }

    public CompletableFuture<List<OrderChartData>> getOrderChart() {
        return apiClient.getAsync(AppConfig.ORDER_SERVICE_URL + "/order-chart", new TypeReference<List<OrderChartData>>() {})
                .exceptionally(ex -> getFallbackChartData());
    }

    public CompletableFuture<Boolean> cancelOrder(String orderId) {
        if (orderId == null) return CompletableFuture.completedFuture(false);
        localOrders.stream().filter(o -> orderId.equals(o.getId())).findFirst().ifPresent(o -> o.setStatus("cancelled"));
        saveOrdersToDisk();
        return apiClient.putAsync(AppConfig.ORDER_SERVICE_URL + "/orders/" + orderId + "/cancel", null, Object.class)
                .thenApply(res -> true).exceptionally(ex -> true);
    }

    public synchronized void recordLocalOrder(Order order) {
        recordLocalOrderInternal(order);
        saveOrdersToDisk();
    }

    private synchronized void recordLocalOrderInternal(Order order) {
        if (order == null || order.getId() == null) return;
        // When real order arrives, clean out any matching temporary mock order
        if (!order.getId().startsWith("mock_")) {
            localOrders.removeIf(o -> o.getId() != null && o.getId().startsWith("mock_")
                    && Objects.equals(o.getAmount(), order.getAmount())
                    && Objects.equals(o.getEmail(), order.getEmail()));
        }
        localOrders.removeIf(o -> o.getId() != null && o.getId().equals(order.getId()));
        localOrders.add(0, order);
    }

    private void initFallbackOrders() {
        localOrders.addAll(List.of(
                new Order("ord_priya_101", "user_priya", "priya@example.com", 15980, "success",
                        List.of(new OrderItem("Columbia Alpine Thermal Jacket", 1, 8990), new OrderItem("Nike Air Essentials Pullover", 1, 6990)),
                        Instant.now().minusSeconds(3600)),
                new Order("ord_66f3a1b01", "user_cust_01", "john.doe@example.com", 7980, "success",
                        List.of(new OrderItem("Adidas CoreFit T-Shirt", 2, 3990)), Instant.now().minusSeconds(86400 * 2)),
                new Order("ord_66f3a1b02", "user_cust_02", "sarah.connor@example.com", 6990, "success",
                        List.of(new OrderItem("Nike Air Essentials Pullover", 1, 6990)), Instant.now().minusSeconds(86400 * 3)),
                new Order("ord_66f3a1b03", "user_cust_03", "michael.j@example.com", 5990, "pending",
                        List.of(new OrderItem("Puma Ultra Warm Zip", 1, 5990)), Instant.now().minusSeconds(86400 * 4)),
                new Order("ord_66f3a1b04", "user_cust_04", "emma.watson@example.com", 4990, "success",
                        List.of(new OrderItem("Under Armour StormFleece", 1, 4990)), Instant.now().minusSeconds(86400 * 5)),
                new Order("ord_66f3a1b05", "user_cust_05", "david.beck@example.com", 8990, "failed",
                        List.of(new OrderItem("Columbia Alpine Thermal Jacket", 1, 8990)), Instant.now().minusSeconds(86400 * 6))
        ));
    }

    public List<OrderChartData> getFallbackChartData() {
        return List.of(
                new OrderChartData("June", 0L, 0L, 0.0),
                new OrderChartData("July", 0L, 0L, 0.0),
                new OrderChartData("August", 0L, 0L, 0.0),
                new OrderChartData("September", 0L, 0L, 0.0),
                new OrderChartData("October", 8L, 8L, 78420.0),
                new OrderChartData("November", 1L, 1L, 36440.0)
        );
    }
}
