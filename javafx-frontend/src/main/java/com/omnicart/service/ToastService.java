package com.omnicart.service;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ToastService {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public enum ToastType {
        SUCCESS("✔ ", "toast-success"),
        INFO("ℹ ", "toast-info"),
        ERROR("✖ ", "toast-error"),
        WARNING("⚠ ", "toast-warning");

        final String icon;
        final String styleClass;

        ToastType(String icon, String styleClass) {
            this.icon = icon;
            this.styleClass = styleClass;
        }
    }

    public static void showSuccess(String message) { show(message, ToastType.SUCCESS); }
    public static void showError(String message)   { show(message, ToastType.ERROR); }
    public static void showInfo(String message)    { show(message, ToastType.INFO); }
    public static void showWarning(String message) { show(message, ToastType.WARNING); }

    public static void show(String message, ToastType type) {
        if (primaryStage == null || !primaryStage.isShowing()) return;

        Platform.runLater(() -> {
            var popup = new Popup();
            popup.setAutoFix(true);
            popup.setAutoHide(true);

            var toastType = type != null ? type : ToastType.INFO;
            var iconLabel = new Label(toastType.icon);
            iconLabel.getStyleClass().add("toast-icon");

            var msgLabel = new Label(message);
            msgLabel.getStyleClass().add("toast-message");
            msgLabel.setWrapText(true);

            var toastCard = new HBox(10, iconLabel, msgLabel);
            toastCard.setAlignment(Pos.CENTER_LEFT);
            toastCard.getStyleClass().addAll("toast-container", toastType.styleClass);

            var scene = primaryStage.getScene();
            if (scene != null && !scene.getStylesheets().isEmpty()) {
                toastCard.getStylesheets().addAll(scene.getStylesheets());
            }

            popup.getContent().add(toastCard);
            popup.show(primaryStage, primaryStage.getX() + primaryStage.getWidth() - 340, primaryStage.getY() + primaryStage.getHeight() - 100);

            var fadeIn = new FadeTransition(Duration.millis(250), toastCard);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            var fadeOut = new FadeTransition(Duration.millis(350), toastCard);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> popup.hide());

            new SequentialTransition(fadeIn, new PauseTransition(Duration.millis(3000)), fadeOut).play();
        });
    }
}
