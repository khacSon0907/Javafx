package com.coffeeshop.coffeeshop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // 👉 Đổi sang login-view.fxml
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/Auth/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200,800);
        stage.setTitle("Đăng nhập");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
