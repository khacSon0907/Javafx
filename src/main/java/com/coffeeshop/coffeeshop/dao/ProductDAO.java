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

    public List<Product> getAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.id as c_id, c.nameCategories FROM products p JOIN Categories c ON p.category_id = c.id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categories c = new Categories(rs.getInt("c_id"), rs.getString("nameCategories"));
                Product p = new Product(
                        rs.getInt("id"),
                        rs.getString("nameProducts"),
                        rs.getString("descriptionProducts"),
                        rs.getDouble("price"),
                        c,
                        rs.getBoolean("is_active")
                );
                list.add(p);
            }
        }
        return list;
    }

    public void insert(Product p) throws SQLException {
        String sql = "INSERT INTO products (nameProducts, descriptionProducts, price, category_id, is_active) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNameProducts());
            ps.setString(2, p.getDescriptionProducts());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getCategories().getId());      // FIX: Đổi từ 5 thành 4
            ps.setBoolean(5, p.isActive());               // FIX: Đổi từ 6 thành 5
            ps.executeUpdate();
        }
    }

    public void update(Product p) throws SQLException {
        String sql = "UPDATE products SET nameProducts=?, descriptionProducts=?, price=?, category_id=?, is_active=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNameProducts());
            ps.setString(2, p.getDescriptionProducts());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getCategories().getId());      // FIX: Đổi từ 5 thành 4
            ps.setBoolean(5, p.isActive());               // FIX: Đổi từ 6 thành 5
            ps.setInt(6, p.getId());                      // FIX: Đổi từ 7 thành 6
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}