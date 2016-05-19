package com.dashngo.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.dashngo.android.net.model.ProductWrapper;
import com.dashngo.android.tools.ExtTextUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReceiptActivity extends AppCompatActivity {

    private static final String EXTRA_PRODUCT_LIST = ReceiptActivity.class.getSimpleName() + "extra_product_list";

    public static Intent createIntent(Context context, ArrayList<ProductWrapper> productList) {
        Intent intent = new Intent(context, ReceiptActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_PRODUCT_LIST, productList);
        return intent;
    }

    @BindView(R.id.shopping_cart)
    RecyclerView shoppingCartView;
    @BindView(R.id.pay_button)
    Button payButtonView;
    @BindView(R.id.total_cost)
    TextView totalCostView;

    private ShoppingCartAdapter shoppingCartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);
        ButterKnife.bind(this);

        initToolbar();
        initView();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowHomeEnabled(true);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("Receipt");
        }
    }

    private void initView() {
        ArrayList<Parcelable> productListParcel = getIntent().getParcelableArrayListExtra(EXTRA_PRODUCT_LIST);
        ArrayList<ProductWrapper> productList = new ArrayList<>();
        for (Parcelable parcel : productListParcel) {
            productList.add((ProductWrapper) parcel);
        }

        shoppingCartAdapter = new ShoppingCartAdapter(productList);
        shoppingCartView.setLayoutManager(new LinearLayoutManager(this));
        shoppingCartView.setAdapter(shoppingCartAdapter);

        float totalCost = 0;
        for (ProductWrapper product : productList) {
            totalCost += product.getTotalPrice();
        }
        String totalCostStr = ExtTextUtils.formatPrice(totalCost);
        totalCostView.setText(totalCostStr);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }
}
