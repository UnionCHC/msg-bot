package com.desprice.unionchc.sqlite;


import com.desprice.unionchc.Options;

import java.sql.*;

public class SQLite {


    private static SQLite ourInstance = new SQLite();

    private Connection mConnection = null;

    public static SQLite getInstance() {
        return ourInstance;
    }

    private SQLite() {
        init();
    }

    public void init() {

        try {
            Class.forName("org.sqlite.JDBC");
            String path = Options.getInstance().getConfig().sqlite.get("path");
            if (null == path) {
                throw new SQLException("Not configure Db path ");
            }
            String dbURL = "jdbc:sqlite:" + path;

            mConnection = DriverManager.getConnection(dbURL);
            if (mConnection != null) {
                System.out.println("Connected to the database");
                DatabaseMetaData dm = mConnection.getMetaData();
                System.out.println("Driver name: " + dm.getDriverName());
                System.out.println("Driver version: " + dm.getDriverVersion());
                System.out.println("Product name: " + dm.getDatabaseProductName());
                System.out.println("Product version: " + dm.getDatabaseProductVersion());

            }
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }


    public Connection getConnection() {
        return mConnection;
    }

    public void closeConnection() {
        try {
            mConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void checkTables() {
        TUsers.getInstance().checkTable();
        TStep.getInstance().checkTable();
    }

    public boolean checkTableExists(String tableName) {
        boolean result = false;
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name = ?";
        PreparedStatement ps = null;
        try {
            ps = mConnection.prepareStatement(sql);
            ps.setString(1, tableName);
            ResultSet resultSql = ps.executeQuery();
            if (resultSql.next())
                result = resultSql.isFirst();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public void executeSql(Connection connection, String sql) {
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}


