package com.omnicart.controller.admin;

import com.omnicart.api.OrderApiService;
import com.omnicart.api.ProductApiService;
import com.omnicart.model.Order;
import com.omnicart.model.OrderChartData;
import com.omnicart.model.Product;
import com.omnicart.model.ProductRecommendation;
import com.omnicart.service.ImageLoaderService;
import com.omnicart.service.ToastService;
import com.omnicart.util.CurrencyFormatter;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private BarChart<String, Number> revenueBarChart;
    @FXML private CategoryAxis revenueXAxis;
    @FXML private NumberAxis revenueYAxis;
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalOrdersCountLabel;

    @FXML private VBox latestTransactionsList;
    @FXML private VBox popularProductsList;

    @FXML private DatePicker todoDatePicker;
    @FXML private VBox todoItemsBox;

    @FXML private TableView<ProductRecommendation> recommendationsTable;
    @FXML private TableColumn<ProductRecommendation, String> colProduct;
    @FXML private TableColumn<ProductRecommendation, String> colCostAndPrice;
    @FXML private TableColumn<ProductRecommendation, String> colDemandCi;
    @FXML private TableColumn<ProductRecommendation, Integer> colConfidence;
    @FXML private TableColumn<ProductRecommendation, String> colPricingAdvice;
    @FXML private TableColumn<ProductRecommendation, String> colInventoryAdvice;
    @FXML private TableColumn<ProductRecommendation, Void> colQuickAction;
    @FXML private Label highConfidenceCountLabel;

    private final ProductApiService productApiService = ProductApiService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initRevenueChart();
        initTodoList();
        loadLatestTransactions();
        loadPopularProducts();
        setupRecommendationsTable();
        loadFinancialInsights();
    }

    private void initRevenueChart() {
        revenueYAxis.setLabel("Revenue (₹)");

        OrderApiService.getInstance().getOrderChart().thenAccept(chartData -> {
            Platform.runLater(() -> {
                XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
                revenueSeries.setName("Revenue (₹)");

                double grandTotalRevenue = 0.0;
                long grandTotalOrders = 0;

                for (OrderChartData d : chartData) {
                    String monthName = d.getMonth().substring(0, Math.min(3, d.getMonth().length()));
                    double rev = d.getRevenue() != null ? d.getRevenue() : 0.0;
                    grandTotalRevenue += rev;
                    grandTotalOrders += d.getTotal();

                    revenueSeries.getData().add(new XYChart.Data<>(monthName, rev));
                }

                if (totalRevenueLabel != null) {
                    totalRevenueLabel.setText(String.format("Total Revenue: ₹%,.0f", grandTotalRevenue));
                }
                if (totalOrdersCountLabel != null) {
                    totalOrdersCountLabel.setText(String.format("%d Orders", grandTotalOrders));
                }

                revenueBarChart.getData().setAll(revenueSeries);
            });
        });
    }

    private void initTodoList() {
        todoDatePicker.setValue(LocalDate.now());

        String[] tasks = {
                "Restock: Nike Air Essentials Pullover",
                "Restock: Adidas CoreFit T-Shirt",
                "Pack and ship pending customer orders",
                "Check supplier delivery status"
        };

        boolean[] states = {false, false, true, false};

        todoItemsBox.getChildren().clear();
        for (int i = 0; i < tasks.length; i++) {
            HBox item = new HBox(10);
            item.setAlignment(Pos.CENTER_LEFT);
            item.getStyleClass().add("todo-item-card");

            CheckBox cb = new CheckBox(tasks[i]);
            cb.setSelected(states[i]);
            cb.getStyleClass().add("todo-checkbox");

            item.getChildren().add(cb);
            todoItemsBox.getChildren().add(item);
        }
    }

    private void loadLatestTransactions() {
        OrderApiService.getInstance().getAllOrders(5).thenAccept(orders -> {
            Platform.runLater(() -> {
                latestTransactionsList.getChildren().clear();
                for (Order ord : orders) {
                    HBox card = new HBox(12);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.getStyleClass().add("admin-mini-card");

                    VBox info = new VBox(2);
                    HBox.setHgrow(info, Priority.ALWAYS);

                    Label email = new Label(ord.getEmail());
                    email.getStyleClass().add("mini-card-title");

                    Label badge = new Label(ord.getStatus().toUpperCase());
                    badge.getStyleClass().add("status-badge");
                    if ("success".equalsIgnoreCase(ord.getStatus()) || "paid".equalsIgnoreCase(ord.getStatus())) {
                        badge.getStyleClass().add("status-badge-success");
                    } else {
                        badge.getStyleClass().add("status-badge-pending");
                    }

                    info.getChildren().addAll(email, badge);

                    Label amount = new Label(CurrencyFormatter.format(ord.getEffectiveAmount()));
                    amount.getStyleClass().add("mini-card-price");

                    card.getChildren().addAll(info, amount);
                    latestTransactionsList.getChildren().add(card);
                }
            });
        });
    }

    private void loadPopularProducts() {
        ProductApiService.getInstance().getProducts("newest", null, null, 5).thenAccept(products -> {
            Platform.runLater(() -> {
                popularProductsList.getChildren().clear();
                for (Product p : products) {
                    HBox card = new HBox(12);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.getStyleClass().add("admin-mini-card");

                    ImageView iv = new ImageView(ImageLoaderService.loadImage(p.getImageForColor("gray")));
                    iv.setFitWidth(40);
                    iv.setFitHeight(40);
                    iv.setPreserveRatio(true);

                    VBox info = new VBox(2);
                    HBox.setHgrow(info, Priority.ALWAYS);

                    Label title = new Label(p.getName());
                    title.getStyleClass().add("mini-card-title");

                    Label cat = new Label(p.getCategorySlug());
                    cat.getStyleClass().add("mini-card-sub");

                    info.getChildren().addAll(title, cat);

                    Label price = new Label(CurrencyFormatter.format(p.getEffectivePrice()));
                    price.getStyleClass().add("mini-card-price");

                    card.getChildren().addAll(iv, info, price);
                    popularProductsList.getChildren().add(card);
                }
            });
        });
    }

    private void setupRecommendationsTable() {
        colProduct.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));

        colCostAndPrice.setCellValueFactory(c -> {
            ProductRecommendation r = c.getValue();
            double cp = r.getCostPrice() != null ? r.getCostPrice() : 0.0;
            double p = r.getCurrentPrice() != null ? r.getCurrentPrice() : 0.0;
            double m = r.getCurrentMarginPercent() != null ? r.getCurrentMarginPercent() : 0.0;
            return new SimpleStringProperty(String.format(
                "₹%.0f → ₹%.0f (%.1f%%)", cp, p, m
            ));
        });

        colDemandCi.setCellValueFactory(c -> {
            ProductRecommendation r = c.getValue();
            double demand = r.getPosteriorDailyDemand() != null ? r.getPosteriorDailyDemand() : 0.0;
            return new SimpleStringProperty(String.format("~%.1f units/day", demand));
        });

        // Confidence Cell with Color Coding (No Hallucinated Numbers Shown When Low)
        colConfidence.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getConfidencePercent()));
        colConfidence.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer conf, boolean empty) {
                super.updateItem(conf, empty);
                if (empty || conf == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(conf + "%");
                    if (conf >= 75) setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    else if (conf >= 65) setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #6b7280;");
                }
            }
        });

        colPricingAdvice.setCellValueFactory(c -> {
            ProductRecommendation r = c.getValue();
            if ("INSUFFICIENT_DATA".equals(r.getPricingAction())) {
                return new SimpleStringProperty("Awaiting sales data to optimize");
            }
            if ("HOLD".equals(r.getPricingAction())) {
                double curPrice = r.getCurrentPrice() != null ? r.getCurrentPrice() : 0.0;
                return new SimpleStringProperty("Keep current price (₹" + String.format("%.0f", curPrice) + ")");
            }
            String tag = "INCREASE_PRICE".equals(r.getPricingAction()) ? "▲ Raise to " : "▼ Discount to ";
            double recPrice = r.getRecommendedPrice() != null ? r.getRecommendedPrice() : 0.0;
            double projMargin = r.getProjectedMarginPercent() != null ? r.getProjectedMarginPercent() : 0.0;
            return new SimpleStringProperty(String.format(
                "%s₹%.0f (%.0f%% margin)",
                tag, recPrice, projMargin
            ));
        });

        colInventoryAdvice.setCellValueFactory(c -> {
            ProductRecommendation r = c.getValue();
            int stock = r.getCurrentStock() != null ? r.getCurrentStock() : 0;
            if ("URGENT_RESTOCK".equals(r.getInventoryAction())) {
                int buyQty = r.getRecommendedBuyQty() != null ? r.getRecommendedBuyQty() : 0;
                return new SimpleStringProperty(String.format("⚠ Low Stock (%d left) → Restock +%d units", stock, buyQty));
            }
            if (stock == 0) {
                return new SimpleStringProperty("⚠ Out of stock — Restock needed");
            }
            double daysLeft = r.getDaysOfStockLeft() != null ? r.getDaysOfStockLeft() : 0.0;
            return new SimpleStringProperty(String.format("In Stock: %d (~%.0f days left)", stock, daysLeft));
        });

        // One-Click Action Buttons (Apply Price or Restock)
        colQuickAction.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                ProductRecommendation r = getTableView().getItems().get(getIndex());
                if (r == null) {
                    setGraphic(null);
                    return;
                }
                VBox box = new VBox(6);
                box.setAlignment(Pos.CENTER_LEFT);

                boolean hasPriceAction = "INCREASE_PRICE".equals(r.getPricingAction())
                                      || "APPLY_DISCOUNT".equals(r.getPricingAction());
                boolean hasRestockAction = "URGENT_RESTOCK".equals(r.getInventoryAction());

                if (hasPriceAction && r.getRecommendedPrice() != null) {
                    Button priceBtn = new Button("Apply ₹" + String.format("%.0f", r.getRecommendedPrice()));
                    priceBtn.getStyleClass().add("btn-primary");
                    priceBtn.setOnAction(e -> handleApplyPrice(r));
                    box.getChildren().add(priceBtn);
                }

                if (hasRestockAction && r.getRecommendedBuyQty() != null) {
                    Button restockBtn = new Button("Restock +" + r.getRecommendedBuyQty());
                    restockBtn.getStyleClass().add("btn-warning");
                    restockBtn.setOnAction(e -> handleRestock(r));
                    box.getChildren().add(restockBtn);
                }

                if (!hasPriceAction && !hasRestockAction) {
                    Label ok = new Label("No Action Needed");
                    ok.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
                    box.getChildren().add(ok);
                }

                setGraphic(box);
            }
        });
    }

    @FXML
    public void loadFinancialInsights() {
        productApiService.getRecommendations().thenAccept(list -> Platform.runLater(() -> {
            if (list != null) {
                recommendationsTable.setItems(FXCollections.observableArrayList(list));
                long actionable = list.stream()
                    .filter(r -> (!"HOLD".equals(r.getPricingAction()) && !"INSUFFICIENT_DATA".equals(r.getPricingAction()))
                              || "URGENT_RESTOCK".equals(r.getInventoryAction()))
                    .count();
                highConfidenceCountLabel.setText(actionable + " Actionable Insights");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> ToastService.showError("Failed to load financial insights: " + ex.getMessage()));
            return null;
        });
    }

    private void handleApplyPrice(ProductRecommendation r) {
        productApiService.applyRecommendedPrice(r.getProductId(), r.getRecommendedPrice())
            .thenAccept(updated -> Platform.runLater(() -> {
                ToastService.showSuccess("Updated " + r.getProductName() + " price to ₹" + r.getRecommendedPrice());
                loadFinancialInsights();
            })).exceptionally(ex -> {
                Platform.runLater(() -> ToastService.showError("Failed to apply price: " + ex.getMessage()));
                return null;
            });
    }

    private void handleRestock(ProductRecommendation r) {
        productApiService.restockProduct(r.getProductId(), r.getRecommendedBuyQty())
            .thenAccept(updated -> Platform.runLater(() -> {
                ToastService.showSuccess("Added +" + r.getRecommendedBuyQty() + " units to " + r.getProductName());
                loadFinancialInsights();
            })).exceptionally(ex -> {
                Platform.runLater(() -> ToastService.showError("Failed to restock: " + ex.getMessage()));
                return null;
            });
    }
}
