package com.dashngo.android.net;

import com.dashngo.android.net.model.Product;
import com.dashngo.android.net.model.StoreInfo;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DashNGoApi {

    @GET("storeinfo")
    Call<StoreInfo> storeInfo();

    @GET("productlist")
    Call<List<Product>> productList();

    @GET("productlist")
    Call<Map<Long, Product>> productListMap();
}
