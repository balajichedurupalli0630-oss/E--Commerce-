package com.ecom.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private List<Map<String, Object>> emailAddresses;
    private Map<String, Object> publicMetadata;
    private String imageUrl;
    private Boolean banned;

    public UserDTO() {}

    public UserDTO(String id, String username, String firstName, String lastName, List<Map<String, Object>> emailAddresses, Map<String, Object> publicMetadata) {
        this(id, username, firstName, lastName, emailAddresses, publicMetadata, null, false);
    }

    public UserDTO(String id, String username, String firstName, String lastName, List<Map<String, Object>> emailAddresses, Map<String, Object> publicMetadata, String imageUrl, Boolean banned) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddresses = emailAddresses;
        this.publicMetadata = publicMetadata;
        this.imageUrl = imageUrl;
        this.banned = banned;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String username;
        private String firstName;
        private String lastName;
        private List<Map<String, Object>> emailAddresses;
        private Map<String, Object> publicMetadata;
        private String imageUrl;
        private Boolean banned = false;

        public Builder id(String id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder emailAddresses(List<Map<String, Object>> emailAddresses) { this.emailAddresses = emailAddresses; return this; }
        public Builder publicMetadata(Map<String, Object> publicMetadata) { this.publicMetadata = publicMetadata; return this; }
        public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public Builder banned(Boolean banned) { this.banned = banned; return this; }

        public UserDTO build() {
            return new UserDTO(id, username, firstName, lastName, emailAddresses, publicMetadata, imageUrl, banned);
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
}
