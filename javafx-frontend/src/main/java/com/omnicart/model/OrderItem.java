package com.omnicart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItem {
    private String name;
    private Integer quantity;
    private Integer price;

    public OrderItem() {}

    public OrderItem(String name, Integer quantity, Integer price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
