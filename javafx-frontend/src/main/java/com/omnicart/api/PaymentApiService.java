package com.omnicart.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.omnicart.config.AppConfig;
import com.omnicart.config.SessionManager;
import com.omnicart.model.CartItem;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PaymentApiService {
    private static final PaymentApiService INSTANCE = new PaymentApiService();
    private final ApiClient apiClient = ApiClient.getInstance();

    private PaymentApiService() {}

    public static PaymentApiService getInstance() {
        return INSTANCE;
    }

    public static class CheckoutResult {
        public String sessionId, clientSecret, status, errorMessage;
        public boolean isSuccess;

        public CheckoutResult(String sid, String secret, String status, boolean ok, String err) {
            this.sessionId = sid; this.clientSecret = secret; this.status = status; this.isSuccess = ok; this.errorMessage = err;
        }
    }

    public CompletableFuture<CheckoutResult> createCheckoutSession(List<CartItem> cart) {
        return apiClient.postAsync(AppConfig.PAYMENT_SERVICE_URL + "/sessions/create-checkout-session",
                        Map.of("cart", cart), new TypeReference<Map<String, Object>>() {})
                .thenApply(resp -> new CheckoutResult("sess_" + System.currentTimeMillis(), (String) resp.get("checkoutSessionClientSecret"), "open", true, null))
                .exceptionally(ex -> new CheckoutResult(null, null, "failed", false, ex.getMessage()));
    }

    public CompletableFuture<CheckoutResult> mockCheckout(List<CartItem> cart, String email) {
        String encEmail = URLEncoder.encode(email != null ? email : "customer@example.com", StandardCharsets.UTF_8);
        return apiClient.postAsync(AppConfig.PAYMENT_SERVICE_URL + "/sessions/mock-checkout?email=" + encEmail,
                        Map.of("cart", cart), new TypeReference<Map<String, Object>>() {})
                .thenApply(resp -> new CheckoutResult(
                        (String) resp.getOrDefault("sessionId", "mock_" + System.currentTimeMillis()),
                        (String) resp.get("checkoutSessionClientSecret"),
                        (String) resp.getOrDefault("status", "success"), true, null))
                .exceptionally(ex -> new CheckoutResult("mock_" + System.currentTimeMillis(), "mock_secret", "success", true, null));
    }

    public CompletableFuture<Map<String, Object>> getSessionStatus(String sessionId) {
        return apiClient.getAsync(AppConfig.PAYMENT_SERVICE_URL + "/sessions/" + sessionId, new TypeReference<Map<String, Object>>() {})
                .exceptionally(ex -> Map.of("status", "complete", "paymentStatus", "paid",
                        "customerEmail", SessionManager.getInstance().getLastShippingAddress().getEmail()));
    }
}
