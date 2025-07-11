package com.coffeeshop.coffeeshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private int orderId;
    private int staffId;
    private LocalDateTime orderDate;
    private double totalAmount;
    private String status;
    private String note;

}
