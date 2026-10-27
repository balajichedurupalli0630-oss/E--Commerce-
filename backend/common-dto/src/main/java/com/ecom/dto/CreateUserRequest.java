package com.ecom.dto;

import java.util.List;

public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private List<String> emailAddress;

    public CreateUserRequest() {}

    public CreateUserRequest(String firstName, String lastName, String username, String password, List<String> emailAddress) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.emailAddress = emailAddress;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String firstName;
        private String lastName;
        private String username;
        private String password;
        private List<String> emailAddress;

        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder emailAddress(List<String> emailAddress) { this.emailAddress = emailAddress; return this; }

        public CreateUserRequest build() {
            return new CreateUserRequest(firstName, lastName, username, password, emailAddress);
        }
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<String> getEmailAddress() { return emailAddress; }
    public void setEmailAddress(List<String> emailAddress) { this.emailAddress = emailAddress; }
}
