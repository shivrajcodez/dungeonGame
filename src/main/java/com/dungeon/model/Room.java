package com.dungeon.model;

public class Room {
    private int id, dangerLevel;
    private String name, description, roomType;
    private Integer northId, southId, eastId, westId;

    public String getExits() {
        StringBuilder sb = new StringBuilder();
        if (northId != null) sb.append("[N] ");
        if (southId != null) sb.append("[S] ");
        if (eastId  != null) sb.append("[E] ");
        if (westId  != null) sb.append("[W] ");
        return sb.toString().trim();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public Integer getNorthId() { return northId; }
    public void setNorthId(Integer n) { this.northId = n; }
    public Integer getSouthId() { return southId; }
    public void setSouthId(Integer s) { this.southId = s; }
    public Integer getEastId() { return eastId; }
    public void setEastId(Integer e) { this.eastId = e; }
    public Integer getWestId() { return westId; }
    public void setWestId(Integer w) { this.westId = w; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String t) { this.roomType = t; }
    public int getDangerLevel() { return dangerLevel; }
    public void setDangerLevel(int d) { this.dangerLevel = d; }
}
