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
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
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

public class SalesReportActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private TextView totalSalesText, totalPaidText, totalRemainingText;
    private AutoCompleteTextView searchTypeSpinner;
    private TextInputEditText fromDateEdit, toDateEdit, searchEdit;
    private View loadingProgress, loadMoreButton, exportButton;
    private Connection connection;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final int PAGE_SIZE = 50;
    private int currentPage = 0;
    private boolean isLoading = false;
    private List<SalesRecord> cachedRecords = new ArrayList<>();

    private static class SalesRecord {
        int id;
        String date;
        String customerName;
        String productName;
        double quantity;
        double unitPrice;
        double price_for_sell;
        double remaining;
        String invoiceType;

        SalesRecord(ResultSet rs) throws Exception {
            this.id = rs.getInt("sales_id");
            this.date = rs.getString("sales_date");
            this.customerName = rs.getString("sales_cst_name");
            this.productName = rs.getString("sales_product_name");
            this.quantity = rs.getDouble("sales_product_count");
            this.unitPrice = rs.getDouble("sales_unit_price");
            this.price_for_sell = rs.getDouble("sales_price_for_sell");

            this.invoiceType = rs.getString("sales_fatora_type");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_report);

        initializeViews();
        setupSearchTypeSpinner();
        setupDatePickers();
        setupSearchButton();
        setupLoadMoreButton();
        setupExportButton();

        executor.execute(this::initializeDatabase);
    }

    private void initializeViews() {
        tableLayout = findViewById(R.id.tableLayout);
        totalSalesText = findViewById(R.id.totalSalesText);
        totalPaidText = findViewById(R.id.totalPaidText);
        totalRemainingText = findViewById(R.id.totalRemainingText);
        searchTypeSpinner = findViewById(R.id.searchTypeSpinner);
        fromDateEdit = findViewById(R.id.fromDateEdit);
        toDateEdit = findViewById(R.id.toDateEdit);
        searchEdit = findViewById(R.id.searchEdit);
        loadingProgress = findViewById(R.id.loadingProgress);
        loadMoreButton = findViewById(R.id.loadMoreButton);
        exportButton = findViewById(R.id.exportButton);

        String currentDate = dateFormat.format(new Date());
        fromDateEdit.setText(currentDate);
        toDateEdit.setText(currentDate);
    }

    private void setupSearchTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.search_types, android.R.layout.simple_dropdown_item_1line);
        searchTypeSpinner.setAdapter(adapter);
        searchTypeSpinner.setText(adapter.getItem(0).toString(), false);

        searchTypeSpinner.setOnItemClickListener((parent, view, position, id) -> {
            searchEdit.setHint(getSearchHint(position));
            searchEdit.setText("");
        });
    }

    private String getSearchHint(int position) {
        switch (position) {
            case 1: return "ادخل اسم المنتج";
            case 2: return "ادخل اسم العميل";
            case 3: return "ادخل اسم الموظف";
            case 4: return "ادخل رقم الفاتورة";
            case 5: return "ادخل الفئة";
            default: return "بحث";
        }
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

        fromDateEdit.setOnClickListener(dateClickListener);
        toDateEdit.setOnClickListener(dateClickListener);
    }

    private void setupSearchButton() {
        findViewById(R.id.searchButton).setOnClickListener(v -> {
            v.setEnabled(false);
            fetchData(true);
        });
    }

    private void setupLoadMoreButton() {
        loadMoreButton.setOnClickListener(v -> {
            currentPage++;
            fetchData(false);
        });
    }

    private void setupExportButton() {
        exportButton.setOnClickListener(v -> exportToPdf());
    }

    private void initializeDatabase() {
        try {
            connection = new ConnectionHelper().conclass();
            if (connection != null) {
                mainHandler.post(() -> fetchData(true));
            } else {
                showError("فشل الاتصال بقاعدة البيانات");
            }
        } catch (Exception e) {
            showError("خطأ في الاتصال: " + e.getMessage());
        }
    }

    private void fetchData(boolean resetPage) {
        if (isLoading) return;
        isLoading = true;

        if (resetPage) {
            currentPage = 0;
            cachedRecords.clear();
            clearTable();
        }

        loadingProgress.setVisibility(View.VISIBLE);
        loadMoreButton.setVisibility(View.GONE);

        executor.execute(() -> {
            try {
                if (connection == null || connection.isClosed()) {
                    connection = new ConnectionHelper().conclass();
                }

                if (connection == null) {
                    showError("لا يوجد اتصال بقاعدة البيانات");
                    return;
                }

                String query = buildQuery();
                try (PreparedStatement ps = connection.prepareStatement(query,
                        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

                    setQueryParameters(ps);

                    try (ResultSet rs = ps.executeQuery()) {
                        processResultSet(rs);
                    }
                }
            } catch (Exception e) {
                showError("خطأ في قاعدة البيانات: " + e.getMessage());
            } finally {
                mainHandler.post(() -> {
                    isLoading = false;
                    loadingProgress.setVisibility(View.GONE);
                    findViewById(R.id.searchButton).setEnabled(true);
                });
            }
        });
    }

    private String buildQuery() {
        StringBuilder query = new StringBuilder();
        query.append("WITH NumberedRows AS (");
        query.append("    SELECT *, ROW_NUMBER() OVER (ORDER BY sales_date DESC) AS RowNum");
        query.append("    FROM sales_table WHERE 1=1");

        String searchType = searchTypeSpinner.getText().toString();

        if (searchType.equals("بحث برقم الفاتورة")) {
            query.append(" AND sales_id = ?");
        } else {
            // Add date range for all other search types
            query.append(" AND sales_date BETWEEN ? AND ?");

            if (searchType.equals("بحث باسم المنتج")) {
                query.append(" AND sales_product_name LIKE ?");
            } else if (searchType.equals("بحث باسم العميل")) {
                query.append(" AND sales_cst_name LIKE ?");
            } else if (searchType.equals("بحث بالموظف")) {
                query.append(" AND (sales_user LIKE ? OR sales_mandoob LIKE ?)");
            } else if (searchType.equals("بحث بالفئة")) {
                query.append(" AND sales_pro_category LIKE ?");
            }
        }

        query.append(")");
        query.append(" SELECT * FROM NumberedRows");
        query.append(" WHERE RowNum BETWEEN ? AND ?");

        return query.toString();
    }

    private void setQueryParameters(PreparedStatement ps) throws Exception {
        int paramIndex = 1;
        String searchType = searchTypeSpinner.getText().toString();
        String searchValue = searchEdit.getText().toString().trim();

        if (searchType.equals("بحث برقم الفاتورة")) {
            ps.setInt(paramIndex++, Integer.parseInt(searchValue));
        } else {
            // Set date range parameters for all other search types
            ps.setString(paramIndex++, fromDateEdit.getText().toString());
            ps.setString(paramIndex++, toDateEdit.getText().toString());

            if (searchType.equals("بحث بالموظف")) {
                String likeValue = "%" + searchValue + "%";
                ps.setString(paramIndex++, likeValue);
                ps.setString(paramIndex++, likeValue);
            } else if (!searchType.equals("بحث في يوم أو فترة")) {
                ps.setString(paramIndex++, "%" + searchValue + "%");
            }
        }

        // Set pagination parameters
        int startRow = (currentPage * PAGE_SIZE) + 1;
        int endRow = startRow + PAGE_SIZE - 1;
        ps.setInt(paramIndex++, startRow);
        ps.setInt(paramIndex, endRow);
    }

    private void processResultSet(ResultSet rs) throws Exception {
        List<SalesRecord> newRecords = new ArrayList<>();
        final double[] totals = new double[3]; // [totalSales, totalPaid, totalRemaining]
        int count = 0;

        while (rs.next() && count < PAGE_SIZE) {
            SalesRecord record = new SalesRecord(rs);
            newRecords.add(record);

            totals[0] += record.unitPrice * record.quantity;
            totals[1] += record.price_for_sell;
            totals[2] += record.remaining;
            count++;
        }

        final boolean hasMore = count == PAGE_SIZE;
        cachedRecords.addAll(newRecords);

        mainHandler.post(() -> {
            updateTableWithRecords(newRecords);
            updateTotals(totals[0], totals[1], totals[2]);
            loadMoreButton.setVisibility(hasMore ? View.VISIBLE : View.GONE);
        });
    }

    private void updateTableWithRecords(List<SalesRecord> records) {
        for (SalesRecord record : records) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            row.addView(createCell(String.valueOf(record.id)));
            row.addView(createCell(record.date));
            row.addView(createCell(record.customerName));
            row.addView(createCell(record.productName));
            row.addView(createCell(String.format(Locale.US, "%.2f", record.quantity)));
            row.addView(createCell(String.format(Locale.US, "%.2f", record.unitPrice)));
            row.addView(createCell(String.format(Locale.US, "%.2f", record.price_for_sell)));
            row.addView(createCell(record.invoiceType));

            tableLayout.addView(row);
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
        cell.setTextColor(getResources().getColor(R.color.gray_700));
        cell.setBackgroundResource(android.R.color.white);
        return cell;
    }

    private void clearTable() {
        int childCount = tableLayout.getChildCount();
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1);
        }
    }

    private void updateTotals(double totalSales, double totalPaid, double totalRemaining) {
        totalSalesText.setText(String.format(Locale.US, "%.2f", totalSales));
        totalPaidText.setText(String.format(Locale.US, "%.2f", totalPaid));
        totalRemainingText.setText(String.format(Locale.US, "%.2f", totalRemaining));
    }

    private void showError(String message) {
        mainHandler.post(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    private void exportToPdf() {
        executor.execute(() -> {
            try {
                String fileName = "sales_report_" + dateFormat.format(new Date()) + ".pdf";
                File pdfFile = new File(getExternalFilesDir(null), fileName);

                Document document = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                document.open();

                BaseFont baseFont = BaseFont.createFont("assets/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                Font arabicFont = new Font(baseFont, 12);
                Font headerFont = new Font(baseFont, 14, Font.BOLD);

                Paragraph title = new Paragraph("تقرير المبيعات", headerFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph("\n"));

                Paragraph dateRange = new Paragraph(
                        "من " + fromDateEdit.getText() + " إلى " + toDateEdit.getText(), arabicFont);
                dateRange.setAlignment(Element.ALIGN_CENTER);
                document.add(dateRange);
                document.add(new Paragraph("\n"));

                PdfPTable pdfTable = new PdfPTable(9);
                pdfTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                pdfTable.setWidthPercentage(100);

                String[] headers = {"رقم الفاتورة", "التاريخ", "اسم العميل", "المنتج",
                        "الكمية", "السعر", "الاجمالي",  "نوع الفاتورة"};

                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setPadding(8);
                    pdfTable.addCell(cell);
                }

                for (SalesRecord record : cachedRecords) {
                    pdfTable.addCell(new Phrase(String.valueOf(record.id), arabicFont));
                    pdfTable.addCell(new Phrase(record.date, arabicFont));
                    pdfTable.addCell(new Phrase(record.customerName, arabicFont));
                    pdfTable.addCell(new Phrase(record.productName, arabicFont));
                    pdfTable.addCell(new Phrase(String.format(Locale.US, "%.2f", record.quantity), arabicFont));
                    pdfTable.addCell(new Phrase(String.format(Locale.US, "%.2f", record.unitPrice), arabicFont));
                    pdfTable.addCell(new Phrase(String.format(Locale.US, "%.2f", record.price_for_sell), arabicFont));
                    pdfTable.addCell(new Phrase(record.invoiceType, arabicFont));
                }

                document.add(pdfTable);
                document.close();

                mainHandler.post(() -> Toast.makeText(this,
                        "تم حفظ التقرير في " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                showError("خطأ في إنشاء التقرير: " + e.getMessage());
            }
        });
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