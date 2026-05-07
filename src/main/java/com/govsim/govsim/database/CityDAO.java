package com.govsim.govsim.database;

import com.govsim.govsim.model.City;
import java.sql.*;

/** Data access object for City state persistence */
public class CityDAO {

    /** Save or update city state for a user */
    public void save(int userId, City city) {
        save(userId, city, 100, 100, 100, 100, 100, 1);
    }

    /** Save or update city state including ministry health values */
    public void save(int userId, City city,
                     int interiorHealth, int defenseHealth, int financeHealth,
                     int populationHealth, int healthHealth) {
        save(userId, city, interiorHealth, defenseHealth, financeHealth,
                populationHealth, healthHealth, 1);
    }

    /** Save or update city state including ministry health and current day */
    public void save(int userId, City city,
                     int interiorHealth, int defenseHealth, int financeHealth,
                     int populationHealth, int healthHealth, int currentDay) {
        try {
            Connection conn = DBManager.getConnection();

            // Ensure column exists (migration for existing DBs)
            try {
                conn.createStatement().executeUpdate(
                        "ALTER TABLE city_state ADD COLUMN IF NOT EXISTS current_day INT NOT NULL DEFAULT 1");
            } catch (Exception ignored) {}

            PreparedStatement check = conn.prepareStatement(
                    "SELECT id FROM city_state WHERE user_id = ?");
            check.setInt(1, userId);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE city_state SET month=?, year=?, current_day=?, budget=?, satisfaction=?, population=?, " +
                                "interior_health=?, defense_health=?, finance_health=?, population_health=?, health_health=? " +
                                "WHERE user_id=?");
                ps.setInt(1, city.getMonth());
                ps.setInt(2, city.getYear());
                ps.setInt(3, currentDay);
                ps.setDouble(4, city.getBudget());
                ps.setDouble(5, city.getSatisfaction());
                ps.setInt(6, city.getPopulation());
                ps.setInt(7, interiorHealth);
                ps.setInt(8, defenseHealth);
                ps.setInt(9, financeHealth);
                ps.setInt(10, populationHealth);
                ps.setInt(11, healthHealth);
                ps.setInt(12, userId);
                ps.executeUpdate();
            } else {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO city_state (user_id, month, year, current_day, budget, satisfaction, population, " +
                                "interior_health, defense_health, finance_health, population_health, health_health) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
                ps.setInt(1, userId);
                ps.setInt(2, city.getMonth());
                ps.setInt(3, city.getYear());
                ps.setInt(4, currentDay);
                ps.setDouble(5, city.getBudget());
                ps.setDouble(6, city.getSatisfaction());
                ps.setInt(7, city.getPopulation());
                ps.setInt(8, interiorHealth);
                ps.setInt(9, defenseHealth);
                ps.setInt(10, financeHealth);
                ps.setInt(11, populationHealth);
                ps.setInt(12, healthHealth);
                ps.executeUpdate();
            }

            System.out.println("[CityDAO] Saved. Day=" + currentDay);
        } catch (Exception e) {
            System.out.println("[CityDAO] Save error: " + e.getMessage());
        }
    }

    /** Load city state for a user (returns null if not found) */
    public City load(int userId) {
        String sql = "SELECT * FROM city_state WHERE user_id = ?";
        try {
            PreparedStatement ps = DBManager.getConnection().prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double savedBudget       = rs.getDouble("budget");
                double savedSatisfaction = rs.getDouble("satisfaction");
                int    savedPopulation   = rs.getInt("population");
                int    savedMonth        = rs.getInt("month");
                int    savedYear         = rs.getInt("year");

                // Create city directly with saved values — no loop needed
                City city = new City(savedBudget);
                city.setSatisfaction(savedSatisfaction);
                city.setPopulation(savedPopulation);

                // Advance month/year to match saved state
                while (city.getYear() < savedYear ||
                        (city.getYear() == savedYear && city.getMonth() < savedMonth)) {
                    city.nextMonth();
                }

                System.out.println("[CityDAO] Loaded: Month " + savedMonth + " Year " + savedYear
                        + " Budget=" + savedBudget + " Satisfaction=" + savedSatisfaction);
                return city;
            }
        } catch (Exception e) {
            System.out.println("[CityDAO] Load error: " + e.getMessage());
        }
        return null;
    }

    /** Load the saved current day for a user (returns 1 if not found) */
    public int loadCurrentDay(int userId) {
        try {
            PreparedStatement ps = DBManager.getConnection().prepareStatement(
                    "SELECT current_day FROM city_state WHERE user_id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("current_day");
        } catch (Exception e) {
            System.out.println("[CityDAO] loadCurrentDay error: " + e.getMessage());
        }
        return 1;
    }

    /** Load ministry health values — returns int[5]: interior, defense, finance, population, health */
    public int[] loadMinistryHealth(int userId) {
        int[] health = {100, 100, 100, 100, 100};
        try {
            PreparedStatement ps = DBManager.getConnection().prepareStatement(
                    "SELECT interior_health, defense_health, finance_health, population_health, health_health " +
                            "FROM city_state WHERE user_id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                health[0] = rs.getInt("interior_health");
                health[1] = rs.getInt("defense_health");
                health[2] = rs.getInt("finance_health");
                health[3] = rs.getInt("population_health");
                health[4] = rs.getInt("health_health");
            }
        } catch (Exception e) {
            System.out.println("[CityDAO] Health load error: " + e.getMessage());
        }
        return health;
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