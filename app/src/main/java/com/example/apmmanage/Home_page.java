package com.example.apmmanage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;

public class Home_page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        MaterialCardView pro_btn = findViewById(R.id.pro_btn);
        pro_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getApplicationContext(), products_page.class);

                startActivity(i);
            }
        });

        MaterialCardView barcode_scan = findViewById(R.id.barcode_scan);
        barcode_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iii = new Intent(getApplicationContext(), Barcode_activity.class);
                startActivity(iii);
            }
        });

        Button sales_btn=(Button) findViewById(R.id.sales_btn);
        sales_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent ii= new Intent(getApplicationContext(),AllSalesActivity.class);
//                // Toast.makeText(Home_page.this,"ssssssss",Toast.LENGTH_SHORT).show();
//                startActivity(ii);


            }
        });

        Button tas3eer_btn=(Button) findViewById(R.id.tas3eer_btn);
        tas3eer_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent pa= new Intent(getApplicationContext(),PricingActivity.class);
                // Toast.makeText(Home_page.this,"ssssssss",Toast.LENGTH_SHORT).show();
                startActivity(pa);


            }
        });

        Button reports_btn=(Button) findViewById(R.id.reports_btn);
        reports_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent pa= new Intent(getApplicationContext(),ReportsMenuActivity.class);
                // Toast.makeText(Home_page.this,"ssssssss",Toast.LENGTH_SHORT).show();
                startActivity(pa);


            }
        });
    }
}