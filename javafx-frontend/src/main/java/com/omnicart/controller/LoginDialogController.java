package com.omnicart.controller;

import com.omnicart.api.AuthApiService;
import com.omnicart.config.SessionManager;
import com.omnicart.model.User;
import com.omnicart.service.ToastService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginDialogController implements Initializable {

    @FXML private TabPane tabPane;
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginErrorLabel;
    @FXML private Button signInBtn;

    @FXML private TextField regFirstNameField;
    @FXML private TextField regLastNameField;
    @FXML private TextField regUsernameField;
    @FXML private TextField regEmailField;
    @FXML private PasswordField regPasswordField;
    @FXML private Label regErrorLabel;
    @FXML private Button registerBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginErrorLabel.setVisible(false);
        regErrorLabel.setVisible(false);
    }

    @FXML
    private void onSignIn() {
        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText();

        if (username.isEmpty()) {
            loginErrorLabel.setText("Please enter username or email.");
            loginErrorLabel.setVisible(true);
            return;
        }

        signInBtn.setDisable(true);
        loginErrorLabel.setVisible(false);

        AuthApiService.getInstance().authenticate(username, password).thenAccept(user -> {
            Platform.runLater(() -> {
                signInBtn.setDisable(false);
                if (user != null) {
                    SessionManager.getInstance().setSession(user, "token_" + user.getId());
                    ToastService.showSuccess("Signed in as " + user.getFullName() + " (" + user.getRole().toUpperCase() + ")");
                    closeDialog();
                } else {
                    loginErrorLabel.setText("Invalid credentials.");
                    loginErrorLabel.setVisible(true);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                signInBtn.setDisable(false);
                loginErrorLabel.setText("Login error: " + ex.getMessage());
                loginErrorLabel.setVisible(true);
            });
            return null;
        });
    }

    @FXML
    private void onQuickAdminLogin() {
        loginUsernameField.setText("admin");
        loginPasswordField.setText("admin123");
        onSignIn();
    }

    @FXML
    private void onQuickCustomerLogin() {
        loginUsernameField.setText("johndoe");
        loginPasswordField.setText("password123");
        onSignIn();
    }

    @FXML
    private void onQuickPriyaLogin() {
        loginUsernameField.setText("priya");
        loginPasswordField.setText("priya123");
        onSignIn();
    }

    @FXML
    private void onRegister() {
        String fname = regFirstNameField.getText().trim();
        String lname = regLastNameField.getText().trim();
        String uname = regUsernameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = regPasswordField.getText();

        if (uname.isEmpty() || email.isEmpty() || !email.contains("@") || password.length() < 4) {
            regErrorLabel.setText("Please provide valid information (password min 4 chars).");
            regErrorLabel.setVisible(true);
            return;
        }

        registerBtn.setDisable(true);
        regErrorLabel.setVisible(false);

        AuthApiService.getInstance().createUser(uname, fname, lname, email, password).thenAccept(user -> {
            Platform.runLater(() -> {
                registerBtn.setDisable(false);
                SessionManager.getInstance().setSession(user, "token_" + user.getId());
                ToastService.showSuccess("Account created successfully!");
                closeDialog();
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                registerBtn.setDisable(false);
                regErrorLabel.setText("Registration error: " + ex.getMessage());
                regErrorLabel.setVisible(true);
            });
            return null;
        });
    }

    private void closeDialog() {
        Stage stage = (Stage) tabPane.getScene().getWindow();
        stage.close();
    }
}
