package com.dashngo.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.dashngo.android.net.model.ProductWrapper;
import com.dashngo.android.tools.ExtTextUtils;
import com.google.protobuf.ByteString;

import org.bitcoin.protocols.payments.Protos;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.script.ScriptBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.schildbach.wallet.integration.android.DashIntegration;

public class ReceiptActivity extends AppCompatActivity {

    private static final String EXTRA_PRODUCT_LIST = ReceiptActivity.class.getSimpleName() + "extra_product_list";

    private static final int REQUEST_CODE = 0x01;

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
    @BindView(R.id.payment_status)
    TextView paymentStatusView;

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

    @OnClick(R.id.payment_status)
    public void onPaymentStatusClick() {
        if (transactionHash != null) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://test.explorer.dash.org/tx/" + transactionHash));
            startActivity(browserIntent);
        }
        Toast.makeText(this, "Payment Status Clicked", Toast.LENGTH_LONG).show();
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
        } catch (AddressFormatException e) {
            Toast.makeText(this, "Invalid address found", Toast.LENGTH_LONG).show();
            return;
        }

        final Protos.PaymentDetails.Builder paymentDetails = Protos.PaymentDetails.newBuilder();
        paymentDetails.setNetwork(basicNetworkParams.getPaymentProtocolId());
        Protos.Output.Builder[] outputArr = createOutputs(productList);
        for (Protos.Output.Builder output : outputArr) {
            paymentDetails.addOutputs(output);
        }
        paymentDetails.setMemo("Dash N Go payment #" + (1000 + new Random().nextInt(1000)));
        paymentDetails.setTime(System.currentTimeMillis());

        final Protos.PaymentRequest.Builder paymentRequest = Protos.PaymentRequest.newBuilder();
        paymentRequest.setSerializedPaymentDetails(paymentDetails.build().toByteString());

        DashIntegration.requestForResult(this, REQUEST_CODE, paymentRequest.build().toByteArray());
    }

    private Protos.Output.Builder[] createOutputs(List<ProductWrapper> productList) {
        int count = productList.size();
        Protos.Output.Builder[] outputArr = new Protos.Output.Builder[count];
        try {
            for (int i = 0; i < count; i++) {
                String address = productList.get(i).getAddress();
                float totalPriceDash = productList.get(i).getTotalPrice();
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
                transactionHash = DashIntegration.transactionHashFromResult(data);
                if (transactionHash != null) {
                    final SpannableStringBuilder messageBuilder = new SpannableStringBuilder("Transaction hash:\n");
                    messageBuilder.append(transactionHash);
                    messageBuilder.setSpan(new TypefaceSpan("monospace"), messageBuilder.length() - transactionHash.length(), messageBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    if (DashIntegration.paymentFromResult(data) != null) {
                        messageBuilder.append("\n(also a BIP70 payment message was received)");
                    }

                    paymentStatusView.setText(messageBuilder);
                    paymentStatusView.setVisibility(View.VISIBLE);
                }

                Toast.makeText(this, "Thank you!", Toast.LENGTH_LONG).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Unknown result.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
