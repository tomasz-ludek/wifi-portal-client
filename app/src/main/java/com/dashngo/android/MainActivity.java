package com.dashngo.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.crashlytics.android.Crashlytics;
import com.dashngo.android.barcode.CaptureActivityAnyOrientation;
import com.dashngo.android.net.ApiClient;
import com.dashngo.android.net.model.Product;
import com.dashngo.android.net.model.ProductWrapper;
import com.dashngo.android.net.model.StoreInfo;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
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
    @BindView(R.id.shopping_cart)
    RecyclerView shoppingCartView;

    private ApiClient apiClient;
    private Call<Map<String, Product>> productListCall;
    private Call<StoreInfo> storeInfoCall;

    private ShoppingCartAdapter shoppingCartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        apiClient = ApiClient.getInstance();
        refreshStoreInfo();
        initShoppingCart();
    }

    private void initShoppingCart() {
        shoppingCartAdapter = new ShoppingCartAdapter(this, new ArrayList<ProductWrapper>());
        shoppingCartView.setLayoutManager(new LinearLayoutManager(this));
        shoppingCartView.setAdapter(shoppingCartAdapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                switch (swipeDir) {
                    case ItemTouchHelper.LEFT:
                        shoppingCartAdapter.increaseQuantity(position, 1);
                        break;
                    case ItemTouchHelper.RIGHT:
                        ProductWrapper item = shoppingCartAdapter.getItem(position);
                        if (item.getQuantity() > 1) {
                            shoppingCartAdapter.increaseQuantity(position, -1);
                        } else {
                            removeShoppingCartEntry(position);
                        }
                        break;
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(shoppingCartView);
    }

    private void removeShoppingCartEntry(final int position) {
        new AlertDialog.Builder(this)
                .setMessage("Remove product?")
                .setCancelable(true)
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shoppingCartAdapter.removeItem(position);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shoppingCartAdapter.notifyItemChanged(position);
                    }
                }).create().show();
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
        productListCall.enqueue(new Callback<Map<String, Product>>() {
            @Override
            public void onResponse(Call<Map<String, Product>> call, Response<Map<String, Product>> response) {
                if (!response.isSuccessful()) {
                    //error
                }
                Map<String, Product> productMap = response.body();
                String upc = barcode.getContents();
                Product product = productMap.get(upc);
                if (product != null) {
                    ProductWrapper productWrapper = ProductWrapper.wrap(product);
                    productWrapper.setUpc(upc);
                    shoppingCartAdapter.addItem(productWrapper);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Product>> call, Throwable t) {

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
