package com.dashngo.android.data;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Product")
public class ProductEntity extends Model {

    @Column(name = "Receipt")
    public ReceiptEntity receipt;

    @Column(name = "Name")
    public String name;

    @Column(name = "DashAddress")
    public String dashAddress;

    @Column(name = "Price")
    public float price;

    @Column(name = "Quantity")
    public int quantity;
}
