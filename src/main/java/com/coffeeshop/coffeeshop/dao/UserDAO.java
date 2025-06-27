package com.coffeeshop.coffeeshop.dao;

import com.coffeeshop.coffeeshop.model.Role;
import com.coffeeshop.coffeeshop.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    public User login(String username, String password) throws SQLException {
        String query = """
            SELECT u.UserID, u.UserName, u.Password,
                   r.RoleID, r.RoleName, r.Description
            FROM Users u
            JOIN Roles r ON u.RoleID = r.RoleID
            WHERE u.UserName = ? AND u.Password = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Role role = new Role(
                        rs.getInt("RoleID"),
                        rs.getString("RoleName"),
                        rs.getString("Description")
                );

                User user = new User();
                user.setUserId(rs.getInt("UserID"));
                user.setUserName(rs.getString("UserName"));
                user.setPassword(rs.getString("Password"));
                user.setRole(role);
                return user;
            }
        }
        return null;
    }

    public boolean isUsernameTaken(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM Users WHERE UserName = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    public boolean register(User user) throws SQLException {
        String query = "INSERT INTO Users (UserName, Password, RoleID) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getRole().getId()); // Lưu RoleID
            return ps.executeUpdate() > 0;
        }
    }
}
