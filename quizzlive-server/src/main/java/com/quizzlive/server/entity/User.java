package com.quizzlive.server.entity;

import java.sql.Timestamp;

public class User {
    private Long userId;
    private String username;
    private String salt;
    private String hash;
    private String email;
    private String fullName;
    private String avatar;
    private String bio;
    private String status; // ACTIVE, BLOCKED, PENDING
    private boolean isOnline;
    private Timestamp lastLogin;
    private Timestamp createdAt;

    public User() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    // Tương thích ngược với các hàm gọi cũ
    public String getPassword() { return hash; }
    public void setPassword(String password) { this.hash = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
