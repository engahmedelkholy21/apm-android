package com.example.apmmanage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class products_page extends AppCompatActivity {
    TextView pro_header;
    Typeface font;
    Button btn_Get_pro;
    ListView LV_Country;
    SimpleAdapter ADAhere;
    EditText pro_name_txt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_page);
        pro_header=(TextView) findViewById(R.id.pro_header);
        LV_Country=(ListView)findViewById(R.id.LV_Country);
        btn_Get_pro=(Button)findViewById(R.id.btn_Get_pro);
        pro_name_txt=(EditText)findViewById(R.id.pro_name_txt);

        btn_Get_pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Map<String,String>> MyData = null;
                get_pro_list mydata =new get_pro_list();


                MyData= mydata.doInBackground(pro_name_txt.getText().toString());
                String[] fromwhere = {"pro_name","pro_count","pro_bee3","pro_cost_price","pro_stock","pro_ID"};


                int[] viewswhere = {R.id.lb_pro_name,R.id.lb_pro_count_value,R.id.lb_sell_price_value,R.id.lb_taklefa_price_value,R.id.lb_stock_value,R.id.lb_item_code_value};
                ADAhere = new SimpleAdapter(products_page.this, MyData,R.layout.pro_list, fromwhere, viewswhere);

                LV_Country.setAdapter(ADAhere);

            }
        });


        LV_Country.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String,Object> obj=(HashMap<String,Object>)ADAhere.getItem(position);
                String ID=(String)obj.get("A");
                Toast.makeText(products_page.this, ID, Toast.LENGTH_SHORT).show();

            }
        });







    }
}