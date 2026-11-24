package com.ecom.payment.service;

import com.ecom.dto.CartItemDTO;
import com.ecom.dto.OrderItemDTO;
import com.ecom.dto.SessionStatusResponse;
import com.ecom.event.PaymentSuccessfulEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String createCheckoutSession(List<CartItemDTO> cart, String userId) {
        String sessionId = "mock_sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.info("Created native checkout session {} for user {}", sessionId, userId);
        return sessionId;
    }

    public SessionStatusResponse getSessionStatus(String sessionId) {
        return new SessionStatusResponse("complete", "paid");
    }

    public void publishMockPayment(List<CartItemDTO> cart, String userId, String email) {
        List<OrderItemDTO> products = new ArrayList<>();
        int totalAmount = 0;
        if (cart != null) {
            for (CartItemDTO item : cart) {
                int itemPrice = (item.getPrice() != null ? item.getPrice() : 0);
                int qty = (item.getQuantity() != null ? item.getQuantity() : 1);
                totalAmount += itemPrice * qty * 100;
                products.add(OrderItemDTO.builder()
                        .productId(item.getId())
                        .name(item.getName() != null ? item.getName() : "Product")
                        .quantity(qty)
                        .price(itemPrice * 100)
                        .build());
            }
        }

        PaymentSuccessfulEvent paymentEvent = PaymentSuccessfulEvent.builder()
                .userId(userId != null && !userId.isBlank() ? userId : "guest_user")
                .email(email != null && !email.isBlank() ? email : "customer@example.com")
                .amount(totalAmount)
                .status("success")
                .products(products)
                .build();

        kafkaTemplate.send("payment.successful", paymentEvent);
        log.info("Published native payment.successful event for user {}", paymentEvent.getUserId());
    }
}
