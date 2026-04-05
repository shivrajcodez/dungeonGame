package com.dungeon.dao;
import com.dungeon.model.Monster;
import com.dungeon.util.DBConnection;
import java.sql.*;
import java.util.*;

public class MonsterDAO {
    public List<Monster> findByLevel(int playerLevel) throws SQLException {
        List<Monster> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT * FROM monsters WHERE min_level <= ? ORDER BY min_level")) {
            ps.setInt(1, playerLevel + 2);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Monster findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM monsters WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    private Monster map(ResultSet rs) throws SQLException {
        Monster m = new Monster();
        m.setId(rs.getInt("id")); m.setName(rs.getString("name"));
        m.setDescription(rs.getString("description"));
        m.setHp(rs.getInt("hp")); m.setMaxHp(rs.getInt("hp"));
        m.setAttack(rs.getInt("attack")); m.setDefense(rs.getInt("defense"));
        m.setXpReward(rs.getInt("xp_reward")); m.setGoldReward(rs.getInt("gold_reward"));
        m.setMinLevel(rs.getInt("min_level"));
        int li = rs.getInt("loot_item_id"); m.setLootItemId(rs.wasNull() ? null : li);
        m.setLootChance(rs.getDouble("loot_chance")); m.setIcon(rs.getString("icon"));
        return m;
    }
}
