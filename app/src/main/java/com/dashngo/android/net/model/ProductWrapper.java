package com.dashngo.android.net.model;

public class ProductWrapper {

    private Product product;

    private int quantity = 1;

    public static ProductWrapper wrap(Product product) {
        return new ProductWrapper(product);
    }

    private ProductWrapper(Product product) {
        this.product = product;
    }

    public String getUpc() {
        return product.getUpc();
    }

    public void setUpc(String upc) {
        product.setUpc(upc);
    }

    public String getName() {
        return product.getName();
    }

    public String getPrice() {
        return product.getPrice();
    }

    public String getAddress() {
        return product.getAddress();
    }

    public Float getPriceFloat() {
        String price = product.getPrice();
        return Float.parseFloat(price);
    }

    public int getQuantity() {
        return quantity;
    }

    public void increaseQuantity(int value) {
        quantity += value;
    }
}