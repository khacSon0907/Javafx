package com.coffeeshop.coffeeshop.dao;

import com.coffeeshop.coffeeshop.model.Order;
import com.coffeeshop.coffeeshop.model.OrderDetail;
import com.coffeeshop.coffeeshop.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // Dùng chung DBConnection (SQL Server)
    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    // Lấy tất cả đơn hàng
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM Orders"; // Bảng Orders

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int orderID = resultSet.getInt("OrderID");
                int userID = resultSet.getInt("UserID");
                Order order = new Order(orderID, userID, null); // Người dùng có thể truy vấn riêng nếu cần
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    // Thêm đơn hàng
    public boolean addOrder(Order order) {
        String query = "INSERT INTO Orders (UserID) VALUES (?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, order.getUserID());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật thông tin đơn hàng
    public boolean updateOrder(Order order) {
        String query = "UPDATE Orders SET UserID = ? WHERE OrderID = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, order.getUserID());
            preparedStatement.setInt(2, order.getOrderID());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa đơn hàng
    public boolean deleteOrder(int orderID) {
        String query = "DELETE FROM Orders WHERE OrderID = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, orderID);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy chi tiết đơn hàng (OrderDetails)
    public List<OrderDetail> getOrderDetails(int orderID) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        String query = "SELECT * FROM OrderDetails WHERE OrderID = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, orderID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int productID = resultSet.getInt("ProductID");
                    int quantity = resultSet.getInt("Quantity");
                    double price = resultSet.getDouble("Price");
                    orderDetails.add(new OrderDetail(orderID, productID, quantity, price));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderDetails;
    }

    // Thêm chi tiết đơn hàng
    public boolean addOrderDetail(OrderDetail orderDetail) {
        String query = "INSERT INTO OrderDetails (OrderID, ProductID, Quantity, Price) VALUES (?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, orderDetail.getOrderID());
            preparedStatement.setInt(2, orderDetail.getProductID());
            preparedStatement.setInt(3, orderDetail.getQuantity());
            preparedStatement.setDouble(4, orderDetail.getPrice());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
