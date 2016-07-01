package com.dashngo.android.data;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.Date;
import java.util.List;

@Table(name = "Receipt")
public class ReceiptEntity extends Model {

    @Column(name = "StoreName")
    public String storeName;

    @Column(name = "StoreDashPrice")
    public String storeDashPrice;

    @Column(name = "StoreFiat")
    public String storeFiat;

    @Column(name = "Number")
    public long number;

    @Column(name = "TransactionHash")
    public String transactionHash;

    @Column(name = "TotalCost")
    public float totalCost;

    @Column(name = "Date")
    public Date date;

    public List<ProductEntity> products() {
        return getMany(ProductEntity.class, "Receipt");
    }

    public static List<ReceiptEntity> loadAll() {
        return new Select().from(ReceiptEntity.class).execute();
    }
}
