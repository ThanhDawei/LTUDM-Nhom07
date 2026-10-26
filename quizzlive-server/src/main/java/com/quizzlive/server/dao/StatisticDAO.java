package com.quizzlive.server.dao;

import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.shared.dto.LeaderboardDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatisticDAO {

    public int getTotalUserCount() {
        String sql = "SELECT COUNT(*) FROM NGUOI_DUNG";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getOnlineUserCount() {
        String sql = "SELECT COUNT(*) FROM NGUOI_DUNG WHERE TrangThaiOnline = TRUE";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Yêu cầu 16: Người thắng nhiều nhất
    public List<LeaderboardDTO.LeaderboardItem> getTopWinners(int limit) {
        String sql = "SELECT u.MaNguoiDung, u.TenDangNhap, h.HoTen, h.TongSoTran, h.TongSoTranThang, " +
                "h.ChuoiThangHienTai, h.ChuoiThangDaiNhat, h.TongDiem, h.DiemIQ " +
                "FROM NGUOI_DUNG u " +
                "JOIN HO_SO_NGUOI_DUNG h ON u.MaNguoiDung = h.MaNguoiDung " +
                "WHERE u.TrangThai = 'ACTIVE' " +
                "ORDER BY h.TongSoTranThang DESC, h.TongDiem DESC LIMIT ?";
        return queryLeaderboard(sql, limit);
    }

    // Yêu cầu 16: Chuỗi thắng dài nhất
    public List<LeaderboardDTO.LeaderboardItem> getTopStreaks(int limit) {
        String sql = "SELECT u.MaNguoiDung, u.TenDangNhap, h.HoTen, h.TongSoTran, h.TongSoTranThang, " +
                "h.ChuoiThangHienTai, h.ChuoiThangDaiNhat, h.TongDiem, h.DiemIQ " +
                "FROM NGUOI_DUNG u " +
                "JOIN HO_SO_NGUOI_DUNG h ON u.MaNguoiDung = h.MaNguoiDung " +
                "WHERE u.TrangThai = 'ACTIVE' " +
                "ORDER BY h.ChuoiThangDaiNhat DESC LIMIT ?";
        return queryLeaderboard(sql, limit);
    }

    // Yêu cầu 16: Người tham gia nhiều trận nhất
    public List<LeaderboardDTO.LeaderboardItem> getMostPlayed(int limit) {
        String sql = "SELECT u.MaNguoiDung, u.TenDangNhap, h.HoTen, h.TongSoTran, h.TongSoTranThang, " +
                "h.ChuoiThangHienTai, h.ChuoiThangDaiNhat, h.TongDiem, h.DiemIQ " +
                "FROM NGUOI_DUNG u " +
                "JOIN HO_SO_NGUOI_DUNG h ON u.MaNguoiDung = h.MaNguoiDung " +
                "WHERE u.TrangThai = 'ACTIVE' " +
                "ORDER BY h.TongSoTran DESC LIMIT ?";
        return queryLeaderboard(sql, limit);
    }

    private List<LeaderboardDTO.LeaderboardItem> queryLeaderboard(String sql, int limit) {
        List<LeaderboardDTO.LeaderboardItem> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    LeaderboardDTO.LeaderboardItem item = new LeaderboardDTO.LeaderboardItem();
                    item.rank = rank++;
                    item.userId = rs.getLong("MaNguoiDung");
                    item.username = rs.getString("TenDangNhap");
                    item.fullName = rs.getString("HoTen");
                    item.totalMatches = rs.getInt("TongSoTran");
                    item.winMatches = rs.getInt("TongSoTranThang");
                    item.winRate = item.totalMatches > 0 ? (double) item.winMatches / item.totalMatches * 100 : 0.0;
                    item.currentStreak = rs.getInt("ChuoiThangHienTai");
                    item.maxStreak = rs.getInt("ChuoiThangDaiNhat");
                    item.totalPoints = rs.getLong("TongDiem");
                    item.iqScore = rs.getDouble("DiemIQ");
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
