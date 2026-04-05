package com.dungeon.dao;
import com.dungeon.model.Item;
import com.dungeon.util.DBConnection;
import java.sql.*;

public class ItemDAO {
    public Item findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM items WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public static Item map(ResultSet rs) throws SQLException {
        Item i = new Item();
        i.setId(rs.getInt("id")); i.setName(rs.getString("name"));
        i.setDescription(rs.getString("description")); i.setItemType(rs.getString("item_type"));
        i.setAttackBonus(rs.getInt("attack_bonus")); i.setDefenseBonus(rs.getInt("defense_bonus"));
        i.setHpRestore(rs.getInt("hp_restore")); i.setMpRestore(rs.getInt("mp_restore"));
        i.setGoldValue(rs.getInt("gold_value")); i.setRarity(rs.getString("rarity"));
        i.setIcon(rs.getString("icon"));
        return i;
    }
}
