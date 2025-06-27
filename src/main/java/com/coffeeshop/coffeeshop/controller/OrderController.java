package com.coffeeshop.coffeeshop.controller;

import com.coffeeshop.coffeeshop.dao.CategoriesDao;
import com.coffeeshop.coffeeshop.dao.OrderDAO;
import com.coffeeshop.coffeeshop.dao.OrderDetailDAO;
import com.coffeeshop.coffeeshop.dao.ProductDAO;
import com.coffeeshop.coffeeshop.model.Categories;
import com.coffeeshop.coffeeshop.model.Order;
import com.coffeeshop.coffeeshop.model.OrderDetail;
import com.coffeeshop.coffeeshop.model.Product;
import com.coffeeshop.coffeeshop.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

public class OrderController {

    @FXML private TextField txtOrderID;
    @FXML private ComboBox<String> cbUser;
    @FXML private ComboBox<String> cbCustomer;
    @FXML private ComboBox<String> cbCategory;
    @FXML private FlowPane productsFlowPane;
    @FXML private TableView<OrderDetail> tblOrderDetails;
    @FXML private TableColumn<OrderDetail, Integer> colProductID;
    @FXML private TableColumn<OrderDetail, Integer> colQuantity;
    @FXML private TableColumn<OrderDetail, Double> colPrice;
    @FXML private Label lblMessage;
    @FXML private Label lblTotal;

    private OrderDAO orderDAO;
    private OrderDetailDAO orderDetailDAO;
    private CategoriesDao categoriesDao;
    private ProductDAO productDAO;
    private Connection connection;
    private ObservableList<OrderDetail> orderItems = FXCollections.observableArrayList();
    private DecimalFormat currencyFormat = new DecimalFormat("#,###");

    @FXML
    public void initialize() {
        try {
            connection = DBConnection.getConnection();
            orderDAO = new OrderDAO();
            orderDetailDAO = new OrderDetailDAO();
            categoriesDao = new CategoriesDao();
            productDAO = new ProductDAO(connection);

            // Setup table columns
            colProductID.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("productID"));
            colQuantity.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantity"));
            colPrice.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("price"));

