package com.dungeon.model;

public class PlayerStats {
    private int id, userId, currentRoom;
    private int hp, maxHp, mp, maxMp, attack, defense;
    private int level, xp, xpNext, gold, kills, deaths;
    private boolean inCombat;
    private Integer currentMonsterId;
    private String roomName, username;

    public static int xpForLevel(int level) {
        return (int)(100 * Math.pow(1.5, level - 1));
    }

    public int xpPercent() {
        int base = xpForLevel(level);
        int range = xpNext - base;
        if (range <= 0) return 0;
        return Math.max(0, Math.min(100, (int)(((double)(xp - base) / range) * 100)));
    }

    public int hpPercent() {
        return maxHp == 0 ? 0 : Math.max(0, Math.min(100, (int)((double)hp / maxHp * 100)));
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int u) { this.userId = u; }
    public int getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(int r) { this.currentRoom = r; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = Math.max(0, hp); }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int m) { this.maxHp = m; }
    public int getMp() { return mp; }
    public void setMp(int mp) { this.mp = Math.max(0, mp); }
    public int getMaxMp() { return maxMp; }
    public void setMaxMp(int m) { this.maxMp = m; }
    public int getAttack() { return attack; }
    public void setAttack(int a) { this.attack = a; }
    public int getDefense() { return defense; }
    public void setDefense(int d) { this.defense = d; }
    public int getLevel() { return level; }
    public void setLevel(int l) { this.level = l; }
    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }
    public int getXpNext() { return xpNext; }
    public void setXpNext(int x) { this.xpNext = x; }
    public int getGold() { return gold; }
    public void setGold(int g) { this.gold = g; }
    public int getKills() { return kills; }
    public void setKills(int k) { this.kills = k; }
    public int getDeaths() { return deaths; }
    public void setDeaths(int d) { this.deaths = d; }
    public boolean isInCombat() { return inCombat; }
    public void setInCombat(boolean b) { this.inCombat = b; }
    public Integer getCurrentMonsterId() { return currentMonsterId; }
    public void setCurrentMonsterId(Integer m) { this.currentMonsterId = m; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String r) { this.roomName = r; }
    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }
}
