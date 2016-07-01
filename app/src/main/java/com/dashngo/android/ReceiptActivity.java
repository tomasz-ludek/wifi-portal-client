package com.dashngo.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TypefaceSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dashngo.android.data.ProductEntity;
import com.dashngo.android.data.ReceiptEntity;
import com.dashngo.android.net.model.Product;
import com.dashngo.android.net.model.ProductWrapper;
import com.dashngo.android.net.model.StoreInfo;
import com.dashngo.android.tools.ExtTextUtils;
import com.google.protobuf.ByteString;

import org.bitcoin.protocols.payments.Protos;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.script.ScriptBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.schildbach.wallet.integration.android.DashIntegration;

public class ReceiptActivity extends AppCompatActivity {

    private static final String EXTRA_HISTORY_RECEIPT_ID = ReceiptActivity.class.getSimpleName() + "extra_history_receipt_id";
    private static final String EXTRA_STORE_INFO = ReceiptActivity.class.getSimpleName() + "extra_store_info";
    private static final String EXTRA_PRODUCT_LIST = ReceiptActivity.class.getSimpleName() + "extra_product_list";

    private static final int REQUEST_CODE = 0x01;

    public static Intent createIntent(Context context, StoreInfo storeInfo, ArrayList<ProductWrapper> productList) {
        Intent intent = new Intent(context, ReceiptActivity.class);
        intent.putExtra(EXTRA_STORE_INFO, storeInfo);
        intent.putParcelableArrayListExtra(EXTRA_PRODUCT_LIST, productList);
        return intent;
    }

    public static Intent createIntent(Context context, long historyReceiptId) {
        Intent intent = new Intent(context, ReceiptActivity.class);
        intent.putExtra(EXTRA_HISTORY_RECEIPT_ID, historyReceiptId);
        return intent;
    }

    @BindView(R.id.shopping_cart)
    RecyclerView shoppingCartView;
    @BindView(R.id.pay_button)
    Button payButtonView;
    @BindView(R.id.total_cost_dash)
    TextView totalCostDashView;
    @BindView(R.id.total_cost_fiat)
    TextView totalCostFiatView;
    @BindView(R.id.payment_status)
    TextView paymentStatusView;

    private long receiptId = System.currentTimeMillis();
    private StoreInfo storeInfo;
    private ShoppingCartAdapter shoppingCartAdapter;

