package com.example.apmmanage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class sql_connect_method {
    private static final String DB_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String CONNECTION_STRING_TEMPLATE = "jdbc:sqlserver://%s:%d;databaseName=%s;user=%s;password=%s;";
    private String ip;
    private String dbName;
    private int port;
    private String un;
    private String pw;

    public sql_connect_method(String ip, String dbName, int port, String un, String pw) {
        this.ip = ip;
        this.dbName = dbName;
        this.port = port;
        this.un = un;
        this.pw = pw;
    }

    public Connection getConnection() throws SQLException {
        String connectionString = String.format(CONNECTION_STRING_TEMPLATE, ip, port, dbName, un, pw);
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Could not load database driver: " + e.getMessage());
        }
        return DriverManager.getConnection(connectionString);
    }
}
