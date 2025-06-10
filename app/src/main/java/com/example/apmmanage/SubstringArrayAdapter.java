package com.example.apmmanage;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class SubstringArrayAdapter extends ArrayAdapter<String> implements Filterable {

    private final List<String> originalItems;
    private final List<String> filteredItems;

    public SubstringArrayAdapter(Context context, int resource, List<String> items) {
        super(context, resource, new ArrayList<>(items));
        this.originalItems = new ArrayList<>(items);
        this.filteredItems = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @Override
    public String getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                filteredItems.clear();

                if (constraint != null && constraint.length() > 0) {
                    String term = constraint.toString().toLowerCase();
                    for (String item : originalItems) {
                        if (item.toLowerCase().contains(term)) {
                            filteredItems.add(item);
                        }
                    }
                } else {
                    filteredItems.addAll(originalItems);
                }

                results.values = filteredItems;
                results.count = filteredItems.size();
                return results;
            }

            @Override protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };
    }
}
