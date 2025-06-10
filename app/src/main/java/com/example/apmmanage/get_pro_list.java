package com.example.apmmanage;

        import java.sql.Connection;
        import java.sql.ResultSet;
        import java.sql.Statement;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.regex.Pattern;
        import java.util.regex.Matcher;

/**
 * Created by Alaeddin on 5/21/2017.
 */

public class get_pro_list {

    Connection connect;
    String ConnectionResult = "";
    Boolean isSuccess = false;

    public List<Map<String,String>> doInBackground(String product_name) {

        List<Map<String, String>> data = null;
        data = new ArrayList<Map<String, String>>();
        try
        {
            ConnectionHelper conStr=new ConnectionHelper();
            connect =conStr.conclass();        // Connect to database
            if (connect == null)
            {
                ConnectionResult = "Check Your Internet Access!";
            }
            else
            {
                String query;
                if (product_name=="")
                {
                    query = "select * from products_table WHERE(pro_count>0) ORDER BY pro_name";

                }
                else
                {
                    Pattern pattern = Pattern.compile("^[0-9]+$");
                    Matcher matcher = pattern.matcher(product_name);

                    if (matcher.matches()) {
                        // Handle case where productName contains only numbers
                        query = "SELECT * FROM products_table WHERE pro_count > 0 AND pro_int_code = '" + product_name + "'";
                    } else {
                        // Handle case where productName contains other characters
                        query = "SELECT * FROM products_table WHERE pro_count > 0 AND pro_name LIKE '%" + product_name + "%'";
                    }
                    // Change below query according to your own database.

                   // query = "select * from products_table WHERE(pro_count>0) AND pro_name LIKE '%'+ '" + product_name + "' +'%' ORDER BY pro_name";
                }
                Statement stmt = connect.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()){
                    Map<String,String> datanum=new HashMap<String,String>();
                    datanum.put("pro_name",rs.getString("pro_name"));
                    datanum.put("pro_count",rs.getString("pro_count"));
                    datanum.put("pro_bee3",rs.getString("pro_bee3"));
                    datanum.put("pro_cost_price",rs.getString("pro_cost_price"));
                    datanum.put("pro_stock",rs.getString("pro_stock"));
                    datanum.put("pro_ID",rs.getString("pro_ID"));
                    data.add(datanum);
                }


                ConnectionResult = "successful";
                isSuccess=true;
                connect.close();
            }
        }
        catch (Exception ex)
        {
            isSuccess = false;
            ConnectionResult = ex.getMessage();
        }

        return data;
    }





}
