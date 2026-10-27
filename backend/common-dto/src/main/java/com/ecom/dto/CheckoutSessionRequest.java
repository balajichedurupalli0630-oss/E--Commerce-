package com.ecom.dto;

import java.util.List;

public class CheckoutSessionRequest {
    private List<CartItemDTO> cart;

    public CheckoutSessionRequest() {}

    public CheckoutSessionRequest(List<CartItemDTO> cart) {
        this.cart = cart;
    }

    public List<CartItemDTO> getCart() { return cart; }
    public void setCart(List<CartItemDTO> cart) { this.cart = cart; }
}
