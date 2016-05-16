package com.dashngo.android;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dashngo.android.net.model.Product;

import java.util.List;

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.CustomViewHolder> {

    private List<Product> dataset;

    public ShoppingCartAdapter(Context context, List<Product> dataset) {
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
        Product productItem = dataset.get(i);
        customViewHolder.setName(productItem.getName());
        customViewHolder.setPrice("$" + productItem.getPrice());
        customViewHolder.setDashAddress(productItem.getAddress());
    }

    @Override
    public int getItemCount() {
        return (dataset != null ? dataset.size() : 0);
    }

    public void addItem(Product product) {
        dataset.add(product);
        notifyItemInserted(dataset.size());
    }

    public void removeItem(int position) {
        dataset.remove(position);
        notifyItemRemoved(position);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView nameView;
        private TextView priceView;
        private TextView dashAddressView;

        public CustomViewHolder(View view) {
            super(view);
            nameView = (TextView) view.findViewById(R.id.name);
            priceView = (TextView) view.findViewById(R.id.price);
            dashAddressView = (TextView) view.findViewById(R.id.dash_address);
        }

        public void setName(String name) {
            nameView.setText(name);
        }

        public void setPrice(String price) {
            priceView.setText(price);
        }

        public void setDashAddress(String dashAddress) {
            dashAddressView.setText(dashAddress);
        }
    }
}
