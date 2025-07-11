package com.coffeeshop.coffeeshop.controller;

import com.coffeeshop.coffeeshop.dao.OrderDao;
import com.coffeeshop.coffeeshop.dao.ProductDAO;
import com.coffeeshop.coffeeshop.model.Order;
import com.coffeeshop.coffeeshop.model.OrderDetail;
import com.coffeeshop.coffeeshop.model.Product;
import com.coffeeshop.coffeeshop.util.DBConnection;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderUpdateController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatusComboBox;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, Integer> staffIdColumn;
    @FXML private TableColumn<Order, String> orderDateColumn;
    @FXML private TableColumn<Order, String> totalAmountColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> noteColumn;
    @FXML private TableColumn<Order, Void> actionColumn;

    @FXML private TitledPane detailPane;
    @FXML private TextField editOrderIdField;
    @FXML private TextField editStaffIdField;
    @FXML private DatePicker editOrderDatePicker;
    @FXML private ComboBox<String> editStatusComboBox;
    @FXML private TextField editNoteField;

    @FXML private TableView<OrderDetail> orderDetailsTable;
    @FXML private TableColumn<OrderDetail, Integer> productIdColumn;
    @FXML private TableColumn<OrderDetail, String> productNameColumn;
    @FXML private TableColumn<OrderDetail, String> sizeColumn;
    @FXML private TableColumn<OrderDetail, Integer> quantityColumn;
    @FXML private TableColumn<OrderDetail, String> unitPriceColumn;
    @FXML private TableColumn<OrderDetail, String> subtotalColumn;
    @FXML private Label totalAmountLabel;

    private OrderDao orderDao;
    private ProductDAO productDAO;
    private ObservableList<Order> ordersList = FXCollections.observableArrayList();
    private ObservableList<Order> filteredOrdersList = FXCollections.observableArrayList();
    private ObservableList<OrderDetail> detailsList = FXCollections.observableArrayList();
    private Order currentEditingOrder;
    private DecimalFormat currencyFormat = new DecimalFormat("#,###.## VND");
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private Connection connection;

    public void setConnection(Connection connection) {
        System.out.println("=== DEBUG: setConnection() được gọi ===");
        this.connection = connection;
        System.out.println("DEBUG: Connection đã được set: " + (connection != null ? "Thành công" : "Null"));

        this.orderDao = new OrderDao(connection);
        System.out.println("DEBUG: OrderDao đã được khởi tạo");

        loadOrders();
    }

    @FXML
    public void initialize() {
        System.out.println("=== DEBUG: initialize() bắt đầu ===");

        // Khởi tạo ComboBox
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "Tất cả", "Pending", "Completed", "Cancelled"
        );
        filterStatusComboBox.setItems(statusOptions);
        filterStatusComboBox.setValue("Tất cả");

        editStatusComboBox.setItems(FXCollections.observableArrayList(
                "Pending", "Completed", "Cancelled"
        ));

        // Cấu hình bảng đơn hàng
        setupOrdersTable();

        // Cấu hình bảng chi tiết đơn hàng
        setupOrderDetailsTable();

        // THÊM DEBUG: Kiểm tra connection
        System.out.println("DEBUG: Connection trong initialize(): " + (connection != null ? "Có" : "NULL"));
        System.out.println("DEBUG: OrderDao trong initialize(): " + (orderDao != null ? "Có" : "NULL"));

        // Nếu connection chưa có thì thử tạo connection tự động
        if (connection == null) {
            System.out.println("DEBUG: Connection chưa có, thử tạo connection tự động");
            try {
                System.out.println("DEBUG: Đang gọi DBConnection.getConnection()...");
                Connection conn = DBConnection.getConnection();
                System.out.println("DEBUG: DBConnection.getConnection() returned: " + conn);

                if (conn != null) {
                    System.out.println("DEBUG: Connection thành công, khởi tạo OrderDao...");
                    this.connection = conn;
                    this.orderDao = new OrderDao(conn);
                    this.productDAO = new ProductDAO(conn);
                    System.out.println("DEBUG: Đã tạo connection, OrderDao và ProductDAO tự động thành công");
                    loadOrders();
                } else {
                    System.out.println("DEBUG: Connection trả về null");
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Exception khi tạo connection: " + e.getClass().getName());
                System.out.println("DEBUG: Exception message: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("DEBUG: initialize() hoàn thành");
    }

    private void setupOrdersTable() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        staffIdColumn.setCellValueFactory(new PropertyValueFactory<>("staffId"));

        orderDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getOrderDate().format(dateTimeFormatter))
        );

        totalAmountColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(currencyFormat.format(cellData.getValue().getTotalAmount()))
        );

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));

        // Thêm nút hành động
        setupActionColumn();

        // Xử lý double click để xem chi tiết
        ordersTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    loadOrderDetails(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupActionColumn() {
        Callback<TableColumn<Order, Void>, TableCell<Order, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Order, Void> call(final TableColumn<Order, Void> param) {
                final TableCell<Order, Void> cell = new TableCell<>() {
                    private final Button editBtn = new Button("Sửa");
                    private final Button deleteBtn = new Button("Xóa");

                    {
                        editBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-cursor: hand;");
                        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");

                        editBtn.setOnAction(event -> {
                            Order order = getTableView().getItems().get(getIndex());
                            loadOrderDetails(order);
                        });

                        deleteBtn.setOnAction(event -> {
                            Order order = getTableView().getItems().get(getIndex());
                            handleDeleteOrder(order);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox hbox = new HBox(10);
                            hbox.getChildren().addAll(editBtn, deleteBtn);
                            setGraphic(hbox);
                        }
                    }
                };
                return cell;
            }
        };

        actionColumn.setCellFactory(cellFactory);
    }

    private void setupOrderDetailsTable() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        unitPriceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(currencyFormat.format(cellData.getValue().getUnitPrice()))
        );

        subtotalColumn.setCellValueFactory(cellData -> {
            OrderDetail detail = cellData.getValue();
            double subtotal = detail.getQuantity() * detail.getUnitPrice();
            return new SimpleStringProperty(currencyFormat.format(subtotal));
        });
    }

    private void loadOrders() {
        System.out.println("=== DEBUG: loadOrders() bắt đầu ===");

        if (orderDao == null) {
            System.out.println("DEBUG: orderDao is null, không thể load orders");
            return;
        }

        try {
            List<Order> orders = orderDao.getAllOrders();
            System.out.println("DEBUG: Số lượng orders lấy được: " + (orders != null ? orders.size() : 0));

            if (orders != null) {
                ordersList.setAll(orders);
                filteredOrdersList.setAll(orders);
                ordersTable.setItems(filteredOrdersList);
                System.out.println("DEBUG: Đã load " + orders.size() + " orders vào table");
            } else {
                System.out.println("DEBUG: getAllOrders() trả về null");
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Lỗi trong loadOrders(): " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("DEBUG: loadOrders() hoàn thành");
    }

    @FXML
    private void handleSearch() {
        System.out.println("=== DEBUG: handleSearch() được gọi ===");

        if (ordersList == null || ordersList.isEmpty()) {
            System.out.println("DEBUG: ordersList is null or empty");
            showAlert("Không có dữ liệu để tìm kiếm", Alert.AlertType.WARNING);
            return;
        }

        String searchText = searchField.getText().toLowerCase();
        String selectedStatus = filterStatusComboBox.getValue();

        System.out.println("DEBUG: Search text: " + searchText + ", Status: " + selectedStatus);

        List<Order> filtered = ordersList.stream()
                .filter(order -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                            String.valueOf(order.getOrderId()).contains(searchText) ||
                            String.valueOf(order.getStaffId()).contains(searchText) ||
                            (order.getNote() != null && order.getNote().toLowerCase().contains(searchText));

                    boolean matchesStatus = "Tất cả".equals(selectedStatus) ||
                            order.getStatus().equals(selectedStatus);

                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toList());

        System.out.println("DEBUG: Số orders sau khi filter: " + filtered.size());
        filteredOrdersList.setAll(filtered);
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        filterStatusComboBox.setValue("Tất cả");
        loadOrders();
        detailPane.setExpanded(false);
    }

    private void loadOrderDetails(Order order) {
        currentEditingOrder = order;

        // Load thông tin order
        editOrderIdField.setText(String.valueOf(order.getOrderId()));
        editStaffIdField.setText(String.valueOf(order.getStaffId()));
        editOrderDatePicker.setValue(order.getOrderDate().toLocalDate());
        editStatusComboBox.setValue(order.getStatus());
        editNoteField.setText(order.getNote());

        // Load chi tiết order
        List<OrderDetail> details = orderDao.getOrderDetails(order.getOrderId());

        // Set tên sản phẩm cho mỗi OrderDetail
        if (productDAO != null) {
            for (OrderDetail detail : details) {
                try {
                    // Lấy thông tin sản phẩm từ database
                    String productName = getProductName(detail.getProductId());
                    if (productName != null) {
                        detail.setProductName(productName);
                    } else {
                        detail.setProductName("Sản phẩm #" + detail.getProductId());
                    }
                } catch (Exception e) {
                    System.out.println("Lỗi khi lấy tên sản phẩm: " + e.getMessage());
                    detail.setProductName("Sản phẩm #" + detail.getProductId());
                }
            }
        }

        detailsList.clear();
        detailsList.addAll(details);
        orderDetailsTable.setItems(detailsList);

        // Tính tổng tiền
        updateTotalAmount();

        // Mở panel chi tiết
        detailPane.setExpanded(true);
    }

    private void updateTotalAmount() {
        double total = detailsList.stream()
                .mapToDouble(d -> d.getQuantity() * d.getUnitPrice())
                .sum();
        totalAmountLabel.setText(currencyFormat.format(total));
    }

    @FXML
    private void handleUpdateOrder() {
        if (currentEditingOrder == null) {
            showAlert("Vui lòng chọn đơn hàng cần cập nhật", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Lấy thông tin mới
            int staffId = Integer.parseInt(editStaffIdField.getText());
            LocalDateTime orderDate = LocalDateTime.of(editOrderDatePicker.getValue(), LocalTime.now());
            String status = editStatusComboBox.getValue();
            String note = editNoteField.getText();

            // Tạo order mới với thông tin cập nhật
            Order updatedOrder = new Order(
                    currentEditingOrder.getOrderId(),
                    staffId,
                    orderDate,
                    0, // Sẽ tính lại
                    status,
                    note
            );

            // Tính tổng tiền
            double total = detailsList.stream()
                    .mapToDouble(d -> d.getQuantity() * d.getUnitPrice())
                    .sum();
            updatedOrder.setTotalAmount(total);

            // Cập nhật vào database
            boolean success = orderDao.updateOrder(updatedOrder, detailsList);

            if (success) {
                showAlert("Cập nhật đơn hàng thành công!", Alert.AlertType.INFORMATION);
                loadOrders(); // Reload danh sách
                detailPane.setExpanded(false);
            } else {
                showAlert("Cập nhật thất bại. Vui lòng thử lại.", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            showAlert("Vui lòng nhập đúng định dạng số cho mã nhân viên", Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelEdit() {
        detailPane.setExpanded(false);
        currentEditingOrder = null;
        clearEditForm();
    }

    private void handleDeleteOrder(Order order) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa đơn hàng #" + order.getOrderId() + "?");
        alert.setContentText("Hành động này không thể hoàn tác.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Ở đây bạn có thể thêm phương thức deleteOrder trong OrderDao
            // Tạm thời có thể cập nhật status thành "Cancelled"
            boolean success = orderDao.updateOrderStatus(order.getOrderId(), "Cancelled");
            if (success) {
                showAlert("Đã hủy đơn hàng thành công!", Alert.AlertType.INFORMATION);
                loadOrders();
            } else {
                showAlert("Không thể hủy đơn hàng.", Alert.AlertType.ERROR);
            }
        }
    }

    private void clearEditForm() {
        editOrderIdField.clear();
        editStaffIdField.clear();
        editOrderDatePicker.setValue(null);
        editStatusComboBox.setValue(null);
        editNoteField.clear();
        detailsList.clear();
        totalAmountLabel.setText("0 VND");
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAddProduct() {
        if (currentEditingOrder == null) {
            showAlert("Vui lòng chọn đơn hàng trước khi thêm sản phẩm", Alert.AlertType.WARNING);
            return;
        }
        showAddProductDialog();
    }

    @FXML
    private void handleEditProduct() {
        OrderDetail selected = orderDetailsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn sản phẩm cần sửa", Alert.AlertType.WARNING);
            return;
        }
        showEditProductDialog(selected);
    }

    @FXML
    private void handleDeleteProduct() {
        OrderDetail selected = orderDetailsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn sản phẩm cần xóa", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa sản phẩm khỏi đơn hàng?");
        confirm.setContentText("Bạn có chắc chắn muốn xóa sản phẩm này?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            detailsList.remove(selected);
            updateTotalAmount();
        }
    }

    private void showAddProductDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Thêm sản phẩm vào đơn hàng");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        // ComboBox chọn sản phẩm
        Label lblProduct = new Label("Chọn sản phẩm:");
        ComboBox<Product> cbProducts = new ComboBox<>();

        try {
            List<Product> products = getSimpleProductList();
            cbProducts.setItems(FXCollections.observableArrayList(products));
            cbProducts.setConverter(new javafx.util.StringConverter<Product>() {
                @Override
                public String toString(Product product) {
                    return product != null ? product.getNameProducts() + " - " + currencyFormat.format(product.getPrice()) : "";
                }
                @Override
                public Product fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi khi tải danh sách sản phẩm", Alert.AlertType.ERROR);
        }

        // ComboBox chọn size
        Label lblSize = new Label("Chọn size:");
        ComboBox<String> cbSize = new ComboBox<>();
        cbSize.setItems(FXCollections.observableArrayList("S", "M", "L"));
        cbSize.setValue("M");

        // Spinner số lượng
        Label lblQuantity = new Label("Số lượng:");
        Spinner<Integer> spinQuantity = new Spinner<>(1, 99, 1);
        spinQuantity.setEditable(true);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnAdd = new Button("Thêm");
        btnAdd.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnAdd.setOnAction(e -> {
            Product selectedProduct = cbProducts.getValue();
            if (selectedProduct == null) {
                showAlert("Vui lòng chọn sản phẩm", Alert.AlertType.WARNING);
                return;
            }

            OrderDetail newDetail = new OrderDetail();
            newDetail.setProductId(selectedProduct.getId());
            newDetail.setProductName(selectedProduct.getNameProducts());
            newDetail.setSize(cbSize.getValue());
            newDetail.setQuantity(spinQuantity.getValue());
            newDetail.setUnitPrice(selectedProduct.getPrice());

            // Kiểm tra xem sản phẩm đã tồn tại chưa
            OrderDetail existing = detailsList.stream()
                    .filter(d -> d.getProductId() == newDetail.getProductId() &&
                            d.getSize().equals(newDetail.getSize()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + newDetail.getQuantity());
                orderDetailsTable.refresh();
            } else {
                detailsList.add(newDetail);
            }

            updateTotalAmount();
            dialog.close();
        });

        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        btnCancel.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(btnAdd, btnCancel);

        vbox.getChildren().addAll(
                lblProduct, cbProducts,
                lblSize, cbSize,
                lblQuantity, spinQuantity,
                buttonBox
        );

        Scene scene = new Scene(vbox, 400, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showEditProductDialog(OrderDetail detail) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Sửa sản phẩm");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        // Hiển thị tên sản phẩm
        Label lblProduct = new Label("Sản phẩm: " + detail.getProductName());
        lblProduct.setStyle("-fx-font-weight: bold;");

        // ComboBox size
        Label lblSize = new Label("Size:");
        ComboBox<String> cbSize = new ComboBox<>();
        cbSize.setItems(FXCollections.observableArrayList("S", "M", "L"));
        cbSize.setValue(detail.getSize());

        // Spinner số lượng
        Label lblQuantity = new Label("Số lượng:");
        Spinner<Integer> spinQuantity = new Spinner<>(1, 99, detail.getQuantity());
        spinQuantity.setEditable(true);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnSave = new Button("Lưu");
        btnSave.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnSave.setOnAction(e -> {
            detail.setSize(cbSize.getValue());
            detail.setQuantity(spinQuantity.getValue());
            orderDetailsTable.refresh();
            updateTotalAmount();
            dialog.close();
        });

        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        btnCancel.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(btnSave, btnCancel);

        vbox.getChildren().addAll(
                lblProduct,
                lblSize, cbSize,
                lblQuantity, spinQuantity,
                buttonBox
        );

        Scene scene = new Scene(vbox, 350, 250);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // Phương thức helper để lấy tên sản phẩm
    private String getProductName(int productId) {
        String sql = "SELECT NameProducts FROM Products WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("NameProducts");
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy tên sản phẩm: " + e.getMessage());
        }
        return null;
    }

    // Phương thức helper để lấy danh sách sản phẩm đơn giản
    private List<Product> getSimpleProductList() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT ID, NameProducts, Price FROM Products";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("ID"));
                p.setNameProducts(rs.getString("NameProducts"));
                p.setPrice(rs.getDouble("Price"));
                products.add(p);
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }
}