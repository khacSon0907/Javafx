package com.coffeeshop.coffeeshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private int orderID;      // ID đơn hàng
    private int userID;       // ID người dùng
    private User user;        // Thông tin người dùng nếu cần
}