            // Set custom cell factory for better display
            colProductID.setCellFactory(col -> new TableCell<OrderDetail, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String productName = getProductNameById(item);
                        setText(productName != null ? productName : "ID: " + item);
                    }
                }
            });

            colPrice.setCellFactory(col -> new TableCell<OrderDetail, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(currencyFormat.format(item) + " VND");
                    }
                }
            });

            // Setup table data
            tblOrderDetails.setItems(orderItems);

            // Load initial data
            loadCategories();
            loadCustomers();
            loadProducts(""); // Load all products initially
            updateTotal();

            // Setup category change listener
            cbCategory.setOnAction(e -> {
                String selectedCategory = cbCategory.getValue();
                if (selectedCategory != null && !selectedCategory.equals("Tất cả")) {
                    loadProductsByCategory(selectedCategory);
                } else {
                    loadProducts(""); // Load all products
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            lblMessage.setText("Lỗi kết nối cơ sở dữ liệu.");
            lblMessage.setStyle("-fx-text-fill: red;");
        }
    }

    // Load categories from database using CategoriesDao
    private void loadCategories() {
        try {
            List<Categories> categoriesList = categoriesDao.getAllCategories();
            ObservableList<String> categories = FXCollections.observableArrayList();
            categories.add("Tất cả"); // Add "All" option
            for (Categories cat : categoriesList) {
                categories.add(cat.getNameCategories());
            }
            cbCategory.setItems(categories);
            cbCategory.setValue("Tất cả"); // Set default selection
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Lỗi tải danh mục sản phẩm.");
        }
    }

    // Load customers from database
    private void loadCustomers() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT Username FROM Users";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> customers = FXCollections.observableArrayList();
            while (rs.next()) {
                String username = rs.getString("Username");
                customers.add(username);
            }
            cbCustomer.setItems(customers);
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Lỗi tải danh sách khách hàng.");
        }
    }

    // Load products from database using ProductDAO
    private void loadProducts(String categoryFilter) {
        productsFlowPane.getChildren().clear();

        try {
            List<Product> productsList = productDAO.getAllProducts();
            for (Product p : productsList) {
                if (categoryFilter == null || categoryFilter.isEmpty() || categoryFilter.equals("Tất cả") ||
                        p.getCategories().getNameCategories().equals(categoryFilter)) {
                    VBox productCard = createProductCard(p);
                    productsFlowPane.getChildren().add(productCard);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblMessage.setText("Lỗi tải danh sách sản phẩm.");
        }
    }

    // Load products by specific category
    private void loadProductsByCategory(String categoryName) {
        productsFlowPane.getChildren().clear();

        try {
            List<Product> productsList = productDAO.getAllProducts();
            for (Product p : productsList) {
                if (p.getCategories().getNameCategories().equals(categoryName)) {
                    VBox productCard = createProductCard(p);
                    productsFlowPane.getChildren().add(productCard);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblMessage.setText("Lỗi tải danh sách sản phẩm theo danh mục.");
        }
    }

    // Create product card UI with description
    private VBox createProductCard(Product product) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(150);
        card.setPrefHeight(150); // Tăng chiều cao để chứa mô tả
        card.setStyle("-fx-background-color: white; -fx-border-color: #CD853F; -fx-border-width: 1; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");

        // Product name
        Label nameLabel = new Label(product.getNameProducts());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #8B4513;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        // Product description
        Label descriptionLabel = new Label(product.getDescriptionProducts());
        descriptionLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6B4E31; -fx-wrap-text: true;");
        descriptionLabel.setMaxWidth(130); // Giới hạn chiều rộng để text wrap
        descriptionLabel.setAlignment(Pos.CENTER);

        // Product price
        Label priceLabel = new Label(currencyFormat.format(product.getPrice()) + " VND");
        priceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #B8860B; -fx-font-weight: bold;");

        // Add button
        Button addButton = new Button("+ Thêm");
        addButton.setStyle("-fx-background-color: #8B4513; -fx-text-fill: white; -fx-font-size: 11px; " +
                "-fx-background-radius: 5; -fx-padding: 5 10;");

        // Add hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #8B4513; " +
                "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-padding: 10; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-border-color: #CD853F; " +
                "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-padding: 10; -fx-cursor: hand;"));

        // Add click handler
        addButton.setOnAction(e -> addProductToOrder(product));
        card.setOnMouseClicked(e -> addProductToOrder(product));

        // Add all elements to card
        card.getChildren().addAll(nameLabel, descriptionLabel, priceLabel, addButton);
        return card;
    }

    // Add product to order
    private void addProductToOrder(Product product) {
        boolean found = false;
        for (OrderDetail item : orderItems) {
            if (item.getProductID() == product.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                found = true;
                break;
            }
        }

        if (!found) {
            OrderDetail newItem = new OrderDetail(0, product.getId(), 1, product.getPrice());
            orderItems.add(newItem);
        }

        tblOrderDetails.refresh();
        updateTotal();
        lblMessage.setText("Đã thêm " + product.getNameProducts() + " vào đơn hàng");
        lblMessage.setStyle("-fx-text-fill: green;");
    }

    // Update total amount
    private void updateTotal() {
        double total = 0;
        for (OrderDetail item : orderItems) {
            total += item.getQuantity() * item.getPrice();
        }
        lblTotal.setText(currencyFormat.format(total) + " VND");
    }

    // Get product name by ID
    private String getProductNameById(int productID) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT ProductName FROM Products WHERE ProductID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("ProductName");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Handle save order
    @FXML
    private void handleSaveOrder(ActionEvent event) {
        String selectedCustomer = cbCustomer.getValue();

        if (selectedCustomer == null || orderItems.isEmpty()) {
            lblMessage.setText("Vui lòng chọn khách hàng và thêm sản phẩm vào đơn hàng.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT UserID FROM Users WHERE Username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, selectedCustomer);
            ResultSet rs = stmt.executeQuery();

            int userID = -1;
            if (rs.next()) {
                userID = rs.getInt("UserID");
            }

            if (userID == -1) {
                lblMessage.setText("Không tìm thấy khách hàng.");
                lblMessage.setStyle("-fx-text-fill: red;");
                return;
            }

            Order order = new Order();
            order.setUserID(userID);

            boolean success = orderDAO.addOrder(order);
            if (success) {
                String getOrderIdSql = "SELECT TOP 1 OrderID FROM Orders ORDER BY OrderID DESC";
                PreparedStatement getOrderIdStmt = conn.prepareStatement(getOrderIdSql);
                ResultSet orderIdRs = getOrderIdStmt.executeQuery();

                if (orderIdRs.next()) {
                    int orderID = orderIdRs.getInt("OrderID");

                    boolean allDetailsAdded = true;
                    for (OrderDetail item : orderItems) {
                        item.setOrderID(orderID);
                        if (!orderDetailDAO.addOrderDetail(item)) {
                            allDetailsAdded = false;
                            break;
                        }
                    }

                    if (allDetailsAdded) {
                        lblMessage.setText("Đơn hàng #" + orderID + " đã được tạo thành công!");
                        lblMessage.setStyle("-fx-text-fill: green;");

                        // Reset giao diện
                        orderItems.clear();
                        cbCustomer.getSelectionModel().clearSelection();
                        cbCategory.getSelectionModel().clearSelection();
                        productsFlowPane.getChildren().clear();
                        loadProducts("");
                        updateTotal();
                        tblOrderDetails.refresh();
                    } else {
                        lblMessage.setText("Có lỗi khi lưu chi tiết đơn hàng.");
                        lblMessage.setStyle("-fx-text-fill: red;");
                    }
                }
            } else {
                lblMessage.setText("Lỗi khi tạo đơn hàng.");
                lblMessage.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Lỗi kết nối cơ sở dữ liệu.");
            lblMessage.setStyle("-fx-text-fill: red;");
        }
    }

    // Handle add order detail (compatibility method)
    @FXML
    private void handleAddOrderDetail(ActionEvent event) {
        lblMessage.setText("Vui lòng chọn sản phẩm từ danh sách để thêm vào đơn hàng.");
        lblMessage.setStyle("-fx-text-fill: blue;");
    }

    // Handle back button with connection cleanup
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            Parent root = FXMLLoader.load(getClass().getResource("/views/Home/HomeUser.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Trang chính");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}