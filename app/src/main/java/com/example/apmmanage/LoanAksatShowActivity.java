package com.example.apmmanage;

import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import java.sql.*;
import java.util.*;

public class LoanAksatShowActivity extends AppCompatActivity {

    AutoCompleteTextView loanInput;
    Button searchBtn;
    TextView cstName, loanCode, mandoob, damen, moqadma, extra, masareef, finalAmount, totalPaid, totalRemaining;
    TextView loanNotes, reb7, taklefa;
    RecyclerView aksatRecycler, productRecycler;

    ArrayList<AksatModel> aksatList = new ArrayList<>();
    ArrayList<ProductModel> productList = new ArrayList<>();
    ArrayList<String> nameSuggestions = new ArrayList<>();
    AksatAdapter aksatAdapter;
    ProductAdapter productAdapter;

    String currentFar3 = GlobalData.userFar3;
    String loan_code = "", customer_name = "";
    int fatora_num = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_aksat_show);

        initViews();
        loadNameSuggestions();

        searchBtn.setOnClickListener(v -> {
            String input = loanInput.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "ادخل كود أو اسم العميل", Toast.LENGTH_SHORT).show();
                return;
            }

            clearData();

            if (input.matches("\\d+")) {
                loan_code = input;
                getLoanByCode();
            } else {
                customer_name = input;
                getLoanByName();
            }

            loadAksat();
        });

        loanInput.setOnItemClickListener((parent, view, position, id) -> {
            customer_name = nameSuggestions.get(position);
        });
    }
    void initViews() {
        loanInput = findViewById(R.id.loanCodeInput);
        searchBtn = findViewById(R.id.searchBtn);
        cstName = findViewById(R.id.cstName);
        loanCode = findViewById(R.id.loanCode);
        mandoob = findViewById(R.id.mandoob);
        damen = findViewById(R.id.damen);
        moqadma = findViewById(R.id.moqadma);
        extra = findViewById(R.id.extra);
        masareef = findViewById(R.id.masareef);
        finalAmount = findViewById(R.id.finalAmount);
        totalPaid = findViewById(R.id.totalPaid);
        totalRemaining = findViewById(R.id.totalRemaining);
        loanNotes = findViewById(R.id.loanNotes);
        reb7 = findViewById(R.id.reb7);
        taklefa = findViewById(R.id.taklefa);

        aksatRecycler = findViewById(R.id.aksatRecycler);
        productRecycler = findViewById(R.id.productsRecycler);

        aksatAdapter = new AksatAdapter(this, aksatList);
        productAdapter = new ProductAdapter(this, productList);

        aksatRecycler.setLayoutManager(new LinearLayoutManager(this));
        productRecycler.setLayoutManager(new LinearLayoutManager(this));
        aksatRecycler.setAdapter(aksatAdapter);
        productRecycler.setAdapter(productAdapter);
    }

    void clearData() {
        aksatList.clear();
        productList.clear();
        aksatAdapter.notifyDataSetChanged();
        productAdapter.notifyDataSetChanged();

        cstName.setText(""); loanCode.setText(""); mandoob.setText(""); damen.setText("");
        moqadma.setText(""); extra.setText(""); masareef.setText(""); finalAmount.setText("");
        totalPaid.setText(""); totalRemaining.setText("");
        loanNotes.setText(""); reb7.setText(""); taklefa.setText("");
        fatora_num = -1;
    }

    void loadNameSuggestions() {
        try {
            Connection conn = new ConnectionHelper().conclass();
            String sql = "SELECT DISTINCT loan_cst_name FROM loans_requests_table WHERE loan_far3 = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentFar3);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                nameSuggestions.add(rs.getString("loan_cst_name"));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            Toast.makeText(this, "Name Suggest Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, nameSuggestions);
        loanInput.setAdapter(adapter);
    }
    void getLoanByCode() {
        try {
            Connection conn = new ConnectionHelper().conclass();
            String sql = "SELECT fatora_num, loan_notes, arba7_cost, takelfa_fatora FROM ( " +
                    "SELECT loans.loan_code AS kest_loan_code, loans.fatora_num, loans.loan_notes, " +
                    "arba7.arba7_cost, arba7.takelfa_fatora " +
                    "FROM loans_requests_table AS loans " +
                    "LEFT JOIN customers AS cust ON loans.loan_cst_code = cust.cst_ID " +
                    "LEFT JOIN customers AS d1 ON loans.loan_1st_damen_code = d1.cst_ID " +
                    "LEFT JOIN customers AS d2 ON loans.loan_2nd_damen_code = d2.cst_ID " +
                    "LEFT JOIN arba7 ON loans.fatora_num = arba7.arba7_fatora_id " +
                    ") AS byan_7ala_table " +
                    "WHERE kest_loan_code = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, loan_code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                fatora_num = rs.getInt("fatora_num");
                loanNotes.setText(rs.getString("loan_notes"));
                reb7.setText(rs.getString("arba7_cost"));
                taklefa.setText(rs.getString("takelfa_fatora"));
            }

            rs.close(); stmt.close();

            // Get sales madfoo3 from sales_table
            if (fatora_num > 0) {
                PreparedStatement s = conn.prepareStatement("SELECT sales_madfoo3 FROM sales_table WHERE sales_id = ? AND sales_pro_stock = ?");
                s.setInt(1, fatora_num);
                s.setString(2, currentFar3);
                ResultSet sr = s.executeQuery();
                if (sr.next()) {
                    moqadma.setText(sr.getString("sales_madfoo3"));
                }
                sr.close();
                s.close();
            }

            // Get additional loan info
            PreparedStatement info = conn.prepareStatement("SELECT loan_code, loan_cst_name, loan_mandoob, loan_1st_damen_name, loan_extra_amount, loans_msareef, loan_final_amount FROM loans_requests_table WHERE loan_code = ?");
            info.setString(1, loan_code);
            ResultSet r2 = info.executeQuery();
            if (r2.next()) {
                cstName.setText(r2.getString("loan_cst_name"));
                loanCode.setText(r2.getString("loan_code"));
                mandoob.setText(r2.getString("loan_mandoob"));
                damen.setText(r2.getString("loan_1st_damen_name"));
                extra.setText(r2.getString("loan_extra_amount"));
                masareef.setText(r2.getString("loans_msareef"));
                finalAmount.setText(r2.getString("loan_final_amount"));
            }

            r2.close();
            info.close();

            loadProducts();

        } catch (Exception e) {
            Toast.makeText(this, "Load By Code Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    void getLoanByName() {
        try {
            Connection conn = new ConnectionHelper().conclass();
            String sql = "SELECT TOP 1 loan_code FROM loans_requests_table WHERE loan_cst_name = ? AND loan_far3 = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, customer_name);
            stmt.setString(2, currentFar3);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                loan_code = rs.getString("loan_code");
                getLoanByCode(); // Reuse
            }
            rs.close(); stmt.close();
        } catch (Exception e) {
            Toast.makeText(this, "Load By Name Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    void loadAksat() {
        double total = 0;
        try {
            Connection conn = new ConnectionHelper().conclass();
            String sql = "SELECT kest_number, kest_amount, kest_status, kest_est7kak_date, kest_pay_date, kest_notes " +
                    "FROM aksat_table WHERE kest_cst_name = ? AND kest_far3 = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, cstName.getText().toString());
            stmt.setString(2, currentFar3);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                double amount = rs.getDouble("kest_amount");
                if ("مسدد".equals(rs.getString("kest_status"))) {
                    total += amount;
                }

                aksatList.add(new AksatModel(
                        rs.getInt("kest_number"),
                        amount,
                        rs.getString("kest_status"),
                        rs.getString("kest_est7kak_date"),
                        rs.getString("kest_pay_date"),
                        rs.getString("kest_notes")
                ));
            }

            rs.close(); stmt.close();
        } catch (Exception e) {
            Toast.makeText(this, "Load Aksat Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        totalPaid.setText(String.valueOf(total));
        try {
            double finalVal = Double.parseDouble(finalAmount.getText().toString());
            totalRemaining.setText(String.valueOf(finalVal - total));
        } catch (Exception ex) {
            totalRemaining.setText("0");
        }

        aksatAdapter.notifyDataSetChanged();
    }

    void loadProducts() {
        if (fatora_num <= 0) return;
        try {
            Connection conn = new ConnectionHelper().conclass();
            String sql = "SELECT sales_product_name, sales_product_count, sales_unit_price, " +
                    "sales_product_count * sales_unit_price AS sales_price_for_sell " +
                    "FROM sales_table WHERE sales_id = ? AND sales_pro_stock = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, fatora_num);
            stmt.setString(2, currentFar3);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                productList.add(new ProductModel(
                        rs.getString("sales_product_name"),
                        rs.getDouble("sales_product_count"),
                        rs.getDouble("sales_unit_price"),
                        rs.getDouble("sales_price_for_sell")
                ));
            }

            rs.close(); stmt.close();
        } catch (Exception e) {
            Toast.makeText(this, "Load Products Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        productAdapter.notifyDataSetChanged();
    }

    // ================= MODELS ===================
    public static class AksatModel {
        int number;
        double amount;
        String status, estDate, payDate, notes;

        public AksatModel(int number, double amount, String status, String estDate, String payDate, String notes) {
            this.number = number;
            this.amount = amount;
            this.status = status;
            this.estDate = estDate;
            this.payDate = payDate;
            this.notes = notes;
        }
    }

    public static class ProductModel {
        String name;
        double quantity, unitPrice, total;

        public ProductModel(String name, double quantity, double unitPrice, double total) {
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.total = total;
        }
    }

    // ================= ADAPTERS ===================
    public static class AksatAdapter extends RecyclerView.Adapter<AksatAdapter.Holder> {
        Context context;
        ArrayList<AksatModel> list;

        public AksatAdapter(Context context, ArrayList<AksatModel> list) {
            this.context = context;
            this.list = list;
        }

        public static class Holder extends RecyclerView.ViewHolder {
            TextView line1, line2, line3;

            public Holder(View view) {
                super(view);
                line1 = view.findViewById(R.id.line1);
                line2 = view.findViewById(R.id.line2);
                line3 = view.findViewById(R.id.line3);
            }
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.activity_loan_aksat_show_row, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(Holder h, int i) {
            AksatModel m = list.get(i);
            h.line1.setText("القسط: " + m.number + " | " + m.status);
            h.line2.setText("المبلغ: " + m.amount + " | الاستحقاق: " + m.estDate);
            h.line3.setText("السداد: " + m.payDate + " | ملاحظات: " + m.notes);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    public static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.Holder> {
        Context context;
        ArrayList<ProductModel> list;

        public ProductAdapter(Context context, ArrayList<ProductModel> list) {
            this.context = context;
            this.list = list;
        }

        public static class Holder extends RecyclerView.ViewHolder {
            TextView line;

            public Holder(View view) {
                super(view);
                line = view.findViewById(R.id.line);
            }
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.activity_loan_product_row, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(Holder h, int i) {
            ProductModel m = list.get(i);
            h.line.setText("المنتج: " + m.name + " | الكمية: " + m.quantity +
                    " | السعر: " + m.unitPrice + " | الإجمالي: " + m.total);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}
