package com.omnicart.controller.admin;

import com.omnicart.api.OrderApiService;
import com.omnicart.model.Order;
import com.omnicart.service.ToastService;
import com.omnicart.util.CurrencyFormatter;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class AdminOrdersController implements Initializable {

    @FXML private TextField searchField;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Boolean> selectColumn;
    @FXML private TableColumn<Order, String> idColumn;
    @FXML private TableColumn<Order, String> emailColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, Double> amountColumn;
    @FXML private TableColumn<Order, String> dateColumn;
    @FXML private TableColumn<Order, String> productsColumn;
    @FXML private TableColumn<Order, Order> actionsColumn;

    private final ObservableList<Order> masterList = FXCollections.observableArrayList();
    private FilteredList<Order> filteredList;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(false));
        selectColumn.setCellFactory(col -> new TableCell<>() {
            private final CheckBox cb = new CheckBox();
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setAlignment(Pos.CENTER);
                    setGraphic(cb);
                }
            }
        });

        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

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

        amountColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getEffectiveAmount()));
        amountColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(CurrencyFormatter.format(amount));
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

        actionsColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);
                if (empty || order == null) {
                    setGraphic(null);
                } else {
                    MenuButton menu = new MenuButton("Actions");
                    menu.getStyleClass().add("table-action-menu");

                    MenuItem copyId = new MenuItem("Copy Order ID");
                    copyId.setOnAction(e -> {
                        ClipboardContent content = new ClipboardContent();
                        content.putString(order.getId());
                        Clipboard.getSystemClipboard().setContent(content);
                        ToastService.showInfo("Copied Order ID");
                    });

                    MenuItem cancelOrder = new MenuItem("Cancel Order");
                    if ("cancelled".equalsIgnoreCase(order.getStatus()) || "canceled".equalsIgnoreCase(order.getStatus())) {
                        cancelOrder.setText("Cancelled");
                        cancelOrder.setDisable(true);
                    } else {
                        cancelOrder.setOnAction(e -> handleCancelOrder(order));
                    }

                    menu.getItems().addAll(copyId, new SeparatorMenuItem(), cancelOrder);
                    setAlignment(Pos.CENTER);
                    setGraphic(menu);
                }
            }
        });

        filteredList = new FilteredList<>(masterList, o -> true);
        ordersTable.setItems(filteredList);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(o -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase();
                return (o.getId() != null && o.getId().toLowerCase().contains(lower)) ||
                        (o.getEmail() != null && o.getEmail().toLowerCase().contains(lower)) ||
                        (o.getStatus() != null && o.getStatus().toLowerCase().contains(lower));
            });
        });

        loadOrders();
    }

    private void handleCancelOrder(Order order) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Customer Order");
        alert.setHeaderText("Cancel Order: " + order.getId());
        alert.setContentText("Are you sure you want to cancel this order from customer " + order.getEmail() + "?");

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                OrderApiService.getInstance().cancelOrder(order.getId()).thenAccept(ok -> {
                    Platform.runLater(() -> {
                        order.setStatus("cancelled");
                        ordersTable.refresh();
                        ToastService.showSuccess("Order " + order.getId() + " cancelled successfully.");
                    });
                });
            }
        });
    }

    private void loadOrders() {
        OrderApiService.getInstance().getAllOrders(100).thenAccept(orders -> {
            Platform.runLater(() -> {
                masterList.setAll(orders);
            });
        });
    }
}
