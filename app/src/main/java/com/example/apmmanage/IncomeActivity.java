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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IncomeActivity extends AppCompatActivity {
    private TextInputEditText fromDateTxt, toDateTxt, projectTxt;
    private AutoCompleteTextView branchSpinner;
    private TableLayout tableLayout;
    private TextView totalText;
    private Connection connection;
    private double totalIncome;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        initializeViews();
        setupDatePickers();
        setupSearchButton();
        loadBranches();

        executor.execute(() -> {
            connection = new ConnectionHelper().conclass();
            if (connection == null) {
                mainHandler.post(() ->
                        Toast.makeText(this, "فشل الاتصال بقاعدة البيانات", Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void initializeViews() {
        fromDateTxt = findViewById(R.id.fromDateTxt);
        toDateTxt = findViewById(R.id.toDateTxt);
        projectTxt = findViewById(R.id.projectTxt);
        branchSpinner = findViewById(R.id.branchSpinner);
        tableLayout = findViewById(R.id.tableLayout);
        totalText = findViewById(R.id.totalText);
    }

    private void loadBranches() {
        executor.execute(() -> {
            try {
                Connection conn = new ConnectionHelper().conclass();
                if (conn != null) {
                    List<String> branches = new ArrayList<>();
                    String query = "SELECT stock_name FROM stock_table ORDER BY stock_name";
                    try (PreparedStatement ps = conn.prepareStatement(query);
                         ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            branches.add(rs.getString("stock_name"));
                        }
                    }
                    conn.close();

                    mainHandler.post(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                branches
                        );
                        branchSpinner.setAdapter(adapter);
                        if (!branches.isEmpty()) {
                            branchSpinner.setText(branches.get(0), false);
                        }
                    });
                }
            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "خطأ في تحميل الفروع: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
                e.printStackTrace();
            }
        });
    }

    private void setupDatePickers() {
        View.OnClickListener dateClickListener = v -> {
            TextInputEditText clickedEdit = (TextInputEditText) v;
            new DatePickerDialog(this, (view, year, month, day) -> {
                calendar.set(year, month, day);
                clickedEdit.setText(dateFormat.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        };

        fromDateTxt.setOnClickListener(dateClickListener);
        toDateTxt.setOnClickListener(dateClickListener);
    }

    private void setupSearchButton() {
        findViewById(R.id.searchBtn).setOnClickListener(v -> fetchData());
    }

    private void fetchData() {
        if (connection == null) {
            Toast.makeText(this, "لا يوجد اتصال بقاعدة البيانات", Toast.LENGTH_LONG).show();
            return;
        }

        clearTable();
        totalIncome = 0;

        String fromDate = fromDateTxt.getText().toString();
        String toDate = toDateTxt.getText().toString();
        String project = projectTxt.getText().toString().trim();
        String branch = branchSpinner.getText().toString();

        executor.execute(() -> {
            try {
                String query;
                boolean hasData = false;

                if (project.isEmpty()) {
                    if (fromDate.equals(toDate)) {
                        query = "SELECT * FROM income_table WHERE income_date = ? AND income_far3 = ?";
                        hasData = executeQuery(query, new String[]{fromDate, branch});
                    } else {
                        query = "SELECT * FROM income_table WHERE income_date BETWEEN ? AND ? AND income_far3 = ?";
                        hasData = executeQuery(query, new String[]{fromDate, toDate, branch});
                    }
                } else {
                    query = "SELECT * FROM income_table WHERE income_date BETWEEN ? AND ? AND income_esm_elmashroo3 = ? AND income_far3 = ?";
                    hasData = executeQuery(query, new String[]{fromDate, toDate, project, branch});

                    if (!hasData) {
                        query = "SELECT * FROM income_table WHERE income_date BETWEEN ? AND ? AND income_user = ? AND income_far3 = ?";
                        hasData = executeQuery(query, new String[]{fromDate, toDate, project, branch});
                        if (hasData) {
                            mainHandler.post(() ->
                                    Toast.makeText(this, "عرض حسب المستخدم", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                }

                if (!hasData) {
                    mainHandler.post(() ->
                            Toast.makeText(this, "لا توجد بيانات", Toast.LENGTH_LONG).show()
                    );
                }

                final double finalTotal = totalIncome;
                mainHandler.post(() ->
                        totalText.setText(String.format(Locale.US, "%.2f", finalTotal))
                );

            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "خطأ في قاعدة البيانات: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
                e.printStackTrace();
            }
        });
    }

    private boolean executeQuery(String query, String[] params) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return processResultSet(rs);
            }
        }
    }

    private boolean processResultSet(ResultSet rs) throws Exception {
        boolean hasData = false;
        int rowIndex = 0;

        while (rs.next()) {
            hasData = true;
            TableRow row = createTableRow(rs, rowIndex++);
            mainHandler.post(() -> tableLayout.addView(row));
            totalIncome += rs.getDouble("income_cost");
        }

        return hasData;
    }

    private TableRow createTableRow(ResultSet rs, int index) throws Exception {
        TableRow row = new TableRow(this);
        row.setPadding(8, 8, 8, 8);
        row.setBackgroundColor(index % 2 == 0 ? 0xFFF9F9F9 : 0xFFFFFFFF);

        row.addView(createCell(String.valueOf(rs.getInt("income_id")), 70));
        row.addView(createCell(rs.getString("income_date"), 0));
        row.addView(createCell(rs.getString("income_details"), 350));

        TextView costCell = createCell(String.format(Locale.US, "%.2f", rs.getDouble("income_cost")), 0);
        costCell.setBackgroundColor(0xFFE8F5E9);
        row.addView(costCell);

        row.addView(createCell(String.valueOf(rs.getInt("income_fator_id")), 0));
        row.addView(createCell(rs.getString("income_source"), 0));
        row.addView(createCell(rs.getString("income_user"), 90));
        row.addView(createCell(rs.getString("income_far3"), 120));
        row.addView(createCell(rs.getString("income_notes"), 130));

        return row;
    }

    private TextView createCell(String text, int widthDp) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setPadding(16, 16, 16, 16);
        cell.setGravity(Gravity.START);
        cell.setTextColor(getResources().getColor(android.R.color.darker_gray));

        if (widthDp > 0) {
            float scale = getResources().getDisplayMetrics().density;
            cell.setWidth((int)(widthDp * scale));
        }

        return cell;
    }

    private void clearTable() {
        int childCount = tableLayout.getChildCount();
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1);
        }
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