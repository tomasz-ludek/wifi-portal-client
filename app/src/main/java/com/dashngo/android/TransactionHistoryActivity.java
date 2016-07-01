package com.dashngo.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.dashngo.android.data.ReceiptEntity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransactionHistoryActivity extends AppCompatActivity implements TransactionHistoryAdapter.OnItemClickListener {

    public static Intent createIntent(Context context) {
        return new Intent(context, TransactionHistoryActivity.class);
    }

    @BindView(R.id.transaction_history)
    RecyclerView shoppingCartView;

    TransactionHistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_history);
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
            supportActionBar.setTitle("Transaction History");
        }
    }

    private void initView() {
        List<ReceiptEntity> receiptEntities = ReceiptEntity.loadAll();
        historyAdapter = new TransactionHistoryAdapter(receiptEntities, this);
        shoppingCartView.setLayoutManager(new LinearLayoutManager(this));
        shoppingCartView.setAdapter(historyAdapter);
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

    @Override
    public void onItemClick(int position, ReceiptEntity item) {
        Intent intent = ReceiptActivity.createIntent(this, item.getId());
        startActivity(intent);
    }
}
