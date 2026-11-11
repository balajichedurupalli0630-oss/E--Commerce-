package com.omnicart.service;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ToastService {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public enum ToastType {
        SUCCESS, INFO, ERROR, WARNING
    }

    public static void showSuccess(String message) {
        show(message, ToastType.SUCCESS);
    }

    public static void showError(String message) {
        show(message, ToastType.ERROR);
    }

    public static void showInfo(String message) {
        show(message, ToastType.INFO);
    }

    public static void show(String message, ToastType type) {
        if (primaryStage == null || !primaryStage.isShowing()) return;

        javafx.application.Platform.runLater(() -> {
            Popup popup = new Popup();
            popup.setAutoFix(true);
            popup.setAutoHide(true);

            HBox toastCard = new HBox(10);
            toastCard.setAlignment(Pos.CENTER_LEFT);
            toastCard.getStyleClass().add("toast-container");

            String iconText = "✔ ";
            String colorClass = "toast-success";
            if (type == ToastType.ERROR) {
                iconText = "✖ ";
                colorClass = "toast-error";
            } else if (type == ToastType.INFO) {
                iconText = "ℹ ";
                colorClass = "toast-info";
            } else if (type == ToastType.WARNING) {
                iconText = "⚠ ";
                colorClass = "toast-warning";
            }
            toastCard.getStyleClass().add(colorClass);

            Label iconLabel = new Label(iconText);
            iconLabel.getStyleClass().add("toast-icon");

            Label msgLabel = new Label(message);
            msgLabel.getStyleClass().add("toast-message");
            msgLabel.setWrapText(true);

            toastCard.getChildren().addAll(iconLabel, msgLabel);

            // Add CSS
            Scene scene = primaryStage.getScene();
            if (scene != null && !scene.getStylesheets().isEmpty()) {
                toastCard.getStylesheets().addAll(scene.getStylesheets());
            }

            popup.getContent().add(toastCard);

            // Position at bottom-right of window
            double x = primaryStage.getX() + primaryStage.getWidth() - 340;
            double y = primaryStage.getY() + primaryStage.getHeight() - 100;
            popup.show(primaryStage, x, y);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toastCard);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            PauseTransition delay = new PauseTransition(Duration.millis(3000));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(350), toastCard);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> popup.hide());

            SequentialTransition seq = new SequentialTransition(fadeIn, delay, fadeOut);
            seq.play();
        });
    }
}
