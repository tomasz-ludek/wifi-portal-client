package com.dashngo.android.net.model;

public class Product {

    private String upc;
    private String name;
    private String price;
    private String address;

    public void setUpc(String upc) {
        this.upc = upc;
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
}