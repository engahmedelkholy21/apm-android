package com.example.apmmanage;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    // region --- Product Model ---
    public static class Product {
        private int proID;
        private String proName;
        private String proCategory;
        private double proCostPrice;
        private double proBee3;
        private int proCount;
        private int proLimit;
        private String proExpirationDate;
        private String proMadaFa3ala;
        private String proMarad;
        private String proMwared;
        private String proMwaredPhone;
        private String proAddedBy;
        private double nesbaSaydaly;
        private int proIntCode;
        private String proStock;
        private int proPiecesInPacket;
        private int proCountInPieces;
        private double proGomlaGomla;
        private double proBee3_2;
        private String lastGardDate;

        public Product() {}

        public int getProID() {
            return proID;
        }
        public void setProID(int proID) {
            this.proID = proID;
        }

        public String getProName() {
            return proName;
        }
        public void setProName(String proName) {
            this.proName = proName;
        }

        public String getProCategory() {
            return proCategory;
        }
        public void setProCategory(String proCategory) {
            this.proCategory = proCategory;
        }

        public double getProCostPrice() {
            return proCostPrice;
        }
        public void setProCostPrice(double proCostPrice) {
            this.proCostPrice = proCostPrice;
        }

        public double getProBee3() {
            return proBee3;
        }
        public void setProBee3(double proBee3) {
            this.proBee3 = proBee3;
        }

        public int getProCount() {
            return proCount;
        }
        public void setProCount(int proCount) {
            this.proCount = proCount;
        }

        public int getProLimit() {
            return proLimit;
        }
        public void setProLimit(int proLimit) {
            this.proLimit = proLimit;
        }

        public String getProExpirationDate() {
            return proExpirationDate;
        }
        public void setProExpirationDate(String proExpirationDate) {
            this.proExpirationDate = proExpirationDate;
        }

        public String getProMadaFa3ala() {
            return proMadaFa3ala;
        }
        public void setProMadaFa3ala(String proMadaFa3ala) {
            this.proMadaFa3ala = proMadaFa3ala;
        }

        public String getProMarad() {
            return proMarad;
        }
        public void setProMarad(String proMarad) {
            this.proMarad = proMarad;
        }

        public String getProMwared() {
            return proMwared;
        }
        public void setProMwared(String proMwared) {
            this.proMwared = proMwared;
        }

        public String getProMwaredPhone() {
            return proMwaredPhone;
        }
        public void setProMwaredPhone(String proMwaredPhone) {
            this.proMwaredPhone = proMwaredPhone;
        }

        public String getProAddedBy() {
            return proAddedBy;
        }
        public void setProAddedBy(String proAddedBy) {
            this.proAddedBy = proAddedBy;
        }

        public double getNesbaSaydaly() {
            return nesbaSaydaly;
        }
        public void setNesbaSaydaly(double nesbaSaydaly) {
            this.nesbaSaydaly = nesbaSaydaly;
        }

        public int getProIntCode() {
            return proIntCode;
        }
        public void setProIntCode(int proIntCode) {
            this.proIntCode = proIntCode;
        }

        public String getProStock() {
            return proStock;
        }
        public void setProStock(String proStock) {
            this.proStock = proStock;
        }

        public int getProPiecesInPacket() {
            return proPiecesInPacket;
        }
        public void setProPiecesInPacket(int proPiecesInPacket) {
            this.proPiecesInPacket = proPiecesInPacket;
        }

        public int getProCountInPieces() {
            return proCountInPieces;
        }
        public void setProCountInPieces(int proCountInPieces) {
            this.proCountInPieces = proCountInPieces;
        }

        public double getProGomlaGomla() {
            return proGomlaGomla;
        }
        public void setProGomlaGomla(double proGomlaGomla) {
            this.proGomlaGomla = proGomlaGomla;
        }

        public double getProBee3_2() {
            return proBee3_2;
        }
        public void setProBee3_2(double proBee3_2) {
            this.proBee3_2 = proBee3_2;
        }

        public String getLastGardDate() {
            return lastGardDate;
        }
        public void setLastGardDate(String lastGardDate) {
            this.lastGardDate = lastGardDate;
        }
    }
    // endregion

    // region --- DAO ---
    public static class ProductDAO {
        private Connection connection;

        public ProductDAO() {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            connection = new ConnectionHelper().conclass();
        }

        /**
         * Testing constructor allowing injection of a custom connection.
         */
        public ProductDAO(Connection connection) {
            this.connection = connection;
        }

        public boolean isProductExists(String name, String stock) throws SQLException {
            String sql = "SELECT pro_ID FROM products_table WHERE pro_name = ? AND pro_stock = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, stock);
            ResultSet rs = ps.executeQuery();
            boolean exists = rs.next();
            rs.close();
            ps.close();
            return exists;
        }

        public boolean isIntCodeExists(String code, String stock) throws SQLException {
            String sql = "SELECT pro_ID FROM products_table WHERE pro_int_code = ? AND pro_stock = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, code);
            ps.setString(2, stock);
            ResultSet rs = ps.executeQuery();
            boolean exists = rs.next();
            rs.close();
            ps.close();
            return exists;
        }

        public int insertProduct(Product p) throws SQLException {
            String sql = "INSERT INTO products_table (" +
                    "pro_name, pro_category, pro_cost_price, pro_bee3, pro_count, pro_limit, " +
                    "pro_expiration_date, pro_mada_fa3ala, pro_marad, pro_mwared, pro_mwared_phone, " +
                    "pro_added_by, nesba_saydaly, pro_int_code, pro_stock, pro_pieces_in_packet, " +
                    "pro_count_in_pieces, pro_gomla_gomla, pro_bee3_2, gard_status, last_gard_date" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, p.getProName());
            ps.setString(2, p.getProCategory());
            ps.setDouble(3, p.getProCostPrice());
            ps.setDouble(4, p.getProBee3());
            ps.setInt(5, p.getProCount());
            ps.setInt(6, p.getProLimit());
            ps.setString(7, p.getProExpirationDate());
            ps.setString(8, p.getProMadaFa3ala());
            ps.setString(9, p.getProMarad());
            ps.setString(10, p.getProMwared());
            ps.setString(11, p.getProMwaredPhone());
            ps.setString(12, p.getProAddedBy());
            ps.setDouble(13, p.getNesbaSaydaly());
            ps.setString(14, String.valueOf(p.getProIntCode()));
            ps.setString(15, p.getProStock());
            ps.setInt(16, p.getProPiecesInPacket());
            ps.setInt(17, p.getProCountInPieces());
            ps.setDouble(18, p.getProGomlaGomla());
            ps.setDouble(19, p.getProBee3_2());
            ps.setString(20, "تم الجرد");
            ps.setString(21, p.getLastGardDate());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0)
                throw new SQLException("Insertion failed, no rows affected.");

            ResultSet genKeys = ps.getGeneratedKeys();
            int id = 0;
            if (genKeys.next()) {
                id = genKeys.getInt(1);
            }
            genKeys.close();
            ps.close();

            if (p.getProIntCode() == 0) {
                String updateSql = "UPDATE products_table SET pro_int_code = ? WHERE pro_ID = ?";
                PreparedStatement ps2 = connection.prepareStatement(updateSql);
                ps2.setString(1, String.valueOf(id));
                ps2.setInt(2, id);
                ps2.executeUpdate();
                ps2.close();
            }

            return id;
        }

        public List<String> getAllStocks() throws SQLException {
            List<String> stocks = new ArrayList<>();
            String sql = "SELECT DISTINCT stock_name FROM stock_table";
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) stocks.add(rs.getString("stock_name"));
            rs.close();
            ps.close();
            return stocks;
        }

        public List<String> getAllCategories() throws SQLException {
            List<String> categories = new ArrayList<>();
            String sql = "SELECT DISTINCT Category_name FROM Category";
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) categories.add(rs.getString("Category_name"));
            rs.close();
            ps.close();
            return categories;
        }
    }
    // endregion

    // region --- Activity UI ---
    private EditText nameTxt, costTxt, bee3Txt, countTxt, limitTxt, intCodeTxt, basicProfitTxt, piecesInPacketTxt;
    private EditText expirationDateTxt, madaFa3alaTxt, maradTxt, mwaredTxt, mwaredPhoneTxt, addedByTxt;
    private Spinner stockSpinner, categorySpinner;
    private Button saveBtn;
    private ProductDAO dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        dao = new ProductDAO();
        initViews();
        populateSpinners();
        setupListeners();
    }

    private void initViews() {
        nameTxt = findViewById(R.id.nameTxt);
        costTxt = findViewById(R.id.costTxt);
        bee3Txt = findViewById(R.id.bee3Txt);
        countTxt = findViewById(R.id.countTxt);
        limitTxt = findViewById(R.id.limitTxt);
        intCodeTxt = findViewById(R.id.intCodeTxt);
        basicProfitTxt = findViewById(R.id.basicProfitTxt);
        piecesInPacketTxt = findViewById(R.id.piecesInPacketTxt);
        expirationDateTxt = findViewById(R.id.expirationDateTxt);
        madaFa3alaTxt = findViewById(R.id.madaFa3alaTxt);
        maradTxt = findViewById(R.id.maradTxt);
        mwaredTxt = findViewById(R.id.mwaredTxt);
        mwaredPhoneTxt = findViewById(R.id.mwaredPhoneTxt);
        addedByTxt = findViewById(R.id.addedByTxt);
        stockSpinner = findViewById(R.id.stockSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        saveBtn = findViewById(R.id.saveBtn);
    }

    private void populateSpinners() {
        try {
            stockSpinner.setAdapter(
                    new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dao.getAllStocks()));
            categorySpinner.setAdapter(
                    new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dao.getAllCategories()));
        } catch (SQLException e) {
            Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupListeners() {
        saveBtn.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        try {
            String name = nameTxt.getText().toString().trim();
            String stock = stockSpinner.getSelectedItem().toString();
            String category = categorySpinner.getSelectedItem().toString();
            double cost = costTxt.getText().toString().isEmpty() ? 0 : Double.parseDouble(costTxt.getText().toString());
            double sale = bee3Txt.getText().toString().isEmpty() ? 0 : Double.parseDouble(bee3Txt.getText().toString());
            int count = countTxt.getText().toString().isEmpty() ? 0 : Integer.parseInt(countTxt.getText().toString());
            int limit = limitTxt.getText().toString().isEmpty() ? 0 : Integer.parseInt(limitTxt.getText().toString());
            String expDate = expirationDateTxt.getText().toString();
            String mada = madaFa3alaTxt.getText().toString();
            String marad = maradTxt.getText().toString();
            String mwared = mwaredTxt.getText().toString();
            String mwaredPhone = mwaredPhoneTxt.getText().toString();
            String addedBy = addedByTxt.getText().toString();
            int intCode = intCodeTxt.getText().toString().isEmpty() ? 0 : Integer.parseInt(intCodeTxt.getText().toString());
            double nesba = basicProfitTxt.getText().toString().isEmpty() ? 0 : Double.parseDouble(basicProfitTxt.getText().toString());
            int pieces = piecesInPacketTxt.getText().toString().isEmpty() ? 1 : Integer.parseInt(piecesInPacketTxt.getText().toString());

            if (name.isEmpty()) {
                Toast.makeText(this, "Enter product name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (sale < cost) {
                Toast.makeText(this, "Sale price must be >= cost price", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dao.isProductExists(name, stock)) {
                Toast.makeText(this, "Product already exists", Toast.LENGTH_SHORT).show();
                return;
            }
            if (intCode != 0 && dao.isIntCodeExists(String.valueOf(intCode), stock)) {
                Toast.makeText(this, "Internal code already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            Product p = new Product();
            p.setProName(name);
            p.setProCategory(category);
            p.setProCostPrice(cost);
            p.setProBee3(sale);
            p.setProCount(count);
            p.setProLimit(limit);
            p.setProExpirationDate(expDate);
            p.setProMadaFa3ala(mada);
            p.setProMarad(marad);
            p.setProMwared(mwared);
            p.setProMwaredPhone(mwaredPhone);
            p.setProAddedBy(addedBy);
            p.setNesbaSaydaly(nesba);
            p.setProStock(stock);
            p.setProIntCode(intCode);
            p.setProPiecesInPacket(pieces);
            p.setProCountInPieces(pieces * count);
            p.setProGomlaGomla(sale * pieces);
            p.setProBee3_2(sale);
            p.setLastGardDate(expDate);

            int newId = dao.insertProduct(p);
            if (newId > 0) {
                Toast.makeText(this, "Product added successfully (ID=" + newId + ")", Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (SQLException e) {
            String err = "DB Error: " + e.getMessage();
            Toast.makeText(this, err, Toast.LENGTH_LONG).show();
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("DB Error", err);
            clipboard.setPrimaryClip(clip);
        } catch (NumberFormatException nfe) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }
    // endregion
}
