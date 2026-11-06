package com.omnicart.controller.admin;

import com.omnicart.config.SessionManager;
import com.omnicart.navigation.NavigationManager;
import com.omnicart.navigation.ViewType;
import com.omnicart.service.ImageLoaderService;
import com.omnicart.service.ToastService;
import com.omnicart.util.SvgIcons;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminLayoutController implements Initializable {

    @FXML private ImageView adminLogoImageView;
    @FXML private Button homeNavBtn;
    @FXML private Button productsNavBtn;
    @FXML private Button ordersNavBtn;
    @FXML private Button usersNavBtn;
    @FXML private Button addProductNavBtn;
    @FXML private Button addCategoryNavBtn;
    @FXML private Button addUserNavBtn;
    @FXML private Button returnToStoreBtn;
    @FXML private Label adminUserNameLabel;
    @FXML private Label breadcrumbLabel;
    @FXML private StackPane adminContentArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NavigationManager.getInstance().setContentArea(adminContentArea);

        adminLogoImageView.setImage(ImageLoaderService.loadImage("/logo.png"));

        // Setup SVG icons
        homeNavBtn.setGraphic(SvgIcons.createIcon(SvgIcons.HOME, 16, Color.valueOf("#4b5563")));
        productsNavBtn.setGraphic(SvgIcons.createIcon(SvgIcons.SHIRT, 16, Color.valueOf("#4b5563")));
        ordersNavBtn.setGraphic(SvgIcons.createIcon(SvgIcons.SHOPPING_BAG, 16, Color.valueOf("#4b5563")));
        usersNavBtn.setGraphic(SvgIcons.createIcon(SvgIcons.USER, 16, Color.valueOf("#4b5563")));

        addProductNavBtn.setGraphic(SvgIcons.createIcon(SvgIcons.PLUS, 14, Color.valueOf("#6b7280")));
        addCategoryNavBtn.setGraphic(SvgIcons.createIcon(SvgIcons.PLUS, 14, Color.valueOf("#6b7280")));
        addUserNavBtn.setGraphic(SvgIcons.createIcon(SvgIcons.PLUS, 14, Color.valueOf("#6b7280")));
        returnToStoreBtn.setGraphic(SvgIcons.createIcon(SvgIcons.SHOPPING_BAG, 14, Color.valueOf("#374151")));

        if (SessionManager.getInstance().getCurrentUser() != null) {
            adminUserNameLabel.setText(SessionManager.getInstance().getCurrentUser().getFullName());
        } else {
            adminUserNameLabel.setText("System Admin");
        }
    }

    @FXML
    private void onNavigateHome() {
        breadcrumbLabel.setText("Dashboard");
        NavigationManager.getInstance().navigateTo(ViewType.ADMIN_DASHBOARD);
    }

    @FXML
    private void onNavigateProducts() {
        breadcrumbLabel.setText("Dashboard / Products");
        NavigationManager.getInstance().navigateTo(ViewType.ADMIN_PRODUCTS);
    }

    @FXML
    private void onNavigateOrders() {
        breadcrumbLabel.setText("Dashboard / Transactions");
        NavigationManager.getInstance().navigateTo(ViewType.ADMIN_ORDERS);
    }

    @FXML
    private void onNavigateUsers() {
        breadcrumbLabel.setText("Dashboard / Users");
        NavigationManager.getInstance().navigateTo(ViewType.ADMIN_USERS);
    }

    @FXML
    private void onOpenAddProduct() {
        openDialog("/com/omnicart/fxml/admin/add-product-dialog.fxml", "Add New Product");
    }

    @FXML
    private void onOpenAddCategory() {
        openDialog("/com/omnicart/fxml/admin/add-category-dialog.fxml", "Add New Category");
    }

    @FXML
    private void onOpenAddUser() {
        openDialog("/com/omnicart/fxml/admin/add-user-dialog.fxml", "Add New User");
    }

    @FXML
    private void onReturnToStore() {
        NavigationManager.getInstance().showClientApp();
    }

    @FXML
    private void onSwitchPortal() {
        SessionManager.getInstance().logout();
        NavigationManager.getInstance().showPortalSelector();
    }

    @FXML
    private void onSignOut() {
        SessionManager.getInstance().logout();
        ToastService.showInfo("Admin signed out");
        NavigationManager.getInstance().showPortalSelector();
    }

    private void openDialog(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(NavigationManager.getInstance().getPrimaryStage());
            stage.setTitle(title);

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(
                    getClass().getResource("/com/omnicart/css/common.css").toExternalForm(),
                    getClass().getResource("/com/omnicart/css/admin.css").toExternalForm()
            );

            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
