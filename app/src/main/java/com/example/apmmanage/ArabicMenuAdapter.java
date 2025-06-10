package com.example.apmmanage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ArabicMenuAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<String> groupList;
    private HashMap<String, ArrayList<String>> itemList;
    private HashMap<String, Integer> groupIcons;
    private HashMap<String, Integer> listIcons;

    public ArabicMenuAdapter(Context context,
                             ArrayList<String> groupList,
                             HashMap<String, ArrayList<String>> itemList) {
        this.context = context;
        this.groupList = groupList;
        this.itemList = itemList;
        setupGroupIcons();
       // setupListIcons();
    }

    private void setupGroupIcons() {
        groupIcons = new HashMap<>();
        groupIcons.put("إدارة الأصناف",     R.drawable.ic_manage_items);
        groupIcons.put("المخازن",          R.drawable.ic_warehouses);
        groupIcons.put("العروض",           R.drawable.ic_offers);
        groupIcons.put("المبيعات",         R.drawable.ic_sales);
        groupIcons.put("ادارة الانتاج",    R.drawable.ic_production);
        groupIcons.put("التوريدات",        R.drawable.ic_supplies);
        groupIcons.put("ادارة الصيانة",    R.drawable.ic_maintenance);
        groupIcons.put("ادارة التحويلات",  R.drawable.ic_transfers);
        groupIcons.put("المشتريات",        R.drawable.ic_purchases);
        groupIcons.put("الارباح",          R.drawable.ic_purchases);
        groupIcons.put("العملاء",          R.drawable.ic_customers);
        groupIcons.put("إدارة المدينين",   R.drawable.ic_debtors);
        groupIcons.put("ادارة الموردين",   R.drawable.ic_supplies);
        groupIcons.put("إدارة الدائنين",   R.drawable.ic_creditors);
        groupIcons.put("ادارة التقسيط",    R.drawable.ic_installments);
        groupIcons.put("العمليات",         R.drawable.ic_operations);
        groupIcons.put("ادارة الموظفين",   R.drawable.ic_employees);
        groupIcons.put("المسئولين",        R.drawable.ic_admins);
        groupIcons.put("إدارة الخزينة",    R.drawable.ic_treasury);
        groupIcons.put("إدارة المستخدمين", R.drawable.ic_users);


    }
