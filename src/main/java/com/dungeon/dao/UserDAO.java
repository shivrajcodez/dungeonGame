package com.dungeon.dao;
import com.dungeon.model.User;
import com.dungeon.model.PlayerStats;
import com.dungeon.util.DBConnection;
import java.sql.*;

public class UserDAO {

    public User register(String username, String passwordHash, String email) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (username, password_hash, email) VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, username); ps.setString(2, passwordHash); ps.setString(3, email);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys(); rs.next();
            int userId = rs.getInt(1);
            DBConnection.close(rs, ps);

            ps = conn.prepareStatement(
                "INSERT INTO player_stats (user_id,current_room,hp,max_hp,mp,max_mp," +
                "attack,defense,level,xp,xp_next,gold) VALUES (?,1,100,100,50,50,10,5,1,0,100,50)");
            ps.setInt(1, userId); ps.executeUpdate(); DBConnection.close(ps);

            ps = conn.prepareStatement(
                "INSERT INTO inventory (user_id,item_id,quantity,equipped) VALUES (?,?,?,?)");
            int[][] starters = {{1,1,1},{6,1,1},{10,3,0}};
            for (int[] s : starters) {
                ps.setInt(1,userId); ps.setInt(2,s[0]); ps.setInt(3,s[1]); ps.setInt(4,s[2]);
                ps.addBatch();
            }
            ps.executeBatch(); DBConnection.close(ps);

            logEvent(conn, userId, "system", "Welcome to Dungeon Realm! Your adventure begins...");
            conn.commit();
            return new User(userId, username, email);
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    public User findByUsername(String username) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT id,username,password_hash,email,is_online FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setEmail(rs.getString("email"));
                    u.setOnline(rs.getBoolean("is_online"));
                    return u;
                }
            }
        }
        return null;
    }

    public void setOnline(int userId, boolean online) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE users SET is_online=?, last_login=NOW() WHERE id=?")) {
            ps.setBoolean(1, online); ps.setInt(2, userId); ps.executeUpdate();
        }
    }

    public PlayerStats loadStats(int userId) throws SQLException {
        String sql = "SELECT ps.*, r.name AS room_name, u.username " +
                     "FROM player_stats ps " +
                     "JOIN rooms r ON r.id=ps.current_room " +
                     "JOIN users u ON u.id=ps.user_id " +
                     "WHERE ps.user_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapStats(rs);
            }
        }
        return null;
    }

    public void saveStats(PlayerStats s) throws SQLException {
        String sql = "UPDATE player_stats SET current_room=?,hp=?,max_hp=?,mp=?,max_mp=?," +
                     "attack=?,defense=?,level=?,xp=?,xp_next=?,gold=?,kills=?,deaths=?," +
                     "in_combat=?,current_monster_id=? WHERE user_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1,s.getCurrentRoom()); ps.setInt(2,s.getHp()); ps.setInt(3,s.getMaxHp());
            ps.setInt(4,s.getMp()); ps.setInt(5,s.getMaxMp()); ps.setInt(6,s.getAttack());
            ps.setInt(7,s.getDefense()); ps.setInt(8,s.getLevel()); ps.setInt(9,s.getXp());
            ps.setInt(10,s.getXpNext()); ps.setInt(11,s.getGold()); ps.setInt(12,s.getKills());
            ps.setInt(13,s.getDeaths()); ps.setBoolean(14,s.isInCombat());
            if (s.getCurrentMonsterId() != null) ps.setInt(15, s.getCurrentMonsterId());
            else ps.setNull(15, Types.INTEGER);
            ps.setInt(16, s.getUserId());
            ps.executeUpdate();
        }
    }

    private PlayerStats mapStats(ResultSet rs) throws SQLException {
        PlayerStats s = new PlayerStats();
        s.setId(rs.getInt("id")); s.setUserId(rs.getInt("user_id"));
        s.setCurrentRoom(rs.getInt("current_room")); s.setHp(rs.getInt("hp"));
        s.setMaxHp(rs.getInt("max_hp")); s.setMp(rs.getInt("mp"));
        s.setMaxMp(rs.getInt("max_mp")); s.setAttack(rs.getInt("attack"));
        s.setDefense(rs.getInt("defense")); s.setLevel(rs.getInt("level"));
        s.setXp(rs.getInt("xp")); s.setXpNext(rs.getInt("xp_next"));
        s.setGold(rs.getInt("gold")); s.setKills(rs.getInt("kills"));
        s.setDeaths(rs.getInt("deaths")); s.setInCombat(rs.getBoolean("in_combat"));
        int m = rs.getInt("current_monster_id");
        s.setCurrentMonsterId(rs.wasNull() ? null : m);
        s.setRoomName(rs.getString("room_name")); s.setUsername(rs.getString("username"));
        return s;
    }

    private void logEvent(Connection conn, int userId, String type, String msg) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO game_log (user_id,event_type,message) VALUES (?,?,?)");
        ps.setInt(1,userId); ps.setString(2,type); ps.setString(3,msg);
        ps.executeUpdate(); DBConnection.close(ps);
    }
}
