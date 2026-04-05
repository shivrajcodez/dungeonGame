package com.dungeon.dao;
import com.dungeon.model.ChatMessage;
import com.dungeon.util.DBConnection;
import java.sql.*;
import java.util.*;

public class ChatDAO {
    public void save(int userId, String username, String message, String channel) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO chat_messages (user_id,username,message,channel) VALUES (?,?,?,?)")) {
            ps.setInt(1,userId); ps.setString(2,username);
            ps.setString(3,message); ps.setString(4,channel); ps.executeUpdate();
        }
    }

    public List<ChatMessage> getRecent(String channel, int limit) throws SQLException {
        List<ChatMessage> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT * FROM chat_messages WHERE channel=? ORDER BY created_at DESC LIMIT ?")) {
            ps.setString(1,channel); ps.setInt(2,limit);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        Collections.reverse(list);
        return list;
    }

    public List<ChatMessage> pollSince(String channel, long sinceId) throws SQLException {
        List<ChatMessage> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT * FROM chat_messages WHERE channel=? AND id>? ORDER BY id ASC LIMIT 50")) {
            ps.setString(1,channel); ps.setLong(2,sinceId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    private ChatMessage map(ResultSet rs) throws SQLException {
        ChatMessage m = new ChatMessage();
        m.setId(rs.getLong("id")); m.setUserId(rs.getInt("user_id"));
        m.setUsername(rs.getString("username")); m.setMessage(rs.getString("message"));
        m.setChannel(rs.getString("channel"));
        m.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return m;
    }
}
