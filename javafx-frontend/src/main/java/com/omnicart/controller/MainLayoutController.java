package com.omnicart.controller;

import com.omnicart.config.SessionManager;
import com.omnicart.model.User;
import com.omnicart.navigation.NavigationManager;
import com.omnicart.navigation.ViewType;
import com.omnicart.service.CartService;
import com.omnicart.service.ImageLoaderService;
import com.omnicart.service.ToastService;
import com.omnicart.util.SvgIcons;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainLayoutController implements Initializable {

    @FXML private ImageView logoImageView;
    @FXML private TextField searchField;
    @FXML private Button homeButton;
    @FXML private Button bellButton;
    @FXML private StackPane cartButton;
    @FXML private Button cartIconButton;
    @FXML private Label cartBadge;
    @FXML private Button authButton;
    @FXML private MenuButton userMenuButton;
    @FXML private Button adminSwitchButton;
    @FXML private StackPane contentArea;
    @FXML private ImageView footerLogoImageView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NavigationManager.getInstance().setContentArea(contentArea);

        // Load logos
        logoImageView.setImage(ImageLoaderService.loadImage("/logo.png"));
        if (footerLogoImageView != null) {
            footerLogoImageView.setImage(ImageLoaderService.loadImage("/logo.png"));
        }

        // Set SVG icons
        homeButton.setGraphic(SvgIcons.createIcon(SvgIcons.HOME, 16, Color.valueOf("#4b5563")));
        bellButton.setGraphic(SvgIcons.createIcon(SvgIcons.BELL, 16, Color.valueOf("#4b5563")));

        if (cartIconButton != null) {
            cartIconButton.setGraphic(SvgIcons.createIcon(SvgIcons.SHOPPING_CART, 18, Color.valueOf("#4b5563")));
            cartIconButton.setOnAction(e -> onCartClicked());
        }

        // Search action
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String query = searchField.getText();
                NavigationManager.getInstance().navigateToProducts(null, query, null);
            }
        });

        // Cart badge listener
        CartService.getInstance().addListener(this::updateCartBadge);
        updateCartBadge();

        // Auth state listener
        SessionManager.getInstance().addAuthListener(this::updateAuthUi);
        updateAuthUi(SessionManager.getInstance().getCurrentUser());
    }

    private void updateCartBadge() {
        Platform.runLater(() -> {
            int count = CartService.getInstance().getTotalItemCount();
            if (count > 0) {
                cartBadge.setText(String.valueOf(count));
                cartBadge.setVisible(true);
            } else {
                cartBadge.setText("0");
                cartBadge.setVisible(false);
            }
        });
    }

    private void updateAuthUi(User user) {
        Platform.runLater(() -> {
            if (user != null) {
                authButton.setVisible(false);
                authButton.setManaged(false);
                userMenuButton.setVisible(true);
                userMenuButton.setManaged(true);
                userMenuButton.setText(user.getFullName());

                adminSwitchButton.setVisible(user.isAdmin());
                adminSwitchButton.setManaged(user.isAdmin());
                if (user.isAdmin()) {
                    cartButton.setVisible(false);
                    cartButton.setManaged(false);
                } else {
                    cartButton.setVisible(true);
                    cartButton.setManaged(true);
                }
            } else {
                authButton.setVisible(true);
                authButton.setManaged(true);
                userMenuButton.setVisible(false);
                userMenuButton.setManaged(false);
                adminSwitchButton.setVisible(false);
                adminSwitchButton.setManaged(false);
                cartButton.setVisible(true);
                cartButton.setManaged(true);
            }
        });
    }

    @FXML
    private void onLogoClicked() {
        NavigationManager.getInstance().navigateTo(ViewType.HOME);
    }

    @FXML
    private void onHomeClicked() {
        NavigationManager.getInstance().navigateTo(ViewType.HOME);
    }

    @FXML
    private void onCartClicked() {
        if (SessionManager.getInstance().isAdmin()) {
            ToastService.showError("Admins cannot use the cart or make purchases.");
            return;
        }
        NavigationManager.getInstance().navigateTo(ViewType.CART);
    }

    @FXML
    private void onAuthClicked() {
        openLoginDialog();
    }

    @FXML
    private void onSeeOrdersClicked() {
        NavigationManager.getInstance().navigateTo(ViewType.ORDERS);
    }

    @FXML
    private void onSignOutClicked() {
        SessionManager.getInstance().logout();
        ToastService.showInfo("You have been signed out.");
        NavigationManager.getInstance().navigateTo(ViewType.HOME);
    }

    @FXML
    private void onSwitchToAdminClicked() {
        NavigationManager.getInstance().showAdminApp();
    }

    @FXML
    private void onSwitchPortalClicked() {
        SessionManager.getInstance().logout();
        NavigationManager.getInstance().showPortalSelector();
    }

    // ── Footer link handlers ──────────────────────────────────────────────────

    @FXML
    private void onFooterHomeClicked() {
        NavigationManager.getInstance().navigateTo(ViewType.HOME);
    }

    @FXML
    private void onFooterAllProductsClicked() {
        NavigationManager.getInstance().navigateToProducts(null, null, null);
    }

    @FXML
    private void onFooterNewArrivalsClicked() {
        NavigationManager.getInstance().navigateToProducts(null, null, "newest");
    }

    @FXML
    private void onFooterBestSellersClicked() {
        NavigationManager.getInstance().navigateToProducts(null, null, "popular");
    }

    @FXML
    private void onFooterSpecialDealsClicked() {
        NavigationManager.getInstance().navigateToProducts(null, null, "price_asc");
    }

    @FXML
    private void onFooterContactClicked() {
        ToastService.showInfo("📧 Contact us at: support@omnicart.com");
    }

    @FXML
    private void onFooterTermsClicked() {
        ToastService.showInfo("📄 Terms of Service: OmniCart © 2026. All transactions subject to our terms.");
    }

    @FXML
    private void onFooterPrivacyClicked() {
        ToastService.showInfo("🔒 Privacy Policy: We never share your data. Your privacy is our priority.");
    }

    @FXML
    private void onFooterAboutClicked() {
        ToastService.showInfo("🏪 About OmniCart: A modern e-commerce platform built for speed & style.");
    }

    @FXML
    private void onFooterAffiliateClicked() {
        ToastService.showInfo("🤝 Affiliate Program: Coming soon! Earn commissions by referring customers.");
    }

    @FXML
    private void onFooterCareersClicked() {
        ToastService.showInfo("💼 Careers: We're hiring! Send your resume to careers@omnicart.com");
    }

    @FXML
    private void onFooterSupportClicked() {
        ToastService.showInfo("❓ Support FAQ: Visit our help center at help.omnicart.com");
    }

    private void openLoginDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/omnicart/fxml/login-dialog.fxml"));
            Parent dialogRoot = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(NavigationManager.getInstance().getPrimaryStage());
            dialogStage.setTitle("Sign In to OmniCart");

            Scene scene = new Scene(dialogRoot);
            scene.getStylesheets().addAll(
                    getClass().getResource("/com/omnicart/css/common.css").toExternalForm(),
                    getClass().getResource("/com/omnicart/css/client.css").toExternalForm()
            );

            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
