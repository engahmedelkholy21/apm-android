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

public class LoansUnderRevisionActivity extends AppCompatActivity {

    String statusTitle = "";
    TextView totalTxt;
    AutoCompleteTextView filterInput;
    Button searchBtn;
    RecyclerView recyclerView;
    ArrayList<LoanModel> loans = new ArrayList<>();
    LoanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loans_under_revision);

        statusTitle = getIntent().getStringExtra("status_title");

        filterInput = findViewById(R.id.cstFilter);
        searchBtn = findViewById(R.id.searchBtn);
        totalTxt = findViewById(R.id.totalTxt);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LoanAdapter(this, loans);
        recyclerView.setAdapter(adapter);

        searchBtn.setOnClickListener(v -> {
            String input = filterInput.getText().toString().trim();
            loadLoans(input);
        });

        loadLoans("");
    }

    void loadLoans(String filter) {
        loans.clear();
        try {
            Connection conn = new ConnectionHelper().conclass();
            Statement stmt = conn.createStatement();

            String baseQuery = "SELECT loan_code, loan_cst_name, loan_cst_code, loan_amount, loan_extra_amount, loan_final_amount, loan_1st_damen_name, loan_period, loans_msareef FROM loans_requests_table WHERE loan_agreed_or_not = N'" + statusTitle + "'";
            if (!filter.isEmpty()) {
                baseQuery += " AND (loan_cst_name LIKE N'%" + filter + "%' OR loan_cst_code LIKE N'%" + filter + "%')";
            }
            baseQuery += " ORDER BY loan_code DESC";

            ResultSet rs = stmt.executeQuery(baseQuery);
            while (rs.next()) {
                loans.add(new LoanModel(
                        rs.getString("loan_code"),
                        rs.getString("loan_cst_name"),
                        rs.getString("loan_cst_code"),
                        rs.getDouble("loan_amount"),
                        rs.getDouble("loan_extra_amount"),
                        rs.getDouble("loan_final_amount"),
                        rs.getString("loan_1st_damen_name"),
                        rs.getString("loan_period"),
                        rs.getDouble("loans_msareef")
                ));
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            Toast.makeText(this, "Load Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        adapter.notifyDataSetChanged();
        totalTxt.setText("عدد العمليات: " + loans.size());
    }

    public static class LoanModel {
        public String code, name, cstCode, damen1, period;
        public double amount, extra, total, masareef;

        public LoanModel(String code, String name, String cstCode, double amount, double extra, double total, String damen1, String period, double masareef) {
            this.code = code;
            this.name = name;
            this.cstCode = cstCode;
            this.amount = amount;
            this.extra = extra;
            this.total = total;
            this.damen1 = damen1;
            this.period = period;
            this.masareef = masareef;
        }
    }

    public static class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.ViewHolder> {
        private final ArrayList<LoanModel> list;
        private final Context context;

        public LoanAdapter(Context context, ArrayList<LoanModel> list) {
            this.context = context;
            this.list = list;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView line1, amountTxt, extraTxt, masareefTxt, damenTxt, periodTxt;

            public ViewHolder(View view) {
                super(view);
                line1 = view.findViewById(R.id.line1);
                amountTxt = view.findViewById(R.id.amountTxt);
                extraTxt = view.findViewById(R.id.extraTxt);
                masareefTxt = view.findViewById(R.id.masareefTxt);
                damenTxt = view.findViewById(R.id.damenTxt);
                periodTxt = view.findViewById(R.id.periodTxt);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.activity_loans_under_revision_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder h, int position) {
            LoanModel l = list.get(position);

            h.line1.setText("العميل: " + l.name + " | الكود: " + l.code);
            h.amountTxt.setText(String.valueOf(l.amount));
            h.extraTxt.setText(String.valueOf(l.extra));
            h.masareefTxt.setText(String.valueOf(l.masareef));
            h.damenTxt.setText(l.damen1);
            h.periodTxt.setText(l.period + " شهر");
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}
