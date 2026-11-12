package com.ecom.order.service;

import com.ecom.dto.OrderChartDTO;
import com.ecom.dto.OrderDTO;
import com.ecom.event.OrderCreatedEvent;
import com.ecom.event.PaymentSuccessfulEvent;
import com.ecom.order.model.Order;
import com.ecom.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<OrderDTO> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toDTO).toList();
    }

    public List<OrderDTO> getOrders(Integer limit) {
        int pageSize = (limit != null && limit > 0) ? limit : 50;
        return orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, pageSize)).stream().map(this::toDTO).toList();
    }

    public OrderDTO createOrder(PaymentSuccessfulEvent event) {
        Order order = Order.builder()
                .userId(event.getUserId())
                .email(event.getEmail())
                .amount(event.getAmount())
                .status(event.getStatus() != null ? event.getStatus() : "success")
                .products(event.getProducts() != null ? event.getProducts() : new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Order saved = orderRepository.save(order);
        log.info("Saved order {} for user {}", saved.getId(), saved.getUserId());

        try {
            OrderCreatedEvent createdEvent = OrderCreatedEvent.builder()
                    .email(saved.getEmail())
                    .amount(saved.getAmount())
                    .status(saved.getStatus())
                    .build();
            kafkaTemplate.send("order.created", createdEvent);
            log.info("Published order.created event to Kafka for order {}", saved.getId());
        } catch (Exception e) {
            log.error("Failed to publish order.created event", e);
        }

        return toDTO(saved);
    }

    public OrderDTO saveOrder(OrderDTO dto) {
        Order order = Order.builder()
                .userId(dto.getUserId())
                .email(dto.getEmail())
                .amount(dto.getAmount())
                .status(dto.getStatus() != null ? dto.getStatus() : "success")
                .products(dto.getProducts() != null ? dto.getProducts() : new ArrayList<>())
                .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : Instant.now())
                .updatedAt(Instant.now())
                .build();
        Order saved = orderRepository.save(order);
        log.info("Saved REST order {} for user {}", saved.getId(), saved.getUserId());
        return toDTO(saved);
    }

    public OrderDTO cancelOrder(String orderId) {
        return orderRepository.findById(orderId).map(order -> {
            order.setStatus("cancelled");
            order.setUpdatedAt(Instant.now());
            log.info("Cancelled order {}", orderId);
            return toDTO(orderRepository.save(order));
        }).orElse(null);
    }

    public List<OrderChartDTO> getOrderChart() {
        YearMonth currentMonth = YearMonth.from(LocalDate.now(ZoneOffset.UTC));
        Instant sixMonthsAgo = currentMonth.minusMonths(5).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        List<Order> recentOrders = orderRepository.findByCreatedAtBetween(sixMonthsAgo, Instant.now());
        List<OrderChartDTO> results = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            Instant monthStart = targetMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant monthEnd = targetMonth.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

            long total = 0, successful = 0;
            for (Order o : recentOrders) {
                Instant c = o.getCreatedAt();
                if (c != null && !c.isBefore(monthStart) && !c.isAfter(monthEnd)) {
                    total++;
                    if ("success".equalsIgnoreCase(o.getStatus())) successful++;
                }
            }

            results.add(OrderChartDTO.builder()
                    .month(targetMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                    .total(total)
                    .successful(successful)
                    .build());
        }

        return results;
    }

    public OrderDTO toDTO(Order o) {
        if (o == null) return null;
        return OrderDTO.builder()
                .id(o.getId()).userId(o.getUserId()).email(o.getEmail())
                .amount(o.getAmount()).status(o.getStatus()).products(o.getProducts())
                .createdAt(o.getCreatedAt()).updatedAt(o.getUpdatedAt())
                .build();
    }
}
