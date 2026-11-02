package com.quizzlive.server.dao;

import com.quizzlive.server.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAO tương tác với bảng BAN_BE
 * Phụ trách phần nghiệp vụ Bạn bè và Báo cáo (Thiết kế bảng User, Friend, IQ_Question).
 * Hỗ trợ tự động fallback sang bộ nhớ In-Memory khi CSDL được tinh gọn chuẩn 3NF.
 */
public class FriendDAO {
    private static final Set<String> inMemoryFriends = ConcurrentHashMap.newKeySet();

    public boolean addFriend(long userId1, long userId2) {
        String sql = "INSERT INTO BAN_BE (MaNguoiDung1, MaNguoiDung2, TrangThai, NgayKetBan) VALUES (?, ?, 'ACCEPTED', ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId1);
            ps.setLong(2, userId2);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            boolean ok = ps.executeUpdate() > 0;
            if (ok) inMemoryFriends.add(userId1 + "_" + userId2);
            return ok;
        } catch (SQLException e) {
            inMemoryFriends.add(userId1 + "_" + userId2);
            return true;
        }
    }

    public boolean isFriend(long userId1, long userId2) {
        String sql = "SELECT 1 FROM BAN_BE WHERE ((MaNguoiDung1 = ? AND MaNguoiDung2 = ?) OR (MaNguoiDung1 = ? AND MaNguoiDung2 = ?)) AND TrangThai = 'ACCEPTED'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId1);
            ps.setLong(2, userId2);
            ps.setLong(3, userId2);
            ps.setLong(4, userId1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return true;
            }
        } catch (SQLException ignored) {}
        return inMemoryFriends.contains(userId1 + "_" + userId2) || inMemoryFriends.contains(userId2 + "_" + userId1);
    }

    public List<Long> getFriendIds(long userId) {
        List<Long> friends = new ArrayList<>();
        String sql = "SELECT CASE WHEN MaNguoiDung1 = ? THEN MaNguoiDung2 ELSE MaNguoiDung1 END AS FriendId " +
                     "FROM BAN_BE WHERE (MaNguoiDung1 = ? OR MaNguoiDung2 = ?) AND TrangThai = 'ACCEPTED'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, userId);
            ps.setLong(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    friends.add(rs.getLong("FriendId"));
                }
            }
        } catch (SQLException ignored) {}

        for (String pair : inMemoryFriends) {
            String[] parts = pair.split("_");
            if (parts.length == 2) {
                try {
                    long u1 = Long.parseLong(parts[0]);
                    long u2 = Long.parseLong(parts[1]);
                    if (u1 == userId && !friends.contains(u2)) friends.add(u2);
                    else if (u2 == userId && !friends.contains(u1)) friends.add(u1);
                } catch (NumberFormatException ignored) {}
            }
        }
        return friends;
    }
}
