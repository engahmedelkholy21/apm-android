package com.example.apmmanage;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Da2nActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private TextView totalText;
    private Connection connection;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AutoCompleteTextView searchView;
    private ScrollView scrollView;
    private List<String> allNames = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("ar"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_da2n);

        totalText = findViewById(R.id.totalText);
        tableLayout = findViewById(R.id.tableLayout);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        searchView = findViewById(R.id.searchView);
        scrollView = findViewById(R.id.scrollView);
        connection = new ConnectionHelper().conclass();
        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(2);

        setupSearch();
        setupScrollRefreshFix();
        fetchData();
    }

    private void setupScrollRefreshFix() {
        swipeRefreshLayout.setOnRefreshListener(this::fetchData);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            swipeRefreshLayout.setEnabled(!scrollView.canScrollVertically(-1));
        });

        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    private void setupSearch() {
        searchView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT DISTINCT da2n_small_name FROM da2n_small_reports");
             ResultSet rs = ps.executeQuery()) {

            allNames.clear();
            while (rs.next()) {
                allNames.add(rs.getString("da2n_small_name"));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    allNames
            );
            searchView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterData(String query) {
        if (tableLayout.getChildCount() > 1) {
            tableLayout.removeViews(1, tableLayout.getChildCount() - 1);
        }

        double totalRemaining = 0;
        String sql = "SELECT da2n_small_id, da2n_small_name, da2n_small_matloob, " +
                "da2n_small_madfoo3, da2n_small_ba2y FROM da2n_small_reports " +
                "WHERE da2n_small_name LIKE ? ORDER BY da2n_small_ba2y DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ResultSet rs = ps.executeQuery();

            boolean hasRows = false;
            int rowIndex = 0;

            while (rs.next()) {
                hasRows = true;
                String name = rs.getString("da2n_small_name");
                double matloob = rs.getDouble("da2n_small_matloob");
                double madfoo3 = rs.getDouble("da2n_small_madfoo3");
                double ba2y = rs.getDouble("da2n_small_ba2y");
                totalRemaining += ba2y;

                TableRow row = new TableRow(this);
                row.setBackgroundColor((rowIndex % 2 == 0) ? 0xFFFFFFFF : 0xFFF0F0F0);

                row.setOnLongClickListener(v -> {
                    PopupMenu popup = new PopupMenu(Da2nActivity.this, v);
                    popup.getMenu().add("تفاصيل حساب دائن");
                    popup.getMenu().add("دفع مبلغ من الحساب");
                    popup.getMenu().add("اضافة مبلغ للحساب");
                    popup.setOnMenuItemClickListener(item -> {
                        String title = item.getTitle().toString();
                        Intent intent = null;

                        switch (title) {
                            case "تفاصيل حساب دائن":
                                intent = new Intent(Da2nActivity.this, Mosta7akatDa2neenActivity.class);
                                intent.putExtra("creditor_name", name);
                                break;
                            case "دفع مبلغ من الحساب":
                                intent = new Intent(Da2nActivity.this, pay_da2n.class);
                                intent.putExtra("creditor_name", name);
                                intent.putExtra("is_adding_debt", false);
                                break;
                            case "اضافة مبلغ للحساب":
                                intent = new Intent(Da2nActivity.this, pay_da2n.class);
                                intent.putExtra("creditor_name", name);
                                intent.putExtra("is_adding_debt", true);
                                break;
                        }

                        if (intent != null) startActivity(intent);
                        return true;
                    });
                    popup.show();
                    return true;
                });

                row.addView(createCell(name, Gravity.START));
                row.addView(createCell(currencyFormat.format(matloob), Gravity.END));
                row.addView(createCell(currencyFormat.format(madfoo3), Gravity.END));
                row.addView(createCell(currencyFormat.format(ba2y), Gravity.END));

                tableLayout.addView(row);
                rowIndex++;
            }

            if (!hasRows) {
                showEmptyState();
            }

            totalText.setText(String.format("إجمالي الباقي: %s", currencyFormat.format(totalRemaining)));

        } catch (Exception e) {
            Toast.makeText(this, "خطأ في قاعدة البيانات: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void fetchData() {
        searchView.setText("");
        filterData("");
        swipeRefreshLayout.setRefreshing(false);
    }

    private TextView createCell(String text, int gravity) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(12, 16, 12, 16);
        tv.setTextSize(16);
        tv.setGravity(gravity);
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT, gravity == Gravity.START ? 2f : 1.2f);
        tv.setLayoutParams(params);
        return tv;
    }

    private void showEmptyState() {
        TableRow emptyRow = new TableRow(this);
        TextView emptyText = new TextView(this);
        emptyText.setText("لا توجد بيانات حالياً");
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setPadding(16, 32, 16, 32);
        emptyText.setTextSize(16);
        emptyText.setTextColor(0xFF888888);

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        params.span = 4;
        emptyText.setLayoutParams(params);

        emptyRow.addView(emptyText);
        tableLayout.addView(emptyRow);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (connection == null || connection.isClosed()) {
                connection = new ConnectionHelper().conclass();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "فشل إعادة الاتصال بقاعدة البيانات", Toast.LENGTH_LONG).show();
        }
        fetchData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
