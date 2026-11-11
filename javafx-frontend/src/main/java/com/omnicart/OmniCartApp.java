package com.omnicart;

import com.omnicart.config.SessionManager;
import com.omnicart.model.User;
import com.omnicart.navigation.NavigationManager;
import com.omnicart.service.ImageLoaderService;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class OmniCartApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize default demo session (can be customer, or admin if requested)
        User defaultUser = new User("user_cust_01", "johndoe", "John", "Doe", "john.doe@example.com", "user");
        defaultUser.setImageUrl("/users/2.png");
        SessionManager.getInstance().setSession(defaultUser, "demo_token_cust");

        // Set application window icon
        Image icon = ImageLoaderService.loadImage("/logo.png");
        if (icon != null) {
            primaryStage.getIcons().add(icon);
        }

        NavigationManager.getInstance().init(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
