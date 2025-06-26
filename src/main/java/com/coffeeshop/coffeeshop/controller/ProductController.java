package com.coffeeshop.coffeeshop.controller;

import com.coffeeshop.coffeeshop.dao.CategoriesDao;
import com.coffeeshop.coffeeshop.dao.ProductDAO;
import com.coffeeshop.coffeeshop.model.Categories;
import com.coffeeshop.coffeeshop.model.Product;
import com.coffeeshop.coffeeshop.util.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;

public class ProductController implements Initializable {

    @FXML private TableView<Product> tableProducts;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, String> colDescription;
    @FXML private TableColumn<Product, String> colStatus;

    @FXML private TextField txtName;
    @FXML private TextField txtPrice;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<Categories> cbCategory;
    @FXML private ImageView imgPreview;
    @FXML private TextField txtSearch;
    @FXML private Label lblStatus;
    @FXML private Label lblProductCount;

    private ProductDAO productDAO;
    private ObservableList<Product> productList;
    private Connection connection; // Keep connection alive





    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Keep connection alive throughout the controller lifecycle
            this.connection = DBConnection.getConnection();
            this.productDAO = new ProductDAO(connection);

            setupTableColumns();
            loadProductsInternal(); // Use internal method for initialization
            loadCategories(); // Load categories for ComboBox
            setupSearch(); // Setup search functionality
            setupTableSelection(); // Setup table selection listener
            updateStatus("Sẵn sàng", 0);

        } catch (Exception e) {
            System.err.println("Error initializing ProductController: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Initialization Error", "Failed to initialize the product controller: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nameProducts"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descriptionProducts"));

        // Safe category name display with null checking
        colCategory.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            if (product != null && product.getCategories() != null) {
                return new SimpleStringProperty(product.getCategories().getNameCategories());
            }
            return new SimpleStringProperty("No Category");
        });

        // Safe status display
        colStatus.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            if (product != null) {
                return new SimpleStringProperty(product.isActive() ? "Đang bán" : "Ngừng bán");
            }
            return new SimpleStringProperty("Unknown");
        });
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

    @FXML
    private void handleManageCategories(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/products/categories.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("quản lý danh mục");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void loadProducts() {
        try {
            if (productDAO == null) {
                System.err.println("ProductDAO is null!");
                updateStatus("Lỗi: ProductDAO không khả dụng", 0);
                return;
            }

            updateStatus("Đang tải dữ liệu...", 0);
            List<Product> products = productDAO.getAllProducts();
            System.out.println("Loaded " + products.size() + " products from database");

            // Debug: Print first few products
            for (int i = 0; i < Math.min(3, products.size()); i++) {
                Product p = products.get(i);
                System.out.println("Product " + i + ": " + p.getNameProducts() +
                        " - Category: " + (p.getCategories() != null ? p.getCategories().getNameCategories() : "null"));
            }

            productList = FXCollections.observableArrayList(products);
            tableProducts.setItems(productList);

            // Force table refresh
            tableProducts.refresh();

            updateStatus("Tải dữ liệu thành công", products.size());
            System.out.println("Table updated with " + productList.size() + " items");

        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            updateStatus("Lỗi tải dữ liệu", 0);
            showErrorAlert("Load Error", "Failed to load products: " + e.getMessage());
        }
    }

    // Internal method for loading products without FXML binding
    private void loadProductsInternal() {
        loadProducts();
    }

    private void loadCategories() {
        try {
            CategoriesDao categoriesDao = new CategoriesDao();
            List<Categories> categoryList = categoriesDao.getAllCategories();
            ObservableList<Categories> categories = FXCollections.observableArrayList(categoryList);
            cbCategory.setItems(categories);

            cbCategory.setConverter(new javafx.util.StringConverter<Categories>() {
                @Override
                public String toString(Categories category) {
                    return category != null ? category.getNameCategories() : "";
                }

                @Override
                public Categories fromString(String string) {
                    // Không cần dùng nếu không cho người dùng nhập tay
                    return null;
                }
            });

        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Category Error", "Không thể tải danh sách danh mục: " + e.getMessage());
        }
    }


    @FXML
    private void handleAddProduct() {
        try {
            // Validate input
            if (txtName.getText().trim().isEmpty()) {
                showErrorAlert("Validation Error", "Product name cannot be empty");
                return;
            }

            if (txtPrice.getText().trim().isEmpty()) {
                showErrorAlert("Validation Error", "Price cannot be empty");
                return;
            }

            if (cbCategory.getValue() == null) {
                showErrorAlert("Validation Error", "Please select a category");
                return;
            }

            Product p = new Product();
            p.setNameProducts(txtName.getText().trim());
            p.setDescriptionProducts(txtDescription.getText().trim());
            p.setPrice(Double.parseDouble(txtPrice.getText().trim()));
            p.setCategories(cbCategory.getValue());
            p.setActive(true);
            productDAO.insert(p);
            loadProductsInternal();
            clearForm();

            updateStatus("Thêm sản phẩm thành công", productList.size());
            showInfoAlert("Success", "Product added successfully!");

        } catch (NumberFormatException e) {
            showErrorAlert("Validation Error", "Please enter a valid number for price");
        } catch (Exception e) {
            System.err.println("Error adding product: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Add Error", "Failed to add product: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Selection Error", "Please select a product to delete");
            return;
        }

        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Product");
        confirmation.setContentText("Are you sure you want to delete: " + selected.getNameProducts() + "?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                productDAO.delete(selected.getId());
                loadProductsInternal();
                clearForm();
                updateStatus("Xóa sản phẩm thành công", productList.size());
                showInfoAlert("Success", "Product deleted successfully!");
            } catch (Exception e) {
                System.err.println("Error deleting product: " + e.getMessage());
                e.printStackTrace();
                showErrorAlert("Delete Error", "Failed to delete product: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditProduct() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Selection Error", "Please select a product to edit");
            return;
        }

        try {
            // Validate input
            if (txtName.getText().trim().isEmpty()) {
                showErrorAlert("Validation Error", "Product name cannot be empty");
                return;
            }

            if (txtPrice.getText().trim().isEmpty()) {
                showErrorAlert("Validation Error", "Price cannot be empty");
                return;
            }

            if (cbCategory.getValue() == null) {
                showErrorAlert("Validation Error", "Please select a category");
                return;
            }

            selected.setNameProducts(txtName.getText().trim());
            selected.setDescriptionProducts(txtDescription.getText().trim());
            selected.setPrice(Double.parseDouble(txtPrice.getText().trim()));
            selected.setCategories(cbCategory.getValue());

            productDAO.update(selected);
            loadProductsInternal();
            clearForm();

            updateStatus("Cập nhật sản phẩm thành công", productList.size());
            showInfoAlert("Success", "Product updated successfully!");

        } catch (NumberFormatException e) {
            showErrorAlert("Validation Error", "Please enter a valid number for price");
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Update Error", "Failed to update product: " + e.getMessage());
        }
    }

    @FXML
    private void handleTableSelection() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected != null) {
            populateForm(selected);
            updateStatus("Đã chọn sản phẩm: " + selected.getNameProducts(), productList.size());
        }
    }

    private void setupTableSelection() {
        tableProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
                updateStatus("Đã chọn sản phẩm: " + newSelection.getNameProducts(), productList.size());
            }
        });
    }

    private void setupSearch() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filterProducts(newValue);
            });
        }
    }

    private void filterProducts(String searchText) {
        if (productList == null) return;

        if (searchText == null || searchText.trim().isEmpty()) {
            tableProducts.setItems(productList);
            updateStatus("Hiển thị tất cả sản phẩm", productList.size());
        } else {
            ObservableList<Product> filteredList = FXCollections.observableArrayList();
            String lowerCaseFilter = searchText.toLowerCase();

            for (Product product : productList) {
                if (product.getNameProducts().toLowerCase().contains(lowerCaseFilter) ||
                        product.getDescriptionProducts().toLowerCase().contains(lowerCaseFilter) ||
                        (product.getCategories() != null &&
                                product.getCategories().getNameCategories().toLowerCase().contains(lowerCaseFilter))) {
                    filteredList.add(product);
                }
            }

            tableProducts.setItems(filteredList);
            updateStatus("Tìm thấy " + filteredList.size() + " sản phẩm", productList.size());
        }
    }

    private void updateStatus(String message, int totalProducts) {
        if (lblStatus != null) {
            lblStatus.setText(message);
        }
        if (lblProductCount != null) {
            lblProductCount.setText("Tổng: " + totalProducts + " sản phẩm");
        }
    }

    private void populateForm(Product product) {
        txtName.setText(product.getNameProducts());
        txtDescription.setText(product.getDescriptionProducts());
        txtPrice.setText(String.valueOf(product.getPrice()));
        cbCategory.setValue(product.getCategories());
    }



    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private void clearForm() {
        txtName.clear();
        txtDescription.clear();
        txtPrice.clear();
        cbCategory.setValue(null);
        if (txtSearch != null) {
            txtSearch.clear();
        }
        tableProducts.getSelectionModel().clearSelection();
        updateStatus("Form đã được xóa", productList != null ? productList.size() : 0);
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}