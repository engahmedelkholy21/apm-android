package com.example.apmmanage;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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

public class SarfFromOutcomeActivity extends AppCompatActivity {
    private TextInputEditText dateEdit, detailsEdit, costEdit, notesEdit;
    private AutoCompleteTextView personNameEdit, sourceEdit;
    private TextInputEditText attachmentEdit;
    private Connection connection;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String username = GlobalData.user_name; // Replace with actual user
    private String branch = GlobalData.userFar3;    // Replace with actual branch
    private Uri selectedFileUri;

    private final ActivityResultLauncher<String[]> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    attachmentEdit.setText(uri.getLastPathSegment());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sarf_from_outcome);

        initializeViews();
        setupDatePicker();
        setupAmountValidation();
        setupSaveButton();
        setupAttachmentPicker();
        loadSuggestions();
    }

    private void initializeViews() {
        dateEdit = findViewById(R.id.dateEdit);
        detailsEdit = findViewById(R.id.detailsEdit);
        costEdit = findViewById(R.id.costEdit);
        notesEdit = findViewById(R.id.notesEdit);
        personNameEdit = findViewById(R.id.personNameEdit);
        sourceEdit = findViewById(R.id.sourceEdit);
        attachmentEdit = findViewById(R.id.attachmentEdit);

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
        findViewById(R.id.saveButton).setOnClickListener(v -> saveExpense());
    }

    private void setupAttachmentPicker() {
        findViewById(R.id.attachmentButton).setOnClickListener(v ->
                filePickerLauncher.launch(new String[]{"application/pdf", "image/*"})
        );
    }

    private void loadSuggestions() {
        executor.execute(() -> {
            try {
                if (connection == null || connection.isClosed()) {
                    connection = new ConnectionHelper().conclass();
                }

                Set<String> names = new HashSet<>();
                Set<String> sources = new HashSet<>();

                // Get names and sources from outcome table
                String query = "SELECT DISTINCT outcome_esm_elmashroo3, outcome_source FROM outcome_table";
                try (PreparedStatement ps = connection.prepareStatement(query);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        names.add(rs.getString("outcome_esm_elmashroo3"));
                        sources.add(rs.getString("outcome_source"));
                    }
                }

                // Get names from madeen_small_report
                String query2 = "SELECT DISTINCT madeen_small_name FROM madeen_small_report";
                try (PreparedStatement ps = connection.prepareStatement(query2);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        names.add(rs.getString(1));
                    }
                }

                mainHandler.post(() -> {
                    ArrayAdapter<String> namesAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line,
                            new ArrayList<>(names));
                    personNameEdit.setAdapter(namesAdapter);
                    personNameEdit.setThreshold(1);

                    ArrayAdapter<String> sourcesAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line,
                            new ArrayList<>(sources));
                    sourceEdit.setAdapter(sourcesAdapter);
                    sourceEdit.setThreshold(1);
                });

            } catch (Exception e) {
                showError("خطأ في تحميل الاقتراحات: " + e.getMessage());
            }
        });
    }

    private void saveExpense() {
        if (!validateInput()) return;

        String date = dateEdit.getText().toString();
        String details = detailsEdit.getText().toString();
        double amount = Double.parseDouble(costEdit.getText().toString());
        String person = personNameEdit.getText().toString();
        String source = sourceEdit.getText().toString();
        String notes = notesEdit.getText().toString();

        executor.execute(() -> {
            try {
                if (connection == null || connection.isClosed()) {
                    connection = new ConnectionHelper().conclass();
                }

                connection.setAutoCommit(false);

                // Insert outcome
                String sql = "INSERT INTO outcome_table (outcome_date, outcome_details, outcome_cost, " +
                        "outcome_fator_id, outcome_source, outcome_esm_elmashroo3, outcome_user, " +
                        "outcome_notes, outcome_far3) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, date);
                    ps.setString(2, details);
                    ps.setDouble(3, amount);
                    ps.setInt(4, 0);
                    ps.setString(5, source);
                    ps.setString(6, person);
                    ps.setString(7, username);
                    ps.setString(8, notes);
                    ps.setString(9, branch);
                    ps.executeUpdate();
                }



                // Check if person exists in madeen_small_report
                String checkQuery = "SELECT * FROM madeen_small_report WHERE madeen_small_name = ?";
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
                    showSuccess("تم الصرف بنجاح");
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
                .setMessage("يوجد مدين بنفس الاسم، هل تريد إضافة المبلغ للحساب؟")
                .setPositiveButton("نعم", (dialog, which) ->
                        updateDebtorAccount(person, amount, date))
                .setNegativeButton("لا", (dialog, which) -> {
                    try {
                        connection.commit();
                        showSuccess("تم الصرف بنجاح");
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
                // Update madeen_small_report
                String updateSql = "UPDATE madeen_small_report " +
                        "SET madeen_small_matloob = madeen_small_matloob + ? " +
                        "WHERE madeen_small_name = ?";

                try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                    ps.setDouble(1, amount);
                    ps.setString(2, person);
                    ps.executeUpdate();
                }

                // Get current values
                double matloobBefore = 0;
                double madfoo3Before = 0;

                String selectSql = "SELECT madeen_small_matloob, madeen_small_madfoo3 " +
                        "FROM madeen_small_report WHERE madeen_small_name = ?";

                try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
                    ps.setString(1, person);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            matloobBefore = rs.getDouble("madeen_small_matloob");
                            madfoo3Before = rs.getDouble("madeen_small_madfoo3");
                        }
                    }
                }

                // Insert into mosta7akat_table
                String insertSql = "INSERT INTO mosta7akat_table " +
                        "(mosta7akat_cst_name, mosta7akat_date, " +
                        "mosta7akat_total_mosta7ak, mosta7akat_total_paid, " +
                        "matloob_fatora, mosta7akat_dof3ah_amount, " +
                        "mosta7akat_user, mosta7akat_matloob_before, " +
                        "mosta7akat_matloob_after, mosta7akat_notes, " +
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
                    ps.setString(10, "مبلغ منصرف من الخزينة");
                    ps.setString(11, "مدين");
                    ps.setInt(12, 0);
                    ps.executeUpdate();
                }

                connection.commit();
                showSuccess("تم الصرف وتحديث حساب المدين بنجاح");
                clearFields();

            } catch (Exception e) {
                try { if (connection != null) connection.rollback(); } catch (Exception ignored) {}
                showError("خطأ في تحديث حساب المدين: " + e.getMessage());
            }
        });
    }

    private boolean validateInput() {
        if (sourceEdit.getText().toString().trim().isEmpty()) {
            showError("الرجاء إدخال المصدر");
            sourceEdit.requestFocus();
            return false;
        }
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
            sourceEdit.setText("");
            attachmentEdit.setText("");
            dateEdit.setText(dateFormat.format(new Date()));
            selectedFileUri = null;
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