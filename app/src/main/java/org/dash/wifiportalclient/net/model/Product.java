package org.dash.wifiportalclient.net.model;

public class Product {

    private long upc;
    private String name;
    private String price;
    private String address;

    public long getUpc() {
        return upc;
    }

    public String getUpcString() {
        return upc + "";
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public Float getPriceFloat() {
        return Float.parseFloat(price);
    }

    public String getAddress() {
        return address;
    }
}