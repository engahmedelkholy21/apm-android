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

public class pay_madeen extends AppCompatActivity {
    private TextInputEditText debtorNameEdit, amountDueEdit, amountPaidEdit, remainingEdit;
    private TextInputEditText paymentAmountEdit, notesEdit, dateEdit;
    private Connection connection;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isAddingDebt = false;
    private String username = GlobalData.user_name; // TODO: replace with actual user
    private String branch = GlobalData.userFar3;   // TODO: replace with actual branch

    private double matloobBefore = 0;
    private double madfoo3Before = 0;
    private double ba2yBefore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_madeen);

        initializeViews();
        setupDatePicker();
        setupPaymentAmountValidation();
        setupSaveButton();

        String debtorName = getIntent().getStringExtra("debtor_name");
        isAddingDebt = getIntent().getBooleanExtra("is_adding_debt", false);

        if (debtorName != null) {
            debtorNameEdit.setText(debtorName);
            loadDebtorDetails(debtorName);
        }
    }

    private void initializeViews() {
        debtorNameEdit = findViewById(R.id.debtorNameEdit);
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

    private void loadDebtorDetails(String debtorName) {
        executor.execute(() -> {
            try {
                if (connection == null || connection.isClosed()) {
                    connection = new ConnectionHelper().conclass();
                }
                String query = "SELECT madeen_small_matloob, madeen_small_madfoo3, madeen_small_ba2y " +
                        "FROM madeen_small_report WHERE madeen_small_name = ?";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, debtorName);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            matloobBefore = rs.getDouble("madeen_small_matloob");
                            madfoo3Before = rs.getDouble("madeen_small_madfoo3");
                            ba2yBefore = rs.getDouble("madeen_small_ba2y");
                            mainHandler.post(() -> {
                                amountDueEdit.setText(String.format(Locale.US, "%.2f", matloobBefore));
                                amountPaidEdit.setText(String.format(Locale.US, "%.2f", madfoo3Before));
                                remainingEdit.setText(String.format(Locale.US, "%.2f", ba2yBefore));
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                showError("خطأ في تحميل بيانات المدين: " + e.getMessage());
            }
        });
    }

    private void savePayment() {
        if (!validateInput()) return;

        String debtorName = debtorNameEdit.getText().toString();
        double amount;
        try {
            amount = Double.parseDouble(paymentAmountEdit.getText().toString());
        } catch (NumberFormatException e) {
            showError("الرجاء إدخال مبلغ صحيح");
            return;
        }
      

        String date = dateEdit.getText().toString();

        executor.execute(() -> {
            try {
                if (connection == null || connection.isClosed()) {
                    connection = new ConnectionHelper().conclass();
                }
                connection.setAutoCommit(false);
                if (isAddingDebt) {

                    String notes = notesEdit.getText().toString();
                    // Add debt: update matloob, insert record
                    updateMatloob(debtorName, amount);
                    double matloobAfter = matloobBefore + amount;
                    insertMosta7akat(debtorName, date,
                            matloobAfter, madfoo3Before,
                            amount, // dof3ah_amount
                            0,       // matloob_fatora
                            username,
                            matloobBefore, matloobAfter,
                            notes, "مدين", 0);
                } else {
                    String notes = notesEdit.getText().toString()+" "+"سداد من الحساب";
                    // Pay debt: update madfoo3, insert record, then income
                    updateMadfoo3(debtorName, amount);
                    double madfoo3After = madfoo3Before + amount;
                    double ba2yAfter = ba2yBefore - amount;
                    insertMosta7akat(debtorName, date,
                            matloobBefore, madfoo3After,
                            amount, // dof3ah_amount
                            0,       // matloob_fatora
                            username,
                            ba2yBefore, ba2yAfter,
                            notes, "دائن", 0);
                    insertIncome(debtorName, date, amount, notes);
                }
                connection.commit();
                mainHandler.post(() -> {
                    showSuccess("تمت العملية بنجاح");
                    askToPrintReceipt();
                });
            } catch (SQLException e) {
                try { if (connection != null) connection.rollback(); } catch (SQLException ignore) {}
                showError("خطأ في قاعدة البيانات: " + e.getMessage());
            }
        });
    }

    private void updateMatloob(String name, double amount) throws SQLException {
        String sql = "UPDATE madeen_small_report SET madeen_small_matloob = madeen_small_matloob + ?, madeen_small_madfoo3 = madeen_small_madfoo3 WHERE madeen_small_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    private void updateMadfoo3(String name, double amount) throws SQLException {
        String sql = "UPDATE madeen_small_report SET madeen_small_madfoo3 = madeen_small_madfoo3 + ? WHERE madeen_small_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    private void insertMosta7akat(String name, String date,
                                  double totalMosta7ak,
                                  double totalPaid,
                                  double dof3ahAmount,
                                  double matloobFatora,
                                  String user,
                                  double matloobBefore,
                                  double matloobAfter,
                                  String notes,
                                  String type,
                                  int fatoraNumber) throws SQLException {
        String sql = "INSERT INTO mosta7akat_table (mosta7akat_cst_name, mosta7akat_date, mosta7akat_total_mosta7ak, " +
                "mosta7akat_total_paid, mosta7akat_dof3ah_amount, matloob_fatora, mosta7akat_user, " +
                "mosta7akat_matloob_before, mosta7akat_matloob_after, mosta7akat_notes, mosta7kat_type, fatora_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, date);
            ps.setDouble(3, totalMosta7ak);
            ps.setDouble(4, totalPaid);
            ps.setDouble(5, dof3ahAmount);
            ps.setDouble(6, matloobFatora);
            ps.setString(7, user);
            ps.setDouble(8, matloobBefore);
            ps.setDouble(9, matloobAfter);
            ps.setString(10, notes);
            ps.setString(11, type);
            ps.setInt(12, fatoraNumber);
            ps.executeUpdate();
        }
    }

    private void insertIncome(String name, String date, double amount, String notes) throws SQLException {


        String sql = "INSERT INTO income_table (income_date, income_details, income_cost, income_fator_id, income_source, " +
                "income_esm_elmashroo3, income_user, income_notes, income_far3, ba2y_fatora) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, date);
            ps.setString(2, "سداد من الحساب للعميل " + name);
            ps.setDouble(3, amount);
            ps.setInt(4, 0);
            ps.setString(5, "دفعات مدينين");
            ps.setString(6, name);
            ps.setString(7, username);
            ps.setString(8, notes);
            ps.setString(9, branch);
            ps.setInt(10, 0);
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

    private void askToPrintReceipt() {
        new AlertDialog.Builder(this)
                .setTitle("تأكيد")
                .setMessage("تمت العملية بنجاح، هل تريد طباعة إيصال؟")
                .setPositiveButton("نعم", (dialog, which) -> finish())
                .setNegativeButton("لا", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showError(String msg) {
        mainHandler.post(() -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show());
    }

    private void showSuccess(String msg) {
        mainHandler.post(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        try { if (connection != null && !connection.isClosed()) connection.close(); } catch (SQLException ignored) {}
    }
}
