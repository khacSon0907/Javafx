package com.coffeeshop.coffeeshop.controller;

import com.coffeeshop.coffeeshop.model.User;
import com.coffeeshop.coffeeshop.util.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HomeUserController {

    @FXML private Label lblWelcome;
    @FXML private Button btnCheckIn;
    @FXML private Button btnOrderDrink;
    @FXML private Button btnCashier;
    @FXML private Button btnKitchen;

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();
        if (user != null && user.getRole() != null) {
            String roleName = user.getRole().getNameRole().toLowerCase(); // lấy nameRole từ Role

            lblWelcome.setText("Xin chào, " + user.getUsername() + " (" + roleName + ")");

            // Phân quyền hiển thị nút
            btnCheckIn.setVisible(true);
            btnOrderDrink.setVisible("staff".equals(roleName));
            btnCashier.setVisible("cashier".equals(roleName));
            btnKitchen.setVisible("kitchen".equals(roleName));
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
    private void handleCheckIn(ActionEvent event) {
        System.out.println("Thực hiện chấm công...");
    }

    @FXML
    private void handleOrderDrink(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Order/OrderView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Trang chính");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewOrder(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Order/OrderUpdate.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Trang chính");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCashier(ActionEvent event) {
        System.out.println("Thực hiện xuất hóa đơn...");
    }



    @FXML
    private void handleKitchen(ActionEvent event) {
        System.out.println("Nhận order để pha chế...");
    }
}
