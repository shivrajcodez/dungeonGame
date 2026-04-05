package com.dungeon.dao;
import com.dungeon.model.Room;
import com.dungeon.util.DBConnection;
import java.sql.*;

public class RoomDAO {
    public Room findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM rooms WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public static Room map(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setId(rs.getInt("id")); r.setName(rs.getString("name"));
        r.setDescription(rs.getString("description"));
        int n = rs.getInt("north_id"); r.setNorthId(rs.wasNull() ? null : n);
        int s = rs.getInt("south_id"); r.setSouthId(rs.wasNull() ? null : s);
        int e = rs.getInt("east_id");  r.setEastId(rs.wasNull()  ? null : e);
        int w = rs.getInt("west_id");  r.setWestId(rs.wasNull()  ? null : w);
        r.setRoomType(rs.getString("room_type"));
        r.setDangerLevel(rs.getInt("danger_level"));
        return r;
    }
}
