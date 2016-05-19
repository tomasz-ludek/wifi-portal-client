package com.dashngo.android;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.dashngo.android.barcode.CaptureActivityAnyOrientation;
import com.dashngo.android.net.ApiClient;
import com.dashngo.android.net.model.Product;
import com.dashngo.android.net.model.ProductWrapper;
import com.dashngo.android.net.model.StoreInfo;
import com.dashngo.android.tools.Configuration;
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

    private static final int REQUEST_CAMERA_PERMISSION = 0x11;

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
    @BindView(R.id.pay_button)
    Button payButtonView;

    private ApiClient apiClient;
    private Call<Map<String, Product>> productListCall;
    private Call<StoreInfo> storeInfoCall;

    private ShoppingCartAdapter shoppingCartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initToolbar();

        apiClient = ApiClient.getInstance();
        refreshStoreInfo();
        initShoppingCart();

        Configuration configuration = Configuration.getInstance();
        boolean firstRun = configuration.isFirstRun();
        if (firstRun) {
            configuration.setIsFirstRun(false);
            showHelp();
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowHomeEnabled(true);
            supportActionBar.setIcon(R.mipmap.ic_launcher);
        }
    }

    private void initShoppingCart() {
        shoppingCartAdapter = new ShoppingCartAdapter(new ArrayList<ProductWrapper>());
        shoppingCartView.setLayoutManager(new LinearLayoutManager(this));
        shoppingCartView.setAdapter(shoppingCartAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(shoppingCartView);
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

    private void refreshViewState() {
        payButtonView.setEnabled(shoppingCartAdapter.getItemCount() > 0);
    }

    @OnClick(R.id.scan_barcode)
    public void onScanQrButtonClick() {
        if (cameraPermissionGranted()) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        }
    }

    @OnClick(R.id.pay_button)
    public void onPayButtonClick() {
        Intent intent = ReceiptActivity.createIntent(this, (ArrayList<ProductWrapper>) shoppingCartAdapter.getItems());
        startActivity(intent);
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
            case R.id.action_help:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public boolean cameraPermissionGranted() {
        int checkResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (checkResult == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onScanQrButtonClick();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        showCameraPermissionDeniedDialog();
                    } else {
                        showCameraPermissionRationaleDialog();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showCameraPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.camera_permission_denied_dialog_title)
                .setMessage(R.string.camera_permission_denied_dialog_message)
                .setPositiveButton(R.string.permission_denied_positive_button_text, null)
                .setNegativeButton(R.string.permission_denied_negative_button_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onScanQrButtonClick();
                    }
                })
                .show();
    }

    private void showCameraPermissionRationaleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.permission_rationale_dialog_message)
                .setPositiveButton(R.string.permission_rationale_positive_button_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.permission_rationale_negative_button_text, null)
                .show();
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
                    refreshViewState();
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

        private void removeShoppingCartEntry(final int position) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Remove product?")
                    .setCancelable(true)
                    .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            shoppingCartAdapter.removeItem(position);
                            refreshViewState();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            shoppingCartAdapter.notifyItemChanged(position);
                        }
                    }).create().show();
        }
    };
}
