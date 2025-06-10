package com.example.apmmanage;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class pay_da2n extends AppCompatActivity {
    private TextInputEditText creditorNameEdit, amountDueEdit, amountPaidEdit, remainingEdit;
    private TextInputEditText paymentAmountEdit, notesEdit, dateEdit;
    private Connection connection;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isAddingDebt = false;
    private String username = GlobalData.user_name;
    private String branch = GlobalData.userFar3;

    private double matloobBefore = 0;
    private double madfoo3Before = 0;
    private double ba2yBefore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_da2n);

        initializeViews();
        setupDatePicker();
        setupPaymentAmountValidation();
        setupSaveButton();

        String creditorName = getIntent().getStringExtra("creditor_name");
        isAddingDebt = getIntent().getBooleanExtra("is_adding_debt", false);

        if (creditorName != null) {
            creditorNameEdit.setText(creditorName);
            loadCreditorDetails(creditorName);
        }
    }

    private void initializeViews() {
        creditorNameEdit = findViewById(R.id.creditorNameEdit);
        amountDueEdit = findViewById(R.id.amountDueEdit);
        amountPaidEdit = findViewById(R.id.amountPaidEdit);
        remainingEdit = findViewById(R.id.remainingEdit);
        paymentAmountEdit = findViewById(R.id.paymentAmountEdit);
        notesEdit = findViewById(R.id.notesEdit);
        dateEdit = findViewById(R.id.dateEdit);

        dateEdit.setText(dateFormat.format(new Date()));
    }

    private void setupDatePicker() {
        dateEdit.setOnClickListener(v -> new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            dateEdit.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void setupPaymentAmountValidation() {
        paymentAmountEdit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (!text.isEmpty() && !text.matches("^\\d*\\.?\\d*$")) {
                    paymentAmountEdit.setText(text.replaceAll("[^\\d.]", ""));
                    paymentAmountEdit.setSelection(paymentAmountEdit.length());
                }
            }
        });
    }

    private void setupSaveButton() {
        findViewById(R.id.saveButton).setOnClickListener(v -> savePayment());
    }

    private void loadCreditorDetails(String creditorName) {
        executor.execute(() -> {
            try {
                if (connection == null || connection.isClosed()) {
                    connection = new ConnectionHelper().conclass();
                }
                String query = "SELECT da2n_small_matloob, da2n_small_madfoo3, da2n_small_ba2y " +
                        "FROM da2n_small_reports WHERE da2n_small_name = ?";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, creditorName);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            matloobBefore = rs.getDouble("da2n_small_matloob");
                            madfoo3Before = rs.getDouble("da2n_small_madfoo3");
                            ba2yBefore = rs.getDouble("da2n_small_ba2y");
                            mainHandler.post(() -> {
                                amountDueEdit.setText(String.format(Locale.US, "%.2f", matloobBefore));
                                amountPaidEdit.setText(String.format(Locale.US, "%.2f", madfoo3Before));
                                remainingEdit.setText(String.format(Locale.US, "%.2f", ba2yBefore));
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                showError("خطأ في تحميل بيانات الدائن: " + e.getMessage());
            }
        });
    }

    private void savePayment() {
        if (!validateInput()) return;

        String creditorName = creditorNameEdit.getText().toString();
        double amount;
        try {
            amount = Double.parseDouble(paymentAmountEdit.getText().toString());
        } catch (NumberFormatException e) {
            showError("الرجاء إدخال مبلغ صحيح");
            return;
        }

        if (!isAddingDebt && amount > ba2yBefore) {
            showError("المبلغ المدفوع أكبر من الباقي");
            return;
        }

        String date = dateEdit.getText().toString();
        String notes = notesEdit.getText().toString();

        new AlertDialog.Builder(this)
                .setTitle("تأكيد")
                .setMessage("هل تريد " + (isAddingDebt ? "إضافة" : "سداد") + " مبلغ " + amount + "؟")
                .setPositiveButton("نعم", (dialog, which) -> executeTransaction(creditorName, amount, date, notes))
                .setNegativeButton("لا", null)
                .show();
    }

    private void executeTransaction(String creditorName, double amount, String date, String notes) {
        executor.execute(() -> {
            try {
                if (connection == null || connection.isClosed()) {
                    connection = new ConnectionHelper().conclass();
                }
                connection.setAutoCommit(false);

                if (isAddingDebt) {
                    handleAddDebt(creditorName, amount, date, notes);
                } else {
                    handlePayDebt(creditorName, amount, date, notes);
                }

                connection.commit();
                mainHandler.post(() -> {
                    showSuccess("تمت العملية بنجاح");
                    finish();
                });
            } catch (SQLException e) {
                try { if (connection != null) connection.rollback(); } catch (SQLException ignore) {}
                showError("خطأ في قاعدة البيانات: " + e.getMessage());
            }
        });
    }

    private void handleAddDebt(String name, double amount, String date, String notes) throws SQLException {
        // Update da2n_small_reports
        String updateSql = "UPDATE da2n_small_reports SET da2n_small_matloob = da2n_small_matloob + ? WHERE da2n_small_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
            ps.setDouble(1, amount);
            ps.setString(2, name);
            ps.executeUpdate();
        }

        // Insert into mosta7akat_da2neen_table
        double matloobAfter = matloobBefore + amount;
        double ba2yAfter = matloobAfter - madfoo3Before;

        insertMosta7akatDa2neen(name, date, matloobAfter, madfoo3Before, amount, 0,
                username, ba2yBefore, ba2yAfter, notes, "مدين", 0);
    }

    private void handlePayDebt(String name, double amount, String date, String notes) throws SQLException {
        // Update da2n_small_reports
        String updateSql = "UPDATE da2n_small_reports SET da2n_small_madfoo3 = da2n_small_madfoo3 + ? WHERE da2n_small_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
            ps.setDouble(1, amount);
            ps.setString(2, name);
            ps.executeUpdate();
        }

        // Insert into mosta7akat_da2neen_table
        double madfoo3After = madfoo3Before + amount;
        double ba2yAfter = matloobBefore - madfoo3After;

        insertMosta7akatDa2neen(name, date, matloobBefore, madfoo3After, 0, amount,
                username, ba2yBefore, ba2yAfter, "سداد من الحساب " + notes, "دائن", 0);

        // Insert into outcome_table
        insertOutcome(name, date, amount, notes);
    }

    private void insertMosta7akatDa2neen(String name, String date, double totalMosta7ak,
                                         double totalPaid, double matloobFatora, double dof3ahAmount,
                                         String user, double matloobBefore, double matloobAfter,
                                         String notes, String type, int fatoraNumber) throws SQLException {
        String sql = "INSERT INTO mosta7akat_da2neen_table (mosta7akat_da2neen_cst_name, mosta7akat_da2neen_date, " +
                "mosta7akat_da2neen_total_mosta7ak, mosta7akat_da2neen_total_paid, matloob_fatora, " +
                "mosta7akat_da2neen_dof3ah_amount, mosta7akat_da2neen_user, mosta7akat_da2neen_matloob_before, " +
                "mosta7akat_da2neen_matloob_after, mosta7akat_da2neen_notes, mosta7kat_type, fatora_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, date);
            ps.setDouble(3, totalMosta7ak);
            ps.setDouble(4, totalPaid);
            ps.setDouble(5, matloobFatora);
            ps.setDouble(6, dof3ahAmount);
            ps.setString(7, user);
            ps.setDouble(8, matloobBefore);
            ps.setDouble(9, matloobAfter);
            ps.setString(10, notes);
            ps.setString(11, type);
            ps.setInt(12, fatoraNumber);
            ps.executeUpdate();
        }
    }

    private void insertOutcome(String name, String date, double amount, String notes) throws SQLException {
        String sql = "INSERT INTO outcome_table (outcome_date, outcome_details, outcome_cost, outcome_fator_id, " +
                "outcome_source, outcome_esm_elmashroo3, outcome_notes, outcome_user, outcome_far3) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, date);
            ps.setString(2, "مبلغ مدفوع من الحساب");
            ps.setDouble(3, amount);
            ps.setInt(4, 0);
            ps.setString(5, "دفعات دائنين");
            ps.setString(6, name);
            ps.setString(7, notes);
            ps.setString(8, username);
            ps.setString(9, branch);
            ps.executeUpdate();
        }
    }

    private boolean validateInput() {
        if (paymentAmountEdit.getText().toString().trim().isEmpty()) {
            showError("الرجاء إدخال المبلغ");
            paymentAmountEdit.requestFocus();
            return false;
        }
        return true;
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
        try { if (connection != null && !connection.isClosed()) connection.close(); } catch (SQLException ignored) {}
    }
}