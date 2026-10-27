package com.ecom.payment.controller;

import com.ecom.dto.CheckoutSessionRequest;
import com.ecom.dto.CheckoutSessionResponse;
import com.ecom.dto.SessionStatusResponse;
import com.ecom.payment.service.StripeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private static final Logger log = LoggerFactory.getLogger(SessionController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final StripeService stripeService;

    public SessionController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(
            @RequestBody CheckoutSessionRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId) {

        String userId = headerUserId;
        if (userId == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            userId = extractUserIdFromJwt(authHeader.substring(7));
        }

        try {
            String clientSecret = stripeService.createCheckoutSession(
                    request.getCart(),
                    userId != null ? userId : "guest_user"
            );
            return ResponseEntity.ok(new CheckoutSessionResponse(clientSecret));
        } catch (Exception e) {
            log.error("Error creating checkout session", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/mock-checkout")
    public ResponseEntity<?> mockCheckout(
            @RequestBody CheckoutSessionRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestParam(value = "email", required = false) String email) {

        String userId = headerUserId;
        if (userId == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            userId = extractUserIdFromJwt(authHeader.substring(7));
        }

        try {
            String mockSessionId = "mock_" + System.currentTimeMillis();
            stripeService.publishMockPayment(
                    request.getCart(),
                    userId != null ? userId : "guest_user",
                    email != null ? email : "customer@example.com"
            );
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "sessionId", mockSessionId,
                    "checkoutSessionClientSecret", "mock_secret_" + System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("Error creating mock checkout", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{session_id}")
    public ResponseEntity<?> getSession(@PathVariable("session_id") String sessionId) {
        try {
            SessionStatusResponse response = stripeService.getSessionStatus(sessionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving session {}", sessionId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String extractUserIdFromJwt(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
                JsonNode jsonNode = objectMapper.readTree(new String(decoded, StandardCharsets.UTF_8));
                if (jsonNode.has("sub")) {
                    return jsonNode.get("sub").asText();
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract sub from JWT: {}", e.getMessage());
        }
        return null;
    }
}
