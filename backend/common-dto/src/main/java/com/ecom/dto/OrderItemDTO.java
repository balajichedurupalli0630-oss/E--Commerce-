package com.ecom.dto;

public class OrderItemDTO {
    private String name;
    private Integer quantity;
    private Integer price;

    public OrderItemDTO() {}

    public OrderItemDTO(String name, Integer quantity, Integer price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private Integer quantity;
        private Integer price;

        public Builder name(String name) { this.name = name; return this; }
        public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public Builder price(Integer price) { this.price = price; return this; }

        public OrderItemDTO build() {
            return new OrderItemDTO(name, quantity, price);
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
