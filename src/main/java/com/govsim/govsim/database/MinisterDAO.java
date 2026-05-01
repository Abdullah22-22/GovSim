package com.govsim.govsim.database;

import com.govsim.govsim.model.Minister;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Data access object for Minister persistence */
public class MinisterDAO {

    /** Save all ministers for a user (replaces existing data) */
    public void saveAll(int userId, List<Minister> ministers) {
        try {
            Connection conn = DBManager.getConnection();

            // Get city ID for the user
            PreparedStatement getCityId = conn.prepareStatement(
                    "SELECT id FROM city_state WHERE user_id = ?");
            getCityId.setInt(1, userId);
            ResultSet rs = getCityId.executeQuery();
            if (!rs.next()) return;
            int cityId = rs.getInt("id");

            // Delete existing ministers
            PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM ministers WHERE city_id = ?");
            del.setInt(1, cityId);
            del.executeUpdate();

            // Insert new ministers
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO ministers (city_id, name, ministry, score, warnings, status) VALUES (?,?,?,?,?,?)");

            for (Minister m : ministers) {
                ps.setInt(1, cityId);
                ps.setString(2, m.getName());
                ps.setString(3, m.getMinistry());
                ps.setDouble(4, m.getScore());
                ps.setInt(5, m.getWarnings());
                ps.setString(6, m.getStatus());
                ps.addBatch();
            }

            ps.executeBatch();
            System.out.println("[MinisterDAO] Saved " + ministers.size() + " ministers.");

        } catch (Exception e) {
            System.out.println("[MinisterDAO] Save error: " + e.getMessage());
        }
    }

    /** Load all ministers for a user */
    public List<Minister> loadAll(int userId) {
        List<Minister> ministers = new ArrayList<>();

        try {
            Connection conn = DBManager.getConnection();

            // Get city ID for the user
            PreparedStatement getCityId = conn.prepareStatement(
                    "SELECT id FROM city_state WHERE user_id = ?");
            getCityId.setInt(1, userId);
            ResultSet rs = getCityId.executeQuery();
            if (!rs.next()) return ministers;

            int cityId = rs.getInt("id");

            // Fetch ministers
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM ministers WHERE city_id = ?");
            ps.setInt(1, cityId);
            ResultSet data = ps.executeQuery();

            while (data.next()) {
                Minister m = new Minister(
                        data.getString("name"),
                        data.getString("ministry")
                );
                m.setScore(data.getDouble("score"));
                m.setWarnings(data.getInt("warnings"));
                m.setStatus(data.getString("status"));

                ministers.add(m);
            }

            System.out.println("[MinisterDAO] Loaded " + ministers.size() + " ministers.");

        } catch (Exception e) {
            System.out.println("[MinisterDAO] Load error: " + e.getMessage());
        }

        return ministers;
    }
}