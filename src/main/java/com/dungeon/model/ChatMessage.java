package com.dungeon.model;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private long id;
    private int userId;
    private String username, message, channel;
    private LocalDateTime createdAt;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int u) { this.userId = u; }
    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }
    public String getMessage() { return message; }
    public void setMessage(String m) { this.message = m; }
    public String getChannel() { return channel; }
    public void setChannel(String c) { this.channel = c; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime d) { this.createdAt = d; }
    public String getTimeFormatted() { return createdAt == null ? "" : createdAt.format(FMT); }
}
