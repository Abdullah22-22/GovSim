package com.govsim.govsim.database;

import com.govsim.govsim.model.Decision;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Data access object for Decision persistence */
public class DecisionDAO {

    /** Save a single decision */
    public void save(int userId, Decision decision, double satImpact) {

        try {
            Connection conn = DBManager.getConnection();

            // Get city ID for the user
            String sql = "SELECT id FROM city_state WHERE user_id = ?";
            PreparedStatement getCityId = conn.prepareStatement(sql);
            getCityId.setInt(1, userId);

            ResultSet rs = getCityId.executeQuery();
            if (!rs.next()) return;

            int cityId = rs.getInt("id");

            // Get latest event ID
            String eventSql =
                    "SELECT id FROM events_log WHERE minister_id IN " +
                            "(SELECT id FROM ministers WHERE city_id = ?) " +
                            "ORDER BY id DESC LIMIT 1";

            PreparedStatement getEventId = conn.prepareStatement(eventSql);
            getEventId.setInt(1, cityId);

            ResultSet eventRs = getEventId.executeQuery();

            int eventId = 0;
            if (eventRs.next()) {
                eventId = eventRs.getInt("id");
            }

            // Insert decision
            String insertSql =
                    "INSERT INTO decisions (event_id, decision, cost, outcome, sat_impact) " +
                            "VALUES (?,?,?,?,?)";

            PreparedStatement ps = conn.prepareStatement(insertSql);

            ps.setInt(1, eventId);
            ps.setString(2, decision.getChoice());
            ps.setDouble(3, decision.getCost());
            ps.setString(4, decision.getOutcome());
            ps.setDouble(5, satImpact);

            ps.executeUpdate();

            System.out.println("[DecisionDAO] Saved decision: " + decision.getChoice());

        } catch (Exception e) {
            System.out.println("[DecisionDAO] Save error: " + e.getMessage());
        }
    }

    /** Load all decisions for a user */
    public List<Decision> loadAll(int userId) {

        List<Decision> decisions = new ArrayList<>();

        try {
            Connection conn = DBManager.getConnection();

            // Get city ID
            String sql = "SELECT id FROM city_state WHERE user_id = ?";
            PreparedStatement getCityId = conn.prepareStatement(sql);
            getCityId.setInt(1, userId);

            ResultSet rs = getCityId.executeQuery();
            if (!rs.next()) return decisions;

            int cityId = rs.getInt("id");

            // Load decisions
            String query =
                    "SELECT d.* FROM decisions d " +
                            "JOIN events_log e ON d.event_id = e.id " +
                            "JOIN ministers m ON e.minister_id = m.id " +
                            "WHERE m.city_id = ?";

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, cityId);

            ResultSet data = ps.executeQuery();

            while (data.next()) {
                Decision d = new Decision(
                        null,
                        data.getString("decision"),
                        data.getDouble("cost")
                );

                d.setOutcome(data.getString("outcome"));
                decisions.add(d);
            }

            System.out.println("[DecisionDAO] Loaded " + decisions.size() + " decisions.");

        } catch (Exception e) {
            System.out.println("[DecisionDAO] Load error: " + e.getMessage());
        }

        return decisions;
    }
}