package com.dashngo.android;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dashngo.android.net.model.ProductWrapper;

import java.util.List;
import java.util.Locale;

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.CustomViewHolder> {

    private List<ProductWrapper> dataset;

    public ShoppingCartAdapter(Context context, List<ProductWrapper> dataset) {
        this.dataset = dataset;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.shopping_cart_item, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        ProductWrapper productItem = dataset.get(i);
        customViewHolder.setName(productItem.getName());
        customViewHolder.setPrice(productItem.getPriceFloat(), productItem.getQuantity());
        customViewHolder.setDashAddress(productItem.getAddress());
        customViewHolder.setQuantity(productItem.getQuantity() + " x");
    }

    @Override
    public int getItemCount() {
        return (dataset != null ? dataset.size() : 0);
    }

    public void increaseQuantity(int position, int value) {
        dataset.get(position).increaseQuantity(value);
        notifyItemChanged(position);
    }

    public void addItem(ProductWrapper item) {
        int position = findItem(item.getUpc());
        if (position >= 0) {
            ProductWrapper productWrapper = dataset.get(position);
            productWrapper.increaseQuantity(1);
            notifyItemChanged(position);
        } else {
            dataset.add(item);
            notifyItemInserted(dataset.size());
        }
    }

    public void removeItem(int position) {
        ProductWrapper productWrapper = dataset.get(position);
        if (productWrapper.getQuantity() > 1) {
            productWrapper.increaseQuantity(-1);
            notifyItemChanged(position);
        } else {
            dataset.remove(position);
            notifyItemRemoved(position);
        }
    }

    private int findItem(String upc) {
        for (int i = 0; i < dataset.size(); i++) {
            ProductWrapper product = dataset.get(i);
            if (upc.equals(product.getUpc())) {
                return i;
            }
        }
        return -1;
    }

    public ProductWrapper getItem(int position) {
        return dataset.get(position);
    }

    public List<ProductWrapper> getItems() {
        return dataset;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView quantityView;
        private TextView nameView;
        private TextView priceView;
        private TextView totalPriceView;
        private TextView dashAddressView;

        public CustomViewHolder(View view) {
            super(view);
            quantityView = (TextView) view.findViewById(R.id.quantity);
            nameView = (TextView) view.findViewById(R.id.name);
            priceView = (TextView) view.findViewById(R.id.price);
            totalPriceView = (TextView) view.findViewById(R.id.total_price);
            dashAddressView = (TextView) view.findViewById(R.id.dash_address);
        }

        public void setQuantity(String quantity) {
            quantityView.setText(quantity);
        }

        public void setName(String name) {
            nameView.setText(name);
        }

        public void setPrice(Float price, float quantity) {
            totalPriceView.setText(formatPrice(price * quantity));
            priceView.setVisibility(quantity > 1 ? View.VISIBLE : View.GONE);
            priceView.setText(formatPrice(price));
        }

        private String formatPrice(float value) {
            return String.format(Locale.US, "$%.2f", value);
        }

        public void setDashAddress(String dashAddress) {
            dashAddressView.setText(dashAddress);
        }
    }
}
