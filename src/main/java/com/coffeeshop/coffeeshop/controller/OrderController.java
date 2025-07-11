package com.coffeeshop.coffeeshop.controller;

import com.coffeeshop.coffeeshop.dao.OrderDao;
import com.coffeeshop.coffeeshop.dao.ProductDAO;
import com.coffeeshop.coffeeshop.model.Order;
import com.coffeeshop.coffeeshop.model.OrderDetail;
import com.coffeeshop.coffeeshop.model.Product;
import com.coffeeshop.coffeeshop.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class OrderController {

    private ProductDAO productDAO;
    private List<Product> allProducts;

    @FXML private ComboBox<String> cbCategory;
    @FXML private FlowPane productsFlowPane;

    @FXML private TableView<OrderDetail> tblOrderDetails;
    @FXML private TableColumn<OrderDetail, ImageView> colProductImage;
    @FXML private TableColumn<OrderDetail, String> colProductName;
    @FXML private TableColumn<OrderDetail, String> colSize;
    @FXML private TableColumn<OrderDetail, Integer> colQuantity;
    @FXML private TableColumn<OrderDetail, String> colDiscount;
    @FXML private TableColumn<OrderDetail, Double> colPrice;

    @FXML private ComboBox<String> cbCustomer;
    @FXML private Label lblTotal;
    @FXML private Label lblMessage;
    @FXML private Label lblItemCount;

    private ObservableList<OrderDetail> orderDetailList = FXCollections.observableArrayList();
    private OrderDao orderDao;
    private Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        this.orderDao = new OrderDao(connection);
        this.productDAO = new ProductDAO(connection);
        loadAndRenderProducts();
    }

    @FXML
    public void initialize() {
        setupTable();

        cbCustomer.setItems(FXCollections.observableArrayList("Khách lẻ", "Thẻ thành viên", "Khách VIP"));
        cbCustomer.getSelectionModel().selectFirst();

        if (connection == null) {
            try {
                connection = DBConnection.getConnection();
                this.orderDao = new OrderDao(connection);
                this.productDAO = new ProductDAO(connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (connection != null && productDAO != null) {
            loadAndRenderProducts();
        }
    }

    private void loadAndRenderProducts() {
        try {
            allProducts = productDAO.getAllProducts();
            if (allProducts == null) allProducts = new ArrayList<>();

            List<String> categories = allProducts.stream()
                    .map(p -> p.getCategories() != null ? p.getCategories().getNameCategories() : "Không rõ danh mục")
                    .distinct()
                    .collect(Collectors.toList());
            categories.add(0, "Tất cả");

            cbCategory.setItems(FXCollections.observableArrayList(categories));
            cbCategory.getSelectionModel().selectFirst();

            cbCategory.setOnAction(e -> renderProductsByCategory(cbCategory.getValue()));
            renderProductsByCategory("Tất cả");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderProductsByCategory(String categoryName) {
        List<Product> filtered;
        if ("Tất cả".equals(categoryName)) {
            filtered = new ArrayList<>(allProducts);
        } else {
            filtered = allProducts.stream()
                    .filter(p -> p.getCategories() != null && p.getCategories().getNameCategories().equals(categoryName))
                    .collect(Collectors.toList());
        }

        productsFlowPane.getChildren().clear();

        for (Product p : filtered) {
            VBox card = new VBox(5);
            card.setPadding(new Insets(10));
            card.setStyle("-fx-background-color: #FFF8DC; -fx-border-color: #CD853F; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
            card.setPrefWidth(150);

            ImageView imageView;
            if (p.getImage() != null && !p.getImage().isEmpty()) {
                File file = new File(p.getImage());
                if (file.exists()) {
                    imageView = new ImageView(new Image(file.toURI().toString(), 80, 80, true, true));
                } else {
                    imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/no-image.png"), 80, 80, true, true));
                }
            } else {
                imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/no-image.png"), 80, 80, true, true));
            }

            Label lblName = new Label(p.getNameProducts());
            lblName.setStyle("-fx-font-weight: bold;");

            // Tính giá sau discount
            double finalPrice = p.getPrice();
            if (p.getDiscount() > 0) {
                finalPrice = p.getPrice() * (1 - p.getDiscount() / 100.0);
            }

            Label lblPrice;
            if (p.getDiscount() > 0) {
                lblPrice = new Label(
                        String.format("Giá: %.0f VND\n(-%.0f%%)", finalPrice, p.getDiscount())
                );
                lblPrice.setStyle("-fx-text-fill: red;");
            } else {
                lblPrice = new Label(String.format("Giá: %.0f VND", p.getPrice()));
                lblPrice.setStyle("-fx-text-fill: #8B4513;");
            }

            ComboBox<String> cbSize = new ComboBox<>();
            cbSize.setItems(FXCollections.observableArrayList("S", "M", "L"));
            cbSize.setValue("M");
            cbSize.setPrefWidth(120);

            double finalPriceForLambda = finalPrice;
            float discountForLambda = p.getDiscount();

            Button btnAdd = new Button("➕ Thêm");
            btnAdd.setPrefWidth(120);
            btnAdd.setStyle("-fx-background-color: #DEB887; -fx-text-fill: white; -fx-font-size: 12px;");
            btnAdd.setOnAction(e -> {
                String selectedSize = cbSize.getValue();
                addProductToOrder(
                        p.getId(),
                        p.getNameProducts(),
                        1,
                        selectedSize,
                        finalPriceForLambda,
                        p.getImage(),
                        discountForLambda
                );
            });

            card.getChildren().addAll(imageView, lblName, lblPrice, cbSize, btnAdd);
            productsFlowPane.getChildren().add(card);
        }
    }

    private void setupTable() {
        colProductImage.setCellValueFactory(new PropertyValueFactory<>("productImageView"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discountFormatted"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        tblOrderDetails.setItems(orderDetailList);
        setupTableContextMenu();
        setupTableDoubleClick();
    }

    public void addProductToOrder(int productId, String productName, int quantity, String size, double price, String imagePath, float discount) {
        OrderDetail existingDetail = orderDetailList.stream()
                .filter(d -> d.getProductId() == productId && Objects.equals(d.getSize(), size))
                .findFirst()
                .orElse(null);

        if (existingDetail != null) {
            existingDetail.setQuantity(existingDetail.getQuantity() + quantity);
            lblMessage.setText("Đã cập nhật số lượng sản phẩm");
        } else {
            OrderDetail d = new OrderDetail();
            d.setProductId(productId);
            d.setProductName(productName);
            d.setQuantity(quantity);
            d.setSize(size);
            d.setUnitPrice(price);
            d.setDiscount(discount);
            d.setProductImage(imagePath);

            ImageView iv;
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (file.exists()) {
                    iv = new ImageView(new Image(file.toURI().toString(), 50, 50, true, true));
                } else {
                    iv = new ImageView(new Image(getClass().getResourceAsStream("/images/no-image.png"), 50, 50, true, true));
                }
            } else {
                iv = new ImageView(new Image(getClass().getResourceAsStream("/images/no-image.png"), 50, 50, true, true));
            }
            d.setProductImageView(iv);

            orderDetailList.add(d);
            lblMessage.setText("Đã thêm sản phẩm vào đơn hàng");
        }

        updateTotal();
        tblOrderDetails.refresh();
    }

    private void updateTotal() {
        double total = orderDetailList.stream()
                .mapToDouble(d -> d.getTotalPrice())
                .sum();
        int itemCount = orderDetailList.stream()
                .mapToInt(OrderDetail::getQuantity)
                .sum();

        lblTotal.setText(String.format("%,.0f VND", total));
        lblItemCount.setText(String.valueOf(itemCount));
    }

    @FXML
    private void handleClearOrder() {
        if (!orderDetailList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận");
            alert.setHeaderText("Xóa toàn bộ đơn hàng?");
            alert.setContentText("Bạn có chắc chắn muốn xóa tất cả món trong đơn hàng không?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                orderDetailList.clear();
                updateTotal();
                lblMessage.setText("Đã xóa tất cả món khỏi đơn hàng");
            }
        } else {
            lblMessage.setText("Đơn hàng đã trống");
        }
    }

    @FXML
    private void handleSaveOrder() {
        if (!validateOrder()) return;

        Order order = new Order();
        order.setStaffId(1);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("Chờ xử lý");
        order.setNote(cbCustomer.getValue());

        boolean success = orderDao.addOrder(order, new ArrayList<>(orderDetailList));
        if (success) {
            lblMessage.setText("✅ Đơn hàng đã được tạo! Mã: " + order.getOrderId());
            orderDetailList.clear();
            updateTotal();
        } else {
            lblMessage.setText("❌ Lỗi khi tạo đơn hàng.");
        }
    }

    private boolean validateOrder() {
        if (orderDetailList.isEmpty()) {
            lblMessage.setText("❌ Chưa có món nào trong đơn hàng");
            return false;
        }
        if (cbCustomer.getValue() == null || cbCustomer.getValue().trim().isEmpty()) {
            lblMessage.setText("❌ Vui lòng chọn loại khách hàng");
            return false;
        }
        return true;
    }

    @FXML
    private void handleEditSelectedItem() {
        OrderDetail selected = tblOrderDetails.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showEditDialog(selected);
        } else {
            lblMessage.setText("❌ Vui lòng chọn món cần chỉnh sửa");
        }
    }

    @FXML
    private void handleDeleteSelectedItem() {
        OrderDetail selected = tblOrderDetails.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận xóa");
            alert.setHeaderText("Xóa món khỏi đơn hàng?");
            alert.setContentText("Bạn có chắc chắn muốn xóa \"" + selected.getProductName() + "\" khỏi đơn hàng không?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                orderDetailList.remove(selected);
                updateTotal();
                lblMessage.setText("✅ Đã xóa món: " + selected.getProductName());
            }
        } else {
            lblMessage.setText("❌ Vui lòng chọn món cần xóa");
        }
    }

    private void showEditDialog(OrderDetail orderDetail) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Chỉnh sửa món: " + orderDetail.getProductName());

        VBox dialogVbox = new VBox(15);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setStyle("-fx-background-color: #F5F5DC;");

        Label headerLabel = new Label("✏️ Chỉnh sửa món ăn");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8B4513;");

        Label productLabel = new Label("Món: " + orderDetail.getProductName());
        ComboBox<String> cbEditSize = new ComboBox<>();
        cbEditSize.setItems(FXCollections.observableArrayList("S", "M", "L"));
        cbEditSize.setValue(orderDetail.getSize());

        Spinner<Integer> spinnerQuantity = new Spinner<>(1, 99, orderDetail.getQuantity());
        spinnerQuantity.setEditable(true);

        Label totalLabel = new Label();
        Runnable updateTotal = () -> {
            double total = spinnerQuantity.getValue() * orderDetail.getUnitPrice();
            totalLabel.setText(String.format("Thành tiền: %.0f VND", total));
        };
        updateTotal.run();
        spinnerQuantity.valueProperty().addListener((obs, oldValue, newValue) -> updateTotal.run());

        Button btnSave = new Button("💾 Lưu");
        btnSave.setOnAction(e -> {
            orderDetail.setSize(cbEditSize.getValue());
            orderDetail.setQuantity(spinnerQuantity.getValue());
            tblOrderDetails.refresh();
            updateTotal();
            lblMessage.setText("✅ Đã cập nhật món: " + orderDetail.getProductName());
            dialog.close();
        });

        Button btnCancel = new Button("❌ Hủy");
        btnCancel.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(10, btnSave, btnCancel);
        buttonBox.setAlignment(Pos.CENTER);

        dialogVbox.getChildren().addAll(headerLabel, productLabel, cbEditSize, spinnerQuantity, totalLabel, buttonBox);
        dialog.setScene(new Scene(dialogVbox, 300, 250));
        dialog.showAndWait();
    }

    private void setupTableContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("✏️ Chỉnh sửa");
        editItem.setOnAction(e -> handleEditSelectedItem());

        MenuItem deleteItem = new MenuItem("🗑️ Xóa món");
        deleteItem.setOnAction(e -> handleDeleteSelectedItem());

        contextMenu.getItems().addAll(editItem, deleteItem);
        tblOrderDetails.setContextMenu(contextMenu);
    }

    private void setupTableDoubleClick() {
        tblOrderDetails.setRowFactory(tv -> {
            TableRow<OrderDetail> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showEditDialog(row.getItem());
                }
            });
            return row;
        });
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
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
