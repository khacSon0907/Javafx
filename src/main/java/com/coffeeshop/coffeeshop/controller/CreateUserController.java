package com.coffeeshop.coffeeshop.controller;

import com.coffeeshop.coffeeshop.dao.UserDAO;
import com.coffeeshop.coffeeshop.model.Role;
import com.coffeeshop.coffeeshop.model.User;
import com.coffeeshop.coffeeshop.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class CreateUserController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private Label lblMessage;

    // Dùng để map từ tên role sang đối tượng Role (có id)
    private Map<String, Role> roleMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadRoles();
    }

    private void loadRoles() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, nameRole FROM Roles";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> roleNames = FXCollections.observableArrayList();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("nameRole");

                Role role = new Role();
                role.setId(id);
                role.setNameRole(name);

                roleNames.add(name);
                roleMap.put(name, role);
            }
            cbRole.setItems(roleNames);

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Lỗi tải danh sách vai trò.");
        }
    }

    @FXML
    private void handleSaveUser(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String selectedRoleName = cbRole.getValue();

        if (username.isEmpty() || password.isEmpty() || selectedRoleName == null) {
            lblMessage.setText("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        Role selectedRole = roleMap.get(selectedRoleName);
        if (selectedRole == null) {
            lblMessage.setText("Vai trò không hợp lệ.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            UserDAO dao = new UserDAO(conn);

            if (dao.isUsernameTaken(username)) {
                lblMessage.setText("Tên đăng nhập đã tồn tại.");
                return;
            }

            User user = new User();
            user.setUsername(username);
            user.setPassword(password); // ❗ Nên mã hóa sau này
            user.setRole(selectedRole); // ✅ Gán đúng đối tượng Role

            boolean success = dao.register(user);
            if (success) {
                lblMessage.setStyle("-fx-text-fill: green;");
                lblMessage.setText("Tạo tài khoản thành công!");
                txtUsername.clear();
                txtPassword.clear();
                cbRole.getSelectionModel().clearSelection();
            } else {
                lblMessage.setText("Lỗi khi tạo tài khoản.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Lỗi kết nối cơ sở dữ liệu.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Home/mainAdmin-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Trang chính");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
