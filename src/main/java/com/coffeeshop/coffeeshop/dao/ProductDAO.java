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
        String sql = "SELECT p.*, c.id as c_id, c.nameCategories " +
                "FROM products p " +
                "JOIN Categories c ON p.category_id = c.id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categories c = new Categories(
                        rs.getInt("c_id"),
                        rs.getString("nameCategories")
                );

                float discount = rs.getFloat("discount");
                if (rs.wasNull()) {
                    discount = 0f;
                }

                Product p = new Product(
                        rs.getInt("id"),
                        rs.getString("nameProducts"),
                        rs.getString("descriptionProducts"),
                        rs.getDouble("price"),
                        c,
                        rs.getBoolean("is_active"),
                        rs.getString("image"),
                        discount
                );
                list.add(p);
            }
        }
        return list;
    }

    public void insert(Product p) throws SQLException {
        String sql = "INSERT INTO products " +
                "(nameProducts, descriptionProducts, price, category_id, is_active, image, discount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNameProducts());
            ps.setString(2, p.getDescriptionProducts());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getCategories().getId());
            ps.setBoolean(5, p.isActive());
            ps.setString(6, p.getImage());
            ps.setFloat(7, p.getDiscount());
            ps.executeUpdate();
        }
    }

    public void update(Product p) throws SQLException {
        String sql = "UPDATE products SET " +
                "nameProducts = ?, " +
                "descriptionProducts = ?, " +
                "price = ?, " +
                "category_id = ?, " +
                "is_active = ?, " +
                "image = ?, " +
                "discount = ? " +
                "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNameProducts());
            ps.setString(2, p.getDescriptionProducts());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getCategories().getId());
            ps.setBoolean(5, p.isActive());
            ps.setString(6, p.getImage());
            ps.setFloat(7, p.getDiscount());
            ps.setInt(8, p.getId());
            ps.executeUpdate();
        }
    }

    public Product getProductById(int productId) throws SQLException {
        String sql = "SELECT p.*, c.id as c_id, c.nameCategories " +
                "FROM products p " +
                "JOIN Categories c ON p.category_id = c.id " +
                "WHERE p.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Categories category = new Categories(
                            rs.getInt("c_id"),
                            rs.getString("nameCategories")
                    );

                    float discount = rs.getFloat("discount");
                    if (rs.wasNull()) {
                        discount = 0f;
                    }

                    return new Product(
                            rs.getInt("id"),
                            rs.getString("nameProducts"),
                            rs.getString("descriptionProducts"),
                            rs.getDouble("price"),
                            category,
                            rs.getBoolean("is_active"),
                            rs.getString("image"),
                            discount
                    );
                }
            }
        }
        return null;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
