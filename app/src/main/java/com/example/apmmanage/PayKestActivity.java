package com.example.apmmanage;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PayKestActivity extends AppCompatActivity {

    EditText loanCodeInput, payAmountInput;
    TextView customerName, customerCode, loanCodeTextView, kestNumber, est7kakDate, kestAmount;
    TextView mandoobName, mandoobCode, loanPeriodView, fatoraNumView;
    TextView damen1Name, damen1Code, damen2Name, damen2Code;
    Button payButton;

    String today, notes, status = "مسدد";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_kest);

        loanCodeInput = findViewById(R.id.loanCodeInput);
        payAmountInput = findViewById(R.id.payAmountInput);
        customerName = findViewById(R.id.customerName);
        customerCode = findViewById(R.id.customerCode);
        loanCodeTextView = findViewById(R.id.loanCodeTextView);
        kestNumber = findViewById(R.id.kestNumber);
        est7kakDate = findViewById(R.id.est7kakDate);
        kestAmount = findViewById(R.id.kestAmount);
        mandoobName = findViewById(R.id.mandoobName);
        mandoobCode = findViewById(R.id.mandoobCode);
        loanPeriodView = findViewById(R.id.loanPeriod);
        fatoraNumView = findViewById(R.id.fatoraNum);
        damen1Name = findViewById(R.id.damen1Name);
        damen1Code = findViewById(R.id.damen1Code);
        damen2Name = findViewById(R.id.damen2Name);
        damen2Code = findViewById(R.id.damen2Code);
        payButton = findViewById(R.id.payButton);

        findViewById(R.id.search_button).setOnClickListener(v -> fetchKestData());
        payButton.setOnClickListener(v -> processPayment());
    }
    private void fetchKestData() {
        try (Connection conn = new ConnectionHelper().conclass();) {
            String keyword = loanCodeInput.getText().toString().trim();

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT TOP 1 a.*, l.loan_period, l.fatora_num, c.cst_ID " +
                            "FROM aksat_table a " +
                            "JOIN loans_requests_table l ON a.kest_loan_code = l.loan_code AND l.loan_far3 = ? " +
                            "JOIN customers c ON a.kest_cst_code = c.cst_ID " +
                            "WHERE (a.kest_loan_code = ? OR a.kest_cst_name LIKE ?) " +
                            "AND a.kest_far3 = ? AND a.kest_status != 'مسدد' " +
                            "ORDER BY a.kest_number ASC"
            );

            stmt.setString(1, GlobalData.userFar3);
            stmt.setString(2, keyword);
            stmt.setString(3, "%" + keyword + "%");
            stmt.setString(4, GlobalData.userFar3);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                customerCode.setText(rs.getString("kest_cst_code"));
                customerName.setText(rs.getString("kest_cst_name"));
                loanCodeTextView.setText(rs.getString("kest_loan_code"));
                kestNumber.setText(rs.getString("kest_number"));
                est7kakDate.setText(rs.getString("kest_est7kak_date"));
                kestAmount.setText(rs.getString("kest_amount"));
                payAmountInput.setText(rs.getString("kest_amount"));
                mandoobName.setText(rs.getString("kest_mandoob_name"));
                mandoobCode.setText(rs.getString("kest_mandoob_code"));
                loanPeriodView.setText(rs.getString("loan_period"));
                fatoraNumView.setText(rs.getString("fatora_num"));
                damen1Name.setText(rs.getString("kest_1st_damen_name"));
                damen1Code.setText(rs.getString("kest_1st_damen_code"));
                damen2Name.setText(rs.getString("kest_2nd_damen_name"));
                damen2Code.setText(rs.getString("kest_2nd_damen_code"));
            } else {
                showToast("لا توجد بيانات مطابقة");
            }

        } catch (Exception e) {
            showToast("خطأ في تحميل البيانات: " + e.getMessage());
        }
    }
    private void processPayment() {
        try (Connection conn = new ConnectionHelper().conclass();) {
            String payAmountStr = payAmountInput.getText().toString().trim();
            String fullAmountStr = kestAmount.getText().toString().trim();
            String kestNumStr = kestNumber.getText().toString().trim();
            String loanCode = loanCodeTextView.getText().toString().trim();
            today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            if (payAmountStr.isEmpty() || fullAmountStr.isEmpty()) {
                showToast("أدخل المبلغ بشكل صحيح"); return;
            }
            if (Double.parseDouble(payAmountStr) > Double.parseDouble(fullAmountStr)) {
                showToast("المبلغ أكبر من قيمة القسط"); return;
            }
            if (kestNumStr.isEmpty() || est7kakDate.getText().toString().trim().isEmpty()) {
                showToast("تأكد من صحة البيانات"); return;
            }

            // تحقق من السداد السابق
            PreparedStatement check = conn.prepareStatement(
                    "SELECT * FROM aksat_table WHERE kest_loan_code = ? AND kest_number = ? AND kest_far3 = ? AND kest_status = 'مسدد'");
            check.setString(1, loanCode);
            check.setInt(2, Integer.parseInt(kestNumStr));
            check.setString(3, GlobalData.userFar3);
            if (check.executeQuery().next()) {
                showToast("هذا القسط مسدد مسبقًا"); return;
            }

            double paid = Double.parseDouble(payAmountStr);
            double original = Double.parseDouble(fullAmountStr);

            if (paid < original) {
                // سداد جزئي
                notes = "سداد جزء من قسط رقم " + kestNumStr +" للعميل " + customerName.getText().toString();

                PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO aksat_table (kest_number, kest_cst_code, kest_cst_name, kest_1st_damen_code, kest_1st_damen_name, " +
                                "kest_2nd_damen_code, kest_2nd_damen_name, kest_amount, kest_est7kak_date, kest_pay_date, kest_notes, kest_user, " +
                                "kest_status, kest_delay_days, kest_mandoob_name, kest_mandoob_code, kest_far3, kest_modat_eltamweel, kest_loan_code) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?)");

                insert.setInt(1, Integer.parseInt(kestNumStr));
                insert.setString(2, customerCode.getText().toString());
                insert.setString(3, customerName.getText().toString());
                insert.setString(4, damen1Code.getText().toString());
                insert.setString(5, damen1Name.getText().toString());
                insert.setString(6, damen2Code.getText().toString());
                insert.setString(7, damen2Name.getText().toString());
                insert.setBigDecimal(8, new java.math.BigDecimal(paid));
                insert.setString(9, est7kakDate.getText().toString());
                insert.setString(10, today);
                insert.setString(11, notes);
                insert.setString(12, GlobalData.user_name);
                insert.setString(13, "مسدد");
                insert.setString(14, mandoobName.getText().toString());
                insert.setString(15, mandoobCode.getText().toString());
                insert.setString(16, GlobalData.userFar3);
                insert.setString(17, loanPeriodView.getText().toString());
                insert.setString(18, loanCode);
                insert.executeUpdate();

                // تحديث القسط الأصلي
                PreparedStatement update = conn.prepareStatement(
                        "UPDATE aksat_table SET kest_amount = ? WHERE kest_number = ? AND kest_loan_code = ? AND kest_cst_name = ? AND kest_far3 = ?");
                update.setBigDecimal(1, new java.math.BigDecimal(original - paid));
                update.setInt(2, Integer.parseInt(kestNumStr));
                update.setString(3, loanCode);
                update.setString(4, customerName.getText().toString());
                update.setString(5, GlobalData.userFar3);
                update.executeUpdate();

            } else {
                // سداد كامل
                notes = "قيمة قسط رقم " + kestNumStr + " للعميل " + customerName.getText().toString();//"سداد كامل"+ kestNumStr;

                PreparedStatement update = conn.prepareStatement(
                        "UPDATE aksat_table SET kest_status = ?, kest_pay_date = ?, kest_user = ?, kest_notes = ?, kest_amount = ? " +
                                "WHERE kest_number = ? AND kest_loan_code = ? AND kest_far3 = ?");
                update.setString(1, "مسدد");
                update.setString(2, today);
                update.setString(3, GlobalData.user_name);
                update.setString(4, notes);
                update.setBigDecimal(5, new java.math.BigDecimal(paid));
                update.setInt(6, Integer.parseInt(kestNumStr));
                update.setString(7, loanCode);
                update.setString(8, GlobalData.userFar3);
                update.executeUpdate();
            }

            insertIncomeRecord(conn, paid);
            updateCustomerStatusIfFinished(conn, loanCode, customerCode.getText().toString());

            showToast("تم السداد بنجاح");
            sendSms(loanCode, payAmountStr);

        } catch (Exception e) {
            showToast("خطأ أثناء السداد: " + e.getMessage());
        }
    }
    private void insertIncomeRecord(Connection conn, double paid) throws SQLException {
        PreparedStatement income = conn.prepareStatement(
                "INSERT INTO income_table " +
                        "(income_date, income_details, income_cost, income_fator_id, income_source, " +
                        "income_esm_elmashroo3, income_user, income_notes, income_far3, ba2y_fatora) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

//        income.setString(1, today);
//        income.setString(2, "قيمة " + notes + " للعميل " + customerName.getText().toString());
//        income.setBigDecimal(3, new java.math.BigDecimal(paid));
//        income.setInt(4, Integer.parseInt(fatoraNumView.getText().toString()));
//        income.setString(5, "سداد اقساط");
//        income.setString(6, customerName.getText().toString());
//        income.setString(7, GlobalData.user_name);
//        income.setString(8, GlobalData.userFar3);
//        income.executeUpdate();



        income.setString(1, today);
        income.setString(2,  notes);
        income.setBigDecimal(3, new BigDecimal(paid));
        income.setInt(4, Integer.parseInt(fatoraNumView.getText().toString()));
        income.setString(5, "سداد اقساط");
        income.setString(6, customerName.getText().toString());
        income.setString(7, GlobalData.user_name);
        income.setString(8, "");
        income.setString(9, GlobalData.userFar3);
        income.setBigDecimal(10, new BigDecimal(0));
        income.executeUpdate();
    }

    private void updateCustomerStatusIfFinished(Connection conn, String loanCode, String cstCode) throws SQLException {
        PreparedStatement unpaid = conn.prepareStatement(
                "SELECT * FROM aksat_table WHERE kest_loan_code = ? AND kest_status != 'مسدد' AND kest_far3 = ?");
        unpaid.setString(1, loanCode);
        unpaid.setString(2, GlobalData.userFar3);
        ResultSet rs = unpaid.executeQuery();

        if (!rs.next()) {
            PreparedStatement updateC = conn.prepareStatement(
                    "UPDATE customers SET cst_takseet_status = 'لا توجد التزامات' WHERE cst_ID = ?");
            updateC.setInt(1, Integer.parseInt(cstCode));
            updateC.executeUpdate();

            PreparedStatement updateL = conn.prepareStatement(
                    "UPDATE loans_requests_table SET loan_mandoob = 'متسرب', loan_agreed_or_not = 'متسرب' WHERE loan_code = ?");
            updateL.setString(1, loanCode);
            updateL.executeUpdate();
        }
    }

    private void sendSms(String code, String amount) {
        Toast.makeText(this, "SMS: تم سداد " + amount + " كود " + code, Toast.LENGTH_LONG).show();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
