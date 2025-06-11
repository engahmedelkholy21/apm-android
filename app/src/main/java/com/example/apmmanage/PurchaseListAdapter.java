package com.example.apmmanage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

/** Adapter for displaying purchase items */
public class PurchaseListAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<BuyActivity.PurchaseItem> items;
    private final LayoutInflater inflater;
    private final DecimalFormat format = new DecimalFormat("#,##0.00");

    public PurchaseListAdapter(Context context, ArrayList<BuyActivity.PurchaseItem> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_product_in_invoice, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.productNameTextView);
            holder.qty = convertView.findViewById(R.id.quantityTextView);
            holder.price = convertView.findViewById(R.id.priceTextView);
            holder.total = convertView.findViewById(R.id.totalTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BuyActivity.PurchaseItem item = items.get(position);
        holder.name.setText(item.getName());
        holder.qty.setText(format.format(item.getQuantity()));
        holder.price.setText(format.format(item.getPrice()));
        holder.total.setText(format.format(item.getTotal()));
        return convertView;
    }

    private static class ViewHolder {
        TextView name, qty, price, total;
    }
}
