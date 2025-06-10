package com.example.apmmanage;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionHelper {
    Connection connection;
    String ip,port,db,un,pass;

    @SuppressLint("NewApi")
    public Connection conclass(){

        ip=GlobalData.server;//"100.89.223.6";
        port="1433";
        db="my_data";
        un="me";
        pass="ahmed2010";
        StrictMode.ThreadPolicy tpolicy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(tpolicy);
        Connection con =null;
        String ConnectionURL=null;
        try
        {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL= "jdbc:jtds:sqlserver://"+ip+":"+port+";"+"databaseName="+ db+ ";user="+un+";password="+pass+";";

            con= DriverManager.getConnection(ConnectionURL);

        }
        catch (Exception ex)
        {
            Log.e("Error elkholy : ", ex.getMessage());
        }
        return con;
    }
}
