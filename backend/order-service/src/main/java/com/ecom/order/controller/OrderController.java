package com.ecom.order.controller;

import com.ecom.dto.OrderChartDTO;
import com.ecom.dto.OrderDTO;
import com.ecom.order.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@RestController
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/user-orders")
    public ResponseEntity<List<OrderDTO>> getUserOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) String queryUserId) {

        String userId = null;
        if (queryUserId != null && !queryUserId.isBlank()) {
            userId = queryUserId;
        } else if (headerUserId != null && !headerUserId.isBlank()) {
            userId = headerUserId;
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            userId = extractUserIdFromJwt(authHeader.substring(7));
        }

        if (userId == null) {
            log.warn("No userId found in token or headers for /user-orders, returning all latest user orders for dev");
            return ResponseEntity.ok(orderService.getOrders(50));
        }

        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> getOrders(@RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(orderService.getOrders(limit));
    }

    @GetMapping("/order-chart")
    public ResponseEntity<List<OrderChartDTO>> getOrderChart() {
        return ResponseEntity.ok(orderService.getOrderChart());
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
            log.warn("Could not parse JWT sub: {}", e.getMessage());
        }
        return null;
    }
}
