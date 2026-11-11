package com.omnicart.controller.admin;

import com.omnicart.api.AuthApiService;
import com.omnicart.service.ToastService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddUserDialogController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
    }

    @FXML
    private void onSave() {
        String fname = firstNameField.getText().trim();
        String lname = lastNameField.getText().trim();
        String uname = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (uname.isEmpty() || email.isEmpty() || !email.contains("@") || password.length() < 4) {
            errorLabel.setText("Please provide valid information (password min 4 characters).");
            errorLabel.setVisible(true);
            return;
        }

        saveBtn.setDisable(true);
        errorLabel.setVisible(false);

        AuthApiService.getInstance().createUser(uname, fname, lname, email, password).thenAccept(user -> {
            Platform.runLater(() -> {
                saveBtn.setDisable(false);
                ToastService.showSuccess("User created: " + user.getFullName());
                closeDialog();
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                saveBtn.setDisable(false);
                errorLabel.setText("Failed to create user: " + ex.getMessage());
                errorLabel.setVisible(true);
            });
            return null;
        });
    }

    @FXML
    private void onCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }
}
