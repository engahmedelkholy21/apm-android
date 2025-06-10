package com.example.apmmanage;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Color;


public class Barcode_activity extends AppCompatActivity {

    private EditText editTextProductName;
    private Button gard_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_cativity);

      //  editTextProductName = findViewById(R.id.editTextProductName);

        try {
            // Initialize the barcode scanner
            IntentIntegrator integrator = new IntentIntegrator(Barcode_activity.this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("Scan a barcode");
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(Barcode_activity.this, "Error initiating scan", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TextView pro_int_code_txt = findViewById(R.id.pro_int_code_txt);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Barcode scanned successfully
                String scannedBarcode = result.getContents();

                // Use ConnectionHelper to retrieve data from the database
                ConnectionHelper connectionHelper = new ConnectionHelper();
                Connection connection = connectionHelper.conclass();

                if (connection != null) {
                    try {

                        // Execute your specific SQL query to retrieve data based on the scanned barcode
                        String product_barcode = scannedBarcode;
                        String query = "SELECT * FROM products_table WHERE pro_count > 0 AND pro_int_code = '" + product_barcode + "'";
                        java.sql.Statement stmt = connection.createStatement();
                        java.sql.ResultSet rs = stmt.executeQuery(query);

                        // Process the result set and get values from the first row
                        if (rs.next()) {
                            String pro_name = rs.getString("pro_name");
                            String pro_bee3 = rs.getString("pro_bee3");
                            String gard_status = rs.getString("gard_status");
                            String pro_far3 = rs.getString("pro_stock");
                            String pro_int_code = rs.getString("pro_int_code");

                            int pro_count = rs.getInt("pro_count");

                            // Now you have the values, you can use them as needed
                            // For example, if you have a TextView with id "resultTextView":
                            // TextView resultTextView = findViewById(R.id.resultTextView);
                            // resultTextView.setText("pro_name: " + pro_name + "\npro_bee3: " + pro_bee3 + "\npro_count: " + pro_count);

                            // Print the values as an example
                            Log.d("Database Result", "pro_name: " + pro_name + ", pro_bee3: " + pro_bee3 + ", pro_count: " + pro_count);

                            gard_btn = findViewById(R.id.gard_btn);


                            TextView pro_name_txt = findViewById(R.id.pro_name_txt);
                            TextView pro_bee3_txt = findViewById(R.id.pro_bee3_txt);
                            TextView pro_count_txt = findViewById(R.id.pro_count_txt);
                            TextView pro_gard_status_txt = findViewById(R.id.pro_gard_status_txt);
                            TextView pro_far3_txt = findViewById(R.id.pro_far3_txt);


                            if (gard_status.equals("")) {
                                pro_gard_status_txt.setTextColor(Color.RED);
                                pro_gard_status_txt.setText("لم يتم الجرد");
                                gard_btn.setBackgroundColor(Color.GREEN);
                                gard_btn.setVisibility(View.VISIBLE);
                            } else {
                                pro_gard_status_txt.setText(gard_status);
                                pro_gard_status_txt.setTextColor(Color.GREEN);
                                gard_btn.setVisibility(View.GONE);
                            }

                            // Update the UI with the scanned product details
                            pro_name_txt.setText(pro_name);
                            pro_bee3_txt.setText("سعر البيع " + pro_bee3);
                            pro_count_txt.setText("الكمية " + pro_count);
                            pro_far3_txt.setText( pro_far3);
                            pro_int_code_txt.setText( pro_int_code);
                        } else {
                            // Handle the case where no matching row is found
                            Toast.makeText(this, "No data found for the scanned barcode", Toast.LENGTH_SHORT).show();
                        }

                        // Don't forget to close the result set and statement when done
                        rs.close();
                        stmt.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error executing the query", Toast.LENGTH_SHORT).show();
                    } finally {
                        // Don't forget to close the connection when done
                        try {
                            connection.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(this, "Error connecting to the database", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Barcode scanning canceled
                Toast.makeText(this, "Scan canceled", Toast.LENGTH_SHORT).show();
            }
        }

        gard_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use ConnectionHelper to update data in the database
                ConnectionHelper connectionHelper = new ConnectionHelper();
                Connection connection = connectionHelper.conclass();

                if (connection != null) {
                    try {
                        // Execute your update SQL query
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String currentDate = sdf.format(new Date());
                        String updateQuery = "UPDATE products_table SET gard_status = 'تم الجرد', last_gard_date = '" + currentDate + "' WHERE pro_int_code = '" + pro_int_code_txt.getText() + "'";
                        java.sql.Statement updateStmt = connection.createStatement();
                        int rowsAffected = updateStmt.executeUpdate(updateQuery);

                        // Check if the update was successful
                        if (rowsAffected > 0) {
                            Toast.makeText(Barcode_activity.this, "Database updated successfully", Toast.LENGTH_SHORT).show();

                            try
                            {
                                String query = "SELECT * FROM products_table WHERE pro_count > 0 AND pro_int_code = '" + pro_int_code_txt.getText() + "'";
                                java.sql.Statement stmt = connection.createStatement();
                                java.sql.ResultSet rs = stmt.executeQuery(query);

                                // Process the result set and get values from the first row
                                if (rs.next()) {
                                    String pro_name = rs.getString("pro_name");
                                    String pro_bee3 = rs.getString("pro_bee3");
                                    String gard_status = rs.getString("gard_status");
                                    String pro_far3 = rs.getString("pro_stock");
                                    String pro_int_code = rs.getString("pro_int_code");

                                    int pro_count = rs.getInt("pro_count");

                                    // Now you have the values, you can use them as needed
                                    // For example, if you have a TextView with id "resultTextView":
                                    // TextView resultTextView = findViewById(R.id.resultTextView);
                                    // resultTextView.setText("pro_name: " + pro_name + "\npro_bee3: " + pro_bee3 + "\npro_count: " + pro_count);

                                    // Print the values as an example
                                    Log.d("Database Result", "pro_name: " + pro_name + ", pro_bee3: " + pro_bee3 + ", pro_count: " + pro_count);

                                    gard_btn = findViewById(R.id.gard_btn);


                                    TextView pro_name_txt = findViewById(R.id.pro_name_txt);
                                    TextView pro_bee3_txt = findViewById(R.id.pro_bee3_txt);
                                    TextView pro_count_txt = findViewById(R.id.pro_count_txt);
                                    TextView pro_gard_status_txt = findViewById(R.id.pro_gard_status_txt);
                                    TextView pro_far3_txt = findViewById(R.id.pro_far3_txt);


                                    if (gard_status.equals("")) {
                                        pro_gard_status_txt.setTextColor(Color.RED);
                                        pro_gard_status_txt.setText("لم يتم الجرد");
                                        gard_btn.setVisibility(View.VISIBLE);
                                    } else {
                                        pro_gard_status_txt.setText(gard_status);
                                        pro_gard_status_txt.setTextColor(Color.GREEN);
                                        gard_btn.setVisibility(View.GONE);
                                    }

                                    // Update the UI with the scanned product details
                                    pro_name_txt.setText(pro_name);
                                    pro_bee3_txt.setText("سعر البيع " + pro_bee3);
                                    pro_count_txt.setText("الكمية " + pro_count);
                                    pro_far3_txt.setText( pro_far3);
                                    pro_int_code_txt.setText( pro_int_code);
                                }

                            }
                            catch (Exception e) {}

                        } else {
                            Toast.makeText(Barcode_activity.this, "No rows updated", Toast.LENGTH_SHORT).show();
                        }

                        // Don't forget to close the statement when done
                        updateStmt.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(Barcode_activity.this, "Error updating database", Toast.LENGTH_SHORT).show();
                    } finally {
                        // Don't forget to close the connection when done
                        try {
                            connection.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(Barcode_activity.this, "Error connecting to the database", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
