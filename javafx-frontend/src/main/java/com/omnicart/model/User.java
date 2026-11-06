package com.omnicart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private List<Map<String, Object>> emailAddresses;
    private Map<String, Object> publicMetadata;
    private String imageUrl;
    private Boolean banned;

    public User() {}

    public User(String id, String username, String firstName, String lastName, String email, String role) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        if (email != null) {
            this.emailAddresses = List.of(Map.of("email_address", email));
        }
        if (role != null) {
            this.publicMetadata = Map.of("role", role);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public List<Map<String, Object>> getEmailAddresses() { return emailAddresses; }
    public void setEmailAddresses(List<Map<String, Object>> emailAddresses) { this.emailAddresses = emailAddresses; }

    public Map<String, Object> getPublicMetadata() { return publicMetadata; }
    public void setPublicMetadata(Map<String, Object> publicMetadata) { this.publicMetadata = publicMetadata; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getBanned() { return banned; }
    public void setBanned(Boolean banned) { this.banned = banned; }

    public String getFullName() {
        if (firstName != null && !firstName.isBlank()) {
            return firstName + (lastName != null && !lastName.isBlank() ? " " + lastName : "");
        }
        if (username != null && !username.isBlank()) {
            return username;
        }
        return "Customer";
    }

    public String getPrimaryEmail() {
        if (emailAddresses != null && !emailAddresses.isEmpty()) {
            Object email = emailAddresses.get(0).get("email_address");
            if (email != null) return email.toString();
        }
        return "customer@omnicart.com";
    }

    public String getRole() {
        if (publicMetadata != null && publicMetadata.containsKey("role")) {
            Object role = publicMetadata.get("role");
            if (role != null) return role.toString();
        }
        return "user";
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(getRole());
    }

    public String getStatus() {
        return Boolean.TRUE.equals(banned) ? "banned" : "active";
    }
}
