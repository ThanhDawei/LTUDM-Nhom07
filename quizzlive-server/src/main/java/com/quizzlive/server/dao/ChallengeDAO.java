package com.quizzlive.server.dao;

import com.quizzlive.server.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO tương tác với bảng LOI_MOI_THACH_DAU
 * Phụ trách Yêu cầu 3: Thách đấu bạn bè 1v1 trực tiếp
 */
public class ChallengeDAO {

    public static class ChallengeRecord {
        public long invitationId;
        public Long matchId;
        public long senderId;
        public long targetId;
        public String status;
        public Timestamp sentAt;
        public Timestamp respondedAt;
    }

    /**
     * Tạo bản ghi lời mời thách đấu mới trong CSDL
     */
    public long saveInvitation(long senderId, long targetId, String status) {
        String sql = "INSERT INTO LOI_MOI_THACH_DAU (NguoiGui, NguoiNhan, TrangThai, ThoiGianGui) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, senderId);
            ps.setLong(2, targetId);
            ps.setString(3, status != null ? status : "PENDING");
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Cập nhật trạng thái phản hồi của lời mời thách đấu (ACCEPTED, REJECTED, EXPIRED)
     */
    public void updateInvitationStatus(long invitationId, String status, Long matchId) {
        String sql = "UPDATE LOI_MOI_THACH_DAU SET TrangThai = ?, MaTran = ?, ThoiGianPhanHoi = ? WHERE MaLoiMoi = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (matchId != null) {
                ps.setLong(2, matchId);
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setLong(4, invitationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateInvitationStatus(long invitationId, String status) {
        updateInvitationStatus(invitationId, status, null);
    }

    /**
     * Lấy trạng thái của lời mời thách đấu
     */
    public String getInvitationStatus(long invitationId) {
        String sql = "SELECT TrangThai FROM LOI_MOI_THACH_DAU WHERE MaLoiMoi = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, invitationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("TrangThai");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

