package com.quizzlive.server.dao;

import com.quizzlive.server.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OtpDAO {

    public void saveOtp(Long userId, String otpCode, String purpose, int expireMinutes) {
        String sql = "INSERT INTO OTP (MaNguoiDung, MaXacThuc, MucDich, ThoiGianHetHan, ThoiGianTao) " +
                "VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE), NOW())";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, otpCode);
            ps.setString(3, purpose);
            ps.setInt(4, expireMinutes);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean verifyOtp(Long userId, String otpCode, String purpose) {
        String sql = "SELECT MaOTP FROM OTP WHERE MaNguoiDung = ? AND MaXacThuc = ? AND MucDich = ? " +
                "AND ThoiGianHetHan > NOW() AND ThoiGianXacThuc IS NULL ORDER BY ThoiGianTao DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, otpCode);
            ps.setString(3, purpose);
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
        String sql = "UPDATE OTP SET ThoiGianXacThuc = NOW() WHERE MaOTP = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, otpId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
