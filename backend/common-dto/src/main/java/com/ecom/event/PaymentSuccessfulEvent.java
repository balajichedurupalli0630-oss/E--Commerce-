package com.ecom.event;

import com.ecom.dto.OrderItemDTO;
import java.util.List;

public record PaymentSuccessfulEvent(
        String userId,
        String email,
        Integer amount,
        String status,
        List<OrderItemDTO> products
) {
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String userId;
        private String email;
        private Integer amount;
        private String status;
        private List<OrderItemDTO> products;

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder amount(Integer amount) { this.amount = amount; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder products(List<OrderItemDTO> products) { this.products = products; return this; }

        public PaymentSuccessfulEvent build() {
            return new PaymentSuccessfulEvent(userId, email, amount, status, products);
        }
    }

    public String getUserId() { return userId(); }
    public String getEmail() { return email(); }
    public Integer getAmount() { return amount(); }
    public String getStatus() { return status(); }
    public List<OrderItemDTO> getProducts() { return products(); }
}
