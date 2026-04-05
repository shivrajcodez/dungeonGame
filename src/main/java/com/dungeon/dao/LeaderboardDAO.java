package com.dungeon.dao;
import com.dungeon.util.DBConnection;
import java.sql.*;
import java.util.*;

public class LeaderboardDAO {
    public List<Map<String,Object>> getTop(int limit) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM leaderboard LIMIT ?")) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> row = new LinkedHashMap<>();
                    row.put("rank",     rs.getInt("rank_pos"));
                    row.put("username", rs.getString("username"));
                    row.put("level",    rs.getInt("level"));
                    row.put("xp",       rs.getInt("xp"));
                    row.put("gold",     rs.getInt("gold"));
                    row.put("kills",    rs.getInt("kills"));
                    row.put("deaths",   rs.getInt("deaths"));
                    row.put("online",   rs.getBoolean("is_online"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    public List<Map<String,String>> getOnlinePlayers() throws SQLException {
        List<Map<String,String>> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT u.username, ps.level, r.name AS room_name " +
                 "FROM users u JOIN player_stats ps ON ps.user_id=u.id " +
                 "JOIN rooms r ON r.id=ps.current_room WHERE u.is_online=1")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,String> row = new LinkedHashMap<>();
                    row.put("username", rs.getString("username"));
                    row.put("level",    String.valueOf(rs.getInt("level")));
                    row.put("room",     rs.getString("room_name"));
                    list.add(row);
                }
            }
        }
        return list;
    }
}
