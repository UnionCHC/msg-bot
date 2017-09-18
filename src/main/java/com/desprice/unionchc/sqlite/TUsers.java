package com.desprice.unionchc.sqlite;

import com.desprice.unionchc.entity.BotUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TUsers {


    private static TUsers ourInstance = new TUsers();

    public static TUsers getInstance() {
        return ourInstance;
    }

    private static final String TABLE_USERS = "users";

    private Connection mConnection = SQLite.getInstance().getConnection();


    public void executeSql(String sql) {
        SQLite.getInstance().executeSql(mConnection, sql);
    }


    public void checkTable() {
        if (!SQLite.getInstance().checkTableExists(TABLE_USERS))
            createTableUsers();
    }

    private void createTableUsers() {
        System.out.println("create table: " + TABLE_USERS);
        String sql = " CREATE TABLE " + TABLE_USERS + "(" + BotUser.getFieldCreate() + " );";
        executeSql(sql);
        sql = "CREATE UNIQUE INDEX idx_" + TABLE_USERS + "_user ON " + TABLE_USERS + " (user_id);";
        executeSql(sql);

    }


    public void checkUser(BotUser user) {
        String sql = "SELECT * FROM " + TABLE_USERS + " WHERE " + BotUser.USER_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setLong(1, user.userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println(rs.getLong(BotUser.USER_ID) + "\t" +
                        rs.getString(BotUser.FIRSTNAME) + "\t");

            } else {
                addUser(user);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


    }

    private void addUser(BotUser user) {
        String sql = "INSERT OR REPLACE INTO " + TABLE_USERS + "(" +
                BotUser.CHAT_ID + " , " +
                BotUser.USER_ID + " , " +
                BotUser.FIRSTNAME + " , " +
                BotUser.LASTNAME + " , " +
                BotUser.USERNAME + "  " +
                ") VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setLong(1, user.userId);
            ps.setLong(2, user.userId);
            ps.setString(3, user.firstName);
            ps.setString(4, user.lastName);
            ps.setString(5, user.userName);
            int numRowsInserted = ps.executeUpdate();
            System.out.println("numRowsInserted:" + numRowsInserted);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }


    public BotUser getUser(Long userId) {
        BotUser botUser = new BotUser();
        String sql = "SELECT * FROM " + TABLE_USERS + " WHERE " + BotUser.USER_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                botUser.userId = rs.getLong(BotUser.USER_ID);
                botUser.firstName = rs.getString(BotUser.FIRSTNAME);
                botUser.lastName = rs.getString(BotUser.LASTNAME);
                botUser.userName = rs.getString(BotUser.USERNAME);
                botUser.wallet = rs.getString(BotUser.WALLET);
                botUser.address = rs.getString(BotUser.ADDRESS);
                botUser.password = rs.getString(BotUser.PASSWORD);

                System.out.println(rs.getLong(BotUser.USER_ID) + "\t" +
                   rs.getString(BotUser.FIRSTNAME) + "\t");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return botUser;
    }


    public void updateWallet(BotUser user) {
        String sql = "UPDATE " + TABLE_USERS + " SET " +
                BotUser.WALLET + " = ? " +
                " WHERE "+ BotUser.USER_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setString(1, user.wallet);
            ps.setLong(2, user.userId);
            int numRowsUpdate = ps.executeUpdate();
            System.out.println("updateWallet:" + numRowsUpdate);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateAddress(BotUser user) {
        String sql = "UPDATE " + TABLE_USERS + " SET " +
                BotUser.ADDRESS + " = ? " +
                " WHERE "+ BotUser.USER_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setString(1, user.address);
            ps.setLong(2, user.userId);
            int numRowsUpdate = ps.executeUpdate();
            System.out.println("updateAddress:" + numRowsUpdate);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updatePassword(BotUser user) {
        String sql = "UPDATE " + TABLE_USERS + " SET " +
                BotUser.PASSWORD + " = ? " +
                " WHERE "+ BotUser.USER_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setString(1, user.password);
            ps.setLong(2, user.userId);
            int numRowsUpdate = ps.executeUpdate();
            System.out.println("updatePassword:" + numRowsUpdate);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public void removeUser(Long userId) {
        String sql = "DELETE FROM " + TABLE_USERS +
                " WHERE "+ BotUser.USER_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setLong(1, userId);
            int numRowsUpdate = ps.executeUpdate();
            System.out.println("removeUser:" + numRowsUpdate);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


}
