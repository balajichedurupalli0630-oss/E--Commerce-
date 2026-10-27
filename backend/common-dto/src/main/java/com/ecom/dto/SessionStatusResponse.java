package com.ecom.dto;

public class SessionStatusResponse {
    private String status;
    private String paymentStatus;

    public SessionStatusResponse() {}

    public SessionStatusResponse(String status, String paymentStatus) {
        this.status = status;
        this.paymentStatus = paymentStatus;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
