package com.quizzlive.server.dao;

import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.shared.dto.LeaderboardDTO;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xử lý Bảng xếp hạng và Thống kê lịch sử thi đấu
 * Phụ trách Yêu cầu 10: Xếp hạng chung (thứ hạng, chuỗi thắng/thua, tỉ lệ thắng, lịch sử trận)
 */
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

    // Top người thắng nhiều nhất (Yêu cầu 10, 16)
    public List<LeaderboardDTO.LeaderboardItem> getTopWinners(int limit) {
        String sql = "SELECT u.MaNguoiDung, u.TenDangNhap, h.HoTen, h.TongSoTran, h.TongSoTranThang, h.TongSoTranThua, " +
                "h.ChuoiThangHienTai, h.ChuoiThangDaiNhat, h.TongDiem, h.DiemIQ " +
                "FROM NGUOI_DUNG u " +
                "JOIN HO_SO_NGUOI_DUNG h ON u.MaNguoiDung = h.MaNguoiDung " +
                "WHERE u.TrangThai = 'ACTIVE' " +
                "ORDER BY h.TongSoTranThang DESC, h.TongDiem DESC LIMIT ?";
        return queryLeaderboard(sql, limit);
    }

    // Top tổng điểm cao nhất
    public List<LeaderboardDTO.LeaderboardItem> getTopPoints(int limit) {
        String sql = "SELECT u.MaNguoiDung, u.TenDangNhap, h.HoTen, h.TongSoTran, h.TongSoTranThang, h.TongSoTranThua, " +
                "h.ChuoiThangHienTai, h.ChuoiThangDaiNhat, h.TongDiem, h.DiemIQ " +
                "FROM NGUOI_DUNG u " +
                "JOIN HO_SO_NGUOI_DUNG h ON u.MaNguoiDung = h.MaNguoiDung " +
                "WHERE u.TrangThai = 'ACTIVE' " +
                "ORDER BY h.TongDiem DESC, h.TongSoTranThang DESC LIMIT ?";
        return queryLeaderboard(sql, limit);
    }

    // Top chuỗi thắng dài nhất (Yêu cầu 10, 16)
    public List<LeaderboardDTO.LeaderboardItem> getTopStreaks(int limit) {
        String sql = "SELECT u.MaNguoiDung, u.TenDangNhap, h.HoTen, h.TongSoTran, h.TongSoTranThang, h.TongSoTranThua, " +
                "h.ChuoiThangHienTai, h.ChuoiThangDaiNhat, h.TongDiem, h.DiemIQ " +
                "FROM NGUOI_DUNG u " +
                "JOIN HO_SO_NGUOI_DUNG h ON u.MaNguoiDung = h.MaNguoiDung " +
                "WHERE u.TrangThai = 'ACTIVE' " +
                "ORDER BY h.ChuoiThangDaiNhat DESC, h.TongDiem DESC LIMIT ?";
        return queryLeaderboard(sql, limit);
    }

    // Top người tham gia nhiều trận nhất (Yêu cầu 10, 16)
    public List<LeaderboardDTO.LeaderboardItem> getMostPlayed(int limit) {
        String sql = "SELECT u.MaNguoiDung, u.TenDangNhap, h.HoTen, h.TongSoTran, h.TongSoTranThang, h.TongSoTranThua, " +
                "h.ChuoiThangHienTai, h.ChuoiThangDaiNhat, h.TongDiem, h.DiemIQ " +
                "FROM NGUOI_DUNG u " +
                "JOIN HO_SO_NGUOI_DUNG h ON u.MaNguoiDung = h.MaNguoiDung " +
                "WHERE u.TrangThai = 'ACTIVE' " +
                "ORDER BY h.TongSoTran DESC, h.TongSoTranThang DESC LIMIT ?";
        return queryLeaderboard(sql, limit);
    }

    /**
     * Lấy thông số thành tích cá nhân của người chơi (Thứ hạng, chuỗi thắng/thua, tỉ lệ thắng) (Yêu cầu 10)
     */
    public LeaderboardDTO.LeaderboardItem getUserStats(long userId) {
        String sql = "SELECT u.MaNguoiDung, u.TenDangNhap, h.HoTen, h.TongSoTran, h.TongSoTranThang, h.TongSoTranThua, " +
                "h.ChuoiThangHienTai, h.ChuoiThangDaiNhat, h.TongDiem, h.DiemIQ " +
                "FROM NGUOI_DUNG u " +
                "JOIN HO_SO_NGUOI_DUNG h ON u.MaNguoiDung = h.MaNguoiDung " +
                "WHERE u.MaNguoiDung = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LeaderboardDTO.LeaderboardItem item = new LeaderboardDTO.LeaderboardItem();
                    item.userId = rs.getLong("MaNguoiDung");
                    item.username = rs.getString("TenDangNhap");
                    item.fullName = rs.getString("HoTen");
                    item.totalMatches = rs.getInt("TongSoTran");
                    item.winMatches = rs.getInt("TongSoTranThang");
                    item.loseMatches = rs.getInt("TongSoTranThua");
                    item.drawMatches = Math.max(0, item.totalMatches - item.winMatches - item.loseMatches);
                    item.winRate = item.totalMatches > 0 ? (double) item.winMatches / item.totalMatches * 100.0 : 0.0;
                    item.currentStreak = rs.getInt("ChuoiThangHienTai");
                    item.maxStreak = rs.getInt("ChuoiThangDaiNhat");
                    item.totalPoints = rs.getLong("TongDiem");
                    item.iqScore = rs.getDouble("DiemIQ");

                    // Tính thứ hạng hiện tại của người chơi trên bảng tổng điểm
                    String rankSql = "SELECT COUNT(*) + 1 AS UserRank FROM HO_SO_NGUOI_DUNG h2 " +
                            "JOIN NGUOI_DUNG u2 ON h2.MaNguoiDung = u2.MaNguoiDung " +
                            "WHERE u2.TrangThai = 'ACTIVE' AND (h2.TongDiem > ? OR (h2.TongDiem = ? AND h2.TongSoTranThang > ?))";
                    try (PreparedStatement psRank = conn.prepareStatement(rankSql)) {
                        psRank.setLong(1, item.totalPoints);
                        psRank.setLong(2, item.totalPoints);
                        psRank.setInt(3, item.winMatches);
                        try (ResultSet rsRank = psRank.executeQuery()) {
                            if (rsRank.next()) {
                                item.rank = rsRank.getInt("UserRank");
                            } else {
                                item.rank = 1;
                            }
                        }
                    }
                    return item;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tổng hợp toàn bộ dữ liệu Bảng xếp hạng trả về cho Client kèm thống kê của người chơi hiện tại (Yêu cầu 10)
     */
    public LeaderboardDTO.LeaderboardResponse getLeaderboardResponse(Long currentUserId, int limit) {
        LeaderboardDTO.LeaderboardResponse resp = new LeaderboardDTO.LeaderboardResponse();
        resp.topPlayers = getTopPoints(limit);
        if (resp.topPlayers.isEmpty()) {
            resp.topPlayers = getTopWinners(limit);
        }
        resp.topStreaks = getTopStreaks(limit);
        resp.mostPlayed = getMostPlayed(limit);
        if (currentUserId != null && currentUserId > 0) {
            resp.myStats = getUserStats(currentUserId);
        }
        return resp;
    }

    public LeaderboardDTO.LeaderboardResponse getLeaderboardResponse(int limit) {
        return getLeaderboardResponse(null, limit);
    }

    /**
     * Lưu thông tin trận đấu và chi tiết điểm số của từng người chơi vào CSDL (Yêu cầu 8, 10)
     */
    public void saveMatchRecord(long matchId, String matchCode, long p1Id, long p2Id,
                                int p1Score, int p2Score, Long winnerId,
                                Integer topicId, String difficulty) {
        String sqlTran = "INSERT INTO TRAN_DAU (MaTran, MaTranCode, LoaiTran, MaChuDe, DoKho, TrangThai, ThoiGianBatDau, ThoiGianKetThuc, NguoiThang) " +
                "VALUES (?, ?, 'RANKED', ?, ?, 'FINISHED', ?, ?, ?)";
        String sqlNguoiChoi = "INSERT INTO NGUOI_CHOI_TRAN (MaTran, MaNguoiDung, ViTri, Diem, XepHang, ThoiGianHoanThanh) VALUES (?, ?, ?, ?, ?, ?)";

        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp startTime = new Timestamp(System.currentTimeMillis() - 60000);

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlTran)) {
                ps.setLong(1, matchId);
                ps.setString(2, matchCode != null ? matchCode : "MATCH_" + matchId);
                if (topicId != null && topicId > 0) ps.setInt(3, topicId);
                else ps.setNull(3, Types.INTEGER);
                ps.setString(4, difficulty != null ? difficulty : "MEDIUM");
                ps.setTimestamp(5, startTime);
                ps.setTimestamp(6, now);
                if (winnerId != null) ps.setLong(7, winnerId);
                else ps.setNull(7, Types.BIGINT);
                ps.executeUpdate();
            }

            int p1Rank = (winnerId != null && winnerId == p1Id) ? 1 : ((winnerId != null && winnerId == p2Id) ? 2 : 0);
            int p2Rank = (winnerId != null && winnerId == p2Id) ? 1 : ((winnerId != null && winnerId == p1Id) ? 2 : 0);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlNguoiChoi)) {
                ps1.setLong(1, matchId);
                ps1.setLong(2, p1Id);
                ps1.setInt(3, 1);
                ps1.setInt(4, p1Score);
                ps1.setInt(5, p1Rank);
                ps1.setTimestamp(6, now);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(sqlNguoiChoi)) {
                ps2.setLong(1, matchId);
                ps2.setLong(2, p2Id);
                ps2.setInt(3, 2);
                ps2.setInt(4, p2Score);
                ps2.setInt(5, p2Rank);
                ps2.setTimestamp(6, now);
                ps2.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Truy vấn lịch sử thi đấu cá nhân theo userId (Yêu cầu 10)
     */
    public List<LeaderboardDTO.MatchHistoryItem> getMatchHistory(long userId, int limit) {
        List<LeaderboardDTO.MatchHistoryItem> history = new ArrayList<>();
        String sql = "SELECT t.MaTran, t.MaTranCode, t.ThoiGianKetThuc, " +
                "p1.Diem AS MyScore, " +
                "COALESCE(p2.Diem, 0) AS OpponentScore, " +
                "COALESCE(u2.TenDangNhap, 'Đối thủ') AS OpponentUsername, " +
                "COALESCE(h2.HoTen, u2.TenDangNhap, 'Đối thủ') AS OpponentFullName, " +
                "COALESCE(c.TenChuDe, 'Tất cả chủ đề') AS TopicName, " +
                "COALESCE(t.DoKho, 'MEDIUM') AS Difficulty, " +
                "CASE " +
                "    WHEN p1.Diem > COALESCE(p2.Diem, 0) THEN 'WIN' " +
                "    WHEN p1.Diem < COALESCE(p2.Diem, 0) THEN 'LOSS' " +
                "    ELSE 'DRAW' " +
                "END AS Result " +
                "FROM NGUOI_CHOI_TRAN p1 " +
                "JOIN TRAN_DAU t ON p1.MaTran = t.MaTran " +
                "LEFT JOIN NGUOI_CHOI_TRAN p2 ON t.MaTran = p2.MaTran AND p2.MaNguoiDung != p1.MaNguoiDung " +
                "LEFT JOIN NGUOI_DUNG u2 ON p2.MaNguoiDung = u2.MaNguoiDung " +
                "LEFT JOIN HO_SO_NGUOI_DUNG h2 ON u2.MaNguoiDung = h2.MaNguoiDung " +
                "LEFT JOIN CHU_DE c ON t.MaChuDe = c.MaChuDe " +
                "WHERE p1.MaNguoiDung = ? " +
                "ORDER BY t.ThoiGianKetThuc DESC, t.MaTran DESC LIMIT ?";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, limit > 0 ? limit : 20);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LeaderboardDTO.MatchHistoryItem item = new LeaderboardDTO.MatchHistoryItem();
                    item.matchId = rs.getLong("MaTran");
                    item.matchCode = rs.getString("MaTranCode");
                    item.opponentUsername = rs.getString("OpponentUsername");
                    item.opponentFullName = rs.getString("OpponentFullName");
                    item.result = rs.getString("Result");
                    item.myScore = rs.getInt("MyScore");
                    item.opponentScore = rs.getInt("OpponentScore");
                    item.topicName = rs.getString("TopicName");
                    item.difficulty = rs.getString("Difficulty");
                    Timestamp ts = rs.getTimestamp("ThoiGianKetThuc");
                    item.playedAt = (ts != null) ? sdf.format(ts) : "Vừa xong";
                    history.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
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
                    item.loseMatches = rs.getInt("TongSoTranThua");
                    item.drawMatches = Math.max(0, item.totalMatches - item.winMatches - item.loseMatches);
                    item.winRate = item.totalMatches > 0 ? (double) item.winMatches / item.totalMatches * 100.0 : 0.0;
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
