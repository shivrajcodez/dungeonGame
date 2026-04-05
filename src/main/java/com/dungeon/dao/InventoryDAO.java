package com.dungeon.dao;
import com.dungeon.model.InventoryItem;
import com.dungeon.util.DBConnection;
import java.sql.*;
import java.util.*;

public class InventoryDAO {
    private static final String JOIN_SQL =
        "SELECT inv.*, i.name, i.description, i.item_type, i.attack_bonus, " +
        "i.defense_bonus, i.hp_restore, i.mp_restore, i.gold_value, i.rarity, i.icon " +
        "FROM inventory inv JOIN items i ON i.id=inv.item_id ";

    public List<InventoryItem> findByUser(int userId) throws SQLException {
        List<InventoryItem> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(JOIN_SQL + "WHERE inv.user_id=? ORDER BY i.item_type,i.name")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public InventoryItem findUserItem(int userId, int itemId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(JOIN_SQL + "WHERE inv.user_id=? AND inv.item_id=?")) {
            ps.setInt(1, userId); ps.setInt(2, itemId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    public void addItem(int userId, int itemId, int qty) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO inventory (user_id,item_id,quantity) VALUES (?,?,?) " +
                 "ON DUPLICATE KEY UPDATE quantity=quantity+?")) {
            ps.setInt(1,userId); ps.setInt(2,itemId); ps.setInt(3,qty); ps.setInt(4,qty);
            ps.executeUpdate();
        }
    }

    public void consumeItem(int userId, int itemId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement ps = c.prepareStatement(
                "UPDATE inventory SET quantity=quantity-1 WHERE user_id=? AND item_id=?");
            ps.setInt(1,userId); ps.setInt(2,itemId); ps.executeUpdate(); DBConnection.close(ps);
            ps = c.prepareStatement("DELETE FROM inventory WHERE user_id=? AND item_id=? AND quantity<=0");
            ps.setInt(1,userId); ps.setInt(2,itemId); ps.executeUpdate(); DBConnection.close(ps);
        }
    }

    public void equip(int userId, int itemId, String itemType) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement ps = c.prepareStatement(
                "UPDATE inventory inv JOIN items i ON i.id=inv.item_id SET inv.equipped=0 " +
                "WHERE inv.user_id=? AND i.item_type=?");
            ps.setInt(1,userId); ps.setString(2,itemType); ps.executeUpdate(); DBConnection.close(ps);
            ps = c.prepareStatement("UPDATE inventory SET equipped=1 WHERE user_id=? AND item_id=?");
            ps.setInt(1,userId); ps.setInt(2,itemId); ps.executeUpdate(); DBConnection.close(ps);
        }
    }

    public void unequip(int userId, int itemId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE inventory SET equipped=0 WHERE user_id=? AND item_id=?")) {
            ps.setInt(1,userId); ps.setInt(2,itemId); ps.executeUpdate();
        }
    }

    private InventoryItem map(ResultSet rs) throws SQLException {
        InventoryItem ii = new InventoryItem();
        ii.setInventoryId(rs.getInt("id")); ii.setUserId(rs.getInt("user_id"));
        ii.setItemId(rs.getInt("item_id")); ii.setQuantity(rs.getInt("quantity"));
        ii.setEquipped(rs.getBoolean("equipped")); ii.setName(rs.getString("name"));
        ii.setDescription(rs.getString("description")); ii.setItemType(rs.getString("item_type"));
        ii.setAttackBonus(rs.getInt("attack_bonus")); ii.setDefenseBonus(rs.getInt("defense_bonus"));
        ii.setHpRestore(rs.getInt("hp_restore")); ii.setMpRestore(rs.getInt("mp_restore"));
        ii.setGoldValue(rs.getInt("gold_value")); ii.setRarity(rs.getString("rarity"));
        ii.setIcon(rs.getString("icon"));
        return ii;
    }
}
