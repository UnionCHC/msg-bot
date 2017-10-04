package com.desprice.unionchc.sqlite;


import com.desprice.unionchc.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

import static com.desprice.unionchc.Utils.logException;

public class SQLite {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLite.class);

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
                LOGGER.debug("Connected to the database");
                DatabaseMetaData dm = mConnection.getMetaData();
                LOGGER.debug("Driver name: " + dm.getDriverName());
                LOGGER.debug("Driver version: " + dm.getDriverVersion());
                LOGGER.debug("Product name: " + dm.getDatabaseProductName());
                LOGGER.debug("Product version: " + dm.getDatabaseProductVersion());
            }
        } catch (ClassNotFoundException | SQLException ex) {
            logException(ex);
        }
    }

    public Connection getConnection() {
        return mConnection;
    }

    public void closeConnection() {
        try {
            mConnection.close();
        } catch (SQLException ex) {
            logException(ex);
        }
    }

    public void checkTables() {
        TUsers.getInstance().checkTable();
        TStep.getInstance().checkTable();
    }

    public boolean checkTableExists(String tableName) {
        boolean result = false;
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name = ?";
        PreparedStatement ps;
        try {
            ps = mConnection.prepareStatement(sql);
            ps.setString(1, tableName);
            ResultSet resultSql = ps.executeQuery();
            if (resultSql.next())
                result = resultSql.isFirst();
            ps.close();
        } catch (SQLException ex) {
            logException(ex);
        }
        return result;
    }


    public void executeSql(Connection connection, String sql) {
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
            ps.close();
        } catch (SQLException ex) {
            logException(ex);
        }
    }


}


