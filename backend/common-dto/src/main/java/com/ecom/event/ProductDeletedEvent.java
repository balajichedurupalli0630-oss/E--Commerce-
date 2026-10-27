package com.ecom.event;

public class ProductDeletedEvent {
    private Long id;

    public ProductDeletedEvent() {}

    public ProductDeletedEvent(Long id) {
        this.id = id;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;

        public Builder id(Long id) { this.id = id; return this; }

        public ProductDeletedEvent build() {
            return new ProductDeletedEvent(id);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
