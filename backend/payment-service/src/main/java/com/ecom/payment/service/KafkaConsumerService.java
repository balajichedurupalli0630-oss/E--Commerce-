package com.ecom.payment.service;

import com.ecom.event.ProductCreatedEvent;
import com.ecom.event.ProductDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final StripeService stripeService;

    public KafkaConsumerService(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @KafkaListener(topics = "product.created", groupId = "payment-service")
    public void handleProductCreated(ProductCreatedEvent event) {
        log.info("Received product.created: {} - {}", event.getId(), event.getName());
        stripeService.createStripeProduct(event.getId(), event.getName(), event.getPrice());
    }

    @KafkaListener(topics = "product.deleted", groupId = "payment-service")
    public void handleProductDeleted(ProductDeletedEvent event) {
        log.info("Received product.deleted: {}", event.getId());
        stripeService.deleteStripeProduct(event.getId() != null ? event.getId().toString() : null);
    }
}
