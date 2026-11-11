package com.omnicart.ui;

import com.omnicart.config.SessionManager;
import com.omnicart.model.Product;
import com.omnicart.navigation.NavigationManager;
import com.omnicart.service.CartService;
import com.omnicart.service.ImageLoaderService;
import com.omnicart.service.ToastService;
import com.omnicart.util.CurrencyFormatter;
import com.omnicart.util.SvgIcons;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class ProductCardView extends VBox {
    private final Product product;
    private String selectedColor;
    private String selectedSize;
    private final ImageView imageView;

    public ProductCardView(Product product) {
        this.product = product;
        this.selectedColor = (product.getColors() != null && !product.getColors().isEmpty())
                ? product.getColors().get(0) : "gray";
        this.selectedSize = (product.getSizes() != null && !product.getSizes().isEmpty())
                ? product.getSizes().get(0) : "M";

        getStyleClass().add("product-card");
        setPrefWidth(260);
        setMaxWidth(280);
        setSpacing(12);

        // --- IMAGE CONTAINER ---
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("product-card-image-box");
        imageContainer.setPrefHeight(260);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setCursor(Cursor.HAND);

        imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(240);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        updateImage();

        imageContainer.getChildren().add(imageView);

        // Hover scale transition
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), imageView);
        scaleIn.setToX(1.04);
        scaleIn.setToY(1.04);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), imageView);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        imageContainer.setOnMouseEntered(e -> scaleIn.playFromStart());
        imageContainer.setOnMouseExited(e -> scaleOut.playFromStart());
        imageContainer.setOnMouseClicked(e -> NavigationManager.getInstance().navigateToProductDetails(product));

        // --- DETAILS CONTAINER ---
        VBox detailsBox = new VBox(8);
        detailsBox.setPadding(new Insets(0, 12, 12, 12));

        Label titleLabel = new Label(product.getName());
        titleLabel.getStyleClass().add("product-card-title");
        titleLabel.setWrapText(true);
        titleLabel.setCursor(Cursor.HAND);
        titleLabel.setOnMouseClicked(e -> NavigationManager.getInstance().navigateToProductDetails(product));

        Label descLabel = new Label(product.getShortDescription() != null ? product.getShortDescription() : "");
        descLabel.getStyleClass().add("product-card-description");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(36);

        // Size and Color selectors row
        HBox selectorsRow = new HBox(12);
        selectorsRow.setAlignment(Pos.CENTER_LEFT);

        // Size
        VBox sizeBox = new VBox(2);
        Label sizeTitle = new Label("Size");
        sizeTitle.getStyleClass().add("product-card-meta-label");
        ComboBox<String> sizeCombo = new ComboBox<>();
        sizeCombo.getStyleClass().add("product-card-combo");
        if (product.getSizes() != null) {
            sizeCombo.getItems().addAll(product.getSizes());
            sizeCombo.setValue(selectedSize);
        }
        sizeCombo.setOnAction(e -> this.selectedSize = sizeCombo.getValue());
        sizeBox.getChildren().addAll(sizeTitle, sizeCombo);

        // Color swatches
        VBox colorBox = new VBox(2);
        Label colorTitle = new Label("Color");
        colorTitle.getStyleClass().add("product-card-meta-label");

        HBox swatchesBox = new HBox(5);
        swatchesBox.setAlignment(Pos.CENTER_LEFT);
        if (product.getColors() != null) {
            for (String col : product.getColors()) {
                StackPane swatch = createColorSwatch(col);
                swatchesBox.getChildren().add(swatch);
            }
        }
        colorBox.getChildren().addAll(colorTitle, swatchesBox);

        selectorsRow.getChildren().addAll(sizeBox, colorBox);

        // Price and Add to Cart Row
        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(actionRow, Priority.ALWAYS);

        Label priceLabel = new Label(CurrencyFormatter.format(product.getEffectivePrice()));
        priceLabel.getStyleClass().add("product-card-price");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.getStyleClass().add("btn-add-cart");
        addToCartBtn.setGraphic(SvgIcons.createIcon(SvgIcons.SHOPPING_CART, 14, Color.valueOf("#374151")));
        addToCartBtn.setCursor(Cursor.HAND);
        addToCartBtn.setOnAction(e -> {
            if (SessionManager.getInstance().isAdmin()) {
                ToastService.showError("Admins cannot purchase items. Please log in with a customer account.");
                return;
            }
            CartService.getInstance().addToCart(product, 1, selectedSize, selectedColor);
            ToastService.showSuccess("Added \"" + product.getName() + "\" to cart!");
        });

        actionRow.getChildren().addAll(priceLabel, spacer, addToCartBtn);

        detailsBox.getChildren().addAll(titleLabel, descLabel, selectorsRow, actionRow);

        getChildren().addAll(imageContainer, detailsBox);
    }

    private void updateImage() {
        String imgPath = product.getImageForColor(selectedColor);
        imageView.setImage(ImageLoaderService.loadImage(imgPath));
    }

    private StackPane createColorSwatch(String colorName) {
        StackPane container = new StackPane();
        container.getStyleClass().add("swatch-container");
        container.setCursor(Cursor.HAND);

        Circle outerRing = new Circle(8);
        outerRing.setFill(Color.TRANSPARENT);
        outerRing.setStroke(colorName.equalsIgnoreCase(selectedColor) ? Color.valueOf("#9ca3af") : Color.TRANSPARENT);
        outerRing.setStrokeWidth(1.5);

        Circle innerDot = new Circle(6);
        innerDot.setFill(resolveColor(colorName));

        container.getChildren().addAll(outerRing, innerDot);

        container.setOnMouseClicked(e -> {
            this.selectedColor = colorName;
            updateImage();
            // Re-render swatches selection borders
            if (container.getParent() instanceof HBox hbox) {
                for (javafx.scene.Node node : hbox.getChildren()) {
                    if (node instanceof StackPane sp && !sp.getChildren().isEmpty()) {
                        if (sp.getChildren().get(0) instanceof Circle c) {
                            c.setStroke(sp == container ? Color.valueOf("#9ca3af") : Color.TRANSPARENT);
                        }
                    }
                }
            }
        });

        return container;
    }

    private Color resolveColor(String name) {
        if (name == null) return Color.GRAY;
        return switch (name.toLowerCase()) {
            case "red" -> Color.valueOf("#ef4444");
            case "blue" -> Color.valueOf("#3b82f6");
            case "green" -> Color.valueOf("#10b981");
            case "purple" -> Color.valueOf("#8b5cf6");
            case "orange" -> Color.valueOf("#f97316");
            case "pink" -> Color.valueOf("#ec4899");
            case "yellow" -> Color.valueOf("#eab308");
            case "black" -> Color.valueOf("#111827");
            case "white" -> Color.valueOf("#f3f4f6");
            case "gray", "grey" -> Color.valueOf("#6b7280");
            default -> Color.valueOf("#9ca3af");
        };
    }
}
