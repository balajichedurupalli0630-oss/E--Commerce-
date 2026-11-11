package com.omnicart;

import com.omnicart.api.AuthApiService;
import com.omnicart.api.OrderApiService;
import com.omnicart.api.PaymentApiService;
import com.omnicart.api.ProductApiService;
import com.omnicart.model.Product;
import com.omnicart.service.CartService;
import com.omnicart.service.ImageLoaderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

public class SmokeTest {

    @Test
    public void testResourcesExist() {
        String[] fxmlFiles = {
                "/com/omnicart/fxml/main-layout.fxml",
                "/com/omnicart/fxml/home-view.fxml",
                "/com/omnicart/fxml/products-view.fxml",
                "/com/omnicart/fxml/product-details-view.fxml",
                "/com/omnicart/fxml/cart-view.fxml",
                "/com/omnicart/fxml/return-view.fxml",
                "/com/omnicart/fxml/orders-view.fxml",
                "/com/omnicart/fxml/login-dialog.fxml",
                "/com/omnicart/fxml/admin/admin-layout.fxml",
                "/com/omnicart/fxml/admin/admin-dashboard-view.fxml",
                "/com/omnicart/fxml/admin/admin-products-view.fxml",
                "/com/omnicart/fxml/admin/admin-orders-view.fxml",
                "/com/omnicart/fxml/admin/admin-users-view.fxml",
                "/com/omnicart/fxml/admin/add-product-dialog.fxml",
                "/com/omnicart/fxml/admin/add-category-dialog.fxml",
                "/com/omnicart/fxml/admin/add-user-dialog.fxml"
        };

        for (String fxml : fxmlFiles) {
            InputStream in = getClass().getResourceAsStream(fxml);
            Assertions.assertNotNull(in, "FXML file must exist in resources: " + fxml);
        }

        String[] cssFiles = {
                "/com/omnicart/css/common.css",
                "/com/omnicart/css/client.css",
                "/com/omnicart/css/admin.css"
        };

        for (String css : cssFiles) {
            InputStream in = getClass().getResourceAsStream(css);
            Assertions.assertNotNull(in, "CSS file must exist in resources: " + css);
        }

        String[] imgFiles = {
                "/com/omnicart/images/logo.png",
                "/com/omnicart/images/featured.png",
                "/com/omnicart/images/cards.png",
                "/com/omnicart/images/klarna.png",
                "/com/omnicart/images/stripe.png",
                "/com/omnicart/images/products/1g.png"
        };

        for (String img : imgFiles) {
            InputStream in = getClass().getResourceAsStream(img);
            Assertions.assertNotNull(in, "Image file must exist in resources: " + img);
        }
    }

    @Test
    public void testCartOperations() {
        CartService cart = CartService.getInstance();
        cart.clearCart();
        Assertions.assertEquals(0, cart.getTotalItemCount());

        Product p = new Product(100L, "Test Sneaker", "Comfortable", "Desc", 49.99,
                List.of("M"), List.of("blue"), null, "shoes", null);

        cart.addToCart(p, 2, "M", "blue");
        Assertions.assertEquals(2, cart.getTotalItemCount());
        Assertions.assertEquals(99.98, cart.getSubtotal(), 0.01);

        cart.clearCart();
        Assertions.assertEquals(0, cart.getTotalItemCount());
    }

    @Test
    public void testApiServicesFallback() throws Exception {
        var products = ProductApiService.getInstance().getProducts("newest", null, null, 10).get();
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());

        var categories = ProductApiService.getInstance().getCategories().get();
        Assertions.assertNotNull(categories);
        Assertions.assertFalse(categories.isEmpty());

        var chart = OrderApiService.getInstance().getOrderChart().get();
        Assertions.assertNotNull(chart);
        Assertions.assertFalse(chart.isEmpty());

        var users = AuthApiService.getInstance().getUsers().get();
        Assertions.assertNotNull(users);
        Assertions.assertFalse(users.isEmpty());
    }
}
