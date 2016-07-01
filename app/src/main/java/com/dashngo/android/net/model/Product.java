package com.dashngo.android.net.model;

import com.dashngo.android.data.ProductEntity;

import java.io.Serializable;

public class Product implements Serializable {

    private String upc;
    private String name;
    private String price;
    private String address;

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUpc() {
        return upc;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getAddress() {
        return address;
    }

    public static Product convert(ProductEntity historyProduct) {
        Product product = new Product();
        product.name = historyProduct.name;
        product.address = historyProduct.dashAddress;
        product.price = Float.toString(historyProduct.price);
        return product;
    }
}