package com.example.apmmanage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MenuData {

    public static void initMenu(List<String> groupList, HashMap<String, ArrayList<String>> itemList) {
        groupList.clear();
        itemList.clear();

        groupList.add("إدارة الأصناف");
        itemList.put("إدارة الأصناف", new ArrayList<String>() {{
            add("اضافة صنف جديد");
            add("قائمة الأصناف");
//            add("تعديل صنف");
//            add("ادخال فئة جديده");
//            add("فئات الاصناف");
//            add("تعديل فئة");
//            add("النواقص");
//            add("طباعة الباركود");
            add("عرض الهالك");
            add("الجرد بالباركود");
        }});



//        groupList.add("العروض");
//        itemList.put("العروض", new ArrayList<String>() {{
//            add("إضافة عرض");
//            add("قائمة العروض");
//            add("طباعة الجرد");
//        }});

        groupList.add("المبيعات");
        itemList.put("المبيعات", new ArrayList<String>() {{
            add("فاتورة بيع");
            add("قائمة المبيعات");
            add("مرتجع بيع");
            add("قائمة المرتجعات");
            add("طباعة اخر فاتورة");
            add("فاتورة بيع يدوي");
            add("فاتورة تسعير");
            add("عرض فواتير التسعير");
        }});

//        groupList.add("ادارة الانتاج");
//        itemList.put("ادارة الانتاج", new ArrayList<String>() {{
//            add("معادلة جديدة");
//            add("عرض معادلات الانتاج");
//            add("فاتورة مسحوبات");
//            add("تقارير المسحوبات");
//            add("امر شغل جديد");
//            add("تقارير اوامر الشغل");
//            add("مصروف امر شغل");
//        }});
//
//        groupList.add("التوريدات");
//        itemList.put("التوريدات", new ArrayList<String>() {{
//            add("فاتورة توريد من الانتاج");
//            add("تقارير التوريدات");
//        }});
//
//        groupList.add("ادارة الصيانة");
//        itemList.put("ادارة الصيانة", new ArrayList<String>() {{
//            add("فاتورة استلام صيانة");
//            add("تسليم صيانة");
//            add("تقارير الصيانة");
//            add("مسحوبات الصيانة");
//        }});

//        groupList.add("ادارة التحويلات");
//        itemList.put("ادارة التحويلات", new ArrayList<String>() {{
//            add("انشاء امر تحويل");
//            add("عرض اوامر التحويل");
//        }});

        groupList.add("الارباح");
        itemList.put("الارباح", new ArrayList<String>() {{
            add("عرض الارباح");
        }});
        groupList.add("المشتريات");
        itemList.put("المشتريات", new ArrayList<String>() {{
            add("فاتورة شراء");
            add("قائمة المشتريات");
            add("مرتجع شراء");
            add("قائمة المرتجعات");
        }});



        groupList.add("العملاء");
        itemList.put("العملاء", new ArrayList<String>() {{
           // add("اضافة عميل جديد");
            add("قائمة العملاء");
        }});

        groupList.add("إدارة المدينين");
        itemList.put("إدارة المدينين", new ArrayList<String>() {{
            add("قائمة المدينين");
            add("تفاصيل مدين");
//            add("إضافة مدين");
        }});

        groupList.add("ادارة الموردين");
        itemList.put("ادارة الموردين", new ArrayList<String>() {{
           // add("اضافة مورد جديد");
            add("قائمة الموردين");
        }});

        groupList.add("إدارة الدائنين");
        itemList.put("إدارة الدائنين", new ArrayList<String>() {{
            add("قائمة الدائنين");
            add("تفاصيل دائن");
//            add("إضافة دائن سابق");
        }});

        groupList.add("ادارة التقسيط");
        itemList.put("ادارة التقسيط", new ArrayList<String>() {{
            add("بيان حالة عميل");
            add("سداد قسط");
            add("حذف قسط");
            add("إحصائيات الأقساط");
            add("الأقساط المتأخرة");
            add("الأقساط المسددة");
            add("نموذج معرفة الاقساط");
            add("سداد مبكر");
            add("العمليات القائمة");
            add("العمليات المنتهية");
        }});



        groupList.add("ادارة الموظفين");
        itemList.put("ادارة الموظفين", new ArrayList<String>() {{
            add("إضافة موظف");
            add("قائمة الموظفين");
            add("حضور الموظفين");
            add("انصراف الموظفين");
            add("اضافي");
            add("قس");
            add("عمل مكافأة");
            add("عمل خصم");
            add("مراجعة حضور الموظفين");
        }});

        groupList.add("المسئولين");
        itemList.put("المسئولين", new ArrayList<String>() {{
            add("اضافة مسئول");
            add("قائمة المسئولين");
            add("مستحقات المسئولين");
        }});

        groupList.add("إدارة الخزينة");
        itemList.put("إدارة الخزينة", new ArrayList<String>() {{
            add("الوارد للخزينة");
            add("الصادر من الخزينة");
            add("إضافة مبلغ للخزينة");
            add("صرف مبلغ من الخزينة");
            add("صرف نسبة ادارة");
            add("حالة الخزينة");
//            add("تقفيل الخزينة");
//            add("انشاء اذن صرف مبلغ مالي");
//            add("مراجعة اذونات الصرف");
        }});

        groupList.add("إدارة المستخدمين");
        itemList.put("إدارة المستخدمين", new ArrayList<String>() {{
            add("إضافة مستخدم");
            add("عرض مستخدم");
            add("حذف مستخدم");
            add("تغيير المستخدم");
            add("تغيير كلمة السر");
            add("تعديل صلاحيات");
        }});
    }
}
