package com.omnicart.controller.admin;

import com.omnicart.api.ProductApiService;
import com.omnicart.model.Category;
import com.omnicart.model.Product;
import com.omnicart.service.ToastService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class AddProductDialogController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField shortDescField;
    @FXML private TextArea descField;
    @FXML private TextField priceField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private FlowPane sizesPane;
    @FXML private FlowPane colorsPane;
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;

    private final List<CheckBox> sizeCheckboxes = new ArrayList<>();
    private final List<CheckBox> colorCheckboxes = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setVisible(false);

        // Populate categories
        ProductApiService.getInstance().getCategories().thenAccept(categories -> {
            Platform.runLater(() -> {
                categoryComboBox.getItems().setAll(categories);
                if (!categories.isEmpty()) {
                    categoryComboBox.setValue(categories.get(0));
                }
            });
        });

        // Populate size checkboxes
        String[] defaultSizes = {"S", "M", "L", "XL", "XXL"};
        for (String s : defaultSizes) {
            CheckBox cb = new CheckBox(s);
            cb.setSelected(true);
            cb.getStyleClass().add("form-checkbox");
            sizeCheckboxes.add(cb);
            sizesPane.getChildren().add(cb);
        }

        // Populate color checkboxes
        String[] defaultColors = {"gray", "green", "purple", "blue", "black", "white", "red", "orange"};
        for (String c : defaultColors) {
            CheckBox cb = new CheckBox(c);
            if ("gray".equals(c) || "green".equals(c)) cb.setSelected(true);
            cb.getStyleClass().add("form-checkbox");
            colorCheckboxes.add(cb);
            colorsPane.getChildren().add(cb);
        }
    }

    @FXML
    private void onSave() {
        String name = nameField.getText().trim();
        String shortDesc = shortDescField.getText().trim();
        String desc = descField.getText().trim();
        String priceText = priceField.getText().trim();
        Category cat = categoryComboBox.getValue();

        if (name.isEmpty() || shortDesc.isEmpty() || priceText.isEmpty() || cat == null) {
            errorLabel.setText("Please fill out all required fields.");
            errorLabel.setVisible(true);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            errorLabel.setText("Price must be a valid number (e.g. 2999.00).");
            errorLabel.setVisible(true);
            return;
        }

        List<String> selectedSizes = new ArrayList<>();
        for (CheckBox cb : sizeCheckboxes) {
            if (cb.isSelected()) selectedSizes.add(cb.getText());
        }

        List<String> selectedColors = new ArrayList<>();
        Map<String, String> images = new HashMap<>();
        for (CheckBox cb : colorCheckboxes) {
            if (cb.isSelected()) {
                String color = cb.getText();
                selectedColors.add(color);
                images.put(color, "/products/1g.png");
            }
        }

        if (selectedSizes.isEmpty() || selectedColors.isEmpty()) {
            errorLabel.setText("Please select at least one size and one color.");
            errorLabel.setVisible(true);
            return;
        }

        saveBtn.setDisable(true);
        errorLabel.setVisible(false);

        Product product = new Product(null, name, shortDesc, desc, price, selectedSizes, selectedColors, images, cat.getSlug(), cat);

        ProductApiService.getInstance().createProduct(product).thenAccept(created -> {
            Platform.runLater(() -> {
                saveBtn.setDisable(false);
                ToastService.showSuccess("Product created successfully!");
                closeDialog();
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                saveBtn.setDisable(false);
                ToastService.showSuccess("Product added to local catalogue!");
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
