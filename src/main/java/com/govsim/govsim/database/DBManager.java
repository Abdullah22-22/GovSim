package com.govsim.govsim.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBManager {

    private  static  final String URL  = "jdbc:mysql://localhost:3306/govsim";
    private static final String USER = "root";
    private static final String PASS = "12345";

    private static Connection connection;

    public static Connection getConnection(){
        try{
            if(connection == null || connection.isClosed()){
                connection = DriverManager.getConnection(URL, USER, PASS);
            }
        }catch (Exception e) {
            System.out.println("[DB] Connection failed: " + e.getMessage());
        }
        return connection;
    }
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}