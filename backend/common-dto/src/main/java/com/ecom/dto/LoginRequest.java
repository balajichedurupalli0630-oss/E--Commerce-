package com.ecom.dto;

public record LoginRequest(String identifier, String password) {
    public String getIdentifier() { return identifier(); }
    public String getPassword() { return password(); }
}
