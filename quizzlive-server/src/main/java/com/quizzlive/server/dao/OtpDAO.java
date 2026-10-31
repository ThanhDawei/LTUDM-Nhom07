package com.quizzlive.server.dao;

import com.quizzlive.server.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Tầng truy xuất dữ liệu mã xác thực OTP trong bảng OTP.
 * Phụ trách Yêu cầu 1: "Xác thực email bằng mã OTP (thời hạn 10 phút) trước khi kích hoạt tài khoản."
 */
public class OtpDAO {

    public void saveOtp(Long userId, String otpCode, String purpose, int expireMinutes) {
        // Hủy các mã OTP cũ chưa xác thực của user cho mục đích này trước khi tạo mã mới
        invalidatePendingOtps(userId, purpose);

        String sql = "INSERT INTO OTP (MaNguoiDung, MaXacThuc, MucDich, ThoiGianHetHan, ThoiGianTao) " +
                "VALUES (?, ?, ?, ?, ?)";
        long nowMillis = System.currentTimeMillis();
        Timestamp now = new Timestamp(nowMillis);
        Timestamp expireTime = new Timestamp(nowMillis + (long) expireMinutes * 60 * 1000L);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, otpCode);
            ps.setString(3, purpose);
            ps.setTimestamp(4, expireTime);
            ps.setTimestamp(5, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean verifyOtp(Long userId, String otpCode, String purpose) {
        String sql = "SELECT MaOTP FROM OTP WHERE MaNguoiDung = ? AND MaXacThuc = ? AND MucDich = ? " +
                "AND ThoiGianHetHan > ? AND ThoiGianXacThuc IS NULL ORDER BY ThoiGianTao DESC LIMIT 1";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, otpCode);
            ps.setString(3, purpose);
            ps.setTimestamp(4, now);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long otpId = rs.getLong(1);
                    markVerified(otpId);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void markVerified(long otpId) {
        String sql = "UPDATE OTP SET ThoiGianXacThuc = ? WHERE MaOTP = ?";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, now);
            ps.setLong(2, otpId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void invalidatePendingOtps(Long userId, String purpose) {
        String sql = "UPDATE OTP SET ThoiGianXacThuc = ? WHERE MaNguoiDung = ? AND MucDich = ? AND ThoiGianXacThuc IS NULL";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, now);
            ps.setLong(2, userId);
            ps.setString(3, purpose);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
