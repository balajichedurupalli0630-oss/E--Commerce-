package com.ecom.dto;

import java.util.List;

public record CheckoutSessionRequest(List<CartItemDTO> cart) {
    public List<CartItemDTO> getCart() { return cart(); }
}
