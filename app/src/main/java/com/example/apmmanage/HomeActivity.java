package com.example.apmmanage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.example.apmmanage.R;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private Toolbar toolbar;
    private ExpandableListView expandableListView;
    private ArabicMenuAdapter adapter;
    private ActionBarDrawerToggle toggle;

    // Now using ArrayList and HashMap<String, ArrayList<String>>
    private ArrayList<String> listGroup;
    private HashMap<String, ArrayList<String>> listItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        TextView user_txt=(TextView)findViewById(R.id.user_txt);
        user_txt.setText("مرحبا بك "+GlobalData.user_name);
        // 1) Toolbar setup
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2) Drawer + NavigationView + ExpandableListView
        drawerLayout = findViewById(R.id.drawer_layout);
        navView      = findViewById(R.id.nav_view);
        expandableListView = findViewById(R.id.expandableListView);

        // 3) Drawer toggle (requires strings in strings.xml)
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 4) Prepare data structures
        listGroup = new ArrayList<>();
        listItem  = new HashMap<>();

        // 5) Fill with your actual menu
        MenuData.initMenu(listGroup, listItem);

        // 6) Set up adapter
        adapter = new ArabicMenuAdapter(this, listGroup, listItem);
        expandableListView.setAdapter(adapter);

        // 7) Handle clicks
        expandableListView.setOnChildClickListener((parent, v, groupPos, childPos, id) -> {
            String group = listGroup.get(groupPos);
            String child = listItem.get(group).get(childPos);
            // TODO: Navigate based on 'group' and 'child'

            if (child.equals("قائمة الأصناف")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, products_page.class);
                startActivity(intent);
                return true;
            }
            if (child.equals("عرض الهالك")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, HalekShowActivity.class);
                startActivity(intent);
                return true;
            }

            if (child.equals("الجرد بالباركود")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, Barcode_activity.class);
                startActivity(intent);
                return true;
            }

            if (child.equals("فاتورة تسعير")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, PricingActivity.class);
                startActivity(intent);
                return true;
            }

            if (child.equals("اضافة صنف جديد")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, AddProductActivity.class);
                startActivity(intent);
                return true;
            }if (child.equals("قائمة المدينين")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, MadeenActivity.class);
                startActivity(intent);
                return true;
            }if (child.equals("قائمة الدائنين")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, Da2nActivity.class);
                startActivity(intent);
                return true;
            }
            if (child.equals("قائمة المبيعات")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, SalesReportActivity.class);
                startActivity(intent);
                return true;
            }if (child.equals("قائمة المشتريات")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, PurchasesReportActivity.class);
                startActivity(intent);
                return true;
            }if (child.equals("الوارد للخزينة")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, IncomeActivity.class);
                startActivity(intent);
                return true;
            }if (child.equals("الصادر من الخزينة")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, OutcomeActivity.class);
                startActivity(intent);
                return true;
            }if (child.equals("عرض الارباح")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, ProfitsActivity.class);
                startActivity(intent);
                return true;
            }
            if (child.equals("تفاصيل مدين")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, Mosta7akatShowActivity.class);
                startActivity(intent);
                return true;
            }

            if (child.equals("حالة الخزينة")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, SafeStatusActivity.class);
                startActivity(intent);
                return true;
            }

            if (child.equals("إضافة مبلغ للخزينة")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, AddToIncomeActivity.class);
                startActivity(intent);
                return true;
            }
            if (child.equals("فاتورة بيع")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, SellActivity.class);
                startActivity(intent);
                return true;
            }

            if (child.equals("صرف مبلغ من الخزينة")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, SarfFromOutcomeActivity.class);
                startActivity(intent);
                return true;
            }

            if (child.equals("سداد قسط")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, PayKestActivity.class);
                startActivity(intent);
                return true;
            }

            if (child.equals("إحصائيات الأقساط")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, AksatSummaryActivity.class);
                startActivity(intent);
                return true;
            }
            if (child.equals("قائمة العملاء")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, CustomersShowActivity.class);
                startActivity(intent);
                return true;
            }

            if (child.equals("قائمة الموردين")) {
                // TODO: replace with your actual ProductsListActivity
                Intent intent = new Intent(HomeActivity.this, SuppliersShowActivity.class);
                startActivity(intent);
                return true;
            }
            if (child.equals("العمليات القائمة")) {
                Intent intent = new Intent(HomeActivity.this, LoansUnderRevisionActivity.class);
                intent.putExtra("status_title", "تم الصرف"); // زي C#
                startActivity(intent);
                return true;
            }

            if (child.equals("العمليات المنتهية")) {
                Intent intent = new Intent(HomeActivity.this, LoansUnderRevisionActivity.class);
                intent.putExtra("status_title", "متسرب"); // زي C#
                startActivity(intent);
                return true;
            }

            if (child.equals("الأقساط المتأخرة")) {
                Intent intent = new Intent(HomeActivity.this, DelayedAksatActivity.class);
                intent.putExtra("title", "الاقساط المتأخرة"); // زي C#
                startActivity(intent);
                return true;
            }

            if (child.equals("الأقساط المسددة")) {
                Intent intent = new Intent(HomeActivity.this, DelayedAksatActivity.class);
                intent.putExtra("title", "الاقساط المسددة"); // زي C#
                startActivity(intent);
                return true;
            }

            if (child.equals("بيان حالة عميل")) {
                Intent intent = new Intent(HomeActivity.this, LoanAksatShowActivity.class);

                startActivity(intent);
                return true;
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }
}