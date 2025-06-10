package com.example.apmmanage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Adapter for displaying product items in a list
 */
public class ProductListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<SellActivity.ProductItem> items;
    private LayoutInflater inflater;
    private DecimalFormat decimalFormat;

    public ProductListAdapter(Context context, ArrayList<SellActivity.ProductItem> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
        this.decimalFormat = new DecimalFormat("#,##0.00");
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
            holder.nameTextView = convertView.findViewById(R.id.productNameTextView);
            holder.quantityTextView = convertView.findViewById(R.id.quantityTextView);
            holder.priceTextView = convertView.findViewById(R.id.priceTextView);
            holder.totalTextView = convertView.findViewById(R.id.totalTextView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the current item
        SellActivity.ProductItem item = items.get(position);

        // Set the data
        holder.nameTextView.setText(item.getName());
        holder.quantityTextView.setText(decimalFormat.format(item.getQuantity()));
        holder.priceTextView.setText(decimalFormat.format(item.getPrice()));

        // Calculate total
        double total = item.getQuantity() * item.getPrice();
        holder.totalTextView.setText(decimalFormat.format(total));

        return convertView;
    }

    private static class ViewHolder {
        TextView nameTextView;
        TextView quantityTextView;
        TextView priceTextView;
        TextView totalTextView;
    }
}