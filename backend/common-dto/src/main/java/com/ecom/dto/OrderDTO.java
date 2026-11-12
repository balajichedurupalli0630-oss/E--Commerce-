package com.ecom.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public class OrderDTO {
    @JsonProperty("_id")
    @JsonAlias({"id", "_id"})
    private String id;
    private String userId;
    private String email;
    private Integer amount;
    private String status;
    private List<OrderItemDTO> products;
    private Instant createdAt;
    private Instant updatedAt;

    public OrderDTO() {}

    public OrderDTO(String id, String userId, String email, Integer amount, String status, List<OrderItemDTO> products, Instant createdAt, Instant updatedAt) {
        this.id = id; this.userId = userId; this.email = email; this.amount = amount;
        this.status = status; this.products = products; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final OrderDTO dto = new OrderDTO();

        public Builder id(String id) { dto.id = id; return this; }
        public Builder userId(String userId) { dto.userId = userId; return this; }
        public Builder email(String email) { dto.email = email; return this; }
        public Builder amount(Integer amount) { dto.amount = amount; return this; }
        public Builder status(String status) { dto.status = status; return this; }
        public Builder products(List<OrderItemDTO> products) { dto.products = products; return this; }
        public Builder createdAt(Instant createdAt) { dto.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { dto.updatedAt = updatedAt; return this; }

        public OrderDTO build() { return dto; }
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
    public List<OrderItemDTO> getProducts() { return products; }
    public void setProducts(List<OrderItemDTO> products) { this.products = products; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
