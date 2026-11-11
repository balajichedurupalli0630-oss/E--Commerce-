package com.omnicart.navigation;

import com.omnicart.config.SessionManager;
import com.omnicart.controller.ProductDetailsController;
import com.omnicart.controller.ProductsController;
import com.omnicart.controller.ReturnController;
import com.omnicart.model.Product;
import com.omnicart.service.ToastService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NavigationManager {
    private static final NavigationManager INSTANCE = new NavigationManager();

    private Stage primaryStage;
    private BorderPane clientRoot;
    private BorderPane adminRoot;
    private StackPane contentArea;

    private boolean isAdminPortal = false;
    private ViewType currentView = ViewType.HOME;

    // View parameter stores
    private Product selectedProduct;
    private String selectedCategory;
    private String searchQuery;
    private String sortOrder;
    private String currentSessionId;

    private NavigationManager() {}

    public static NavigationManager getInstance() {
        return INSTANCE;
    }

    public void init(Stage stage) {
        this.primaryStage = stage;
        ToastService.setPrimaryStage(stage);
        showPortalSelector();
    }

    public void showPortalSelector() {
        this.isAdminPortal = false;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/omnicart/fxml/portal-selector.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            double w = Math.min(900, screenBounds.getWidth());
            double h = Math.min(580, screenBounds.getHeight());

            javafx.scene.Scene scene = new javafx.scene.Scene(root, w, h);
            scene.getStylesheets().add(
                    getClass().getResource("/com/omnicart/css/common.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("OmniCart — Select Portal");
            primaryStage.setMinWidth(760);
            primaryStage.setMinHeight(480);
            primaryStage.setMaximized(false);
            primaryStage.centerOnScreen();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public boolean isAdminPortal() {
        return isAdminPortal;
    }

    public ViewType getCurrentView() {
        return currentView;
    }

    public void showClientApp() {
        this.isAdminPortal = false;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/omnicart/fxml/main-layout.fxml"));
            Parent root = loader.load();

            // Detect screen size and set scene accordingly
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            double sceneWidth = Math.min(1280, screenBounds.getWidth());
            double sceneHeight = Math.min(800, screenBounds.getHeight());

            Scene scene = new Scene(root, sceneWidth, sceneHeight);
            scene.getStylesheets().addAll(
                    getClass().getResource("/com/omnicart/css/common.css").toExternalForm(),
                    getClass().getResource("/com/omnicart/css/client.css").toExternalForm()
            );

            primaryStage.setScene(scene);
            primaryStage.setTitle("OmniCart - Modern E-Commerce Platform");
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);

            // Maximize to fill screen on startup
            primaryStage.setMaximized(true);

            navigateTo(ViewType.HOME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAdminApp() {
        // Enforce role-based authorization
        if (!SessionManager.getInstance().isAdmin()) {
            ToastService.showError("Access Denied: Administrator role required to access Admin Portal");
            return;
        }

        this.isAdminPortal = true;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/omnicart/fxml/admin/admin-layout.fxml"));
            Parent root = loader.load();

            // Detect screen size and set scene accordingly
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            double sceneWidth = Math.min(1280, screenBounds.getWidth());
            double sceneHeight = Math.min(800, screenBounds.getHeight());

            Scene scene = new Scene(root, sceneWidth, sceneHeight);
            scene.getStylesheets().addAll(
                    getClass().getResource("/com/omnicart/css/common.css").toExternalForm(),
                    getClass().getResource("/com/omnicart/css/admin.css").toExternalForm()
            );

            primaryStage.setScene(scene);
            primaryStage.setTitle("OmniCart Admin - Dashboard & Control Panel");
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);

            // Maximize to fill screen on startup
            primaryStage.setMaximized(true);

            navigateTo(ViewType.ADMIN_DASHBOARD);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void navigateTo(ViewType viewType) {
        this.currentView = viewType;
        if (contentArea == null) return;

        String fxmlPath = getFxmlPath(viewType);
        if (fxmlPath == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();

            // Pass parameters to controllers
            if (controller instanceof ProductDetailsController && selectedProduct != null) {
                ((ProductDetailsController) controller).setProduct(selectedProduct);
            } else if (controller instanceof ProductsController) {
                ((ProductsController) controller).setFilter(selectedCategory, searchQuery, sortOrder);
            } else if (controller instanceof ReturnController && currentSessionId != null) {
                ((ReturnController) controller).setSessionId(currentSessionId);
            }

            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("Failed to navigate to view " + viewType + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void navigateToProducts(String category, String search, String sort) {
        this.selectedCategory = category;
        this.searchQuery = search;
        this.sortOrder = sort;
        navigateTo(ViewType.PRODUCTS);
    }

    public void navigateToProductDetails(Product product) {
        this.selectedProduct = product;
        navigateTo(ViewType.PRODUCT_DETAILS);
    }

    public void navigateToReturn(String sessionId) {
        this.currentSessionId = sessionId;
        navigateTo(ViewType.RETURN);
    }

    private String getFxmlPath(ViewType viewType) {
        return switch (viewType) {
            case HOME -> "/com/omnicart/fxml/home-view.fxml";
            case PRODUCTS -> "/com/omnicart/fxml/products-view.fxml";
            case PRODUCT_DETAILS -> "/com/omnicart/fxml/product-details-view.fxml";
            case CART -> "/com/omnicart/fxml/cart-view.fxml";
            case RETURN -> "/com/omnicart/fxml/return-view.fxml";
            case ORDERS -> "/com/omnicart/fxml/orders-view.fxml";
            case ADMIN_DASHBOARD -> "/com/omnicart/fxml/admin/admin-dashboard-view.fxml";
            case ADMIN_PRODUCTS -> "/com/omnicart/fxml/admin/admin-products-view.fxml";
            case ADMIN_ORDERS -> "/com/omnicart/fxml/admin/admin-orders-view.fxml";
            case ADMIN_USERS -> "/com/omnicart/fxml/admin/admin-users-view.fxml";
        };
    }
}
