package com.govsim.govsim.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Initializes the database schema on first connection
 */
public class DatabaseInitializer {
    
    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String DB_NAME = "govsim";
    private static final String USER = "root";
    private static final String PASSWORD = "2004";
    
    /**
     * Initializes database and creates tables if they don't exist
     * @throws SQLException if initialization fails
     */
    public static void initializeDatabase() throws SQLException {
        // First, create database if it doesn't exist
        createDatabaseIfNotExists();
        
        // Then create tables if they don't exist
        createTablesIfNotExist();
        
        System.out.println("✓ Database initialization complete");
    }
    
    /**
     * Reinitializes database by dropping and recreating all tables
     * @throws SQLException if reinitialization fails
     */
    public static void reinitializeDatabase() throws SQLException {
        String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME;
        
        try (Connection conn = DriverManager.getConnection(url, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("⚠ Dropping existing tables...");
            
            // Drop tables in reverse order of creation (due to foreign keys)
            stmt.executeUpdate("DROP TABLE IF EXISTS reports");
            stmt.executeUpdate("DROP TABLE IF EXISTS decisions");
            stmt.executeUpdate("DROP TABLE IF EXISTS events_log");
            stmt.executeUpdate("DROP TABLE IF EXISTS ministers");
            stmt.executeUpdate("DROP TABLE IF EXISTS city_state");
            stmt.executeUpdate("DROP TABLE IF EXISTS users");
            
            System.out.println("✓ Tables dropped");
            
        } catch (SQLException e) {
            System.err.println("✗ Error dropping tables: " + e.getMessage());
            throw e;
        }
        
        // Now create fresh tables
        createTablesIfNotExist();
        System.out.println("✓ Database reinitialized successfully");
    }
    
    /**
     * Creates the database if it doesn't exist
     */
    private static void createDatabaseIfNotExists() throws SQLException {
        String urlWithoutDB = "jdbc:mysql://" + HOST + ":" + PORT;
        
        try (Connection conn = DriverManager.getConnection(urlWithoutDB, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            String sql = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
            stmt.executeUpdate(sql);
            System.out.println("✓ Database '" + DB_NAME + "' ready");
            
        } catch (SQLException e) {
            System.err.println("✗ Error creating database: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Creates all required tables if they don't exist
     */
    private static void createTablesIfNotExist() throws SQLException {
        String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME;
        
        try (Connection conn = DriverManager.getConnection(url, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // 1. USERS TABLE
            String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            stmt.executeUpdate(createUsers);
            System.out.println("✓ Table 'users' ready");
            
            // 2. CITY_STATE TABLE
            String createCityState = "CREATE TABLE IF NOT EXISTS city_state (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "month INT NOT NULL DEFAULT 1, " +
                    "year INT NOT NULL DEFAULT 1, " +
                    "budget DOUBLE NOT NULL DEFAULT 1000000, " +
                    "satisfaction DOUBLE NOT NULL DEFAULT 100, " +
                    "population INT NOT NULL DEFAULT 100000, " +
                    "saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            stmt.executeUpdate(createCityState);
            System.out.println("✓ Table 'city_state' ready");
            
            // 3. MINISTERS TABLE
            String createMinisters = "CREATE TABLE IF NOT EXISTS ministers (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "city_id INT NOT NULL, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "ministry VARCHAR(50) NOT NULL, " +
                    "score DOUBLE NOT NULL DEFAULT 100, " +
                    "warnings INT NOT NULL DEFAULT 0, " +
                    "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', " +
                    "FOREIGN KEY (city_id) REFERENCES city_state(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            stmt.executeUpdate(createMinisters);
            System.out.println("✓ Table 'ministers' ready");
            
            // 4. EVENTS_LOG TABLE
            String createEventsLog = "CREATE TABLE IF NOT EXISTS events_log (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "minister_id INT NOT NULL, " +
                    "day INT NOT NULL, " +
                    "ministry VARCHAR(50) NOT NULL, " +
                    "severity ENUM('NORMAL', 'DANGEROUS') NOT NULL, " +
                    "resolved BOOLEAN NOT NULL DEFAULT FALSE, " +
                    "FOREIGN KEY (minister_id) REFERENCES ministers(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            stmt.executeUpdate(createEventsLog);
            System.out.println("✓ Table 'events_log' ready");
            
            // 5. DECISIONS TABLE
            String createDecisions = "CREATE TABLE IF NOT EXISTS decisions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "event_id INT NOT NULL, " +
                    "decision VARCHAR(100) NOT NULL, " +
                    "cost DOUBLE NOT NULL DEFAULT 0, " +
                    "outcome VARCHAR(255) DEFAULT 'PENDING', " +
                    "sat_impact DOUBLE DEFAULT 0, " +
                    "FOREIGN KEY (event_id) REFERENCES events_log(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            stmt.executeUpdate(createDecisions);
            System.out.println("✓ Table 'decisions' ready");
            
            // 6. REPORTS TABLE
            String createReports = "CREATE TABLE IF NOT EXISTS reports (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "city_id INT NOT NULL, " +
                    "ministry VARCHAR(50) NOT NULL, " +
                    "type ENUM('MONTHLY', 'ANNUAL') NOT NULL, " +
                    "month INT NOT NULL, " +
                    "total_events INT NOT NULL DEFAULT 0, " +
                    "resolved INT NOT NULL DEFAULT 0, " +
                    "rating DOUBLE NOT NULL DEFAULT 100, " +
                    "budget_end DOUBLE NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY (city_id) REFERENCES city_state(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            stmt.executeUpdate(createReports);
            System.out.println("✓ Table 'reports' ready");
            
        } catch (SQLException e) {
            System.err.println("✗ Error creating tables: " + e.getMessage());
            throw e;
        }
    }
}
