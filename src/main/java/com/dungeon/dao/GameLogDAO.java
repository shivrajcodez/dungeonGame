package com.dungeon.dao;
import com.dungeon.util.DBConnection;
import java.sql.*;
import java.util.*;

public class GameLogDAO {
    public void log(int userId, String eventType, String message) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO game_log (user_id,event_type,message) VALUES (?,?,?)")) {
            ps.setInt(1,userId); ps.setString(2,eventType); ps.setString(3,message);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("GameLog error: " + e.getMessage());
        }
    }

    public List<Map<String,String>> getRecent(int userId, int limit) throws SQLException {
        List<Map<String,String>> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT event_type,message,created_at FROM game_log " +
                 "WHERE user_id=? ORDER BY created_at DESC LIMIT ?")) {
            ps.setInt(1,userId); ps.setInt(2,limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,String> row = new LinkedHashMap<>();
                    row.put("type",    rs.getString("event_type"));
                    row.put("message", rs.getString("message"));
                    row.put("time",    rs.getString("created_at"));
                    list.add(row);
                }
            }
        }
        Collections.reverse(list);
        return list;
    }
}
