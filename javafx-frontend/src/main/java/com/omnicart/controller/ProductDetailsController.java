package com.omnicart.controller;

import com.omnicart.config.SessionManager;
import com.omnicart.model.Product;
import com.omnicart.navigation.NavigationManager;
import com.omnicart.navigation.ViewType;
import com.omnicart.service.CartService;
import com.omnicart.service.ImageLoaderService;
import com.omnicart.service.ToastService;
import com.omnicart.util.CurrencyFormatter;
import com.omnicart.util.SvgIcons;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class ProductDetailsController implements Initializable {

    @FXML private ImageView productImageView;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label priceLabel;
    @FXML private HBox sizesContainer;
    @FXML private HBox colorsContainer;
    @FXML private Label quantityLabel;
    @FXML private Button decrementBtn;
    @FXML private Button incrementBtn;
    @FXML private Button addToCartBtn;
    @FXML private Button buyNowBtn;
    @FXML private ImageView klarnaImageView;
    @FXML private ImageView cardsImageView;
    @FXML private ImageView stripeImageView;

    private Product product;
    private String selectedSize;
    private String selectedColor;
    private int quantity = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        decrementBtn.setGraphic(SvgIcons.createIcon(SvgIcons.MINUS, 14, Color.valueOf("#374151")));
        incrementBtn.setGraphic(SvgIcons.createIcon(SvgIcons.PLUS, 14, Color.valueOf("#374151")));
        addToCartBtn.setGraphic(SvgIcons.createIcon(SvgIcons.PLUS, 14, Color.WHITE));
        buyNowBtn.setGraphic(SvgIcons.createIcon(SvgIcons.SHOPPING_CART, 14, Color.valueOf("#1f2937")));

        klarnaImageView.setImage(ImageLoaderService.loadImage("/klarna.png"));
        cardsImageView.setImage(ImageLoaderService.loadImage("/cards.png"));
        stripeImageView.setImage(ImageLoaderService.loadImage("/stripe.png"));
    }

    public void setProduct(Product product) {
        this.product = product;
        this.quantity = 1;
        this.selectedSize = (product.getSizes() != null && !product.getSizes().isEmpty())
                ? product.getSizes().get(0) : "M";
        this.selectedColor = (product.getColors() != null && !product.getColors().isEmpty())
                ? product.getColors().get(0) : "gray";

        titleLabel.setText(product.getName());
        descriptionLabel.setText(product.getDescription() != null ? product.getDescription() : product.getShortDescription());
        priceLabel.setText(CurrencyFormatter.format(product.getEffectivePrice()));
        quantityLabel.setText(String.valueOf(quantity));

        renderSizes();
        renderColors();
        updateImage();
    }

    private void updateImage() {
        String path = product.getImageForColor(selectedColor);
        productImageView.setImage(ImageLoaderService.loadImage(path));
    }

    private void renderSizes() {
        sizesContainer.getChildren().clear();
        if (product.getSizes() == null) return;

        for (String size : product.getSizes()) {
            Button btn = new Button(size.toUpperCase());
            btn.getStyleClass().add("size-box-btn");
            if (size.equalsIgnoreCase(selectedSize)) {
                btn.getStyleClass().add("size-box-btn-active");
            }
            btn.setCursor(Cursor.HAND);
            btn.setOnAction(e -> {
                this.selectedSize = size;
                for (javafx.scene.Node node : sizesContainer.getChildren()) {
                    node.getStyleClass().remove("size-box-btn-active");
                }
                btn.getStyleClass().add("size-box-btn-active");
            });
            sizesContainer.getChildren().add(btn);
        }
    }

    private void renderColors() {
        colorsContainer.getChildren().clear();
        if (product.getColors() == null) return;

        for (String col : product.getColors()) {
            StackPane swatch = new StackPane();
            swatch.getStyleClass().add("color-swatch-box");
            swatch.setCursor(Cursor.HAND);

            Circle borderCircle = new Circle(12);
            borderCircle.setFill(Color.TRANSPARENT);
            borderCircle.setStroke(col.equalsIgnoreCase(selectedColor) ? Color.valueOf("#1f2937") : Color.TRANSPARENT);
            borderCircle.setStrokeWidth(2.0);

            Circle colorCircle = new Circle(9);
            colorCircle.setFill(resolveColor(col));

            swatch.getChildren().addAll(borderCircle, colorCircle);
            swatch.setOnMouseClicked(e -> {
                this.selectedColor = col;
                updateImage();
                for (javafx.scene.Node node : colorsContainer.getChildren()) {
                    if (node instanceof StackPane sp && sp.getChildren().get(0) instanceof Circle c) {
                        c.setStroke(sp == swatch ? Color.valueOf("#1f2937") : Color.TRANSPARENT);
                    }
                }
            });
            colorsContainer.getChildren().add(swatch);
        }
    }

    @FXML
    private void onDecrement() {
        if (quantity > 1) {
            quantity--;
            quantityLabel.setText(String.valueOf(quantity));
        }
    }

    @FXML
    private void onIncrement() {
        quantity++;
        quantityLabel.setText(String.valueOf(quantity));
    }

    @FXML
    private void onAddToCart() {
        if (product == null) return;
        if (SessionManager.getInstance().isAdmin()) {
            ToastService.showError("Admins cannot purchase items. Please log in with a customer account.");
            return;
        }
        CartService.getInstance().addToCart(product, quantity, selectedSize, selectedColor);
        ToastService.showSuccess("Added " + quantity + " x \"" + product.getName() + "\" to cart");
    }

    @FXML
    private void onBuyNow() {
        if (product == null) return;
        if (SessionManager.getInstance().isAdmin()) {
            ToastService.showError("Admins cannot purchase items. Please log in with a customer account.");
            return;
        }
        CartService.getInstance().addToCart(product, quantity, selectedSize, selectedColor);
        NavigationManager.getInstance().navigateTo(ViewType.CART);
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
