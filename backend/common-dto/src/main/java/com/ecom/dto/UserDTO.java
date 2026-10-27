package com.ecom.dto;

import java.util.List;
import java.util.Map;

public class UserDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private List<Map<String, Object>> emailAddresses;
    private Map<String, Object> publicMetadata;

    public UserDTO() {}

    public UserDTO(String id, String username, String firstName, String lastName, List<Map<String, Object>> emailAddresses, Map<String, Object> publicMetadata) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddresses = emailAddresses;
        this.publicMetadata = publicMetadata;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String username;
        private String firstName;
        private String lastName;
        private List<Map<String, Object>> emailAddresses;
        private Map<String, Object> publicMetadata;

        public Builder id(String id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder emailAddresses(List<Map<String, Object>> emailAddresses) { this.emailAddresses = emailAddresses; return this; }
        public Builder publicMetadata(Map<String, Object> publicMetadata) { this.publicMetadata = publicMetadata; return this; }

        public UserDTO build() {
            return new UserDTO(id, username, firstName, lastName, emailAddresses, publicMetadata);
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
}
