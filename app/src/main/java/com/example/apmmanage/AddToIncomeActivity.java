package com.example.apmmanage;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddToIncomeActivity extends AppCompatActivity {
    private TextInputEditText dateEdit, detailsEdit, costEdit, notesEdit;
    private AutoCompleteTextView personNameEdit;
    private Connection connection;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String username = GlobalData.user_name; // Replace with actual user
    private String branch = GlobalData.userFar3;    // Replace with actual branch

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_income);

        initializeViews();
        setupDatePicker();
        setupAmountValidation();
        setupSaveButton();
        loadSuggestions();
    }

    private void initializeViews() {
        dateEdit = findViewById(R.id.dateEdit);
        detailsEdit = findViewById(R.id.detailsEdit);
        costEdit = findViewById(R.id.costEdit);
        notesEdit = findViewById(R.id.notesEdit);
        personNameEdit = findViewById(R.id.personNameEdit);

        dateEdit.setText(dateFormat.format(new Date()));
    }

    private void setupDatePicker() {
        dateEdit.setOnClickListener(v -> new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            dateEdit.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void setupAmountValidation() {
        costEdit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (!text.isEmpty() && !text.matches("^\\d*\\.?\\d*$")) {
                    costEdit.setText(text.replaceAll("[^\\d.]", ""));
                    costEdit.setSelection(costEdit.length());
                }
            }
        });
    }

    private void setupSaveButton() {
        findViewById(R.id.saveButton).setOnClickListener(v -> saveIncome());
    }

    private void loadSuggestions() {
        executor.execute(() -> {
            try {
                if (connection == null || connection.isClosed()) {
                    connection = new ConnectionHelper().conclass();
                }

                Set<String> names = new HashSet<>();

                // Get names from income table
                String query1 = "SELECT DISTINCT income_esm_elmashroo3 FROM income_table";
                try (PreparedStatement ps = connection.prepareStatement(query1);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        names.add(rs.getString(1));
                    }
                }

                // Get names from da2n_small_reports
                String query2 = "SELECT DISTINCT da2n_small_name FROM da2n_small_reports";
                try (PreparedStatement ps = connection.prepareStatement(query2);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        names.add(rs.getString(1));
                    }
                }

                mainHandler.post(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line,
                            new ArrayList<>(names));
                    personNameEdit.setAdapter(adapter);
                    personNameEdit.setThreshold(1);
                });

            } catch (Exception e) {
                showError("خطأ في تحميل الاقتراحات: " + e.getMessage());
            }
        });
    }

    private void saveIncome() {
        if (!validateInput()) return;

        String date = dateEdit.getText().toString();
        String details = detailsEdit.getText().toString();
        double amount = Double.parseDouble(costEdit.getText().toString());
        String person = personNameEdit.getText().toString();
        String notes = notesEdit.getText().toString();

        executor.execute(() -> {
            try {
                if (connection == null || connection.isClosed()) {
                    connection = new ConnectionHelper().conclass();
                }

                connection.setAutoCommit(false);

                // Insert income
                String sql = "INSERT INTO income_table (income_date, income_details, income_cost, " +
                        "income_source, income_esm_elmashroo3, income_user, income_notes, " +
                        "income_far3, income_fator_id, ba2y_fatora) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, date);
                    ps.setString(2, details);
                    ps.setDouble(3, amount);
                    ps.setString(4, "عمليات سريعه");
                    ps.setString(5, person);
                    ps.setString(6, username);
                    ps.setString(7, notes);
                    ps.setString(8, branch);
                    ps.setInt(9, 0);
                    ps.setInt(10, 0);
                    ps.executeUpdate();
                }

                // Check if person exists in da2n_small_reports
                String checkQuery = "SELECT * FROM da2n_small_reports WHERE da2n_small_name = ?";
                boolean hasDebtor = false;
                try (PreparedStatement ps = connection.prepareStatement(checkQuery)) {
                    ps.setString(1, person);
                    try (ResultSet rs = ps.executeQuery()) {
                        hasDebtor = rs.next();
                    }
                }

                if (hasDebtor) {
                    mainHandler.post(() -> showDebtorDialog(person, amount, date));
                } else {
                    connection.commit();
                    showSuccess("تمت الإضافة بنجاح");
                    clearFields();
                }

            } catch (Exception e) {
                try { if (connection != null) connection.rollback(); } catch (Exception ignored) {}
                showError("خطأ في حفظ العملية: " + e.getMessage());
            }
        });
    }

    private void showDebtorDialog(String person, double amount, String date) {
        new AlertDialog.Builder(this)
                .setTitle("تنبيه")
                .setMessage("يوجد دائن بنفس الاسم، هل تريد إضافة المبلغ للحساب؟")
                .setPositiveButton("نعم", (dialog, which) ->
                        updateDebtorAccount(person, amount, date))
                .setNegativeButton("لا", (dialog, which) -> {
                    try {
                        connection.commit();
                        showSuccess("تمت الإضافة بنجاح");
                        clearFields();
                    } catch (Exception e) {
                        showError("خطأ: " + e.getMessage());
                    }
                })
                .show();
    }

    private void updateDebtorAccount(String person, double amount, String date) {
        executor.execute(() -> {
            try {
                // Update da2n_small_reports
                String updateSql = "UPDATE da2n_small_reports " +
                        "SET da2n_small_matloob = da2n_small_matloob + ? " +
                        "WHERE da2n_small_name = ?";

                try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                    ps.setDouble(1, amount);
                    ps.setString(2, person);
                    ps.executeUpdate();
                }

                // Get current values
                double matloobBefore = 0;
                double madfoo3Before = 0;

                String selectSql = "SELECT da2n_small_matloob, da2n_small_madfoo3 " +
                        "FROM da2n_small_reports WHERE da2n_small_name = ?";

                try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
                    ps.setString(1, person);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            matloobBefore = rs.getDouble("da2n_small_matloob");
                            madfoo3Before = rs.getDouble("da2n_small_madfoo3");
                        }
                    }
                }

                // Insert into mosta7akat_da2neen_table
                String insertSql = "INSERT INTO mosta7akat_da2neen_table " +
                        "(mosta7akat_da2neen_cst_name, mosta7akat_da2neen_date, " +
                        "mosta7akat_da2neen_total_mosta7ak, mosta7akat_da2neen_total_paid, " +
                        "matloob_fatora, mosta7akat_da2neen_dof3ah_amount, " +
                        "mosta7akat_da2neen_user, mosta7akat_da2neen_matloob_before, " +
                        "mosta7akat_da2neen_matloob_after, mosta7akat_da2neen_notes, " +
                        "mosta7kat_type, fatora_number) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                    ps.setString(1, person);
                    ps.setString(2, date);
                    ps.setDouble(3, matloobBefore + amount);
                    ps.setDouble(4, madfoo3Before);
                    ps.setDouble(5, amount);
                    ps.setDouble(6, 0);
                    ps.setString(7, username);
                    ps.setDouble(8, matloobBefore);
                    ps.setDouble(9, matloobBefore + amount);
                    ps.setString(10, "مبلغ مضاف للخزينة");
                    ps.setString(11, "مدين");
                    ps.setInt(12, 0);
                    ps.executeUpdate();
                }

                connection.commit();
                showSuccess("تمت الإضافة بنجاح");
                clearFields();

            } catch (Exception e) {
                try { if (connection != null) connection.rollback(); } catch (Exception ignored) {}
                showError("خطأ في تحديث حساب الدائن: " + e.getMessage());
            }
        });
    }

    private boolean validateInput() {
        if (costEdit.getText().toString().trim().isEmpty()) {
            showError("الرجاء إدخال المبلغ");
            costEdit.requestFocus();
            return false;
        }
        return true;
    }

    private void clearFields() {
        mainHandler.post(() -> {
            detailsEdit.setText("");
            costEdit.setText("");
            personNameEdit.setText("");
            notesEdit.setText("");
            dateEdit.setText(dateFormat.format(new Date()));
        });
    }

    private void showError(String message) {
        mainHandler.post(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    private void showSuccess(String message) {
        mainHandler.post(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        try { if (connection != null && !connection.isClosed()) connection.close(); } catch (Exception ignored) {}
    }
}
