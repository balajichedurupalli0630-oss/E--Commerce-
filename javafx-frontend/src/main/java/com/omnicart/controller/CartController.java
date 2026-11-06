package com.omnicart.controller;

import com.omnicart.api.OrderApiService;
import com.omnicart.api.PaymentApiService;
import com.omnicart.config.SessionManager;
import com.omnicart.model.CartItem;
import com.omnicart.model.Order;
import com.omnicart.model.OrderItem;
import com.omnicart.model.ShippingAddress;
import com.omnicart.navigation.NavigationManager;
import com.omnicart.navigation.ViewType;
import com.omnicart.service.CartService;
import com.omnicart.service.ImageLoaderService;
import com.omnicart.service.ToastService;
import com.omnicart.util.BrowserUtil;
import com.omnicart.util.CurrencyFormatter;
import com.omnicart.util.SvgIcons;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CartController implements Initializable {

    // Step indicators
    @FXML private HBox step1Indicator;
    @FXML private HBox step2Indicator;
    @FXML private HBox step3Indicator;
    @FXML private Label step1Circle;
    @FXML private Label step2Circle;
    @FXML private Label step3Circle;

    // Wizard panels
    @FXML private VBox step1Panel;
    @FXML private VBox step2Panel;
    @FXML private VBox step3Panel;

    // Step 1: Cart Items
    @FXML private VBox cartItemsList;
    @FXML private Label emptyCartLabel;

    // Step 2: Shipping Form
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private Label shippingErrorLabel;

    // Step 3: Payment
    @FXML private Label deliveryNameLabel;
    @FXML private Label deliveryAddressLabel;
    @FXML private Label deliveryPhoneLabel;
    @FXML private Label paymentTotalLabel;
    @FXML private Label paymentItemsCountLabel;
    @FXML private Button placeOrderBtn;
    @FXML private Button payWithStripeBtn;
    @FXML private ProgressIndicator paymentProgress;

    // Cart Details Sidebar
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label shippingFeeLabel;
    @FXML private Label totalLabel;
    @FXML private Button continueToStep2Btn;

    private int currentStep = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        continueToStep2Btn.setGraphic(SvgIcons.createIcon(SvgIcons.ARROW_RIGHT, 14, Color.WHITE));
        placeOrderBtn.setGraphic(SvgIcons.createIcon(SvgIcons.CHECK_CIRCLE, 16, Color.WHITE));
        payWithStripeBtn.setGraphic(SvgIcons.createIcon(SvgIcons.CREDIT_CARD, 16, Color.valueOf("#111827")));

        // Pre-populate shipping form with last known address or user details
        ShippingAddress addr = SessionManager.getInstance().getLastShippingAddress();
        if (addr != null) {
            nameField.setText(addr.getName());
            emailField.setText(addr.getEmail());
            phoneField.setText(addr.getPhone());
            addressField.setText(addr.getAddress());
            cityField.setText(addr.getCity());
        }

        renderCart();
        setStep(1);
    }

    private void setStep(int step) {
        this.currentStep = step;

        step1Panel.setVisible(step == 1);
        step1Panel.setManaged(step == 1);
        step2Panel.setVisible(step == 2);
        step2Panel.setManaged(step == 2);
        step3Panel.setVisible(step == 3);
        step3Panel.setManaged(step == 3);

        updateStepIndicator(step1Indicator, step1Circle, step >= 1, step == 1);
        updateStepIndicator(step2Indicator, step2Circle, step >= 2, step == 2);
        updateStepIndicator(step3Indicator, step3Circle, step >= 3, step == 3);

        continueToStep2Btn.setVisible(step == 1);
        continueToStep2Btn.setManaged(step == 1);

        if (step == 3) {
            populateStep3Summary();
        }
    }

    private void updateStepIndicator(HBox indicator, Label circle, boolean completedOrActive, boolean isActive) {
        indicator.getStyleClass().removeAll("step-active", "step-inactive");
        indicator.getStyleClass().add(isActive ? "step-active" : "step-inactive");

        circle.getStyleClass().removeAll("step-circle-active", "step-circle-inactive");
        circle.getStyleClass().add(isActive ? "step-circle-active" : "step-circle-inactive");
    }

    private void renderCart() {
        cartItemsList.getChildren().clear();
        List<CartItem> items = CartService.getInstance().getCartItems();

        if (items.isEmpty()) {
            emptyCartLabel.setVisible(true);
            emptyCartLabel.setManaged(true);
            continueToStep2Btn.setDisable(true);
        } else {
            emptyCartLabel.setVisible(false);
            emptyCartLabel.setManaged(false);
            continueToStep2Btn.setDisable(false);

            for (CartItem item : items) {
                cartItemsList.getChildren().add(createCartItemRow(item));
            }
        }

        double subtotal = CartService.getInstance().getSubtotal();
        subtotalLabel.setText(CurrencyFormatter.format(subtotal));
        discountLabel.setText("₹0.00");
        shippingFeeLabel.setText("₹0.00");
        totalLabel.setText(CurrencyFormatter.format(subtotal));
    }

    private HBox createCartItemRow(CartItem item) {
        HBox row = new HBox(16);
        row.getStyleClass().add("cart-item-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));

        // Thumbnail
        StackPane thumbBox = new StackPane();
        thumbBox.getStyleClass().add("cart-thumb-box");
        thumbBox.setPrefSize(80, 80);
        ImageView iv = new ImageView(ImageLoaderService.loadImage(item.getDisplayImage()));
        iv.setFitWidth(70);
        iv.setFitHeight(70);
        iv.setPreserveRatio(true);
        thumbBox.getChildren().add(iv);

        // Details
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(item.getName());
        name.getStyleClass().add("cart-item-title");

        Label meta = new Label("Size: " + item.getSelectedSize() + "  |  Color: " + item.getSelectedColor());
        meta.getStyleClass().add("cart-item-meta");

        HBox qtyRow = new HBox(8);
        qtyRow.setAlignment(Pos.CENTER_LEFT);

        Button decBtn = new Button("-");
        decBtn.getStyleClass().add("qty-btn");
        decBtn.setOnAction(e -> {
            CartService.getInstance().updateQuantity(item, item.getQuantity() - 1);
            renderCart();
        });

        Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
        qtyLabel.getStyleClass().add("qty-text");

        Button incBtn = new Button("+");
        incBtn.getStyleClass().add("qty-btn");
        incBtn.setOnAction(e -> {
            CartService.getInstance().updateQuantity(item, item.getQuantity() + 1);
            renderCart();
        });

        qtyRow.getChildren().addAll(decBtn, qtyLabel, incBtn);
        info.getChildren().addAll(name, meta, qtyRow);

        // Subtotal
        Label itemTotal = new Label(CurrencyFormatter.format(item.getSubtotal()));
        itemTotal.getStyleClass().add("cart-item-price");

        // Remove button
        Button removeBtn = new Button();
        removeBtn.getStyleClass().add("btn-trash");
        removeBtn.setGraphic(SvgIcons.createIcon(SvgIcons.TRASH, 14, Color.valueOf("#ef4444")));
        removeBtn.setCursor(Cursor.HAND);
        removeBtn.setOnAction(e -> {
            CartService.getInstance().removeFromCart(item);
            renderCart();
            ToastService.showInfo("Removed item from cart");
        });

        row.getChildren().addAll(thumbBox, info, itemTotal, removeBtn);
        return row;
    }

    private void populateStep3Summary() {
        ShippingAddress addr = SessionManager.getInstance().getLastShippingAddress();
        if (addr != null) {
            deliveryNameLabel.setText(addr.getName() + " (" + addr.getEmail() + ")");
            deliveryAddressLabel.setText(addr.getAddress() + ", " + addr.getCity());
            deliveryPhoneLabel.setText("Phone: " + addr.getPhone());
        }

        double subtotal = CartService.getInstance().getSubtotal();
        paymentTotalLabel.setText(CurrencyFormatter.format(subtotal));
        paymentItemsCountLabel.setText(CartService.getInstance().getTotalItemCount() + " item(s)");
        placeOrderBtn.setText("Place Order (" + CurrencyFormatter.format(subtotal) + ")");
    }

    @FXML
    private void onContinueToStep2() {
        if (CartService.getInstance().getCartItems().isEmpty()) {
            ToastService.showError("Your cart is empty!");
            return;
        }
        if (SessionManager.getInstance().isAdmin()) {
            ToastService.showError("Admins cannot purchase items or checkout. Please use a customer account.");
            return;
        }
        setStep(2);
    }

    @FXML
    private void onBackToStep1() {
        setStep(1);
    }

    @FXML
    private void onContinueToStep3() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String city = cityField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || !email.contains("@") || phone.isEmpty() || address.isEmpty() || city.isEmpty()) {
            shippingErrorLabel.setText("Please fill out all address fields correctly.");
            shippingErrorLabel.setVisible(true);
            return;
        }

        shippingErrorLabel.setVisible(false);
        ShippingAddress shipping = new ShippingAddress(name, email, phone, address, city);
        SessionManager.getInstance().setLastShippingAddress(shipping);
        setStep(3);
    }

    @FXML
    private void onBackToStep2() {
        setStep(2);
    }

    @FXML
    private void onPlaceOrder() {
        if (SessionManager.getInstance().isAdmin()) {
            ToastService.showError("Admins cannot place orders or buy items.");
            return;
        }
        List<CartItem> cart = new ArrayList<>(CartService.getInstance().getCartItems());
        if (cart.isEmpty()) return;

        paymentProgress.setVisible(true);
        placeOrderBtn.setDisable(true);

        ShippingAddress addr = SessionManager.getInstance().getLastShippingAddress();
        String email = addr != null ? addr.getEmail() : "customer@example.com";

        PaymentApiService.getInstance().mockCheckout(cart, email).thenAccept(result -> {
            Platform.runLater(() -> {
                paymentProgress.setVisible(false);
                placeOrderBtn.setDisable(false);

                // Record local order in order service for immediate display in orders table
                int totalCents = (int) Math.round(CartService.getInstance().getSubtotal() * 100);
                List<OrderItem> orderItems = new ArrayList<>();
                for (CartItem ci : cart) {
                    orderItems.add(new OrderItem(ci.getName(), ci.getQuantity(), (int) Math.round(ci.getEffectivePrice() * 100)));
                }

                Order newOrder = new Order(result.sessionId,
                        SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().getId() : "user_cust_01",
                        email, totalCents, "success", orderItems, Instant.now());
                OrderApiService.getInstance().recordLocalOrder(newOrder);

                CartService.getInstance().clearCart();
                ToastService.showSuccess("Payment processed successfully!");
                NavigationManager.getInstance().navigateToReturn(result.sessionId);
            });
        });
    }

    @FXML
    private void onPayWithStripe() {
        if (SessionManager.getInstance().isAdmin()) {
            ToastService.showError("Admins cannot purchase items.");
            return;
        }
        List<CartItem> cart = new ArrayList<>(CartService.getInstance().getCartItems());
        if (cart.isEmpty()) return;

        paymentProgress.setVisible(true);
        payWithStripeBtn.setDisable(true);

        PaymentApiService.getInstance().createCheckoutSession(cart).thenAccept(result -> {
            Platform.runLater(() -> {
                paymentProgress.setVisible(false);
                payWithStripeBtn.setDisable(false);

                if (result.clientSecret != null && !result.clientSecret.isBlank()) {
                    // Open Stripe Hosted Checkout in system browser
                    String stripeUrl = "https://checkout.stripe.com/c/pay/" + result.clientSecret;
                    BrowserUtil.openUrl(stripeUrl);
                    ToastService.showInfo("Stripe Checkout opened in your browser.");
                } else {
                    // Fall back to direct mock checkout
                    onPlaceOrder();
                }
            });
        });
    }
}
