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

public class PurchasesReportActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private TextView totalPurchasesText, totalPaidText, totalRemainingText;
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
    private List<PurchaseRecord> cachedRecords = new ArrayList<>();

    private static class PurchaseRecord {
        int id;
        String date;
        String supplierName;
        String supplierPhone;
        String supplierAddress;
        String productId;
        String productName;
        double quantity;
        double unitPrice;
        double price_for_sell;
        String notes;
        String stock;
        double priceBeforeDiscount;
        double discountPercentage;

        PurchaseRecord(ResultSet rs) throws Exception {
            this.id = rs.getInt("Purchases_id");
            this.date = rs.getString("Purchases_date");
            this.supplierName = rs.getString("Purchases_mwared_name");
            this.supplierPhone = rs.getString("Purchases_mwared_phone");
            this.supplierAddress = rs.getString("Purchases_mwared_address");
            this.productId = rs.getString("Purchases_product_ID");
            this.productName = rs.getString("Purchases_product_name");
            this.quantity = rs.getDouble("Purchases_product_count");
            this.unitPrice = rs.getDouble("Purchases_unit_price");
            this.price_for_sell = rs.getDouble("sales_price_for_sell");
            this.notes = rs.getString("sales_notes");
            this.stock = rs.getString("stock");
            this.priceBeforeDiscount = rs.getDouble("price_before_discount");
            this.discountPercentage = rs.getDouble("discount_nesba");
        }

        double getTotal() {
            return quantity * unitPrice;
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchases_report);

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
        totalPurchasesText = findViewById(R.id.totalPurchasesText);
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
                R.array.purchase_report_types, android.R.layout.simple_dropdown_item_1line);
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
            case 2: return "ادخل اسم المورد";
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
            currentPage = 0;
            cachedRecords.clear();
            clearTable();
            fetchData();
        });
    }

    private void setupLoadMoreButton() {
        loadMoreButton.setOnClickListener(v -> {
            currentPage++;
            fetchData();
        });
    }

    private void setupExportButton() {
        exportButton.setOnClickListener(v -> exportToPdf());
    }

    private void initializeDatabase() {
        try {
            connection = new ConnectionHelper().conclass();
            if (connection != null) {
                mainHandler.post(this::fetchData);
            } else {
                showError("فشل الاتصال بقاعدة البيانات");
            }
        } catch (Exception e) {
            showError("خطأ في الاتصال: " + e.getMessage());
        }
    }

    private void fetchData() {
        if (isLoading) return;
        isLoading = true;

        loadingProgress.setVisibility(View.VISIBLE);
        loadMoreButton.setVisibility(View.GONE);

        executor.execute(() -> {
            try {
                if (connection == null || connection.isClosed()) {
                    connection = new ConnectionHelper().conclass();
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
        query.append("    SELECT *, ROW_NUMBER() OVER (ORDER BY Purchases_date DESC) AS RowNum");
        query.append("    FROM Purchases_table WHERE 1=1");
        query.append("    AND Purchases_date BETWEEN ? AND ?");

        String searchType = searchTypeSpinner.getText().toString();
        String searchValue = searchEdit.getText().toString().trim();

        if (!searchValue.isEmpty()) {
            if (searchType.equals("تقرير حسب المنتج")) {
                query.append(" AND Purchases_product_name LIKE ?");
            } else if (searchType.equals("تقرير حسب المورد")) {
                query.append(" AND Purchases_mwared_name LIKE ?");
            }
        }

        query.append(")");
        query.append(" SELECT * FROM NumberedRows");
        query.append(" WHERE RowNum BETWEEN ? AND ?");

        return query.toString();
    }

    private void setQueryParameters(PreparedStatement ps) throws Exception {
        int paramIndex = 1;
        ps.setString(paramIndex++, fromDateEdit.getText().toString());
        ps.setString(paramIndex++, toDateEdit.getText().toString());

        String searchType = searchTypeSpinner.getText().toString();
        String searchValue = searchEdit.getText().toString().trim();

        if (!searchValue.isEmpty() && !searchType.equals("تقرير المشتريات اليومي")) {
            ps.setString(paramIndex++, "%" + searchValue + "%");
        }

        int startRow = (currentPage * PAGE_SIZE) + 1;
        int endRow = startRow + PAGE_SIZE - 1;
        ps.setInt(paramIndex++, startRow);
        ps.setInt(paramIndex, endRow);
    }

    private void processResultSet(ResultSet rs) throws Exception {
        List<PurchaseRecord> newRecords = new ArrayList<>();
        double totalPurchases = 0;
        double totalPaid = 0;
        double totalRemaining = 0;
        int count = 0;

        while (rs.next() && count < PAGE_SIZE) {
            PurchaseRecord record = new PurchaseRecord(rs);
            newRecords.add(record);

            double total = record.getTotal();
            totalPurchases += total;
            totalPaid += record.price_for_sell;
            totalRemaining +=0;

            count++;
        }

        final boolean hasMore = count == PAGE_SIZE;
        cachedRecords.addAll(newRecords);

        final double finalTotalPurchases = totalPurchases;
        final double finalTotalPaid = totalPaid;
        final double finalTotalRemaining = totalRemaining;

        mainHandler.post(() -> {
            for (PurchaseRecord record : newRecords) {
                addTableRow(record);
            }
            updateTotals(finalTotalPurchases, finalTotalPaid, finalTotalRemaining);
            loadMoreButton.setVisibility(hasMore ? View.VISIBLE : View.GONE);
        });
    }

    private void addTableRow(PurchaseRecord record) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        row.addView(createCell(String.valueOf(record.id)));
        row.addView(createCell(record.date));
        row.addView(createCell(record.supplierName));
        row.addView(createCell(record.productName));
        row.addView(createCell(String.format(Locale.US, "%.2f", record.quantity)));
        row.addView(createCell(String.format(Locale.US, "%.2f", record.unitPrice)));
        row.addView(createCell(String.format(Locale.US, "%.2f", record.price_for_sell)));

        row.addView(createCell(record.notes != null ? record.notes : ""));

        tableLayout.addView(row);
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

    private void updateTotals(double totalPurchases, double totalPaid, double totalRemaining) {
        totalPurchasesText.setText(String.format(Locale.US, "%.2f", totalPurchases));
        totalPaidText.setText(String.format(Locale.US, "%.2f", totalPaid));
        totalRemainingText.setText(String.format(Locale.US, "%.2f", totalRemaining));
    }

    private void exportToPdf() {
        executor.execute(() -> {
            try {
                String fileName = "purchases_report_" + dateFormat.format(new Date()) + ".pdf";
                File pdfFile = new File(getExternalFilesDir(null), fileName);

                Document document = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                document.open();

                BaseFont baseFont = BaseFont.createFont("assets/fonts/arial.ttf",
                        BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                Font arabicFont = new Font(baseFont, 12);
                Font headerFont = new Font(baseFont, 14, Font.BOLD);

                Paragraph title = new Paragraph("تقرير المشتريات", headerFont);
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

                String[] headers = {"رقم الفاتورة", "التاريخ", "اسم المورد", "المنتج",
                        "الكمية", "السعر", "الاجمالي", "ملاحظات"};

                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setPadding(8);
                    pdfTable.addCell(cell);
                }

                for (PurchaseRecord record : cachedRecords) {
                    pdfTable.addCell(new Phrase(String.valueOf(record.id), arabicFont));
                    pdfTable.addCell(new Phrase(record.date, arabicFont));
                    pdfTable.addCell(new Phrase(record.supplierName, arabicFont));
                    pdfTable.addCell(new Phrase(record.productName, arabicFont));
                    pdfTable.addCell(new Phrase(String.format(Locale.US, "%.2f", record.quantity), arabicFont));
                    pdfTable.addCell(new Phrase(String.format(Locale.US, "%.2f", record.unitPrice), arabicFont));
                    pdfTable.addCell(new Phrase(String.format(Locale.US, "%.2f", record.price_for_sell), arabicFont));
                    pdfTable.addCell(new Phrase(record.notes != null ? record.notes : "", arabicFont));
                }

                document.add(pdfTable);

                Paragraph totals = new Paragraph(
                        String.format(Locale.US,
                                "إجمالي المشتريات: %.2f | إجمالي المدفوع: %.2f | إجمالي الباقي: %.2f",
                                Double.parseDouble(totalPurchasesText.getText().toString()),
                                Double.parseDouble(totalPaidText.getText().toString()),
                                Double.parseDouble(totalRemainingText.getText().toString())),
                        headerFont);
                totals.setAlignment(Element.ALIGN_CENTER);
                document.add(new Paragraph("\n"));
                document.add(totals);

                document.close();

                mainHandler.post(() -> Toast.makeText(this,
                        "تم حفظ التقرير في " + pdfFile.getAbsolutePath(),
                        Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                showError("خطأ في إنشاء التقرير: " + e.getMessage());
            }
        });
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
