package com.coffeeshop.coffeeshop.model;


import jdk.jfr.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private int id;
    private String nameProducts;
    private  String descriptionProducts;
    private double price;
    private Categories categories;
    private boolean active;


}
