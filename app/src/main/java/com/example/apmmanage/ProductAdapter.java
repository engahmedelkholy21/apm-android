package com.example.apmmanage;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final List<PricingInvoiceItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(PricingInvoiceItem item);
        void onQuantityChange(PricingInvoiceItem item, int newQuantity);
    }

    public ProductAdapter(List<PricingInvoiceItem> items, OnItemClickListener listener) {
        this.items = items != null ? items : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pricing_invoice, parent, false); // correct layout reference
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView itemNameTv;
        private final TextView itemCodeTv;
        private final TextView itemQtyTv;
        private final TextView itemPriceTv;
        private final ImageButton deleteItemBtn;

        private TextWatcher currentWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemNameTv = itemView.findViewById(R.id.itemNameTv);
            itemCodeTv = itemView.findViewById(R.id.itemCodeTv);
            itemQtyTv = itemView.findViewById(R.id.itemQtyTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            deleteItemBtn = itemView.findViewById(R.id.deleteItemBtn);
        }

        public void bind(PricingInvoiceItem item) {
            if (item == null || item.getProduct() == null) return;

            itemNameTv.setText(item.getProduct().getName());
            itemCodeTv.setText(item.getProduct().getInternationalCode());
            itemQtyTv.setText("Qty: " + item.getQuantity());
            itemPriceTv.setText(String.format("EGP %.2f", item.getUnitPrice()));

            deleteItemBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(item);
                }
            });

            // Optional: If you want to support quantity changes inside item layout,
            // use an EditText instead of TextView and manage TextWatcher safely.
        }
    }
}
