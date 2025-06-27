package com.coffeeshop.coffeeshop.dao;

import com.coffeeshop.coffeeshop.model.Categories;
import com.coffeeshop.coffeeshop.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriesDao {

    // Dùng chung DBConnection (SQL Server)
    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    // Get all categories
    public List<Categories> getAllCategories() {
        List<Categories> categories = new ArrayList<>();
        String query = "SELECT * FROM Categories"; // Tên bảng C viết hoa trong SQL Server

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int categoryId = resultSet.getInt("CategoryID");  // Đổi từ id thành CategoryID
                String categoryName = resultSet.getString("CategoryName");  // Đổi từ nameCategories thành CategoryName
                categories.add(new Categories(categoryId, categoryName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // Add new category
    public boolean addCategory(Categories category) {
        String query = "INSERT INTO Categories (CategoryName) VALUES (?)";  // Đổi từ nameCategories thành CategoryName

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, category.getNameCategories());  // Đổi từ getNameCategories thành getCategoryName
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update category
    public boolean updateCategory(Categories category) {
        String query = "UPDATE Categories SET CategoryName = ? WHERE CategoryID = ?";  // Đổi từ nameCategories thành CategoryName, id thành CategoryID

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, category.getNameCategories());  // Đổi từ getNameCategories thành getCategoryName
            preparedStatement.setInt(2, category.getCategoryId());  // Đổi từ getId thành getCategoryId
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete category
    public boolean deleteCategory(int categoryId) {
        String query = "DELETE FROM Categories WHERE CategoryID = ?";  // Đổi từ id thành CategoryID

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, categoryId);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
