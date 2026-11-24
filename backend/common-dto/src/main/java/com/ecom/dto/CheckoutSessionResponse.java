package com.ecom.dto;

public record CheckoutSessionResponse(String checkoutSessionClientSecret) {
    public String getCheckoutSessionClientSecret() { return checkoutSessionClientSecret(); }
}
