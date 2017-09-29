package com.desprice.unionchc.sqlite;

import com.desprice.unionchc.entity.UserBot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.desprice.unionchc.Utils.logException;

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
        String sql = " CREATE TABLE " + TABLE_USERS + "(" + UserBot.getFieldCreate() + " );";
        executeSql(sql);
        sql = "CREATE UNIQUE INDEX idx_" + TABLE_USERS + "_user ON " + TABLE_USERS + " (user_id);";
        executeSql(sql);

    }


    public void checkUser(UserBot user) {
        String sql = "SELECT * FROM " + TABLE_USERS + " WHERE " + UserBot.USER_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setLong(1, user.userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println(rs.getLong(UserBot.USER_ID) + "\t" +
                        rs.getString(UserBot.FIRSTNAME) + "\t");
            } else {
                addUser(user);
            }
        } catch (SQLException ex) {
            logException(ex);
        }


    }

    private void addUser(UserBot user) {
        String sql = "INSERT OR REPLACE INTO " + TABLE_USERS + "(" +
                UserBot.CHAT_ID + " , " +
                UserBot.USER_ID + " , " +
                UserBot.FIRSTNAME + " , " +
                UserBot.LASTNAME + " , " +
                UserBot.USERNAME + "  " +
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
        } catch (SQLException ex) {
            logException(ex);
        }

    }

    public UserBot getUser(Long userId) {
        UserBot userBot = new UserBot();
        String sql = "SELECT * FROM " + TABLE_USERS + " WHERE " + UserBot.USER_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                userFromDb(userBot, rs);
            }
        } catch (SQLException ex) {
            logException(ex);
        }
        return userBot;
    }

    public UserBot getUserFromAddress(String address) {
        UserBot userBot = new UserBot();
        String sql = "SELECT * FROM " + TABLE_USERS + " WHERE " + UserBot.ADDRESS + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setString(1, address);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                userFromDb(userBot, rs);
            }
        } catch (SQLException ex) {
            logException(ex);
        }
        return userBot;
    }

    private void userFromDb(UserBot userBot, ResultSet rs) throws SQLException {
        userBot.userId = rs.getLong(UserBot.USER_ID);
        userBot.messageId = rs.getLong(UserBot.MESSAGE_ID);
        userBot.verify = rs.getInt(UserBot.VERIFY);
        userBot.firstName = rs.getString(UserBot.FIRSTNAME);
        userBot.lastName = rs.getString(UserBot.LASTNAME);
        userBot.userName = rs.getString(UserBot.USERNAME);
        userBot.wallet = rs.getString(UserBot.WALLET);
        userBot.address = rs.getString(UserBot.ADDRESS);
        userBot.password = rs.getString(UserBot.PASSWORD);
        System.out.println(rs.getLong(UserBot.USER_ID) + "\t" +
                rs.getString(UserBot.FIRSTNAME) + "\t");
    }

    public void updateWallet(UserBot user) {
        String sql = "UPDATE " + TABLE_USERS + " SET " +
                UserBot.WALLET + " = ? " +
                " WHERE " + UserBot.USER_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setString(1, user.wallet);
            ps.setLong(2, user.userId);
            int numRowsUpdate = ps.executeUpdate();
            System.out.println("updateWallet:" + numRowsUpdate);
        } catch (SQLException ex) {
            logException(ex);
        }
    }

    public void updateAddress(UserBot user) {
        String sql = "UPDATE " + TABLE_USERS + " SET " +
                UserBot.ADDRESS + " = ? " +
                " WHERE " + UserBot.USER_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setString(1, user.address);
            ps.setLong(2, user.userId);
            int numRowsUpdate = ps.executeUpdate();
            System.out.println("updateAddress:" + numRowsUpdate);
        } catch (SQLException ex) {
            logException(ex);
        }
    }

    public void updatePassword(UserBot user) {
        String sql = "UPDATE " + TABLE_USERS + " SET " +
                UserBot.PASSWORD + " = ? " +
                " WHERE " + UserBot.USER_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setString(1, user.password);
            ps.setLong(2, user.userId);
            int numRowsUpdate = ps.executeUpdate();
            System.out.println("updatePassword:" + numRowsUpdate);
        } catch (SQLException ex) {
            logException(ex);
        }
    }

    public void setMessage(UserBot user) {
        String sql = "UPDATE " + TABLE_USERS + " SET " +
                UserBot.MESSAGE_ID + " = ? " +
                " WHERE " + UserBot.USER_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setLong(1, user.messageId);
            ps.setLong(2, user.userId);
            int numRowsUpdate = ps.executeUpdate();
            System.out.println("updatePassword:" + numRowsUpdate);
        } catch (SQLException ex) {
            logException(ex);
        }
    }


    public void updateVerify(UserBot user) {
        String sql = "UPDATE " + TABLE_USERS + " SET " +
                UserBot.ADDRESS + " = ? ," +
                UserBot.PASSWORD + " = ? , " +
                UserBot.VERIFY + " = ? " +
                " WHERE " + UserBot.USER_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setString(1, user.address);
            ps.setString(2, user.password);
            ps.setLong(3, user.verify);
            ps.setLong(4, user.userId);
            int numRowsUpdate = ps.executeUpdate();
            System.out.println("updateVerify:" + numRowsUpdate);
        } catch (SQLException ex) {
            logException(ex);
        }
    }


    public void removeUser(Long userId) {
        String sql = "DELETE FROM " + TABLE_USERS +
                " WHERE " + UserBot.USER_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setLong(1, userId);
            int numRowsUpdate = ps.executeUpdate();
            System.out.println("removeUser:" + numRowsUpdate);
        } catch (SQLException ex) {
            logException(ex);
        }
    }


}
