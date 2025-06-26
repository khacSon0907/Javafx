package com.coffeeshop.coffeeshop.controller;

import com.coffeeshop.coffeeshop.dao.CategoriesDao;
import com.coffeeshop.coffeeshop.model.Categories;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CategoriesController implements Initializable {

    @FXML
    private TextField txtCategoryName;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnClear;

    @FXML
    private TableView<Categories> tableCategories;

    @FXML
    private TableColumn<Categories, Integer> colId;

    @FXML
    private TableColumn<Categories, String> colName;

    @FXML
    private Label lblStatus;

    // DAO instance
    private CategoriesDao categoriesDao;
    private ObservableList<Categories> categoriesList;
    private Categories selectedCategory;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo DAO
        categoriesDao = new CategoriesDao();
        categoriesList = FXCollections.observableArrayList();

        // Thiết lập cột của bảng
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nameCategories"));

        // Gán dữ liệu cho bảng
        tableCategories.setItems(categoriesList);

        // Thêm sự kiện click vào bảng
        tableCategories.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectedCategory = newValue;
                        txtCategoryName.setText(newValue.getNameCategories());
                        updateButtonStates();
                    }
                }
        );

        // Tải dữ liệu ban đầu
        loadCategories();
        updateButtonStates();
    }

    // Tải dữ liệu từ database
    private void loadCategories() {
        try {
            List<Categories> categories = categoriesDao.getAllCategories();
            categoriesList.clear();
            categoriesList.addAll(categories);
            updateStatus("Đã tải " + categories.size() + " danh mục");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu: " + e.getMessage());
            updateStatus("Lỗi khi tải dữ liệu");
        }
    }

    // Thêm danh mục mới
    @FXML
    private void handleAdd() {
        String categoryName = txtCategoryName.getText().trim();

        if (categoryName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập tên danh mục!");
            return;
        }

        // Kiểm tra trùng tên
        if (isDuplicateName(categoryName, null)) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Tên danh mục đã tồn tại!");
            return;
        }

        try {
            Categories newCategory = new Categories(0, categoryName);
            boolean success = categoriesDao.addCategory(newCategory);

            if (success) {
                loadCategories(); // Reload dữ liệu
                clearForm();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm danh mục thành công!");
                updateStatus("Đã thêm danh mục: " + categoryName);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm danh mục!");
                updateStatus("Lỗi khi thêm danh mục");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi thêm danh mục: " + e.getMessage());
            updateStatus("Lỗi khi thêm danh mục");
        }
    }

    // Cập nhật danh mục
    @FXML
    private void handleUpdate() {
        if (selectedCategory == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn danh mục cần cập nhật!");
            return;
        }

        String categoryName = txtCategoryName.getText().trim();

        if (categoryName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập tên danh mục!");
            return;
        }

        // Kiểm tra trùng tên (trừ chính nó)
        if (isDuplicateName(categoryName, selectedCategory.getId())) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Tên danh mục đã tồn tại!");
            return;
        }

        try {
            selectedCategory.setNameCategories(categoryName);
            boolean success = categoriesDao.updateCategory(selectedCategory);

            if (success) {
                loadCategories(); // Reload dữ liệu
                clearForm();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật danh mục thành công!");
                updateStatus("Đã cập nhật danh mục: " + categoryName);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật danh mục!");
                updateStatus("Lỗi khi cập nhật danh mục");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi cập nhật danh mục: " + e.getMessage());
            updateStatus("Lỗi khi cập nhật danh mục");
        }
    }

    // Xóa danh mục
    @FXML
    private void handleDelete() {
        if (selectedCategory == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn danh mục cần xóa!");
            return;
        }

        // Xác nhận xóa
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa danh mục này?");
        confirmAlert.setContentText("Danh mục: " + selectedCategory.getNameCategories());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = categoriesDao.deleteCategory(selectedCategory.getId());

                if (success) {
                    loadCategories(); // Reload dữ liệu
                    clearForm();
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa danh mục thành công!");
                    updateStatus("Đã xóa danh mục: " + selectedCategory.getNameCategories());
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa danh mục!");
                    updateStatus("Lỗi khi xóa danh mục");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi xóa danh mục: " + e.getMessage());
                updateStatus("Lỗi khi xóa danh mục");
            }
        }
    }

    // Làm mới form
    @FXML
    private void handleClear() {
        clearForm();
        updateStatus("Đã làm mới form");
    }

    // Xóa form
    private void clearForm() {
        txtCategoryName.clear();
        selectedCategory = null;
        tableCategories.getSelectionModel().clearSelection();
        updateButtonStates();
    }

    // Cập nhật trạng thái nút
    private void updateButtonStates() {
        boolean hasSelection = selectedCategory != null;
        btnUpdate.setDisable(!hasSelection);
        btnDelete.setDisable(!hasSelection);
    }

    // Kiểm tra tên trùng lặp
    private boolean isDuplicateName(String name, Integer excludeId) {
        for (Categories category : categoriesList) {
            if (category.getNameCategories().equalsIgnoreCase(name)) {
                if (excludeId == null || category.getId() == excludeId) {
                    return true;
                }
            }
        }
        return false;
    }

    // Hiển thị thông báo
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Cập nhật trạng thái
    private void updateStatus(String message) {
        lblStatus.setText(message);
    }
}