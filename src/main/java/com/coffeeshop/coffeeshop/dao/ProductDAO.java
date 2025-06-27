package com.coffeeshop.coffeeshop.dao;

import com.coffeeshop.coffeeshop.model.Categories;
import com.coffeeshop.coffeeshop.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private Connection conn;

    public ProductDAO(Connection conn) {
        this.conn = conn;
    }

    // Lấy tất cả sản phẩm
    public List<Product> getAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.CategoryID as c_id, c.CategoryName FROM Products p JOIN Categories c ON p.CategoryID = c.CategoryID";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categories c = new Categories(rs.getInt("c_id"), rs.getString("CategoryName"));
                Product p = new Product(
                        rs.getInt("ProductID"),
                        rs.getString("ProductName"),
                        rs.getString("Description"),
                        rs.getDouble("Price"),
                        c,
                        rs.getBoolean("IsActive")
                );
                list.add(p);
            }
        }
        return list;
    }

    // Thêm sản phẩm mới
    public void insert(Product p) throws SQLException {
        String sql = "INSERT INTO Products (ProductName, Description, Price, CategoryID, IsActive) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNameProducts());
            ps.setString(2, p.getDescriptionProducts());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getCategories().getCategoryId());  // Chỉnh sửa lại từ CategoryID
            ps.setBoolean(5, p.isActive());
            ps.executeUpdate();
        }
    }

    // Cập nhật sản phẩm
    public void update(Product p) throws SQLException {
        String sql = "UPDATE Products SET ProductName=?, Description=?, Price=?, CategoryID=?, IsActive=? WHERE ProductID=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNameProducts());
            ps.setString(2, p.getDescriptionProducts());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getCategories().getCategoryId());  // Chỉnh sửa lại từ CategoryID
            ps.setBoolean(5, p.isActive());
            ps.setInt(6, p.getId());  // Chỉnh sửa lại từ ProductID
            ps.executeUpdate();
        }
    }

    // Xóa sản phẩm
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Products WHERE ProductID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
