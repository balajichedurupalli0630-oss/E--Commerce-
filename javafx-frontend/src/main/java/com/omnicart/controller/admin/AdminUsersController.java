package com.omnicart.controller.admin;

import com.omnicart.api.AuthApiService;
import com.omnicart.model.User;
import com.omnicart.navigation.NavigationManager;
import com.omnicart.service.ImageLoaderService;
import com.omnicart.service.ToastService;
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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminUsersController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button addUserBtn;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Boolean> selectColumn;
    @FXML private TableColumn<User, String> avatarColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, User> actionsColumn;

    private final ObservableList<User> masterList = FXCollections.observableArrayList();
    private FilteredList<User> filteredList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addUserBtn.setGraphic(SvgIcons.createIcon(SvgIcons.PLUS, 14, Color.WHITE));

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

        avatarColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getImageUrl()));
        avatarColumn.setCellFactory(col -> new TableCell<>() {
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
                if (empty) {
                    setGraphic(null);
                } else {
                    String imgPath = path != null && !path.isBlank() ? path : "/users/1.png";
                    iv.setImage(ImageLoaderService.loadImage(imgPath));
                    setGraphic(container);
                }
            }
        });

        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrimaryEmail()));

        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        roleColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(role.toUpperCase());
                    badge.getStyleClass().add("status-badge");
                    if ("admin".equalsIgnoreCase(role)) {
                        badge.getStyleClass().add("role-badge-admin");
                    } else {
                        badge.getStyleClass().add("role-badge-user");
                    }
                    setAlignment(Pos.CENTER);
                    setGraphic(badge);
                }
            }
        });

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
                    if ("active".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("status-badge-success");
                    } else {
                        badge.getStyleClass().add("status-badge-failed");
                    }
                    setAlignment(Pos.CENTER);
                    setGraphic(badge);
                }
            }
        });

        actionsColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    MenuButton menu = new MenuButton("Actions");
                    menu.getStyleClass().add("table-action-menu");

                    MenuItem copyId = new MenuItem("Copy User ID");
                    copyId.setOnAction(e -> {
                        ClipboardContent content = new ClipboardContent();
                        content.putString(user.getId());
                        Clipboard.getSystemClipboard().setContent(content);
                        ToastService.showInfo("Copied User ID");
                    });

                    MenuItem deleteUser = new MenuItem("Delete user");
                    deleteUser.setOnAction(e -> deleteUser(user));

                    menu.getItems().addAll(copyId, new SeparatorMenuItem(), deleteUser);
                    setAlignment(Pos.CENTER);
                    setGraphic(menu);
                }
            }
        });

        filteredList = new FilteredList<>(masterList, u -> true);
        usersTable.setItems(filteredList);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(u -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase();
                return u.getFullName().toLowerCase().contains(lower) ||
                        u.getPrimaryEmail().toLowerCase().contains(lower) ||
                        (u.getUsername() != null && u.getUsername().toLowerCase().contains(lower));
            });
        });

        loadUsers();
    }

    private void loadUsers() {
        AuthApiService.getInstance().getUsers().thenAccept(users -> {
            Platform.runLater(() -> masterList.setAll(users));
        });
    }

    private void deleteUser(User user) {
        if (user.getId() == null) return;
        AuthApiService.getInstance().deleteUser(user.getId()).thenAccept(res -> {
            Platform.runLater(() -> {
                masterList.remove(user);
                ToastService.showSuccess("Deleted user: " + user.getFullName());
            });
        });
    }

    @FXML
    private void onAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/omnicart/fxml/admin/add-user-dialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(NavigationManager.getInstance().getPrimaryStage());
            stage.setTitle("Add New User");

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(
                    getClass().getResource("/com/omnicart/css/common.css").toExternalForm(),
                    getClass().getResource("/com/omnicart/css/admin.css").toExternalForm()
            );

            stage.setScene(scene);
            stage.showAndWait();
            loadUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
