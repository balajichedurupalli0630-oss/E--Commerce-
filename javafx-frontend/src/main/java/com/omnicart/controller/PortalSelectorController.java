package com.omnicart.controller;

import com.omnicart.api.AuthApiService;
import com.omnicart.config.SessionManager;
import com.omnicart.model.User;
import com.omnicart.navigation.NavigationManager;
import com.omnicart.service.ImageLoaderService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class PortalSelectorController implements Initializable {

    @FXML private ImageView logoImage;
    @FXML private VBox customerCard;
    @FXML private VBox adminCard;
    @FXML private StackPane adminNormalPane;
    @FXML private StackPane adminLoginPane;
    @FXML private TextField adminUsernameField;
    @FXML private PasswordField adminPasswordField;
    @FXML private Label adminErrorLabel;
    @FXML private Button adminLoginBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logoImage.setImage(ImageLoaderService.loadImage("/logo.png"));

        // Allow Enter key to submit admin login
        adminPasswordField.setOnAction(e -> onAdminLoginSubmit());
        adminUsernameField.setOnAction(e -> adminPasswordField.requestFocus());
    }

    // ── Customer Portal ───────────────────────────────────────────────────────

    @FXML
    private void onCustomerPortalClicked() {
        // Set demo customer session and open client app
        User customer = new User("user_cust_01", "johndoe", "John", "Doe",
                "john.doe@example.com", "user");
        customer.setImageUrl("/users/2.png");
        SessionManager.getInstance().setSession(customer, "demo_token_cust");
        NavigationManager.getInstance().showClientApp();
    }

    @FXML
    private void onCustomerCardHover() {
        customerCard.setStyle(
            "-fx-background-color: #ffffff; -fx-background-radius: 16px;" +
            "-fx-border-color: #111827; -fx-border-radius: 16px; -fx-border-width: 2px;" +
            "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.18), 18, 0, 0, 6);"
        );
    }

    @FXML
    private void onCustomerCardExit() {
        customerCard.setStyle(
            "-fx-background-color: #ffffff; -fx-background-radius: 16px;" +
            "-fx-border-color: #e5e7eb; -fx-border-radius: 16px; -fx-border-width: 2px;" +
            "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 12, 0, 0, 4);"
        );
    }

    // ── Admin Portal ──────────────────────────────────────────────────────────

    @FXML
    private void onAdminCardClicked() {
        // Show the login form inside the admin card
        adminNormalPane.setVisible(false);
        adminNormalPane.setManaged(false);
        adminLoginPane.setVisible(true);
        adminLoginPane.setManaged(true);
        adminErrorLabel.setVisible(false);
        adminUsernameField.setText("");
        adminPasswordField.setText("");
        adminUsernameField.requestFocus();
    }

    @FXML
    private void onAdminLoginCancel() {
        adminLoginPane.setVisible(false);
        adminLoginPane.setManaged(false);
        adminNormalPane.setVisible(true);
        adminNormalPane.setManaged(true);
        adminErrorLabel.setVisible(false);
    }

    @FXML
    private void onAdminLoginSubmit() {
        String username = adminUsernameField.getText().trim();
        String password = adminPasswordField.getText();

        if (username.isEmpty()) {
            showAdminError("Please enter username.");
            return;
        }

        adminLoginBtn.setDisable(true);
        adminLoginBtn.setText("Signing in...");
        adminErrorLabel.setVisible(false);

        AuthApiService.getInstance().authenticate(username, password).thenAccept(user -> {
            Platform.runLater(() -> {
                adminLoginBtn.setDisable(false);
                adminLoginBtn.setText("Sign In");

                if (user != null) {
                    if (user.isAdmin()) {
                        SessionManager.getInstance().setSession(user, "token_" + user.getId());
                        NavigationManager.getInstance().showAdminApp();
                    } else {
                        showAdminError("Access denied: Admin role required.");
                    }
                } else {
                    // Fallback: try demo admin credentials offline
                    if ("admin".equalsIgnoreCase(username) && "admin123".equals(password)) {
                        User adminUser = new User("user_adm_01", "admin", "Admin", "User",
                                "admin@omnicart.com", "admin");
                        SessionManager.getInstance().setSession(adminUser, "demo_token_admin");
                        NavigationManager.getInstance().showAdminApp();
                    } else {
                        showAdminError("Invalid credentials. Try admin / admin123");
                    }
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                adminLoginBtn.setDisable(false);
                adminLoginBtn.setText("Sign In");
                // Backend not available — try offline demo credentials
                String u = adminUsernameField.getText().trim();
                String p = adminPasswordField.getText();
                if ("admin".equalsIgnoreCase(u) && "admin123".equals(p)) {
                    User adminUser = new User("user_adm_01", "admin", "Admin", "User",
                            "admin@omnicart.com", "admin");
                    SessionManager.getInstance().setSession(adminUser, "demo_token_admin");
                    NavigationManager.getInstance().showAdminApp();
                } else {
                    showAdminError("Invalid credentials. Try admin / admin123");
                }
            });
            return null;
        });
    }

    @FXML
    private void onAdminCardHover() {
        if (adminNormalPane.isVisible()) {
            adminCard.setStyle(
                "-fx-background-color: #ffffff; -fx-background-radius: 16px;" +
                "-fx-border-color: #7c3aed; -fx-border-radius: 16px; -fx-border-width: 2px;" +
                "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(124,58,237,0.2), 18, 0, 0, 6);"
            );
        }
    }

    @FXML
    private void onAdminCardExit() {
        if (adminNormalPane.isVisible()) {
            adminCard.setStyle(
                "-fx-background-color: #ffffff; -fx-background-radius: 16px;" +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16px; -fx-border-width: 2px;" +
                "-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 12, 0, 0, 4);"
            );
        }
    }

    private void showAdminError(String msg) {
        adminErrorLabel.setText(msg);
        adminErrorLabel.setVisible(true);
    }
}