    private String transactionHash;

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
            supportActionBar.setTitle("Receipt #" + receiptId);
        }
    }

    private void initView() {
        ArrayList<ProductWrapper> productList;
        if (getIntent().hasExtra(EXTRA_HISTORY_RECEIPT_ID)) {
            long historyReceiptId = getIntent().getLongExtra(EXTRA_HISTORY_RECEIPT_ID, -1);
            ReceiptEntity receipt = ReceiptEntity.load(ReceiptEntity.class, historyReceiptId);
            storeInfo = new StoreInfo(receipt.storeName, receipt.storeDashPrice, receipt.storeFiat, null);
            productList = createProductList(receipt);
            setTransactionStatus(receipt.transactionHash, false);
        } else {
            storeInfo = getIntent().getParcelableExtra(EXTRA_STORE_INFO);
            productList = new ArrayList<>();
            ArrayList<Parcelable> productListParcel = getIntent().getParcelableArrayListExtra(EXTRA_PRODUCT_LIST);
            for (Parcelable parcel : productListParcel) {
                productList.add((ProductWrapper) parcel);
            }
        }

        shoppingCartAdapter = new ShoppingCartAdapter(productList);
        shoppingCartView.setLayoutManager(new LinearLayoutManager(this));
        shoppingCartView.setAdapter(shoppingCartAdapter);

        float totalCostFiat = shoppingCartAdapter.getTotalCost();
        for (ProductWrapper product : productList) {
            totalCostFiat += product.getTotalPrice();
        }
        String totalCostStr = ExtTextUtils.formatPrice("$", totalCostFiat);
        totalCostFiatView.setText(totalCostStr);
        float totalCostDash = totalCostFiat / storeInfo.getDashPriceFloat();
        String totalCostDashStr = ExtTextUtils.formatPrice("", totalCostDash);
        totalCostDashView.setText(totalCostDashStr);
    }

    private ArrayList<ProductWrapper> createProductList(ReceiptEntity receipt) {
        ArrayList<ProductWrapper> productList = new ArrayList<>();
        List<ProductEntity> historyProductList = receipt.products();
        for (ProductEntity historyProduct : historyProductList) {
            Product product = Product.convert(historyProduct);
            ProductWrapper productWrapper = ProductWrapper.wrap(product);
            productWrapper.setQuantity(historyProduct.quantity);
            productList.add(productWrapper);
        }
        return productList;
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

    @OnClick(R.id.payment_status)
    public void onPaymentStatusClick() {
        if (transactionHash != null) {
//            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://test.explorer.dash.org/tx/" + transactionHash));
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://explorer.dash.org/tx/" + transactionHash));
            startActivity(browserIntent);
        }
    }

    @OnClick(R.id.pay_button)
    public void onPayButtonClick() {
        performPayment();
    }

    private void performPayment() {
        List<ProductWrapper> productList = shoppingCartAdapter.getItems();

        NetworkParameters basicNetworkParams;
        try {
            String basicPaymentAddress = productList.get(0).getAddress();
            basicNetworkParams = Address.getParametersFromAddress(basicPaymentAddress);
            validateAddresses(basicNetworkParams, productList);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid address found", Toast.LENGTH_LONG).show();
            return;
        }

        final Protos.PaymentDetails.Builder paymentDetails = Protos.PaymentDetails.newBuilder();
        paymentDetails.setNetwork(basicNetworkParams.getPaymentProtocolId());
        Protos.Output.Builder[] outputArr = createOutputs(productList);
        for (Protos.Output.Builder output : outputArr) {
            paymentDetails.addOutputs(output);
        }
        paymentDetails.setMemo("Dash N Go\nPayment #" + (1000 + new Random().nextInt(1000)));
        paymentDetails.setTime(System.currentTimeMillis());

        final Protos.PaymentRequest.Builder paymentRequest = Protos.PaymentRequest.newBuilder();
        paymentRequest.setSerializedPaymentDetails(paymentDetails.build().toByteString());

        DashIntegration.requestForResult(this, REQUEST_CODE, paymentRequest.build().toByteArray(), true);
    }

    private Protos.Output.Builder[] createOutputs(List<ProductWrapper> productList) {
        int count = productList.size();
        Protos.Output.Builder[] outputArr = new Protos.Output.Builder[count];
        try {
            for (int i = 0; i < count; i++) {
                String address = productList.get(i).getAddress();
                float totalPriceDash = productList.get(i).getTotalPrice() / storeInfo.getDashPriceFloat();
                long totalPriceDashSatoshi = (long) (totalPriceDash * 100000000);

                final NetworkParameters params = Address.getParametersFromAddress(address);
                final Protos.Output.Builder output = Protos.Output.newBuilder();
                output.setAmount(totalPriceDashSatoshi);
                output.setScript(ByteString.copyFrom(ScriptBuilder.createOutputScript(new Address(params, address)).getProgram()));
                outputArr[i] = output;
            }
        } catch (final AddressFormatException x) {
            throw new RuntimeException(x);
        }
        return outputArr;
    }

    private void validateAddresses(NetworkParameters basicParams, List<ProductWrapper> productList) throws AddressFormatException {
        for (ProductWrapper product : productList) {
            String address = product.getAddress();
            NetworkParameters parameters = Address.getParametersFromAddress(address);
            boolean isTheSameProtocol = basicParams.getPaymentProtocolId().equals(parameters.getPaymentProtocolId());
            if (!isTheSameProtocol) {
                throw new AddressFormatException("All products payment addresses should use the same network! " + address);
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String transactionHashResult = DashIntegration.transactionHashFromResult(data);
                if (transactionHashResult != null) {
                    setTransactionStatus(transactionHashResult, DashIntegration.paymentFromResult(data) != null);
                    saveReceipt(transactionHashResult);
                }
                Toast.makeText(this, "Thank you!", Toast.LENGTH_LONG).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Unknown result.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setTransactionStatus(String transactionHash, boolean bip70received) {
        this.transactionHash = transactionHash;
        final SpannableStringBuilder messageBuilder = new SpannableStringBuilder("Transaction hash: ");
        messageBuilder.append(transactionHash);
        messageBuilder.setSpan(new TypefaceSpan("monospace"), messageBuilder.length() - transactionHash.length(), messageBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (bip70received) {
            messageBuilder.append("\n(also a BIP70 payment message was received)");
        }

        paymentStatusView.setText(messageBuilder);
        paymentStatusView.setVisibility(View.VISIBLE);
        payButtonView.setEnabled(false);
        payButtonView.setBackgroundResource(R.drawable.paid_with_dash_normal);
        findViewById(R.id.root_view).setBackgroundColor(Color.parseColor("#66BB6A"));
    }

    private void saveReceipt(String transactionHash) {

        ReceiptEntity receiptEntity = new ReceiptEntity();
        receiptEntity.storeName = storeInfo.getName();
        receiptEntity.storeDashPrice = storeInfo.getDashPrice();
        receiptEntity.storeFiat = storeInfo.getFiat();
        receiptEntity.number = receiptId;
        receiptEntity.transactionHash = transactionHash;
        receiptEntity.date = new Date();
        receiptEntity.totalCost = shoppingCartAdapter.getTotalCost();
        receiptEntity.save();

        List<ProductWrapper> productList = shoppingCartAdapter.getItems();
        for (ProductWrapper product : productList) {
            ProductEntity productEntity = new ProductEntity();
            productEntity.receipt = receiptEntity;
            productEntity.name = product.getName();
            productEntity.dashAddress = product.getAddress();
            productEntity.price = product.getPriceFloat();
            productEntity.quantity = product.getQuantity();
            productEntity.save();
        }
    }
}
