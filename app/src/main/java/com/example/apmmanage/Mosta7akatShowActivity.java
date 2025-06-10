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

public class Mosta7akatShowActivity extends AppCompatActivity {
    private AutoCompleteTextView cstNameInput;
    private TextInputEditText fromDateEdit, toDateEdit;
    private TableLayout tableLayout;
    private TextView totalDueText, totalPaidText, totalRemainingText;
    private View loadingProgress;
    private Connection connection;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mosta7akat_show);

        initializeViews();
        setupDatePickers();
        setupExportButton();

        String cstName = getIntent().getStringExtra("cst_name");
        if (cstName != null) {
            cstNameInput.setText(cstName);
        }

        executor.execute(() -> {
            connection = new ConnectionHelper().conclass();
            if (connection != null) {
                setupCustomerAutoComplete();
                mainHandler.post(this::fetchData);
            } else {
                mainHandler.post(() ->
                        Toast.makeText(this, "فشل الاتصال بقاعدة البيانات", Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void initializeViews() {
        cstNameInput = findViewById(R.id.cstNameInput);
        fromDateEdit = findViewById(R.id.fromDateEdit);
        toDateEdit = findViewById(R.id.toDateEdit);
        tableLayout = findViewById(R.id.tableLayout);
        totalDueText = findViewById(R.id.totalDueText);
        totalPaidText = findViewById(R.id.totalPaidText);
        totalRemainingText = findViewById(R.id.totalRemainingText);
        loadingProgress = findViewById(R.id.loadingProgress);

        String currentDate = dateFormat.format(new Date());
        fromDateEdit.setText(currentDate);
        toDateEdit.setText(currentDate);

        cstNameInput.setOnItemClickListener((parent, view, position, id) -> fetchData());
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

    private void setupExportButton() {
        findViewById(R.id.exportButton).setOnClickListener(v -> exportToPdf());
    }

    private void setupCustomerAutoComplete() {
        try {
            String query = "SELECT DISTINCT mosta7akat_cst_name FROM mosta7akat_table ORDER BY mosta7akat_cst_name";
            try (PreparedStatement ps = connection.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {

                List<String> names = new ArrayList<>();
                while (rs.next()) {
                    names.add(rs.getString(1));
                }

                mainHandler.post(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line, names);
                    cstNameInput.setAdapter(adapter);
                    cstNameInput.setThreshold(1);
                });
            }
        } catch (Exception e) {
            mainHandler.post(() ->
                    Toast.makeText(this, "خطأ في تحميل قائمة العملاء: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
    }

    private void fetchData() {
        loadingProgress.setVisibility(View.VISIBLE);
        clearTable();

        executor.execute(() -> {
            try {
                String query = "SELECT * FROM mosta7akat_table " +
                        "WHERE mosta7akat_cst_name = ? " +
                        "AND mosta7akat_date BETWEEN ? AND ? " +
                        "ORDER BY mosta7akat_date DESC";

                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, cstNameInput.getText().toString());
                    ps.setString(2, fromDateEdit.getText().toString());
                    ps.setString(3, toDateEdit.getText().toString());

                    try (ResultSet rs = ps.executeQuery()) {
                        processResultSet(rs);
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> {
                    Toast.makeText(this, "خطأ في قاعدة البيانات: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loadingProgress.setVisibility(View.GONE);
                });
                e.printStackTrace();
            }
        });
    }

    private void processResultSet(ResultSet rs) throws Exception {
        List<TableRow> rows = new ArrayList<>();
        double totalDue = 0, totalPaid = 0, totalRemaining = 0;
        int rowIndex = 0;

        while (rs.next()) {
            TableRow row = createTableRow(rs, rowIndex++);
            rows.add(row);

            totalDue += rs.getDouble("mosta7akat_matloob_after");
            totalPaid += rs.getDouble("mosta7akat_dof3ah_amount");
            totalRemaining += rs.getDouble("mosta7akat_matloob_after");
        }

        final double finalTotalDue = totalDue;
        final double finalTotalPaid = totalPaid;
        final double finalTotalRemaining = totalRemaining;

        mainHandler.post(() -> {
            for (TableRow row : rows) {
                tableLayout.addView(row);
            }

            totalDueText.setText(String.format(Locale.US, "المطلوب الكلي: %.2f", finalTotalDue));
            totalPaidText.setText(String.format(Locale.US, "المدفوع الكلي: %.2f", finalTotalPaid));
            totalRemainingText.setText(String.format(Locale.US, "الباقي الكلي: %.2f", finalTotalRemaining));

            loadingProgress.setVisibility(View.GONE);

            if (rows.isEmpty()) {
                Toast.makeText(this, "لا توجد بيانات للعرض", Toast.LENGTH_LONG).show();
            }
        });
    }

    private TableRow createTableRow(ResultSet rs, int index) throws Exception {
        TableRow row = new TableRow(this);
        row.setPadding(8, 8, 8, 8);
        row.setBackgroundColor(index % 2 == 0 ? 0xFFF9F9F9 : 0xFFFFFFFF);

        // Add cells in the same order as headers
        row.addView(createCell(String.valueOf(rs.getInt("mosta7akat_id")), 50));
        row.addView(createCell(rs.getString("mosta7kat_type"), 65));
        row.addView(createCell(rs.getString("mosta7akat_date")));
        row.addView(createCell(rs.getString("mosta7akat_notes"), 400));

        TextView beforeCell = createCell(String.format(Locale.US, "%.2f", rs.getDouble("mosta7akat_matloob_before")), 120);
        beforeCell.setBackgroundColor(0xFFFFE0E0); // Light red
        row.addView(beforeCell);

        TextView fatouraCell = createCell(String.format(Locale.US, "%.2f", rs.getDouble("matloob_fatora")), 120);
        fatouraCell.setBackgroundColor(0xFFFFB0B0); // Medium red
        row.addView(fatouraCell);

        TextView dofaCell = createCell(String.format(Locale.US, "%.2f", rs.getDouble("mosta7akat_dof3ah_amount")));
        dofaCell.setBackgroundColor(0xFFE0FFE0); // Light green
        row.addView(dofaCell);

        row.addView(createCell(String.format(Locale.US, "%.2f", rs.getDouble("mosta7akat_matloob_after")), 120));
        row.addView(createCell(rs.getString("mosta7akat_user"), 120));
        row.addView(createCell(rs.getString("fatora_number"), 60));

        return row;
    }

    private TextView createCell(String text, int widthDp) {
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

        if (widthDp > 0) {
            float scale = getResources().getDisplayMetrics().density;
            cell.setWidth((int)(widthDp * scale));
        }

        return cell;
    }

    private TextView createCell(String text) {
        return createCell(text, 0);
    }

    private void clearTable() {
        int childCount = tableLayout.getChildCount();
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1);
        }
    }

    private void exportToPdf() {
        // TODO: Implement PDF export similar to SalesReportActivity
        Toast.makeText(this, "جاري تطوير خاصية التصدير", Toast.LENGTH_SHORT).show();
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