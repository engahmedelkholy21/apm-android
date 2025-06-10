package com.example.apmmanage;

import android.app.DatePickerDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.*;
import java.util.*;

public class DelayedAksatActivity extends AppCompatActivity {

    Button fromBtn, toBtn, searchBtn;
    TextView totalCount, totalAmount, fromTxt, toTxt;
    RecyclerView recyclerView;
    Calendar calendarFrom = Calendar.getInstance();
    Calendar calendarTo = Calendar.getInstance();

    ArrayList<AksatModel> aksatList = new ArrayList<>();
    AksatAdapter adapter;
    String title = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delayed_aksat);

        title = getIntent().getStringExtra("title");

        fromBtn = findViewById(R.id.fromBtn);
        toBtn = findViewById(R.id.toBtn);
        searchBtn = findViewById(R.id.searchBtn);
        totalCount = findViewById(R.id.totalCount);
        totalAmount = findViewById(R.id.totalAmount);
        fromTxt = findViewById(R.id.fromTxt);
        toTxt = findViewById(R.id.toTxt);
        recyclerView = findViewById(R.id.recyclerView);

        fromTxt.setText(getFormattedDate(calendarFrom));
        toTxt.setText(getFormattedDate(calendarTo));

        adapter = new AksatAdapter(this, aksatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fromBtn.setOnClickListener(v -> pickDate(calendarFrom, fromTxt));
        toBtn.setOnClickListener(v -> pickDate(calendarTo, toTxt));
        searchBtn.setOnClickListener(v -> loadData());
    }

    void pickDate(Calendar cal, TextView target) {
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            target.setText(getFormattedDate(cal));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    String getFormattedDate(Calendar c) {
        return String.format(Locale.US, "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    void loadData() {
        aksatList.clear();
        double total = 0;
        String status = title.contains("المسددة") ? "مسدد" : "غير مسدد";
        String dateField = status.equals("مسدد") ? "kest_pay_date" : "kest_est7kak_date";

        String dateFrom = getFormattedDate(calendarFrom);
        String dateTo = getFormattedDate(calendarTo);
        String far3 = GlobalData.userFar3;

        try {
            Connection conn = new ConnectionHelper().conclass();
            String query = "SELECT kest_number, kest_cst_name, kest_loan_code, kest_amount, kest_est7kak_date, kest_pay_date, kest_status, " +
                    "kest_1st_damen_name, cst_phone, cst_address " +
                    "FROM aksat_table INNER JOIN customers ON aksat_table.kest_cst_code = customers.cst_ID " +
                    "WHERE kest_status = ? AND " + dateField + " BETWEEN ? AND ? AND kest_far3 = ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, status);
            stmt.setString(2, dateFrom);
            stmt.setString(3, dateTo);
            stmt.setString(4, far3);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                double amount = rs.getDouble("kest_amount");
                total += amount;
                aksatList.add(new AksatModel(
                        rs.getInt("kest_number"),
                        rs.getString("kest_cst_name"),
                        rs.getString("kest_loan_code"),
                        amount,
                        rs.getString("kest_est7kak_date"),
                        rs.getString("kest_pay_date"),
                        rs.getString("kest_status"),
                        rs.getString("kest_1st_damen_name"),
                        rs.getString("cst_phone"),
                        rs.getString("cst_address")
                ));
            }
            rs.close(); stmt.close();
        } catch (Exception e) {
            Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        adapter.notifyDataSetChanged();
        totalCount.setText("عدد الأقساط: " + aksatList.size());
        totalAmount.setText("إجمالي: " + total);
    }

    public static class AksatModel {
        int number;
        String name, loanCode, est7kakDate, payDate, status, damen, phone, address;
        double amount;

        public AksatModel(int number, String name, String loanCode, double amount, String est7kakDate, String payDate, String status, String damen, String phone, String address) {
            this.number = number;
            this.name = name;
            this.loanCode = loanCode;
            this.amount = amount;
            this.est7kakDate = est7kakDate;
            this.payDate = payDate;
            this.status = status;
            this.damen = damen;
            this.phone = phone;
            this.address = address;
        }
    }

    public static class AksatAdapter extends RecyclerView.Adapter<AksatAdapter.Holder> {

        Context context;
        ArrayList<AksatModel> list;

        public AksatAdapter(Context context, ArrayList<AksatModel> list) {
            this.context = context;
            this.list = list;
        }

        public static class Holder extends RecyclerView.ViewHolder {
            TextView line1, line2, line3;
            Button callBtn, whatsappBtn;

            public Holder(View view) {
                super(view);
                line1 = view.findViewById(R.id.line1);
                line2 = view.findViewById(R.id.line2);
                line3 = view.findViewById(R.id.line3);
                callBtn = view.findViewById(R.id.callBtn);
                whatsappBtn = view.findViewById(R.id.whatsappBtn);
            }
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.activity_delayed_aksat_row, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(Holder h, int i) {
            AksatModel m = list.get(i);
            h.line1.setText("القسط: " + m.number + " | العميل: " + m.name);
            h.line2.setText("المبلغ: " + m.amount + " | الضامن: " + m.damen + " | الكود: " + m.loanCode);
            h.line3.setText("تاريخ: " + (m.status.equals("مسدد") ? m.payDate : m.est7kakDate) + " | " + m.status);

            boolean hasPhone = m.phone != null && !m.phone.trim().isEmpty();
            h.callBtn.setVisibility(hasPhone ? View.VISIBLE : View.GONE);
            h.whatsappBtn.setVisibility(hasPhone ? View.VISIBLE : View.GONE);

            if (hasPhone) {
                String phone = m.phone.replaceAll("[^\\d]", "");
                if (!phone.startsWith("2")) phone = "2" + phone;
                final String finalPhone = phone;

                h.callBtn.setOnClickListener(v ->
                        context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+" + finalPhone)))
                );

                h.whatsappBtn.setOnClickListener(v -> {
                    String url = "https://wa.me/+" + finalPhone;
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                });
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}
