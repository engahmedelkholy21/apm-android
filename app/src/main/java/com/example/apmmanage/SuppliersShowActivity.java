package com.example.apmmanage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class SuppliersShowActivity extends AppCompatActivity {

    AutoCompleteTextView nameFilter;
    Button searchBtn;
    TextView totalTxt;
    RecyclerView recyclerView;
    ArrayList<SupplierModel> suppliers = new ArrayList<>();
    ArrayList<String> allNames = new ArrayList<>();
    SupplierAdapter adapter;
    ConnectionHelper connectionHelper = new ConnectionHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suppliers_show);

        nameFilter = findViewById(R.id.nameFilter);
        searchBtn = findViewById(R.id.searchBtn);
        totalTxt = findViewById(R.id.totalTxt);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SupplierAdapter(this, suppliers);
        recyclerView.setAdapter(adapter);

        setupAutoComplete();
        loadSuppliers("");

        searchBtn.setOnClickListener(v -> {
            String name = nameFilter.getText().toString().trim();
            loadSuppliers(name);
        });
    }

    void setupAutoComplete() {
        allNames.clear();
        try {
            Connection conn = connectionHelper.conclass();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT mwared_name FROM mwardeen");
            while (rs.next()) {
                allNames.add(rs.getString("mwared_name"));
            }
            rs.close(); stmt.close();
        } catch (Exception e) {
            Toast.makeText(this, "AutoComplete Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, allNames);
        nameFilter.setAdapter(adapter);
    }

    void loadSuppliers(String name) {
        suppliers.clear();
        try {
            Connection conn = connectionHelper.conclass();
            Statement stmt = conn.createStatement();
            String query = "SELECT mwared_ID, mwared_name, mwared_phone, mwared_address, mwared_notes FROM mwardeen";
            if (!name.isEmpty()) {
                query += " WHERE mwared_name LIKE N'%" + name + "%'";
            }
            query += " ORDER BY mwared_ID DESC";

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String mName = rs.getString("mwared_name");
                if (mName != null && !mName.trim().isEmpty()) {
                    suppliers.add(new SupplierModel(
                            rs.getInt("mwared_ID"),
                            mName,
                            rs.getString("mwared_phone"),
                            rs.getString("mwared_address"),
                            rs.getString("mwared_notes")
                    ));
                }
            }
            rs.close(); stmt.close();
        } catch (Exception e) {
            Toast.makeText(this, "Load Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        adapter.notifyDataSetChanged();
        totalTxt.setText("عدد الموردين: " + suppliers.size());
    }

    public static class SupplierModel {
        public int id;
        public String name, phone, address, notes;

        public SupplierModel(int id, String name, String phone, String address, String notes) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.address = address;
            this.notes = notes;
        }
    }

    public static class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.ViewHolder> {
        private final ArrayList<SupplierModel> list;
        private final Context context;

        public SupplierAdapter(Context context, ArrayList<SupplierModel> list) {
            this.context = context;
            this.list = list;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, phone, address, notes;
            Button callBtn, whatsappBtn;

            public ViewHolder(View view) {
                super(view);
                name = view.findViewById(R.id.itemName);
                phone = view.findViewById(R.id.itemPhone);
                address = view.findViewById(R.id.itemAddress);
                notes = view.findViewById(R.id.itemNotes);
                callBtn = view.findViewById(R.id.callBtn);
                whatsappBtn = view.findViewById(R.id.whatsappBtn);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.activity_suppliers_show_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            SupplierModel supplier = list.get(position);
            holder.name.setText("الاسم: " + supplier.name);

            if (supplier.phone != null && !supplier.phone.trim().isEmpty()) {
                holder.phone.setVisibility(View.VISIBLE);
                holder.phone.setText("الهاتف: " + supplier.phone);
                holder.callBtn.setVisibility(View.VISIBLE);
                holder.whatsappBtn.setVisibility(View.VISIBLE);

                holder.callBtn.setOnClickListener(v -> {
                    String phone = supplier.phone.replaceAll("[^\\d]", "");
                    if (!phone.startsWith("2")) phone = "2" + phone;
                    context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+" + phone)));
                });

                holder.whatsappBtn.setOnClickListener(v -> {
                    String phone = supplier.phone.replaceAll("[^\\d]", "");
                    if (!phone.startsWith("2")) phone = "2" + phone;
                    String url = "https://wa.me/+" + phone;
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                });
            } else {
                holder.phone.setVisibility(View.GONE);
                holder.callBtn.setVisibility(View.GONE);
                holder.whatsappBtn.setVisibility(View.GONE);
            }

            holder.address.setText("العنوان: " + supplier.address);
            holder.notes.setText("ملاحظات: " + supplier.notes);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}
