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
                        rs.getString("image_path"),
                        c,
                        rs.getBoolean("is_active")
                );
                list.add(p);
            }
        }
        return list;
    }

    public void insert(Product p) throws SQLException {
        String sql = "INSERT INTO products (nameProducts, descriptionProducts, price, image_path, category_id, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNameProducts());
            ps.setString(2, p.getDescriptionProducts());
            ps.setDouble(3, p.getPrice());
            ps.setString(4, p.getImage_path());
            ps.setInt(5, p.getCategories().getId());
            ps.setBoolean(6, p.isActive());
            ps.executeUpdate();
        }
    }

    public void update(Product p) throws SQLException {
        String sql = "UPDATE products SET nameProducts=?, descriptionProducts=?, price=?, image_path=?, category_id=?, is_active=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNameProducts());
            ps.setString(2, p.getDescriptionProducts());
            ps.setDouble(3, p.getPrice());
            ps.setString(4, p.getImage_path());
            ps.setInt(5, p.getCategories().getId());
            ps.setBoolean(6, p.isActive());
            ps.setInt(7, p.getId());
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
