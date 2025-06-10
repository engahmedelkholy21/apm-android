package com.example.apmmanage;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HalekShowActivity extends AppCompatActivity {

    private static final String TAG = "HalekShowActivity";
    // Use SimpleDateFormat for consistent date formatting
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    // View Variables
    private AutoCompleteTextView nameTxt;
    private TextView totalTxt;
    private TextView stockTxt;
    private Button fromDateBtn;
    private Button toDateBtn;
    private ListView listView;

    // Data Variables
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> items = new ArrayList<>();
    private String fromDate = "";
    private String toDate = "";
    private String currentStock = "";

    // Consider Dependency Injection for ConnectionHelper in a real-world app
    private ConnectionHelper connectionHelper = new ConnectionHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halek_show);

        // Initialize Views
        initializeViews();

        // Setup Initial State
        setupInitialState();

        // Load initial data and autocomplete suggestions
        // Consider moving database operations off the main thread (e.g., using AsyncTask, Executors, or Coroutines)
        // For simplicity and adhering to constraints (no new files/major refactoring), kept on main thread.
        loadAutoComplete();
        loadData();

        // Setup Listeners
        setupListeners();
    }

    private void initializeViews() {
        nameTxt = findViewById(R.id.nameTxt);
        totalTxt = findViewById(R.id.totalTxt);
        stockTxt = findViewById(R.id.stockTxt);
        fromDateBtn = findViewById(R.id.fromDateBtn);
        toDateBtn = findViewById(R.id.toDateBtn);
        listView = findViewById(R.id.listView);
    }

    private void setupInitialState() {
        // Get stock name from GlobalData (Consider passing via Intent extras for better decoupling)
        currentStock = GlobalData.userFar3 != null ? GlobalData.userFar3 : ""; // Handle potential null
        // TODO: Extract hardcoded strings to strings.xml
        stockTxt.setText(String.format("الفرع: %s", currentStock));

        // Initialize ListView Adapter
        // TODO: For better performance and UI customization, use RecyclerView with a custom ViewHolder and item layout.
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, items); // Reverted to original system layout
        listView.setAdapter(listAdapter);

        // Initialize Dates
        Calendar calendar = Calendar.getInstance();
        fromDate = toDate = DATE_FORMAT.format(calendar.getTime());
        // TODO: Extract hardcoded strings to strings.xml
        fromDateBtn.setText(String.format("من: %s", fromDate));
        toDateBtn.setText(String.format("الى: %s", toDate));
    }

    private void setupListeners() {
        nameTxt.setOnItemClickListener((parent, view, position, id) -> loadData());
        // Use lambda expressions for conciseness
        fromDateBtn.setOnClickListener(v -> showDatePicker(date -> {
            fromDate = date;
            // TODO: Extract hardcoded strings to strings.xml
            fromDateBtn.setText(String.format("من: %s", date));
            loadData();
        }));

        toDateBtn.setOnClickListener(v -> showDatePicker(date -> {
            toDate = date;
            // TODO: Extract hardcoded strings to strings.xml
            toDateBtn.setText(String.format("الى: %s", date));
            loadData();
        }));
    }

    private void loadData() {
        items.clear();
        double total = 0;
        // TODO: Add a loading indicator (e.g., ProgressBar) while data is fetched.

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = connectionHelper.conclass(); // Use the instance variable
            if (con == null) {
                // TODO: Extract hardcoded strings to strings.xml
                showErrorToast("فشل الاتصال بقاعدة البيانات");
                return;
            }

            String filter = nameTxt.getText().toString().trim();
            String baseQuery = "SELECT halek_material_name, halek_material_count, halek_material_date, halek_stock FROM halek_table WHERE halek_material_date BETWEEN ? AND ? AND halek_stock = ?";
            String query;

            if (filter.isEmpty()) {
                query = baseQuery + " ORDER BY halek_material_date DESC"; // Add ordering
                pstmt = con.prepareStatement(query);
                pstmt.setString(1, fromDate);
                pstmt.setString(2, toDate);
                pstmt.setString(3, currentStock);
            } else {
                query = baseQuery + " AND halek_material_name LIKE ? ORDER BY halek_material_date DESC"; // Add ordering
                pstmt = con.prepareStatement(query);
                pstmt.setString(1, fromDate);
                pstmt.setString(2, toDate);
                pstmt.setString(3, currentStock);
                pstmt.setString(4, "%" + filter + "%"); // Use wildcard for LIKE
            }

            rs = pstmt.executeQuery();

            // TODO: The header should ideally be part of the layout (e.g., using a separate TextView or included in RecyclerView header), not the data list.
            // items.add(String.format("%-15s | %-10s | %-12s | %-10s", "الاسم", "الكمية", "التاريخ", "الفرع")); // Header - Removed for cleaner data list

            while (rs.next()) {
                String name = rs.getString("halek_material_name");
                double count = rs.getDouble("halek_material_count");
                String date = rs.getString("halek_material_date");
                String st = rs.getString("halek_stock");

                // Format data to simulate table columns using fixed-width padding.
                // Adjust widths as needed for best visual alignment with the header.
                // NOTE: This relies on the font used by simple_list_item_1 being somewhat consistent.
                String row = String.format(Locale.US, "%-20s %10.2f   %-15s %-15s",
                        name.length() > 20 ? name.substring(0, 17) + "..." : name, // Truncate long names
                        count,
                        date,
                        st);
                items.add(row);
                total += count;
            }

            listAdapter.notifyDataSetChanged();
            // TODO: Extract hardcoded strings to strings.xml
            totalTxt.setText(String.format(Locale.US, "%.2f", total)); // Format total

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error loading data: " + e.getMessage(), e);
            // TODO: Extract hardcoded strings to strings.xml
            showErrorToast("خطأ في تحميل البيانات: " + e.getMessage());
        } catch (Exception e) {
            // Catch other potential exceptions
            Log.e(TAG, "Error loading data: " + e.getMessage(), e);
            // TODO: Extract hardcoded strings to strings.xml
            showErrorToast("حدث خطأ غير متوقع: " + e.getMessage());
        } finally {
            // Ensure resources are closed
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                Log.e(TAG, "Error closing database resources: " + e.getMessage(), e);
            }
            // TODO: Hide loading indicator here.
        }
    }

    private void loadAutoComplete() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<String> names = new ArrayList<>();

        try {
            con = connectionHelper.conclass();
            if (con == null) {
                // Silently fail or log, as autocomplete is less critical than main data
                Log.w(TAG, "Database connection failed for autocomplete.");
                return;
            }
            stmt = con.createStatement();
            // Query distinct names for the current stock only for relevance
            String query = "SELECT DISTINCT halek_material_name FROM halek_table WHERE halek_stock = '" + currentStock + "'";
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                names.add(rs.getString(1));
            }

            ArrayAdapter<String> autoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
            nameTxt.setAdapter(autoAdapter);
            nameTxt.setThreshold(1); // Show suggestions after 1 character

        } catch (SQLException e) {
            Log.e(TAG, "SQL Error loading autocomplete: " + e.getMessage(), e);
            // Optionally inform user, but might be too noisy
        } catch (Exception e) {
            Log.e(TAG, "Error loading autocomplete: " + e.getMessage(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                Log.e(TAG, "Error closing autocomplete resources: " + e.getMessage(), e);
            }
        }
    }

    private void showDatePicker(DatePickedCallback callback) {
        Calendar calendar = Calendar.getInstance();
        // Try parsing the current date string to set the initial date in the picker
        try {
            // Determine which button was clicked to set the initial date
            String currentDateString = (String) (callback == fromDateCallback ? fromDateBtn.getText() : toDateBtn.getText());
            // Extract date part if formatted like "من: YYYY-MM-DD"
            String datePart = currentDateString.substring(currentDateString.indexOf(":") + 1).trim();
            Date currentParsedDate = DATE_FORMAT.parse(datePart);
            if (currentParsedDate != null) {
                calendar.setTime(currentParsedDate);
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not parse date for DatePicker initial value: " + e.getMessage());
            // Keep calendar as Calendar.getInstance() if parsing fails
        }

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, day);
                    String dateStr = DATE_FORMAT.format(selectedCalendar.getTime());
                    callback.onDatePicked(dateStr);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    // Define callbacks as instance variables for cleaner referencing in showDatePicker
    private final DatePickedCallback fromDateCallback = date -> {
        fromDate = date;
        fromDateBtn.setText(String.format("من: %s", date));
        loadData();
    };

    private final DatePickedCallback toDateCallback = date -> {
        toDate = date;
        toDateBtn.setText(String.format("الى: %s", date));
        loadData();
    };


    // Helper to show Toast messages consistently
    private void showErrorToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Functional interface for callback
    interface DatePickedCallback {
        void onDatePicked(String date);
    }
}
