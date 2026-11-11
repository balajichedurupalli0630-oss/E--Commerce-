package com.omnicart.config;

import com.omnicart.model.ShippingAddress;
import com.omnicart.model.User;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();

    private final ObjectProperty<User> currentUser = new SimpleObjectProperty<>(null);
    private String authToken;
    private ShippingAddress lastShippingAddress = new ShippingAddress(
            "John Doe", "customer@example.com", "+1 234 567 8900", "123 Market Street", "San Francisco"
    );

    public interface AuthListener {
        void onAuthStateChanged(User user);
    }

    private final List<AuthListener> listeners = new ArrayList<>();

    private SessionManager() {
        // By default initialize with guest or test customer session
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public ObjectProperty<User> currentUserProperty() {
        return currentUser;
    }

    public User getCurrentUser() {
        return currentUser.get();
    }

    public boolean isAuthenticated() {
        return currentUser.get() != null;
    }

    public boolean isAdmin() {
        User user = currentUser.get();
        return user != null && user.isAdmin();
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setSession(User user, String token) {
        this.currentUser.set(user);
        this.authToken = token;
        if (user != null) {
            String name = (user.getFullName() != null && !user.getFullName().isBlank())
                    ? user.getFullName() : user.getUsername();
            String email = (user.getPrimaryEmail() != null && !user.getPrimaryEmail().isBlank())
                    ? user.getPrimaryEmail() : (user.getUsername() + "@example.com");
            this.lastShippingAddress = new ShippingAddress(name, email, "+91 98765 43210", "42 Park Avenue", "Mumbai");
        }
        notifyListeners(user);
    }

    public void logout() {
        this.currentUser.set(null);
        this.authToken = null;
        notifyListeners(null);
    }

    public ShippingAddress getLastShippingAddress() {
        return lastShippingAddress;
    }

    public void setLastShippingAddress(ShippingAddress lastShippingAddress) {
        this.lastShippingAddress = lastShippingAddress;
    }

    public void addAuthListener(AuthListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeAuthListener(AuthListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(User user) {
        for (AuthListener listener : listeners) {
            try {
                listener.onAuthStateChanged(user);
            } catch (Exception ignored) {}
        }
    }
}
