package com.omnicart.service;

import com.omnicart.model.CartItem;
import com.omnicart.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CartService {
    private static final CartService INSTANCE = new CartService();

    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    public interface CartChangeListener {
        void onCartChanged();
    }

    private final java.util.List<CartChangeListener> listeners = new java.util.ArrayList<>();

    private CartService() {}

    public static CartService getInstance() {
        return INSTANCE;
    }

    public ObservableList<CartItem> getCartItems() {
        return cartItems;
    }

    public void addToCart(Product product, int quantity, String selectedSize, String selectedColor) {
        if (product == null) return;
        if (quantity <= 0) quantity = 1;

        // Check if matching item exists
        for (CartItem item : cartItems) {
            if (item.getId().equals(product.getId())
                    && java.util.Objects.equals(item.getSelectedSize(), selectedSize)
                    && java.util.Objects.equals(item.getSelectedColor(), selectedColor)) {
                item.setQuantity(item.getQuantity() + quantity);
                notifyChange();
                return;
            }
        }

        cartItems.add(new CartItem(product, quantity, selectedSize, selectedColor));
        notifyChange();
    }

    public void removeFromCart(CartItem item) {
        cartItems.remove(item);
        notifyChange();
    }

    public void updateQuantity(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            removeFromCart(item);
        } else {
            item.setQuantity(newQuantity);
            notifyChange();
        }
    }

    public void clearCart() {
        cartItems.clear();
        notifyChange();
    }

    public int getTotalItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    public double getSubtotal() {
        double sum = 0.0;
        for (CartItem item : cartItems) {
            sum += item.getSubtotal();
        }
        return sum;
    }

    public void addListener(CartChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(CartChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyChange() {
        for (CartChangeListener listener : listeners) {
            try {
                listener.onCartChanged();
            } catch (Exception ignored) {}
        }
    }
}
