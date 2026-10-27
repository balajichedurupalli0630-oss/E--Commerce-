package com.ecom.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ProductDTO {
    private Long id;
    private String name;
    private String shortDescription;
    private String description;
    private Integer price;
    private List<String> sizes;
    private List<String> colors;
    private Map<String, String> images;
    private String categorySlug;
    private CategoryDTO category;
    private Instant createdAt;
    private Instant updatedAt;

    public ProductDTO() {}

    public ProductDTO(Long id, String name, String shortDescription, String description, Integer price,
                      List<String> sizes, List<String> colors, Map<String, String> images,
                      String categorySlug, CategoryDTO category, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.shortDescription = shortDescription;
        this.description = description;
        this.price = price;
        this.sizes = sizes;
        this.colors = colors;
        this.images = images;
        this.categorySlug = categorySlug;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String shortDescription;
        private String description;
        private Integer price;
        private List<String> sizes;
        private List<String> colors;
        private Map<String, String> images;
        private String categorySlug;
        private CategoryDTO category;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder shortDescription(String shortDescription) { this.shortDescription = shortDescription; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder price(Integer price) { this.price = price; return this; }
        public Builder sizes(List<String> sizes) { this.sizes = sizes; return this; }
        public Builder colors(List<String> colors) { this.colors = colors; return this; }
        public Builder images(Map<String, String> images) { this.images = images; return this; }
        public Builder categorySlug(String categorySlug) { this.categorySlug = categorySlug; return this; }
        public Builder category(CategoryDTO category) { this.category = category; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public ProductDTO build() {
            return new ProductDTO(id, name, shortDescription, description, price, sizes, colors, images, categorySlug, category, createdAt, updatedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public List<String> getSizes() { return sizes; }
    public void setSizes(List<String> sizes) { this.sizes = sizes; }
    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }
    public Map<String, String> getImages() { return images; }
    public void setImages(Map<String, String> images) { this.images = images; }
    public String getCategorySlug() { return categorySlug; }
    public void setCategorySlug(String categorySlug) { this.categorySlug = categorySlug; }
    public CategoryDTO getCategory() { return category; }
    public void setCategory(CategoryDTO category) { this.category = category; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
