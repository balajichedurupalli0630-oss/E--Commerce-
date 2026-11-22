package com.ecom.auth.model;

import com.ecom.dto.UserDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthUser {
    private String id;
    private String username;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String imageUrl;
    private Boolean banned = false;
    private long createdAt;

    public AuthUser() {}

    public AuthUser(String id, String username, String passwordHash, String firstName, String lastName, String email, String role, String imageUrl) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role != null ? role : "user";
        this.imageUrl = imageUrl != null ? imageUrl : "/users/1.png";
        this.banned = false;
        this.createdAt = System.currentTimeMillis();
    }

    public UserDTO toUserDTO() {
        return UserDTO.builder()
                .id(id)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .emailAddresses(List.of(Map.of("email_address", email != null ? email : "", "id", "email_" + id)))
                .publicMetadata(Map.of("role", role != null ? role : "user"))
                .imageUrl(imageUrl)
                .banned(banned)
                .build();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getBanned() { return banned; }
    public void setBanned(Boolean banned) { this.banned = banned; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
