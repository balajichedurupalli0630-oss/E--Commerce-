package com.omnicart.controller;

import com.omnicart.api.OrderApiService;
import com.omnicart.model.Order;
import com.omnicart.util.CurrencyFormatter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class OrdersController implements Initializable {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> idColumn;
    @FXML private TableColumn<Order, String> totalColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> dateColumn;
    @FXML private TableColumn<Order, String> productsColumn;
    @FXML private VBox loadingBox;
    @FXML private Label emptyOrdersLabel;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            .withZone(ZoneId.systemDefault());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        totalColumn.setCellValueFactory(data -> new SimpleStringProperty(CurrencyFormatter.format(data.getValue().getEffectiveAmount())));

        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status.toUpperCase());
                    badge.getStyleClass().add("status-badge");
                    if ("success".equalsIgnoreCase(status) || "paid".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("status-badge-success");
                    } else if ("pending".equalsIgnoreCase(status) || "processing".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("status-badge-pending");
                    } else {
                        badge.getStyleClass().add("status-badge-failed");
                    }
                    setAlignment(Pos.CENTER);
                    setGraphic(badge);
                }
            }
        });

        dateColumn.setCellValueFactory(data -> {
            if (data.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(DATE_FORMATTER.format(data.getValue().getCreatedAt()));
            }
            return new SimpleStringProperty("-");
        });

        productsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductsSummary()));

        loadOrders();
    }

    private void loadOrders() {
        loadingBox.setVisible(true);
        loadingBox.setManaged(true);
        ordersTable.getItems().clear();

        OrderApiService.getInstance().getUserOrders().thenAccept(orders -> {
            Platform.runLater(() -> {
                loadingBox.setVisible(false);
                loadingBox.setManaged(false);

                if (orders.isEmpty()) {
                    emptyOrdersLabel.setVisible(true);
                    emptyOrdersLabel.setManaged(true);
                } else {
                    emptyOrdersLabel.setVisible(false);
                    emptyOrdersLabel.setManaged(false);
                    ordersTable.setItems(FXCollections.observableArrayList(orders));
                }
            });
        });
    }
}
