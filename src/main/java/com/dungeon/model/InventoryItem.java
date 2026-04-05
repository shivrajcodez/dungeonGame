package com.dungeon.model;

public class InventoryItem {
    private int inventoryId, userId, itemId, quantity;
    private int attackBonus, defenseBonus, hpRestore, mpRestore, goldValue;
    private boolean equipped;
    private String name, description, itemType, rarity, icon;

    public int getInventoryId() { return inventoryId; }
    public void setInventoryId(int i) { this.inventoryId = i; }
    public int getUserId() { return userId; }
    public void setUserId(int u) { this.userId = u; }
    public int getItemId() { return itemId; }
    public void setItemId(int i) { this.itemId = i; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int q) { this.quantity = q; }
    public boolean isEquipped() { return equipped; }
    public void setEquipped(boolean e) { this.equipped = e; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public String getItemType() { return itemType; }
    public void setItemType(String t) { this.itemType = t; }
    public int getAttackBonus() { return attackBonus; }
    public void setAttackBonus(int a) { this.attackBonus = a; }
    public int getDefenseBonus() { return defenseBonus; }
    public void setDefenseBonus(int d) { this.defenseBonus = d; }
    public int getHpRestore() { return hpRestore; }
    public void setHpRestore(int h) { this.hpRestore = h; }
    public int getMpRestore() { return mpRestore; }
    public void setMpRestore(int m) { this.mpRestore = m; }
    public int getGoldValue() { return goldValue; }
    public void setGoldValue(int g) { this.goldValue = g; }
    public String getRarity() { return rarity; }
    public void setRarity(String r) { this.rarity = r; }
    public String getIcon() { return icon; }
    public void setIcon(String i) { this.icon = i; }
}
