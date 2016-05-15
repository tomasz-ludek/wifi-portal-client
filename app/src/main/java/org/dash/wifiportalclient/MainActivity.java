package org.dash.wifiportalclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.dash.wifiportalclient.barcode.CaptureActivityAnyOrientation;
import org.dash.wifiportalclient.net.ApiClient;
import org.dash.wifiportalclient.net.model.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ApiClient apiClient;
    private Call<List<Product>> productListCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScanQrButtonClick(view);
//        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show();
            }
        });

        apiClient = ApiClient.getInstance();
    }

    @OnClick(R.id.fab)
    public void onScanQrButtonClick(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            processBarcode(scanResult);
        }
    }

    private void processBarcode(final IntentResult barcode) {
        productListCall = apiClient.productList();
        productListCall.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (!response.isSuccessful()) {
                    //error
                }
                List<Product> productList = response.body();
                Map<String, Product> productMap = new HashMap<>();
                for (Product product : productList) {
                    productMap.put(product.getUpcString(), product);
                }

//                Long upc = barcode.getContents();
                Long upc = 1l;
                Product product = productMap.get(upc);
                if (product != null) {
                    //show info
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (productListCall != null) {
            productListCall.cancel();
        }
    }
}
