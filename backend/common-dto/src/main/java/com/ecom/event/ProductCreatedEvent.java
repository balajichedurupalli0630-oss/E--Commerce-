package com.ecom.event;

public class ProductCreatedEvent {
    private String id;
    private String name;
    private Integer price;

    public ProductCreatedEvent() {}

    public ProductCreatedEvent(String id, String name, Integer price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String name;
        private Integer price;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder price(Integer price) { this.price = price; return this; }

        public ProductCreatedEvent build() {
            return new ProductCreatedEvent(id, name, price);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
