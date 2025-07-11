package com.coffeeshop.coffeeshop.dao;

import com.coffeeshop.coffeeshop.model.Order;
import com.coffeeshop.coffeeshop.model.OrderDetail;

import java.sql.*;
import java.util.List;

public class OrderDao {

    private final Connection connection;

    public OrderDao(Connection connection) {
        this.connection = connection;
    }

    public boolean addOrder(Order order, List<OrderDetail> orderDetails) {
        String sqlOrder = "INSERT INTO Orders (StaffID, OrderDate, TotalAmount, Status, Note) VALUES (?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO OrderDetails (OrderID, ProductID, Size, Quantity, UnitPrice) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement orderStmt = null;
        PreparedStatement detailStmt = null;

        try {
            connection.setAutoCommit(false);

            // Tính tổng tiền từ chi tiết
            double total = orderDetails.stream()
                    .mapToDouble(d -> d.getQuantity() * d.getUnitPrice())
                    .sum();
            order.setTotalAmount(total);

            // Insert Orders
            orderStmt = connection.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, order.getStaffId());
            orderStmt.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            orderStmt.setDouble(3, order.getTotalAmount());
            orderStmt.setString(4, order.getStatus());
            orderStmt.setString(5, order.getNote());
            orderStmt.executeUpdate();

            ResultSet generatedKeys = orderStmt.getGeneratedKeys();
            int orderId;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
                order.setOrderId(orderId);
            } else {
                connection.rollback();
                System.err.println("Không thể lấy OrderID sau khi insert.");
                return false;
            }

            // Insert OrderDetails
            detailStmt = connection.prepareStatement(sqlDetail);
            for (OrderDetail d : orderDetails) {
                detailStmt.setInt(1, orderId);
                detailStmt.setInt(2, d.getProductId());
                detailStmt.setString(3, d.getSize());
                detailStmt.setInt(4, d.getQuantity());
                detailStmt.setDouble(5, d.getUnitPrice()); // giá đã giảm, truyền từ Controller
                detailStmt.addBatch();
            }
            detailStmt.executeBatch();

            connection.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;

        } finally {
            try {
                if (orderStmt != null) orderStmt.close();
                if (detailStmt != null) detailStmt.close();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new java.util.ArrayList<>();
        String sql = "SELECT * FROM Orders ORDER BY OrderDate DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("OrderID"));
                order.setStaffId(rs.getInt("StaffID"));
                order.setOrderDate(rs.getTimestamp("OrderDate").toLocalDateTime());
                order.setTotalAmount(rs.getDouble("TotalAmount"));
                order.setStatus(rs.getString("Status"));
                order.setNote(rs.getString("Note"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    public boolean updateOrder(Order order, List<OrderDetail> orderDetails) {
        String sqlOrder = "UPDATE Orders SET StaffID = ?, OrderDate = ?, TotalAmount = ?, Status = ?, Note = ? WHERE OrderID = ?";
        String sqlDeleteDetails = "DELETE FROM OrderDetails WHERE OrderID = ?";
        String sqlInsertDetail = "INSERT INTO OrderDetails (OrderID, ProductID, Size, Quantity, UnitPrice) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement orderStmt = null;
        PreparedStatement deleteDetailStmt = null;
        PreparedStatement insertDetailStmt = null;

        try {
            connection.setAutoCommit(false);

            // Update Orders
            orderStmt = connection.prepareStatement(sqlOrder);
            orderStmt.setInt(1, order.getStaffId());
            orderStmt.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            orderStmt.setDouble(3, order.getTotalAmount());
            orderStmt.setString(4, order.getStatus());
            orderStmt.setString(5, order.getNote());
            orderStmt.setInt(6, order.getOrderId());
            orderStmt.executeUpdate();

            // Delete old details
            deleteDetailStmt = connection.prepareStatement(sqlDeleteDetails);
            deleteDetailStmt.setInt(1, order.getOrderId());
            deleteDetailStmt.executeUpdate();

            // Insert new details
            insertDetailStmt = connection.prepareStatement(sqlInsertDetail);
            for (OrderDetail detail : orderDetails) {
                insertDetailStmt.setInt(1, order.getOrderId());
                insertDetailStmt.setInt(2, detail.getProductId());
                insertDetailStmt.setString(3, detail.getSize());
                insertDetailStmt.setInt(4, detail.getQuantity());
                insertDetailStmt.setDouble(5, detail.getUnitPrice());
                insertDetailStmt.addBatch();
            }
            insertDetailStmt.executeBatch();

            connection.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;

        } finally {
            try {
                if (orderStmt != null) orderStmt.close();
                if (deleteDetailStmt != null) deleteDetailStmt.close();
                if (insertDetailStmt != null) insertDetailStmt.close();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM Orders WHERE OrderID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("OrderID"));
                    order.setStaffId(rs.getInt("StaffID"));
                    order.setOrderDate(rs.getTimestamp("OrderDate").toLocalDateTime());
                    order.setTotalAmount(rs.getDouble("TotalAmount"));
                    order.setStatus(rs.getString("Status"));
                    order.setNote(rs.getString("Note"));
                    return order;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE Orders SET Status = ? WHERE OrderID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, orderId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy chi tiết đơn hàng (JOIN bảng products để lấy tên, ảnh và discount sản phẩm)
     */
    public List<OrderDetail> getOrderDetails(int orderId) {
        List<OrderDetail> details = new java.util.ArrayList<>();
        String sql = """
            SELECT d.*, p.nameProducts, p.image, p.discount
            FROM OrderDetails d
            JOIN products p ON d.ProductID = p.id
            WHERE d.OrderID = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderDetail detail = new OrderDetail();
                    detail.setOrderDetailId(rs.getInt("OrderDetailID"));
                    detail.setOrderId(rs.getInt("OrderID"));
                    detail.setProductId(rs.getInt("ProductID"));
                    detail.setSize(rs.getString("Size"));
                    detail.setQuantity(rs.getInt("Quantity"));

                    double unitPrice = rs.getDouble("UnitPrice");
                    double discount = rs.getDouble("discount");
                    double finalPrice = unitPrice;

                    // Nếu discount > 0, kiểm tra xem giá unit price lưu trong DB
                    // đã là giá discount chưa. (Ở code của bạn, giá lưu vào DB đã là giá đã giảm)

                    detail.setUnitPrice(finalPrice);
                    detail.setProductName(rs.getString("nameProducts"));
                    detail.setProductImage(rs.getString("image"));

                    details.add(detail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return details;
    }
}
