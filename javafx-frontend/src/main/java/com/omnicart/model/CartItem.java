package com.omnicart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItem {
    private Long id;
    private String name;
    private String shortDescription;
    private String description;
    private Double price;
    private List<String> sizes;
    private List<String> colors;
    private Map<String, String> images = new HashMap<>();
    private String categorySlug;
    private Integer quantity = 1;
    private String selectedSize;
    private String selectedColor;

    public CartItem() {}

    public CartItem(Product p, int quantity, String selectedSize, String selectedColor) {
        if (p != null) {
            this.id = p.getId(); this.name = p.getName(); this.shortDescription = p.getShortDescription();
            this.description = p.getDescription(); this.price = p.getEffectivePrice();
            this.sizes = p.getSizes(); this.colors = p.getColors(); this.images = p.getImages();
            this.categorySlug = p.getCategorySlug();
        }
        this.quantity = quantity;
        this.selectedSize = selectedSize;
        this.selectedColor = selectedColor;
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
    public void setSizes(List<String> sizes) { this.sizes = sizes; }
    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }
    public Map<String, String> getImages() { return images; }
    public void setImages(Map<String, String> images) { this.images = images; }
    public String getCategorySlug() { return categorySlug; }
    public void setCategorySlug(String categorySlug) { this.categorySlug = categorySlug; }
    public Integer getQuantity() { return quantity != null ? quantity : 1; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getSelectedSize() { return selectedSize; }
    public void setSelectedSize(String selectedSize) { this.selectedSize = selectedSize; }
    public String getSelectedColor() { return selectedColor; }
    public void setSelectedColor(String selectedColor) { this.selectedColor = selectedColor; }

    public double getEffectivePrice() { return price != null ? price : 0.0; }
    public double getSubtotal() { return getEffectivePrice() * getQuantity(); }

    public String getDisplayImage() {
        if (images != null && selectedColor != null && images.containsKey(selectedColor)) return images.get(selectedColor);
        if (images != null && !images.isEmpty()) return images.values().iterator().next();
        return "/products/1g.png";
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof CartItem c && Objects.equals(id, c.id) && Objects.equals(selectedSize, c.selectedSize) && Objects.equals(selectedColor, c.selectedColor));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, selectedSize, selectedColor);
    }
}
