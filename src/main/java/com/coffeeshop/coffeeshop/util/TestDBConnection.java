package com.coffeeshop.coffeeshop.util;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDBConnection {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Kết nối thành công đến SQL Server!");
            } else {
                System.out.println("❌ Kết nối thất bại!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi kết nối SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
