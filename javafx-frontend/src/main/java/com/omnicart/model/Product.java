package com.omnicart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private Long id;
    private String name;
    private String shortDescription;
    private String description;
    private Double price;
    private List<String> sizes = new ArrayList<>();
    private List<String> colors = new ArrayList<>();
    private Map<String, String> images = new HashMap<>();
    private String categorySlug;
    private Category category;
    private Instant createdAt;
    private Instant updatedAt;

    public Product() {}

    public Product(Long id, String name, String shortDescription, String description, Double price,
                   List<String> sizes, List<String> colors, Map<String, String> images,
                   String categorySlug, Category category) {
        this.id = id;
        this.name = name;
        this.shortDescription = shortDescription;
        this.description = description;
        this.price = price;
        if (sizes != null) this.sizes = sizes;
        if (colors != null) this.colors = colors;
        if (images != null) this.images = images;
        this.categorySlug = categorySlug;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public List<String> getSizes() { return sizes; }
    public void setSizes(List<String> sizes) { this.sizes = sizes != null ? sizes : new ArrayList<>(); }

    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors != null ? colors : new ArrayList<>(); }

    public Map<String, String> getImages() { return images; }
    public void setImages(Map<String, String> images) { this.images = images != null ? images : new HashMap<>(); }

    public String getCategorySlug() { return categorySlug; }
    public void setCategorySlug(String categorySlug) { this.categorySlug = categorySlug; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Resolves display price in INR (if backend returned cents > 1000, adjust to decimal, else decimal directly).
     */
    public double getEffectivePrice() {
        if (price == null) return 0.0;
        return price;
    }

    public String getImageForColor(String color) {
        if (images == null || images.isEmpty()) {
            return "/products/1g.png";
        }
        if (color != null && images.containsKey(color)) {
            return images.get(color);
        }
        return images.values().iterator().next();
    }
}
