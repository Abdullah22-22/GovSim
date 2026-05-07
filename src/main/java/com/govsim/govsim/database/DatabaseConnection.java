package com.govsim.govsim.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages database connections to MySQL server
 */
public class DatabaseConnection {
    
    private static final String URL = "jdbc:mysql://localhost:3306/govsim";
    private static final String USER = "root";
    private static final String PASSWORD = "12345";
    
    private static Connection connection;
    private static boolean initialized = false;
    
    /**
     * Establishes connection to the database and initializes it if needed
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            if (connection == null || connection.isClosed()) {
                // Initialize database on first connection
                if (!initialized) {
                    try {
                        DatabaseInitializer.initializeDatabase();
                    } catch (SQLException e) {
                        System.err.println("Initial setup failed, attempting to reinitialize...");
                        try {
                            DatabaseInitializer.reinitializeDatabase();
                        } catch (SQLException e2) {
                            System.err.println("Reinitialization failed: " + e2.getMessage());
                            throw e2;
                        }
                    }
                    initialized = true;
                }
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
            return connection;
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver not found", e);
        }
    }
    
    /**
     * Closes the database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Tests the database connection
     * @return true if connection successful
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
