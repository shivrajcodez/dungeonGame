package com.dungeon.model;
import java.time.LocalDateTime;

public class User {
    private int id;
    private String username, passwordHash, email;
    private LocalDateTime createdAt, lastLogin;
    private boolean online;

    public User() {}
    public User(int id, String username, String email) {
        this.id = id; this.username = username; this.email = email;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String p) { this.passwordHash = p; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime d) { this.createdAt = d; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime d) { this.lastLogin = d; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean o) { this.online = o; }
}
