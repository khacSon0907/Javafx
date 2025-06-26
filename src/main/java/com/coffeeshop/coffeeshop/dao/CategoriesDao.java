package com.coffeeshop.coffeeshop.dao;

import com.coffeeshop.coffeeshop.model.Categories;
import com.coffeeshop.coffeeshop.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class    CategoriesDao {

    // Dùng chung DBConnection (SQL Server)
    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    // Get all categories
    public List<Categories> getAllCategories() {
        List<Categories> categories = new ArrayList<>();
        String query = "SELECT * FROM Categories"; // table C viết hoa đúng SQL Server

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("nameCategories");
                categories.add(new Categories(id, name));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // Add new category
    public boolean addCategory(Categories category) {
        String query = "INSERT INTO Categories (nameCategories) VALUES (?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, category.getNameCategories());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update category
    public boolean updateCategory(Categories category) {
        String query = "UPDATE Categories SET nameCategories = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, category.getNameCategories());
            preparedStatement.setInt(2, category.getId());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete category
    public boolean deleteCategory(int categoryId) {
        String query = "DELETE FROM Categories WHERE id = ?";

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
