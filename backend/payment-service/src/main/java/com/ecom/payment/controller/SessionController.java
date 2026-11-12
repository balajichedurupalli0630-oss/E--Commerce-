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
        try {
            String clientSecret = stripeService.createCheckoutSession(request.getCart(), resolveUserId(headerUserId, authHeader));
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
        try {
            long now = System.currentTimeMillis();
            stripeService.publishMockPayment(request.getCart(), resolveUserId(headerUserId, authHeader), email != null ? email : "customer@example.com");
            return ResponseEntity.ok(Map.of("status", "success", "sessionId", "mock_" + now, "checkoutSessionClientSecret", "mock_secret_" + now));
        } catch (Exception e) {
            log.error("Error creating mock checkout", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{session_id}")
    public ResponseEntity<?> getSession(@PathVariable("session_id") String sessionId) {
        try {
            return ResponseEntity.ok(stripeService.getSessionStatus(sessionId));
        } catch (Exception e) {
            log.error("Error retrieving session {}", sessionId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String resolveUserId(String headerUserId, String authHeader) {
        if (headerUserId != null && !headerUserId.isBlank()) return headerUserId;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String[] parts = authHeader.substring(7).split("\\.");
                if (parts.length >= 2) {
                    byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
                    JsonNode node = objectMapper.readTree(new String(decoded, StandardCharsets.UTF_8));
                    if (node.has("sub")) return node.get("sub").asText();
                }
            } catch (Exception e) {
                log.warn("Could not extract sub from JWT: {}", e.getMessage());
            }
        }
        return "guest_user";
    }
}
