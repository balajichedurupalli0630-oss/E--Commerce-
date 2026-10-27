package com.ecom.dto;

public class CheckoutSessionResponse {
    private String checkoutSessionClientSecret;

    public CheckoutSessionResponse() {}

    public CheckoutSessionResponse(String checkoutSessionClientSecret) {
        this.checkoutSessionClientSecret = checkoutSessionClientSecret;
    }

    public String getCheckoutSessionClientSecret() { return checkoutSessionClientSecret; }
    public void setCheckoutSessionClientSecret(String checkoutSessionClientSecret) {
        this.checkoutSessionClientSecret = checkoutSessionClientSecret;
    }
}
