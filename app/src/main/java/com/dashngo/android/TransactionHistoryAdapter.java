package com.dashngo.android;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dashngo.android.data.ReceiptEntity;
import com.dashngo.android.tools.ExtTextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder> {

    private List<ReceiptEntity> dataset;
    private OnItemClickListener onItemClickListener;

    public TransactionHistoryAdapter(List<ReceiptEntity> dataset, OnItemClickListener onItemClickListener) {
        this.dataset = dataset;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.history_cart_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        ReceiptEntity receipt = dataset.get(i);
        viewHolder.setStoreName(receipt.storeName);
        viewHolder.setDate(receipt.date);
        viewHolder.setTotalCost(receipt.totalCost);
        viewHolder.setTransactionHash(receipt.transactionHash);
    }

    @Override
    public int getItemCount() {
        return (dataset != null ? dataset.size() : 0);
    }

    public ReceiptEntity getItem(int position) {
        return dataset.get(position);
    }

    public ArrayList<ReceiptEntity> getItems() {
        return (ArrayList<ReceiptEntity>) dataset;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView shopNameView;
        private TextView totalCostView;
        private TextView dateView;
        private TextView txHashView;

        public ViewHolder(View view) {
            super(view);
            shopNameView = (TextView) view.findViewById(R.id.shop_name);
            totalCostView = (TextView) view.findViewById(R.id.total_cost);
            dateView = (TextView) view.findViewById(R.id.date);
            txHashView = (TextView) view.findViewById(R.id.tx_hash);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    ReceiptEntity item = getItem(position);
                    onItemClickListener.onItemClick(position, item);
                }
            });
        }

        public void setStoreName(String name) {
            shopNameView.setText(name);
        }

        public void setTotalCost(Float totalPrice) {
            totalCostView.setText(ExtTextUtils.formatPrice("$", totalPrice));
        }

        public void setDate(Date date) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
            dateView.setText(dateFormat.format(date));
        }

        public void setTransactionHash(String txHash) {
            txHashView.setText(txHash);
        }

    }

    public interface OnItemClickListener {
        void onItemClick(int position, ReceiptEntity item);
    }
}
