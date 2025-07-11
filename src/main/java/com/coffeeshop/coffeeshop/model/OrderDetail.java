package com.coffeeshop.coffeeshop.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {

    private int orderDetailId;
    private int orderId;
    private int productId;
    private String size;
    private int quantity;
    private double unitPrice;
    private String productName;
    private String productImage;

    /**
     * Thuộc tính discount (%) cho món
     */
    private float discount;

    /**
     * ImageView để hiển thị ảnh sản phẩm
     */
    private ImageView productImageView;

    public void setProductImage(String productImagePath) {
        this.productImage = productImagePath;
        if (productImagePath != null && !productImagePath.isEmpty()) {
            File file = new File(productImagePath);
            if (file.exists()) {
                Image img = new Image(file.toURI().toString(), 50, 50, true, true);
                this.productImageView = new ImageView(img);
            } else {
                this.productImageView = new ImageView(
                        new Image(getClass().getResourceAsStream("/images/no-image.png"), 50, 50, true, true)
                );
            }
        } else {
            this.productImageView = new ImageView(
                    new Image(getClass().getResourceAsStream("/images/no-image.png"), 50, 50, true, true)
            );
        }
    }

    /**
     * Trả về discount dạng "10%" để binding vào TableView
     */
    public String getDiscountFormatted() {
        return discount > 0 ? String.format("%.0f%%", discount) : "0%";
    }

    /**
     * Tính thành tiền của từng OrderDetail
     */
    public double getTotalPrice() {
        return quantity * unitPrice;
    }
}
