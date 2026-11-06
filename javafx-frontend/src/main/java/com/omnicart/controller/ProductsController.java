package com.omnicart.controller;

import com.omnicart.api.ProductApiService;
import com.omnicart.model.Category;
import com.omnicart.model.Product;
import com.omnicart.ui.ProductCardView;
import com.omnicart.util.SvgIcons;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class ProductsController implements Initializable {

    @FXML private HBox categoriesBox;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private FlowPane productsGrid;
    @FXML private VBox loadingBox;
    @FXML private Label statusLabel;
    @FXML private Label searchNoticeLabel;

    private String currentCategory = "all";
    private String currentSearch = null;
    private String currentSort = "newest";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sortComboBox.getItems().addAll("Newest", "Oldest", "Price: Low to High", "Price: High to Low");
        sortComboBox.setValue("Newest");

        sortComboBox.setOnAction(e -> {
            String val = sortComboBox.getValue();
            if ("Price: Low to High".equals(val)) {
                currentSort = "asc";
            } else if ("Price: High to Low".equals(val)) {
                currentSort = "desc";
            } else if ("Oldest".equals(val)) {
                currentSort = "oldest";
            } else {
                currentSort = "newest";
            }
            loadProducts();
        });

        loadCategories();
    }

    public void setFilter(String category, String search, String sort) {
        if (category != null) this.currentCategory = category;
        if (search != null) this.currentSearch = search;
        if (sort != null) this.currentSort = sort;

        if (searchNoticeLabel != null) {
            if (currentSearch != null && !currentSearch.isBlank()) {
                searchNoticeLabel.setText("Showing results for \"" + currentSearch + "\"");
                searchNoticeLabel.setVisible(true);
                searchNoticeLabel.setManaged(true);
            } else {
                searchNoticeLabel.setVisible(false);
                searchNoticeLabel.setManaged(false);
            }
        }

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
        if (cat.getSlug().equalsIgnoreCase(currentCategory)) {
            btn.getStyleClass().add("category-pill-active");
        }

        btn.setGraphic(SvgIcons.createIcon(getCategoryIcon(cat.getSlug()), 14, Color.valueOf("#4b5563")));
        btn.setCursor(Cursor.HAND);

        btn.setOnAction(e -> {
            this.currentCategory = cat.getSlug();
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

        ProductApiService.getInstance().getProducts(currentSort, currentCategory, currentSearch, 50)
                .thenAccept(products -> {
                    Platform.runLater(() -> {
                        loadingBox.setVisible(false);
                        loadingBox.setManaged(false);

                        if (products.isEmpty()) {
                            statusLabel.setText("No products found matching your filters.");
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
                        statusLabel.setText("Failed to load products.");
                        statusLabel.setVisible(true);
                    });
                    return null;
                });
    }
}
