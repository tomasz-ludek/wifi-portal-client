package org.dash.wifiportalclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.dash.wifiportalclient.barcode.CaptureActivityAnyOrientation;
import org.dash.wifiportalclient.net.ApiClient;
import org.dash.wifiportalclient.net.model.Product;
import org.dash.wifiportalclient.net.model.StoreInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.store_info_view_switcher)
    ViewSwitcher storeInfoViewSwitcher;
    @BindView(R.id.store_name)
    TextView storeNameView;
    @BindView(R.id.store_url)
    TextView storeUrlView;
    @BindView(R.id.dash_price)
    TextView dashPriceView;
    @BindView(R.id.fiat)
    TextView fiatView;

    private ApiClient apiClient;
    private Call<List<Product>> productListCall;
    private Call<StoreInfo> storeInfoCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        apiClient = ApiClient.getInstance();
        refreshStoreInfo();
    }

    private void refreshStoreInfo() {
        if (storeInfoViewSwitcher.getCurrentView() == storeInfoViewSwitcher.getChildAt(0)) {
            storeInfoViewSwitcher.showNext();
        }
        if (storeInfoCall != null) {
            storeInfoCall.cancel();
        }
        storeInfoCall = apiClient.storeInfo();
        storeInfoCall.enqueue(new Callback<StoreInfo>() {
            @Override
            public void onResponse(Call<StoreInfo> call, Response<StoreInfo> response) {
                if (response.isSuccessful()) {
                    StoreInfo storeInfo = response.body();
                    storeNameView.setText(storeInfo.getName());
                    storeUrlView.setText(storeInfo.getAddress());
                    dashPriceView.setText(storeInfo.getDashPrice());
                    fiatView.setText(storeInfo.getFiat());
                    storeInfoViewSwitcher.showPrevious();
                }
            }

            @Override
            public void onFailure(Call<StoreInfo> call, Throwable t) {
//                Snackbar.make(findViewById(R.id.scan_barcode), t.getMessage(), Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
    }

    @OnClick(R.id.scan_barcode)
    public void onScanQrButtonClick(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_info:
                refreshStoreInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        if (storeInfoCall != null) {
            storeInfoCall.cancel();
        }
        if (productListCall != null) {
            productListCall.cancel();
        }
    }
}
