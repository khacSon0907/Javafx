package com.coffeeshop.coffeeshop.controller;

import com.coffeeshop.coffeeshop.dao.CategoriesDao;
import com.coffeeshop.coffeeshop.dao.ProductDAO;
import com.coffeeshop.coffeeshop.model.Categories;
import com.coffeeshop.coffeeshop.model.Product;
import com.coffeeshop.coffeeshop.util.DBConnection;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class ProductController implements Initializable {

    @FXML private TableView<Product> tableProducts;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Float> colDiscount;
    @FXML private TableColumn<Product, Double> colFinalPrice;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, String> colDescription;
    @FXML private TableColumn<Product, String> colStatus;
    @FXML private TableColumn<Product, ImageView> colImage;

    @FXML private TextField txtName;
    @FXML private TextField txtPrice;
    @FXML private TextField txtDiscount;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<Categories> cbCategory;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ImageView imgPreview;
    @FXML private TextField txtSearch;
    @FXML private Label lblStatus;
    @FXML private Label lblProductCount;
    @FXML private Button btnChooseImage;
    @FXML private Button btnRemoveImage;

    private ProductDAO productDAO;
    private ObservableList<Product> productList;
    private Connection connection;
    private String selectedImagePath;
    private final String IMAGE_DIRECTORY = "src/main/resources/images/products/";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            createImageDirectory();

            this.connection = DBConnection.getConnection();
            this.productDAO = new ProductDAO(connection);

            setupTableColumns();
            loadProductsInternal();
            loadCategories();
            setupSearch();
            setupTableSelection();
            setupImagePreview();
            setupStatusComboBox();

            updateStatus("Sẵn sàng", 0);

        } catch (Exception e) {
            System.err.println("Error initializing ProductController: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Initialization Error", "Failed to initialize the product controller: " + e.getMessage());
        }
    }

    private void setupStatusComboBox() {
        if (cbStatus != null) {
            cbStatus.setItems(FXCollections.observableArrayList("Đang bán", "Ngừng bán"));
        }
    }

    private void createImageDirectory() {
        try {
            Path imageDir = Paths.get(IMAGE_DIRECTORY);
            if (!Files.exists(imageDir)) {
                Files.createDirectories(imageDir);
                System.out.println("Created image directory: " + IMAGE_DIRECTORY);
            }
        } catch (IOException e) {
            System.err.println("Error creating image directory: " + e.getMessage());
        }
    }

    private void setupImagePreview() {
        if (imgPreview != null) {
            imgPreview.setFitWidth(200);
            imgPreview.setFitHeight(150);
            imgPreview.setPreserveRatio(true);
            imgPreview.setSmooth(true);

            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/no-image.png"));
                imgPreview.setImage(defaultImage);
            } catch (Exception e) {
                System.out.println("No default image found");
            }
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"
        ));
        Stage stage = (Stage) btnChooseImage.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                imgPreview.setImage(image);
                selectedImagePath = selectedFile.getAbsolutePath();
                btnRemoveImage.setDisable(false);
                updateStatus("Đã chọn ảnh: " + selectedFile.getName(), productList != null ? productList.size() : 0);
            } catch (Exception e) {
                showErrorAlert("Image Error", "Không thể tải ảnh: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRemoveImage() {
        selectedImagePath = null;
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/no-image.png"));
            imgPreview.setImage(defaultImage);
        } catch (Exception e) {
            imgPreview.setImage(null);
        }
        btnRemoveImage.setDisable(true);
        updateStatus("Đã xóa ảnh", productList != null ? productList.size() : 0);
    }

    private String saveImageToDirectory(String sourceImagePath, String productName) {
        if (sourceImagePath == null || sourceImagePath.isEmpty()) return null;

        try {
            Path sourcePath = Paths.get(sourceImagePath);
            String fileExtension = getFileExtension(sourceImagePath);
            String newFileName = productName.replaceAll("[^a-zA-Z0-9]", "_") +
                    "_" + UUID.randomUUID().toString().substring(0, 8) +
                    fileExtension;

            Path targetPath = Paths.get(IMAGE_DIRECTORY + newFileName);
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return IMAGE_DIRECTORY + newFileName;

        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            showErrorAlert("Save Error", "Không thể lưu ảnh: " + e.getMessage());
            return null;
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1)
                ? fileName.substring(lastDotIndex)
                : ".jpg";
    }

    private void loadImageToPreview(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    imgPreview.setImage(new Image(imageFile.toURI().toString()));
                    btnRemoveImage.setDisable(false);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
        try {
            imgPreview.setImage(new Image(getClass().getResourceAsStream("/images/no-image.png")));
        } catch (Exception e) {
            imgPreview.setImage(null);
        }
        btnRemoveImage.setDisable(true);
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colImage.setCellValueFactory(cellData -> {
            Product p = cellData.getValue();
            if (p != null && p.getImage() != null && !p.getImage().isEmpty()) {
                File file = new File(p.getImage());
                if (file.exists()) {
                    ImageView iv = new ImageView(new Image(file.toURI().toString()));
                    iv.setFitWidth(80);
                    iv.setFitHeight(50);
                    iv.setPreserveRatio(true);
                    return new SimpleObjectProperty<>(iv);
                }
            }
            return new SimpleObjectProperty<>(new ImageView());
        });

        colName.setCellValueFactory(new PropertyValueFactory<>("nameProducts"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));

        colFinalPrice.setCellValueFactory(cellData -> {
            Product p = cellData.getValue();
            double finalPrice = p.getPrice() * (1 - p.getDiscount() / 100.0);
            return new SimpleObjectProperty<>(finalPrice);
        });

        colDescription.setCellValueFactory(new PropertyValueFactory<>("descriptionProducts"));

        colCategory.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            if (product != null && product.getCategories() != null) {
                return new SimpleStringProperty(product.getCategories().getNameCategories());
            }
            return new SimpleStringProperty("No Category");
        });

        colStatus.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            if (product != null) {
                return new SimpleStringProperty(product.isActive() ? "Đang bán" : "Ngừng bán");
            }
            return new SimpleStringProperty("Unknown");
        });
    }

    @FXML
    private void handleAddProduct() {
        try {
            if (txtName.getText().trim().isEmpty()) {
                showErrorAlert("Validation Error", "Tên sản phẩm không được để trống!");
                return;
            }

            if (txtPrice.getText().trim().isEmpty()) {
                showErrorAlert("Validation Error", "Giá không được để trống!");
                return;
            }

            if (cbCategory.getValue() == null) {
                showErrorAlert("Validation Error", "Vui lòng chọn danh mục!");
                return;
            }

            if (cbStatus.getValue() == null) {
                showErrorAlert("Validation Error", "Vui lòng chọn trạng thái!");
                return;
            }

            Product p = new Product();
            p.setNameProducts(txtName.getText().trim());
            p.setDescriptionProducts(txtDescription.getText().trim());
            p.setPrice(Double.parseDouble(txtPrice.getText().trim()));
            p.setCategories(cbCategory.getValue());
            p.setActive(cbStatus.getValue().equals("Đang bán"));

            float discount = 0f;
            if (!txtDiscount.getText().trim().isEmpty()) {
                discount = Float.parseFloat(txtDiscount.getText().trim());
            }
            p.setDiscount(discount);

            if (selectedImagePath != null) {
                String savedImagePath = saveImageToDirectory(selectedImagePath, p.getNameProducts());
                p.setImage(savedImagePath);
            }

            productDAO.insert(p);
            loadProductsInternal();
            clearForm();
            updateStatus("Thêm sản phẩm thành công", productList.size());
            showInfoAlert("Success", "Thêm sản phẩm thành công!");

        } catch (NumberFormatException e) {
            showErrorAlert("Validation Error", "Giá và Giảm giá phải là số hợp lệ!");
        } catch (Exception e) {
            showErrorAlert("Add Error", "Không thể thêm sản phẩm: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditProduct() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Selection Error", "Vui lòng chọn sản phẩm để sửa!");
            return;
        }

        try {
            if (txtName.getText().trim().isEmpty()) {
                showErrorAlert("Validation Error", "Tên sản phẩm không được để trống!");
                return;
            }

            if (txtPrice.getText().trim().isEmpty()) {
                showErrorAlert("Validation Error", "Giá không được để trống!");
                return;
            }

            if (cbCategory.getValue() == null) {
                showErrorAlert("Validation Error", "Vui lòng chọn danh mục!");
                return;
            }

            if (cbStatus.getValue() == null) {
                showErrorAlert("Validation Error", "Vui lòng chọn trạng thái!");
                return;
            }

            selected.setNameProducts(txtName.getText().trim());
            selected.setDescriptionProducts(txtDescription.getText().trim());
            selected.setPrice(Double.parseDouble(txtPrice.getText().trim()));
            selected.setCategories(cbCategory.getValue());
            selected.setActive(cbStatus.getValue().equals("Đang bán"));

            float discount = 0f;
            if (!txtDiscount.getText().trim().isEmpty()) {
                discount = Float.parseFloat(txtDiscount.getText().trim());
            }
            selected.setDiscount(discount);

            if (selectedImagePath != null) {
                if (selected.getImage() != null && !selected.getImage().isEmpty()
                        && !selectedImagePath.equals(selected.getImage())) {
                    try {
                        Files.deleteIfExists(Paths.get(selected.getImage()));
                    } catch (IOException e) {
                        System.err.println("Error deleting old image: " + e.getMessage());
                    }
                }
                String savedImagePath = saveImageToDirectory(selectedImagePath, selected.getNameProducts());
                selected.setImage(savedImagePath);
            } else {
                selected.setImage(selected.getImage());
            }

            productDAO.update(selected);
            loadProductsInternal();
            clearForm();
            updateStatus("Cập nhật sản phẩm thành công", productList.size());
            showInfoAlert("Success", "Cập nhật sản phẩm thành công!");

        } catch (NumberFormatException e) {
            showErrorAlert("Validation Error", "Giá và Giảm giá phải là số hợp lệ!");
        } catch (Exception e) {
            showErrorAlert("Update Error", "Không thể cập nhật sản phẩm: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Selection Error", "Vui lòng chọn sản phẩm để xóa!");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận xóa");
        confirmation.setHeaderText("Xóa sản phẩm");
        confirmation.setContentText("Bạn chắc chắn muốn xóa: " + selected.getNameProducts() + "?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                if (selected.getImage() != null && !selected.getImage().isEmpty()) {
                    Files.deleteIfExists(Paths.get(selected.getImage()));
                }
                productDAO.delete(selected.getId());
                loadProductsInternal();
                clearForm();
                updateStatus("Xóa sản phẩm thành công", productList.size());
                showInfoAlert("Success", "Xóa sản phẩm thành công!");
            } catch (Exception e) {
                showErrorAlert("Delete Error", "Không thể xóa sản phẩm: " + e.getMessage());
            }
        }
    }

    private void populateForm(Product product) {
        txtName.setText(product.getNameProducts());
        txtDescription.setText(product.getDescriptionProducts());
        txtPrice.setText(String.valueOf(product.getPrice()));
        cbCategory.setValue(product.getCategories());
        cbStatus.setValue(product.isActive() ? "Đang bán" : "Ngừng bán");
        txtDiscount.setText(String.valueOf(product.getDiscount()));
        loadImageToPreview(product.getImage());
        selectedImagePath = product.getImage();
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private void clearForm() {
        txtName.clear();
        txtDescription.clear();
        txtPrice.clear();
        txtDiscount.clear();
        cbCategory.setValue(null);
        cbStatus.setValue(null);
        selectedImagePath = null;

        try {
            imgPreview.setImage(new Image(getClass().getResourceAsStream("/images/no-image.png")));
        } catch (Exception e) {
            imgPreview.setImage(null);
        }
        btnRemoveImage.setDisable(true);
        txtSearch.clear();
        tableProducts.getSelectionModel().clearSelection();
        updateStatus("Form đã được xóa", productList != null ? productList.size() : 0);
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
            productList = FXCollections.observableArrayList(products);
            tableProducts.setItems(productList);
            tableProducts.refresh();
            updateStatus("Tải dữ liệu thành công", products.size());

        } catch (Exception e) {
            updateStatus("Lỗi tải dữ liệu", 0);
            showErrorAlert("Load Error", "Không thể tải sản phẩm: " + e.getMessage());
        }
    }

    private void loadProductsInternal() {
        loadProducts();
    }

    private void loadCategories() {
        try {
            CategoriesDao categoriesDao = new CategoriesDao();
            List<Categories> categoryList = categoriesDao.getAllCategories();
            ObservableList<Categories> categories = FXCollections.observableArrayList(categoryList);
            cbCategory.setItems(categories);
            cbCategory.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(Categories object) {
                    return object != null ? object.getNameCategories() : "";
                }
                @Override
                public Categories fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            showErrorAlert("Category Error", "Không thể tải danh mục: " + e.getMessage());
        }
    }

    private void setupTableSelection() {
        tableProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateForm(newSel);
                updateStatus("Đã chọn sản phẩm: " + newSel.getNameProducts(), productList.size());
            }
        });
    }

    private void setupSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterProducts(newVal));
    }

    private void filterProducts(String text) {
        if (productList == null) return;

        if (text == null || text.trim().isEmpty()) {
            tableProducts.setItems(productList);
            updateStatus("Hiển thị tất cả sản phẩm", productList.size());
        } else {
            ObservableList<Product> filteredList = FXCollections.observableArrayList();
            String lower = text.toLowerCase();
            for (Product p : productList) {
                if (p.getNameProducts().toLowerCase().contains(lower) ||
                        p.getDescriptionProducts().toLowerCase().contains(lower) ||
                        (p.getCategories() != null && p.getCategories().getNameCategories().toLowerCase().contains(lower))) {
                    filteredList.add(p);
                }
            }
            tableProducts.setItems(filteredList);
            updateStatus("Tìm thấy " + filteredList.size() + " sản phẩm", productList.size());
        }
    }

    private void updateStatus(String msg, int total) {
        if (lblStatus != null) lblStatus.setText(msg);
        if (lblProductCount != null) lblProductCount.setText("Tổng: " + total + " sản phẩm");
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
