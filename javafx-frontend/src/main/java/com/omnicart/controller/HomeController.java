package com.omnicart.controller;

import com.omnicart.api.ProductApiService;
import com.omnicart.model.Category;
import com.omnicart.model.Product;
import com.omnicart.navigation.NavigationManager;
import com.omnicart.navigation.ViewType;
import com.omnicart.service.ImageLoaderService;
import com.omnicart.ui.ProductCardView;
import com.omnicart.util.SvgIcons;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML private ImageView heroBannerImageView;
    @FXML private HBox categoriesBox;
    @FXML private FlowPane productsGrid;
    @FXML private VBox loadingBox;
    @FXML private Label statusLabel;

    private String selectedCategorySlug = "all";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        heroBannerImageView.setImage(ImageLoaderService.loadImage("/featured.png"));
        loadCategories();
        loadProducts();
    }

    private void loadCategories() {
        ProductApiService.getInstance().getCategories().thenAccept(categories -> {
            Platform.runLater(() -> {
                categoriesBox.getChildren().clear();
                for (Category cat : categories) {
                    Button catBtn = createCategoryPill(cat);
                    categoriesBox.getChildren().add(catBtn);
                }
            });
        });
    }

    private Button createCategoryPill(Category cat) {
        Button btn = new Button(cat.getName());
        btn.getStyleClass().add("category-pill");
        if (cat.getSlug().equalsIgnoreCase(selectedCategorySlug)) {
            btn.getStyleClass().add("category-pill-active");
        }

        // Add matching icon
        String iconPath = getCategoryIcon(cat.getSlug());
        btn.setGraphic(SvgIcons.createIcon(iconPath, 14, Color.valueOf("#4b5563")));
        btn.setCursor(Cursor.HAND);

        btn.setOnAction(e -> {
            this.selectedCategorySlug = cat.getSlug();
            for (javafx.scene.Node node : categoriesBox.getChildren()) {
                node.getStyleClass().remove("category-pill-active");
            }
            btn.getStyleClass().add("category-pill-active");
            loadProducts();
        });

        return btn;
    }

    private String getCategoryIcon(String slug) {
        if (slug == null) return SvgIcons.SHOPPING_BAG;
        return switch (slug.toLowerCase()) {
            case "t-shirts", "hoodies", "pullovers", "jackets" -> SvgIcons.SHIRT;
            case "shoes" -> SvgIcons.FOOTPRINTS;
            case "accessories" -> SvgIcons.GLASSES;
            case "bags" -> SvgIcons.BRIEFCASE;
            case "dresses" -> SvgIcons.VENUS;
            default -> SvgIcons.SHOPPING_BAG;
        };
    }

    private void loadProducts() {
        loadingBox.setVisible(true);
        loadingBox.setManaged(true);
        productsGrid.getChildren().clear();

        ProductApiService.getInstance().getProducts("newest", selectedCategorySlug, null, 8)
                .thenAccept(products -> {
                    Platform.runLater(() -> {
                        loadingBox.setVisible(false);
                        loadingBox.setManaged(false);

                        if (products.isEmpty()) {
                            statusLabel.setText("No products found in this category.");
                            statusLabel.setVisible(true);
                        } else {
                            statusLabel.setVisible(false);
                            for (Product p : products) {
                                productsGrid.getChildren().add(new ProductCardView(p));
                            }
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        loadingBox.setVisible(false);
                        loadingBox.setManaged(false);
                        statusLabel.setText("Failed to load products. Please check connection.");
                        statusLabel.setVisible(true);
                    });
                    return null;
                });
    }

    @FXML
    private void onViewAllProductsClicked() {
        NavigationManager.getInstance().navigateToProducts(selectedCategorySlug, null, null);
    }
}
