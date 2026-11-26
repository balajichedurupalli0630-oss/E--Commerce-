package com.ecom.order.model;

import com.ecom.dto.OrderItemDTO;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_orders_created_at", columnList = "created_at")
})
public class Order {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "status", length = 50)
    private String status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Order() {}

    @PrePersist
    public void onPrePersist() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString().replace("-", "");
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        for (OrderItem item : this.items) {
            item.setOrder(this);
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addItem(OrderItem item) {
        if (this.items == null) this.items = new ArrayList<>();
        this.items.add(item);
        item.setOrder(this);
    }

    public List<OrderItemDTO> getProducts() {
        if (items != null && !items.isEmpty()) {
            return items.stream()
                    .map(i -> new OrderItemDTO(i.getProductId(), i.getName(), i.getQuantity(), i.getPrice()))
                    .toList();
        }
        return new ArrayList<>();
    }

    public void setProducts(List<OrderItemDTO> dtoList) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        } else {
            this.items.clear();
        }
        if (dtoList != null) {
            for (OrderItemDTO dto : dtoList) {
                addItem(OrderItem.builder()
                        .productId(dto.productId())
                        .name(dto.name())
                        .quantity(dto.quantity())
                        .price(dto.price())
                        .build());
            }
        }
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String userId;
        private String email;
        private Integer amount;
        private String status;
        private List<OrderItemDTO> products = new ArrayList<>();
        private List<OrderItem> items = new ArrayList<>();
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder amount(Integer amount) { this.amount = amount; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder products(List<OrderItemDTO> products) { this.products = products; return this; }
        public Builder items(List<OrderItem> items) { this.items = items; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Order build() {
            Order order = new Order();
            order.id = this.id;
            order.userId = this.userId;
            order.email = this.email;
            order.amount = this.amount;
            order.status = this.status;
            order.createdAt = this.createdAt;
            order.updatedAt = this.updatedAt;
            if (this.items != null && !this.items.isEmpty()) {
                for (OrderItem it : this.items) {
                    order.addItem(it);
                }
            } else if (this.products != null && !this.products.isEmpty()) {
                order.setProducts(this.products);
            }
            return order;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
