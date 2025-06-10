package com.example.apmmanage;

import android.content.Intent;
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

public class SellActivity extends AppCompatActivity {

    private EditText productSearchEditText;
    private EditText quantityEditText;
    private EditText customerNameEditText;
    private EditText customerPhoneEditText;
    private EditText customerAddressEditText;
    private TextView invoiceNumberTextView;
    private TextView dateTextView;
    private TextView subtotalTextView;
    private TextView taxTotalTextView;
    private TextView totalPriceTextView;
    private EditText discountEditText;
    private EditText paidAmountEditText;
    private TextView balanceTextView;
    private ListView productsListView;
    private Button addProductButton;
    private Button barcodeButton;
    private Button saveInvoiceButton;
    private Button clearButton;
    private Button wholesaleButton;
    private RadioButton cashRadioButton;
    private RadioButton creditRadioButton;

    private ArrayList<ProductItem> productItems;
    private ProductListAdapter adapter;
    private ConnectionHelper connectionHelper;
    private DecimalFormat decimalFormat;

    private static final int BARCODE_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        // Initialize connection helper and decimal format
        connectionHelper = new ConnectionHelper();
        decimalFormat = new DecimalFormat("#,##0.00");

        // Initialize UI components
        initializeViews();

        // Initialize product list and adapter
        productItems = new ArrayList<>();
        adapter = new ProductListAdapter(this, productItems);
        productsListView.setAdapter(adapter);

        // Set current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateTextView.setText(dateFormat.format(new Date()));

        // Set up button click listeners
        setupClickListeners();

        // Generate new invoice number
        generateInvoiceNumber();
    }

    private void initializeViews() {
        productSearchEditText = findViewById(R.id.productSearchEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        customerNameEditText = findViewById(R.id.customerNameEditText);
        customerPhoneEditText = findViewById(R.id.customerPhoneEditText);
        customerAddressEditText = findViewById(R.id.customerAddressEditText);
        invoiceNumberTextView = findViewById(R.id.invoiceNumberTextView);
        dateTextView = findViewById(R.id.dateTextView);
        subtotalTextView = findViewById(R.id.subtotalTextView);
        taxTotalTextView = findViewById(R.id.taxTotalTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        discountEditText = findViewById(R.id.discountEditText);
        paidAmountEditText = findViewById(R.id.paidAmountEditText);
        balanceTextView = findViewById(R.id.balanceTextView);
        productsListView = findViewById(R.id.productsListView);
        addProductButton = findViewById(R.id.addProductButton);
        barcodeButton = findViewById(R.id.barcodeButton);
        saveInvoiceButton = findViewById(R.id.saveInvoiceButton);
        clearButton = findViewById(R.id.clearButton);
        wholesaleButton = findViewById(R.id.wholesaleButton);
        cashRadioButton = findViewById(R.id.cashRadioButton);
        creditRadioButton = findViewById(R.id.creditRadioButton);
    }

    private void setupClickListeners() {
        barcodeButton.setOnClickListener(v -> {
            Intent intent = new Intent(SellActivity.this, Barcode_activity.class);
            startActivityForResult(intent, BARCODE_REQUEST_CODE);
        });

        addProductButton.setOnClickListener(v -> {
            addProductToList();
        });

        saveInvoiceButton.setOnClickListener(v -> {
            saveInvoice();
        });

        clearButton.setOnClickListener(v -> {
            clearInvoice();
        });

        wholesaleButton.setOnClickListener(v -> {
            toggleWholesale();
        });
    }

    private void addProductToList() {
        String productSearch = productSearchEditText.getText().toString();
        if (productSearch.isEmpty()) {
            Toast.makeText(this, "Please enter a product", Toast.LENGTH_SHORT).show();
            return;
        }

        Connection connection = connectionHelper.conclass();
        if (connection != null) {
            try {
                String query = "SELECT * FROM products_table WHERE pro_name LIKE ? OR pro_int_code = ? AND pro_count > 0";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, "%" + productSearch + "%");
                ps.setString(2, productSearch);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String productCode = rs.getString("pro_int_code");
                    String productName = rs.getString("pro_name");
                    double price = rs.getDouble("pro_bee3");
                    addProductToInvoice(productCode, productName, price);
                } else {
                    Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                }

                rs.close();
                ps.close();
                connection.close();
            } catch (Exception e) {
                Toast.makeText(this, "Error searching product: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addProductToInvoice(String code, String name, double price) {
        try {
            double quantity = Double.parseDouble(quantityEditText.getText().toString());
            ProductItem item = new ProductItem(code, name, quantity, price);
            productItems.add(item);
            adapter.notifyDataSetChanged();
            calculateTotals();

            // Clear input fields
            productSearchEditText.setText("");
            quantityEditText.setText("1");
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateTotals() {
        double subtotal = 0;
        double tax = 0;

        for (ProductItem item : productItems) {
            subtotal += item.getQuantity() * item.getPrice();
            tax += (item.getQuantity() * item.getPrice()) * 0.15; // 15% tax example
        }

        double total = subtotal + tax;

        subtotalTextView.setText(decimalFormat.format(subtotal));
        taxTotalTextView.setText(decimalFormat.format(tax));
        totalPriceTextView.setText(decimalFormat.format(total));
    }

    private void saveInvoice() {
        // Implement save invoice logic here
        Toast.makeText(this, "Saving invoice...", Toast.LENGTH_SHORT).show();
    }

    private void clearInvoice() {
        productItems.clear();
        adapter.notifyDataSetChanged();
        calculateTotals();
        customerNameEditText.setText("");
        customerPhoneEditText.setText("");
        customerAddressEditText.setText("");
        discountEditText.setText("0");
        paidAmountEditText.setText("0");
    }

    private void toggleWholesale() {
        if (wholesaleButton.getText().equals("قطاعي")) {
            wholesaleButton.setText("جملة");
        } else {
            wholesaleButton.setText("قطاعي");
        }
        // Recalculate prices based on wholesale status
        calculateTotals();
    }

    private void generateInvoiceNumber() {
        Connection connection = connectionHelper.conclass();
        if (connection != null) {
            try {
                String query = "SELECT MAX(sales_id) + 1 AS next_id FROM sales_table";
                PreparedStatement ps = connection.prepareStatement(query);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int nextId = rs.getInt("next_id");
                    invoiceNumberTextView.setText(String.valueOf(nextId));
                }

                rs.close();
                ps.close();
                connection.close();
            } catch (Exception e) {
                Toast.makeText(this, "Error generating invoice number",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BARCODE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String barcode = data.getStringExtra("barcode");
            productSearchEditText.setText(barcode);
            searchProduct(barcode);
        }
    }

    private void searchProduct(String barcode) {
        Connection connection = connectionHelper.conclass();
        if (connection != null) {
            try {
                String query = "SELECT * FROM products_table WHERE pro_int_code = ? AND pro_count > 0";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, barcode);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String productName = rs.getString("pro_name");
                    double price = rs.getDouble("pro_bee3");
                    addProductToInvoice(barcode, productName, price);
                } else {
                    Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                }

                rs.close();
                ps.close();
                connection.close();
            } catch (Exception e) {
                Toast.makeText(this, "Error searching product: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class ProductItem {
        private String code;
        private String name;
        private double quantity;
        private double price;

        public ProductItem(String code, String name, double quantity, double price) {
            this.code = code;
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        // Getters
        public String getCode() { return code; }
        public String getName() { return name; }
        public double getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getTotal() { return quantity * price; }
    }
}
