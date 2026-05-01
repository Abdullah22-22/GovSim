package com.govsim.govsim.database;

import com.govsim.govsim.model.User;
import java.sql.*;

/** Data access object for User authentication and persistence */
public class UserDAO {

    /** Register a new user (returns false if username already exists) */
    public boolean register(User user) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try {
            PreparedStatement ps = DBManager.getConnection().prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.executeUpdate();

            // Retrieve generated user ID
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }

            System.out.println("[UserDAO] Registered: " + user.getUsername());
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            // Username already exists
            System.out.println("[UserDAO] Username already exists.");
            return false;

        } catch (Exception e) {
            System.out.println("[UserDAO] Register error: " + e.getMessage());
            return false;
        }
    }

    /** Authenticate user login (returns User if valid, otherwise null) */
    public User login(String username, String password) {
        String sql = "SELECT id, username, password FROM users WHERE username = ? AND password = ?";

        try {
            PreparedStatement ps = DBManager.getConnection().prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password")
                );
                user.setId(rs.getInt("id"));


                System.out.println("[UserDAO] Login success: " + username);
                return user;
            }

            System.out.println("[UserDAO] Wrong username or password.");

        } catch (Exception e) {
            System.out.println("[UserDAO] Login error: " + e.getMessage());
        }

        return null;
    }
}