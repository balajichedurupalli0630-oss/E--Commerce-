package com.ecom.dto;

public record OrderItemDTO(Long productId, String name, Integer quantity, Integer price) {
    public OrderItemDTO(String name, Integer quantity, Integer price) {
        this(null, name, quantity, price);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long productId;
        private String name;
        private Integer quantity;
        private Integer price;

        public Builder productId(Long productId) { this.productId = productId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public Builder price(Integer price) { this.price = price; return this; }
        public OrderItemDTO build() { return new OrderItemDTO(productId, name, quantity, price); }
    }

    public Long getProductId() { return productId(); }
    public String getName() { return name(); }
    public Integer getQuantity() { return quantity(); }
    public Integer getPrice() { return price(); }
}
