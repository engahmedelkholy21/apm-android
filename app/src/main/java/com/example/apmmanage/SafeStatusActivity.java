package com.example.apmmanage;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SafeStatusActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private TextInputEditText fromDateEdit, toDateEdit;
    private AutoCompleteTextView userSpinner;
    private Connection connection;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private View loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_status);

        initializeViews();
        setupDatePickers();
        setupUserSpinner();
        setupSearchButton();

        executor.execute(() -> {
            connection = new ConnectionHelper().conclass();
            if (connection != null) {
                loadUsers();
                fetchData();
            } else {
                showError("فشل الاتصال بقاعدة البيانات");
            }
        });
    }

    private void initializeViews() {
        tableLayout = findViewById(R.id.tableLayout);
        fromDateEdit = findViewById(R.id.fromDateEdit);
        toDateEdit = findViewById(R.id.toDateEdit);
        userSpinner = findViewById(R.id.userSpinner);
        loadingProgress = findViewById(R.id.loadingProgress);

        fromDateEdit.setText(dateFormat.format(new Date()));
        toDateEdit.setText(dateFormat.format(new Date()));
    }

    private void setupDatePickers() {
        View.OnClickListener dateClickListener = v -> {
            TextInputEditText clickedEdit = (TextInputEditText) v;
            new DatePickerDialog(this, (view, year, month, day) -> {
                calendar.set(year, month, day);
                clickedEdit.setText(dateFormat.format(calendar.getTime()));
                fetchData();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        };

        fromDateEdit.setOnClickListener(dateClickListener);
        toDateEdit.setOnClickListener(dateClickListener);
    }

    private void setupUserSpinner() {
        userSpinner.setOnItemClickListener((parent, view, position, id) -> fetchData());
    }

    private void setupSearchButton() {
        findViewById(R.id.searchButton).setOnClickListener(v -> fetchData());
    }

    private void loadUsers() {
        try {
            String query = "SELECT user_name FROM user_previliges";
            try (PreparedStatement ps = connection.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {

                List<String> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(rs.getString("user_name"));
                }

                mainHandler.post(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line, users);
                    userSpinner.setAdapter(adapter);
                });
            }
        } catch (Exception e) {
            showError("خطأ في تحميل المستخدمين: " + e.getMessage());
        }
    }

    private void fetchData() {
        if (connection == null) return;

        loadingProgress.setVisibility(View.VISIBLE);
        clearTable();

        String fromDate = fromDateEdit.getText().toString();
        String toDate = toDateEdit.getText().toString();
        String selectedUser = userSpinner.getText().toString();

        executor.execute(() -> {
            try {
                // Get income (wared)
                String incomeQuery = "SELECT SUM(income_cost) as total FROM income_table " +
                        "WHERE income_date BETWEEN ? AND ? " +
                        (selectedUser.isEmpty() ? "" : "AND income_user = ?");

                // Get outcome (sader)
                String outcomeQuery = "SELECT SUM(outcome_cost) as total FROM outcome_table " +
                        "WHERE outcome_date BETWEEN ? AND ? " +
                        (selectedUser.isEmpty() ? "" : "AND outcome_user = ?");

                // Get profits (arba7)
                String profitsQuery = "SELECT SUM(arba7_cost) as total FROM arba7 " +
                        "WHERE arba7_date BETWEEN ? AND ? " +
                        (selectedUser.isEmpty() ? "" : "AND arba7_user = ?");

                // Get expenses (nathryat)
                String expensesQuery = "SELECT SUM(outcome_cost) as total FROM outcome_table " +
                        "WHERE outcome_date BETWEEN ? AND ? " +
                        "AND outcome_source NOT IN ('شراء','مرتجع بيع','دفعات دائنين','ادارة','شركاء') " +
                        (selectedUser.isEmpty() ? "" : "AND outcome_user = ?");

                double wared = executeSum(incomeQuery, fromDate, toDate, selectedUser);
                double sader = executeSum(outcomeQuery, fromDate, toDate, selectedUser);
                double arba7 = executeSum(profitsQuery, fromDate, toDate, selectedUser);
                double nathryat = executeSum(expensesQuery, fromDate, toDate, selectedUser);

                double safy = wared - sader;
                double safy_arba7 = arba7 - nathryat;

                mainHandler.post(() -> {
                    TableRow row = new TableRow(this);
                    row.setPadding(8, 8, 8, 8);
                    row.setBackgroundColor(0xFFF9F9F9);

                    row.addView(createCell(selectedUser.isEmpty() ? "الكل" : selectedUser));
                    row.addView(createCell(String.format(Locale.US, "%.2f", wared)));
                    row.addView(createCell(String.format(Locale.US, "%.2f", sader)));
                    row.addView(createCell(String.format(Locale.US, "%.2f", safy)));
                    row.addView(createCell(String.format(Locale.US, "%.2f", arba7)));
                    row.addView(createCell(String.format(Locale.US, "%.2f", nathryat)));
                    row.addView(createCell(String.format(Locale.US, "%.2f", safy_arba7)));

                    tableLayout.addView(row);
                    loadingProgress.setVisibility(View.GONE);
                });

            } catch (Exception e) {
                showError("خطأ في قاعدة البيانات: " + e.getMessage());
                loadingProgress.setVisibility(View.GONE);
            }
        });
    }

    private double executeSum(String query, String fromDate, String toDate, String user) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, fromDate);
            ps.setString(2, toDate);
            if (!user.isEmpty()) {
                ps.setString(3, user);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("total") : 0;
            }
        }
    }

    private TextView createCell(String text) {
        TextView cell = new TextView(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 8, 8, 8);
        cell.setLayoutParams(params);
        cell.setText(text);
        cell.setPadding(16, 16, 16, 16);
        cell.setGravity(Gravity.CENTER);
        cell.setTextColor(getResources().getColor(android.R.color.darker_gray));
        return cell;
    }

    private void clearTable() {
        int childCount = tableLayout.getChildCount();
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1);
        }
    }

    private void showError(String message) {
        mainHandler.post(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
