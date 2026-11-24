package com.ecom.event;

public record UserCreatedEvent(String username, String email) {
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String username;
        private String email;

        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public UserCreatedEvent build() { return new UserCreatedEvent(username, email); }
    }

    public String getUsername() { return username(); }
    public String getEmail() { return email(); }
}
