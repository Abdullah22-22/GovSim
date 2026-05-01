package com.govsim.govsim.database;

import com.govsim.govsim.model.Report;
import java.sql.*;
import java.util.List;

/** Data access object for Report persistence */
public class ReportDAO {

    /** Save reports for one month */
    public void saveMonthly(int userId, List<Report> reports) {

        try {
            Connection conn = DBManager.getConnection();

            // Get city ID for the user
            String sql = "SELECT id FROM city_state WHERE user_id = ?";
            PreparedStatement getCityId = conn.prepareStatement(sql);
            getCityId.setInt(1, userId);

            ResultSet rs = getCityId.executeQuery();

            if (!rs.next()) return;

            int cityId = rs.getInt("id");

            // Insert reports
            String insertSql =
                    "INSERT INTO reports (city_id, ministry, type, month, total_events, resolved, rating, budget_end) " +
                            "VALUES (?,?,?,?,?,?,?,?)";

            PreparedStatement ps = conn.prepareStatement(insertSql);

            for (Report r : reports) {
                ps.setInt(1, cityId);
                ps.setString(2, r.getMinistry());
                ps.setString(3, "MONTHLY");
                ps.setInt(4, r.getMonth());
                ps.setInt(5, r.getTotalEvents());
                ps.setInt(6, r.getResolved());
                ps.setDouble(7, r.getRating());
                ps.setDouble(8, 0);

                ps.addBatch();
            }

            ps.executeBatch();

            System.out.println("[ReportDAO] Saved " + reports.size() + " reports.");

        } catch (Exception e) {
            System.out.println("[ReportDAO] Save error: " + e.getMessage());
        }
    }
}