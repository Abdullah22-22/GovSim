package com.govsim.govsim.database;

import java.sql.Connection;

public class DBManager {

    public static Connection getConnection() {
        try {
            return DatabaseConnection.getConnection();
        } catch (Exception e) {
            System.out.println("[DB] Connection failed: " + e.getMessage());
            return null;
        }
    }

    public static void close() {
        DatabaseConnection.closeConnection();
    }
}