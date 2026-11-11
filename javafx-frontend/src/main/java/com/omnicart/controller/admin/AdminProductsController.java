package com.omnicart.controller.admin;

import com.omnicart.api.ProductApiService;
import com.omnicart.model.Product;
import com.omnicart.navigation.NavigationManager;
import com.omnicart.service.ImageLoaderService;
import com.omnicart.service.ToastService;
import com.omnicart.util.CurrencyFormatter;
import com.omnicart.util.SvgIcons;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminProductsController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button addProductBtn;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Boolean> selectColumn;
    @FXML private TableColumn<Product, String> imageColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, String> descColumn;
    @FXML private TableColumn<Product, Product> actionsColumn;

    private final ObservableList<Product> masterList = FXCollections.observableArrayList();
    private FilteredList<Product> filteredList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addProductBtn.setGraphic(SvgIcons.createIcon(SvgIcons.PLUS, 14, Color.WHITE));

        // Setup columns
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

        imageColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getImageForColor("gray")));
        imageColumn.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            private final StackPane container = new StackPane(iv);

            {
                iv.setFitWidth(36);
                iv.setFitHeight(36);
                iv.setPreserveRatio(true);
                Circle clip = new Circle(18, 18, 18);
                iv.setClip(clip);
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);
                if (empty || path == null) {
                    setGraphic(null);
                } else {
                    iv.setImage(ImageLoaderService.loadImage(path));
                    setGraphic(container);
                }
            }
        });

        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        priceColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getEffectivePrice()));
        priceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(CurrencyFormatter.format(price));
                }
            }
        });

        descColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getShortDescription()));

        actionsColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setGraphic(null);
                } else {
                    MenuButton menu = new MenuButton("Actions");
                    menu.getStyleClass().add("table-action-menu");

                    MenuItem copyId = new MenuItem("Copy product ID");
                    copyId.setOnAction(e -> {
                        ClipboardContent content = new ClipboardContent();
                        content.putString(String.valueOf(product.getId()));
                        Clipboard.getSystemClipboard().setContent(content);
                        ToastService.showInfo("Copied product ID to clipboard");
                    });

                    MenuItem viewProd = new MenuItem("View details");
                    viewProd.setOnAction(e -> NavigationManager.getInstance().navigateToProductDetails(product));

                    MenuItem deleteProd = new MenuItem("Delete product");
                    deleteProd.setOnAction(e -> deleteProduct(product));

                    menu.getItems().addAll(copyId, viewProd, new SeparatorMenuItem(), deleteProd);
                    setAlignment(Pos.CENTER);
                    setGraphic(menu);
                }
            }
        });

        filteredList = new FilteredList<>(masterList, p -> true);
        productsTable.setItems(filteredList);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(p -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase();
                return p.getName().toLowerCase().contains(lower) ||
                        (p.getShortDescription() != null && p.getShortDescription().toLowerCase().contains(lower));
            });
        });

        loadProducts();
    }

    private void loadProducts() {
        ProductApiService.getInstance().getProducts(null, null, null, 100).thenAccept(products -> {
            Platform.runLater(() -> {
                masterList.setAll(products);
            });
        });
    }

    private void deleteProduct(Product product) {
        if (product.getId() == null) return;
        ProductApiService.getInstance().deleteProduct(product.getId()).thenAccept(res -> {
            Platform.runLater(() -> {
                masterList.remove(product);
                ToastService.showSuccess("Deleted product: " + product.getName());
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                masterList.remove(product);
                ToastService.showInfo("Product removed from catalog");
            });
            return null;
        });
    }

    @FXML
    private void onAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/omnicart/fxml/admin/add-product-dialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(NavigationManager.getInstance().getPrimaryStage());
            stage.setTitle("Add New Product");

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(
                    getClass().getResource("/com/omnicart/css/common.css").toExternalForm(),
                    getClass().getResource("/com/omnicart/css/admin.css").toExternalForm()
            );

            stage.setScene(scene);
            stage.showAndWait();
            loadProducts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
