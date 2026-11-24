package com.ecom.dto;

public record AuthResponseDTO(String token, UserDTO user) {
    public String getToken() { return token(); }
    public UserDTO getUser() { return user(); }
}
