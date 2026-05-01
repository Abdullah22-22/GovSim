package com.govsim.govsim.database;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Severity;
import java.sql.*;
import java.util.*;

/** Data access object for Event persistence */
public class EventDAO {

    /** Save all events for a user */
    public void saveAll(int userId, List<Event> events) {

        try {
            Connection conn = DBManager.getConnection();

            // Get city ID
            String citySql = "SELECT id FROM city_state WHERE user_id = ?";
            PreparedStatement getCityId = conn.prepareStatement(citySql);
            getCityId.setInt(1, userId);

            ResultSet rs = getCityId.executeQuery();
            if (!rs.next()) return;

            int cityId = rs.getInt("id");

            // Get ministers for mapping
            String minSql = "SELECT id, ministry FROM ministers WHERE city_id = ?";
            PreparedStatement getMinId = conn.prepareStatement(minSql);
            getMinId.setInt(1, cityId);

            ResultSet minRs = getMinId.executeQuery();

            Map<String, Integer> ministryToId = new HashMap<>();
            while (minRs.next()) {
                ministryToId.put(
                        minRs.getString("ministry"),
                        minRs.getInt("id")
                );
            }

            // Insert events
            String insertSql =
                    "INSERT INTO events_log (minister_id, day, ministry, severity, resolved) " +
                            "VALUES (?,?,?,?,?)";

            PreparedStatement ps = conn.prepareStatement(insertSql);

            for (Event e : events) {

                Integer minId = ministryToId.get(e.getMinistry());
                if (minId == null) continue;

                ps.setInt(1, minId);
                ps.setInt(2, e.getDay());
                ps.setString(3, e.getMinistry());
                ps.setString(4, e.getSeverity().name());
                ps.setBoolean(5, e.isResolved());

                ps.addBatch();
            }

            ps.executeBatch();

            System.out.println("[EventDAO] Saved " + events.size() + " events.");

        } catch (Exception e) {
            System.out.println("[EventDAO] Save error: " + e.getMessage());
        }
    }

    /** Delete all events for a user */
    public void deleteAll(int userId) {

        try {
            Connection conn = DBManager.getConnection();

            // Get city ID
            String sql = "SELECT id FROM city_state WHERE user_id = ?";
            PreparedStatement getCityId = conn.prepareStatement(sql);
            getCityId.setInt(1, userId);

            ResultSet rs = getCityId.executeQuery();
            if (!rs.next()) return;

            int cityId = rs.getInt("id");

            // Delete events
            String deleteSql =
                    "DELETE FROM events_log " +
                            "WHERE minister_id IN (SELECT id FROM ministers WHERE city_id = ?)";

            PreparedStatement ps = conn.prepareStatement(deleteSql);
            ps.setInt(1, cityId);

            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("[EventDAO] Delete error: " + e.getMessage());
        }
    }
}