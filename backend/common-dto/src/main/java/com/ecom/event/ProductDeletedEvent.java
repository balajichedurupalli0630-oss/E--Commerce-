package com.ecom.event;

public record ProductDeletedEvent(Long id) {
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        public Builder id(Long id) { this.id = id; return this; }
        public ProductDeletedEvent build() { return new ProductDeletedEvent(id); }
    }

    public Long getId() { return id(); }
}
