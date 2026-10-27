package com.ecom.event;

public class OrderCreatedEvent {
    private String email;
    private Integer amount;
    private String status;

    public OrderCreatedEvent() {}

    public OrderCreatedEvent(String email, Integer amount, String status) {
        this.email = email;
        this.amount = amount;
        this.status = status;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String email;
        private Integer amount;
        private String status;

        public Builder email(String email) { this.email = email; return this; }
        public Builder amount(Integer amount) { this.amount = amount; return this; }
        public Builder status(String status) { this.status = status; return this; }

        public OrderCreatedEvent build() {
            return new OrderCreatedEvent(email, amount, status);
        }
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