//    private void setupListIcons() {
//        listIcons = new HashMap<>();
//
//        // إدارة الأصناف
//        listIcons.put("اضافة صنف جديد",      R.drawable.ic_add_item);
//        listIcons.put("قائمة الأصناف",       R.drawable.ic_list_items);
//        listIcons.put("عرض الهالك",          R.drawable.ic_damaged);
//        listIcons.put("الجرد بالباركود",     R.drawable.ic_barcode_inventory);
//
//        // المبيعات
//        listIcons.put("فاتورة بيع",          R.drawable.ic_sales_invoice);
//        listIcons.put("قائمة المبيعات",      R.drawable.ic_sales_list);
//        listIcons.put("مرتجع بيع",           R.drawable.ic_sales_return);
//        listIcons.put("قائمة المرتجعات",     R.drawable.ic_returns_list);
//        listIcons.put("طباعة اخر فاتورة",    R.drawable.ic_print_invoice);
//        listIcons.put("فاتورة بيع يدوي",     R.drawable.ic_manual_invoice);
//        listIcons.put("فاتورة تسعير",        R.drawable.ic_pricing_invoice);
//        listIcons.put("عرض فواتير التسعير",  R.drawable.ic_pricing_list);
//
//        // الارباح
//        listIcons.put("عرض الارباح",         R.drawable.ic_profits);
//
//        // المشتريات
//        listIcons.put("فاتورة شراء",         R.drawable.ic_purchase_invoice);
//        listIcons.put("قائمة المشتريات",     R.drawable.ic_purchase_list);
//        listIcons.put("مرتجع شراء",          R.drawable.ic_purchase_return);
//        listIcons.put("قائمة المرتجعات",     R.drawable.ic_returns_list);
//
//        // العملاء
//        listIcons.put("قائمة العملاء",        R.drawable.ic_customers_list);
//
//        // إدارة المدينين
//        listIcons.put("قائمة المدينين",       R.drawable.ic_debtors_list);
//        listIcons.put("تفاصيل مدين",         R.drawable.ic_debtor_details);
//
//        // ادارة الموردين
//        listIcons.put("قائمة الموردين",       R.drawable.ic_suppliers_list);
//
//        // إدارة الدائنين
//        listIcons.put("قائمة الدائنين",       R.drawable.ic_creditors_list);
//        listIcons.put("تفاصيل دائن",         R.drawable.ic_creditor_details);
//
//        // ادارة التقسيط
//        listIcons.put("بيان حالة عميل",      R.drawable.ic_client_status);
//        listIcons.put("سداد قسط",            R.drawable.ic_pay_installment);
//        listIcons.put("حذف قسط",             R.drawable.ic_delete_installment);
//        listIcons.put("إحصائيات الأقساط",    R.drawable.ic_installment_stats);
//        listIcons.put("الأقساط المتأخرة",    R.drawable.ic_late_installments);
//        listIcons.put("الأقساط المسددة",     R.drawable.ic_paid_installments);
//        listIcons.put("نموذج معرفة الاقساط", R.drawable.ic_installment_info);
//        listIcons.put("سداد مبكر",           R.drawable.ic_early_payment);
//        listIcons.put("العمليات القائمة",     R.drawable.ic_active_operations);
//        listIcons.put("العمليات المنتهية",    R.drawable.ic_completed_operations);
//
//        // ادارة الموظفين
//        listIcons.put("إضافة موظف",          R.drawable.ic_add_employee);
//        listIcons.put("قائمة الموظفين",      R.drawable.ic_employees_list);
//        listIcons.put("حضور الموظفين",       R.drawable.ic_attendance);
//        listIcons.put("انصراف الموظفين",     R.drawable.ic_departure);
//        listIcons.put("اضافي",               R.drawable.ic_overtime);
//        listIcons.put("قس",                  R.drawable.ic_salary_cut);
//        listIcons.put("عمل مكافأة",          R.drawable.ic_bonus);
//        listIcons.put("عمل خصم",             R.drawable.ic_deduction);
//        listIcons.put("مراجعة حضور الموظفين", R.drawable.ic_attendance_review);
//
//        // المسئولين
//        listIcons.put("اضافة مسئول",         R.drawable.ic_add_admin);
//        listIcons.put("قائمة المسئولين",     R.drawable.ic_admin_list);
//        listIcons.put("مستحقات المسئولين",   R.drawable.ic_admin_payments);
//
//        // إدارة الخزينة
//        listIcons.put("الوارد للخزينة",       R.drawable.ic_cash_in);
//        listIcons.put("الصادر من الخزينة",     R.drawable.ic_cash_out);
//        listIcons.put("إضافة مبلغ للخزينة",   R.drawable.ic_add_cash);
//        listIcons.put("صرف مبلغ من الخزينة",  R.drawable.ic_spend_cash);
//        listIcons.put("صرف نسبة ادارة",       R.drawable.ic_admin_percentage);
//        listIcons.put("حالة الخزينة",          R.drawable.ic_treasury_status);
//
//        // إدارة المستخدمين
//        listIcons.put("إضافة مستخدم",         R.drawable.ic_add_user);
//        listIcons.put("عرض مستخدم",           R.drawable.ic_view_user);
//        listIcons.put("حذف مستخدم",           R.drawable.ic_delete_user);
//        listIcons.put("تغيير المستخدم",        R.drawable.ic_switch_user);
//        listIcons.put("تغيير كلمة السر",       R.drawable.ic_change_password);
//        listIcons.put("تعديل صلاحيات",         R.drawable.ic_edit_permissions);
   // }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return itemList.get(groupList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return itemList.get(groupList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.menu_group_item, parent, false);
        }

        String title = (String) getGroup(groupPosition);
        TextView titleView = convertView.findViewById(R.id.groupTitle);
        ImageView iconView = convertView.findViewById(R.id.groupIcon);
        ImageView expandIcon = convertView.findViewById(R.id.expandIcon);

        titleView.setText(title);

        // Set group icon
        Integer iconRes = groupIcons.get(title);
        if (iconRes != null) {
            iconView.setImageResource(iconRes);
            iconView.setVisibility(View.VISIBLE);
        } else {
            iconView.setVisibility(View.GONE);
        }

        // Rotate expand icon based on group state
        float rotation = isExpanded ? 180f : 0f;
        expandIcon.setRotation(rotation);

        // Animate the rotation when group expands/collapses
        if (expandIcon.getTag() != null) {
            float previousRotation = (float) expandIcon.getTag();
            if (previousRotation != rotation) {
                RotateAnimation animation = new RotateAnimation(
                        previousRotation, rotation,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );
                animation.setDuration(300);
                animation.setFillAfter(true);
                expandIcon.startAnimation(animation);
            }
        }
        expandIcon.setTag(rotation);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.menu_child_item, parent, false);
        }

        String item = (String) getChild(groupPosition, childPosition);
        TextView titleView = convertView.findViewById(R.id.childTitle);
        titleView.setText(item);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}