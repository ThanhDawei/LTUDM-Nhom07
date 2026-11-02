package com.quizzlive.server.dao;

import com.quizzlive.server.database.DatabaseManager;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DAO tương tác với bảng LOI_MOI_THACH_DAU
 * Phụ trách Yêu cầu 3: Thách đấu bạn bè 1v1 trực tiếp
 * Hỗ trợ tự động fallback sang bộ nhớ In-Memory khi CSDL được tinh gọn chuẩn 3NF.
 */
public class ChallengeDAO {
    private static final Map<Long, String> inMemoryStatus = new ConcurrentHashMap<>();
    private static final AtomicLong idSeq = new AtomicLong(100);

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
     * Tạo bản ghi lời mời thách đấu mới trong CSDL hoặc lưu RAM
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
                    long id = rs.getLong(1);
                    inMemoryStatus.put(id, status != null ? status : "PENDING");
                    return id;
                }
            }
        } catch (SQLException e) {
            long id = idSeq.incrementAndGet();
            inMemoryStatus.put(id, status != null ? status : "PENDING");
            return id;
        }
        long id = idSeq.incrementAndGet();
        inMemoryStatus.put(id, status != null ? status : "PENDING");
        return id;
    }

    /**
     * Cập nhật trạng thái phản hồi của lời mời thách đấu (ACCEPTED, REJECTED, EXPIRED)
     */
    public void updateInvitationStatus(long invitationId, String status, Long matchId) {
        inMemoryStatus.put(invitationId, status);
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
        } catch (SQLException ignored) {}
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
        } catch (SQLException ignored) {}
        return inMemoryStatus.get(invitationId);
    }
}
