package com.omnicart.controller;

import com.omnicart.api.PaymentApiService;
import com.omnicart.navigation.NavigationManager;
import com.omnicart.navigation.ViewType;
import com.omnicart.util.SvgIcons;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class ReturnController implements Initializable {

    @FXML private StackPane checkmarkCircle;
    @FXML private Label titleLabel;
    @FXML private Label sessionIdLabel;
    @FXML private Label paymentStatusLabel;
    @FXML private Button viewOrdersBtn;
    @FXML private Button continueShoppingBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkmarkCircle.getChildren().add(SvgIcons.createIcon(SvgIcons.CHECK_CIRCLE, 36, Color.valueOf("#10b981")));
        viewOrdersBtn.setGraphic(SvgIcons.createIcon(SvgIcons.ARROW_RIGHT, 14, Color.WHITE));
        continueShoppingBtn.setGraphic(SvgIcons.createIcon(SvgIcons.SHOPPING_BAG, 14, Color.valueOf("#374151")));
    }

    public void setSessionId(String sessionId) {
        sessionIdLabel.setText(sessionId);

        PaymentApiService.getInstance().getSessionStatus(sessionId).thenAccept(resp -> {
            Platform.runLater(() -> {
                String status = (String) resp.getOrDefault("paymentStatus", "Paid");
                paymentStatusLabel.setText(status != null ? status : "Paid");
            });
        });
    }

    @FXML
    private void onViewOrders() {
        NavigationManager.getInstance().navigateTo(ViewType.ORDERS);
    }

    @FXML
    private void onContinueShopping() {
        NavigationManager.getInstance().navigateTo(ViewType.HOME);
    }
}
