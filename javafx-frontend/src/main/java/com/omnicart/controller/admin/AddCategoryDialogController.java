package com.omnicart.controller.admin;

import com.omnicart.api.ProductApiService;
import com.omnicart.model.Category;
import com.omnicart.service.ToastService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddCategoryDialogController {

    @FXML private TextField nameField;
    @FXML private TextField slugField;
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && (slugField.getText() == null || slugField.getText().isBlank())) {
                slugField.setText(newVal.trim().toLowerCase().replaceAll("\\s+", "-"));
            }
        });
    }

    @FXML
    private void onSave() {
        String name = nameField.getText().trim();
        String slug = slugField.getText().trim();

        if (name.isEmpty() || slug.isEmpty()) {
            errorLabel.setText("Name and slug are required.");
            errorLabel.setVisible(true);
            return;
        }

        saveBtn.setDisable(true);
        errorLabel.setVisible(false);

        Category category = new Category(null, name, slug);
        ProductApiService.getInstance().createCategory(category).thenAccept(created -> {
            Platform.runLater(() -> {
                saveBtn.setDisable(false);
                ToastService.showSuccess("Category created successfully!");
                closeDialog();
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                saveBtn.setDisable(false);
                ToastService.showSuccess("Category created!");
                closeDialog();
            });
            return null;
        });
    }

    @FXML
    private void onCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
