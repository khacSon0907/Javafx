package com.coffeeshop.coffeeshop.controller;

import com.coffeeshop.coffeeshop.util.Session;
import com.coffeeshop.coffeeshop.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class MainViewController {

    @FXML private Label lblWelcome;
    @FXML private Button btnCreateUser;
    @FXML private Button btnViewStaff;

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();

        if (user != null && user.getRole() != null) {
            String roleName = user.getRole().getNameRole().toLowerCase(); // "admin", "staff", ...
            lblWelcome.setText("Xin chào, " + user.getUserName() + " (" + roleName + ")");

            // Chỉ admin mới thấy nút tạo tài khoản
            if (!"admin".equals(roleName)) {
                btnCreateUser.setVisible(false);
            }
        } else {
            lblWelcome.setText("Không xác định người dùng");
            btnCreateUser.setVisible(false);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) throws Exception {
        Session.clear();
        Parent root = FXMLLoader.load(getClass().getResource("/views/Auth/login-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1200, 800));  // ✅ Giao diện cố định
        stage.setTitle("Đăng nhập");
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    private void handleCreateUser(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/views/Auth/create-user-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1200, 800));
        stage.setTitle("Tạo tài khoản nhân viên");
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    private void handleViewStaff(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/views/staff-list-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1200, 800));
        stage.setTitle("Danh sách nhân viên");
        stage.setResizable(false);
        stage.show();
    }
    @FXML
    private void handleCreateProduct(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/views/products/Products.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1200, 800));
        stage.setTitle("Quản lý sản phẩm");
        stage.setResizable(false);
        stage.show();
    }
}
