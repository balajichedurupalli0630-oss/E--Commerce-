package com.omnicart.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    @JsonProperty("_id")
    @JsonAlias({"id", "_id"})
    private String id;

    private String userId;
    private String email;
    private Integer amount;
    private String status;
    private List<OrderItem> products = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public Order() {}

    public Order(String id, String userId, String email, Integer amount, String status, List<OrderItem> products, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.amount = amount;
        this.status = status;
        if (products != null) this.products = products;
        this.createdAt = createdAt;
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

    public List<OrderItem> getProducts() { return products; }
    public void setProducts(List<OrderItem> products) { this.products = products != null ? products : new ArrayList<>(); }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public double getEffectiveAmount() {
        if (amount == null) return 0.0;
        // Amounts are displayed in thousands of INR (e.g. ₹7,980.00)
        if (amount >= 100000) {
            return amount / 100.0;
        } else if (amount >= 1000) {
            return amount.doubleValue();
        } else {
            return amount * 100.0;
        }
    }

    public String getProductsSummary() {
        if (products == null || products.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < products.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(products.get(i).getName());
        }
        return sb.toString();
    }
}
