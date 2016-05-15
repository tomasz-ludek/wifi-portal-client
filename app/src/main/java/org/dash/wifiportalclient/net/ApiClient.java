package org.dash.wifiportalclient.net;

import org.dash.wifiportalclient.net.model.Product;
import org.dash.wifiportalclient.net.model.StoreInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String poloniexApiEndpoint = "http://dashngo.com/";

    private DashNGoApi dashNGoApi;

    private static ApiClient sInstance;

    public static synchronized ApiClient getInstance() {
        if (sInstance == null) {
            sInstance = new ApiClient();
        }
        return sInstance;
    }

    public ApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.poloniexApiEndpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        dashNGoApi = retrofit.create(DashNGoApi.class);
    }

    public Call<StoreInfo> storeInfo(Callback<StoreInfo> callback) {
        return dashNGoApi.storeInfo();
    }

    public Call<List<Product>> productList() {
        return dashNGoApi.productList();
    }
}
