package com.coffeeshop.coffeeshop.model;

import javafx.beans.property.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
    private int orderID;       // ID đơn hàng
    private int productID;     // ID sản phẩm
    private int quantity;      // Số lượng
    private double price;      // Giá sản phẩm
}
