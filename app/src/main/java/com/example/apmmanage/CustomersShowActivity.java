package com.example.apmmanage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class CustomersShowActivity extends AppCompatActivity {

    AutoCompleteTextView nameFilter;
    TextView totalTxt;
    RecyclerView recyclerView;
    ArrayList<CustomerModel> customers = new ArrayList<>();
    ArrayList<String> allNames = new ArrayList<>();
    CustomerAdapter adapter;
    ConnectionHelper connectionHelper = new ConnectionHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_show);

        nameFilter = findViewById(R.id.nameFilter);
        totalTxt = findViewById(R.id.totalTxt);
        recyclerView = findViewById(R.id.recyclerView);
        Button searchBtn = findViewById(R.id.searchBtn);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerAdapter(this, customers);
        recyclerView.setAdapter(adapter);

        loadCustomers("");
        setupAutoComplete();

        searchBtn.setOnClickListener(v -> {
            String name = nameFilter.getText().toString().trim();
            loadCustomers(name);
        });
    }

    void setupAutoComplete() {
        allNames.clear();
        try {
            Connection conn = connectionHelper.conclass();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT cst_name FROM customers");
            while (rs.next()) {
                allNames.add(rs.getString("cst_name"));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            Toast.makeText(this, "AutoComplete Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, allNames);
        nameFilter.setAdapter(adapter);
    }

    void loadCustomers(String nameFilterValue) {
        customers.clear();
        try {
            Connection conn = connectionHelper.conclass();
            Statement stmt = conn.createStatement();
            String query = "SELECT cst_ID, cst_name, cst_phone, cst_address, cst_notes, " +
                    "cst_price_category, cst_national_id, cst_takseet_status, betaka_url FROM customers";
            if (nameFilterValue == null || nameFilterValue.trim().isEmpty()) {
                query += " ORDER BY cst_ID DESC";
            } else {
                query += " WHERE cst_name LIKE N'%" + nameFilterValue + "%' ORDER BY cst_ID DESC";
            }

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String name = rs.getString("cst_name");
                if (name != null && !name.trim().isEmpty()) {
                    customers.add(new CustomerModel(
                            rs.getInt("cst_ID"),
                            name,
                            rs.getString("cst_phone"),
                            rs.getString("cst_address"),
                            rs.getString("cst_notes"),
                            rs.getString("cst_price_category"),
                            rs.getString("cst_national_id"),
                            rs.getString("cst_takseet_status"),
                            rs.getString("betaka_url")
                    ));
                }
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            Toast.makeText(this, "Load Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        adapter.notifyDataSetChanged();
        totalTxt.setText("عدد العملاء: " + customers.size());
    }

    public static class CustomerModel {
        public int id;
        public String name, phone, address, notes, priceCategory, nationalId, takseetStatus, betakaUrl;

        public CustomerModel(int id, String name, String phone, String address, String notes,
                             String priceCategory, String nationalId, String takseetStatus, String betakaUrl) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.address = address;
            this.notes = notes;
            this.priceCategory = priceCategory;
            this.nationalId = nationalId;
            this.takseetStatus = takseetStatus;
            this.betakaUrl = betakaUrl;
        }
    }

    public static class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
        private final ArrayList<CustomerModel> list;
        private final Context context;

        public CustomerAdapter(Context context, ArrayList<CustomerModel> list) {
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
            View view = LayoutInflater.from(context).inflate(R.layout.activity_customers_show_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            CustomerModel customer = list.get(position);

            holder.name.setText("الاسم: " + customer.name);

            if (customer.phone != null && !customer.phone.trim().isEmpty()) {
                holder.phone.setVisibility(View.VISIBLE);
                holder.phone.setText("الهاتف: " + customer.phone);
                holder.callBtn.setVisibility(View.VISIBLE);
                holder.whatsappBtn.setVisibility(View.VISIBLE);

                holder.callBtn.setOnClickListener(v -> { String phone = customer.phone.replaceAll("[^\\d]", "");
                    if (!phone.startsWith("2")) {
                        phone = "2" + phone;
                    }

                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{android.Manifest.permission.CALL_PHONE}, 1);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:+" + phone));
                        context.startActivity(intent);
                    }
                });

                holder.whatsappBtn.setOnClickListener(v -> {
                    String phone = customer.phone.replaceAll("[^\\d]", "");
                    if (!phone.startsWith("2")) {
                        phone = "2" + phone;
                    }
                    String url = "https://wa.me/+" + phone;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    context.startActivity(i);
                });

            } else {
                holder.phone.setVisibility(View.GONE);
                holder.callBtn.setVisibility(View.GONE);
                holder.whatsappBtn.setVisibility(View.GONE);
            }

            if (customer.address != null && !customer.address.trim().isEmpty()) {
                holder.address.setVisibility(View.VISIBLE);
                holder.address.setText("العنوان: " + customer.address);
            } else {
                holder.address.setVisibility(View.GONE);
            }

            if (customer.notes != null && !customer.notes.trim().isEmpty()) {
                holder.notes.setVisibility(View.VISIBLE);
                holder.notes.setText("ملاحظات: " + customer.notes);
            } else {
                holder.notes.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}
