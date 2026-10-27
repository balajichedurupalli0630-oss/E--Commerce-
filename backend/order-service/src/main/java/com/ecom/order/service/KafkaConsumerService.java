package com.ecom.order.service;

import com.ecom.event.PaymentSuccessfulEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final OrderService orderService;

    public KafkaConsumerService(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "payment.successful", groupId = "order-service")
    public void handlePaymentSuccessful(PaymentSuccessfulEvent event) {
        log.info("Received payment.successful Kafka event for userId: {}", event.getUserId());
        try {
            orderService.createOrder(event);
        } catch (Exception e) {
            log.error("Error creating order from payment event", e);
        }
    }
}
