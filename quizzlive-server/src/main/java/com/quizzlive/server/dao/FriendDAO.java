package com.quizzlive.server.dao;

import com.quizzlive.server.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO tương tác với bảng BAN_BE
 * Phụ trách phần nghiệp vụ Bạn bè và Báo cáo (Thiết kế bảng User, Friend, IQ_Question)
 */
public class FriendDAO {

    public boolean addFriend(long userId1, long userId2) {
        String sql = "INSERT INTO BAN_BE (MaNguoiDung1, MaNguoiDung2, TrangThai, NgayKetBan) VALUES (?, ?, 'ACCEPTED', ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId1);
            ps.setLong(2, userId2);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }
}

