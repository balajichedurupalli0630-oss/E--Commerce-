package com.ecom.dto;

public record SessionStatusResponse(String status, String paymentStatus) {
    public String getStatus() { return status(); }
    public String getPaymentStatus() { return paymentStatus(); }
}
