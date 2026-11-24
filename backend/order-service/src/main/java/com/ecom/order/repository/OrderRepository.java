package com.ecom.order.repository;

import com.ecom.dto.OrderItemDTO;
import com.ecom.order.model.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Order> findByCreatedAtBetween(Instant start, Instant end);
    List<Order> findByCreatedAtGreaterThanEqual(Instant since);

    default List<Object[]> findProductSalesSince(LocalDateTime since) {
        Instant instant = since != null ? since.toInstant(ZoneOffset.UTC) : Instant.EPOCH;
        return findProductSalesSince(instant);
    }

    default List<Object[]> findProductSalesSince(Instant since) {
        List<Order> orders = findByCreatedAtGreaterThanEqual(since);
        Map<Long, Integer> salesByProduct = new HashMap<>();
        for (Order o : orders) {
            String status = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
            if ("SUCCESS".equals(status) || "PAID".equals(status) || "SHIPPED".equals(status) || "DELIVERED".equals(status)) {
                if (o.getProducts() != null) {
                    for (OrderItemDTO item : o.getProducts()) {
                        if (item.getProductId() != null) {
                            int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                            salesByProduct.merge(item.getProductId(), qty, Integer::sum);
                        }
                    }
                }
            }
        }
        return salesByProduct.entrySet().stream()
                .map(e -> new Object[]{ e.getKey(), e.getValue() })
                .toList();
    }
}
