package com.example.apmmanage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ReportsMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_menu);

        Button salesReportsBtn     = findViewById(R.id.salesReportsBtn);
        Button purchaseReportsBtn  = findViewById(R.id.purchaseReportsBtn);
        Button cashInBtn           = findViewById(R.id.cashInBtn);
        Button cashOutBtn          = findViewById(R.id.cashOutBtn);
        Button debtorsListBtn      = findViewById(R.id.debtorsListBtn);
        Button creditorsListBtn    = findViewById(R.id.creditorsListBtn);

        // Uncomment and implement when ReportViewerActivity exists:
        salesReportsBtn.setOnClickListener(v -> openReport("SALES"));
        purchaseReportsBtn.setOnClickListener(v -> openReport("PURCHASES"));
        cashInBtn.setOnClickListener(v -> openReport("INCOMING_TREASURY"));
        cashOutBtn.setOnClickListener(v -> openReport("OUTGOING_TREASURY"));
        debtorsListBtn.setOnClickListener(v -> openReport("DEBTORS"));
        creditorsListBtn.setOnClickListener(v -> openReport("CREDITORS"));
    }

    private void openReport(String type) {
//        Intent intent = new Intent(this, ReportViewerActivity.class);
//        intent.putExtra("REPORT_TYPE", type);
//        startActivity(intent);
    }
}
