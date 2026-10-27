package com.ecom.email.service;

import com.ecom.event.OrderCreatedEvent;
import com.ecom.event.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaEmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEmailConsumer.class);

    private final MailService mailService;

    public KafkaEmailConsumer(MailService mailService) {
        this.mailService = mailService;
    }

    @KafkaListener(topics = "user.created", groupId = "email-service")
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("Received user.created event for username: {}", event.getUsername());
        if (event.getEmail() != null && !event.getEmail().isBlank()) {
            mailService.sendEmail(
                    event.getEmail(),
                    "Welcome to E-commerce App",
                    String.format("Welcome %s. Your account has been created!", event.getUsername())
            );
        }
    }

    @KafkaListener(topics = "order.created", groupId = "email-service")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created event for email: {}", event.getEmail());
        if (event.getEmail() != null && !event.getEmail().isBlank()) {
            double amountFormatted = (event.getAmount() != null ? event.getAmount() : 0) / 100.0;
            mailService.sendEmail(
                    event.getEmail(),
                    "Order has been created",
                    String.format("Hello! Your order: Amount: ₹%.2f, Status: %s", amountFormatted, event.getStatus())
            );
        }
    }
}
