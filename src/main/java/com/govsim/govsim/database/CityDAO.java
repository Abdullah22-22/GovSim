package com.govsim.govsim.database;

import com.govsim.govsim.model.City;
import java.sql.*;

/** Data access object for City state persistence */
public class CityDAO {

    /** Save or update city state for a user */
    public void save(int userId, City city){
        try {
            Connection conn = DBManager.getConnection();

            // Check if a record already exists
            PreparedStatement check = conn.prepareStatement(
                    "SELECT id FROM city_state WHERE user_id = ?");
            check.setInt(1, userId);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                // Update existing record
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE city_state SET month=?, year=?, budget=?, satisfaction=?, population=? WHERE user_id=?");
                ps.setInt(1, city.getMonth());
                ps.setInt(2, city.getYear());
                ps.setDouble(3, city.getBudget());
                ps.setDouble(4, city.getSatisfaction());
                ps.setInt(5, city.getPopulation());
                ps.setInt(6, userId);
                ps.executeUpdate();
            } else {
                // Insert new record
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO city_state (user_id, month, year, budget, satisfaction, population) VALUES (?,?,?,?,?,?)");
                ps.setInt(1, userId);
                ps.setInt(2, city.getMonth());
                ps.setInt(3, city.getYear());
                ps.setDouble(4, city.getBudget());
                ps.setDouble(5, city.getSatisfaction());
                ps.setInt(6, city.getPopulation());
                ps.executeUpdate();
            }

            System.out.println("[CityDAO] Saved.");
        } catch (Exception e) {
            System.out.println("[CityDAO] Save error: " + e.getMessage());
        }
    }

    /** Load city state for a user (returns null if not found) */
    public City load(int userId){
        String sql = "SELECT * FROM city_state WHERE user_id = ?";
        try{
            PreparedStatement ps = DBManager.getConnection().prepareStatement(sql);
            ps.setInt(1,userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                City city = new City(rs.getDouble("budget"));
                city.setSatisfaction(rs.getDouble("satisfaction"));
                city.setPopulation(rs.getInt("population"));

                int targetMonth = rs.getInt("month");
                int targetYear = rs.getInt("year");

                // Advance simulation until saved date
                while (city.getYear() < targetYear ||
                        (city.getYear() == targetYear && city.getMonth() < targetMonth)) {
                    city.nextMonth();
                }

                System.out.println("[CityDAO] Loaded: Month " + targetMonth + " Year " + targetYear);
                return city;
            }
        } catch (Exception e) {
            System.out.println("[CityDAO] Load error: " + e.getMessage());
        }
        return null;
    }

    /** Delete saved city state (used on win/lose) */
    public void delete(int userId) {
        try {
            PreparedStatement ps = DBManager.getConnection().prepareStatement(
                    "DELETE FROM city_state WHERE user_id = ?");
            ps.setInt(1, userId);
            ps.executeUpdate();
            System.out.println("[CityDAO] Deleted.");
        } catch (Exception e) {
            System.out.println("[CityDAO] Delete error: " + e.getMessage());
        }
    }

    /** Check if a saved state exists for a user */
    public boolean hasSave(int userId) {
        try {
            PreparedStatement ps = DBManager.getConnection().prepareStatement(
                    "SELECT id FROM city_state WHERE user_id = ?");
            ps.setInt(1, userId);
            return ps.executeQuery().next();
        } catch (Exception e) {
            return false;
        }
    }
}