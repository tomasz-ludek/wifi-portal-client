package com.dashngo.android.net.model;

import com.google.gson.annotations.SerializedName;

public class StoreInfo {

    private String name;
    @SerializedName("dashprice")
    private String dashPrice;
    private String fiat;
    private String address;

    public String getName() {
        return name;
    }

    public String getDashPrice() {
        return dashPrice;
    }

    public float getDashPriceFloat() {
        return Float.parseFloat(dashPrice);
    }

    public String getFiat() {
        return fiat;
    }

    public String getAddress() {
        return address;
    }
}