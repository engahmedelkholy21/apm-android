package com.example.apmmanage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PricingActivity extends AppCompatActivity {
    private EditText customerCodeEt, customerNameEt, customerPhoneEt, customerAddressEt;
    private EditText productNameEt, quantityEt, discountEt;
    private TextView subtotalTv, totalTv;
    private Button addProductBtn, saveInvoiceBtn;
    private ImageButton scanBarcodeBtn;
    private RecyclerView productsRecyclerView;

    private ProductAdapter productAdapter;
    private List<PricingInvoiceItem> invoiceItems;
    private ConnectionHelper connectionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pricing);

        initializeViews();

        invoiceItems = new ArrayList<>();
        connectionHelper = new ConnectionHelper();

        setupRecyclerView();
        setupListeners();
    }

    private void initializeViews() {
        customerCodeEt = findViewById(R.id.customerCodeEt);
        customerNameEt = findViewById(R.id.customerNameEt);
        customerPhoneEt = findViewById(R.id.customerPhoneEt);
        customerAddressEt = findViewById(R.id.customerAddressEt);
        productNameEt = findViewById(R.id.productNameEt);
        quantityEt = findViewById(R.id.quantityEt);
        discountEt = findViewById(R.id.discountEt);
        subtotalTv = findViewById(R.id.subtotalTv);
        totalTv = findViewById(R.id.totalTv);
        addProductBtn = findViewById(R.id.addProductBtn);
        saveInvoiceBtn = findViewById(R.id.saveInvoiceBtn);
        scanBarcodeBtn = findViewById(R.id.scanBarcodeBtn);
        productsRecyclerView = findViewById(R.id.productsRecyclerView);

        quantityEt.setText("1");
        discountEt.setText("0");
    }

    private void setupRecyclerView() {
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(invoiceItems, new ProductAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(PricingInvoiceItem item) {
                invoiceItems.remove(item);
                productAdapter.notifyDataSetChanged();
                updateTotals();
            }

            @Override
            public void onQuantityChange(PricingInvoiceItem item, int newQuantity) {
                item.setQuantity(newQuantity);
                updateTotals();
                productAdapter.notifyDataSetChanged();
            }
        });
        productsRecyclerView.setAdapter(productAdapter);
        productsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void setupListeners() {
        customerCodeEt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.length() > 0) new LoadCustomerTask().execute(s.toString());
            }
        });

        addProductBtn.setOnClickListener(v -> addProduct());
        saveInvoiceBtn.setOnClickListener(v -> new SaveInvoiceTask().execute());
        scanBarcodeBtn.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("Scan product barcode");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.initiateScan();
        });
        discountEt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                updateTotals();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            productNameEt.setText(result.getContents());
            addProduct();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addProduct() {
        String code = productNameEt.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Please enter product code", Toast.LENGTH_SHORT).show();
            return;
        }
        new AddProductTask().execute(code);
    }

    private void clearProductInputs() {
        productNameEt.setText("");
        quantityEt.setText("1");
        productNameEt.requestFocus();
    }

    private void updateTotals() {
        double subtotal = 0;
        for (PricingInvoiceItem item : invoiceItems) subtotal += item.getTotal();
        double discount = 0;
        try { discount = Double.parseDouble(discountEt.getText().toString()); }
        catch (NumberFormatException e) { discountEt.setText("0"); }
        subtotalTv.setText(String.format("%.2f", subtotal));
        totalTv.setText(String.format("%.2f", subtotal - discount));
    }

    // AsyncTask: Add Product
    private class AddProductTask extends AsyncTask<String, Void, PricingInvoiceItem> {
        private Exception error;

        @Override
        protected PricingInvoiceItem doInBackground(String... params) {
            String code = params[0];
            try (Connection conn = connectionHelper.conclass()) {
                String sql = "SELECT * FROM products_table WHERE (pro_int_code = ? OR pro_name = ?) AND pro_stock = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, code);
                stmt.setString(2, code);

                stmt.setString(3, GlobalData.userFar3);  // third '?'
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) return null;

                Product product = new Product();
                product.setInternationalCode(rs.getString("pro_int_code"));
                product.setName(rs.getString("pro_name"));
                double bee3 = rs.getDouble("pro_bee3");
                product.setSellingPrice(bee3);
                String stock = rs.getString("pro_stock");
                product.setStock(stock);
                int qty = Integer.parseInt(quantityEt.getText().toString());

                return new PricingInvoiceItem(product, qty, bee3, stock);
            } catch (Exception e) {
                error = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(PricingInvoiceItem item) {
            if (error != null) {
                Toast.makeText(PricingActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (item == null) {
                Toast.makeText(PricingActivity.this, "Product not found", Toast.LENGTH_SHORT).show();
                return;
            }

            for (PricingInvoiceItem existing : invoiceItems) {
                if (existing.getProduct().getInternationalCode().equals(item.getProduct().getInternationalCode())) {
                    existing.setQuantity(existing.getQuantity() + item.getQuantity());
                    productAdapter.notifyDataSetChanged();
                    updateTotals();
                    clearProductInputs();
                    return;
                }
            }
            invoiceItems.add(item);
            productAdapter.notifyDataSetChanged();
            updateTotals();
            clearProductInputs();
            productsRecyclerView.smoothScrollToPosition(invoiceItems.size() - 1);
        }
    }

    private class LoadCustomerTask extends AsyncTask<String, Void, Customer> {
        private Exception error;

        @Override
        protected Customer doInBackground(String... codes) {
            try (Connection conn = connectionHelper.conclass()) {
                String sql = "SELECT * FROM customers WHERE cst_ID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, codes[0]);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Customer c = new Customer();
                    c.setName(rs.getString("cst_name"));
                    c.setPhone(rs.getString("cst_phone"));
                    c.setAddress(rs.getString("cst_address"));
                    return c;
                }
            } catch (Exception e) { error = e; }
            return null;
        }

        @Override
        protected void onPostExecute(Customer customer) {
            if (error != null) {
                Toast.makeText(PricingActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (customer != null) {
                customerNameEt.setText(customer.getName());
                customerPhoneEt.setText(customer.getPhone());
                customerAddressEt.setText(customer.getAddress());
            }
        }
    }

    private class SaveInvoiceTask extends AsyncTask<Void, Void, Integer> {
        private Exception error;

        @Override
        protected Integer doInBackground(Void... voids) {
            if (invoiceItems.isEmpty()) return null;

            Connection conn = null;
            try {
                conn = connectionHelper.conclass();
                conn.setAutoCommit(false);

                int invoiceId = getNextInvoiceId(conn); // ✅ generate the ID
                insertInvoice(conn, invoiceId);
                updateTas3eerNum(conn);

                conn.commit();
                return invoiceId;

            } catch (Exception e) {
                error = e;
                if (conn != null) try { conn.rollback(); } catch (Exception ignored) {}
                return null;
            } finally {
                if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) {}
            }
        }

        @Override
        protected void onPostExecute(Integer invoiceId) {
            if (invoiceId != null) {
                Toast.makeText(PricingActivity.this,
                        "Invoice saved successfully. ID: " + invoiceId,
                        Toast.LENGTH_LONG).show();
                finish();
            } else if (error != null) {
                Toast.makeText(PricingActivity.this,
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(PricingActivity.this,
                        "Please add products first",
                        Toast.LENGTH_LONG).show();
            }
        }
    }


    private int getNextInvoiceId(Connection conn) throws SQLException {
        String sql = "SELECT MAX(tas3eer_ID) AS next_id FROM Numbers_table WHERE stock = 'الرئيسي'";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getInt("next_id") + 1 : 1;
    }

    private void updateTas3eerNum(Connection conn) throws SQLException {
        String sql = "UPDATE Numbers_table SET tas3eer_ID = tas3eer_ID + 1 WHERE stock = 'الرئيسي'";
        PreparedStatement stmt = conn.prepareStatement(sql);
        if (stmt.executeUpdate() == 0) throw new SQLException("Failed to update tas3eer_ID");
    }

    private void insertInvoice(Connection conn, int invoiceId) throws SQLException {
        String sql = "INSERT INTO tas3eer_table (tas3eer_ID, tas3eer_date, tas3eer_cst_name, tas3eer_cst_phone, tas3eer_cst_address, tas3eer_discount, tas3eer_product_ID, tas3eer_product_name, tas3eer_product_count, tas3eer_unit_price, tas3eer_pro_stock) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (PricingInvoiceItem item : invoiceItems) {
            stmt.setInt(1, invoiceId);
            stmt.setDate(2, new java.sql.Date(new Date().getTime()));
            stmt.setString(3, customerNameEt.getText().toString());
            stmt.setString(4, customerPhoneEt.getText().toString());
            stmt.setString(5, customerAddressEt.getText().toString());
            stmt.setDouble(6, Double.parseDouble(discountEt.getText().toString()));
            stmt.setString(7, item.getProduct().getInternationalCode());
            stmt.setString(8, item.getProduct().getName());
            stmt.setInt(9, item.getQuantity());
            stmt.setDouble(10, item.getUnitPrice());
            stmt.setString(11, item.getProduct().getStock());
            stmt.addBatch();
        }
        stmt.executeBatch();
    }
}