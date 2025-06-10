package com.example.apmmanage;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.view.Gravity;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.ViewGroup;
import android.view.View;
import android.graphics.Typeface;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class AksatSummaryActivity extends AppCompatActivity {

    EditText searchInput;
    RecyclerView recyclerView;
    AksatSummaryAdapter adapter;
    ArrayList<AksatSummaryModel> dataList = new ArrayList<>();
    Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aksat_summary);

        searchInput = findViewById(R.id.search_input);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        connection = new ConnectionHelper().conclass();

        fetchData("");

        searchInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                fetchData(s.toString());
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void fetchData(String keyword) {
        dataList.clear();
        try {
            if (connection != null) {
                Statement stmt = connection.createStatement();
                String query = "SELECT kest_loan_code, " +
                        "MIN(kest_cst_name) AS kest_cst_name, " +
                        "SUM(kest_amount) AS total_loan_amount, " +
                        "SUM(CASE WHEN kest_status = N'مسدد' THEN kest_amount ELSE 0 END) AS total_paid, " +
                        "SUM(CASE WHEN kest_status = N'غير مسدد' THEN kest_amount ELSE 0 END) AS total_not_paid " +
                        "FROM aksat_table " +
                        "WHERE kest_loan_code LIKE '%" + keyword + "%' " +
                        "OR kest_cst_name LIKE N'%" + keyword + "%' " +
                        "GROUP BY kest_loan_code ORDER BY kest_loan_code";

                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    AksatSummaryModel model = new AksatSummaryModel();
                    model.loanCode = rs.getString("kest_loan_code");
                    model.customerName = rs.getString("kest_cst_name");
                    model.totalLoan = rs.getDouble("total_loan_amount");
                    model.totalPaid = rs.getDouble("total_paid");
                    model.totalNotPaid = rs.getDouble("total_not_paid");
                    dataList.add(model);
                }

                adapter = new AksatSummaryAdapter(this, dataList);
                recyclerView.setAdapter(adapter);

            } else {
                Toast.makeText(this, "فشل الاتصال", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public class AksatSummaryModel {
        public String loanCode;
        public String customerName;
        public double totalLoan;
        public double totalPaid;
        public double totalNotPaid;
    }

    public class AksatSummaryAdapter extends RecyclerView.Adapter<AksatSummaryAdapter.ViewHolder> {

        Context context;
        ArrayList<AksatSummaryModel> data;

        public AksatSummaryAdapter(Context context, ArrayList<AksatSummaryModel> data) {
            this.context = context;
            this.data = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            row.setPadding(6, 6, 6, 6);

            TextView loanCode = createCell(1);
            TextView customerName = createCell(2);
            TextView totalLoan = createCell(1);
            TextView totalPaid = createCell(1);
            TextView totalNotPaid = createCell(1);

            row.addView(loanCode);
            row.addView(customerName);
            row.addView(totalLoan);
            row.addView(totalPaid);
            row.addView(totalNotPaid);

            return new ViewHolder(row, loanCode, customerName, totalLoan, totalPaid, totalNotPaid);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AksatSummaryModel item = data.get(position);
            holder.loanCode.setText(item.loanCode);
            holder.customerName.setText(item.customerName);
            holder.totalLoan.setText(String.format("%.0f", item.totalLoan));
            holder.totalPaid.setText(String.format("%.0f", item.totalPaid));
            holder.totalNotPaid.setText(String.format("%.0f", item.totalNotPaid));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        private TextView createCell(float weight) {
            TextView tv = new TextView(context);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
            tv.setTextSize(14);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(4, 4, 4, 4);
            tv.setTypeface(Typeface.MONOSPACE);
            return tv;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView loanCode, customerName, totalLoan, totalPaid, totalNotPaid;

            public ViewHolder(View itemView, TextView loanCode, TextView customerName,
                              TextView totalLoan, TextView totalPaid, TextView totalNotPaid) {
                super(itemView);
                this.loanCode = loanCode;
                this.customerName = customerName;
                this.totalLoan = totalLoan;
                this.totalPaid = totalPaid;
                this.totalNotPaid = totalNotPaid;
            }
        }
    }
}
