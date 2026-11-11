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

    public CartItem(Product product, int quantity, String selectedSize, String selectedColor) {
        if (product != null) {
            this.id = product.getId();
            this.name = product.getName();
            this.shortDescription = product.getShortDescription();
            this.description = product.getDescription();
            this.price = product.getEffectivePrice();
            this.sizes = product.getSizes();
            this.colors = product.getColors();
            this.images = product.getImages();
            this.categorySlug = product.getCategorySlug();
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

    public double getEffectivePrice() {
        if (price == null) return 0.0;
        return price;
    }

    public double getSubtotal() {
        return getEffectivePrice() * getQuantity();
    }

    public String getDisplayImage() {
        if (images != null && selectedColor != null && images.containsKey(selectedColor)) {
            return images.get(selectedColor);
        }
        if (images != null && !images.isEmpty()) {
            return images.values().iterator().next();
        }
        return "/products/1g.png";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(id, cartItem.id) &&
                Objects.equals(selectedSize, cartItem.selectedSize) &&
                Objects.equals(selectedColor, cartItem.selectedColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, selectedSize, selectedColor);
    }
}
