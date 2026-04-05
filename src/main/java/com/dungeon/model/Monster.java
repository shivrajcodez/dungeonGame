package com.dungeon.model;

public class Monster {
    private int id, hp, maxHp, attack, defense, xpReward, goldReward, minLevel;
    private String name, description, icon;
    private Integer lootItemId;
    private double lootChance;

    public int hpPercent() {
        return maxHp == 0 ? 0 : Math.max(0, Math.min(100, (int)((double)hp / maxHp * 100)));
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int m) { this.maxHp = m; }
    public int getAttack() { return attack; }
    public void setAttack(int a) { this.attack = a; }
    public int getDefense() { return defense; }
    public void setDefense(int d) { this.defense = d; }
    public int getXpReward() { return xpReward; }
    public void setXpReward(int x) { this.xpReward = x; }
    public int getGoldReward() { return goldReward; }
    public void setGoldReward(int g) { this.goldReward = g; }
    public int getMinLevel() { return minLevel; }
    public void setMinLevel(int m) { this.minLevel = m; }
    public Integer getLootItemId() { return lootItemId; }
    public void setLootItemId(Integer l) { this.lootItemId = l; }
    public double getLootChance() { return lootChance; }
    public void setLootChance(double l) { this.lootChance = l; }
    public String getIcon() { return icon; }
    public void setIcon(String i) { this.icon = i; }
}
