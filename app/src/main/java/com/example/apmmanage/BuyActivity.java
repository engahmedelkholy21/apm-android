package com.example.apmmanage;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BuyActivity extends AppCompatActivity {

    private EditText supplierNameEditText, supplierPhoneEditText, supplierAddressEditText;
    private EditText productSearchEditText, quantityEditText, unitPriceEditText;
    private TextView invoiceNumberTextView, dateTextView, subtotalTextView, balanceTextView;
    private EditText discountEditText, paidAmountEditText;
    private ListView productsListView;
    private Button addProductButton, saveInvoiceButton;

    private ArrayList<PurchaseItem> items;
    private PurchaseListAdapter adapter;
    private ConnectionHelper connectionHelper;
    private final DecimalFormat format = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        connectionHelper = new ConnectionHelper();
        initViews();
        items = new ArrayList<>();
        adapter = new PurchaseListAdapter(this, items);
        productsListView.setAdapter(adapter);

        dateTextView.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        generateInvoiceNumber();

        addProductButton.setOnClickListener(v -> addProduct());
        saveInvoiceButton.setOnClickListener(v -> saveInvoice());
    }

    private void initViews() {
        supplierNameEditText = findViewById(R.id.supplierNameEditText);
        supplierPhoneEditText = findViewById(R.id.supplierPhoneEditText);
        supplierAddressEditText = findViewById(R.id.supplierAddressEditText);
        productSearchEditText = findViewById(R.id.productSearchEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        unitPriceEditText = findViewById(R.id/unitPriceEditText);
        invoiceNumberTextView = findViewById(R.id.invoiceNumberTextView);
        dateTextView = findViewById(R.id.dateTextView);
        subtotalTextView = findViewById(R.id.subtotalTextView);
        balanceTextView = findViewById(R.id.balanceTextView);
        discountEditText = findViewById(R.id.discountEditText);
        paidAmountEditText = findViewById(R.id.paidAmountEditText);
        productsListView = findViewById(R.id.productsListView);
        addProductButton = findViewById(R.id.addProductButton);
        saveInvoiceButton = findViewById(R.id.saveInvoiceButton);
    }

    private void generateInvoiceNumber() {
        Connection con = connectionHelper.conclass();
        if (con != null) {
            try (PreparedStatement ps = con.prepareStatement("SELECT ISNULL(MAX(Purchases_id),0)+1 AS next_id FROM Purchases_table")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    invoiceNumberTextView.setText(String.valueOf(rs.getInt("next_id")));
                }
            } catch (Exception ignored) { }
        }
    }

    private void addProduct() {
        String search = productSearchEditText.getText().toString().trim();
        if (search.isEmpty()) {
            Toast.makeText(this, "ادخل اسم المنتج", Toast.LENGTH_SHORT).show();
            return;
        }
        Connection con = connectionHelper.conclass();
        if (con != null) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT pro_int_code, pro_name, pro_cost_price FROM products_table WHERE pro_name LIKE ? OR pro_int_code = ?")) {
                ps.setString(1, "%" + search + "%");
                ps.setString(2, search);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String code = rs.getString("pro_int_code");
                    String name = rs.getString("pro_name");
                    double price = rs.getDouble("pro_cost_price");
                    try {
                        double qty = Double.parseDouble(quantityEditText.getText().toString());
                        double unitPrice = unitPriceEditText.getText().toString().isEmpty() ? price : Double.parseDouble(unitPriceEditText.getText().toString());
                        items.add(new PurchaseItem(code, name, qty, unitPrice));
                        adapter.notifyDataSetChanged();
                        calculateTotals();
                        productSearchEditText.setText("");
                        quantityEditText.setText("1");
                        unitPriceEditText.setText("");
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "كمية غير صالحة", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "لم يتم العثور على المنتج", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void calculateTotals() {
        double subtotal = 0;
        for (PurchaseItem item : items) {
            subtotal += item.getTotal();
        }
        subtotalTextView.setText(format.format(subtotal));
        updateBalance();
    }

    private void updateBalance() {
        double subtotal = 0;
        try { subtotal = Double.parseDouble(subtotalTextView.getText().toString().replace(",", "")); } catch (Exception ignored) {}
        double discount = discountEditText.getText().toString().isEmpty() ? 0 : Double.parseDouble(discountEditText.getText().toString());
        double paid = paidAmountEditText.getText().toString().isEmpty() ? 0 : Double.parseDouble(paidAmountEditText.getText().toString());
        double remaining = subtotal - discount - paid;
        balanceTextView.setText(format.format(remaining));
    }

    private void saveInvoice() {
        if (items.isEmpty()) {
            Toast.makeText(this, "لا توجد منتجات", Toast.LENGTH_SHORT).show();
            return;
        }
        Connection con = connectionHelper.conclass();
        if (con != null) {
            try {
                con.setAutoCommit(false);
                String insertSql = "INSERT INTO Purchases_table (Purchases_id, Purchases_date, Purchases_mwared_name, Purchases_mwared_phone, Purchases_mwared_address, Purchases_product_ID, Purchases_product_name, Purchases_product_count, Purchases_unit_price, sales_notes, discount_nesba, stock, price_before_discount, sales_price_for_sell) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement ps = con.prepareStatement(insertSql);
                for (PurchaseItem item : items) {
                    ps.setInt(1, Integer.parseInt(invoiceNumberTextView.getText().toString()));
                    ps.setString(2, dateTextView.getText().toString());
                    ps.setString(3, supplierNameEditText.getText().toString());
                    ps.setString(4, supplierPhoneEditText.getText().toString());
                    ps.setString(5, supplierAddressEditText.getText().toString());
                    ps.setString(6, item.getCode());
                    ps.setString(7, item.getName());
                    ps.setDouble(8, item.getQuantity());
                    ps.setDouble(9, item.getPrice());
                    ps.setString(10, "");
                    ps.setDouble(11, discountEditText.getText().toString().isEmpty() ? 0 : Double.parseDouble(discountEditText.getText().toString()));
                    ps.setString(12, "الرئيسي");
                    ps.setDouble(13, item.getPrice());
                    ps.setDouble(14, item.getPrice());
                    ps.addBatch();
                }
                ps.executeBatch();

                String updateSql = "UPDATE products_table SET pro_count = pro_count + ? WHERE pro_int_code = ? AND pro_stock = ?";
                PreparedStatement ps2 = con.prepareStatement(updateSql);
                for (PurchaseItem item : items) {
                    ps2.setDouble(1, item.getQuantity());
                    ps2.setString(2, item.getCode());
                    ps2.setString(3, "الرئيسي");
                    ps2.addBatch();
                }
                ps2.executeBatch();

                con.commit();
                ps.close();
                ps2.close();
                con.close();
                Toast.makeText(this, "تم حفظ الفاتورة", Toast.LENGTH_LONG).show();
                finish();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static class PurchaseItem {
        private final String code;
        private final String name;
        private final double quantity;
        private final double price;

        public PurchaseItem(String code, String name, double quantity, double price) {
            this.code = code;
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public double getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getTotal() { return quantity * price; }
    }
}
