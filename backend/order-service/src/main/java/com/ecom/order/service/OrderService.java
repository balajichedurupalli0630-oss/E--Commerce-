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
import java.util.stream.Collectors;

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
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrders(Integer limit) {
        int pageSize = (limit != null && limit > 0) ? limit : 50;
        return orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, pageSize)).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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

        // Publish order.created to Kafka
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

    public List<OrderChartDTO> getOrderChart() {
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        YearMonth currentMonth = YearMonth.from(now);

        Instant sixMonthsAgo = currentMonth.minusMonths(5).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant nowInstant = Instant.now();

        List<Order> recentOrders = orderRepository.findByCreatedAtBetween(sixMonthsAgo, nowInstant);

        List<OrderChartDTO> results = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            Instant monthStart = targetMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant monthEnd = targetMonth.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

            long total = recentOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().isBefore(monthStart) && !o.getCreatedAt().isAfter(monthEnd))
                    .count();

            long successful = recentOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().isBefore(monthStart) && !o.getCreatedAt().isAfter(monthEnd))
                    .filter(o -> "success".equalsIgnoreCase(o.getStatus()))
                    .count();

            String monthName = targetMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            results.add(OrderChartDTO.builder()
                    .month(monthName)
                    .total(total)
                    .successful(successful)
                    .build());
        }

        return results;
    }

    public OrderDTO toDTO(Order order) {
        if (order == null) return null;
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .email(order.getEmail())
                .amount(order.getAmount())
                .status(order.getStatus())
                .products(order.getProducts())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
