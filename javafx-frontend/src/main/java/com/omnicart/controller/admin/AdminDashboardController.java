package com.omnicart.controller.admin;

import com.omnicart.api.OrderApiService;
import com.omnicart.api.ProductApiService;
import com.omnicart.model.Order;
import com.omnicart.model.OrderChartData;
import com.omnicart.model.Product;
import com.omnicart.service.ImageLoaderService;
import com.omnicart.util.CurrencyFormatter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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

    @FXML private PieChart browserPieChart;

    @FXML private AreaChart<String, Number> visitorsAreaChart;
    @FXML private CategoryAxis visitorsXAxis;
    @FXML private NumberAxis visitorsYAxis;

    @FXML private VBox latestTransactionsList;
    @FXML private VBox popularProductsList;

    @FXML private DatePicker todoDatePicker;
    @FXML private VBox todoItemsBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initRevenueChart();
        initBrowserPieChart();
        initVisitorsAreaChart();
        initTodoList();
        loadLatestTransactions();
        loadPopularProducts();
    }

    private void initRevenueChart() {
        OrderApiService.getInstance().getOrderChart().thenAccept(chartData -> {
            Platform.runLater(() -> {
                XYChart.Series<String, Number> totalSeries = new XYChart.Series<>();
                totalSeries.setName("Total");

                XYChart.Series<String, Number> successfulSeries = new XYChart.Series<>();
                successfulSeries.setName("Successful");

                for (OrderChartData d : chartData) {
                    totalSeries.getData().add(new XYChart.Data<>(d.getMonth().substring(0, Math.min(3, d.getMonth().length())), d.getTotal()));
                    successfulSeries.getData().add(new XYChart.Data<>(d.getMonth().substring(0, Math.min(3, d.getMonth().length())), d.getSuccessful()));
                }

                revenueBarChart.getData().setAll(totalSeries, successfulSeries);
            });
        });
    }

    private void initBrowserPieChart() {
        browserPieChart.getData().addAll(
                new PieChart.Data("Chrome (40%)", 275),
                new PieChart.Data("Firefox (25%)", 287),
                new PieChart.Data("Safari (18%)", 200),
                new PieChart.Data("Edge (10%)", 173),
                new PieChart.Data("Other (7%)", 190)
        );
    }

    private void initVisitorsAreaChart() {
        XYChart.Series<String, Number> desktopSeries = new XYChart.Series<>();
        desktopSeries.setName("Desktop");
        desktopSeries.getData().add(new XYChart.Data<>("Jan", 186));
        desktopSeries.getData().add(new XYChart.Data<>("Feb", 305));
        desktopSeries.getData().add(new XYChart.Data<>("Mar", 237));
        desktopSeries.getData().add(new XYChart.Data<>("Apr", 73));
        desktopSeries.getData().add(new XYChart.Data<>("May", 209));
        desktopSeries.getData().add(new XYChart.Data<>("Jun", 214));

        XYChart.Series<String, Number> mobileSeries = new XYChart.Series<>();
        mobileSeries.setName("Mobile");
        mobileSeries.getData().add(new XYChart.Data<>("Jan", 80));
        mobileSeries.getData().add(new XYChart.Data<>("Feb", 200));
        mobileSeries.getData().add(new XYChart.Data<>("Mar", 120));
        mobileSeries.getData().add(new XYChart.Data<>("Apr", 190));
        mobileSeries.getData().add(new XYChart.Data<>("May", 130));
        mobileSeries.getData().add(new XYChart.Data<>("Jun", 140));

        visitorsAreaChart.getData().setAll(desktopSeries, mobileSeries);
    }

    private void initTodoList() {
        todoDatePicker.setValue(LocalDate.now());

        String[] tasks = {
                "Review pending payments in Stripe queue",
                "Approve incoming vendor catalogue updates",
                "Verify MongoDB order reconciliation logs",
                "Restock Nike Air Essentials Pullover"
        };

        boolean[] states = {true, true, false, false};

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
}
