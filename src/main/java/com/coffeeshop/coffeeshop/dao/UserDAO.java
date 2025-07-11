package com.coffeeshop.coffeeshop.dao;

import com.coffeeshop.coffeeshop.model.Role;
import com.coffeeshop.coffeeshop.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private final Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    public User login(String username, String password) throws SQLException {
        String query = """
            SELECT u.id, u.username, u.password,
                   r.id AS role_id, r.nameRole, r.descriptionRole
            FROM Users u
            JOIN Roles r ON u.role_id = r.id
            WHERE u.username = ? AND u.password = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Role role = new Role(
                        rs.getInt("role_id"),
                        rs.getString("nameRole"),
                        rs.getString("descriptionRole")
                );

                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(role);
                return user;
            }
        }
        return null;
    }

    public boolean isUsernameTaken(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM Users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    public boolean register(User user) throws SQLException {
        String query = "INSERT INTO Users (username, password, role_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getRole().getId()); // Lưu role_id
            return ps.executeUpdate() > 0;
        }
    }
}
