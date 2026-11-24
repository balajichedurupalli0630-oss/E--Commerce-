package com.ecom.event;

public record ProductCreatedEvent(String id, String name, Integer price) {
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String name;
        private Integer price;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder price(Integer price) { this.price = price; return this; }
        public ProductCreatedEvent build() { return new ProductCreatedEvent(id, name, price); }
    }

    public String getId() { return id(); }
    public String getName() { return name(); }
    public Integer getPrice() { return price(); }
}
