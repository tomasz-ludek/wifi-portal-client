package com.dashngo.android.net.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ProductWrapper implements Parcelable {

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

    public String getAddress() {
        return product.getAddress();
    }

    public Float getPriceFloat() {
        String price = product.getPrice();
        return Float.parseFloat(price);
    }

    public float getTotalPrice() {
        return getPriceFloat() * getQuantity();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void increaseQuantity(int value) {
        quantity += value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.product);
        dest.writeInt(this.quantity);
    }

    protected ProductWrapper(Parcel in) {
        this.product = (Product) in.readSerializable();
        this.quantity = in.readInt();
    }

    public static final Parcelable.Creator<ProductWrapper> CREATOR = new Parcelable.Creator<ProductWrapper>() {
        @Override
        public ProductWrapper createFromParcel(Parcel source) {
            return new ProductWrapper(source);
        }

        @Override
        public ProductWrapper[] newArray(int size) {
            return new ProductWrapper[size];
        }
    };
}