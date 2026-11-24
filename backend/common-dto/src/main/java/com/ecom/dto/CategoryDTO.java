package com.ecom.dto;

public record CategoryDTO(Long id, String name, String slug) {
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private String slug;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder slug(String slug) { this.slug = slug; return this; }
        public CategoryDTO build() { return new CategoryDTO(id, name, slug); }
    }

    public Long getId() { return id(); }
    public String getName() { return name(); }
    public String getSlug() { return slug(); }
}
