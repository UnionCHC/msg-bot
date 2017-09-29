package com.desprice.unionchc.sqlite;

import com.desprice.unionchc.entity.UserStep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.desprice.unionchc.Utils.logException;

public class TStep {


    private static TStep ourInstance = new TStep();

    public static TStep getInstance() {
        return ourInstance;
    }

    private static final String TABLE_STEP = "step";

    private Connection mConnection = SQLite.getInstance().getConnection();


    private void executeSql(String sql) {
        SQLite.getInstance().executeSql(mConnection, sql);
    }


    public void checkTable() {
        if (!SQLite.getInstance().checkTableExists(TABLE_STEP))
            createTable();
    }

    private void createTable() {
        System.out.println("create table: " + TABLE_STEP);
        String sql = " CREATE TABLE " + TABLE_STEP + "(" + UserStep.getFieldCreate() + " );";
        executeSql(sql);
        sql = "CREATE UNIQUE INDEX idx_" + TABLE_STEP + "_user_bot ON " + TABLE_STEP + " (" + UserStep.USER_ID + " , " + UserStep.BOT_ID + ");";
        executeSql(sql);
    }


    public void updateStep(UserStep userStep, int step) {
        userStep.step = step;
        updateStep(userStep);
    }

    public void updateStep(UserStep step) {
        String sql = "INSERT OR REPLACE INTO " + TABLE_STEP + "(" +
                UserStep.USER_ID + " , " +
                UserStep.BOT_ID + " , " +
                UserStep.STEP + " ,  " +
                UserStep.VALUE + "  " +
                ") VALUES (?, ?, ? ,?)";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setLong(1, step.userId);
            ps.setInt(2, step.botId);
            ps.setInt(3, step.step);
            ps.setLong(4, step.value);
            int numRowsInserted = ps.executeUpdate();
            System.out.println("updateStep numRowsInserted:" + numRowsInserted);
        } catch (SQLException ex) {
            logException(ex);
        }
    }


    public UserStep getStep(Long userId, int botid) {
        UserStep userStep = new UserStep(userId, botid);
        if (userId == 0) return userStep;
        String sql = "SELECT " + UserStep.STEP + "," + UserStep.VALUE + " FROM " + TABLE_STEP + " WHERE "
                + UserStep.USER_ID + " = ? AND "
                + UserStep.BOT_ID + " = ?";
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.setLong(1, userId);
            ps.setInt(2, botid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                userStep.step = rs.getInt(UserStep.STEP);
                userStep.value = rs.getLong(UserStep.VALUE);
                System.out.println("getStep: " + userStep.step);
            }

        } catch (SQLException ex) {
            logException(ex);
        }
        return userStep;

    }


}
