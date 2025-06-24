package com.coffeeshop.coffeeshop.controller;

import com.coffeeshop.coffeeshop.dao.UserDAO;
import com.coffeeshop.coffeeshop.model.User;
import com.coffeeshop.coffeeshop.util.DBConnection;
import com.coffeeshop.coffeeshop.util.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;

public class LoginController {

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblMessage;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Vui lòng nhập đầy đủ tài khoản và mật khẩu.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            UserDAO dao = new UserDAO(conn);
            User user = dao.login(username, password);

            if (user != null) {
                Session.setCurrentUser(user); // Lưu thông tin user đăng nhập

                String roleName = user.getRole().getNameRole().toLowerCase(); // "admin", "staff", "kitchen", ...

                String viewPath;

                if ("admin".equals(roleName)) {
                    viewPath = "/views/Home/mainAdmin-view.fxml";
                } else {
                    viewPath = "/views/Home/HomeUser.fxml";
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root, 1200, 800); // Load full 1200x800
                stage.setScene(scene);
                stage.setTitle("Coffee Shop");
                stage.setResizable(false);
                stage.show();

            } else {
                lblMessage.setText("Sai tài khoản hoặc mật khẩu.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Lỗi hệ thống: không thể đăng nhập.");
        }
    }
}
