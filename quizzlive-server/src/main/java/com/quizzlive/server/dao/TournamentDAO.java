package com.quizzlive.server.dao;

import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.shared.dto.TournamentDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournamentDAO {
    /**
     * 1. Lấy danh sách các giải đấu đang hoặc sắp diễn ra (UPCOMING, ONGOING)
     * Kèm theo số lượng người chơi đã đăng ký thực tế (COUNT)
     */
    public List<TournamentDTO.TournamentItem> getActiveTournaments() {
        List<TournamentDTO.TournamentItem> list = new ArrayList<>();
        String sql = "SELECT g.MaGiai, g.TenGiai, g.MoTa, g.TrangThai, g.SoNguoiToiDa, g.ThoiGianBatDau, " +
                "COUNT(xh.MaNguoiDung) AS SoNguoiHienTai " +
                "FROM GIAI_DAU g " +
                "LEFT JOIN XEP_HANG_GIAI xh ON g.MaGiai = xh.MaGiai " +
                "WHERE g.TrangThai IN ('UPCOMING', 'ONGOING') " +
                "GROUP BY g.MaGiai, g.TenGiai, g.MoTa, g.TrangThai, g.SoNguoiToiDa, g.ThoiGianBatDau " +
                "ORDER BY g.MaGiai DESC";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TournamentDTO.TournamentItem item = new TournamentDTO.TournamentItem();
                item.tournamentId = rs.getLong("MaGiai");
                item.title = rs.getString("TenGiai");
                item.description = rs.getString("MoTa");
                item.status = rs.getString("TrangThai");
                item.maxParticipants = rs.getInt("SoNguoiToiDa");
                item.currentParticipants = rs.getInt("SoNguoiHienTai");
                Timestamp start = rs.getTimestamp("ThoiGianBatDau");
                item.startTime = start != null ? start.toString() : "Chưa xác định";
                list.add(item);
            }
        } catch (SQLException e) {
            System.err.println("[TournamentDAO] Lỗi getActiveTournaments: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 2. Kiểm tra xem người dùng đã đăng ký tham gia giải này chưa
     */
    public boolean isUserRegistered(long tournamentId, long userId) {
        String sql = "SELECT 1 FROM XEP_HANG_GIAI WHERE MaGiai = ? AND MaNguoiDung = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tournamentId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[TournamentDAO] Lỗi isUserRegistered: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 3. Đăng ký người chơi vào giải đấu (lưu vào bảng XEP_HANG_GIAI)
     */
    public boolean registerParticipant(long tournamentId, long userId) {
        String sql = "INSERT INTO XEP_HANG_GIAI (MaGiai, MaNguoiDung, BangDau, HatGiong, SoTran, SoTranThang, SoTranHoa, SoTranThua, TongDiem, ViTri, TrangThai) "
                +
                "VALUES (?, ?, 'Chưa xếp bảng', 0, 0, 0, 0, 0, 0, 0, 'CONFIRMED')";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tournamentId);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TournamentDAO] Lỗi registerParticipant: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 4. Lấy Bảng xếp hạng của một giải đấu kèm tên hiển thị (Username)
     */
    public List<TournamentDTO.StandingItem> getTournamentStandings(long tournamentId) {
        List<TournamentDTO.StandingItem> list = new ArrayList<>();
        String sql = "SELECT xh.MaNguoiDung, u.TenDangNhap, xh.BangDau, xh.SoTran, xh.SoTranThang, xh.TongDiem " +
                "FROM XEP_HANG_GIAI xh " +
                "JOIN NGUOI_DUNG u ON xh.MaNguoiDung = u.MaNguoiDung " +
                "WHERE xh.MaGiai = ? " +
                "ORDER BY xh.TongDiem DESC, xh.SoTranThang DESC, xh.SoTran ASC";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    TournamentDTO.StandingItem item = new TournamentDTO.StandingItem();
                    item.rank = rank++;
                    item.username = rs.getString("TenDangNhap");
                    String gName = rs.getString("BangDau");
                    item.groupName = (gName != null && !gName.isEmpty()) ? gName : "Vòng bảng";
                    item.matchesPlayed = rs.getInt("SoTran");
                    item.wins = rs.getInt("SoTranThang");
                    item.points = rs.getInt("TongDiem");
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("[TournamentDAO] Lỗi getTournamentStandings: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 5. Lấy danh sách ID thí sinh đã đăng ký giải để chuẩn bị bốc thăm
     */
    public List<Long> getParticipantUserIds(long tournamentId) {
        List<Long> userIds = new ArrayList<>();
        String sql = "SELECT MaNguoiDung FROM XEP_HANG_GIAI WHERE MaGiai = ? ORDER BY ThoiGianDangKy ASC";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getLong("MaNguoiDung"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TournamentDAO] Lỗi getParticipantUserIds: " + e.getMessage());
            e.printStackTrace();
        }
        return userIds;
    }

    /**
     * 6. Cập nhật bảng đấu cho thí sinh sau khi bốc thăm (ví dụ: Bảng A, Bảng B)
     */
    public void updateParticipantGroup(long tournamentId, long userId, String groupName) {
        String sql = "UPDATE XEP_HANG_GIAI SET BangDau = ? WHERE MaGiai = ? AND MaNguoiDung = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupName);
            ps.setLong(2, tournamentId);
            ps.setLong(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[TournamentDAO] Lỗi updateParticipantGroup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 7. Cập nhật kết quả trận đấu giải: Thắng +3 điểm, Hòa +1 điểm, Thua 0 điểm
     */
    public void updateStandingMatchResult(long tournamentId, long userId, boolean isWin, boolean isDraw) {
        int pointEarned = isWin ? 3 : (isDraw ? 1 : 0);
        String sql = "UPDATE XEP_HANG_GIAI SET " +
                "SoTran = SoTran + 1, " +
                "SoTranThang = SoTranThang + ?, " +
                "SoTranHoa = SoTranHoa + ?, " +
                "SoTranThua = SoTranThua + ?, " +
                "TongDiem = TongDiem + ? " +
                "WHERE MaGiai = ? AND MaNguoiDung = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, isWin ? 1 : 0);
            ps.setInt(2, isDraw ? 1 : 0);
            ps.setInt(3, (!isWin && !isDraw) ? 1 : 0);
            ps.setInt(4, pointEarned);
            ps.setLong(5, tournamentId);
            ps.setLong(6, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[TournamentDAO] Lỗi updateStandingMatchResult: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
