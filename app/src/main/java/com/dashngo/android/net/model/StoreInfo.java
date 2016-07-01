package com.dashngo.android.net.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class StoreInfo implements Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.dashPrice);
        dest.writeString(this.fiat);
        dest.writeString(this.address);
    }

    public StoreInfo() {
    }

    public StoreInfo(String name, String dashPrice, String fiat, String address) {
        this.name = name;
        this.dashPrice = dashPrice;
        this.fiat = fiat;
        this.address = address;
    }

    protected StoreInfo(Parcel in) {
        this.name = in.readString();
        this.dashPrice = in.readString();
        this.fiat = in.readString();
        this.address = in.readString();
    }

    public static final Parcelable.Creator<StoreInfo> CREATOR = new Parcelable.Creator<StoreInfo>() {
        @Override
        public StoreInfo createFromParcel(Parcel source) {
            return new StoreInfo(source);
        }

        @Override
        public StoreInfo[] newArray(int size) {
            return new StoreInfo[size];
        }
    };
}